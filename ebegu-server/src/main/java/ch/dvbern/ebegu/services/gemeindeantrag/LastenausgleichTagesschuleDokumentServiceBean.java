/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxMerger;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.PDFService;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
@Stateless
@Local(LastenausgleichTagesschuleDokumentService.class)
public class LastenausgleichTagesschuleDokumentServiceBean extends AbstractBaseService
	implements LastenausgleichTagesschuleDokumentService {

	@Inject
	LastenausgleichTagesschuleAngabenGemeindeService lastenausgleichTagesschuleAngabenGemeindeService;

	@Inject
	Authorizer authorizer;

	@Inject
	PrincipalBean principalBean;

	@Inject
	GemeindeService gemeindeService;

	@Inject
	EinstellungService einstellungService;

	@Inject
	GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private PDFService pdfService;

	@Override
	@Nonnull
	public byte[] createDocx(@Nonnull String containerId, @Nonnull Sprache sprache, @Nonnull BigDecimal betreuungsstundenPrognose) {
		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"createDocx",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);

		authorizer.checkReadAuthorization(container);

		final byte[] template = container.getGesuchsperiode().getVorlageVerfuegungLatsWithSprache(sprache);
		if (template.length == 0) {
			throw new EbeguRuntimeException(
				"createDocx",
				"LATS Template not found für Gesuchsperiode " + container.getGesuchsperiode().getGesuchsperiodeString() + " und Sprache " + sprache,
				ErrorCodeEnum.ERROR_LATS_VERFUEGUNG_TEMPLATE_NOT_FOUND,
				container.getGesuchsperiode().getGesuchsperiodeString(),
				sprache
			);
		}

		DocxDocument document = new DocxDocument(template);
		LatsDocxMerger merger = new LatsDocxMerger(document);
		merger.addMergeFields(toLatsDocxDTO(container, betreuungsstundenPrognose, sprache));
		merger.merge();
		return document.getDocument();
	}

	@Override
	public byte[] generateLATSReportDokument(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container,
		Sprache sprache) throws MergeDocException {
		Einstellung lohnnormkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, container.getGemeinde(), container.getGesuchsperiode());
		Einstellung lohnnormkostenLessThan50 = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, container.getGemeinde(), container.getGesuchsperiode());
		return pdfService.generateLATSReport(container, sprache, lohnnormkosten, lohnnormkostenLessThan50);

	}

	@Nonnull
	protected LatsDocxDTO toLatsDocxDTO(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container,
		@Nonnull BigDecimal betreuungsstundenPrognose,
		Sprache sprache
	) {

		Objects.requireNonNull(container.getAngabenKorrektur());
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde = container.getAngabenKorrektur();

		LatsDocxDTO dto = new LatsDocxDTO();
		dto.setUserName(this.principalBean.getBenutzer().getFullName());
		dto.setUserEmail(this.principalBean.getBenutzer().getEmail());

		GemeindeStammdaten stammdaten = this.gemeindeService.getGemeindeStammdatenByGemeindeId(container.getGemeinde().getId()).orElseThrow(() ->
			new EbeguEntityNotFoundException("toLatsDocxDTO", container.getGemeinde().getId())
		);

		//Wenn TSAdresse vorhanden, soll diese verwendet werden, sonst 'normale' Gemeinde Adresse
		Adresse adresseToUse = Optional.ofNullable(stammdaten.getTsAdresse())
				.orElse(stammdaten.getAdresse());

		dto.setGemeindeAnschrift(adresseToUse.getOrganisation());
		dto.setGemeindeStrasse(adresseToUse.getStrasse());
		dto.setGemeindeNr(adresseToUse.getHausnummer());
		dto.setGemeindePLZ(adresseToUse.getPlz());
		dto.setGemeindeOrt(adresseToUse.getOrt());
		dto.setGemeindeName(container.getGemeinde().getName());
		dto.setFallNummer(buildFallNummer(container, stammdaten));

		dto.setBetreuungsstunden(angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden());
		setNormlohnkosten(dto, angabenGemeinde, container, sprache);
		dto.setNormlohnkostenTotal(angabenGemeinde.getNormlohnkostenBetreuungBerechnet());
		dto.setElterngebuehren(angabenGemeinde.getEinnahmenElterngebuehren());
		dto.setLastenausgleichsberechtigterBetrag(angabenGemeinde.getLastenausgleichsberechtigerBetrag());

		calculateAndSetPrognoseValues(dto, angabenGemeinde, container, betreuungsstundenPrognose);
		calculateAndSetZahlungen(dto, angabenGemeinde);

		return dto;
	}

	private String buildFallNummer(
		LastenausgleichTagesschuleAngabenGemeindeContainer container,
		GemeindeStammdaten stammdaten) {
		String year = container.getGesuchsperiode().getGueltigkeit().getGueltigAb().format(DateTimeFormatter.ofPattern("yy"));
		return year + '.' + stammdaten.getGemeinde().getBfsNummer();
	}

	private void setNormlohnkosten(
		@Nonnull LatsDocxDTO dto,
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde,
		LastenausgleichTagesschuleAngabenGemeindeContainer container,
		Sprache sprache) {
		List<String> normLohnBetrag = new ArrayList<>();
		List<String> normLohnBetragPrognose = new ArrayList<>();
		List<String> normLohnText = new ArrayList<>();

		Gesuchsperiode periodeOfPrognose = gesuchsperiodeService
			.getNachfolgendeGesuchsperiode(container.getGesuchsperiode())
			.orElseThrow(() -> new EbeguEntityNotFoundException("setNormlohnkosten", "Nachfolgende Periode nicht defniniert"));

		if (angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() != null
		&& angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete().compareTo(BigDecimal.ZERO) > 0) {
			Einstellung lohnnormkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, container.getGemeinde(), container.getGesuchsperiode());
			Einstellung lohnnormkostenPrognose = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, container.getGemeinde(), periodeOfPrognose);
			normLohnBetrag.add(lohnnormkosten.getValue());
			normLohnBetragPrognose.add(lohnnormkostenPrognose.getValue());

			String text = ServerMessageUtil.getMessage(
				"lats_verfuegung_text_paedagogisch",
				sprache.getLocale(),
				Objects.requireNonNull(container.getGemeinde().getMandant()),
				container.getGemeinde().getName(),
				lohnnormkosten.getValue()
			);
			normLohnText.add(text);
		}
		if (angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() != null
			&& angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete().compareTo(BigDecimal.ZERO) > 0) {
			Einstellung lohnnormkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, container.getGemeinde(), container.getGesuchsperiode());
			Einstellung lohnnormkostenPrognose = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, container.getGemeinde(), periodeOfPrognose);
			normLohnBetrag.add(lohnnormkosten.getValue());
			normLohnBetragPrognose.add(lohnnormkostenPrognose.getValue());

			String text = ServerMessageUtil.getMessage(
				"lats_verfuegung_text_nicht_paedagogisch",
				sprache.getLocale(),
				Objects.requireNonNull(container.getGemeinde().getMandant()),
				container.getGemeinde().getName(),
				lohnnormkosten.getValue()
			);
			normLohnText.add(text);
		}
		String betragResult = String.join(" / ", normLohnBetrag);
		String betragResultPrognose = String.join(" / ", normLohnBetragPrognose);
		dto.setNormlohnkosten(betragResult);
		dto.setNormlohnkostenProg(betragResultPrognose);

		String textResult = String.join(" ODER ", normLohnText);
		dto.setTextPaedagogischOderNicht(textResult);
	}

	private void calculateAndSetPrognoseValues(
		@Nonnull LatsDocxDTO dto,
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde,
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container,
		@Nonnull BigDecimal betreuungsstundenPrognose
	) {

		Gesuchsperiode periodeOfPrognose = gesuchsperiodeService
			.getNachfolgendeGesuchsperiode(container.getGesuchsperiode())
			.orElseThrow(() -> new EbeguEntityNotFoundException("setNormlohnkosten", "Nachfolgende Periode nicht defniniert"));

		Einstellung lohnnormkostenMoreThan50 = einstellungService.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN,
			container.getGemeinde(),
			periodeOfPrognose
		);
		Einstellung lohnnormkostenLessThan50 = einstellungService.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
			container.getGemeinde(),
			periodeOfPrognose
		);

		dto.setBetreuungsstundenProg(betreuungsstundenPrognose);

		// falls in der aktuellen Periode nur die tieferen Normlohnkosten verwendet werden, dann werden auch diese für die
		// Prognose verwendet. Falls nur die höheren oder beide Normlohnkosten verwendet werden, dann werden auch die höheren
		// für die Prognose verwendet
		BigDecimal normlohnkosten;
		if (BigDecimal.ZERO.compareTo(angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete()) == 0) {
			normlohnkosten = lohnnormkostenLessThan50.getValueAsBigDecimal();
		} else {
			normlohnkosten = lohnnormkostenMoreThan50.getValueAsBigDecimal();
		}

		BigDecimal normlohnkostenProg = normlohnkosten.multiply(betreuungsstundenPrognose);
		dto.setNormlohnkostenTotalProg(MathUtil.ceilToFrankenRappen(normlohnkostenProg));

		final boolean islastenausgleichberechtigteBetreuungstundenZero =
			BigDecimal.ZERO.compareTo(angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()) == 0;

		BigDecimal proportion = islastenausgleichberechtigteBetreuungstundenZero ?
				BigDecimal.ZERO :
				MathUtil.EXACT.divide(
				betreuungsstundenPrognose,
				angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()
		);

		Objects.requireNonNull(angabenGemeinde.getEinnahmenElterngebuehren());
		// use proportional bigger elternbeitrag in following year
		var elterngebuehren = angabenGemeinde.getEinnahmenElterngebuehren().multiply(proportion);
		dto.setElterngebuehrenProg(MathUtil.ceilToFrankenRappen(elterngebuehren));

		Objects.requireNonNull(dto.getNormlohnkostenTotalProg());
		BigDecimal lastenausgleichBetragProg = dto.getNormlohnkostenTotalProg().subtract(dto.getElterngebuehrenProg());
		dto.setLastenausgleichsberechtigterBetragProg(MathUtil.roundUpToFranken(lastenausgleichBetragProg));

		// zweite rate following schuljahr is 50% of lastenausgleichberechtigter Betrag
		BigDecimal ersteRateProg = MathUtil.EXACT.multiply(dto.getLastenausgleichsberechtigterBetragProg(), new BigDecimal("0.5"));
		dto.setErsteRateProg(MathUtil.roundUpToFranken(ersteRateProg));
	}

	private void calculateAndSetZahlungen(@Nonnull LatsDocxDTO dto, LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde) {
		dto.setErsteRate(angabenGemeinde.getErsteRateAusbezahlt());

		Objects.requireNonNull(dto.getLastenausgleichsberechtigterBetrag());
		BigDecimal zweiteRate = dto.getLastenausgleichsberechtigterBetrag().subtract(dto.getErsteRate());
		dto.setZweiteRate(zweiteRate);

		Objects.requireNonNull(dto.getErsteRateProg());
		BigDecimal totalAuszahlung = dto.getErsteRateProg().add(dto.getZweiteRate());
		dto.setAuszahlungTotal(totalAuszahlung);
	}
}



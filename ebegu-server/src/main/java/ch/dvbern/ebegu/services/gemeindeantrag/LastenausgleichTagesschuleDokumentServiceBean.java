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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxMerger;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.Constants;
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

	@Nonnull
	private LatsDocxDTO toLatsDocxDTO(
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

		dto.setGemeindeAnschrift(stammdaten.getAdresse().getOrganisation());
		dto.setGemeindeStrasse(stammdaten.getAdresse().getStrasse());
		dto.setGemeindeNr(stammdaten.getAdresse().getHausnummer());
		dto.setGemeindePLZ(stammdaten.getAdresse().getPlz());
		dto.setGemeindeOrt(stammdaten.getAdresse().getOrt());
		dto.setDateToday(Constants.DATE_FORMATTER.format(LocalDate.now()));
		dto.setGemeindeName(container.getGemeinde().getName());

		dto.setBetreuungsstunden(angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden());
		setNormlohnkosten(dto, angabenGemeinde, container, sprache);
		dto.setNormlohnkostenTotal(angabenGemeinde.getNormlohnkostenBetreuungBerechnet());
		dto.setElterngebuehren(angabenGemeinde.getEinnahmenElterngebuehren());
		dto.setLastenausgleichsberechtigterBetrag(angabenGemeinde.getLastenausgleichsberechtigerBetrag());

		calculateAndSetPrognoseValues(dto, angabenGemeinde, betreuungsstundenPrognose);
		calculateAndSetZahlungen(dto, angabenGemeinde);

		return dto;
	}

	private void setNormlohnkosten(
		@Nonnull LatsDocxDTO dto,
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde,
		LastenausgleichTagesschuleAngabenGemeindeContainer container,
		Sprache sprache) {
		List<String> normLohnBetrag = new ArrayList<>();
		List<String> normLohnText = new ArrayList<>();

		if (angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() != null
		&& angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete().compareTo(BigDecimal.ZERO) > 0) {
			Einstellung lohnnormkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, container.getGemeinde(), container.getGesuchsperiode());
			normLohnBetrag.add(lohnnormkosten.getValue());

			String text = ServerMessageUtil.getMessage(
				"lats_verfuegung_text_paedagogisch",
				sprache.getLocale(),
				container.getGemeinde().getName(),
				lohnnormkosten.getValue()
			);
			normLohnText.add(text);
		}
		if (angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() != null
			&& angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete().compareTo(BigDecimal.ZERO) > 0) {
			Einstellung lohnnormkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, container.getGemeinde(), container.getGesuchsperiode());
			normLohnBetrag.add(lohnnormkosten.getValue());

			String text = ServerMessageUtil.getMessage(
				"lats_verfuegung_text_nicht_paedagogisch",
				sprache.getLocale(),
				container.getGemeinde().getName(),
				lohnnormkosten.getValue()
			);
			normLohnText.add(text);
		}
		String betragResult = String.join(" / ", normLohnBetrag);
		dto.setNormlohnkosten(betragResult);
		// same values used for following periode
		dto.setNormlohnkostenProg(betragResult);

		String textResult = String.join(" ODER ", normLohnText);
		dto.setTextPaedagogischOderNicht(textResult);
	}

	private void calculateAndSetPrognoseValues(
		@Nonnull LatsDocxDTO dto,
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde,
		@Nonnull BigDecimal betreuungsstundenPrognose
	) {

		dto.setBetreuungsstundenProg(betreuungsstundenPrognose);

		// for gemeinden with both normlohnkosten, use same distribution => use same calculated normlohnkosten
		BigDecimal normlohnkostenCalculated = MathUtil.EXACT.divide(
			angabenGemeinde.getNormlohnkostenBetreuungBerechnet(),
			angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()
			);

		Objects.requireNonNull(normlohnkostenCalculated);
		dto.setNormlohnkostenTotalProg(normlohnkostenCalculated.multiply(betreuungsstundenPrognose));

		BigDecimal proportion = MathUtil.EXACT.divide(
			betreuungsstundenPrognose,
			angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()
		);

		Objects.requireNonNull(angabenGemeinde.getEinnahmenElterngebuehren());
		// use proportional bigger elternbeitrag in following year
		dto.setElterngebuehrenProg(angabenGemeinde.getEinnahmenElterngebuehren().multiply(proportion));

		Objects.requireNonNull(dto.getNormlohnkostenTotalProg());
		BigDecimal lastenausgleichBetragProg = dto.getNormlohnkostenTotalProg().subtract(dto.getElterngebuehrenProg());
		dto.setLastenausgleichsberechtigterBetragProg(lastenausgleichBetragProg);

		// zweite rate following schuljahr is 50% of lastenausgleichberechtigter Betrag
		BigDecimal ersteRateProg = MathUtil.EXACT.multiply(dto.getLastenausgleichsberechtigterBetragProg(), new BigDecimal("0.5"));
		dto.setErsteRateProg(ersteRateProg);
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



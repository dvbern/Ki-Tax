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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.ferienbetreuung.FerienbetreuungDocxDTO;
import ch.dvbern.ebegu.docxmerger.ferienbetreuung.FerienbetreuungDocxMerger;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument_;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer die Dokumente der Ferienbetreuungen
 */
@Stateless
@Local(FerienbetreuungDokumentService.class)
public class FerienbetreuungDokumentServiceBean extends AbstractBaseService
	implements FerienbetreuungDokumentService {

	public static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	@Override
	public FerienbetreuungDokument saveDokument(@Nonnull FerienbetreuungDokument ferienbetreuungDokument) {
		Objects.requireNonNull(ferienbetreuungDokument);
		authorizer.checkWriteAuthorization(ferienbetreuungDokument.getFerienbetreuungAngabenContainer());

		return persistence.merge(ferienbetreuungDokument);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungDokument> findDokument(@Nonnull String dokumentId) {
		Objects.requireNonNull(dokumentId, ID_MUSS_GESETZT_SEIN);
		FerienbetreuungDokument dokument = persistence.find(FerienbetreuungDokument.class, dokumentId);
		authorizer.checkReadAuthorization(dokument.getFerienbetreuungAngabenContainer());
		return Optional.ofNullable(dokument);
	}

	@Override
	public void removeDokument(@Nonnull FerienbetreuungDokument dokument) {
		authorizer.checkWriteAuthorization(dokument.getFerienbetreuungAngabenContainer());
		persistence.remove(dokument);
	}

	@Nonnull
	@Override
	public List<FerienbetreuungDokument> findDokumente(@Nonnull String ferienbetreuungContainerId) {
		Objects.requireNonNull(ferienbetreuungContainerId);
		authorizer.checkReadAuthorizationFerienbetreuung(ferienbetreuungContainerId);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FerienbetreuungDokument> query = cb.createQuery(FerienbetreuungDokument.class);
		Root<FerienbetreuungDokument> root = query.from(FerienbetreuungDokument.class);

		ParameterExpression<String> containerIdParam = cb.parameter(String.class, "ferienbetreuungContainerId");

		Predicate predicateFerienbetreuungFormularId = cb.equal(root.get(FerienbetreuungDokument_.ferienbetreuungAngabenContainer).get(AbstractEntity_.id), containerIdParam);
		query.where(predicateFerienbetreuungFormularId);
		query.orderBy(cb.asc(root.get(FerienbetreuungDokument_.timestampUpload)));
		TypedQuery<FerienbetreuungDokument> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(containerIdParam, ferienbetreuungContainerId);

		return q.getResultList();
	}

	@Override
	@Nonnull
	public byte[] createDocx(@Nonnull String containerId, @Nonnull Sprache sprache) {
		FerienbetreuungAngabenContainer container =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"createDocx",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);

		authorizer.checkReadAuthorization(container);

		final byte[] template = container.getGesuchsperiode().getVorlageVerfuegungFerienbetreuungWithSprache(sprache);
		if (template.length == 0) {
			throw new EbeguRuntimeException(
				"createDocx",
				"Ferienbetreuung Template not found für Gesuchsperiode " + container.getGesuchsperiode().getGesuchsperiodeString() + " und Sprache " + sprache,
				ErrorCodeEnum.ERROR_FERIENBETREUUNG_VERFUEGUNG_TEMPLATE_NOT_FOUND,
				container.getGesuchsperiode().getGesuchsperiodeString(),
				sprache
			);
		}

		DocxDocument document = new DocxDocument(template);
		FerienbetreuungDocxMerger merger = new FerienbetreuungDocxMerger(document);
		merger.addMergeFields(toFerienbetreuungDocxDTO(container));
		merger.merge();
		return document.getDocument();
	}

	@Nonnull
	private FerienbetreuungDocxDTO toFerienbetreuungDocxDTO(@Nonnull FerienbetreuungAngabenContainer container) {
		Objects.requireNonNull(container.getAngabenKorrektur());
		FerienbetreuungAngaben angabenKorrektur = container.getAngabenKorrektur();

		FerienbetreuungDocxDTO dto = new FerienbetreuungDocxDTO();
		dto.setUserName(this.principalBean.getBenutzer().getFullName());
		dto.setUserEmail(this.principalBean.getBenutzer().getEmail());

		GemeindeStammdaten stammdaten = this.gemeindeService.getGemeindeStammdatenByGemeindeId(container.getGemeinde().getId()).orElseThrow(() ->
			new EbeguEntityNotFoundException("toFerienbetreuungDocxDTO", container.getGemeinde().getId())
		);

		dto.setGemeindeAnschrift(stammdaten.getAdresse().getOrganisation());
		dto.setGemeindeStrasse(stammdaten.getAdresse().getStrasse());
		dto.setGemeindeNr(stammdaten.getAdresse().getHausnummer());
		dto.setGemeindePLZ(stammdaten.getAdresse().getPlz());
		dto.setGemeindeOrt(stammdaten.getAdresse().getOrt());
		dto.setFallNummer(buildFallNummer(container, stammdaten));
		dto.setPeriode(container.getGesuchsperiode().getGesuchsperiodeString());
		dto.setAngebot(angabenKorrektur.getFerienbetreuungAngabenAngebot().getAngebot());
		var traegerschaft = angabenKorrektur.getFerienbetreuungAngabenStammdaten().getTraegerschaft() != null
			? angabenKorrektur.getFerienbetreuungAngabenStammdaten().getTraegerschaft()
			: container.getGemeinde().getName();
		dto.setTraegerschaft(traegerschaft);

		dto.setTotalTage(angabenKorrektur.getFerienbetreuungAngabenNutzung().getAnzahlBetreuungstageKinderBern());
		dto.setTageSonderschueler(angabenKorrektur.getFerienbetreuungAngabenNutzung().getAnzahlTageSonderschueler());
		dto.setTageOhneSonderschuelertage(angabenKorrektur.getFerienbetreuungAngabenNutzung().getAnzahlTageOhneSonderschueler());

		var pauschale = einstellungService.findEinstellung(
			EinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG,
			container.getGemeinde(),
			container.getGesuchsperiode()
		).getValueAsBigDecimal();

		var pauschaleSonderschueler = einstellungService.findEinstellung(
			EinstellungKey.FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER,
			container.getGemeinde(),
			container.getGesuchsperiode()
		).getValueAsBigDecimal();

		dto.setPauschale(pauschale);
		dto.setPauschaleSonderschueler(pauschaleSonderschueler);
		dto.setChfOhneSonderschueler(pauschale.multiply(dto.getTageOhneSonderschueler()));
		dto.setChfSonderschueler(pauschaleSonderschueler.multiply(dto.getTageSonderschueler()));

		dto.setTotalTage(angabenKorrektur.getFerienbetreuungAngabenNutzung().getAnzahlBetreuungstageKinderBern());
		Objects.requireNonNull(dto.getChfSonderschueler());
		dto.setTotalChf(dto.getChfSonderschueler().add(dto.getChfOhneSonderschueler()));

		Objects.requireNonNull(angabenKorrektur.getFerienbetreuungAngabenStammdaten().getAuszahlungsdaten());
		dto.setIban(angabenKorrektur.getFerienbetreuungAngabenStammdaten().getAuszahlungsdaten().getIban().getIban());

		return dto;
	}

	private String buildFallNummer(FerienbetreuungAngabenContainer container, GemeindeStammdaten stammdaten) {
		String year = container.getGesuchsperiode().getGueltigkeit().getGueltigAb().format(DateTimeFormatter.ofPattern("yy"));
		return year + '.' + stammdaten.getGemeinde().getBfsNummer();
	}
}



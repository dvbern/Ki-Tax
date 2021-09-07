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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer_;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;

/**
 * Service fuer die Ferienbetreuungen
 */
@Stateless
@Local(FerienbetreuungService.class)
public class FerienbetreuungServiceBean extends AbstractBaseService
	implements FerienbetreuungService {

	public static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";
	public static final String NOT_ALL_PROPERTIES_SET = "Not all required properties are set";

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principal;

	@Inject
	private FerienbetreuungDokumentService ferienbetreuungDokumentService;

	@Inject
	private EbeguConfiguration configuration;

	@Nonnull
	@Override
	public List<FerienbetreuungAngabenContainer> getFerienbetreuungAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status,
		@Nullable String timestampMutiert
	) {
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		Set<Predicate> predicates = new HashSet<>();
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<FerienbetreuungAngabenContainer> query = cb.createQuery(FerienbetreuungAngabenContainer.class);
		Root<FerienbetreuungAngabenContainer> root = query.from(FerienbetreuungAngabenContainer.class);

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
				root.get(FerienbetreuungAngabenContainer_.gemeinde).in(gemeinden);
			predicates.add(gemeindeIn);
		}

		if (gemeinde != null) {
			predicates.add(
				cb.equal(
					root.get(FerienbetreuungAngabenContainer_.gemeinde).get(Gemeinde_.name),
					gemeinde)
			);
		}
		if (periode != null) {
			predicates.add(PredicateHelper.getPredicateFilterGesuchsperiode(cb,
				root.join(FerienbetreuungAngabenContainer_.gesuchsperiode, JoinType.INNER),
				periode));
		}
		if (status != null) {
			if (!EnumUtil.isOneOf(status, FerienbetreuungAngabenStatus.values())) {
				return new ArrayList<>();
			}

			predicates.add(
				cb.equal(
					root.get(FerienbetreuungAngabenContainer_.status),
					FerienbetreuungAngabenStatus.valueOf(status))
			);
		}
		if (timestampMutiert != null) {
			Predicate timestampMutiertPredicate = createTimestampMutiertPredicate(timestampMutiert, cb, root);
			predicates.add(timestampMutiertPredicate);
		}

		Predicate[] predicateArray = new Predicate[predicates.size()];
		query.where(predicates.toArray(predicateArray));

		return persistence.getCriteriaResults(query);
	}

	private Predicate createTimestampMutiertPredicate(
		@Nonnull String timestampMutiert,
		CriteriaBuilder cb,
		Root<FerienbetreuungAngabenContainer> root) {

		Predicate timestampMutiertPredicate;
		try {
			// Wir wollen ohne Zeit vergleichen
			Expression<LocalDate> timestampAsLocalDate =
				root.get(FerienbetreuungAngabenContainer_.timestampMutiert).as(LocalDate.class);
			LocalDate searchDate = LocalDate.parse(timestampMutiert, Constants.DATE_FORMATTER);
			timestampMutiertPredicate = cb.equal(timestampAsLocalDate, searchDate);
		} catch (DateTimeParseException e) {
			// no valid date. we return false, since no antrag should be found
			timestampMutiertPredicate = cb.disjunction();
		}
		return timestampMutiertPredicate;
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenContainer(@Nonnull String containerId) {
		Objects.requireNonNull(containerId, ID_MUSS_GESETZT_SEIN);

		FerienbetreuungAngabenContainer container =
			persistence.find(FerienbetreuungAngabenContainer.class, containerId);

		return Optional.ofNullable(container);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer saveFerienbetreuungAngabenContainer(
		@Nonnull FerienbetreuungAngabenContainer container) {
		return persistence.merge(container);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer createFerienbetreuungAntrag(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		Optional<FerienbetreuungAngabenContainer> existingOptional =
			findFerienbetreuungAngabenContainer(gemeinde, gesuchsperiode);

		if (existingOptional.isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.ERROR,
				FerienbetreuungAngabenContainer.class,
				"FerienbetreuungContainer existiert für Gemeinde und Periode bereits",
				gemeinde.getName() + ' ' + gesuchsperiode.getGesuchsperiodeString(),
				ErrorCodeEnum.ERROR_FERIENBETREUUNG_ALREADY_EXISTS);
		}

		FerienbetreuungAngabenContainer container = new FerienbetreuungAngabenContainer();
		container.setStatus(FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE);
		container.setGemeinde(gemeinde);
		container.setGesuchsperiode(gesuchsperiode);
		container.setAngabenDeklaration(new FerienbetreuungAngaben());
		return persistence.persist(container);
	}

	@Nonnull
	private Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenContainer(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		Objects.requireNonNull(gemeinde, "gemeinde muss gesetzt sein");
		Objects.requireNonNull(gesuchsperiode, "gesuchsperiode muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FerienbetreuungAngabenContainer> query =
			cb.createQuery(FerienbetreuungAngabenContainer.class);
		Root<FerienbetreuungAngabenContainer> root =
			query.from(FerienbetreuungAngabenContainer.class);

		Predicate gemeindePredicate =
			cb.equal(root.get(FerienbetreuungAngabenContainer_.gemeinde), gemeinde);
		Predicate gesuchsperiodePredicate =
			cb.equal(root.get(FerienbetreuungAngabenContainer_.gesuchsperiode), gesuchsperiode);

		query.where(cb.and(gemeindePredicate, gesuchsperiodePredicate));
		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}

	@Nonnull
	@Override
	public void saveKommentar(@Nonnull String containerId, @Nonnull String kommentar) {
		FerienbetreuungAngabenContainer container =
			this.findFerienbetreuungAngabenContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveKommentar",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);
		container.setInternerKommentar(kommentar);
		persistence.persist(container);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenStammdaten> findFerienbetreuungAngabenStammdaten(
		@Nonnull String stammdatenId) {
		Objects.requireNonNull(stammdatenId, ID_MUSS_GESETZT_SEIN);

		FerienbetreuungAngabenStammdaten stammdaten =
			persistence.find(FerienbetreuungAngabenStammdaten.class, stammdatenId);

		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenAngebot> findFerienbetreuungAngabenAngebot(@Nonnull String angebotId) {
		Objects.requireNonNull(angebotId, ID_MUSS_GESETZT_SEIN);

		FerienbetreuungAngabenAngebot angebot =
			persistence.find(FerienbetreuungAngabenAngebot.class, angebotId);

		return Optional.ofNullable(angebot);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenNutzung> findFerienbetreuungAngabenNutzung(@Nonnull String nutzungId) {
		Objects.requireNonNull(nutzungId, ID_MUSS_GESETZT_SEIN);

		FerienbetreuungAngabenNutzung nutzung =
			persistence.find(FerienbetreuungAngabenNutzung.class, nutzungId);

		return Optional.ofNullable(nutzung);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungAngabenKostenEinnahmen> findFerienbetreuungAngabenKostenEinnahmen(
		@Nonnull String kostenEinnahmenId) {
		Objects.requireNonNull(kostenEinnahmenId, ID_MUSS_GESETZT_SEIN);

		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
			persistence.find(FerienbetreuungAngabenKostenEinnahmen.class, kostenEinnahmenId);

		return Optional.ofNullable(kostenEinnahmen);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenStammdaten saveFerienbetreuungAngabenStammdaten(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten) {
		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenAngebot saveFerienbetreuungAngabenAngebot(
		@Nonnull FerienbetreuungAngabenAngebot angebot) {
		return persistence.merge(angebot);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenNutzung saveFerienbetreuungAngabenNutzung(
		@Nonnull FerienbetreuungAngabenNutzung nutzung) {
		return persistence.merge(nutzung);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenKostenEinnahmen saveFerienbetreuungAngabenKostenEinnahmen(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen) {
		return persistence.merge(kostenEinnahmen);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenAngebot ferienbetreuungAngebotAbschliessen(
		@Nonnull FerienbetreuungAngabenAngebot angebot) {

		Preconditions.checkArgument(angebot.isReadyForAbschluss(), NOT_ALL_PROPERTIES_SET);
		Preconditions.checkArgument(
			angebot.getStatus() == FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE,
			"FerienbetreuungAngabenAngebot must be in state IN_BEARBEITUNG_GEMEINDE");

		angebot.setStatus(FerienbetreuungFormularStatus.ABGESCHLOSSEN);

		return persistence.merge(angebot);

	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenAngebot ferienbetreuungAngebotFalscheAngaben(
		@Nonnull FerienbetreuungAngabenAngebot angebot) {
		Preconditions.checkArgument(
			angebot.getStatus() == FerienbetreuungFormularStatus.ABGESCHLOSSEN,
			"FerienbetreuungAngabenAngebot must be in state ABGESCHLOSSEN");

		angebot.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);

		return persistence.merge(angebot);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungAbschliessen(
		@Nonnull FerienbetreuungAngabenNutzung nutzung) {
		Preconditions.checkArgument(nutzung.isReadyForAbschluss(), NOT_ALL_PROPERTIES_SET);
		Preconditions.checkArgument(
			nutzung.getStatus() == FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE,
			"FerienbetreuungAngabenNutzung must be in state IN_BEARBEITUNG_GEMEINDE"
		);

		nutzung.setStatus(FerienbetreuungFormularStatus.ABGESCHLOSSEN);

		return persistence.merge(nutzung);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungFalscheAngaben(
		@Nonnull
			FerienbetreuungAngabenNutzung nutzung) {
		Preconditions.checkArgument(
			nutzung.getStatus() == FerienbetreuungFormularStatus.ABGESCHLOSSEN,
			"FerienbetreuungAngabenNutzung must be in state ABGESCHLOSSEN"
		);

		nutzung.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);

		return persistence.merge(nutzung);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenAbschliessen(
		@Nonnull
			FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen) {
		Preconditions.checkArgument(kostenEinnahmen.isReadyForAbschluss(), NOT_ALL_PROPERTIES_SET);
		Preconditions.checkArgument(
			kostenEinnahmen.getStatus() == FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE,
			"FerienbetreuungAngabenKostenEinnahmen must be in state IN_BEARBEITUNG_GEMEINDE"
		);

		kostenEinnahmen.setStatus(FerienbetreuungFormularStatus.ABGESCHLOSSEN);

		return persistence.merge(kostenEinnahmen);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenFalscheAngaben(
		@Nonnull
			FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen) {
		Preconditions.checkArgument(
			kostenEinnahmen.getStatus() == FerienbetreuungFormularStatus.ABGESCHLOSSEN,
			"FerienbetreuungAngabenKostenEinnahmen must be in state ABGESCHLOSSEN"
		);

		kostenEinnahmen.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);

		return persistence.merge(kostenEinnahmen);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenAbschliessen(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten) {
		Preconditions.checkArgument(stammdaten.isReadyForAbschluss(), NOT_ALL_PROPERTIES_SET);
		Preconditions.checkArgument(
			stammdaten.getStatus() == FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE,
			"FerienbetreuungAngabenNutzung must be in state IN_BEARBEITUNG_GEMEINDE"
		);

		stammdaten.setStatus(FerienbetreuungFormularStatus.ABGESCHLOSSEN);

		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenFalscheAngaben(
		@Nonnull
			FerienbetreuungAngabenStammdaten stammdaten) {
		Preconditions.checkArgument(
			stammdaten.getStatus() == FerienbetreuungFormularStatus.ABGESCHLOSSEN,
			"FerienbetreuungAngabenStammdaten must be in state ABGESCHLOSSEN"
		);

		stammdaten.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);

		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer ferienbetreuungAngabenAbschliessen(
		@Nonnull FerienbetreuungAngabenContainer container) {
		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE,
			"FerienbetreuungAngabenContainer must be in state IN_BEARBEITUNG_GEMEINDE"
		);

		Preconditions.checkArgument(
			container.getAngabenDeklaration().isReadyForFreigeben(),
			"angaben incomplete"
		);

		container.copyForFreigabe();
		container.setStatus(FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON);

		return persistence.merge(container);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer ferienbetreuungAngabenGeprueft(
		@Nonnull FerienbetreuungAngabenContainer container) {
		assert container.getAngabenKorrektur() != null;

		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON,
			"FerienbetreuungAngabenContainer must be in state IN_PRUEFUNG_KANTON"
		);

		Preconditions.checkArgument(
			container.getAngabenKorrektur().isReadyForFreigeben(),
			"angaben incomplete"
		);

		container.setStatus(FerienbetreuungAngabenStatus.GEPRUEFT);

		return persistence.merge(container);
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer ferienbetreuungAngabenZurueckAnGemeinde(
		@Nonnull FerienbetreuungAngabenContainer container) {
		Preconditions.checkArgument(
			container.getStatus() == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON,
			"FerienbetreuungAngabenContainer must be in state IN_PRUEFUNG_KANTON"
		);
		Preconditions.checkArgument(
			container.getAngabenKorrektur() != null,
			"FerienbetreuungAngabenContainer must not be null"
		);

		container.copyForZurueckAnGemeinde();

		container.setStatus(FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE);

		container.getAngabenDeklaration()
			.getFerienbetreuungAngabenAngebot()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenDeklaration()
			.getFerienbetreuungAngabenNutzung()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenDeklaration()
			.getFerienbetreuungAngabenStammdaten()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenDeklaration()
			.getFerienbetreuungAngabenKostenEinnahmen()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		// reopen all korrekutr formulare that might have been geprueft
		// TODO: rename status to IN_BEARBEITUNG and write migration
		container.getAngabenKorrektur()
			.getFerienbetreuungAngabenAngebot()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenKorrektur()
			.getFerienbetreuungAngabenNutzung()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenKorrektur()
			.getFerienbetreuungAngabenStammdaten()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		container.getAngabenKorrektur()
			.getFerienbetreuungAngabenKostenEinnahmen()
			.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);

		return persistence.merge(container);
	}

	@Override
	public void deleteFerienbetreuungAntragIfExists(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		if (!configuration.getIsDevmode()) {
			throw new EbeguRuntimeException(
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer",
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer ist nur im Devmode möglich");
		}

		if (!principal.isCallerInRole(UserRole.SUPER_ADMIN)) {
			throw new EbeguRuntimeException(
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer",
				"deleteLastenausgleichTagesschuleAngabenGemeindeContainer ist nur als SuperAdmin möglich");
		}

		this.getFerienbetreuungAntraege(gemeinde.getName(), gesuchsperiode.getGesuchsperiodeString(), null, null)
			.forEach(antrag -> {
				this.ferienbetreuungDokumentService.findDokumente(antrag.getId())
					.forEach(dokument -> persistence.remove(dokument));
				persistence.remove(antrag);
			});
	}
}



/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.kind;

import ch.dvbern.ebegu.dto.KindDubletteDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.*;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

/**
 * Service fuer Kind
 */
@Stateless
@Local(KindService.class)
public class KindServiceBean extends AbstractBaseService implements KindService {

	private static final Logger LOGGER = LoggerFactory.getLogger(KindService.class);
	@Inject
	private Persistence persistence;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private ValidatorFactory validatorFactory;
	@Inject
	private KindServiceHandler kindServiceHandler;

	@Nonnull
	@Override
	public KindContainer saveKind(@Nonnull KindContainer kind, @Nullable KindContainer dbKind) {
		Objects.requireNonNull(kind);
		if (!kind.isNew()) {
			// Den Lucene-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
			updateLuceneIndex(KindContainer.class, kind.getId());
		}
		kindServiceHandler.resetKindBetreuungenDatenOnKindSave(kind, dbKind);
		final KindContainer mergedKind = persistence.merge(kind);
		mergedKind.getGesuch().addKindContainer(mergedKind);

		// validate explicitly: If KindContainer didn't change but a PensumFachstelle did, the validation won't
		// be automatically triggered
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<KindContainer>> constraintViolations = validator.validate(mergedKind);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}

		kindServiceHandler.resetGesuchDataOnKindSave(mergedKind);

		kindServiceHandler.resetKindBetreuungenStatusOnKindSave(mergedKind, dbKind);

		wizardStepService.updateSteps(kind.getGesuch().getId(), null, mergedKind.getKindJA(), WizardStepName.KINDER);

		return mergedKind;
	}

	@Override
	@Nonnull
	public Optional<KindContainer> findKind(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		KindContainer a = persistence.find(KindContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public List<KindContainer> findAllKinderFromGesuch(@Nonnull String gesuchId) {
		Objects.requireNonNull(gesuchId);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);
		Root<KindContainer> root = query.from(KindContainer.class);
		// Kinder from Gesuch
		Predicate predicateInstitution = root.get(KindContainer_.gesuch).get(AbstractEntity_.id).in(gesuchId);

		query.where(predicateInstitution);
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void removeKind(@Nonnull KindContainer kind) {
		final Gesuch gesuch = kind.getGesuch();
		final String gesuchId = gesuch.getId();

		persistence.remove(kind);

		// the kind needs to be removed from the object as well
		gesuch.getKindContainers().removeIf(k -> k.getId().equalsIgnoreCase(kind.getId()));

		wizardStepService.updateSteps(gesuchId, null, null, WizardStepName.KINDER);

		gesuchService.updateBetreuungenStatus(gesuch);
	}

	@Override
	@Nonnull
	public List<KindContainer> getAllKinderWithMissingStatistics(Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Gesuch> joinGesuch = root.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall, JoinType.LEFT);

		Predicate predicateMutation = cb.equal(joinGesuch.get(Gesuch_.typ), AntragTyp.MUTATION);
		Predicate predicateFlag = cb.isNull(root.get(KindContainer_.kindMutiert));
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtNotIgnoriertStates());
		Predicate predicateMandant = cb.equal(joinFall.get(Fall_.mandant), mandant);

		query.where(predicateMutation, predicateFlag, predicateStatus, predicateMandant);
		query.orderBy(cb.desc(joinGesuch.get(Gesuch_.laufnummer)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	public Set<KindDubletteDTO> getKindDubletten(@Nonnull String gesuchId) {
		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguRuntimeException("getKindDubletten", "No User is logged in"));
		Set<Gemeinde> gemeinden = user.extractGemeindenForUser();

		Set<KindDubletteDTO> dublettenOfAllKinder = new HashSet<>();
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(gesuchId);
		if (gesuchOptional.isPresent()) {
			Set<KindContainer> kindContainers = gesuchOptional.get().getKindContainers();
			for (KindContainer kindContainer : kindContainers) {
				List<KindDubletteDTO> kindDubletten = getKindDubletten(kindContainer, gemeinden, user);
				// Die Resultate sind nach Muationsdatum absteigend sortiert. Wenn also eine Fall-Id noch nicht vorkommt,
				// dann ist dies das neueste Gesuch dieses Falls
				dublettenOfAllKinder.addAll(kindDubletten);
			}
		} else {
			throw new EbeguEntityNotFoundException("getKindDubletten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId);
		}
		return dublettenOfAllKinder;
	}

	@Nonnull
	private List<KindDubletteDTO> getKindDubletten(
		@Nonnull KindContainer kindContainer,
		@Nonnull Set<Gemeinde> gemeinden,
		@Nonnull Benutzer user
	) {
		// Wir suchen nach Name, Vorname und Geburtsdatum
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindDubletteDTO> query = cb.createQuery(KindDubletteDTO.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Kind> joinKind = root.join(KindContainer_.kindJA, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = root.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.INNER);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall, JoinType.INNER);

		query.multiselect(
			joinGesuch.get(AbstractEntity_.id),
			joinGesuch.get(Gesuch_.dossier).get(Dossier_.fall).get(Fall_.fallNummer),
			cb.literal(kindContainer.getKindNummer()),
			root.get(KindContainer_.kindNummer),
			joinGesuch.get(AbstractEntity_.timestampErstellt)
		).distinct(true);

		ArrayList<Predicate> predicates = new ArrayList<>();

		// Identische Merkmale
		predicates.add(cb.equal(joinKind.get(AbstractPersonEntity_.nachname), kindContainer.getKindJA().getNachname()));
		predicates.add(cb.equal(joinKind.get(AbstractPersonEntity_.vorname), kindContainer.getKindJA().getVorname()));
		predicates.add(cb.equal(joinKind.get(AbstractPersonEntity_.geburtsdatum), kindContainer.getKindJA().getGeburtsdatum()));
		// Aber nicht vom selben Dossier
		predicates.add(cb.notEqual(joinGesuch.get(Gesuch_.dossier), kindContainer.getGesuch().getDossier()));
		// Nur das zuletzt gueltige Gesuch
		predicates.add(joinGesuch.get(Gesuch_.status).in(AntragStatus.getForKindDubletten()));
		// Nur Gesuch mit demselben Mandant
		predicates.add(cb.equal(joinFall.get(Fall_.mandant), kindContainer.getGesuch().getDossier().getFall().getMandant()));

		// Eingeloggter Benutzer muss Berechtigung für die Gemeinde haben
		// Superadmin und Mandant können alle Gemeinden sehen
		if (!user.getRole().isRoleMandant() && !user.getRole().isSuperadmin()) {
			// falls der Benutzer nicht Superadmin oder Mandant ist, muss zwingend mindestens eine Gemeinde gefunden werden
			if (gemeinden.isEmpty()) {
				throw new EbeguRuntimeException("getKindDubletten", "Keine Gemeinden für aktiven Benutzer gefunden");
			}

			predicates.add(joinDossier.get(Dossier_.gemeinde).in(gemeinden));
		}

		query.orderBy(cb.desc(joinGesuch.get(AbstractEntity_.timestampErstellt)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Collection<KindContainer> findKinder(
		@Nonnull Integer fallNummer,
		@Nonnull Integer kindNummer,
		int gesuchsperiodeStartJahr) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Gesuch> joinGesuch = root.join(KindContainer_.gesuch);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall);
		Join<Gesuch, Gesuchsperiode> joinGesuchsperiode = joinGesuch.join(Gesuch_.gesuchsperiode);

		Predicate predicateFallNummer = cb.equal(joinFall.get(Fall_.fallNummer), fallNummer);
		Predicate predicateKindNummer = cb.equal(root.get(KindContainer_.kindNummer), kindNummer);

		Expression<Integer> yearExp = cb.function("YEAR", Integer.class, joinGesuchsperiode
			.get(Gesuchsperiode_.gueltigkeit)
			.get(DateRange_.gueltigAb)
		);
		Predicate predicatePeriode = cb.equal(yearExp, gesuchsperiodeStartJahr);
		query.where(predicateFallNummer, predicateKindNummer, predicatePeriode);

		return persistence.getCriteriaResults(query);
	}

	@Override
	public void updateKeinSelbstbehaltFuerGemeinde(
		Collection<KindContainer> kindContainers,
		@Nonnull Boolean keinSelbstbehaltFuerGemeinde) {
		kindContainers.forEach(kindContainer -> {
			long fallNummer = kindContainer.getGesuch().getFall().getFallNummer();
			// Flag nur setzen, falls das Kind immer noch die Checkbox kindAusAsylwesen aktiviert hat
			if (overrideAllowed(kindContainer)) {
				kindContainer.setKeinSelbstbehaltDurchGemeinde(keinSelbstbehaltFuerGemeinde);
				LOGGER.info("Updating KindContainer with id " + kindContainer.getId() +
					", Fallnummer " + fallNummer + " and Kindnummer " + kindContainer.getKindNummer() +
					". Set keinSelbstbehaltFuerGemeinde = " + keinSelbstbehaltFuerGemeinde);
				persistence.persist(kindContainer);
			} else {
				LOGGER.info("KindContainer with id " + kindContainer.getId() +
					", Fallnummer " + fallNummer + " and Kindnummer " + kindContainer.getKindNummer() +
					" has kindAusAsylwesen == false or has wrong Gesuchstatus. Not setting keinSelbstbehaltFuerGemeinde");
			}
		});
	}

	// Nur KindContainers überschreiben die zu verfügten Gesuchen gehören und die immer noch kindAusAsylwesen == true haben.
	private boolean overrideAllowed(@Nonnull KindContainer kindContainer) {
		boolean kindAusAyslwesen = false;
		if (kindContainer.getKindJA() != null && kindContainer.getKindJA().getAusAsylwesen() != null) {
			kindAusAyslwesen = kindContainer.getKindJA().getAusAsylwesen();
		}
		boolean gesuchVerfuegt = kindContainer.getGesuch().getStatus().isAnyStatusOfVerfuegt();
		return kindAusAyslwesen && gesuchVerfuegt;
	}
}

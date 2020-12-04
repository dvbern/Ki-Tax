/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.dto.KindDubletteDTO;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer Kind
 */
@Stateless
@Local(KindService.class)
public class KindServiceBean extends AbstractBaseService implements KindService {

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

	private static final Logger LOGGER = LoggerFactory.getLogger(KindService.class);

	@Nonnull
	@Override
	public KindContainer saveKind(@Nonnull KindContainer kind) {
		Objects.requireNonNull(kind);
		if (!kind.isNew()) {
			// Den Lucene-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
			updateLuceneIndex(KindContainer.class, kind.getId());
		}

		final KindContainer mergedKind = persistence.merge(kind);
		mergedKind.getGesuch().addKindContainer(mergedKind);

		// validate explicitly: If KindContainer didn't change but a PensumFachstelle did, the validation won't
		// be automatically triggered
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<KindContainer>> constraintViolations = validator.validate(mergedKind);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}

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
	public List<KindContainer> getAllKinderWithMissingStatistics() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Gesuch> joinGesuch = root.join(KindContainer_.gesuch, JoinType.LEFT);

		Predicate predicateMutation = cb.equal(joinGesuch.get(Gesuch_.typ), AntragTyp.MUTATION);
		Predicate predicateFlag = cb.isNull(root.get(KindContainer_.kindMutiert));
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates());

		query.where(predicateMutation, predicateFlag, predicateStatus);
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
		predicates.add(joinGesuch.get(Gesuch_.status).in(AntragStatus.FOR_KIND_DUBLETTEN));

		// Eingeloggter Benutzer muss Berechtigung für die Gemeinde haben
		// Superadmin und Mandant können alle Gemeinden sehen
		if (!user.getRole().isRoleMandant() && !user.getRole().isSuperadmin()) {
			// falls der Benutzer nicht Superadmin oder Mandant ist, muss zwingend mindestens eine Gemeinde gefunden werden
			if (gemeinden.isEmpty()) {
				throw new EbeguRuntimeException("getKindDubletten", "Keine Gemeinden für aktiven Benutzer gefunden");
			}

			Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.INNER);
			predicates.add(joinDossier.get(Dossier_.gemeinde).in(gemeinden));
		}

		query.orderBy(cb.desc(joinGesuch.get(AbstractEntity_.timestampErstellt)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		return persistence.getCriteriaResults(query);
	}

	@Override
	public void updateKeinSelbstbehaltFuerGemeinde(
		@Nonnull Integer fallNummer,
		@Nonnull Integer kindNummer,
		int gesuchsperiodeStartJahr,
		@Nonnull Boolean keinSelbstbehaltFuerGemeinde)
	{
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

		// Bei Mutationen innerhalb einer Gesuchsperiode gibt es mehrere Kinder mit der gleichen
		// Fallnummer, Kindnummer undk Gesuchsperiode. Wir updaten alle
		Collection<KindContainer> kindContainers = persistence.getCriteriaResults(query);
		kindContainers.forEach(kindContainer -> {
			kindContainer.setKeinSelbstbehaltDurchGemeinde(keinSelbstbehaltFuerGemeinde);
			LOGGER.info("Updating KindContainer with id " + kindContainer.getId() +
				", Fallnummer " + fallNummer + " and Kindnummer " + kindNummer +
				". Set keinSelbstbehaltFuerGemeinde = " + keinSelbstbehaltFuerGemeinde);
			persistence.persist(kindContainer);
		});
	}
}

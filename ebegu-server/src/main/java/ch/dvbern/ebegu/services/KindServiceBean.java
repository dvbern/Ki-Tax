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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.dto.KindDubletteDTO;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Kind_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Kind
 */
@Stateless
@Local(KindService.class)
@PermitAll
public class KindServiceBean extends AbstractBaseService implements KindService {

	@Inject
	private Persistence persistence;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private GesuchService gesuchService;

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public KindContainer saveKind(@Nonnull KindContainer kind) {
		Objects.requireNonNull(kind);
		if (!kind.isNew()) {
			// Den Lucene-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
			updateLuceneIndex(KindContainer.class, kind.getId());
		}

		final KindContainer mergedKind = persistence.merge(kind);
		mergedKind.getGesuch().addKindContainer(mergedKind);
		wizardStepService.updateSteps(kind.getGesuch().getId(), null, mergedKind.getKindJA(), WizardStepName.KINDER);
		return mergedKind;
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<KindContainer> findKind(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		KindContainer a = persistence.find(KindContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	@PermitAll
	public List<KindContainer> findAllKinderFromGesuch(@Nonnull String gesuchId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);
		Root<KindContainer> root = query.from(KindContainer.class);
		// Kinder from Gesuch
		Predicate predicateInstitution = root.get(KindContainer_.gesuch).get(AbstractEntity_.id).in(gesuchId);

		query.where(predicateInstitution);
		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
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
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS, REVISOR,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Set<KindDubletteDTO> getKindDubletten(@Nonnull String gesuchId) {
		Set<KindDubletteDTO> dublettenOfAllKinder = new HashSet<>();
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(gesuchId);
		if (gesuchOptional.isPresent()) {
			Set<KindContainer> kindContainers = gesuchOptional.get().getKindContainers();
			for (KindContainer kindContainer : kindContainers) {
				List<KindDubletteDTO> kindDubletten = getKindDubletten(kindContainer);
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
	@Override
	public Optional<KindContainer> findKindFromPensumFachstelle(@Nonnull String pensumFachstelleId, @Nullable EntityManager em) {
		if (em == null) {
			em = persistence.getEntityManager();
		}

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = cb.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Kind> joinKind = root.join(KindContainer_.kindJA, JoinType.LEFT);

		Predicate predicatePensumFachstelle = cb
			.equal(joinKind.get(Kind_.pensumFachstelle).get(AbstractEntity_.id), pensumFachstelleId);

		query.where(predicatePensumFachstelle);


		try {
			final Optional<KindContainer> singleResult = Optional.ofNullable(em.createQuery(query).getSingleResult());
			return singleResult;
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Nonnull
	private List<KindDubletteDTO> getKindDubletten(@Nonnull KindContainer kindContainer) {
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

		// Identische Merkmale
		Predicate predicateName = cb.equal(joinKind.get(AbstractPersonEntity_.nachname), kindContainer.getKindJA().getNachname());
		Predicate predicateVorname = cb.equal(joinKind.get(AbstractPersonEntity_.vorname), kindContainer.getKindJA().getVorname());
		Predicate predicateGeburtsdatum = cb.equal(joinKind.get(AbstractPersonEntity_.geburtsdatum), kindContainer.getKindJA().getGeburtsdatum());
		// Aber nicht vom selben Dossier
		Predicate predicateOtherDossier = cb.notEqual(joinGesuch.get(Gesuch_.dossier), kindContainer.getGesuch().getDossier());
		// Nur das zuletzt gueltige Gesuch
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.FOR_KIND_DUBLETTEN);
		query.orderBy(cb.desc(joinGesuch.get(AbstractEntity_.timestampErstellt)));
		query.where(predicateName, predicateVorname, predicateGeburtsdatum, predicateOtherDossier, predicateStatus);

		return persistence.getCriteriaResults(query);
	}
}

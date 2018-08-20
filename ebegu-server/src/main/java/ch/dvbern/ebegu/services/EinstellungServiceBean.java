/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Einstellung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.errors.NoEinstellungFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMINISTRATOR_SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_JA;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Einstellungen
 */
@Stateless
@Local(EinstellungService.class)
@RolesAllowed({ ADMIN, SUPER_ADMIN })
public class EinstellungServiceBean extends AbstractBaseService implements EinstellungService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;


	@Override
	@Nonnull
	public Einstellung saveEinstellung(@Nonnull Einstellung einstellung) {
		Objects.requireNonNull(einstellung);
		if (einstellung.getGemeinde() != null) {
			einstellung.setMandant(einstellung.getGemeinde().getMandant());
		}
		if (einstellung.isNew()) {
			assertUniqueEinstellung(einstellung);
		}
		return persistence.merge(einstellung);
	}

	private void assertUniqueEinstellung(@Nonnull Einstellung einstellung) {
		EntityManager em = persistence.getEntityManager();
		if (einstellung.getGemeinde() != null) {
			Optional<Einstellung> einstellungByGemeinde = findEinstellungByGemeinde(einstellung.getKey(), einstellung.getGemeinde(), einstellung.getGesuchsperiode(), em);
			if (einstellungByGemeinde.isPresent()) {
				throw new IllegalArgumentException();
			}
		} else if (einstellung.getMandant() != null) {
			Optional<Einstellung> einstellungByMandant = findEinstellungByMandant(einstellung.getKey(), einstellung.getMandant(), einstellung.getGesuchsperiode(), em);
			if (einstellungByMandant.isPresent()) {
				throw new IllegalArgumentException();
			}
		} else {
			Optional<Einstellung> einstellungBySystem = findEinstellungBySystem(einstellung.getKey(), einstellung.getGesuchsperiode(), em);
			if (einstellungBySystem.isPresent()) {
				throw new IllegalArgumentException();
			}
		}
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Optional<Einstellung> findEinstellung(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Einstellung a = persistence.find(Einstellung.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Einstellung findEinstellung(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		return findEinstellung(key, gemeinde, gesuchsperiode, persistence.getEntityManager());
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Einstellung findEinstellung(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode, @Nullable final EntityManager
		em) {
		// Wir suchen drei-stufig:
		// (1) Nach Gemeinde
		Optional<Einstellung> einstellungByGemeinde = findEinstellungByGemeinde(key, gemeinde, gesuchsperiode, em);
		if (einstellungByGemeinde.isPresent()) {
			return einstellungByGemeinde.get();
		}
		// (2) Nach Mandant
		Optional<Einstellung> einstellungByMandant = findEinstellungByMandant(key, gemeinde.getMandant(), gesuchsperiode, em);
		if (einstellungByMandant.isPresent()) {
			return einstellungByMandant.get();
		}
		// (3) Nach Default des Systems
		Optional<Einstellung> einstellungBySystem = findEinstellungBySystem(key, gesuchsperiode, em);
		if (einstellungBySystem.isPresent()) {
			return einstellungBySystem.get();
		}
		throw new NoEinstellungFoundException(key, gemeinde, gesuchsperiode);
	}

	private Optional<Einstellung> findEinstellungBySystem(@Nonnull EinstellungKey key, @Nonnull Gesuchsperiode gesuchsperiode, @Nullable final EntityManager em) {
		final CriteriaBuilder cb = em != null ? em.getCriteriaBuilder() : persistence.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateKey = cb.equal(root.get(Einstellung_.key), key);
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde darf nicht gesetzt sein
		final Predicate predicateGemeindeNull = cb.isNull(root.get(Einstellung_.gemeinde));
		// Mandant darf nicht gesetzt sein
		final Predicate predicateMandantNull = cb.isNull(root.get(Einstellung_.mandant));

		query.where(predicateKey, predicateGesuchsperiode, predicateGemeindeNull, predicateMandantNull);
		query.select(root);
		List<Einstellung> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	private Optional<Einstellung> findEinstellungByMandant(@Nonnull EinstellungKey key, @Nonnull Mandant mandant, @Nonnull Gesuchsperiode gesuchsperiode,
			@Nullable final EntityManager em) {
		final CriteriaBuilder cb = em != null ? em.getCriteriaBuilder() : persistence.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateKey = cb.equal(root.get(Einstellung_.key), key);
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde darf nicht gesetzt sein
		final Predicate predicateGemeindeNull = cb.isNull(root.get(Einstellung_.gemeinde));
		// Mandant
		final Predicate predicateMandant = cb.equal(root.get(Einstellung_.mandant), mandant);

		query.where(predicateKey, predicateGesuchsperiode, predicateGemeindeNull, predicateMandant);
		query.select(root);
		List<Einstellung> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	private Optional<Einstellung> findEinstellungByGemeinde(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nullable final EntityManager em) {
		final CriteriaBuilder cb = em != null ? em.getCriteriaBuilder() : persistence.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateKey = cb.equal(root.get(Einstellung_.key), key);
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde
		final Predicate predicateGemeinde = cb.equal(root.get(Einstellung_.gemeinde), gemeinde);
		// Mandant
		final Predicate predicateMandant = cb.equal(root.get(Einstellung_.mandant), gemeinde.getMandant());

		query.where(predicateKey, predicateGesuchsperiode, predicateGemeinde, predicateMandant);
		query.select(root);
		List<Einstellung> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Collection<Einstellung> getEinstellungenByGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		Collection<Einstellung> einstellungen = criteriaQueryHelper.getEntitiesByAttribute(Einstellung.class, gesuchsperiode, Einstellung_
			.gesuchsperiode);
		List<Einstellung> filtered = einstellungen.stream()
			.filter(einstellung -> filterEinstellungSystemDefaults(einstellung))
			.sorted(Comparator.comparing(Einstellung::getKey))
			.collect(Collectors.toCollection(ArrayList::new));
		return filtered;
	}

	private boolean filterEinstellungSystemDefaults(Einstellung einstellung) {
		return einstellung.getMandant() == null && einstellung.getGemeinde() == null;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Map<EinstellungKey, Einstellung> getEinstellungenByGesuchsperiodeAsMap(@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> result = new HashMap<>();
		Collection<Einstellung> paramsForPeriode = getEinstellungenByGesuchsperiode(gesuchsperiode);
		for (Einstellung ebeguParameter : paramsForPeriode) {
			result.put(ebeguParameter.getKey(), ebeguParameter);
		}
		return result;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION,
		ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public void copyEinstellungenToNewGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiodeToCreate, @Nonnull Gesuchsperiode lastGesuchsperiode) {
		Collection<Einstellung> einstellungenOfLastGP = getEinstellungenByGesuchsperiode(lastGesuchsperiode);
		einstellungenOfLastGP
			.forEach(lastGPEinstellung -> {
				Einstellung einstellungOfNewGP = lastGPEinstellung.copyGesuchsperiode(gesuchsperiodeToCreate);
				saveEinstellung(einstellungOfNewGP);
		});
	}
}

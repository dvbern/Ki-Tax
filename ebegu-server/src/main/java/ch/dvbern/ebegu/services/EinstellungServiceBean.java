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
import javax.persistence.TypedQuery;
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
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
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
			Optional<Einstellung> einstellungByGemeinde = findEinstellungByMandantGemeindeOrSystem(einstellung.getKey(), null, einstellung.getGemeinde(), einstellung.getGesuchsperiode(), em);
			if (einstellungByGemeinde.isPresent()) {
				throw new IllegalArgumentException("Einstellung " + einstellung.getKey() + " is already present for Gemeinde");
			}
		} else if (einstellung.getMandant() != null) {
			Optional<Einstellung> einstellungByMandant = findEinstellungByMandantGemeindeOrSystem(einstellung.getKey(), einstellung.getMandant(), null, einstellung.getGesuchsperiode(), em);
			if (einstellungByMandant.isPresent()) {
				throw new IllegalArgumentException("Einstellung " + einstellung.getKey() + " is already present for Mandant");
			}
		} else {
			Optional<Einstellung> einstellungBySystem = findEinstellungByMandantGemeindeOrSystem(einstellung.getKey(), null, null, einstellung.getGesuchsperiode(), em);
			if (einstellungBySystem.isPresent()) {
				throw new IllegalArgumentException("Einstellung " + einstellung.getKey() + " is already present for System");
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
	public Einstellung findEinstellung(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull final EntityManager em) {
		// Wir suchen drei-stufig:
		// (1) Nach Gemeinde
		Optional<Einstellung> einstellungByGemeinde = findEinstellungByMandantGemeindeOrSystem(key, gemeinde.getMandant(), gemeinde, gesuchsperiode, em);
		if (einstellungByGemeinde.isPresent()) {
			return einstellungByGemeinde.get();
		}
		// (2) Nach Mandant
		Optional<Einstellung> einstellungByMandant = findEinstellungByMandantGemeindeOrSystem(key, gemeinde.getMandant(), null, gesuchsperiode, em);
		if (einstellungByMandant.isPresent()) {
			return einstellungByMandant.get();
		}
		// (3) Nach Default des Systems
		Optional<Einstellung> einstellungBySystem = findEinstellungByMandantGemeindeOrSystem(key, null, null, gesuchsperiode, em);
		if (einstellungBySystem.isPresent()) {
			return einstellungBySystem.get();
		}
		throw new NoEinstellungFoundException(key, gemeinde, gesuchsperiode);
	}

	private Optional<Einstellung> findEinstellungByMandantGemeindeOrSystem(
		@Nonnull EinstellungKey key,
		@Nullable Mandant mandant,
		@Nullable Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull final EntityManager em
	) {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateKey = cb.equal(root.get(Einstellung_.key), key);
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde
		final Predicate predicateGemeinde = (gemeinde == null) ?
			cb.isNull(root.get(Einstellung_.gemeinde)) : cb.equal(root.get(Einstellung_.gemeinde), gemeinde);
		// Mandant
		final Predicate predicateMandant = (mandant == null) ?
			cb.isNull(root.get(Einstellung_.mandant)) : cb.equal(root.get(Einstellung_.mandant), mandant);

		query.where(predicateKey, predicateGesuchsperiode, predicateGemeinde, predicateMandant);
		query.select(root);

		final TypedQuery<Einstellung> query1 = em.createQuery(query);
		List<Einstellung> criteriaResults = query1.getResultList();

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		if (criteriaResults.size() > 1) {
			throw new EbeguRuntimeException("findEinstellungByMandantGemeindeOrSystem", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		return Optional.of(criteriaResults.get(0));
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Collection<Einstellung> getAllEinstellungenBySystem(@Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde darf nicht gesetzt sein
		final Predicate predicateGemeindeNull = cb.isNull(root.get(Einstellung_.gemeinde));
		// Mandant
		final Predicate predicateMandantNull = cb.isNull(root.get(Einstellung_.mandant));

		query.where(predicateGesuchsperiode, predicateGemeindeNull, predicateMandantNull);
		query.select(root);
		List<Einstellung> einstellungen = persistence.getCriteriaResults(query, 1);
		List<Einstellung> sorted = einstellungen.stream()
			.sorted(Comparator.comparing(Einstellung::getKey))
			.collect(Collectors.toCollection(ArrayList::new));
		return sorted;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public Map<EinstellungKey, Einstellung> getAllEinstellungenByGemeindeAsMap(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> result = new HashMap<>();
		// Fuer jeden Key muss die spezifischste Einstellung gesucht werden
		for (EinstellungKey einstellungKey : EinstellungKey.values()) {
			Einstellung einstellung = findEinstellung(einstellungKey, gemeinde, gesuchsperiode);
			result.put(einstellungKey, einstellung);
		}
		return result;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION,
		ADMINISTRATOR_SCHULAMT, SCHULAMT, STEUERAMT })
	public void copyEinstellungenToNewGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiodeToCreate, @Nonnull Gesuchsperiode lastGesuchsperiode) {
		Collection<Einstellung> einstellungenOfLastGP = criteriaQueryHelper.getEntitiesByAttribute(Einstellung.class, lastGesuchsperiode,
			Einstellung_.gesuchsperiode);
		einstellungenOfLastGP
			.forEach(lastGPEinstellung -> {
				Einstellung einstellungOfNewGP = lastGPEinstellung.copyGesuchsperiode(gesuchsperiodeToCreate);
				saveEinstellung(einstellungOfNewGP);
			});
	}
}

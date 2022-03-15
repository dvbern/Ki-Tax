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
import java.util.EnumSet;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Traegerschaft_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Traegerschaft
 */
@Stateless
@Local(TraegerschaftService.class)
public class TraegerschaftServiceBean extends AbstractBaseService implements TraegerschaftService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public Traegerschaft createTraegerschaft(@Nonnull Traegerschaft traegerschaft, @Nonnull String adminEmail) {
		requireNonNull(traegerschaft);
		requireNonNull(adminEmail);

		authorizer.checkWriteAuthorization(traegerschaft);

		Traegerschaft persistedTraegerschaft = persistence.persist(traegerschaft);

		final Mandant mandant = requireNonNull(persistedTraegerschaft.getMandant());

		Benutzer benutzer = benutzerService.findBenutzer(adminEmail, mandant).map(b -> {
			if(b.getRole() != UserRole.GESUCHSTELLER) {
				throw new EbeguRuntimeException(
					KibonLogLevel.INFO,
					"createTraegerschaft",
					ErrorCodeEnum.EXISTING_USER_MAIL,
					adminEmail);
			}
			return b;
		}).orElseGet(() -> benutzerService.createAdminTraegerschaftByEmail(adminEmail, persistedTraegerschaft));

		benutzerService.einladen(Einladung.forTraegerschaft(benutzer, persistedTraegerschaft),
				mandant);

		return traegerschaft;
	}

	@Nonnull
	@Override
	public Traegerschaft saveTraegerschaft(@Nonnull Traegerschaft traegerschaft) {
		requireNonNull(traegerschaft);
		authorizer.checkWriteAuthorization(traegerschaft);
		return persistence.merge(traegerschaft);
	}

	@Nonnull
	@Override
	public Optional<Traegerschaft> findTraegerschaft(@Nonnull final String traegerschaftId) {
		requireNonNull(traegerschaftId, "id muss gesetzt sein");
		Traegerschaft a = persistence.find(Traegerschaft.class, traegerschaftId);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<Traegerschaft> getAllActiveTraegerschaften() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Traegerschaft> query = cb.createQuery(Traegerschaft.class);
		Root<Traegerschaft> root = query.from(Traegerschaft.class);

		Mandant mandant = principalBean.getMandant();
		if (mandant == null) {
			throw new EbeguRuntimeException("getAllActiveTraegerschaften", "mandant not found for principal " + principalBean.getPrincipal().getName());
		}

		Predicate mandantPredicate = cb.equal(root.get(Traegerschaft_.mandant), mandant);
		Predicate activePredicate = cb.equal(root.get(Traegerschaft_.active), true);

		query.orderBy(cb.asc(root.get(Traegerschaft_.name)));
		query.where(mandantPredicate, activePredicate);

		Collection<Traegerschaft> traegerschaften = persistence.getCriteriaResults(query);
		traegerschaften.forEach(traegerschaft -> authorizer.checkReadAuthorization(traegerschaft));

		return traegerschaften;
	}

	@Override
	@Nonnull
	public Collection<Traegerschaft> getAllTraegerschaften() {

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Traegerschaft> query = cb.createQuery(Traegerschaft.class);
		Root<Traegerschaft> root = query.from(Traegerschaft.class);

		Mandant mandant = principalBean.getMandant();
		if (mandant == null) {
			throw new EbeguRuntimeException("getAllTraegerschaften", "mandant not found for principal " + principalBean.getPrincipal().getName());
		}

		Predicate mandantPredicate = cb.equal(root.get(Traegerschaft_.mandant), mandant);
		query.orderBy(cb.asc(root.get(Traegerschaft_.name)));
		query.where(mandantPredicate);

		Collection<Traegerschaft> traegerschaften = persistence.getCriteriaResults(query);
		traegerschaften.forEach(traegerschaft -> authorizer.checkReadAuthorization(traegerschaft));

		return traegerschaften;
	}

	@Override
	public void removeTraegerschaft(@Nonnull String traegerschaftId) {
		requireNonNull(traegerschaftId);
		Optional<Traegerschaft> traegerschaftToRemove = findTraegerschaft(traegerschaftId);
		Traegerschaft traegerschaft =
			traegerschaftToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeTraegerschaft",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, traegerschaftId));

		authorizer.checkWriteAuthorization(traegerschaft);
		checkForLinkedBerechtigungen(traegerschaft);

		// Es müssen auch alle Berechtigungen für diese Traegerschaft gelöscht werden
		Collection<BerechtigungHistory> berechtigungenToDelete =
			criteriaQueryHelper.getEntitiesByAttribute(BerechtigungHistory.class, traegerschaft,
				BerechtigungHistory_.traegerschaft);
		for (BerechtigungHistory berechtigungHistory : berechtigungenToDelete) {
			persistence.remove(berechtigungHistory);
		}

		persistence.remove(traegerschaft);
	}

	private void checkForLinkedBerechtigungen(@Nonnull Traegerschaft traegerschaft) {
		final Collection<Berechtigung> linkedBerechtigungen = findBerechtigungByTraegerschaft(traegerschaft);
		if (!linkedBerechtigungen.isEmpty()) {
			throw new EbeguRuntimeException("checkForLinkedBerechtigungen", ErrorCodeEnum.ERROR_LINKED_BERECHTIGUNGEN, traegerschaft.getId());
		}
	}

	private Collection<Berechtigung> findBerechtigungByTraegerschaft(@Nonnull Traegerschaft traegerschaft) {
		requireNonNull(traegerschaft, "traegerschaft cannot be null");
		return criteriaQueryHelper.getEntitiesByAttribute(Berechtigung.class, traegerschaft, Berechtigung_.traegerschaft);
	}

	@Override
	public EnumSet<BetreuungsangebotTyp> getAllAngeboteFromTraegerschaft(@Nonnull String traegerschaftId) {
		requireNonNull(traegerschaftId);
		Optional<Traegerschaft> traegerschaftOptional = findTraegerschaft(traegerschaftId);
		Traegerschaft traegerschaft = traegerschaftOptional.orElseThrow(() -> new EbeguEntityNotFoundException(
			"setInactive",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			traegerschaftId));

		EnumSet<BetreuungsangebotTyp> result = EnumSet.noneOf(BetreuungsangebotTyp.class);

		Collection<Institution> allInstitutionen =
			institutionService.getAllInstitutionenFromTraegerschaft(traegerschaft.getId());
		allInstitutionen.forEach(institution -> {
			BetreuungsangebotTyp angebotInstitution =
				institutionService.getAngebotFromInstitution(institution.getId());
			result.add(angebotInstitution);
		});

		return result;
	}
}

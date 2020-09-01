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

import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Institution;
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

	@Nonnull
	@Override
	public Traegerschaft createTraegerschaft(@Nonnull Traegerschaft traegerschaft, @Nonnull String adminEmail) {
		requireNonNull(traegerschaft);
		requireNonNull(adminEmail);

		Traegerschaft persistedTraegerschaft = persistence.persist(traegerschaft);

		Benutzer benutzer = benutzerService.findBenutzerByEmail(adminEmail).map(b -> {
			if(b.getRole() != UserRole.GESUCHSTELLER) {
				throw new EbeguRuntimeException(
					KibonLogLevel.INFO,
					"createTraegerschaft",
					ErrorCodeEnum.EXISTING_USER_MAIL,
					adminEmail);
			}
			return b;
		}).orElseGet(() -> benutzerService.createAdminTraegerschaftByEmail(adminEmail, persistedTraegerschaft));

		benutzerService.einladen(Einladung.forTraegerschaft(benutzer, persistedTraegerschaft));

		return traegerschaft;
	}

	@Nonnull
	@Override
	public Traegerschaft saveTraegerschaft(@Nonnull Traegerschaft traegerschaft) {
		requireNonNull(traegerschaft);
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
		return criteriaQueryHelper.getEntitiesByAttribute(Traegerschaft.class, true, Traegerschaft_.active);
	}

	@Override
	@Nonnull
	public Collection<Traegerschaft> getAllTraegerschaften() {
		return new ArrayList<>(criteriaQueryHelper.getAllOrdered(Traegerschaft.class, Traegerschaft_.name));
	}

	@Override
	public void removeTraegerschaft(@Nonnull String traegerschaftId) {
		requireNonNull(traegerschaftId);
		Optional<Traegerschaft> traegerschaftToRemove = findTraegerschaft(traegerschaftId);
		Traegerschaft traegerschaft =
			traegerschaftToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeTraegerschaft",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, traegerschaftId));

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

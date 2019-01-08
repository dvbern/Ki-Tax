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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Institution
 */
@Stateless
@Local(InstitutionService.class)
@PermitAll
public class InstitutionServiceBean extends AbstractBaseService implements InstitutionService {

	@Inject
	private Persistence persistence;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	// ID der statischen, unbekannten Institution. Wird verwendet um eine provisorische Berechnung zu generieren
	// und darf dem Benutzer <b>nie>/b> angezeigt werden
	private static final String ID_UNKNOWN_INSTITUTION_KITA = "00000000-0000-0000-0000-000000000000";
	private static final String ID_UNKNOWN_INSTITUTION_TAGESFAMILIE = "00000000-0000-0000-0000-000000000001";

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION })
	public Institution updateInstitution(@Nonnull Institution institution) {
		Objects.requireNonNull(institution);
		return persistence.merge(institution);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT })
	public Institution createInstitution(@Nonnull Institution institution) {
		Objects.requireNonNull(institution);
		if (institution.getMandant() == null) {
			institution.setMandant(requireNonNull(principalBean.getMandant()));
		}

		return persistence.persist(institution);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Institution> findInstitution(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Institution a = persistence.find(Institution.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT })
	public Institution setInstitutionInactive(@Nonnull String institutionId) {
		Objects.requireNonNull(institutionId);

		final InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId);

		institutionStammdaten.setInactive();
		final InstitutionStammdaten mergedInstitutionstammdaten = persistence.merge(institutionStammdaten);
		return mergedInstitutionstammdaten.getInstitution();
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT })
	public void deleteInstitution(@Nonnull String institutionId) {
		Objects.requireNonNull(institutionId);
		Optional<Institution> institutionToRemove = findInstitution(institutionId);
		Institution institution =
			institutionToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeInstitution",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));

		// Es müssen auch alle Berechtigungen für diese Institution gelöscht werden
		Collection<BerechtigungHistory> berechtigungenToDelete =
			criteriaQueryHelper.getEntitiesByAttribute(BerechtigungHistory.class, institution,
				BerechtigungHistory_.institution);
		for (BerechtigungHistory berechtigungHistory : berechtigungenToDelete) {
			persistence.remove(berechtigungHistory);
		}

		persistence.remove(institution);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Institution> getAllInstitutionenFromTraegerschaft(String traegerschaftId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<Institution> root = query.from(Institution.class);
		//Traegerschaft
		Predicate predTraegerschaft =
			cb.equal(root.get(Institution_.traegerschaft).get(AbstractEntity_.id), traegerschaftId);

		query.where(predTraegerschaft, excludeUnknownInstitutionPredicate(cb, root.get(AbstractEntity_.id)));

		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Institution> getAllActiveInstitutionenFromTraegerschaft(String traegerschaftId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);

		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);

		query.select(root.get(InstitutionStammdaten_.institution));

		Join<InstitutionStammdaten, Institution> institutionJoin =
			root.join(InstitutionStammdaten_.institution, JoinType.LEFT);
		//Traegerschaft
		Predicate predTraegerschaft =
			cb.equal(institutionJoin.get(Institution_.traegerschaft).get(AbstractEntity_.id), traegerschaftId);
		Predicate predActive = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			LocalDate.now()
		);

		query.where(predTraegerschaft, predActive, excludeUnknownInstitutionPredicate(cb, root.get(AbstractEntity_.id)));

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@PermitAll
	private Collection<Institution> getAllInstitutionenForSchulamt() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.institution));
		query.distinct(true);

		Predicate predSchulamt =
			root.get(InstitutionStammdaten_.betreuungsangebotTyp).in(BetreuungsangebotTyp.getSchulamtTypes());
		Predicate predActive = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			LocalDate.now()
		);

		query.where(predSchulamt, predActive, excludeUnknownInstitutionPredicate(cb, root.get(AbstractEntity_.id)));

		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Institution> getAllActiveInstitutionen() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.institution));
		query.distinct(true);
		Predicate predActive = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			LocalDate.now()
		);

		query.where(predActive, excludeUnknownInstitutionPredicate(cb, root.get(AbstractEntity_.id)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Institution> getAllInstitutionen() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.institution));
		query.distinct(true);

		query.where(excludeUnknownInstitutionPredicate(cb, root.get(AbstractEntity_.id)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Institution> getAllowedInstitutionenForCurrentBenutzer(boolean restrictedForSCH) {
		Optional<Benutzer> benutzerOptional = benutzerService.getCurrentBenutzer();
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			if (EnumUtil.isOneOf(
				benutzer.getRole(),
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT) && benutzer.getTraegerschaft() != null) {
				return getAllInstitutionenFromTraegerschaft(benutzer.getTraegerschaft().getId());
			}
			if (EnumUtil.isOneOf(benutzer.getRole(), UserRole.ADMIN_INSTITUTION, UserRole.SACHBEARBEITER_INSTITUTION)
				&& benutzer.getInstitution() != null) {
				List<Institution> institutionList = new ArrayList<>();
				if (benutzer.getInstitution() != null) {
					institutionList.add(benutzer.getInstitution());
				}
				return institutionList;
			}
			if (restrictedForSCH && benutzer.getRole().isRoleSchulamt()) {
				return getAllInstitutionenForSchulamt();
			}
			return getAllInstitutionen();
		}
		return Collections.emptyList();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS, REVISOR, ADMIN_MANDANT, ADMIN_TRAEGERSCHAFT,
		ADMIN_INSTITUTION })
	public BetreuungsangebotTyp getAngebotFromInstitution(@Nonnull String institutionId) {
		InstitutionStammdaten allInstStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId);
		return allInstStammdaten.getBetreuungsangebotTyp();
	}

	private Predicate excludeUnknownInstitutionPredicate(CriteriaBuilder cb, Path path) {
		return cb.notEqual(path,ID_UNKNOWN_INSTITUTION_KITA);
	}
}

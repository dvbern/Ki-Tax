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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
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
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer InstitutionStammdaten
 */
@Stateless
@Local(InstitutionStammdatenService.class)
@PermitAll
public class InstitutionStammdatenServiceBean extends AbstractBaseService implements InstitutionStammdatenService {

	private static final String GP_START = "gpStart";
	private static final String GP_END = "gpEnd";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT })
	public InstitutionStammdaten saveInstitutionStammdaten(@Nonnull InstitutionStammdaten institutionStammdaten) {
		Objects.requireNonNull(institutionStammdaten);
		return persistence.merge(institutionStammdaten);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<InstitutionStammdaten> findInstitutionStammdaten(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		InstitutionStammdaten a = persistence.find(InstitutionStammdaten.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<InstitutionStammdaten> getAllInstitutionStammdaten() {
		return new ArrayList<>(criteriaQueryHelper.getAll(InstitutionStammdaten.class));
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	public void removeInstitutionStammdaten(@Nonnull String institutionStammdatenId) {
		Objects.requireNonNull(institutionStammdatenId);
		Optional<InstitutionStammdaten> institutionStammdatenToRemove = findInstitutionStammdaten(institutionStammdatenId);
		final InstitutionStammdaten removeInstitutionStammdaten = institutionStammdatenToRemove.orElseThrow(() -> new EbeguEntityNotFoundException
			("removeInstitutionStammdaten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionStammdatenId));
		persistence.remove(removeInstitutionStammdaten);
	}

	@Override
	@PermitAll
	public Collection<InstitutionStammdaten> getAllInstitutionStammdatenByDate(@Nonnull LocalDate date) {
		return new ArrayList<>(criteriaQueryHelper.getAllInInterval(InstitutionStammdaten.class, date));
	}

	@Override
	public Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiode(@Nonnull String gesuchsperiodeId) {

		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, gesuchsperiodeId);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root);
		Predicate isActivePredicate = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			LocalDate.now()
		);

		ParameterExpression<LocalDate> startParam = cb.parameter(LocalDate.class, GP_START);
		ParameterExpression<LocalDate> endParam = cb.parameter(LocalDate.class, GP_END);

		// InstStammdaten Ende muss NACH GP Start sein
		// InstStammdaten Start muss VOR GP Ende sein
		Predicate startPredicate = cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), startParam);
		Predicate endPredicate = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), endParam);

		query.where(startPredicate, endPredicate, isActivePredicate);

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GP_START, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(GP_END, gesuchsperiode.getGueltigkeit().getGueltigBis());
		return typedQuery.getResultList();
	}

	@Override
	@Nonnull
	@PermitAll
	public InstitutionStammdaten getInstitutionStammdatenByInstitution(String institutionId) {
		Institution institution = institutionService.findInstitution(institutionId).orElseThrow(() -> new EbeguEntityNotFoundException
			("getInstitutionStammdatenByInstitution", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));
		return criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElseThrow(() -> new EbeguEntityNotFoundException
			("getInstitutionStammdatenByInstitution", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));
	}

	@Nullable
	@Override
	@PermitAll
	public InstitutionStammdaten fetchInstitutionStammdatenByInstitution(String institutionId) {
		Institution institution = institutionService.findInstitution(institutionId).orElseThrow(() -> new EbeguEntityNotFoundException
			("fetchInstitutionStammdatenByInstitution", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId));

		return criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElse(null);
	}

	@Override
	@PermitAll
	public Collection<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer() {
		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		if (role.isRoleSchulamt()) { // fuer Schulamt muessen wir nichts machen. Direkt Schulamttypes zurueckgeben
			return BetreuungsangebotTyp.getSchulamtTypes();
		}
		Collection<Institution> institutionenForCurrentBenutzer = institutionService.getAllowedInstitutionenForCurrentBenutzer(false);
		if (institutionenForCurrentBenutzer.isEmpty()) {
			return new ArrayList<>();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungsangebotTyp> query = cb.createQuery(BetreuungsangebotTyp.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.betreuungsangebotTyp));
		query.distinct(true);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = cb.between(dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		Predicate institutionPredicate = root.get(InstitutionStammdaten_.institution).in(institutionenForCurrentBenutzer);

		query.where(intervalPredicate, institutionPredicate);
		TypedQuery<BetreuungsangebotTyp> q = persistence.getEntityManager().createQuery(query).setParameter(dateParam, LocalDate.now());
		List<BetreuungsangebotTyp> resultList = q.getResultList();
		return resultList;
	}


}

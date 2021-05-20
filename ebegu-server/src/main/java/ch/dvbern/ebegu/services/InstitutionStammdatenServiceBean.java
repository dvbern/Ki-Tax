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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.institution.InstitutionEventConverter;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer InstitutionStammdaten
 */
@Stateless
@Local(InstitutionStammdatenService.class)
public class InstitutionStammdatenServiceBean extends AbstractBaseService implements InstitutionStammdatenService {

	private static final String GP_START = "gpStart";
	private static final String GP_END = "gpEnd";
	private static final String GEMEINDEN = "gemeinden";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private InstitutionEventConverter institutionEventConverter;

	@Inject
	private Authorizer authorizer;

	@Inject
	private AdresseService adresseService;

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Nonnull
	@Override
	public InstitutionStammdaten saveInstitutionStammdaten(@Nonnull InstitutionStammdaten institutionStammdaten) {
		requireNonNull(institutionStammdaten);
		authorizer.checkWriteAuthorizationInstitutionStammdaten(institutionStammdaten);
		// always when stammdaten are saved we need to reset the flag stammdatenCheckRequired to false
		institutionService.updateStammdatenCheckRequired(institutionStammdaten.getInstitution().getId(), false);
		InstitutionStammdaten updatedStammdaten = null;
		try {
			updatedStammdaten = persistence.merge(institutionStammdaten);

			//we flush the transaction in order to see if there is some constraint violation
			persistence.getEntityManager().flush();
			return updatedStammdaten;
		} catch (PersistenceException e) {
			String sqlError = e.getCause().getCause().getMessage();
			//if the FK_belegung_ts_modul_modul_ts constraint is raised then we need to inform the user
			if (sqlError.contains("FK_belegung_ts_modul_modul_ts")) {
				throw new EntityExistsException(KibonLogLevel.ERROR, InstitutionStammdaten.class, "Anmeldungen",
					institutionStammdaten.getId(),
					ErrorCodeEnum.ERROR_ANMELDUNGEN_EXISTS);
			}
			//otherwise its an unexpected exception
			throw e;
		}
	}

	@Override
	public void fireStammdatenChangedEvent(@Nonnull InstitutionStammdaten updatedStammdaten) {
		event.fire(institutionEventConverter.of(updatedStammdaten));
	}

	@Nonnull
	@Override
	public Optional<InstitutionStammdaten> findInstitutionStammdaten(@Nonnull final String id) {
		requireNonNull(id, "id muss gesetzt sein");
		InstitutionStammdaten institutionStammdaten = persistence.find(InstitutionStammdaten.class, id);
		authorizer.checkReadAuthorizationInstitutionStammdaten(institutionStammdaten);
		return Optional.ofNullable(institutionStammdaten);
	}

	@Override
	@Nonnull
	public Collection<InstitutionStammdaten> getAllInstitutionStammdaten() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		Join<InstitutionStammdaten, Institution> joinInstitution =
			root.join(InstitutionStammdaten_.institution, JoinType.LEFT);
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));

		Benutzer currentBenutzer = principalBean.getBenutzer();
		boolean roleGemeindeabhaengig = currentBenutzer.getRole().isRoleGemeindeabhaengig();
		if (roleGemeindeabhaengig) {
			ParameterExpression<Collection> gemeindeParam = cb.parameter(Collection.class, GEMEINDEN);
			predicates.add(PredicateHelper.getPredicateBerechtigteInstitutionStammdaten(cb, root, gemeindeParam));
		}

		Predicate predicateMandant = PredicateHelper.getPredicateMandant(cb, joinInstitution.get(Institution_.mandant)
			, currentBenutzer);
		predicates.add(predicateMandant);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		if (roleGemeindeabhaengig) {
			typedQuery.setParameter(GEMEINDEN, currentBenutzer.extractGemeindenForUser());
		}
		return typedQuery.getResultList();
	}

	@Override
	@Nonnull
	public Collection<InstitutionStammdaten> getAllInstitutionStammdatenForTraegerschaft(
		@Nonnull Traegerschaft trageschaft) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		Join<InstitutionStammdaten, Institution> joinInstitution =
			root.join(InstitutionStammdaten_.institution, JoinType.LEFT);
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));

		Benutzer currentBenutzer = principalBean.getBenutzer();

		Predicate predicateTraegerschaft = cb.equal(joinInstitution.get(Institution_.traegerschaft), trageschaft);
		predicates.add(predicateTraegerschaft);
		Predicate predicateMandant = PredicateHelper.getPredicateMandant(cb, joinInstitution.get(Institution_.mandant)
			, currentBenutzer);
		predicates.add(predicateMandant);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		return typedQuery.getResultList();
	}

	@Override
	@Nonnull
	public Collection<InstitutionStammdaten> getAllInstitonStammdatenForBatchjobs() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.where(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void removeInstitutionStammdatenByInstitution(@Nonnull String institutionId) {
		requireNonNull(institutionId);
		InstitutionStammdaten institutionStammdatenToRemove = fetchInstitutionStammdatenByInstitution(
			institutionId,
			true);
		if (institutionStammdatenToRemove != null) {
			authorizer.checkWriteAuthorizationInstitutionStammdaten(institutionStammdatenToRemove);
			event.fire(institutionEventConverter.deleteEvent(institutionStammdatenToRemove));
			persistence.remove(institutionStammdatenToRemove);
		}
	}

	@Override
	public Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull String gesuchsperiodeId,
		@Nonnull String gemeindeId) {

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeId));

		List<Gemeinde> gemeinden = Collections.singletonList(gemeinde);
		return getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(gesuchsperiodeId, gemeinden);
	}

	private Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Collection<Gemeinde> gemeinden
	) {

		requireNonNull(gesuchsperiodeId);
		requireNonNull(gemeinden);

		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, gesuchsperiodeId);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root);
		List<Predicate> predicates = new ArrayList<>();

		ParameterExpression<LocalDate> startParam = cb.parameter(LocalDate.class, GP_START);
		ParameterExpression<LocalDate> endParam = cb.parameter(LocalDate.class, GP_END);

		// InstStammdaten Ende muss NACH GP Start sein
		// InstStammdaten Start muss VOR GP Ende sein
		predicates.addAll(PredicateHelper.getPredicateDateRangedEntityIncludedInRange(cb, root, startParam, endParam));

		ParameterExpression<Collection> gemeindeParam = cb.parameter(Collection.class, GEMEINDEN);
		predicates.add(PredicateHelper.getPredicateBerechtigteInstitutionStammdaten(cb, root, gemeindeParam));

		predicates.add(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GP_START, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(GP_END, gesuchsperiode.getGueltigkeit().getGueltigBis());
		typedQuery.setParameter(GEMEINDEN, gemeinden);
		return typedQuery.getResultList();
	}

	@Nullable
	@Override
	public InstitutionStammdaten fetchInstitutionStammdatenByInstitution(String institutionId, boolean doAuthCheck) {
		Institution institution = institutionService.findInstitution(institutionId, doAuthCheck)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"fetchInstitutionStammdatenByInstitution",
				institutionId));
		if (doAuthCheck) {
			authorizer.checkReadAuthorizationInstitution(institution);
		}

		return criteriaQueryHelper.getEntityByUniqueAttribute(
			InstitutionStammdaten.class,
			institution,
			InstitutionStammdaten_.institution
		).orElse(null);
	}

	@Override
	public Collection<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer() {
		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		if (role.isRoleGemeindeOrTS()) { // fuer Schulamt muessen wir nichts machen. Direkt Schulamttypes zurueckgeben
			return BetreuungsangebotTyp.getSchulamtTypes();
		}
		Collection<Institution> institutionenForCurrentBenutzer =
			institutionService.getInstitutionenReadableForCurrentBenutzer(false);
		if (institutionenForCurrentBenutzer.isEmpty()) {
			return new ArrayList<>();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BetreuungsangebotTyp> query = cb.createQuery(BetreuungsangebotTyp.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.betreuungsangebotTyp));
		query.distinct(true);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = PredicateHelper.getPredicateDateRangedEntityGueltigAm(cb, root, dateParam);
		Predicate institutionPredicate = root.get(InstitutionStammdaten_.institution)
			.in(institutionenForCurrentBenutzer);
		Predicate noUnknown = PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root);

		query.where(intervalPredicate, institutionPredicate, noUnknown);
		TypedQuery<BetreuungsangebotTyp> q = persistence.getEntityManager().createQuery(query)
			.setParameter(dateParam, LocalDate.now());
		List<BetreuungsangebotTyp> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public Collection<InstitutionStammdaten> getTagesschulenForCurrentBenutzer() {
		Collection<Institution> institutionenForCurrentBenutzer =
			institutionService.getInstitutionenReadableForCurrentBenutzer(false);
		if (institutionenForCurrentBenutzer.isEmpty()) {
			return new ArrayList<>();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);

		Predicate institutionPredicate = root.get(InstitutionStammdaten_.institution)
			.in(institutionenForCurrentBenutzer);
		Predicate predicateTypTagesschule =
			cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE);

		query.where(institutionPredicate, predicateTypTagesschule);
		TypedQuery<InstitutionStammdaten> q = persistence.getEntityManager().createQuery(query);
		return q.getResultList();
	}

	@Nonnull
	@Override
	public Set<InstitutionStammdaten> updateGemeindeForBGInstitutionen() {
		Set<InstitutionStammdaten> changed = new HashSet<>();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		Predicate hasBgStammdaten =
			cb.isNotNull(root.get(InstitutionStammdaten_.institutionStammdatenBetreuungsgutscheine));

		query.where(hasBgStammdaten);

		TypedQuery<InstitutionStammdaten> q = persistence.getEntityManager().createQuery(query);
		q.getResultList().forEach(instStammdaten -> {
			// update Adressen aller BG-Insitutionen
			if (adresseService.updateGemeindeAndBFS(instStammdaten.getAdresse())) {
				changed.add(instStammdaten);
			}

			if (requireNonNull(instStammdaten.getInstitutionStammdatenBetreuungsgutscheine()).getBetreuungsstandorte()
				.stream()
				// update Adressen aller Betreuungsstandorte
				.anyMatch(b -> adresseService.updateGemeindeAndBFS(b.getAdresse()))) {
				changed.add(instStammdaten);
			}
		});

		return changed;
	}

	@Override
	@Nonnull
	public Collection<InstitutionStammdaten> getAllTagesschulenForGesuchsperiodeAndGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		Join<InstitutionStammdaten, InstitutionStammdatenTagesschule> joinTagesschule =
			root.join(InstitutionStammdaten_.institutionStammdatenTagesschule, JoinType.LEFT);
		query.select(root);

		ParameterExpression<Gemeinde> gemeindeParam = cb.parameter(Gemeinde.class, GEMEINDEN);
		ParameterExpression<LocalDate> startParam = cb.parameter(LocalDate.class, GP_START);
		ParameterExpression<LocalDate> endParam = cb.parameter(LocalDate.class, GP_END);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE));
		predicates.add(cb.equal(joinTagesschule.get(InstitutionStammdatenTagesschule_.gemeinde), gemeindeParam));
		// InstStammdaten Ende muss NACH GP Start sein
		// InstStammdaten Start muss VOR GP Ende sein
		predicates.addAll(PredicateHelper.getPredicateDateRangedEntityIncludedInRange(cb, root,	startParam, endParam));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GEMEINDEN, gemeinde);
		typedQuery.setParameter(GP_START, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(GP_END, gesuchsperiode.getGueltigkeit().getGueltigBis());
		return typedQuery.getResultList();
	}

	@Override
	@Nonnull
	public Collection<InstitutionStammdaten> getAllTagesschulenForGemeinde(
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		Join<InstitutionStammdaten, InstitutionStammdatenTagesschule> joinTagesschule =
			root.join(InstitutionStammdaten_.institutionStammdatenTagesschule, JoinType.LEFT);
		query.select(root);

		ParameterExpression<Gemeinde> gemeindeParam = cb.parameter(Gemeinde.class, GEMEINDEN);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE));
		predicates.add(cb.equal(joinTagesschule.get(InstitutionStammdatenTagesschule_.gemeinde), gemeindeParam));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<InstitutionStammdaten> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(GEMEINDEN, gemeinde);
		return typedQuery.getResultList();
	}
}

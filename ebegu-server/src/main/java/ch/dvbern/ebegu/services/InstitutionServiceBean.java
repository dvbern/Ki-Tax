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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.institutionclient.InstitutionClientEventConverter;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Institution
 */
@Stateless
@Local(InstitutionService.class)
@PermitAll
public class InstitutionServiceBean extends AbstractBaseService implements InstitutionService {

	private static final String GEMEINDEN = "gemeinden";

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
	@Inject
	private Event<ExportedEvent> exportedEvent;
	@Inject
	private InstitutionClientEventConverter institutionClientEventConverter;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION })
	public Institution updateInstitution(@Nonnull Institution institution) {
		Objects.requireNonNull(institution);

		return persistence.merge(institution);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_GEMEINDE, ADMIN_BG
		, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
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

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Institution activateInstitution(@Nonnull String institutionId) {
		Institution institution = findInstitution(institutionId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"activateInstitution",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		institution.setStatus(InstitutionStatus.AKTIV);
		return updateInstitution(institution);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_GEMEINDE, ADMIN_BG,
		ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Institution setInstitutionInactive(@Nonnull String institutionId) {
		Objects.requireNonNull(institutionId);

		final InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId);

		institutionStammdaten.setInactive();
		final InstitutionStammdaten mergedInstitutionstammdaten = persistence.merge(institutionStammdaten);
		return mergedInstitutionstammdaten.getInstitution();
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

		query.where(predTraegerschaft, PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));

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
		Predicate predActive = PredicateHelper.getPredicateDateRangedEntityGueltig(cb, root);
		Predicate predNoUnknown = PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root);

		query.where(predSchulamt, predActive, predNoUnknown);

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
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));

		boolean roleGemeindeabhaengig = principalBean.getBenutzer().getRole().isRoleGemeindeabhaengig();
		if (roleGemeindeabhaengig) {
			ParameterExpression<Collection> gemeindeParam = cb.parameter(Collection.class, GEMEINDEN);
			predicates.add(PredicateHelper.getPredicateBerechtigteInstitutionStammdaten(cb, root, gemeindeParam));
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		Benutzer currentBenutzer = principalBean.getBenutzer();
		TypedQuery<Institution> typedQuery = persistence.getEntityManager().createQuery(query);
		if (roleGemeindeabhaengig) {
			typedQuery.setParameter(GEMEINDEN, currentBenutzer.extractGemeindenForUser());
		}

		return typedQuery.getResultList();
	}

	@Nonnull
	@PermitAll
	private Collection<Institution> getAllInstitutionenForGemeindeBenutzer() {
		Optional<Benutzer> benutzerOptional = benutzerService.getCurrentBenutzer();
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			return institutionStammdatenService.getAllInstitutionStammdaten()
				.stream()
				.filter(stammdaten -> stammdaten.isVisibleForGemeindeUser(benutzer))
				.map(InstitutionStammdaten::getInstitution)
				.collect(Collectors.toList());
		}

		return new ArrayList<>();
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
			if (benutzer.getRole().isRoleGemeindeabhaengig()) {
				return getAllInstitutionenForGemeindeBenutzer();
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

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, ADMIN_GEMEINDE, ADMIN_BG
		, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public void calculateStammdatenCheckRequired() {
		final Collection<Institution> allInstitutionen = this.getAllInstitutionen();

		// It will set the flag to true or to false accordingly to the value of calculateStammdatenCheckRequired().
		// This is better than only
		// setting it to true because it helps set the flag back to false even when it is incorrectly true or hasn't
		// been updated properly
		allInstitutionen
			.forEach(institution -> {
				final boolean isCheckRequired = calculateStammdatenCheckRequiredForInstitution(institution.getId());
				updateStammdatenCheckRequired(institution.getId(), isCheckRequired);
			});
	}

	@Nullable
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, ADMIN_GEMEINDE, ADMIN_BG
		, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Institution deactivateStammdatenCheckRequired(@Nonnull String institutionId) {
		InstitutionStammdaten stammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId);
		if (stammdaten != null) {
			// save stammdaten to update its timestamp_mutiert, since this field will be used to set the Flag
			// stammdatenCheckRequired
			stammdaten.setTimestampMutiert(LocalDateTime.now());
			institutionStammdatenService.saveInstitutionStammdaten(stammdaten);
		}

		return updateStammdatenCheckRequired(institutionId, false);
	}

	@Nullable
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_GEMEINDE, ADMIN_BG
		, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS })
	public Institution updateStammdatenCheckRequired(@Nonnull String institutionId, boolean isCheckRequired) {
		final Optional<Institution> institutionOpt = findInstitution(institutionId);

		final Institution institution = institutionOpt.orElseThrow(() -> new EbeguEntityNotFoundException(
			"updateStammdatenCheckRequired",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			institutionId));

		if (isCheckRequired != institution.isStammdatenCheckRequired()) {
			institution.setStammdatenCheckRequired(isCheckRequired);
			updateInstitution(institution);
		}

		return institution;
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public void removeInstitution(@Nonnull String institutionId) {
		final Optional<Institution> institutionOpt = findInstitution(institutionId);
		final Institution institution = institutionOpt.orElseThrow(() ->
			new EbeguEntityNotFoundException("removeInstitution",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId)
		);

		checkForLinkedBerechtigungen(institution);
		removeInstitutionFromBerechtigungHistory(institution);

		institutionStammdatenService.removeInstitutionStammdatenByInstitution(institutionId);
		persistence.remove(institution);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION })
	public void saveExternalClients(
		@Nonnull Institution institution,
		@Nonnull Collection<ExternalClient> externalClients) {

		String id = institution.getId();
		Set<ExternalClient> existingExternalClients = institution.getExternalClients();

		// find out which are removed
		HashSet<ExternalClient> removed = new HashSet<>(existingExternalClients);
		removed.removeAll(externalClients);

		removed.stream()
			.map(client -> institutionClientEventConverter.clientRemovedEventOf(id, client))
			.forEach(event -> exportedEvent.fire(event));

		// find out which are added
		HashSet<ExternalClient> added = new HashSet<>(externalClients);
		added.removeAll(existingExternalClients);

		added.stream()
			.map(client -> institutionClientEventConverter.clientAddedEventOf(id, client))
			.forEach(event -> exportedEvent.fire(event));

		institution.setExternalClients(new HashSet<>(externalClients));
	}

	private void checkForLinkedBerechtigungen(@Nonnull Institution institution) {
		final Collection<Berechtigung> linkedBerechtigungen = findBerechtigungByInstitution(institution);
		if (!linkedBerechtigungen.isEmpty()) {
			throw new EbeguRuntimeException("removeInstitution", ErrorCodeEnum.ERROR_LINKED_BERECHTIGUNGEN,
				institution.getId());
		}
	}

	private void removeInstitutionFromBerechtigungHistory(@Nonnull Institution institution) {
		final Collection<BerechtigungHistory> berechtigungHistories = criteriaQueryHelper.getEntitiesByAttribute(
			BerechtigungHistory.class,
			institution,
			BerechtigungHistory_.institution);

		for (BerechtigungHistory berechtigungHistory : berechtigungHistories) {
			persistence.remove(berechtigungHistory);
		}
	}

	private Collection<Berechtigung> findBerechtigungByInstitution(@Nonnull Institution institution) {
		requireNonNull(institution, "institution cannot be null");
		return criteriaQueryHelper.getEntitiesByAttribute(Berechtigung.class, institution, Berechtigung_.institution);
	}

	/**
	 * Checks if the Stammdaten of the given Institution need to be checked by the user. This happens when the
	 * stammdaten haven't
	 * been saved for a long time (usually 100 days)
	 */
	private boolean calculateStammdatenCheckRequiredForInstitution(@Nonnull String institutionId) {
		InstitutionStammdaten instStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId);

		LocalDateTime timestampMutiert = instStammdaten.getTimestampMutiert();

		return timestampMutiert != null
			&& timestampMutiert.isBefore(LocalDateTime.now().minusDays(Constants.DAYS_BEFORE_INSTITUTION_CHECK));
	}

	/**
	 * @param root Root darf Institution oder InstituionStammdaten sein
	 */
	private Predicate excludeUnknownInstitutionPredicate(Root<? extends AbstractEntity> root) {
		List<String> excludedIds = Arrays.asList(ID_UNKNOWN_INSTITUTION_KITA, ID_UNKNOWN_INSTITUTION_TAGESFAMILIE);

		return root.get(AbstractEntity_.id).in(excludedIds).not();
	}
}

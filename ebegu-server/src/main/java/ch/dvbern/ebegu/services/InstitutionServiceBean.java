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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.institutionclient.InstitutionClientEventConverter;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.collections4.map.HashedMap;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Institution
 */
@Stateless
@Local(InstitutionService.class)
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

	@Inject
	private Authorizer authorizer;

	@Inject
	private GesuchService gesuchService;

	@Nonnull
	@Override
	public Institution updateInstitution(@Nonnull Institution institution) {
		Objects.requireNonNull(institution);
		authorizer.checkWriteAuthorizationInstitution(institution);
		return persistence.merge(institution);
	}

	@Nonnull
	@Override
	// same as updateInstitution but without ADMIN_INSTITUTION
	public Institution createInstitution(@Nonnull Institution institution) {
		Objects.requireNonNull(institution);
		authorizer.checkWriteAuthorizationInstitution(institution);

		institution.setMandant(requireNonNull(principalBean.getMandant()));
		return persistence.persist(institution);
	}

	@Nonnull
	@Override
	public Optional<Institution> findInstitution(@Nonnull final String id, boolean doAuthCheck) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Institution institution = persistence.find(Institution.class, id);
		if (doAuthCheck) {
			authorizer.checkReadAuthorizationInstitution(institution);
		}
		return Optional.ofNullable(institution);
	}

	@Override
	@Nonnull
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

	@Override
	@Nonnull
	public Collection<Institution> getAllInstitutionen(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.institution));
		query.distinct(true);
		List<Predicate> predicates = new ArrayList<>();

		predicates.add(PredicateHelper.excludeUnknownInstitutionStammdatenPredicate(root));

		Predicate mandantMatches = cb.equal(root.get(InstitutionStammdaten_.institution).get(Institution_.mandant), mandant);
		predicates.add(mandantMatches);

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

	@Override
	@Nonnull
	public Collection<Institution> getAllInstitutionenForBatchjobs() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Institution> query = cb.createQuery(Institution.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);
		query.select(root.get(InstitutionStammdaten_.institution));
		query.distinct(true);

		List<Predicate> predicates = new ArrayList<>();
		Path<DateRange> dateRangePath = root.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateActive = cb.between(
			cb.literal(LocalDate.now()),
			dateRangePath.get(DateRange_.gueltigAb),
			dateRangePath.get(DateRange_.gueltigBis)
		);
		predicates.add(predicateActive);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	private Collection<Institution> getAllInstitutionenForGemeindeBenutzer(
		boolean editable,
		boolean restrictedForSCH) {
		Optional<Benutzer> benutzerOptional = benutzerService.getCurrentBenutzer();
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			return institutionStammdatenService.getAllInstitutionStammdaten()
				.stream()
				.filter(stammdaten -> isAllowedForMode(stammdaten, benutzer, editable, restrictedForSCH))
				.map(InstitutionStammdaten::getInstitution)
				.collect(Collectors.toList());
		}

		return new ArrayList<>();
	}

	private boolean isAllowedForMode(
		@Nonnull InstitutionStammdaten institutionStammdaten, @Nonnull Benutzer benutzer, boolean editMode,
		boolean restrictedForSCH
	) {
		if (editMode) {
			return authorizer.isWriteAuthorizationInstitutionStammdaten(institutionStammdaten);
		}
		// Falls das restricted-Flag gesetzt ist, ist nicht einmal lesen erlaubt
		if (restrictedForSCH && benutzer.getRole().isRoleTsOnly()) {
			return false;
		}
		return authorizer.isReadAuthorizationInstitutionStammdaten(institutionStammdaten);
	}

	@Override
	public Collection<Institution> getInstitutionenEditableForCurrentBenutzer(boolean restrictedForSCH) {
		return getInstitutionenForCurrentBenutzer(true, restrictedForSCH);
	}

	@Override
	@Nonnull
	public Collection<Institution> getInstitutionenReadableForCurrentBenutzer(boolean restrictedForSCH) {
		return getInstitutionenForCurrentBenutzer(false, restrictedForSCH);
	}

	private Collection<Institution> getInstitutionenForCurrentBenutzer(boolean canEdit, boolean restrictedForSCH) {
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
			if (benutzer.getRole().isRoleGemeindeabhaengig()) {
				return getAllInstitutionenForGemeindeBenutzer(canEdit, restrictedForSCH);
			}
			Objects.requireNonNull(benutzer.getMandant());
			return getAllInstitutionen(benutzer.getMandant());
		}
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public Map<Institution, InstitutionStammdaten> getInstitutionenInstitutionStammdatenEditableForCurrentBenutzer(
		boolean restrictedForSCH) {
		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap = new HashedMap<>();

		Optional<Benutzer> benutzerOptional = benutzerService.getCurrentBenutzer();
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			if (EnumUtil.isOneOf(benutzer.getRole(), UserRole.ADMIN_INSTITUTION, UserRole.SACHBEARBEITER_INSTITUTION)
				&& benutzer.getInstitution() != null) {
				if (benutzer.getInstitution() != null) {
					institutionInstitutionStammdatenMap.put(
						benutzer.getInstitution(),
						institutionStammdatenService.fetchInstitutionStammdatenByInstitution(benutzer.getInstitution()
							.getId(), false));
				}
			} else {
				if (EnumUtil.isOneOf(
					benutzer.getRole(),
					UserRole.ADMIN_TRAEGERSCHAFT,
					UserRole.SACHBEARBEITER_TRAEGERSCHAFT) && benutzer.getTraegerschaft() != null) {
					// Hier suchen wir direkt die Institutionen die sind mit der Traegerschaft verbundet
					institutionStammdatenService.getAllInstitutionStammdatenForTraegerschaft(benutzer.getTraegerschaft())
						.forEach(institutionStammdaten -> {
							institutionInstitutionStammdatenMap.put(
								institutionStammdaten.getInstitution(),
								institutionStammdaten);
						});
				} else if (benutzer.getRole().isRoleGemeindeabhaengig()) {
					// Hier gibt schon in getAllInstitutionStammdaten der GemeindeListe predicate
					institutionStammdatenService.getAllInstitutionStammdaten().forEach(institutionStammdaten -> {
						if (isAllowedForMode(institutionStammdaten, benutzer, true, restrictedForSCH)) {
							institutionInstitutionStammdatenMap.put(
								institutionStammdaten.getInstitution(),
								institutionStammdaten);
						}
					});
				} else {
					// Hier muss man ja alles lesen fuer der Mandant
					institutionStammdatenService.getAllInstitutionStammdaten().forEach(institutionStammdaten -> {
						institutionInstitutionStammdatenMap.put(
							institutionStammdaten.getInstitution(),
							institutionStammdaten);
					});
				}
			}
		}
		return institutionInstitutionStammdatenMap;
	}

	@Nonnull
	@Override
	public boolean isCurrentUserTagesschuleNutzende(boolean restrictedForSCH) {
		return getInstitutionenReadableForCurrentBenutzer(restrictedForSCH).stream().anyMatch(institution -> {
			AtomicBoolean isTagesschule = new AtomicBoolean(false);
			InstitutionStammdaten institutionStammdaten =
				institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institution.getId(), true);

			isTagesschule.set(institutionStammdaten.getBetreuungsangebotTyp().isTagesschule());

			return isTagesschule.get();
		});
	}

	@Override
	public Map<Institution, InstitutionStammdaten> getInstitutionenInstitutionStammdatenForGemeinde(Gemeinde gemeinde) {
		Map<Institution, InstitutionStammdaten> institutionInstitutionStammdatenMap = new HashedMap<>();

		institutionStammdatenService.getAllTagesschulenForGemeinde(gemeinde)
			.forEach(institutionStammdaten -> {
				institutionInstitutionStammdatenMap.put(
					institutionStammdaten.getInstitution(),
					institutionStammdaten);
			});
		return institutionInstitutionStammdatenMap;
	}

	@Override
	public BetreuungsangebotTyp getAngebotFromInstitution(@Nonnull String institutionId) {
		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId, true);
		authorizer.checkReadAuthorizationInstitutionStammdaten(institutionStammdaten);
		return institutionStammdaten.getBetreuungsangebotTyp();
	}

	@Override
	public void updateAllStammdatenCheckRequired() {
		Collection<Institution> allInstitutionen = getAllInstitutionenForBatchjobs();

		allInstitutionen
			.forEach(institution -> {
				updateStammdatenCheckRequired(
					institution.getId(),
					true);
			});
	}

	@Override
	public void deactivateStammdatenCheckRequired(@Nonnull String institutionId) {
		InstitutionStammdaten stammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institutionId, true);
		if (stammdaten != null) {
			// save stammdaten to update its timestamp_mutiert, since this field will be used to set the Flag
			// stammdatenCheckRequired
			stammdaten.setTimestampMutiert(LocalDateTime.now());
			institutionStammdatenService.saveInstitutionStammdaten(stammdaten);
		}

		updateStammdatenCheckRequired(institutionId, false);
	}

	@Override
	public void updateStammdatenCheckRequired(@Nonnull String institutionId, boolean isCheckRequired) {
		final Optional<Institution> institutionOpt = findInstitution(institutionId, false);

		final Institution institution = institutionOpt.orElseThrow(() -> new EbeguEntityNotFoundException(
			"updateStammdatenCheckRequired",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			institutionId));

		if (isCheckRequired != institution.isStammdatenCheckRequired()) {
			institution.setStammdatenCheckRequired(isCheckRequired);
			persistence.merge(institution); // direkt ueber persistence.merge wegen Berechtigung Batchjob
		}
	}

	@Override
	public void removeInstitution(@Nonnull String institutionId) {
		final Optional<Institution> institutionOpt = findInstitution(institutionId, true);
		final Institution institution = institutionOpt.orElseThrow(() ->
			new EbeguEntityNotFoundException("removeInstitution",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionId)
		);
		authorizer.checkWriteAuthorizationInstitution(institution);

		checkForLinkedBerechtigungen(institution);
		removeInstitutionFromBerechtigungHistory(institution);

		institutionStammdatenService.removeInstitutionStammdatenByInstitution(institutionId);
		persistence.remove(institution);
	}

	@Override
	public void saveInstitutionExternalClients(
		@Nonnull Institution institution,
		@Nonnull Collection<InstitutionExternalClient> institutionExternalClients) {

		String id = institution.getId();

		Set<InstitutionExternalClient> existingExternalClients = institution.getInstitutionExternalClients();

		Collection<InstitutionExternalClient> newInstitutionExternalClients = new HashSet<>();

		// find out which are modified and update the Gueltigkeit inside
		if (!existingExternalClients.isEmpty()) {
			for (InstitutionExternalClient institutionExternalClient : existingExternalClients) {
				InstitutionExternalClient existingInstitutionExternalClient = institutionExternalClients.stream()
					.filter(institutionExternalClient1 -> institutionExternalClient1.getExternalClient()
						.getClientName()
						.equals(institutionExternalClient.getExternalClient().getClientName()))
					.findAny().orElse(null);
				if (existingInstitutionExternalClient != null) {
					//set parameters inside the existing one if modified and fire event
					if (existingInstitutionExternalClient.getGueltigkeit()
						.compareTo(institutionExternalClient.getGueltigkeit()) != 0) {
						institutionExternalClient.setGueltigkeit(existingInstitutionExternalClient.getGueltigkeit());
						exportedEvent.fire(institutionClientEventConverter.clientModifiedEventOf(
							id,
							institutionExternalClient));
					}
					//then add to the new collection
					newInstitutionExternalClients.add(institutionExternalClient);
					//and delete it otherwise it will be seen as a new element later
					institutionExternalClients.remove(existingInstitutionExternalClient);
				} else { //it means this client was removed
					exportedEvent.fire(institutionClientEventConverter.clientRemovedEventOf(
						id,
						institutionExternalClient));
				}
			}
		}

		// find out which are added
		HashSet<InstitutionExternalClient> added = new HashSet<>(institutionExternalClients);

		added.stream()
			.map(client -> institutionClientEventConverter.clientAddedEventOf(id, client))
			.forEach(event -> exportedEvent.fire(event));
		newInstitutionExternalClients.addAll(added);

		institution.getInstitutionExternalClients().clear();
		institution.getInstitutionExternalClients().addAll(new HashSet<>(newInstitutionExternalClients));
	}

	@Override
	public Collection<Institution> findAllInstitutionen(
		@Nonnull String dossierId) {
		List<Institution> institutions = new ArrayList<>();
		gesuchService.getAllGesuchForDossier(dossierId).forEach(
			gesuch -> {
				gesuch.extractAllBetreuungen().forEach(
					betreuung -> {
						if (principalBean.getBenutzer().getTraegerschaft() != null &&
							!principalBean.getBenutzer().getTraegerschaft().equals(betreuung.getInstitutionStammdaten().getInstitution().getTraegerschaft())) {
								return;
						}
						if (!institutions.contains(betreuung.getInstitutionStammdaten().getInstitution())) {
							institutions.add(betreuung.getInstitutionStammdaten().getInstitution());
						}
					}
				);
				gesuch.extractAllAnmeldungen().forEach(anmeldung -> {
					if (!institutions.contains(anmeldung.getInstitutionStammdaten().getInstitution())) {
						institutions.add(anmeldung.getInstitutionStammdaten().getInstitution());
					}
				});
			}
		);

		return institutions;
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
		return criteriaQueryHelper.getEntitiesByAttribute(
			Berechtigung.class,
			institution,
			Berechtigung_.institution);
	}
}

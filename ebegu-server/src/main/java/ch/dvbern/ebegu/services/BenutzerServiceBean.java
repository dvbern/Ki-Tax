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

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Traegerschaft_;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.SearchMode;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRole.getJugendamtRoles;
import static ch.dvbern.ebegu.enums.UserRole.getSchulamtRoles;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setInstitutionFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setRoleFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setTraegerschaftFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Benutzer
 */
@PermitAll
@Stateless
@Local(BenutzerService.class)
public class BenutzerServiceBean extends AbstractBaseService implements BenutzerService {

	private static final Logger LOG = LoggerFactory.getLogger(BenutzerServiceBean.class.getSimpleName());

	public static final String ID_SUPER_ADMIN = "22222222-2222-2222-2222-222222222222";

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private AuthService authService;

	@Inject
	private MailService mailService;

	@Inject
	private Authorizer authorizer;


	@Nonnull
	@Override
	@PermitAll
	public Benutzer saveBenutzerBerechtigungen(@Nonnull Benutzer benutzer, boolean currentBerechtigungChanged) {
		requireNonNull(benutzer);
		prepareBenutzerForSave(benutzer, currentBerechtigungChanged);
		authorizer.checkWriteAuthorization(benutzer);
		return persistence.merge(benutzer);
	}

	@Nonnull
	@Override
	@PermitAll
	public Benutzer saveBenutzer(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer);
		authorizer.checkWriteAuthorization(benutzer);
		if (benutzer.isNew()) {
			return persistence.persist(benutzer);
		}
		return persistence.merge(benutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({
		SUPER_ADMIN,
		ADMIN_BG,
		ADMIN_GEMEINDE,
		ADMIN_TS,
		ADMIN_MANDANT,
		ADMIN_INSTITUTION,
		ADMIN_TRAEGERSCHAFT,
	})
	public Benutzer einladen(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer);
		checkArgument(benutzer.getStatus() == BenutzerStatus.EINGELADEN, "Benutzer should have Status EINGELADEN");
		checkArgument(benutzer.isNew(), "Cannot einladen an existing Benutzer");
		checkArgument(Objects.equals(benutzer.getMandant(), principalBean.getMandant()));

		if (findBenutzer(benutzer.getUsername()).isPresent()) {
			throw new EntityExistsException(Benutzer.class, "email", benutzer.getUsername());
		}

		Benutzer persisted = saveBenutzer(benutzer);

		try {
			mailService.sendBenutzerEinladung(principalBean.getBenutzer(), persisted);
		} catch (MailException e) {
			String message =
				String.format("Es konnte keine Email Einladung an %s geschickt werden", benutzer.getEmail());
			throw new EbeguRuntimeException("sendEinladung", message, ErrorCodeEnum.ERROR_MAIL, e);
		}

		return persisted;
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzer(@Nonnull String username) {
		requireNonNull(username, "username muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, username, Benutzer_.username);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzerByEmail(@Nonnull String email) {
		Objects.requireNonNull(email, "email muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, email, Benutzer_.email);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> findBenutzerByExternalUUID(@Nonnull String externalUUID) {
		requireNonNull(externalUUID, "externalUUID muss gesetzt sein");
		return criteriaQueryHelper.getEntityByUniqueAttribute(Benutzer.class, externalUUID, Benutzer_.externalUUID);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getAllBenutzer() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Benutzer.class));
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerBGorAdmin() {
		return getBenutzersOfRoles(getJugendamtRoles());
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Benutzer> getBenutzerSCHorAdminSCH() {
		return getBenutzersOfRoles(getSchulamtRoles());
	}

	private Collection<Benutzer> getBenutzersOfRoles(List<UserRole> roles) {

		Benutzer currentBenutzer = getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		SetJoin<Berechtigung, Gemeinde> joinGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList, JoinType.LEFT);
		query.select(root);

		predicates.add(cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));

		setGemeindeFilterForCurrentUser(currentBenutzer, joinGemeinde, predicates);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	public Collection<Benutzer> getGesuchsteller() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(Benutzer_.berechtigungen);
		query.select(root);

		Predicate predicateActive = cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		Predicate predicateRole = joinBerechtigungen.get(Berechtigung_.role).in(GESUCHSTELLER);
		query.where(predicateActive, predicateRole);
		query.orderBy(cb.asc(root.get(Benutzer_.vorname)), cb.asc(root.get(Benutzer_.nachname)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	public void removeBenutzer(@Nonnull String username) {
		requireNonNull(username);
		Benutzer benutzer = findBenutzer(username).orElseThrow(() -> new EbeguEntityNotFoundException(
			"removeBenutzer",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			username));

		authorizer.checkWriteAuthorization(benutzer);

		// Den Benutzer ausloggen und seine AuthBenutzer loeschen
		authService.logoutAndDeleteAuthorisierteBenutzerForUser(username);
		removeBerechtigungHistoryForBenutzer(benutzer);
		persistence.remove(benutzer);
	}

	private void removeBerechtigungHistoryForBenutzer(@Nonnull Benutzer benutzer) {
		Collection<BerechtigungHistory> histories = getBerechtigungHistoriesForBenutzer(benutzer);
		for (BerechtigungHistory history : histories) {
			persistence.remove(history);
		}
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Benutzer> getCurrentBenutzer() {
		String username = null;
		if (principalBean != null) {
			final Principal principal = principalBean.getPrincipal();
			username = principal.getName();
		}
		if (StringUtils.isNotEmpty(username)) {
			if (Constants.ANONYMOUS_USER_USERNAME.equals(username) && principalBean.isCallerInRole(UserRole.SUPER_ADMIN.name())) {
				return loadSuperAdmin();
			}
			return findBenutzer(username);
		}
		return Optional.empty();
	}

	@Override
	@PermitAll
	public Benutzer updateOrStoreUserFromIAM(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer.getExternalUUID());
		Optional<Benutzer> foundUserOptional = this.findBenutzerByExternalUUID(benutzer.getExternalUUID());

		if (foundUserOptional.isPresent()) {
			// Wir kennen den Benutzer schon: Es werden nur die readonly-Attribute neu von IAM uebernommen
			Benutzer foundUser = foundUserOptional.get();
			// Wir ueberpruefen, ob der Username sich geaendert hat
			if (!foundUser.getUsername().equals(benutzer.getUsername())) {
				LOG.warn("External User has new Username: ExternalUUID {}, old username {}, new username {}. "
						+ "Updating!",
					benutzer.getExternalUUID(), foundUser.getUsername(), benutzer.getUsername());
				foundUser.setUsername(benutzer.getUsername());
			}
			// den username ueberschreiben wir nicht!
			foundUser.setNachname(benutzer.getNachname());
			foundUser.setVorname(benutzer.getVorname());
			foundUser.setEmail(benutzer.getEmail());

			return saveBenutzer(foundUser);
		}

		// Wir kennen den Benutzer noch nicht: Wir uebernehmen alles, setzen aber grundsätzlich die Rolle auf
		// GESUCHSTELLER
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(GESUCHSTELLER);
		berechtigung.setInstitution(null);
		berechtigung.setTraegerschaft(null);
		berechtigung.setBenutzer(benutzer);
		benutzer.getBerechtigungen().clear();
		benutzer.getBerechtigungen().add(berechtigung);

		return saveBenutzer(benutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public Benutzer sperren(@Nonnull String username) {
		Benutzer benutzerFromDB = findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"sperren",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchId invalid: " + username));

		authorizer.checkWriteAuthorization(benutzerFromDB);

		benutzerFromDB.setStatus(BenutzerStatus.GESPERRT);
		int deletedAuthBenutzer = authService.logoutAndDeleteAuthorisierteBenutzerForUser(username);
		logSperreBenutzer(benutzerFromDB, deletedAuthBenutzer);

		return persistence.merge(benutzerFromDB);
	}

	private void logSperreBenutzer(@Nonnull Benutzer benutzer, int deletedAuthBenutzer) {
		LOG.info(
			"Setze Benutzer auf GESPERRT: {} / Eingeloggt: {} / Lösche {} Eintraege aus der AuthorisierteBenutzer"
				+ " Tabelle",
			benutzer.getUsername(),
			principalBean.getBenutzer().getUsername(),
			deletedAuthBenutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT })
	public Benutzer reaktivieren(@Nonnull String username) {
		Benutzer benutzerFromDB = findBenutzer(username)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"reaktivieren",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchId invalid: " + username));

		authorizer.checkWriteAuthorization(benutzerFromDB);

		benutzerFromDB.setStatus(BenutzerStatus.AKTIV);
		logReaktivierenBenutzer(benutzerFromDB);

		return persistence.merge(benutzerFromDB);
	}

	private void logReaktivierenBenutzer(Benutzer benutzerFromDB) {
		LOG.info(
			"Reaktiviere Benutzer: {} / Eingeloggt: {}",
			benutzerFromDB.getUsername(),
			principalBean.getBenutzer().getUsername());
	}

	private void prepareBenutzerForSave(@Nonnull Benutzer benutzer, boolean currentBerechtigungChanged) {
		List<Berechtigung> allSortedBerechtigungen = new LinkedList<>(benutzer.getBerechtigungen());
		allSortedBerechtigungen.sort(Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb()));

		final Berechtigung currentBerechtigung = allSortedBerechtigungen.get(0);

		handleGueltigkeitCurrentBerechtigung(allSortedBerechtigungen,
			currentBerechtigung, currentBerechtigungChanged);

		for (Berechtigung berechtigung : allSortedBerechtigungen) {
			prepareBerechtigungForSave(berechtigung);
		}

		// Ausloggen nur, wenn die aktuelle Berechtigung geändert hat
		if (currentBerechtigungChanged) {
			LOG.info(
				"Aktuelle Berechtigung des Benutzers {} hat geändert, Benutzer wird ausgeloggt",
				benutzer.getUsername());
			authService.logoutAndDeleteAuthorisierteBenutzerForUser(benutzer.getUsername());
		}
	}

	/**
	 * If there are future Berechtigungen it sets the gueltigBis of the currentBerechtigung to one day before the
	 * gueltigAb of the futureBerechtigung.
	 * For no futureBerechtigungen it sets the gueltigBis of the currentBerechtigung to END_OF_TIME
	 * If the currentBerechtigung changed it sets the gueltigAb of the currentBerechtigung to now()
	 */
	private void handleGueltigkeitCurrentBerechtigung(
		@Nonnull List<Berechtigung> allSortedBerechtigungen,
		@Nonnull Berechtigung currentBerechtigung,
		boolean currentBerechtigungChanged) {

		currentBerechtigung.getGueltigkeit().setGueltigBis(
			allSortedBerechtigungen.size() > 1 ?
				allSortedBerechtigungen.get(1).getGueltigkeit().getGueltigAb().minusDays(1) :
				Constants.END_OF_TIME
		);

		if (currentBerechtigungChanged) {
			currentBerechtigung.getGueltigkeit().setGueltigAb(LocalDate.now());
		}
	}

	private void prepareBerechtigungForSave(@Nonnull Berechtigung berechtigung) {
		// Es darf nur eine Institution gesetzt sein, wenn die Rolle INSTITUTION ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_INSTITUTION,
			UserRole.SACHBEARBEITER_INSTITUTION)) {
			berechtigung.setInstitution(null);
		}
		// Es darf nur eine Trägerschaft gesetzt sein, wenn die Rolle TRAEGERSCHAFT ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_TRAEGERSCHAFT,
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT)) {
			berechtigung.setTraegerschaft(null);
		}
	}

	private Optional<Benutzer> loadSuperAdmin() {
		Benutzer benutzer = persistence.find(Benutzer.class, ID_SUPER_ADMIN);
		return Optional.ofNullable(benutzer);
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_MANDANT, REVISOR })
	public Pair<Long, List<Benutzer>> searchBenutzer(@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto) {
		Long countResult = searchBenutzer(benutzerTableFilterDto, SearchMode.COUNT).getLeft();

		if (countResult.equals(0L)) {    // no result found
			return new ImmutablePair<>(0L, Collections.emptyList());
		}

		Pair<Long, List<Benutzer>> searchResult = searchBenutzer(benutzerTableFilterDto, SearchMode.SEARCH);
		return new ImmutablePair<>(countResult, searchResult.getRight());
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	private Pair<Long, List<Benutzer>> searchBenutzer(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDTO,
		@Nonnull SearchMode mode) {

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		@SuppressWarnings("rawtypes") // Je nach Abfrage ist es String oder Long
			CriteriaQuery query = SearchUtil.getQueryForSearchMode(cb, mode, "searchBenutzer");

		// Construct from-clause
		@SuppressWarnings("unchecked") // Je nach Abfrage ist das Query String oder Long
			Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> currentBerechtigungJoin = root.join(Benutzer_.berechtigungen);
		Join<Berechtigung, Institution> institutionJoin =
			currentBerechtigungJoin.join(Berechtigung_.institution, JoinType.LEFT);
		Join<Berechtigung, Traegerschaft> traegerschaftJoin =
			currentBerechtigungJoin.join(Berechtigung_.traegerschaft, JoinType.LEFT);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin =
			currentBerechtigungJoin.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<>();

		// General role based predicates
		Benutzer user =
			getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException("searchBenutzer", "No User is logged "
				+ "in"));

		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)) {
			// Not SuperAdmin users are allowed to see all users of their mandant
			predicates.add(cb.equal(root.get(Benutzer_.mandant), user.getMandant()));

			// They cannot see superadmin users
			predicates.add(cb.notEqual(currentBerechtigungJoin.get(Berechtigung_.role), UserRole.SUPER_ADMIN));

			setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

			setRoleFilterForCurrentUser(user, currentBerechtigungJoin, predicates);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_INSTITUTION)) {
			setInstitutionFilterForCurrentUser(user, currentBerechtigungJoin, cb, predicates);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_TRAEGERSCHAFT)) {
			setTraegerschaftFilterForCurrentUser(user, currentBerechtigungJoin, cb, predicates);
		}

		//prepare predicates from table filters
		if (benutzerTableFilterDTO.getSearch() != null) {
			BenutzerPredicateObjectDTO predicateObjectDto = benutzerTableFilterDTO.getSearch().getPredicateObject();
			// username
			if (predicateObjectDto.getUsername() != null) {
				Expression<String> expression = root.get(Benutzer_.username).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getUsername());
				predicates.add(cb.like(expression, value));
			}
			// vorname
			if (predicateObjectDto.getVorname() != null) {
				Expression<String> expression = root.get(Benutzer_.vorname).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getVorname());
				predicates.add(cb.like(expression, value));
			}
			// nachname
			if (predicateObjectDto.getNachname() != null) {
				Expression<String> expression = root.get(Benutzer_.nachname).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getNachname());
				predicates.add(cb.like(expression, value));
			}
			// email
			if (predicateObjectDto.getEmail() != null) {
				Expression<String> expression = root.get(Benutzer_.email).as(String.class);
				String value = SearchUtil.withWildcards(predicateObjectDto.getEmail());
				predicates.add(cb.like(expression, value));
			}
			// role
			if (predicateObjectDto.getRole() != null) {
				predicates.add(cb.equal(currentBerechtigungJoin.get(Berechtigung_.role), predicateObjectDto.getRole()));
			}
			// roleGueltigBis
			if (predicateObjectDto.getRoleGueltigBis() != null) {
				try {
					LocalDate searchDate =
						LocalDate.parse(predicateObjectDto.getRoleGueltigBis(), Constants.DATE_FORMATTER);
					predicates.add(cb.equal(currentBerechtigungJoin.get(AbstractDateRangedEntity_.gueltigkeit)
						.get(DateRange_.gueltigBis), searchDate));
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			// gemeinde
			if (predicateObjectDto.getGemeinde() != null) {
				predicates.add(cb.equal(gemeindeSetJoin.get(Gemeinde_.name), predicateObjectDto.getGemeinde()));
			}
			// institution
			if (predicateObjectDto.getInstitution() != null) {
				predicates.add(cb.equal(institutionJoin.get(Institution_.name), predicateObjectDto.getInstitution()));
			}
			// traegerschaft
			if (predicateObjectDto.getTraegerschaft() != null) {
				predicates.add(cb.equal(
					traegerschaftJoin.get(Traegerschaft_.name),
					predicateObjectDto.getTraegerschaft()));
			}
			// gesperrt
			if (predicateObjectDto.getStatus() != null) {
				predicates.add(cb.equal(root.get(Benutzer_.status), predicateObjectDto.getStatus()));
			}
		}
		// Construct the select- and where-clause
		switch (mode) {
		case SEARCH:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(root.get(AbstractEntity_.id));
			if (!predicates.isEmpty()) {
				query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			}
			constructOrderByClause(
				benutzerTableFilterDTO,
				cb,
				query,
				root,
				currentBerechtigungJoin,
				institutionJoin,
				traegerschaftJoin,
				gemeindeSetJoin);
			break;
		case COUNT:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(cb.countDistinct(root.get(AbstractEntity_.id)));
			if (!predicates.isEmpty()) {
				query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			}
			break;
		}

		// Prepare and execute the query and build the result
		Pair<Long, List<Benutzer>> result = null;
		switch (mode) {
		case SEARCH:
			//select all ids in order, may contain duplicates
			List<String> benutzerIds = persistence.getCriteriaResults(query);
			List<Benutzer> pagedResult;
			if (benutzerTableFilterDTO.getPagination() != null) {
				int firstIndex = benutzerTableFilterDTO.getPagination().getStart();
				Integer maxresults = benutzerTableFilterDTO.getPagination().getNumber();
				List<String> orderedIdsToLoad =
					SearchUtil.determineDistinctIdsToLoad(benutzerIds, firstIndex, maxresults);
				pagedResult = findBenutzer(orderedIdsToLoad);
			} else {
				pagedResult = findBenutzer(benutzerIds);
			}
			result = new ImmutablePair<>(null, pagedResult);
			break;
		case COUNT:
			Long count = (Long) persistence.getCriteriaSingleResult(query);
			result = new ImmutablePair<>(count, null);
			break;
		}
		return result;
	}

	private void constructOrderByClause(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto, CriteriaBuilder cb, CriteriaQuery query,
		Root<Benutzer> root,
		Join<Benutzer, Berechtigung> currentBerechtigung,
		Join<Berechtigung, Institution> institution,
		Join<Berechtigung, Traegerschaft> traegerschaft,
		@Nonnull SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin) {
		Expression<?> expression;
		if (benutzerTableFilterDto.getSort() != null && benutzerTableFilterDto.getSort().getPredicate() != null) {
			switch (benutzerTableFilterDto.getSort().getPredicate()) {
			case "username":
				expression = root.get(Benutzer_.username);
				break;
			case "vorname":
				expression = root.get(Benutzer_.vorname);
				break;
			case "nachname":
				expression = root.get(Benutzer_.nachname);
				break;
			case "email":
				expression = root.get(Benutzer_.email);
				break;
			case "role":
				expression = currentBerechtigung.get(Berechtigung_.role);
				break;
			case "roleGueltigBis":
				expression = currentBerechtigung.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis);
				break;
			case "gemeinde":
				// Die Gemeinden sind eine Liste innerhalb der Liste (also des Tabelleneintrages).
				// Berechtigungen ohne Gemeinde sollen egal wie sortiert ist am Schluss kommen!
				if (benutzerTableFilterDto.getSort().getReverse()) {
					expression = cb.selectCase().when(gemeindeSetJoin.isNull(), "ZZZZ")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				} else {
					expression = cb.selectCase().when(gemeindeSetJoin.isNull(), "0000")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				}
				break;
			case "institution":
				expression = institution.get(Institution_.name);
				break;
			case "traegerschaft":
				expression = traegerschaft.get(Traegerschaft_.name);
				break;
			case "status":
				expression = root.get(Benutzer_.status);
				break;
			default:
				LOG.warn(
					"Using default sort by Timestamp mutiert because there is no specific clause for predicate {}",
					benutzerTableFilterDto.getSort().getPredicate());
				expression = root.get(AbstractEntity_.timestampMutiert);
				break;
			}
			query.orderBy(benutzerTableFilterDto.getSort().getReverse() ? cb.asc(expression) : cb.desc(expression));
		} else {
			// Default sort when nothing is choosen
			expression = root.get(AbstractEntity_.timestampMutiert);
			query.orderBy(cb.desc(expression));
		}
	}

	private List<Benutzer> findBenutzer(@Nonnull List<String> benutzerIds) {
		if (!benutzerIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
			Root<Benutzer> root = query.from(Benutzer.class);
			Predicate predicate = root.get(AbstractEntity_.id).in(benutzerIds);
			query.where(predicate);
			//reduce to unique Benutzer
			List<Benutzer> listWithDuplicates = persistence.getCriteriaResults(query);
			LinkedHashSet<Benutzer> setOfBenutzer = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String userId : benutzerIds) {
				listWithDuplicates.stream()
					.filter(benutzer -> benutzer.getId().equals(userId))
					.findFirst()
					.ifPresent(setOfBenutzer::add);
			}
			return new ArrayList<>(setOfBenutzer);
		}
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public int handleAbgelaufeneRollen(@Nonnull LocalDate stichtag) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> currentBerechtigung = root.join(Benutzer_.berechtigungen);
		Predicate predicateAbgelaufen =
			cb.lessThan(
				currentBerechtigung.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
				stichtag);
		query.where(predicateAbgelaufen);
		List<Benutzer> userMitAbgelaufenerRolle = persistence.getCriteriaResults(query);

		for (Benutzer benutzer : userMitAbgelaufenerRolle) {
			List<Berechtigung> abgelaufeneBerechtigungen = new ArrayList<>();
			for (Berechtigung berechtigung : benutzer.getBerechtigungen()) {
				if (berechtigung.isAbgelaufen()) {
					abgelaufeneBerechtigungen.add(berechtigung);
				}
			}
			try {
				Berechtigung aktuelleBerechtigung = getAktuellGueltigeBerechtigungFuerBenutzer(benutzer);
				persistence.merge(aktuelleBerechtigung);
			} catch (NoResultException nre) {
				// Sonderfall: Die letzte Berechtigung ist abgelaufen. Wir erstellen sofort eine neue anschliessende
				// Berechtigung als Gesuchsteller
				Berechtigung futureGesuchstellerBerechtigung =
					createFutureBerechtigungAsGesuchsteller(LocalDate.now(), benutzer);
				persistence.persist(futureGesuchstellerBerechtigung);
			}
			// Die abgelaufene Rolle löschen
			for (Berechtigung abgelaufeneBerechtigung : abgelaufeneBerechtigungen) {
				LOG.info("Benutzerrolle ist abgelaufen: {}, war: {}, abgelaufen: {}", benutzer.getUsername(),
					abgelaufeneBerechtigung.getRole(), abgelaufeneBerechtigung.getGueltigkeit().getGueltigBis());
				benutzer.getBerechtigungen().remove(abgelaufeneBerechtigung);
				persistence.merge(benutzer);
				removeBerechtigung(abgelaufeneBerechtigung);
			}

		}
		return userMitAbgelaufenerRolle.size();
	}

	private Berechtigung createFutureBerechtigungAsGesuchsteller(LocalDate startDatum, Benutzer benutzer) {
		Berechtigung futureGesuchstellerBerechtigung = new Berechtigung();
		futureGesuchstellerBerechtigung.getGueltigkeit().setGueltigAb(startDatum);
		futureGesuchstellerBerechtigung.getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);
		futureGesuchstellerBerechtigung.setRole(GESUCHSTELLER);
		futureGesuchstellerBerechtigung.setBenutzer(benutzer);
		return futureGesuchstellerBerechtigung;
	}

	@Nonnull
	private Berechtigung getAktuellGueltigeBerechtigungFuerBenutzer(@Nonnull Benutzer benutzer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Berechtigung> query = cb.createQuery(Berechtigung.class);
		Root<Berechtigung> root = query.from(Berechtigung.class);

		ParameterExpression<Benutzer> benutzerParam = cb.parameter(Benutzer.class, "benutzer");
		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");

		Predicate predicateBenutzer = cb.equal(root.get(Berechtigung_.benutzer), benutzerParam);
		Predicate predicateZeitraum = cb.between(
			dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		query.where(predicateBenutzer, predicateZeitraum);

		TypedQuery<Berechtigung> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dateParam, LocalDate.now());
		q.setParameter(benutzerParam, benutzer);
		List<Berechtigung> resultList = q.getResultList();

		if (resultList.isEmpty()) {
			throw new NoResultException("No Berechtigung found for Benutzer" + benutzer.getUsername());
		}
		if (resultList.size() > 1) {
			throw new NonUniqueResultException("More than one Berechtigung found for Benutzer "
				+ benutzer.getUsername());
		}
		return resultList.get(0);
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN, ADMIN_TS })
	public Optional<Berechtigung> findBerechtigung(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");
		return Optional.ofNullable(persistence.find(Berechtigung.class, id));
	}

	private void removeBerechtigung(@Nonnull Berechtigung berechtigung) {
		authService.logoutAndDeleteAuthorisierteBenutzerForUser(berechtigung.getBenutzer().getUsername());
		persistence.remove(berechtigung);
	}

	@Override
	@PermitAll
	public void saveBerechtigungHistory(@Nonnull Berechtigung berechtigung, boolean deleted) {
		BerechtigungHistory newBerechtigungsHistory = new BerechtigungHistory(berechtigung, deleted);
		newBerechtigungsHistory.setTimestampErstellt(LocalDateTime.now());
		String userMutiert =
			berechtigung.getUserMutiert() != null ? berechtigung.getUserMutiert() : Constants.SYSTEM_USER_USERNAME;
		newBerechtigungsHistory.setUserErstellt(userMutiert);
		persistence.persist(newBerechtigungsHistory);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		ADMIN_MANDANT, REVISOR })
	public Collection<BerechtigungHistory> getBerechtigungHistoriesForBenutzer(@Nonnull Benutzer benutzer) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BerechtigungHistory> query = cb.createQuery(BerechtigungHistory.class);
		Root<BerechtigungHistory> root = query.from(BerechtigungHistory.class);

		ParameterExpression<String> benutzerParam = cb.parameter(String.class, "username");
		Predicate predicateBenutzer = cb.equal(root.get(BerechtigungHistory_.username), benutzerParam);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		query.where(predicateBenutzer);

		TypedQuery<BerechtigungHistory> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(benutzerParam, benutzer.getUsername());
		return q.getResultList();
	}
}

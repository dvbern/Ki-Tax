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

package ch.dvbern.ebegu.services.authentication;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.AuthAccessElement;
import ch.dvbern.ebegu.authentication.AuthLoginElement;
import ch.dvbern.ebegu.entities.AuthorisierterBenutzer;
import ch.dvbern.ebegu.entities.AuthorisierterBenutzer_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.AuthService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.manager.CacheContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Berechtigungen: PermitAll weggenommen weil wir das ohne user aus dem loginmodul aufrufen muessen, sonst wird anonymous genommen und man hat 2 principals nach dem loginmodul
@SuppressWarnings("OverlyBroadCatchBlock")
@Stateless(name = "AuthService")
public class AuthServiceBean implements AuthService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthServiceBean.class);

	@PersistenceContext(unitName = "ebeguPersistenceUnit")
	private EntityManager entityManager;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Resource(lookup = "java:jboss/infinispan/container/ebeguCache")
	private CacheContainer cacheContainer;

	@Nonnull
	@Override
	public Optional<AuthAccessElement> login(@Nonnull AuthLoginElement loginElement) {
		Objects.requireNonNull(loginElement);

		if (StringUtils.isEmpty(loginElement.getUsername()) || StringUtils.isEmpty(loginElement.getPlainTextPassword())) {
			return Optional.empty();
		}

		//Benutzer muss in jedem Fall bekannt sein (wird bei erfolgreichem container login angelegt)
		Optional<Benutzer> benutzer = benutzerService.findBenutzer(loginElement.getUsername());
		if (!benutzer.isPresent()) {
			return Optional.empty();
		}

		AuthorisierterBenutzer authorisierterBenutzer = new AuthorisierterBenutzer();
		authorisierterBenutzer.setBenutzer(benutzer.get());

		authorisierterBenutzer.setAuthToken(UUID.randomUUID().toString());  //auth token generieren
		authorisierterBenutzer.setUsername(loginElement.getUsername());
		authorisierterBenutzer.setRole(loginElement.getRole()); // hier kommt rolle aus property file
		entityManager.persist(authorisierterBenutzer);
		return Optional.of(new AuthAccessElement(
			authorisierterBenutzer.getUsername(),
			authorisierterBenutzer.getAuthToken(),
			UUID.randomUUID().toString(), // XSRF-Token (no need to persist)
			loginElement.getNachname(),
			loginElement.getVorname(),
			loginElement.getEmail(),
			loginElement.getRole()));
	}

	@Override
	public boolean logoutAndDelete(@Nonnull String authToken) {
		if (StringUtils.isEmpty(authToken)) {
			return false;
		}

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaDelete<AuthorisierterBenutzer> delete = criteriaBuilder.createCriteriaDelete(AuthorisierterBenutzer.class);
		Root<AuthorisierterBenutzer> root = delete.from(AuthorisierterBenutzer.class);
		Predicate authTokenPredicate = criteriaBuilder.equal(root.get(AuthorisierterBenutzer_.authToken), authToken);
		delete.where(criteriaBuilder.and(authTokenPredicate));
		cacheContainer.getCache().remove(authToken);
		try {
			entityManager.createQuery(delete).executeUpdate();
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int logoutAndDeleteAuthorisierteBenutzerForUser(@Nonnull String username) {
		Collection<AuthorisierterBenutzer> authUsers = criteriaQueryHelper.getEntitiesByAttribute(AuthorisierterBenutzer.class, username, AuthorisierterBenutzer_.username);
		for (AuthorisierterBenutzer authUser : authUsers) {
			// Den Benutzer ausloggen und den AuthentifiziertenBenutzer löschen
			logoutAndDelete(authUser.getAuthToken());
		}
		return authUsers.size();
	}

	@Override
	@Nonnull
	public AuthAccessElement createLoginFromIAM(AuthorisierterBenutzer authorisierterBenutzer) {
		try {
			Benutzer benutzerFromDB = benutzerService.findBenutzer(authorisierterBenutzer.getUsername())
				.orElseThrow(() -> {
					LOG.error("Could not find Benutzer during login from IAM. Benutzer should have been created"
						+ "(e.g. via REST call) prior to creating the AuthorisierterBenutzer entry.");
					return new EbeguEntityNotFoundException("createLoginFromIam", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, authorisierterBenutzer.getUsername());
				});
			authorisierterBenutzer.setBenutzer(benutzerFromDB);

			entityManager.persist(authorisierterBenutzer);
		} catch (RuntimeException ex) {
			LOG.error("Could not create Login from IAM for user {}", authorisierterBenutzer, ex);
			throw ex;
		}
		Benutzer existingUser = authorisierterBenutzer.getBenutzer();
		return new AuthAccessElement(
			authorisierterBenutzer.getUsername(),
			authorisierterBenutzer.getAuthToken(),
			UUID.randomUUID().toString(), // XSRF-Token (no need to persist)
			existingUser.getNachname(),
			existingUser.getVorname(),
			existingUser.getEmail(),
			existingUser.getRole());
	}

	@Override
	public Optional<AuthorisierterBenutzer> validateAndRefreshLoginToken(String token, boolean doRefreshToken) {
		if (token == null) {
			Throwable t = new Throwable();  //to get stack trace
			LOG.warn("Tried to refresh a login token without actually passing a token", t);
			return Optional.empty();
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		ParameterExpression<String> authTokenParam = cb.parameter(String.class, "authToken");

		CriteriaQuery<AuthorisierterBenutzer> query = cb.createQuery(AuthorisierterBenutzer.class);
		Root<AuthorisierterBenutzer> root = query.from(AuthorisierterBenutzer.class);
		Predicate authTokenPredicate = cb.equal(root.get(AuthorisierterBenutzer_.authToken), authTokenParam);
		query.where(authTokenPredicate);

		try {
			TypedQuery<AuthorisierterBenutzer> tq = entityManager.createQuery(query)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.setParameter(authTokenParam, token);

			AuthorisierterBenutzer authUser = tq.getSingleResult();

			// Das Login verlaengern, falls es sich nicht um einen Timer handelt
			if (doRefreshToken) {
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime maxDateFromNow = now.minus(Constants.LOGIN_TIMEOUT_SECONDS, ChronoUnit.SECONDS);
				if (authUser.getLastLogin().isBefore(maxDateFromNow)) {
					LOG.debug("Token is no longer valid: {}", token);
					return Optional.empty();
				}
				authUser.setLastLogin(now);
				entityManager.persist(authUser);
				entityManager.flush();
				LOG.trace("Valid auth Token '{}' was refreshed", token);
			}
			return Optional.of(authUser);

		} catch (NoResultException ignored) {
			LOG.debug("Could not load Authorisierterbenutzer for token '{}'", token);
			return Optional.empty();
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int deleteInvalidAuthTokens() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaDelete<AuthorisierterBenutzer> delete = criteriaBuilder.createCriteriaDelete(AuthorisierterBenutzer.class);
		Root<AuthorisierterBenutzer> root = delete.from(AuthorisierterBenutzer.class);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime maxDateFromNow = now.minus(Constants.LOGIN_TIMEOUT_SECONDS, ChronoUnit.SECONDS);
		Predicate predicateAbgelaufen = criteriaBuilder.lessThanOrEqualTo(root.get(AuthorisierterBenutzer_.lastLogin), maxDateFromNow);

		delete.where(criteriaBuilder.and(predicateAbgelaufen));
		try {
			int nbrDeleted = entityManager.createQuery(delete).executeUpdate();
			return nbrDeleted;
		} catch (Exception e) {
			LOG.error("Could not delete invalid AuthTokens", e);
			return 0;
		}
	}
}

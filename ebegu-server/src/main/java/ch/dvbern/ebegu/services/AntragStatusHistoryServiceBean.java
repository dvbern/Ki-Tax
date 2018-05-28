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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMINISTRATOR_SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_JA;
import static ch.dvbern.ebegu.enums.UserRoleName.SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer AntragStatusHistory
 */
@Stateless
@Local(AntragStatusHistoryService.class)
@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA, JURIST, REVISOR, ADMINISTRATOR_SCHULAMT, SCHULAMT,  STEUERAMT, GESUCHSTELLER})
public class AntragStatusHistoryServiceBean extends AbstractBaseService implements AntragStatusHistoryService {

	private static final Logger LOG = LoggerFactory.getLogger(AntragStatusHistoryServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public AntragStatusHistory saveStatusChange(@Nonnull Gesuch gesuch, @Nullable Benutzer saveAsUser) {
		Objects.requireNonNull(gesuch);

		Benutzer userToSet = saveAsUser;
		if (userToSet == null) {
			Optional<Benutzer> currentBenutzer = benutzerService.getCurrentBenutzer();
			if (currentBenutzer.isPresent()) {
				userToSet = currentBenutzer.get();
			}
		}
		if (userToSet != null) {
			// Den letzten Eintrag beenden, falls es schon einen gab
			AntragStatusHistory lastStatusChange = findLastStatusChange(gesuch);
			if (lastStatusChange != null) {
				lastStatusChange.setTimestampBis(LocalDateTime.now());
			}
			// Und den neuen speichern
			final AntragStatusHistory newStatusHistory = new AntragStatusHistory();
			newStatusHistory.setStatus(gesuch.getStatus());
			newStatusHistory.setGesuch(gesuch);
			newStatusHistory.setTimestampVon(LocalDateTime.now());
			newStatusHistory.setBenutzer(userToSet);

			return persistence.persist(newStatusHistory);
		}
		throw new EbeguEntityNotFoundException("saveStatusChange", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "No current Benutzer");
	}

	@Override
	@Nullable
	@PermitAll
	public AntragStatusHistory findLastStatusChange(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		try {
			final CriteriaQuery<AntragStatusHistory> query = createQueryAllAntragStatusHistoryProGesuch(gesuch);

			AntragStatusHistory result = persistence.getEntityManager().createQuery(query).setFirstResult(0).setMaxResults(1).getSingleResult();
			authorizer.checkReadAuthorization(result.getGesuch());
			return result;
		} catch (NoResultException e) {
			LOG.debug("No last status change found for gesuch {}", gesuch.getId(), e);
			return null;
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN })
	public void removeAllAntragStatusHistoryFromGesuch(@Nonnull Gesuch gesuch) {
		Collection<AntragStatusHistory> antragStatusHistoryFromGesuch = findAllAntragStatusHistoryByGesuch(gesuch);
		for (AntragStatusHistory antragStatusHistory : antragStatusHistoryFromGesuch) {
			persistence.remove(AntragStatusHistory.class, antragStatusHistory.getId());
		}
	}

	@Override
	@Nonnull
	public Collection<AntragStatusHistory> findAllAntragStatusHistoryByGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		Objects.requireNonNull(gesuch);
		return criteriaQueryHelper.getEntitiesByAttribute(AntragStatusHistory.class, gesuch, AntragStatusHistory_.gesuch);
	}

	@Override
	@Nonnull
	public Collection<AntragStatusHistory> findAllAntragStatusHistoryByGPFall(@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull Fall fall) {
		Objects.requireNonNull(gesuchsperiode);
		Objects.requireNonNull(fall);
		authorizer.checkReadAuthorizationFall(fall);

		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AntragStatusHistory> query = cb.createQuery(AntragStatusHistory.class);
		Set<AntragStatus> antragStatuses = AntragStatus.allowedforRole(role);

		Root<AntragStatusHistory> root = query.from(AntragStatusHistory.class);
		Predicate fallPredicate = cb.equal(root.get(AntragStatusHistory_.gesuch).get(Gesuch_.fall), fall);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(AntragStatusHistory_.gesuch).get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate rolePredicate = root.get(AntragStatusHistory_.gesuch).get(Gesuch_.status).in(antragStatuses);
		query.where(fallPredicate, gesuchsperiodePredicate, rolePredicate);
		query.orderBy(cb.desc(root.get(AntragStatusHistory_.timestampErstellt)));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public AntragStatusHistory findLastStatusChangeBeforeBeschwerde(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		authorizer.checkReadAuthorization(gesuch);
		final CriteriaQuery<AntragStatusHistory> query = createQueryAllAntragStatusHistoryProGesuch(gesuch);

		final List<AntragStatusHistory> lastTwoChanges = persistence.getEntityManager().createQuery(query).setMaxResults(2).getResultList();
		if (lastTwoChanges.size() < 2 || AntragStatus.BESCHWERDE_HAENGIG != lastTwoChanges.get(0).getStatus()) {
			throw new EbeguRuntimeException("findLastStatusChangeBeforeBeschwerde", ErrorCodeEnum.ERROR_NOT_FROM_STATUS_BESCHWERDE, gesuch.getId());
		}
		return lastTwoChanges.get(1); // returns the previous status before Beschwerde_Haengig
	}

	@Nonnull
	@Override
	public AntragStatusHistory findLastStatusChangeBeforePruefungSTV(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		authorizer.checkReadAuthorization(gesuch);
		if (gesuch.getStatus() != AntragStatus.GEPRUEFT_STV) {
			throw new EbeguRuntimeException("findLastStatusChangeBeforePruefungSTV", ErrorCodeEnum.ERROR_ONLY_IN_GEPRUEFT_STV_ALLOWED, gesuch.getId());
		}

		final CriteriaQuery<AntragStatusHistory> query = createQueryAllAntragStatusHistoryProGesuch(gesuch);

		final List<AntragStatusHistory> allStatusChanges = persistence.getEntityManager().createQuery(query).getResultList();
		boolean changeToPruefungSTVFound = false;
		for (final AntragStatusHistory statusChange : allStatusChanges) { //they come DESC ordered from the DB
			if (changeToPruefungSTVFound) {
				return statusChange; // return the previous one
			}
			if (statusChange.getStatus() == AntragStatus.PRUEFUNG_STV) {
				changeToPruefungSTVFound = true;
			}
		}
		throw new EbeguRuntimeException("findLastStatusChangeBeforePruefungSTV", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuch.getId());
	}

	/**
	 * Gibt alle AntragStatusHistory des gegebenen Gesuchs zurueck. Sortiert nach timestampVon DESC
	 */
	@Nonnull
	private CriteriaQuery<AntragStatusHistory> createQueryAllAntragStatusHistoryProGesuch(Gesuch gesuch) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AntragStatusHistory> query = cb.createQuery(AntragStatusHistory.class);
		Root<AntragStatusHistory> root = query.from(AntragStatusHistory.class);

		Predicate predicateInstitution = cb.equal(root.get(AntragStatusHistory_.gesuch).get(Gesuch_.id), gesuch.getId());

		query.where(predicateInstitution);
		query.orderBy(cb.desc(root.get(AntragStatusHistory_.timestampVon)));
		return query;
	}

}

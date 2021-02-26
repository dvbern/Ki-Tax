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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer_;
import ch.dvbern.ebegu.entities.Gesuchsteller_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Fall
 */
@Stateless
@Local(FallService.class)
public class FallServiceBean extends AbstractBaseService implements FallService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Authorizer authorizer;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private DossierService dossierService;

	@Nonnull
	@Override
	public Fall saveFall(@Nonnull Fall fall) {
		Objects.requireNonNull(fall);
		// Den "Besitzer" auf dem Fall ablegen
		if (principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
			Optional<Benutzer> currentBenutzer = benutzerService.getCurrentBenutzer();
			currentBenutzer.ifPresent(fall::setBesitzer);
		}
		authorizer.checkWriteAuthorization(fall);
		return persistence.merge(fall);
	}

	@Nonnull
	@Override
	public Optional<Fall> findFall(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Fall a = persistence.find(Fall.class, key);
		if (a != null) {
			authorizer.checkReadAuthorizationFall(a);
		}
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Optional<Fall> findFallByNumber(@Nonnull Long fallnummer) {
		Objects.requireNonNull(fallnummer, "fallnummer muss gesetzt sein");
		Optional<Fall> fallOptional = criteriaQueryHelper.getEntityByUniqueAttribute(Fall.class, fallnummer, Fall_.fallNummer);
		fallOptional.ifPresent(fall -> authorizer.checkReadAuthorizationFall(fall));
		return fallOptional;
	}

	@Override
	@Nonnull
	public Optional<Fall> findFallByCurrentBenutzerAsBesitzer() {
		Optional<Benutzer> currentBenutzerOptional = benutzerService.getCurrentBenutzer();
		return currentBenutzerOptional.flatMap(this::findFallByBesitzer);
	}

	@Override
	@Nonnull
	public Optional<Fall> findFallByBesitzer(@Nullable Benutzer benutzer) {
		Optional<Fall> fallOptional = criteriaQueryHelper.getEntityByUniqueAttribute(Fall.class, benutzer, Fall_.besitzer);
		fallOptional.ifPresent(fall -> authorizer.checkReadAuthorizationFall(fall));
		return fallOptional;
	}

	@Nonnull
	@Override
	public Collection<Fall> getAllFalle(boolean doAuthCheck) {
		List<Fall> faelle = new ArrayList<>(criteriaQueryHelper.getAll(Fall.class));
		if (doAuthCheck) {
			authorizer.checkReadAuthorizationFaelle(faelle);
		}
		return faelle;
	}

	@Override
	public void removeFallIfExists(@Nonnull String fallId, @Nonnull GesuchDeletionCause deletionCause) {
		Objects.requireNonNull(fallId);
		Optional<Fall> fallToRemove = findFall(fallId);
		if (fallToRemove.isPresent()) {
			Fall loadedFall = fallToRemove.get();
			removeFall(loadedFall, deletionCause);
		}
	}

	@Override
	public void removeFall(@Nonnull Fall fall, @Nonnull GesuchDeletionCause deletionCause) {
		Objects.requireNonNull(fall);
		Optional<Fall> fallToRemove = findFall(fall.getId());
		Fall loadedFall = fallToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeFall", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, fall));
		authorizer.checkWriteAuthorization(loadedFall);
		// Remove all depending objects
		mitteilungService.removeAllMitteilungenForFall(loadedFall);
		// Alle Dossier des Falls loeschen (die entsprechenden Gesuchen werden damit auch geloescht
		Collection<Dossier> dossiersByFall = dossierService.findDossiersByFall(fall.getId());
		for (Dossier dossier : dossiersByFall) {
			dossierService.removeDossier(dossier.getId(), deletionCause);
		}
		//Finally remove the Fall when all other objects are really removed
		persistence.remove(loadedFall);
	}

	@Nonnull
	@Override
	public Optional<Fall> createFallForCurrentGesuchstellerAsBesitzer() {
		UserRole role = principalBean.discoverMostPrivilegedRole();
		if (UserRole.GESUCHSTELLER == role) {
			final Optional<Fall> existingFall = findFallByCurrentBenutzerAsBesitzer();
			if (!existingFall.isPresent()) {
				return Optional.of(saveFall(new Fall()));
			}
		}
		return Optional.empty();
	}

	@Nonnull
	@Override
	public Optional<String> getCurrentEmailAddress(@Nonnull String fallID) {
		Objects.requireNonNull(fallID);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();

		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);
		ParameterExpression<String> fallIdParam = cb.parameter(String.class, "fallId");
		Join<Gesuch, Dossier> dossierJoin = root.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Fall> fallJoin = dossierJoin.join(Dossier_.fall);
		Join<Gesuch, GesuchstellerContainer> gesuchstellerJoin = root.join(Gesuch_.gesuchsteller1, JoinType.LEFT);
		Join<GesuchstellerContainer, Gesuchsteller> gesDataJoin = gesuchstellerJoin.join(GesuchstellerContainer_.gesuchstellerJA, JoinType.LEFT);
		Predicate gesuchOfFall = cb.equal(fallJoin.get(Fall_.id), fallIdParam);
		Path<String> gsEmail = gesDataJoin.get(Gesuchsteller_.mail);
		query.select(gsEmail);
		query.where(gesuchOfFall);
		query.orderBy(cb.desc(gesDataJoin.get(Gesuchsteller_.timestampMutiert))); // Das zuletzt geänderte GS-Objekt
		TypedQuery<String> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(fallIdParam, fallID);
		typedQuery.setMaxResults(1);

		List<String> criteriaResults = typedQuery.getResultList();

		String emailToReturn = null;
		if (!criteriaResults.isEmpty()) {
			if (criteriaResults.size() != 1) {
				throw new EbeguRuntimeException("getEmailAddressForFall", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, criteriaResults.size());
			}
			emailToReturn = criteriaResults.get(0);
		}
		if (emailToReturn == null) {
			emailToReturn = readBesitzerEmailForFall(fallID);

		}
		return Optional.ofNullable(emailToReturn);

	}

	private String readBesitzerEmailForFall(String fallID) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Fall> root = query.from(Fall.class);
		Join<Fall, Benutzer> benutzerJoin = root.join(Fall_.besitzer, JoinType.LEFT);
		ParameterExpression<String> fallIdParam = cb.parameter(String.class, "fallId");
		Predicate gesuchOfFall = cb.equal(root.get(Fall_.id), fallIdParam);
		Path<String> benutzerEmail = benutzerJoin.get(Benutzer_.email);
		query.select(benutzerEmail);
		query.where(gesuchOfFall);
		TypedQuery<String> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(fallIdParam, fallID);

		return typedQuery.getSingleResult();
	}

	@Nonnull
	@Override
	public Fall uploadSozialdienstVollmachtDokument(
		@Nonnull String fallId,
		@Nonnull byte[] content) {
		requireNonNull(fallId);
		requireNonNull(content);
		final Fall fall = findFall(fallId).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"uploadSozialdienstVollmachtDokument - findFall",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				fallId)
		);
		if(fall.getSozialdienstFall() == null) {
			throw new EbeguEntityNotFoundException(
				"uploadSozialdienstVollmachtDokument - getSozialdienstFall",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				fallId);
		}
		fall.getSozialdienstFall().setVollmacht(content);
		return saveFall(fall);
	}

}

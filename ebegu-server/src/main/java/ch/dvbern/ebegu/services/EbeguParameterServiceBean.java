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
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.EbeguParameter_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SCHULAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer E-BEGU-Parameter
 */
@Stateless
@Local(EbeguParameterService.class)
@RolesAllowed({ ADMIN_BG, SUPER_ADMIN })
public class EbeguParameterServiceBean extends AbstractBaseService implements EbeguParameterService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN })
	public EbeguParameter saveEbeguParameter(@Nonnull EbeguParameter ebeguParameter) {
		Objects.requireNonNull(ebeguParameter);
		return persistence.merge(ebeguParameter);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Optional<EbeguParameter> findEbeguParameter(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		EbeguParameter a = persistence.find(EbeguParameter.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN })
	public void removeEbeguParameter(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Optional<EbeguParameter> parameterToRemove = findEbeguParameter(id);
		EbeguParameter param = parameterToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeEbeguParameter", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));
		persistence.remove(param);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Collection<EbeguParameter> getAllEbeguParameter() {
		return new ArrayList<>(criteriaQueryHelper.getAll(EbeguParameter.class));
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Collection<EbeguParameter> getAllEbeguParameterByDate(@Nonnull LocalDate date) {
		return new ArrayList<>(criteriaQueryHelper.getAllInInterval(EbeguParameter.class, date));
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Collection<EbeguParameter> getEbeguParameterByGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		Collection<EbeguParameter> ebeguParameters = getAllEbeguParameterByDate(gesuchsperiode.getGueltigkeit().getGueltigAb());
		List<EbeguParameter> collect = ebeguParameters.stream()
			.filter(ebeguParameter -> ebeguParameter.getName().isProGesuchsperiode())
			.collect(Collectors.toCollection(ArrayList::new));
		collect.sort(Comparator.comparing(EbeguParameter::getName));
		return collect;
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Collection<EbeguParameter> getEbeguParametersByJahr(@Nonnull Integer jahr) {
		Collection<EbeguParameter> ebeguParameters = getAllEbeguParameterByDate(LocalDate.of(jahr, Month.JANUARY, 1));
		List<EbeguParameter> collect = ebeguParameters.stream()
			.filter(ebeguParameter -> !ebeguParameter.getName().isProGesuchsperiode())
			.collect(Collectors.toCollection(ArrayList::new));
		collect.sort(Comparator.comparing(EbeguParameter::getName));
		return collect;
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, ADMIN_TS })
	public Collection<EbeguParameter> getJahresabhParameter() {
		List<EbeguParameterKey> jahresabhParamsKey = Arrays.stream(EbeguParameterKey.values()).filter(ebeguParameterKey -> !ebeguParameterKey.isProGesuchsperiode()).collect(Collectors.toList());
		return getEbeguParameterByKey(jahresabhParamsKey);

	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Optional<EbeguParameter> getEbeguParameterByKeyAndDate(@Nonnull EbeguParameterKey key, @Nonnull LocalDate date) {
		return getEbeguParameterByKeyAndDate(key, date, persistence.getEntityManager());
	}

	/**
	 * Methode zum laden von EEGU Parametern
	 *
	 * @param key Key des property das geladen werden soll
	 * @param date stichtag zu dem der Wert des property gelesen werden soll
	 * @param em wir geben hier einen entity manager mit weil wir diese Methode aus dem validator aufrufen
	 * im Validator darf man nicht einfach direkt den entity manager injecten weil dieser nicht in
	 * der gleiche sein darf wie in den services (sonst gibt es eine concurrentModificationException in hibernate)
	 * http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	 * @return EbeguParameter
	 */
	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Optional<EbeguParameter> getEbeguParameterByKeyAndDate(@Nonnull EbeguParameterKey key, @Nonnull LocalDate date, final EntityManager em) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<EbeguParameter> query = cb.createQuery(EbeguParameter.class);
		Root<EbeguParameter> root = query.from(EbeguParameter.class);
		query.select(root);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = cb.between(dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		ParameterExpression<EbeguParameterKey> keyParam = cb.parameter(EbeguParameterKey.class, "key");
		Predicate keyPredicate = cb.equal(root.get(EbeguParameter_.name), keyParam);

		query.where(intervalPredicate, keyPredicate);
		TypedQuery<EbeguParameter> q = em.createQuery(query);
		q.setParameter(dateParam, date);
		q.setParameter(keyParam, key);
		List<EbeguParameter> resultList = q.getResultList();
		EbeguParameter paramOrNull = null;
		if (!resultList.isEmpty() && resultList.size() == 1) {
			paramOrNull = resultList.get(0);
		} else if (resultList.size() > 1) {
			throw new NonUniqueResultException();
		}
		return Optional.ofNullable(paramOrNull);
	}

	@Nonnull
	private Collection<EbeguParameter> getEbeguParameterByKey(@Nonnull Collection<EbeguParameterKey> keys) {
		if (!keys.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<EbeguParameter> query = cb.createQuery(EbeguParameter.class);
			Root<EbeguParameter> root = query.from(EbeguParameter.class);
			query.select(root);

			Predicate keyPredicate = root.get(EbeguParameter_.name).in(keys);

			query.where(keyPredicate);
			query.orderBy(cb.asc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
			return persistence.getCriteriaResults(query);
		}
		return Collections.emptyList();

	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TS, SCHULAMT, STEUERAMT })
	public void copyEbeguParameterListToNewGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		final Optional<Gesuchsperiode> newestGesuchsperiodeOpt = gesuchsperiodeService.findNewestGesuchsperiode();
		final Gesuchsperiode newestGesuchsperiode = newestGesuchsperiodeOpt.orElseThrow(() -> new EbeguEntityNotFoundException
			("copyEbeguParameterListToNewGesuchsperiode", ErrorCodeEnum.ERROR_GESUCHSPERIODE_MUST_EXIST));

		Collection<EbeguParameter> paramsOfGesuchsperiode = getAllEbeguParameterByDate(newestGesuchsperiode.getGueltigkeit().getGueltigAb());
		paramsOfGesuchsperiode.stream().filter(lastYearParameter -> lastYearParameter.getName().isProGesuchsperiode()).forEach(lastYearParameter -> {
			final Optional<EbeguParameter> existingParameter = findEbeguParameter(lastYearParameter.getName(), gesuchsperiode.getGueltigkeit());
			if (!existingParameter.isPresent()) {
				EbeguParameter newParameter = lastYearParameter.copy(gesuchsperiode.getGueltigkeit());
				saveEbeguParameter(newParameter);
			}
		});
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TS, SCHULAMT, STEUERAMT })
	public void createEbeguParameterListForJahr(@Nonnull Integer jahr) {
		final Optional<Gesuchsperiode> newestGesuchsperiodeOpt = gesuchsperiodeService.findNewestGesuchsperiode();
		final Gesuchsperiode newestGesuchsperiode = newestGesuchsperiodeOpt.orElseThrow(() -> new EbeguEntityNotFoundException
			("createEbeguParameterListForJahr", ErrorCodeEnum.ERROR_GESUCHSPERIODE_MUST_EXIST));

		Collection<EbeguParameter> paramsOfYear = getAllEbeguParameterByDate(newestGesuchsperiode.getGueltigkeit().getGueltigBis());
		paramsOfYear.stream()
			.filter(lastYearParameter -> !lastYearParameter.getName().isProGesuchsperiode())
			.forEach(lastYearParameter -> {
				final DateRange newGueltigkeit = new DateRange(jahr);
				final Optional<EbeguParameter> existingParameter = findEbeguParameter(lastYearParameter.getName(), newGueltigkeit);
				if (!existingParameter.isPresent()) {
					EbeguParameter newParameter = lastYearParameter.copy(newGueltigkeit);
					saveEbeguParameter(newParameter);
				}
			});
	}

	/**
	 * Sucht das Parameter mit Name und Gueltigkeit. Vorausgesehen Name und Gueltigkeit sind unique.
	 * Wenn mehrere Rows gefunden EbeguRuntimeException wird geworfen
	 */
	private Optional<EbeguParameter> findEbeguParameter(@Nonnull EbeguParameterKey name, @Nonnull DateRange gueltigkeit) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<EbeguParameter> query = cb.createQuery(EbeguParameter.class);
		Root<EbeguParameter> root = query.from(EbeguParameter.class);
		query.select(root);

		// This could only throw one row <- Unique Key
		Predicate namePredicate = cb.equal(root.get(EbeguParameter_.name), name);
		Predicate gueltigkeitPredicate = cb.equal(root.get(EbeguParameter_.gueltigkeit), gueltigkeit);

		query.where(namePredicate, gueltigkeitPredicate);

		final List<EbeguParameter> criteriaResults = persistence.getCriteriaResults(query);
		if (criteriaResults.isEmpty()){
			return Optional.empty();
		}
		if (criteriaResults.size() > 1) {
			throw new EbeguRuntimeException("findEbeguParameter", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, name);
		}
		return Optional.of(criteriaResults.get(0));
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, JURIST, REVISOR, GESUCHSTELLER, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION, ADMIN_TS, SCHULAMT, STEUERAMT })
	public Map<EbeguParameterKey, EbeguParameter> getEbeguParameterByGesuchsperiodeAsMap(@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EbeguParameterKey, EbeguParameter> result = new HashMap<>();
		Collection<EbeguParameter> paramsForPeriode = getEbeguParameterByGesuchsperiode(gesuchsperiode);
		for (EbeguParameter ebeguParameter : paramsForPeriode) {
			result.put(ebeguParameter.getName(), ebeguParameter);
		}
		return result;
	}
}

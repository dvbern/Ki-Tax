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

package ch.dvbern.ebegu.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hilfsklasse welche CriteriaQueries erstellt.
 */
@SuppressWarnings({ "unchecked" })
@Dependent
public class CriteriaQueryHelper {

	@Inject
	private Persistence persistence;

	@SuppressWarnings("unchecked")
	public <T> Collection<T> getAll(final Class<T> clazz) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<T> query = cb.createQuery(clazz);
		query.from(clazz);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	public <T extends P, P extends AbstractEntity> Collection<T> getAllOrdered(@Nonnull final Class<T> clazz, @Nonnull SingularAttribute<P, String> orderBy) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<T> query = cb.createQuery(clazz);
		Root root = query.from(clazz);
		query.select(root);
		query.orderBy(cb.asc(root.get(orderBy)));
		return persistence.getCriteriaResults(query);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public <A, E extends AbstractEntity> Optional<E> getEntityByUniqueAttribute(@Nonnull final Class<E> entityClazz,
		@Nullable final A attributeValue,
		@Nonnull final SingularAttribute<E, A> attribute) {
		final Collection<E> results = getEntitiesByAttribute(entityClazz, attributeValue, attribute);
		E result = ensureSingleResult(results, attributeValue);
		return Optional.ofNullable(result);
	}

	@Nullable
	private <A, E> E ensureSingleResult(@Nonnull final Collection<E> results, @Nullable final A attributeValue) {
		if (results.size() > 1) {
			throw new NonUniqueResultException("Attribute '" + attributeValue +
				"' should be unique, therefore there may not be multiple occurences");
		}
		E retVal = null;
		if (!results.isEmpty()) {
			retVal = results.iterator().next();
		}
		return retVal;
	}

	@Nonnull
	public <A, E> Collection<E> getEntitiesByAttribute(@Nonnull final Class<E> entityClass, @Nullable final A attributeValue, @Nonnull final Attribute<E, A> attribute) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<E> query = builder.createQuery(entityClass);
		final Root<E> root = query.from(entityClass);
		final Expression<E> expression;
		if (attribute instanceof SingularAttribute) {
			expression = root.get((SingularAttribute) attribute);
		} else if (attribute instanceof PluralAttribute) {
			expression = root.get((PluralAttribute) attribute);
		} else {
			throw new IllegalArgumentException("attribute must be a PluralAttribute or a SingularAttribute");
		}
		query.where(builder.equal(expression, attributeValue));
		return persistence.getCriteriaResults(query);
	}

	@Nullable
	public static Predicate concatenateExpressions(@Nonnull final CriteriaBuilder builder, @Nonnull final List<Predicate> predicatesToUse) {
		Predicate concatenated = null;
		for (final Predicate expression : predicatesToUse) {
			if (concatenated == null) {
				// Dies ist die erste
				concatenated = expression;
			} else {
				// anhaengen
				concatenated = builder.and(concatenated, expression);
			}
		}
		return concatenated;
	}

	@Nullable
	public static Predicate concatenateExpressions(@Nonnull final CriteriaBuilder builder, @Nonnull Predicate... predicatesToUse) {
		return concatenateExpressions(builder, Arrays.asList(predicatesToUse));
	}

	/**
	 * Gibt alle Datensaetze vom Typ clazz zurück, bei welchen das gegebene Datum zwischen den Werten
	 * von datumVon und DatumBis liegt.
	 *
	 * @param clazz Entity class
	 * @param date Datum fuer die Suche
	 * @param <T> Entity Class
	 * @return Liste mit Datensaetzen
	 */
	public <T extends AbstractDateRangedEntity> Collection<T> getAllInInterval(Class<T> clazz, LocalDate date) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<T> query = cb.createQuery(clazz);
		Root<T> root = query.from(clazz);
		query.select(root);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate intervalPredicate = cb.between(dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));

		query.where(intervalPredicate);
		TypedQuery<T> q = persistence.getEntityManager().createQuery(query).setParameter(dateParam, date);
		List<T> resultList = q.getResultList();
		return resultList;
	}

	public <T extends AbstractEntity> int deleteAllBefore(@Nonnull Class<T> entityClazz, @Nonnull LocalDateTime before) {
		checkNotNull(entityClazz);
		checkNotNull(before);

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaDelete<T> delete = cb.createCriteriaDelete(entityClazz);
		Root<T> root = delete.from(entityClazz);

		ParameterExpression<LocalDateTime> beforeParam = cb.parameter(LocalDateTime.class, "before");
		delete.where(cb.lessThan(root.get(AbstractDateRangedEntity_.timestampMutiert), beforeParam));

		Query query = persistence.getEntityManager().createQuery(delete);
		query.setParameter(beforeParam, before);
		return query.executeUpdate();
	}
}


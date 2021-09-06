/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel_;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;

public final class PredicateHelper<V> {

	public static final Predicate[] NEW = new Predicate[0];

	private PredicateHelper() {
	}

	public static <T extends AbstractDateRangedEntity> Predicate getPredicateDateRangedEntityGueltig(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<T> root
	) {
		Predicate predActive = cb.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			LocalDate.now());
		return predActive;
	}

	public static <T extends AbstractDateRangedEntity> Predicate getPredicateDateRangedEntityGueltigAm(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<T> root,
		ParameterExpression<LocalDate> stichtag
	) {
		Predicate intervalPredicate = cb.between(stichtag,
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		return intervalPredicate;
	}

	public static <T extends AbstractDateRangedEntity> Collection<Predicate> getPredicateDateRangedEntityIncludedInRange(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<T> root,
		ParameterExpression<LocalDate> startParam,
		ParameterExpression<LocalDate> endParam
	) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), startParam));
		predicates.add(cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), endParam));
		return predicates;
	}

	/**
	 * @param root Root darf Institution oder InstitutionStammdaten sein
	 */
	public static Predicate excludeUnknownInstitutionStammdatenPredicate(Root<? extends AbstractEntity> root) {
		return root.get(AbstractEntity_.id)
			.in(Arrays.asList(
				Constants.ID_UNKNOWN_INSTITUTION_STAMMDATEN_KITA,
				Constants.ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESFAMILIE,
				Constants.ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESSCHULE))
			.not();
	}

	public static Predicate getPredicateBerechtigteInstitutionStammdaten(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<InstitutionStammdaten> root,
		@Nonnull ParameterExpression<Collection> gemeindeParam
	) {
		// Falls es sich um ein Tagesschule- oder Ferieninselangebot handelt, muss ich für die Gemeinde berechtigt sein
		Join<InstitutionStammdaten, InstitutionStammdatenTagesschule> joinTagesschule = root.join(InstitutionStammdaten_.institutionStammdatenTagesschule, JoinType.LEFT);
		Join<InstitutionStammdaten, InstitutionStammdatenFerieninsel> joinFerieninsel = root.join(InstitutionStammdaten_.institutionStammdatenFerieninsel, JoinType.LEFT);

		Predicate predicateBetreuungsgutschein =
			root.get(InstitutionStammdaten_.betreuungsangebotTyp).in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		Predicate predicateTypTagesschule = cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE);
		Predicate predicateTypFerieninsel = cb.equal(root.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.FERIENINSEL);

		Predicate predicateGemeindeTagesschule = joinTagesschule.get(InstitutionStammdatenTagesschule_.gemeinde).in(gemeindeParam);
		Predicate predicateGemeindeFerieninsel = joinFerieninsel.get(InstitutionStammdatenFerieninsel_.gemeinde).in(gemeindeParam);

		// TS und FI sind okay, wenn es der jeweilige Typ ist UND ich für die Gemeinde berechtigt bin
		Predicate predicateTagesschule = cb.and(predicateTypTagesschule, predicateGemeindeTagesschule);
		Predicate predicateFerieninsel = cb.and(predicateTypFerieninsel, predicateGemeindeFerieninsel);

		// Die Institution insgesamt ist okay, wenn es ein BG ist ODER eine berechtigte TS oder FI
		Predicate predicateBerechtigteInstitution = cb.or(predicateBetreuungsgutschein, predicateTagesschule, predicateFerieninsel);
		return predicateBerechtigteInstitution;

	}

	public static Predicate getPredicateMandant(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Path<Mandant> path,
		@Nonnull Benutzer eingeloggterBenutzer
	) {
		final Predicate predicateMandant = cb.equal(path, eingeloggterBenutzer.getMandant());
		return predicateMandant;
	}

	public static <T extends AbstractEntity> Predicate getPredicateFilterGesuchsperiode(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Join<T, Gesuchsperiode> joinGesuchsperiode,
		@Nonnull String gesuchsperiodeString
	) {
		String[] years = ensureYearFormat(gesuchsperiodeString);
		Path<DateRange> dateRangePath = joinGesuchsperiode.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateGesuchsperiode =
			cb.and(
				cb.equal(cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigAb)),
					years[0]),
				cb.equal(
					cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigBis)),
					years[1]));

		return predicateGesuchsperiode;
	}

	private static String[] ensureYearFormat(String gesuchsperiodeString) {
		String[] years = gesuchsperiodeString.split("/");
		if (years.length != 2) {
			throw new EbeguRuntimeException(
				"ensureYearFormat",
				"Der Gesuchsperioden string war nicht im erwarteten Format x/y sondern " + gesuchsperiodeString);
		}
		String[] result = new String[2];
		result[0] = changeTwoDigitYearToFourDigit(years[0]);
		result[1] = changeTwoDigitYearToFourDigit(years[1]);
		return result;
	}

	private static String changeTwoDigitYearToFourDigit(String year) {
		//im folgenden wandeln wir z.B 16  in 2016 um. Funktioniert bis ins jahr 2099, da die Periode 2099/2100 mit
		// dieser Methode nicht geht
		String currentYearAsString = String.valueOf(LocalDate.now().getYear());
		if (year.length() == currentYearAsString.length()) {
			return year;
		}
		if (year.length() < currentYearAsString.length()) { // jahr ist im kurzformat
			return currentYearAsString.substring(0, currentYearAsString.length() - year.length()) + year;
		}
		throw new EbeguRuntimeException(
			"changeTwoDigitYearToFourDigit",
			"Der Gesuchsperioden string war nicht im erwarteten Format yy oder yyyy sondern " + year);
	}
}

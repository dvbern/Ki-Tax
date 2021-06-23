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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.lib.date.feiertage.FeiertageHelper;

/**
 * Utils fuer Date Elemente
 */
public final class DateUtil {

	private DateUtil() {
	}

	/**
	 * Parset den gegebenen String als LocalDate mit dem Format "yyyy-MM-dd"
	 * Sollte der gegebene String null oder leer sein, wird now() zurueckgegeben
	 */
	@Nonnull
	public static LocalDate parseStringToDate(@Nonnull String stringDate) {
		return LocalDate.parse(stringDate, Constants.SQL_DATE_FORMAT);
	}

	/**
	 * Parset den gegebenen String als LocalDate mit dem Format "yyyy-MM-dd"
	 */
	@Nonnull
	public static LocalDate parseStringToDateNullSafe(@Nonnull String stringDate) {
		return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	/**
	 * Parset den gegebenen String als LocalDate mit dem Format "yyyy-MM-dd"
	 * Sollte der gegebene String null oder leer sein, wird now() zurueckgegeben
	 */
	@Nonnull
	public static LocalDate parseStringToDateOrReturnNow(@Nullable String stringDate) {
		LocalDate date = LocalDate.now();
		if (stringDate != null && !stringDate.isEmpty()) {
			date = LocalDate.parse(stringDate, Constants.SQL_DATE_FORMAT);
		}
		return date;
	}

	public static boolean isWeekend(@Nonnull LocalDate date) {
		return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
	}

	public static boolean isHoliday(@Nonnull LocalDate date) {
		return FeiertageHelper.isFeiertag_CH(Date.valueOf(date));
	}

	public static LocalDate getMax(@Nonnull LocalDate date1, @Nonnull LocalDate date2) {
		return date1.isAfter(date2) ? date1 : date2;
	}

	public static LocalDate getMin(@Nonnull LocalDate date1, @Nonnull LocalDate date2) {
		return date1.isBefore(date2) ? date1 : date2;
	}

	/**
	 * Berechnet den Anteil des Zeitabschnittes am gesamten Monat als dezimalzahl von 0 bis 1
	 * Dabei werden nur Werktage (d.h. sa do werden ignoriert) beruecksichtigt
	 */
	public static BigDecimal calculateAnteilMonatInklWeekend(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
		LocalDate monatsanfang = von.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate monatsende = bis.with(TemporalAdjusters.lastDayOfMonth());
		long nettoTageMonat = daysBetween(monatsanfang, monatsende);
		long nettoTageIntervall = daysBetween(von, bis);
		return MathUtil.EXACT.divide(MathUtil.EXACT.from(nettoTageIntervall), MathUtil.EXACT.from(nettoTageMonat));
	}

	/**
	 * Berechnet die Anzahl Tage zwischen zwei Daten
	 */
	public static long daysBetween(@Nonnull LocalDate start, @Nonnull LocalDate end) {
		return Stream.iterate(start, d -> d.plusDays(1))
			.limit(start.until(end.plusDays(1), ChronoUnit.DAYS))
			.count();
	}

	/**
	 * Prueft, ob zwei Datum im selben Monat (desselben Jahres) liegen
	 */
	public static boolean isSameMonthAndYear(@Nonnull LocalDate dateOne, @Nonnull LocalDate dateTwo) {
		return dateOne.getYear() == dateTwo.getYear()
			&& dateOne.getMonth() == dateTwo.getMonth();
	}

	public static String incrementYear(@Nonnull String dateStr) {
		LocalDate date = Date.valueOf(dateStr).toLocalDate();
		return date.plus(1, ChronoUnit.YEARS).toString();
	}

	public static DateRange limitToDateRange(DateRange range, DateRange gesuchsperiode) {
		// Wir nehmen das spätere VON und das frühere BIS
		LocalDate von = DateUtil.getMax(range.getGueltigAb(), gesuchsperiode.getGueltigAb());
		LocalDate bis = DateUtil.getMin(range.getGueltigBis(), gesuchsperiode.getGueltigBis());
		return new DateRange(von, bis);
	}
}

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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.stream.Stream;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für BG-Rechner
 */
public abstract class AbstractBGRechner {

	protected static final BigDecimal FAKTOR_KIND = MathUtil.EXACT.from(1);
	protected static final BigDecimal ZWOELF = MathUtil.EXACT.from(12L);
	protected static final BigDecimal NEUN = MathUtil.EXACT.from(9L);
	protected static final BigDecimal ZWANZIG = MathUtil.EXACT.from(20L);
	protected static final BigDecimal ZWEIHUNDERTVIERZIG = MathUtil.EXACT.from(240L);

	/**
	 * Diese Methode muss von den Subklassen ueberschrieben werden und fuehrt die Berechnung fuer  die uebergebenen Verfuegungsabschnitte durch.
	 */
	public abstract VerfuegungZeitabschnitt calculate(VerfuegungZeitabschnitt verfuegungZeitabschnitt, Verfuegung verfuegung, BGRechnerParameterDTO parameterDTO);

	/**
	 * Checkt die für alle Angebote benoetigten Argumente auf Null.
	 * Stellt sicher, dass der Zeitraum innerhalb eines Monates liegt
	 * Wenn nicht wird eine Exception geworfen
	 */
	protected void checkArguments(LocalDate von, LocalDate bis, BigDecimal anspruch, BigDecimal massgebendesEinkommen) {
		// Inputdaten validieren
		Objects.requireNonNull(von, "von darf nicht null sein");
		Objects.requireNonNull(bis, "bis darf nicht null sein");
		Objects.requireNonNull(anspruch, "anspruch darf nicht null sein");
		Objects.requireNonNull(massgebendesEinkommen, "massgebendesEinkommen darf nicht null sein");
		// Max. 1 Monat
		if (!von.getMonth().equals(bis.getMonth())) {
			throw new IllegalArgumentException("BG Rechner duerfen nicht für monatsuebergreifende Zeitabschnitte verwendet werden!");
		}
	}

	/**
	 * Berechnet den Anteil des Zeitabschnittes am gesamten Monat als dezimalzahl von 0 bis 1
	 * Dabei werden nur Werktage (d.h. sa do werden ignoriert) beruecksichtigt
	 */
	protected BigDecimal calculateAnteilMonat(LocalDate von, LocalDate bis) {
		LocalDate monatsanfang = von.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate monatsende = bis.with(TemporalAdjusters.lastDayOfMonth());
		long nettoarbeitstageMonat = workDaysBetween(monatsanfang, monatsende);
		long nettoarbeitstageIntervall = workDaysBetween(von, bis);
		return MathUtil.EXACT.divide(MathUtil.EXACT.from(nettoarbeitstageIntervall), MathUtil.EXACT.from(nettoarbeitstageMonat));
	}

	/**
	 * Berechnet die Kosten einer Betreuungsstunde (Tagi und Tagesfamilien)
	 */
	protected BigDecimal calculateKostenBetreuungsstunde(BigDecimal kostenProStundeMaximal, BigDecimal massgebendesEinkommen, BigDecimal anspruch, BGRechnerParameterDTO parameterDTO) {
		// Massgebendes Einkommen: Minimum und Maximum berücksichtigen
		BigDecimal massgebendesEinkommenBerechnet = (massgebendesEinkommen.max(parameterDTO.getMassgebendesEinkommenMinimal())).min(parameterDTO.getMassgebendesEinkommenMaximal());
		BigDecimal kostenProStundeMaxMinusMin = MathUtil.EXACT.subtract(kostenProStundeMaximal, parameterDTO.getKostenProStundeMinimal());
		BigDecimal massgebendesEinkommenMaxMinusMin = MathUtil.EXACT.subtract(parameterDTO.getMassgebendesEinkommenMaximal(), parameterDTO.getMassgebendesEinkommenMinimal());
		BigDecimal massgebendesEinkommenMinusMin = MathUtil.EXACT.subtract(massgebendesEinkommenBerechnet, parameterDTO.getMassgebendesEinkommenMinimal());
		BigDecimal zwischenresultat1 = MathUtil.EXACT.divide(kostenProStundeMaxMinusMin, massgebendesEinkommenMaxMinusMin);
		BigDecimal zwischenresultat2 = MathUtil.EXACT.multiply(zwischenresultat1, massgebendesEinkommenMinusMin);
		return MathUtil.EXACT.add(zwischenresultat2, parameterDTO.getKostenProStundeMinimal());
	}

	/**
	 * Berechnet die Anzahl Wochentage zwischen (und inklusive) Start und End
	 */
	private long workDaysBetween(LocalDate start, LocalDate end) {
		return Stream.iterate(start, d -> d.plusDays(1))
			.limit(start.until(end.plusDays(1), ChronoUnit.DAYS))
			.filter(d -> !(DayOfWeek.SATURDAY.equals(d.getDayOfWeek()) || DayOfWeek.SUNDAY.equals(d.getDayOfWeek())))
			.count();
	}
}

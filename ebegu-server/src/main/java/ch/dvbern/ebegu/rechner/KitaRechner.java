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
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot KITA.
 */
public class KitaRechner extends AbstractBGRechner {

	@Nonnull
	@Override
	protected BigDecimal getMinimalBeitragProZeiteinheit(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMinVerguenstigungProTg();
	}

	@Nonnull
	@Override
	protected BigDecimal getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull BigDecimal bgPensum) {

		BigDecimal oeffnungstage = parameterDTO.getOeffnungstageKita();
		BigDecimal anteilMonat = getAnteilMonat(parameterDTO, von, bis);
		BigDecimal pensum = MathUtil.EXACT.pctToFraction(bgPensum);
		return MATH.multiplyNullSafe(MATH.divide(oeffnungstage, MATH.from(12)), anteilMonat, pensum);
	}

	@Nonnull
	@Override
	protected BigDecimal getAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		return calculateAnteilMonatInklWeekend(von, bis);
	}

	@Nonnull
	@Override
	protected BigDecimal getMaximaleVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult) {
		if (unter12Monate) {
			return parameterDTO.getMaxVerguenstigungVorschuleBabyProTg();
		}
		if (eingeschult) {
			return parameterDTO.getMaxVerguenstigungSchuleKindProTg();
		}
		return parameterDTO.getMaxVerguenstigungVorschuleKindProTg();
	}

	@Nonnull
	@Override
	protected BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse) {

		return besonderebeduerfnisse ? parameterDTO.getZuschlagBehinderungProTg() : BigDecimal.ZERO;
	}

	/**
	 * Berechnet den Anteil des Zeitabschnittes am gesamten Monat als dezimalzahl von 0 bis 1
	 * Dabei werden nur Werktage (d.h. sa do werden ignoriert) beruecksichtigt
	 */
	@Nonnull
	private BigDecimal calculateAnteilMonatInklWeekend(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
		LocalDate monatsanfang = von.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate monatsende = bis.with(TemporalAdjusters.lastDayOfMonth());
		long nettoTageMonat = daysBetween(monatsanfang, monatsende);
		long nettoTageIntervall = daysBetween(von, bis);
		return MathUtil.EXACT.divide(MathUtil.EXACT.from(nettoTageIntervall), MathUtil.EXACT.from(nettoTageMonat));
	}
}

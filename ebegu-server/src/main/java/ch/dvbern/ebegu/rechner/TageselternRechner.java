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
 * einer Betreuung für das Angebot Tageseltern.
 */
public class TageselternRechner extends AbstractBGRechner {

	@Nonnull
	@Override
	protected BigDecimal getMinimalBeitragProZeiteinheit(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMinVerguenstigungProStd();
	}

	@Nonnull
	@Override
	protected BigDecimal getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nonnull BigDecimal bgPensum) {

		BigDecimal oeffnungstage = parameterDTO.getOeffnungstageTFO();
		BigDecimal oeffnungsstunden = parameterDTO.getOeffnungsstundenTFO();
		BigDecimal anteilMonat = getAnteilMonat(parameterDTO, von, bis);
		BigDecimal pensum = MathUtil.EXACT.pctToFraction(bgPensum);
		BigDecimal stundenGemaessPensumUndAnteilMonat =
			MATH.multiplyNullSafe(MATH.divide(oeffnungstage, MATH.from(12)), anteilMonat, pensum, oeffnungsstunden);
		return stundenGemaessPensumUndAnteilMonat;
	}

	@Nonnull
	@Override
	protected BigDecimal getAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		LocalDate monatsanfang = von.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate monatsende = bis.with(TemporalAdjusters.lastDayOfMonth());
		BigDecimal oeffnungsstunden = parameterDTO.getOeffnungsstundenTFO();
		long nettoTageMonat = daysBetween(monatsanfang, monatsende);
		long nettoTageIntervall = daysBetween(von, bis);
		long stundenMonat = nettoTageMonat * oeffnungsstunden.longValue();
		long stundenIntervall = nettoTageIntervall * oeffnungsstunden.longValue();
		return MathUtil.EXACT.divide(MathUtil.EXACT.from(stundenIntervall), MathUtil.EXACT.from(stundenMonat));
	}

	@Nonnull
	@Override
	protected BigDecimal getMaximaleVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult) {

		if (unter12Monate) {
			return parameterDTO.getMaxVerguenstigungVorschuleBabyProStd();
		}
		if (eingeschult) {
			return parameterDTO.getMaxVerguenstigungSchuleKindProStd();
		}
		return parameterDTO.getMaxVerguenstigungVorschuleKindProStd();
	}

	@Nonnull
	@Override
	protected  BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse) {

		return besonderebeduerfnisse ? parameterDTO.getZuschlagBehinderungProStd() : BigDecimal.ZERO;
	}
}

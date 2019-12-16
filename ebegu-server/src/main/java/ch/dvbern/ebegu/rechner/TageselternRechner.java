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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.roundToNearestQuarter;

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
		@Nonnull BigDecimal anteilMonat,
		@Nonnull BigDecimal bgPensum) {

		BigDecimal oeffnungstageProJahr = parameterDTO.getOeffnungstageTFO();
		BigDecimal oeffnungsstundenProTag = parameterDTO.getOeffnungsstundenTFO();
		BigDecimal pensum = MathUtil.EXACT.pctToFraction(bgPensum);
		BigDecimal oeffnungstageProMonat = MATH.divide(oeffnungstageProJahr, MATH.from(12));
		BigDecimal stundenGemaessPensumUndAnteilMonat =
			MATH.multiplyNullSafe(oeffnungstageProMonat, anteilMonat, pensum, oeffnungsstundenProTag);

		return stundenGemaessPensumUndAnteilMonat;
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
	protected BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse) {

		return besonderebeduerfnisse ? parameterDTO.getZuschlagBehinderungProStd() : BigDecimal.ZERO;
	}

	@Nonnull
	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.HOURS;
	}

	@Override
	protected void roundIfNecessary(@Nonnull BGCalculationResult result) {
		// Die Stundenwerte (Betreuungsstunden, Anspruchsstunden und BG-Stunden) müssen auf 0.25 gerundet werden
		result.setVerfuegteAnzahlZeiteinheiten(roundToNearestQuarter(result.getVerfuegteAnzahlZeiteinheiten()));
		result.setAnspruchsberechtigteAnzahlZeiteinheiten(roundToNearestQuarter(result.getAnspruchsberechtigteAnzahlZeiteinheiten()));
		result.setBetreuungspensumZeiteinheit(roundToNearestQuarter(result.getBetreuungspensumZeiteinheit()));
	}
}

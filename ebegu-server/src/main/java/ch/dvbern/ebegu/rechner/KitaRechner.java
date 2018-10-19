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
		@Nonnull BGRechnerParameterDTO parameterDTO, @Nonnull LocalDate von, @Nonnull
		LocalDate bis) {
		return calculateAnteilMonatInklWeekend(von, bis);
	}

	@Nonnull
	private BigDecimal getMaximaleVerguenstigungProTag(
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

	@Override
	@Nonnull
	protected BigDecimal getVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult,
		@Nonnull Boolean besonderebeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen) {

		BigDecimal maximaleVerguenstigungProTag =
			getMaximaleVerguenstigungProTag(parameterDTO, unter12Monate, eingeschult);
		BigDecimal minEinkommen = parameterDTO.getMinMassgebendesEinkommen();
		BigDecimal maxEinkommen = parameterDTO.getMaxMassgebendesEinkommen();

		BigDecimal op1 = MATH.divide(maximaleVerguenstigungProTag, MATH.subtract(minEinkommen, maxEinkommen));
		BigDecimal op2 = MATH.subtract(massgebendesEinkommen, minEinkommen);
		BigDecimal augment = MATH.multiplyNullSafe(op1, op2);
		BigDecimal verguenstigungProTag = MATH.add(augment, maximaleVerguenstigungProTag);
		// Max und Min beachten
		verguenstigungProTag = verguenstigungProTag.min(maximaleVerguenstigungProTag);
		verguenstigungProTag = verguenstigungProTag.max(BigDecimal.ZERO);
		// (Fixen) Zuschlag fuer Besondere Beduerfnisse
		BigDecimal zuschlagFuerBesondereBeduerfnisse =
			getZuschlagFuerBesondereBeduerfnisse(parameterDTO, besonderebeduerfnisse);
		return MATH.add(verguenstigungProTag, zuschlagFuerBesondereBeduerfnisse);
	}

	@Nonnull
	private BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse) {

		return besonderebeduerfnisse ? parameterDTO.getZuschlagBehinderungProTg() : BigDecimal.ZERO;
	}
}

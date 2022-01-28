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
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot KITA.
 */
public class KitaBernRechner extends AbstractGemeindeBernRechner {

	protected KitaBernRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		super(rechnerRulesForGemeinde);
	}

	@Nonnull
	@Override
	protected BigDecimal getMinimalBeitragProZeiteinheit(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMinVerguenstigungProTg();
	}

	@Nonnull
	@Override
	protected BigDecimal getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull BigDecimal anteilMonat,
		@Nonnull BigDecimal bgPensum) {

		BigDecimal oeffnungstage = parameterDTO.getOeffnungstageKita();
		BigDecimal pensum = MathUtil.EXACT.pctToFraction(bgPensum);

		return EXACT.multiplyNullSafe(EXACT.divide(oeffnungstage, EXACT.from(12)), anteilMonat, pensum);
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

	@Override
	@Nonnull
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.DAYS;
	}
}

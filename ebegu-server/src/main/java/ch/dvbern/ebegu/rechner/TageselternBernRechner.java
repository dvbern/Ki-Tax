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
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot Tageseltern.
 */
public class TageselternBernRechner extends AbstractGemeindeBernRechner {

	protected TageselternBernRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		super(rechnerRulesForGemeinde);
	}

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
		BigDecimal oeffnungstageProMonat = EXACT.divide(oeffnungstageProJahr, EXACT.from(12));
		BigDecimal stundenGemaessPensumUndAnteilMonat =
			EXACT.multiplyNullSafe(oeffnungstageProMonat, anteilMonat, pensum, oeffnungsstundenProTag);

		return stundenGemaessPensumUndAnteilMonat;
	}

	@Nonnull
	@Override
	protected BigDecimal getMaximaleVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nullable EinschulungTyp einschulungTyp) {
		boolean eingeschultKindergarten = einschulungTyp != null && einschulungTyp.isEingeschult();
		boolean eingeschultSchulstufe = einschulungTyp != null &&
				(einschulungTyp.isPrimarstufe() || einschulungTyp.isSekundarstufe());

		if (eingeschultSchulstufe) {
			return parameterDTO.getMaxVerguenstigungPrimarschuleKindProStd();
		}
		if (eingeschultKindergarten) {
			return parameterDTO.getMaxVerguenstigungKindergartenKindProStd();
		}
		if (unter12Monate) {
			return parameterDTO.getMaxVerguenstigungVorschuleBabyProStd();
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

	/**
	 * Bei Tageseletern wird auf 0.25 Stunden gerundet
	 */
	@Nonnull
	@Override
	protected Function<BigDecimal, BigDecimal> zeiteinheitenRoundingStrategy() {
		return MathUtil::roundToNearestQuarter;
	}
}

/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

public class KostenAnteilRule extends AbstractAbschlussRule {

	protected static final MathUtil EXACT = MathUtil.EXACT;

	protected KostenAnteilRule(boolean isDebug) {
		super(isDebug);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {

		zeitabschnitte.forEach(this::calculateKostenAnteil);
		return zeitabschnitte;
	}

	private void calculateKostenAnteil(VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal monatlicheBetreuungskosten = zeitabschnitt.getRelevantBgCalculationInput().getMonatlicheBetreuungskosten();
		BigDecimal betreuungspensum = zeitabschnitt.getRelevantBgCalculationInput().getBetreuungspensumProzent();
		BigDecimal bgPensum = zeitabschnitt.getRelevantBgCalculationInput().getBgPensumProzent();

		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			zeitabschnitt.getGueltigkeit().getGueltigAb(),
			zeitabschnitt.getGueltigkeit().getGueltigBis());

		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = BigDecimal.ZERO;
		if (betreuungspensum.compareTo(BigDecimal.ZERO) > 0) {
			anteilVerguenstigesPensumAmBetreuungspensum =
				EXACT.divide(bgPensum, betreuungspensum);
		}
		BigDecimal vollkostenFuerVerguenstigtesPensum =
			EXACT.multiply(monatlicheBetreuungskosten, anteilVerguenstigesPensumAmBetreuungspensum);
		zeitabschnitt.setKostenAnteilMonat(EXACT.multiply(anteilMonat, vollkostenFuerVerguenstigtesPensum));
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}
}

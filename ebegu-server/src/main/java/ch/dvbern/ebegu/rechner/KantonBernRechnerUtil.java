/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

public final class KantonBernRechnerUtil {

	private KantonBernRechnerUtil() {}

	public static BigDecimal calculateKantonalerZuschlag(
			BigDecimal minMassgebendesEinkommen,
			BigDecimal maxMassgebendesEinkommen,
			BigDecimal massgebendesEinkommen,
			BigDecimal maximaleVerguenstigung) {

		BigDecimal beruecksichtigtesEinkommen = EXACT.subtract(massgebendesEinkommen, minMassgebendesEinkommen);
		BigDecimal product = EXACT.multiplyNullSafe(maximaleVerguenstigung, beruecksichtigtesEinkommen);
		BigDecimal augment = EXACT.divide(product, EXACT.subtract(minMassgebendesEinkommen, maxMassgebendesEinkommen));
		BigDecimal verguenstigungProTag = EXACT.add(augment, maximaleVerguenstigung);

		return MathUtil.minimumMaximum(verguenstigungProTag, BigDecimal.ZERO, maximaleVerguenstigung);
	}
}

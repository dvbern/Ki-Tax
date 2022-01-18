/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;

public class FinanzielleSituationFKJVRechner extends FinanzielleSituationBernRechner {

	@Override
	@Nullable
	protected BigDecimal calcEinkommenProGS(
			@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
			@Nullable BigDecimal geschaeftsgewinnDurchschnitt,
			@Nullable BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			// Art. 12 a
			total = add(total, abstractFinanzielleSituation.getNettolohn());
			// Art. 12 b
			total = add(total, abstractFinanzielleSituation.getErsatzeinkommen());
			// Art. 12 c
			total = add(total, abstractFinanzielleSituation.getErhalteneAlimente());
			// Art. 12 e
			total = add(total, geschaeftsgewinnDurchschnitt);
			// Art. 12 f
			total = add(total, abstractFinanzielleSituation.getBruttoertraegeVermoegen());
			total = add(total, abstractFinanzielleSituation.getNettoertraegeErbengemeinschaft());
			total = add(total, abstractFinanzielleSituation.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
		}
		return total;
	}

	@Override
	protected BigDecimal calcAbzuege(
			@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
			@Nullable AbstractFinanzielleSituation finanzielleSituationGS2
	) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			totalAbzuege = calcAbzuegeGesuchstelledne(totalAbzuege, finanzielleSituationGS1);
		}
		if (finanzielleSituationGS2 != null) {
			totalAbzuege = calcAbzuegeGesuchstelledne(totalAbzuege, finanzielleSituationGS2);
		}
		return totalAbzuege;
	}

	private BigDecimal calcAbzuegeGesuchstelledne(BigDecimal totalAbzuege, AbstractFinanzielleSituation finanzielleSituationGS) {
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getGeleisteteAlimente());
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getAbzugSchuldzinsen());
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getGewinnungskosten());
		return totalAbzuege;
	}

}

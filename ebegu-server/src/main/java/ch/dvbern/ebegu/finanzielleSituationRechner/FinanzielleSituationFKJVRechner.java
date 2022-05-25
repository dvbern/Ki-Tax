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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import org.apache.commons.lang.NotImplementedException;

public class FinanzielleSituationFKJVRechner extends FinanzielleSituationBernRechner {

	@Override
	@Nullable
	protected BigDecimal calcEinkommenProGS(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
		@Nullable BigDecimal geschaeftsgewinnDurchschnitt,
		@Nullable BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			total = add(total, abstractFinanzielleSituation.getNettolohn());
			total = add(total, abstractFinanzielleSituation.getErsatzeinkommen());
			total = add(total, abstractFinanzielleSituation.getErhalteneAlimente());
			total = add(total, abstractFinanzielleSituation.getFamilienzulage());
			total = add(total, geschaeftsgewinnDurchschnitt);
			total = add(total, abstractFinanzielleSituation.getBruttoertraegeVermoegen());
			total = add(total, abstractFinanzielleSituation.getNettoertraegeErbengemeinschaft());
			if (Boolean.TRUE == abstractFinanzielleSituation.getEinkommenInVereinfachtemVerfahrenAbgerechnet()) {
				total = add(total, abstractFinanzielleSituation.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet());
			}
		}
		return total;
	}

	@Override
	public BigDecimal calcAbzuege(
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

	private BigDecimal calcAbzuegeGesuchstelledne(
		BigDecimal totalAbzuege,
		AbstractFinanzielleSituation finanzielleSituationGS) {
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getGeleisteteAlimente());
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getAbzugSchuldzinsen());
		totalAbzuege = add(totalAbzuege, finanzielleSituationGS.getGewinnungskosten());
		return totalAbzuege;
	}

	@Nonnull
	public BigDecimal getZwischetotalAbzuege(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		return calcAbzuegeGesuchstelledne(BigDecimal.ZERO, abstractFinanzielleSituation);
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// bei Bern rechnen wir nie nach Veranlagung.
		throw new NotImplementedException();
	}
}

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

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;

abstract class StaedtischerZuschlagRechner {

	public BigDecimal calculate(BGCalculationInput inputGemeinde, BGRechnerParameterDTO rechnerParameterDTO) {
		BetreuungsangebotTyp betreuungsangebotTyp = inputGemeinde.getBetreuungsangebotTyp();

		if (betreuungsangebotTyp == BetreuungsangebotTyp.KITA) {
			return calculateForKita(inputGemeinde, rechnerParameterDTO);
		}

		if (betreuungsangebotTyp == BetreuungsangebotTyp.TAGESFAMILIEN) {
			return calculateForTfo(inputGemeinde, rechnerParameterDTO);
		}

		throw new IllegalStateException(String.format("Unhandled betreuungsangebotTyp %s", betreuungsangebotTyp));
	}

	abstract BigDecimal calculateForTfo(BGCalculationInput inputGemeinde, BGRechnerParameterDTO rechnerParameterDTO);

	abstract BigDecimal calculateForKita(BGCalculationInput inputGemeinde, BGRechnerParameterDTO rechnerParameterDTO);
}

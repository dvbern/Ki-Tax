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

import ch.dvbern.ebegu.dto.BGCalculationInput;

public class MittagstischSchwyzRechner extends KitaTagestrukturenSchwyzRechner {
	@Override
	BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		return new BigDecimal("17");
	}

	@Override
	protected BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO) {
		return new BigDecimal("7.5");
	}

	@Override
	protected BigDecimal getOeffnungstage(BGRechnerParameterDTO parameterDTO) {
		return new BigDecimal("246");
	}

	@Override
	BigDecimal calculateTagesTarif(BigDecimal betreuungsTageProZeitabschnitt, BGCalculationInput input) {
		return input.getTarifHauptmahlzeit();
	}

}

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
import ch.dvbern.ebegu.enums.PensumUnits;

public class MittagstischSchwyzRechner extends AbstractSchwyzRechner {

	public static final BigDecimal OEFFNUNGSTAGE = new BigDecimal("246");

	@Override
	protected BigDecimal toZeiteinheitProZeitabschnitt(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BigDecimal anteilMonat) {
		return toTageProZeitAbschnitt(effektivesPensumFaktor, anteilMonat, OEFFNUNGSTAGE);
	}

	@Override
	protected BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO) {
		return new BigDecimal("7.5");
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.DAYS;
	}

	@Override
	protected BigDecimal getMittelBetragForAngebot() {
		return BigDecimal.ZERO;
	}

	@Override
	protected BigDecimal getHohereBetragForAngebot() {
		return BigDecimal.ZERO;
	}

	@Override
	protected BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameterDTO) {
		return new BigDecimal("17");
	}

	@Override
	protected BigDecimal calculateTarifProZeiteinheit(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BGCalculationInput input) {
		return input.getTarifHauptmahlzeit();
	}

}

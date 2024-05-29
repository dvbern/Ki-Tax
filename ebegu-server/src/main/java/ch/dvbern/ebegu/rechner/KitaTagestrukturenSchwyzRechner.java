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

public class KitaTagestrukturenSchwyzRechner extends AbstractSchwyzRechner {

	static final BigDecimal KITA_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT = new BigDecimal(65);
	static final BigDecimal KITA_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT = new BigDecimal(100);

	@Override
	protected BigDecimal toZeiteinheitProZeitabschnitt(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BigDecimal anteilMonat) {
		return toTageProZeitAbschnitt(effektivesPensumFaktor, anteilMonat, parameterDTO.getOeffnungstageKita());
	}

	@Override
	protected BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMinVerguenstigungProTg();
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.DAYS;
	}

	@Override
	protected BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		if (input.isBabyTarif()) {
			return parameter.getMaxVerguenstigungVorschuleBabyProTg();
		}

		var eingeschult = input.getEinschulungTyp() != null && input.getEinschulungTyp().isEingeschult();

		if (!eingeschult) {
			return parameter.getMaxVerguenstigungVorschuleKindProTg();
		}

		var betreuungInFerienzeit = input.isBetreuungInFerienzeit();

		if (Boolean.TRUE.equals(betreuungInFerienzeit)) {
			return KITA_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT;
		}

		return KITA_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT;
	}

}

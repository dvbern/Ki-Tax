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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;

public class TageselternLuzernRechner extends AbstractLuzernRechner {

	private static final BigDecimal STUNDEN_PRO_TAG = BigDecimal.valueOf(11);
	private boolean isBaby = false;

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		isBaby = verfuegungZeitabschnitt.getBgCalculationInputAsiv().isBabyTarif();
		super.calculate(verfuegungZeitabschnitt, parameterDTO);
	}

	@Override
	protected BigDecimal getMinimalTarif() {
		return inputParameter.getMinVerguenstigungProStd();
	}

	@Override
	protected BigDecimal getVollkostenTarif() {
		return isBaby ? inputParameter.getVollkostenTarifBabyTFO() : inputParameter.getVollkostenTarifKindTFO();
	}

	@Override
	protected BigDecimal calculateSelbstbehaltElternProzent() {
		BigDecimal prozentuallerSelbstbehaltGemaessFormel = calculateSelbstbehaltProzentenGemaessFormel();

		if(prozentuallerSelbstbehaltGemaessFormel.compareTo(BigDecimal.valueOf(100)) > 0) {
			return BigDecimal.valueOf(100);
		}

		return prozentuallerSelbstbehaltGemaessFormel;
	}

	@Override
	protected BigDecimal calculateBGProTagByEinkommen() {
		return calculateBetreuungsgutscheinProTagAuftrungEinkommenGemaessFormel();
	}

	@Override
	protected BigDecimal getAnzahlZeiteinheitenProMonat() {
		return EXACT.multiply(STUNDEN_PRO_TAG, WOCHEN_PRO_MONAT);
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.HOURS;
	}

	@Override
	protected BigDecimal getMinBetreuungsgutschein() {
		return  isBaby ? inputParameter.getMinBGBabyTFO() : inputParameter.getMinBGKindTFO();
	}
}

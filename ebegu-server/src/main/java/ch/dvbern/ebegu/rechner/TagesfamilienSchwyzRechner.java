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

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

public class TagesfamilienSchwyzRechner extends AbstractSchwyzRechner {

	static final BigDecimal TFO_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT = new BigDecimal("3.60");
	static final BigDecimal TFO_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT = new BigDecimal(6);
	static final BigDecimal VERMITTLUNGSGEBUEHR = new BigDecimal(4);

	static final BigDecimal BEDARFSSTUFE_2_BETRAG_PRO_STUNDE = new BigDecimal("6.60");
	static final BigDecimal BEDARFSSTUFE_3_BETRAG_PRO_STUNDE = new BigDecimal("13.20");

	@Override
	protected BigDecimal toZeiteinheitProZeitabschnitt(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BigDecimal anteilMonat) {
		BigDecimal tageProZeitAbschnitt =
			toTageProZeitAbschnitt(effektivesPensumFaktor, anteilMonat, parameterDTO.getOeffnungstageTFO());
		return EXACT.multiply(tageProZeitAbschnitt, parameterDTO.getOeffnungsstundenTFO());
	}

	@Override
	protected BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMinVerguenstigungProStd();
	}

	@Override
	protected PensumUnits getZeiteinheit() {
		return PensumUnits.HOURS;
	}

	@Override
	protected BigDecimal getBedarfsstufeZweiBetragForAngebot() {
		return BEDARFSSTUFE_2_BETRAG_PRO_STUNDE;
	}

	@Override
	protected BigDecimal getBedarfsstufeDreiBetragForAngebot() {
		return BEDARFSSTUFE_3_BETRAG_PRO_STUNDE;
	}

	@Override
	protected BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		BigDecimal normkostenOhneVermittlungsGebuehr = getNormkostenOhneVermittlungsGebuehr(input, parameter);
		var vermittlungsGebuehr = getVermittlungsKosten(input, parameter);
		return EXACT.add(normkostenOhneVermittlungsGebuehr, vermittlungsGebuehr);
	}

	protected BigDecimal getNormkostenOhneVermittlungsGebuehr(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		if (input.isBabyTarif()) {
			return parameter.getMaxVerguenstigungVorschuleBabyProStd();
		}

		var eingeschult = input.getEinschulungTyp() != null && input.getEinschulungTyp().isEingeschult();

		if (!eingeschult) {
			return parameter.getMaxVerguenstigungVorschuleKindProStd();
		}

		var betreuungInFerienzeit = input.isBetreuungInFerienzeit();

		if (Boolean.TRUE.equals(betreuungInFerienzeit)) {
			return TFO_NORMKOSTEN_PRIMARSTUFE_SCHULFREIEN_ZEIT;
		}

		return TFO_NORMKOSTEN_PRIMARSTUFE_SCHULZEIT;
	}

	protected BigDecimal getVermittlungsKosten(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		if (input.getAnwesenheitsTageProMonat().compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return EXACT.multiply(EXACT.divide(
			input.getAnwesenheitsTageProMonat(),
			calculateEffektiveBetreuungsStundenProMonat(input, parameter)), VERMITTLUNGSGEBUEHR);
	}

	private BigDecimal calculateEffektiveBetreuungsStundenProMonat(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		BigDecimal oeffnungsTageProMonat = EXACT.divide(parameter.getOeffnungstageTFO(), BigDecimal.valueOf(12));
		return EXACT.multiply(
			oeffnungsTageProMonat,
			parameter.getOeffnungsstundenTFO(),
			EXACT.pctToFraction(input.getBetreuungspensumProzent()));
	}
}

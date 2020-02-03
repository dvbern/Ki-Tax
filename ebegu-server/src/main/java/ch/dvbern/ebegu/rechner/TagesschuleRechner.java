/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

public class TagesschuleRechner extends AbstractRechner {


	public TagesschuleRechner() {
	}

	@Nonnull
	@Override
	public BGCalculationResult calculate(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt, @Nonnull BGRechnerParameterDTO parameterDTO) {
		BigDecimal minTarif = parameterDTO.getMinTarifTagesschule();
		BGCalculationResult bgResult = verfuegungZeitabschnitt.getBgCalculationResultAsiv();
		if (bgResult.getTsCalculationResultMitPaedagogischerBetreuung() != null) {
			BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleMitPaedagogischerBetreuung();
			BigDecimal tarifProStunde = calculateTarif(verfuegungZeitabschnitt, maxTarif, minTarif, parameterDTO);
			bgResult.getTsCalculationResultMitPaedagogischerBetreuung().setGebuehrProStunde(tarifProStunde);
			BigDecimal totalKostenProWoche = calculateKostenProWoche(bgResult.getTsCalculationResultMitPaedagogischerBetreuung());
			bgResult.getTsCalculationResultMitPaedagogischerBetreuung().setTotalKostenProWoche(totalKostenProWoche);
		}
		if (bgResult.getTsCalculationResultOhnePaedagogischerBetreuung() != null) {
			BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleOhnePaedagogischerBetreuung();
			BigDecimal tarifProStunde = calculateTarif(verfuegungZeitabschnitt, maxTarif, minTarif, parameterDTO);
			bgResult.getTsCalculationResultOhnePaedagogischerBetreuung().setGebuehrProStunde(tarifProStunde);
			BigDecimal totalKostenProWoche = calculateKostenProWoche(bgResult.getTsCalculationResultOhnePaedagogischerBetreuung());
			bgResult.getTsCalculationResultOhnePaedagogischerBetreuung().setTotalKostenProWoche(totalKostenProWoche);
		}
		return bgResult;
	}

	/**
	 * Berechnet den Tarif pro Stunde für einen gegebenen Zeitabschnit und für ein Modul. Es werden diverse Parameter benoetigt
	 * diese werden in einem DTO uebergeben.
	 */
	public BigDecimal calculateTarif(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull BigDecimal maxTarif,
		@Nonnull BigDecimal minTarif,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Massgebendes Einkommen der Familie. Mit Maximal und Minimalwerten "verrechnen"
		BigDecimal massgebendesEinkommen = zeitabschnitt.getMassgebendesEinkommen();
		BigDecimal tarifProStunde = null;

		// Falls der Gesuchsteller die Finanziellen Daten nicht angeben will, bekommt er der Max Tarif
		if (zeitabschnitt.getBgCalculationInputAsiv().isBezahltVollkosten()
			|| zeitabschnitt.getBgCalculationResultAsiv().isZuSpaetEingereicht()
			|| zeitabschnitt.getBgCalculationResultAsiv().getAnspruchspensumProzent() == 0) {
			tarifProStunde = maxTarif;
		} else {
			BigDecimal mataMinusMita = MathUtil.EXACT.subtract(maxTarif, minTarif);
			BigDecimal maxmEMinusMinmE = MathUtil.EXACT.subtract(parameterDTO.getMaxMassgebendesEinkommen(),
				parameterDTO.getMinMassgebendesEinkommen());
			BigDecimal divided = MathUtil.EXACT.divide(mataMinusMita, maxmEMinusMinmE);

			BigDecimal meMinusMinmE = MathUtil.EXACT.subtract(massgebendesEinkommen,
				parameterDTO.getMinMassgebendesEinkommen());

			BigDecimal multiplyDividedMeMinusMinmE = MathUtil.EXACT.multiply(divided, meMinusMinmE);

			tarifProStunde = MathUtil.DEFAULT.addNullSafe(multiplyDividedMeMinusMinmE,
				parameterDTO.getMinTarifTagesschule());

			tarifProStunde = MathUtil.minimum(tarifProStunde, minTarif);
			tarifProStunde = MathUtil.maximum(tarifProStunde, maxTarif);
		}

		return tarifProStunde;
	}

	private BigDecimal calculateKostenProWoche(TSCalculationResult result) {
		BigDecimal kostenProWoche =
			MathUtil.EXACT.multiply(result.getGebuehrProStunde(), BigDecimal.valueOf(result.getBetreuungszeitProWoche()));
		kostenProWoche = MathUtil.EXACT.divide(kostenProWoche, new BigDecimal(60));
		BigDecimal totalKostenProWoche = MathUtil.DEFAULT.addNullSafe(kostenProWoche, result.getVerpflegungskosten());
		return totalKostenProWoche;
	}
}

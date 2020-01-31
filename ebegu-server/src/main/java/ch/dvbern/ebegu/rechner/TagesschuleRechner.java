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

import ch.dvbern.ebegu.dto.BGCalculationInput;
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
		BGCalculationInput calculationInput = verfuegungZeitabschnitt.getBgCalculationInputAsiv();

		TSCalculationResult tsResultMitBetreuung =
			verfuegungZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		if (tsResultMitBetreuung != null) {
			BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleMitPaedagogischerBetreuung();
			calculateTarif(calculationInput, tsResultMitBetreuung, maxTarif, minTarif, parameterDTO);
		}
		TSCalculationResult tsResultOhneBetreuung =
			verfuegungZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		if (tsResultOhneBetreuung != null) {
			BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleOhnePaedagogischerBetreuung();
			calculateTarif(calculationInput, tsResultOhneBetreuung, maxTarif, minTarif, parameterDTO);
		}
		return verfuegungZeitabschnitt.getBgCalculationResultAsiv();
	}

	/**
	 * Berechnet den Tarif pro Stunde für einen gegebenen Zeitabschnit und für ein Modul. Es werden diverse Parameter benoetigt
	 * diese werden in einem DTO uebergeben.
	 */
	public void calculateTarif(
		@Nonnull BGCalculationInput bgCalculationInput,
		@Nonnull TSCalculationResult tsCalculationResult,
		@Nonnull BigDecimal maxTarif,
		@Nonnull BigDecimal minTarif,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Massgebendes Einkommen der Familie. Mit Maximal und Minimalwerten "verrechnen"
		BigDecimal massgebendesEinkommen = tsCalculationResult.getBgCalculationResult().getMassgebendesEinkommen();
		BigDecimal tarifProStunde = null;

		// Falls der Gesuchsteller die Finanziellen Daten nicht angeben will, bekommt er der Max Tarif
		if (bgCalculationInput.isBezahltVollkosten() || tsCalculationResult.getBgCalculationResult().isZuSpaetEingereicht()) {
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

		tsCalculationResult.setGebuehrProStunde(tarifProStunde);

		BigDecimal kostenProWoche =
			MathUtil.EXACT.multiply(tarifProStunde, BigDecimal.valueOf(tsCalculationResult.getBetreuungszeitProWoche()));
		kostenProWoche = MathUtil.EXACT.divide(kostenProWoche, new BigDecimal(60));
		BigDecimal totalKostenProWoche = MathUtil.DEFAULT.addNullSafe(kostenProWoche, tsCalculationResult.getVerpflegungskosten());
		tsCalculationResult.setTotalKostenProWoche(totalKostenProWoche);
	}

	/**
	 * Berechnet den Tarif pro Stunde für einen gegebenen Zeitabschnit und für ein Modul. Es werden diverse Parameter benoetigt
	 * diese werden in einem DTO uebergeben.
	 */
	@Deprecated
	public BigDecimal calculateTarif(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO,
		@Nonnull boolean wirdPedagogischBetreut
	) {
		// Massgebendes Einkommen der Familie. Mit Maximal und Minimalwerten "verrechnen"
		BigDecimal massgebendesEinkommen = zeitabschnitt.getMassgebendesEinkommen();

		// Falls der Gesuchsteller die Finanziellen Daten nicht angeben will, bekommt er der Max Tarif
		// TODO: Sobald im GUI die Frage nach den Vollkosten vorhanden ist, muss sichergestellt werden, dass eine Rule das "isBezahltVollkosten"-Flag setzt
		if (zeitabschnitt.getBgCalculationInputAsiv().isBezahltVollkosten()) {
			if (wirdPedagogischBetreut) {
				return parameterDTO.getMaxTarifMitPaedagogischerBetreuung();
			}
			else{
				return parameterDTO.getMaxTarifOhnePaedagogischerBetreuung();
			}
		}

		BigDecimal mataMinusMita = null;
		if (wirdPedagogischBetreut) {
			mataMinusMita = MathUtil.EXACT.subtract(parameterDTO.getMaxTarifMitPaedagogischerBetreuung(), parameterDTO.getMinTarif());
		} else {
			mataMinusMita = MathUtil.EXACT.subtract(parameterDTO.getMaxTarifOhnePaedagogischerBetreuung(),
				parameterDTO.getMinTarif());
		}
		BigDecimal maxmEMinusMinmE = MathUtil.EXACT.subtract(parameterDTO.getMaxMassgebendesEinkommen(),
			parameterDTO.getMinMassgebendesEinkommen());
		BigDecimal divided = MathUtil.EXACT.divide(mataMinusMita, maxmEMinusMinmE);


		BigDecimal meMinusMinmE = MathUtil.EXACT.subtract(massgebendesEinkommen,
			parameterDTO.getMinMassgebendesEinkommen());

		BigDecimal multiplyDividedMeMinusMinmE = MathUtil.EXACT.multiply(divided, meMinusMinmE);

		BigDecimal tarif = MathUtil.DEFAULT.addNullSafe(multiplyDividedMeMinusMinmE,
			parameterDTO.getMinTarif());

		tarif = MathUtil.minimum(tarif, parameterDTO.getMinTarif());
		if (wirdPedagogischBetreut) {
			tarif = MathUtil.maximum(tarif, parameterDTO.getMaxTarifMitPaedagogischerBetreuung());
		}
		else{
			tarif = MathUtil.maximum(tarif, parameterDTO.getMaxTarifOhnePaedagogischerBetreuung());
		}
		return tarif;
	}
}

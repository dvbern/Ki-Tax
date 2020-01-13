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

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

public class TagesschuleRechner {


	public TagesschuleRechner() {

	}

	/**
	 * Berechnet den Tarif pro Stunde für einen gegebenen Zeitabschnit und für ein Modul. Es werden diverse Parameter benoetigt
	 * diese werden in einem DTO uebergeben.
	 */
	public BigDecimal calculateTarif(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO,
		@Nonnull boolean wirdPedagogischBetreut
	) {
		// Massgebendes Einkommen der Familie. Mit Maximal und Minimalwerten "verrechnen"
		BigDecimal massgebendesEinkommen = zeitabschnitt.getMassgebendesEinkommen();

		// Falls der Gesuchsteller die Finanziellen Daten nicht angeben will, bekommt er der Max Tarif
		// TODO: Sobald im GUI die Frage nach den Vollkosten vorhanden ist, muss sichergestellt werden, dass eine Rule das "isBezahltVollkosten"-Flag setzt
		if (zeitabschnitt.getBgCalculationInput().isBezahltVollkosten()) {
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

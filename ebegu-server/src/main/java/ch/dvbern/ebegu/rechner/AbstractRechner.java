/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für alle kiBon-Rechner
 */
public abstract class AbstractRechner {

	/**
	 * Diese Methode fuehrt die Berechnung fuer den uebergebenen VerfuegungsZeitabschnitt durch.
	 */
	public final void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BGCalculationInput asivInput = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		BGCalculationResult asivResult = calculateAsiv(asivInput, parameterDTO).roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(asivResult);

		// Das Resultat Gemeinde wird nur gespeichert, wenn es ueberhaupt eine gemeindespezifische Berechnung gab
		if (verfuegungZeitabschnitt.isHasGemeindeSpezifischeBerechnung()) {
			BGCalculationInput gemeindeInput = verfuegungZeitabschnitt.getBgCalculationInputGemeinde();
			BGCalculationResult gemeindeResult = calculateGemeinde(gemeindeInput, parameterDTO)
				.map(BGCalculationResult::roundAllValues)
				.orElseGet(() -> new BGCalculationResult(asivResult));
			verfuegungZeitabschnitt.setBgCalculationResultGemeinde(gemeindeResult);
		}
	}

	/**
	 * Diese Methode fuehrt --falls konfiguriert -- die Berechnung mit dem Regelwerk von Gemeinden durch
	 */
	@Nonnull
	protected abstract Optional<BGCalculationResult> calculateGemeinde(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen BGCalculationInput durch.
	 * Es wird das Regelwerk von ASIV verwendet.
	 */
	@Nonnull
	protected abstract BGCalculationResult calculateAsiv(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Die Mahlzeitenverguenstigungen mit dem Anteil Monat verrechnen. Die Verguenstigung wurde aufgrund der *monatlichen*
	 * Mahlzeiten berechnet und ist darum bei untermonatlichen Pensen zu hoch. Ausserdem muss darf die Verguenstigung nur
	 * fuer den Anteil des verguenstigten Pensums am Betreuungspensum gewaehrt werden
	 * Beispiel: Anpruch 20%, Betreuungspensum 80%, Betreuung ueber einen halben Monat:
	 * berechneteVerguenstigung = eingegebeneVerguenstigung * 0.25 * 0.5
	 */
	protected void handleAnteileMahlzeitenverguenstigung(
		@Nonnull BGCalculationResult result, @Nonnull BigDecimal anteilMonat, @Nonnull BigDecimal anteilVerguenstigesPensumAmBetreuungspensum
	) {
		// Falls der Zeitabschnitt untermonatlich ist, muessen sowohl die Anzahl Mahlzeiten wie auch die Kosten
		// derselben mit dem Anteil des Monats sowie dem Anteil des verguenstigten Pensums am
		// Betreuungspensum korrigiert werden
		final BigDecimal hauptmahlzeitenTotal = result.getVerguenstigungMahlzeitenTotal();
		result.setVerguenstigungMahlzeitenTotal(MathUtil.DEFAULT.multiply(hauptmahlzeitenTotal, anteilMonat, anteilVerguenstigesPensumAmBetreuungspensum));
	}
}

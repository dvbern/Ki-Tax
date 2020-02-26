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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import org.apache.commons.collections.CollectionUtils;

/**
 * Superklasse für BG-Rechner der Gemeinde: Berechnet sowohl nach ASIV wie auch nach Gemeinde-spezifischen Regeln
 */
public abstract class AbstractGemeindeRechner extends AbstractAsivRechner {

	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen Verfuegungsabschnitte durch.
	 */
	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull List<RechnerRule> rechnerRules
	) {
		// Wir berechnen ASIV und Gemeinde separat:
		BGCalculationResult resultAsiv = calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputAsiv(), parameterDTO);
		resultAsiv.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(resultAsiv);

		// Zuerst die "normale" berechnung nach ASIV durchführen - jedoch mit den Input-Werten der Gemeinde
		BGCalculationResult resultGemeinde = calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputGemeinde(), parameterDTO);

		// Ermitteln, ob für diese Gemeinde überhaupt eine Rule definiert ist: Falls nicht, wollen
		// wir gar kein BGCalculationResult für die Gemeinde erstellen
		List<RechnerRule> relevantRules =
			rechnerRules
				.stream()
				.filter(abstractRechnerRule ->
					abstractRechnerRule.isConfigueredForGemeinde(parameterDTO))
				.collect(Collectors.toList());

		// Es gibt Rules: Gemeinde-Gutschein berechnen
		if (CollectionUtils.isNotEmpty(relevantRules)) {
			for (RechnerRule rechnerRule : relevantRules) {
				if (rechnerRule.isRelevantForVerfuegung(verfuegungZeitabschnitt.getBgCalculationInputGemeinde(), parameterDTO)) {
					rechnerRule.executeRule(
						verfuegungZeitabschnitt.getBgCalculationInputGemeinde(),
						resultGemeinde,
						parameterDTO);
				}
			}
			verfuegungZeitabschnitt.setBgCalculationResultGemeinde(resultGemeinde);
		}
	}
}

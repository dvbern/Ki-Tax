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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;

/**
 * Superklasse für alle kiBon-Rechner
 */
public abstract class AbstractRechner {

	/**
	 * Führt die Gesamtberechnung durch: Also Berechnung gemäss ASIV und - falls konfiguriert - Berechnung der Gemeinde
	 */
	public abstract void calculateAsivAndGemeinde(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull List<RechnerRule> rechnerRules);

	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen Verfuegungsabschnitte durch.
	 * Es wird das Regelwerk von ASIV verwendet.
	 */
	@Nonnull
	public abstract BGCalculationResult calculateAsiv(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO);
}

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

package ch.dvbern.ebegu.rechner.rules;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;

public abstract class AbstractRechnerRule {

	/**
	 * Entscheidet, ob die Rule für die betroffene Gemeinde eingeschaltet ist.
	 */
	public abstract boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Entscheidet, ob die Rule für diesen spezifischen Platz angewendet werden soll.
	 */
	public abstract boolean isRelevantForVerfuegung(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Berechnet aufgrund der (vorgängig erfolgten) Berechnung ASIV und den Input-Daten der Gemeinde das Resultat Gemeinde
	 */
	public abstract BGCalculationResult executeRule(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGCalculationResult resultAsiv, @Nonnull BGRechnerParameterDTO parameterDTO);
}

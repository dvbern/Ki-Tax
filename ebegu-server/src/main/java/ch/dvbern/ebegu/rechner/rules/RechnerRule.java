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
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;

public interface RechnerRule {

	/**
	 * Entscheidet, ob die Rule für die betroffene Gemeinde eingeschaltet ist.
	 */
	boolean isConfigueredForGemeinde(
		@Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Entscheidet, ob die Rule für diesen spezifischen Platz angewendet werden soll.
	 */
	boolean isRelevantForVerfuegung(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO);

	/**
	 * Setzt die Parameter, welche fuer diese Rule benoetigt werden
	 */
	void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter);

	/**
	 * Reset Parameter wenn Rules nicht Relevant ist
	 */
	void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter);
}

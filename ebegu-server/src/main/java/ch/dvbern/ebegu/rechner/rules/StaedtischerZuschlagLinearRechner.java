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

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rechner.KantonBernRechnerUtil;

public class StaedtischerZuschlagLinearRechner extends StaedtischerZuschlagRechner {

	@Override
	public BigDecimal calculateForTfo(
			BGCalculationInput inputGemeinde,
			BGRechnerParameterDTO rechnerParameterDTO) {
		return calculateStaedtischerZuschlag(
				rechnerParameterDTO,
				inputGemeinde,
				BGRechnerParameterGemeindeDTO::getGemeindeZusaetzlicherGutscheinLinearTfoMax);
	}

	@Override
	public BigDecimal calculateForKita(
			BGCalculationInput inputGemeinde,
			BGRechnerParameterDTO rechnerParameterDTO) {
		return calculateStaedtischerZuschlag(
				rechnerParameterDTO,
				inputGemeinde,
				BGRechnerParameterGemeindeDTO::getGemeindeZusaetzlicherGutscheinLinearKitaMax);
	}

	private BigDecimal calculateStaedtischerZuschlag(
			BGRechnerParameterDTO rechnerParameterDTO,
			BGCalculationInput inputGemeinde,
			Function<BGRechnerParameterGemeindeDTO, BigDecimal> getMaxVerguenstigung) {
		var minMassgebendesEinkommen = getMinMassgebendesEinkommen(rechnerParameterDTO);
		var maxMassgebendesEinkommen = getMaxMassgebendesEinkommen(rechnerParameterDTO);
		var massgebendesEinkommen = inputGemeinde.getMassgebendesEinkommen();
		var maximaleVerguenstigung = getMaxVerguenstigung.apply(rechnerParameterDTO.getGemeindeParameter());

		return KantonBernRechnerUtil.calculateKantonalerZuschlag(
				minMassgebendesEinkommen, maxMassgebendesEinkommen, massgebendesEinkommen, maximaleVerguenstigung
		);
	}

	private BigDecimal getMinMassgebendesEinkommen(BGRechnerParameterDTO rechnerParameterDTO) {
		return Objects.requireNonNullElse(
				rechnerParameterDTO.getGemeindeParameter()
						.getGemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen(),
				rechnerParameterDTO.getMinMassgebendesEinkommen());
	}

	private BigDecimal getMaxMassgebendesEinkommen(BGRechnerParameterDTO rechnerParameterDTO) {
		return Objects.requireNonNullElse(
				rechnerParameterDTO.getGemeindeParameter()
						.getGemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen(),
				rechnerParameterDTO.getMaxMassgebendesEinkommen());
	}
}

/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;


public class MinimalPauschalbetragGemeindeRechnerRule implements RechnerRule {

	private final Locale locale;

	public MinimalPauschalbetragGemeindeRechnerRule(@Nonnull Locale locale) {
		this.locale = locale;
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getGemeindeParameter().getGemeindePauschalbetragEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO) {
		// Nur Kita und TFO
		if (!inputGemeinde.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			return false;
		}
		if (inputGemeinde.getMassgebendesEinkommen().compareTo(parameterDTO.getGemeindeParameter().getGemeindePauschalbetragMassgebendenEinkommen()) <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setMinimalPauschalBetrag(parameterDTO.getGemeindeParameter().getGemeindePauschalbetrag());
		inputGemeinde.addBemerkung(MsgKey.MINIMAL_PAUSCHALBETRAG_GESICHERT, locale, parameterDTO.getGemeindeParameter().getGemeindePauschalbetrag());
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setMinimalPauschalBetrag(BigDecimal.ZERO);
	}
}

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
import ch.dvbern.ebegu.enums.EinschulungTyp;
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
		if (!inputGemeinde.isAbschnittLiegtNachBEGUStartdatum()) {
			return false;
		}

		// Wenn kein Betreuungspensum gibt es auch kein pauschalbetrag ausbezahlt
		if ((inputGemeinde.getBetreuungspensumProzent().compareTo(BigDecimal.ZERO) == 0)) {
			return false;
		}

		// Nur Kita und TFO
		if (!inputGemeinde.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			return false;
		}

		if (inputGemeinde.getMassgebendesEinkommen()
			.compareTo(parameterDTO.getGemeindeParameter().getGemeindePauschalbetragMassgebendenEinkommen()) <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		BigDecimal minimalPauschalbetrag;
		MsgKey msgKey;

		if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
			minimalPauschalbetrag = parameterDTO.getGemeindeParameter().getGemeindePauschalbetragKita();
			msgKey = MsgKey.MINIMAL_PAUSCHALBETRAG_GESICHERT_KITA;
		} else {
			minimalPauschalbetrag = getMinimalBetragTFO(inputGemeinde.getEinschulungTyp(), parameterDTO);
			msgKey = MsgKey.MINIMAL_PAUSCHALBETRAG_GESICHERT_TFO;
		}

		rechnerParameter.setMinimalPauschalBetrag(minimalPauschalbetrag);
		inputGemeinde.addBemerkung(msgKey, locale, minimalPauschalbetrag);
	}

	private BigDecimal getMinimalBetragTFO(EinschulungTyp einschulungTyp, BGRechnerParameterDTO parameterDTO) {
		if (einschulungTyp == null) {
			throw new IllegalArgumentException("Einschulungstyp darf nicht null sein zur Berrechnung des Gutscheins");
		}

		if (einschulungTyp.isPrimarstufe() || einschulungTyp.isSekundarstufe()) {
			return parameterDTO.getGemeindeParameter().getGemeindePauschalbetragTfoPrimarschule();
		}

		return parameterDTO.getGemeindeParameter().getGemeindePauschalbetragTfo();
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setMinimalPauschalBetrag(BigDecimal.ZERO);
	}
}

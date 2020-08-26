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

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;

public class ZusaetzlicherGutscheinGemeindeRechnerRule implements RechnerRule {

	private final Locale locale;

	public ZusaetzlicherGutscheinGemeindeRechnerRule(@Nonnull Locale locale) {
		this.locale = locale;
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGRechnerParameterDTO parameterDTO) {
		if (!isConfigueredForGemeinde(parameterDTO)) {
			return false;
		}
		boolean hasAnspruch = true;
		// (1) Anspruchsgrenze
		EinschulungTyp einschulungsTypAnspruchsgrenze = getAnspruchsgrenzeSchulstufe(inputGemeinde, parameterDTO);
		EinschulungTyp einschulungTyp = inputGemeinde.getEinschulungTyp();
		if (einschulungTyp != null) {
			boolean schulstufeErfuellt = einschulungTyp.ordinal() <= einschulungsTypAnspruchsgrenze.ordinal();
			if (!schulstufeErfuellt) {
				hasAnspruch = false;
				addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_SCHULSTUFE);
			}
		}
		// (2) Sozialhilfe
		if (inputGemeinde.isSozialhilfeempfaenger()) {
			hasAnspruch = false;
			addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE);
		}
		// (3) Betreuung in Bern
		if (!inputGemeinde.isBetreuungInGemeinde()) {
			hasAnspruch = false;
			addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE);
		}
		// (4) Lange Abwesenheit
		if (inputGemeinde.isLongAbwesenheit()) {
			// Bei Abwesenheit wird der Anspruch *nicht* auf 0 gesetzt, darum muss es hier speziell behandelt werden
			hasAnspruch = false;
		}
		return hasAnspruch;
	}

	@Override
	public void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter
	) {
		rechnerParameter.setZusaetzlicherGutscheinGemeindeBetrag(getBetragZusaetzlicherGutschein(inputGemeinde,
			parameterDTO));
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setZusaetzlicherGutscheinGemeindeBetrag(BigDecimal.ZERO);
	}

	@Nonnull
	private BigDecimal getBetragZusaetzlicherGutschein(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		// Zusatzgutschein gibts nur, wenn grundsaetzlich Anspruch vorhanden
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
				BigDecimal betragKita = rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinBetragKita();
				if (betragKita != null) {
					addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_JA_KITA, betragKita);
					return betragKita;
				}
			} else if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
				BigDecimal betragTfo = rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinBetragTfo();
				if (betragTfo != null) {
					addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_JA_TFO, betragTfo);
					return betragTfo;
				}
			} else {
				throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
			}
		}
		// Kein Anspruch: Zusatzgutschein ebenfalls 0
		return BigDecimal.ZERO;
	}

	@Nonnull
	private EinschulungTyp getAnspruchsgrenzeSchulstufe(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
			return rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita();
		}
		if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
			return rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo();
		}
		throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
	}

	private void addMessage(@Nonnull BGCalculationInput inputGemeinde, @Nonnull MsgKey msgKey, Object... args) {
		// Saemtliche Bemerkungen werden nur dann angezeigt, wenn ueberhaupt prinzipiell Anspruch besteht
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			inputGemeinde.addBemerkung(msgKey, locale, args);
		}
	}
}

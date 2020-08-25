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
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

public class ZusaetzlicherBabyGutscheinRechnerRule implements RechnerRule {

	private final Locale locale;

	public ZusaetzlicherBabyGutscheinRechnerRule(@Nonnull Locale locale) {
		this.locale = locale;
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherBabyGutscheinEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGRechnerParameterDTO parameterDTO) {
		if (!isConfigueredForGemeinde(parameterDTO)) {
			return false;
		}
		boolean hasAnspruch = true;
		// (1) Nur Kita und TFO
		if (!inputGemeinde.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			return false;
		}
		// (2) Nur Kinder bis 12 Monate
		if (!inputGemeinde.isBabyTarif()) {
			return false;
		}
		// (2) Sozialhilfe
		if (inputGemeinde.isSozialhilfeempfaenger()) {
			hasAnspruch = false;
			addMessage(inputGemeinde, MsgKey.BABYGUTSCHEIN_NEIN_SOZIALHILFE);
		}
		// (3) Lange Abwesenheit
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
		final BigDecimal babyGutscheinBetrag = calculateBetragZusaetzlicherBabyGutschein(inputGemeinde, parameterDTO);
		rechnerParameter.setZusaetzlicherBabyGutscheinBetrag(babyGutscheinBetrag);
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setZusaetzlicherBabyGutscheinBetrag(BigDecimal.ZERO);
	}

	private BigDecimal calculateBetragZusaetzlicherBabyGutschein(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		final BigDecimal minEinkommen = parameterDTO.getMinMassgebendesEinkommen();
		final BigDecimal maxEinkommen = parameterDTO.getMaxMassgebendesEinkommen();
		BigDecimal einkommen = inputGemeinde.getMassgebendesEinkommen();
		einkommen = MathUtil.minimumMaximum(einkommen, minEinkommen, maxEinkommen);
		// Bei einem Einkommen >= 160'000 entfaellt der Baby-Gutschein
		if (einkommen.compareTo(maxEinkommen) == 0) {
			return BigDecimal.ZERO;
		}
		final MathUtil MATH = MathUtil.EXACT;
		final BigDecimal maxBabyGutschein = getMaximalBetragZusaetzlicherBabyGutschein(inputGemeinde, parameterDTO);
		// Formel: (Einkommen-MinEinkommen)/(MaxEinkommen-MinEinkommen)*maxBabyGutschein
		final BigDecimal dividend = MATH.subtract(einkommen, minEinkommen);
		final BigDecimal divisor = MATH.subtract(maxEinkommen, minEinkommen);
		final BigDecimal faktor = MATH.divide(dividend, divisor);
		BigDecimal babyGutschein = MATH.multiply(faktor, maxBabyGutschein);
		babyGutschein = MathUtil.minimumMaximum(babyGutschein, BigDecimal.ZERO, maxBabyGutschein); // Kann nicht negativ sein und nicht hoeher als Max
		return babyGutschein;
	}

	@Nonnull
	private BigDecimal getMaximalBetragZusaetzlicherBabyGutschein(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		// Zusatzgutschein gibts nur, wenn grundsaetzlich Anspruch vorhanden
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
				BigDecimal betragKita = rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherBabyGutscheinBetragKita();
				if (betragKita != null) {
					addMessage(inputGemeinde, MsgKey.BABYGUTSCHEIN_JA_KITA, betragKita);
					return betragKita;
				}
			} else if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
				BigDecimal betragTfo = rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherBabyGutscheinBetragTfo();
				if (betragTfo != null) {
					addMessage(inputGemeinde, MsgKey.BABYGUTSCHEIN_JA_TFO, betragTfo);
					return betragTfo;
				}
			} else {
				throw new IllegalArgumentException("Ungültiges Angebot für Babygutschein");
			}
		}
		// Kein Anspruch: Zusatzgutschein ebenfalls 0
		return BigDecimal.ZERO;
	}

	private void addMessage(@Nonnull BGCalculationInput inputGemeinde, @Nonnull MsgKey msgKey, Object... args) {
		// Saemtliche Bemerkungen werden nur dann angezeigt, wenn ueberhaupt prinzipiell Anspruch besteht
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			inputGemeinde.addBemerkung(msgKey, locale, args);
		}
	}
}

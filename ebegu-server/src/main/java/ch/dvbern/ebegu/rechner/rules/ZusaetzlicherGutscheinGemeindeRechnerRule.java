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
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.gemeindekonfiguration.GemeindeZusaetzlicherGutscheinTyp;
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
		// (1) Nur Kita und TFO
		if (!inputGemeinde.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			return false;
		}
		boolean hasAnspruch = true;
		// (2) Anspruchsgrenze
		EinschulungTyp einschulungsTypAnspruchsgrenze = getAnspruchsgrenzeSchulstufe(inputGemeinde, parameterDTO);
		EinschulungTyp einschulungTyp = inputGemeinde.getEinschulungTyp();
		if (einschulungTyp != null) {
			boolean schulstufeErfuellt = einschulungTyp.getOrdinalitaet() <= einschulungsTypAnspruchsgrenze.getOrdinalitaet();
			if (!schulstufeErfuellt) {
				hasAnspruch = false;
				addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_SCHULSTUFE);
			}
		}
		// (3) Sozialhilfe
		if (inputGemeinde.isSozialhilfeempfaenger()) {
			hasAnspruch = false;
			addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE);
		}
		// (4) Betreuung in Bern
		if (!inputGemeinde.isBetreuungInGemeinde()) {
			hasAnspruch = false;
			addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE);
		}
		// (5) Lange Abwesenheit
		if (inputGemeinde.isLongAbwesenheit() && inputGemeinde.isBezahltKompletteVollkosten()) {
			// Bei Abwesenheit wird der Anspruch *nicht* auf 0 gesetzt, darum muss es hier speziell behandelt werden
			// Dies gilt nur, wenn die Abwesenheit während des ganzen Zeitabschnitts gilt (die ganzen Vollkosten werden von den Eltern bezahlt)
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
		// Zusatzgutschein gibts nur, wenn grundsaetzlich Anspruch *aufgrund des Einkommens* vorhanden
		// also nicht, wenn nur Anspruch auf die Pauschale fuer erweiterte Betreuung!
		if (inputGemeinde.isKeinAnspruchAufgrundEinkommen()
				|| inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		GemeindeZusaetzlicherGutscheinTyp zusaetzlicherGutscheinTyp =
				rechnerParameterDTO.getGemeindeParameter().getGemeindeZusaetzlicherGutscheinTyp();

		StaedtischerZuschlagRechner zuschlagRechner = getZuschlagRechner(zusaetzlicherGutscheinTyp);
		BigDecimal staedtischerZuschlag = zuschlagRechner.calculate(inputGemeinde, rechnerParameterDTO);

		if (staedtischerZuschlag != null) {
			if (zusaetzlicherGutscheinTyp == GemeindeZusaetzlicherGutscheinTyp.PAUSCHAL) {
				var messageKey = getZuschlagMessageKeyForPauschal(inputGemeinde.getBetreuungsangebotTyp());
				addMessage(inputGemeinde, messageKey, staedtischerZuschlag);
			}
			else if (zusaetzlicherGutscheinTyp == GemeindeZusaetzlicherGutscheinTyp.LINEAR) {
				var messageKey = getZuschlagMessageKeyForLinear(staedtischerZuschlag);
				addMessage(inputGemeinde, messageKey, staedtischerZuschlag);
			}

			return staedtischerZuschlag;
		}

		return BigDecimal.ZERO;
	}

	StaedtischerZuschlagRechner getZuschlagRechner(GemeindeZusaetzlicherGutscheinTyp zusaetzlicherGutscheinTyp) {
		if (zusaetzlicherGutscheinTyp == GemeindeZusaetzlicherGutscheinTyp.PAUSCHAL) {
			return new StaedtischerZuschlagPauschalRechner();
		}

		if (zusaetzlicherGutscheinTyp == GemeindeZusaetzlicherGutscheinTyp.LINEAR) {
			return new StaedtischerZuschlagLinearRechner();
		}

		throw new IllegalArgumentException(String.format("No Rechner for Gutscheintyp %s", zusaetzlicherGutscheinTyp));
	}

	MsgKey getZuschlagMessageKeyForPauschal(BetreuungsangebotTyp betreuungsangebotTyp) {
		if (betreuungsangebotTyp.isKita()) {
				return MsgKey.ZUSATZGUTSCHEIN_PAUSCHAL_JA_KITA;
		}
		if (betreuungsangebotTyp.isTagesfamilien()) {
			return MsgKey.ZUSATZGUTSCHEIN_PAUSCHAL_JA_TFO;
		}
		throw new IllegalArgumentException(String.format(
				"Ungültiges Angebot für Zusatzgutschein: %s",
				betreuungsangebotTyp));
	}

	MsgKey getZuschlagMessageKeyForLinear(BigDecimal staedtischerZuschlag) {
		return staedtischerZuschlag.compareTo(BigDecimal.ZERO) > 0 ?
				MsgKey.ZUSATZGUTSCHEIN_LINEAR_JA : MsgKey.ZUSATZGUTSCHEIN_LINEAR_NEIN;
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

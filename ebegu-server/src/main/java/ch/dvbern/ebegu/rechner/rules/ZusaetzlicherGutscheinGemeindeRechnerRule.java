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
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.util.MathUtil;

public class ZusaetzlicherGutscheinGemeindeRechnerRule extends AbstractRechnerRule {


	public ZusaetzlicherGutscheinGemeindeRechnerRule(@Nonnull Locale locale) {
		super(locale);
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getGemeindeZusaetzlicherGutscheinEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGRechnerParameterDTO parameterDTO) {
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
		return hasAnspruch;
	}

	@Override
	public BGCalculationResult executeRule(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGCalculationResult resultGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Werte, die aus ASIV uebernommen werden:
		BigDecimal vollkosten = resultGemeinde.getVollkosten();
		BigDecimal verguenstigungVorVollkostenUndMinimalbetragASIV = resultGemeinde.getVerguenstigungOhneBeruecksichtigungVollkosten();
		BigDecimal minBetrag = resultGemeinde.getMinimalerElternbeitrag();

		// Eigentliche Regel: Vergünstigung vor Vollkosten und Minimalbeitrag um den konfigurierten Wert erhöhen
		BigDecimal betragZusaetzlicherGutschein = getBetragZusaetzlicherGutschein(inputGemeinde, parameterDTO);
		BigDecimal verguenstigungVorVollkostenUndMinimalbetragGemeinde = MathUtil.EXACT.add(
			verguenstigungVorVollkostenUndMinimalbetragASIV,
			betragZusaetzlicherGutschein);

		// Alle davon berechneten Werte aufgrund dieser Anpassung neu berechnen
		BigDecimal vollkostenMinusMinimaltarifGemeinde = EXACT.subtract(vollkosten, minBetrag);
		BigDecimal verguenstigungVorMinimalbetragGemeinde = vollkosten.min(verguenstigungVorVollkostenUndMinimalbetragGemeinde);

		BigDecimal verguenstigungGemeinde = verguenstigungVorVollkostenUndMinimalbetragGemeinde.min(vollkostenMinusMinimaltarifGemeinde);
		BigDecimal elternbeitragGemeinde = EXACT.subtract(vollkosten, verguenstigungGemeinde);

		BigDecimal minimalerElternbeitragGekuerztGemeinde = MathUtil.DEFAULT.from(0);
		BigDecimal vollkostenMinusVerguenstigungGemeinde = MathUtil.DEFAULT.subtract(vollkosten, verguenstigungVorMinimalbetragGemeinde);
		if (vollkostenMinusVerguenstigungGemeinde.compareTo(minBetrag) <= 0) {
			minimalerElternbeitragGekuerztGemeinde = MathUtil.DEFAULT.subtract(minBetrag, vollkostenMinusVerguenstigungGemeinde);
		}

		// Das Resultat mit den neu berechneten Werten überschreiben
		resultGemeinde.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetragGemeinde);
		resultGemeinde.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetragGemeinde);
		resultGemeinde.setVerguenstigung(verguenstigungGemeinde);
		resultGemeinde.setElternbeitrag(elternbeitragGemeinde);
		resultGemeinde.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerztGemeinde);
		return resultGemeinde;
	}

	@Nonnull
	public BigDecimal getBetragZusaetzlicherGutschein(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		// Zusatzgutschein gibts nur, wenn grundsaetzlich Anspruch vorhanden
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
				BigDecimal betragKita = rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBetragKita();
				addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_JA_KITA, betragKita);
				return betragKita;
			}
			if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
				BigDecimal betragTfo = rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBetragTfo();
				addMessage(inputGemeinde, MsgKey.ZUSATZGUTSCHEIN_JA_KITA, betragTfo);
				return betragTfo;
			}
			throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
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
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita();
		}
		if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo();
		}
		throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
	}

	private void addMessage(@Nonnull BGCalculationInput inputGemeinde, @Nonnull MsgKey msgKey, Object... args) {
		// Saemtliche Bemerkungen werden nur dann angezeigt, wenn ueberhaupt prinzipiell Anspruch besteht
		if (inputGemeinde.getBgPensumProzent().compareTo(BigDecimal.ZERO) > 0) {
			inputGemeinde.getParent().addBemerkung(
				RuleKey.ZUSATZGUTSCHEIN,
				msgKey,
				getLocale(),
				args);
		}
	}
}

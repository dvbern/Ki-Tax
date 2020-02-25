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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

public class ZusaetzlicherGutscheinGemeindeRechnerRule extends AbstractRechnerRule {

	protected static final MathUtil EXACT = MathUtil.EXACT;

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getGemeindeZusaetzlicherGutscheinEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(@Nonnull BGCalculationInput inputGemeinde, @Nonnull BGRechnerParameterDTO parameterDTO) {
		EinschulungTyp einschulungsTypAnspruchsgrenze = getAnspruchsgrenzeSchulstufe(inputGemeinde, parameterDTO);
		EinschulungTyp einschulungTyp = inputGemeinde.getEinschulungTyp();
		if (einschulungTyp != null) {
			return einschulungTyp.ordinal() <= einschulungsTypAnspruchsgrenze.ordinal();
		}
		return false;
	}

	@Override
	public BGCalculationResult executeRule(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGCalculationResult resultAsiv,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Werte, die aus ASIV uebernommen werden:
		BigDecimal vollkosten = resultAsiv.getVollkosten();
		BigDecimal verguenstigungVorVollkostenUndMinimalbetragASIV = resultAsiv.getVerguenstigungOhneBeruecksichtigungVollkosten();
		BigDecimal minBetrag = resultAsiv.getMinimalerElternbeitrag();

		// Eigentliche Regel: Vergünstigung vor Vollkosten und Minimalbeitrag um den konfigurierten Wert erhöhen
		BigDecimal verguenstigungVorVollkostenUndMinimalbetragGemeinde = MathUtil.EXACT.add(verguenstigungVorVollkostenUndMinimalbetragASIV,
			getBetragZusaetzlicherGutschein(inputGemeinde, parameterDTO));

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
		BGCalculationResult resultGemeinde = new BGCalculationResult(resultAsiv);
		resultGemeinde.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetragGemeinde);
		resultGemeinde.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetragGemeinde);
		resultGemeinde.setVerguenstigung(verguenstigungGemeinde);
		resultGemeinde.setElternbeitrag(elternbeitragGemeinde);
		resultGemeinde.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerztGemeinde);
		return resultGemeinde;
	}

	private BigDecimal getBetragZusaetzlicherGutschein(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBetragKita();
		} else if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBetragTfo();
		}
		throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
	}

	private EinschulungTyp getAnspruchsgrenzeSchulstufe(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO rechnerParameterDTO
	) {
		if (inputGemeinde.getBetreuungsangebotTyp().isKita()) {
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita();
		} else if (inputGemeinde.getBetreuungsangebotTyp().isTagesfamilien()) {
			return rechnerParameterDTO.getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo();
		}
		throw new IllegalArgumentException("Ungültiges Angebot für Zusatzgutschein");
	}
}

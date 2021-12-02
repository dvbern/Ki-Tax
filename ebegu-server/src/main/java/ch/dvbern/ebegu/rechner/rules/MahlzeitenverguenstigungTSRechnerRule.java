/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungTSRechnerRule implements RechnerRule {

	private final Locale locale;

	public MahlzeitenverguenstigungTSRechnerRule(
		@Nonnull Locale locale
	) {
		this.locale = locale;
	}

	@Nonnull
	private BigDecimal getVerguenstigung(
		@Nonnull BigDecimal verguenstigungGemaessEinkommen,
		@Nonnull Map<BigDecimal, Integer> kostenUndAnzMahlzeiten,
		@Nonnull Map<BigDecimal, Integer> kostenUndAnzMahlzeitenZweiWochen,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		BigDecimal verguenstigung = BigDecimal.ZERO;
		for (Map.Entry<BigDecimal, Integer> entry : kostenUndAnzMahlzeiten.entrySet()) {
			BigDecimal verguenstigungEffektivMitBetreuung = parameterDTO.getMahlzeitenverguenstigungParameter().getVerguenstigungEffektiv(verguenstigungGemaessEinkommen,
				entry.getKey(),
				parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit());

			verguenstigung = MathUtil.DEFAULT.addNullSafe(verguenstigung,
				verguenstigungEffektivMitBetreuung.multiply(BigDecimal.valueOf(entry.getValue())));
		}

		for (Map.Entry<BigDecimal, Integer> entry : kostenUndAnzMahlzeitenZweiWochen.entrySet()) {
			BigDecimal verguenstigungEffektivMitBetreuung = parameterDTO.getMahlzeitenverguenstigungParameter().getVerguenstigungEffektiv(verguenstigungGemaessEinkommen,
				entry.getKey(),
				parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit());

			verguenstigungEffektivMitBetreuung = MathUtil.DEFAULT.multiply(verguenstigungEffektivMitBetreuung, BigDecimal.valueOf(0.5));

			verguenstigung = MathUtil.DEFAULT.addNullSafe(verguenstigung,
				verguenstigungEffektivMitBetreuung.multiply(BigDecimal.valueOf(entry.getValue())));
		}
		return verguenstigung;
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMahlzeitenverguenstigungEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		if(!inputGemeinde.getBetreuungsangebotTyp().isTagesschule()){
			return false;
		}

		if (!inputGemeinde.getVerguenstigungMahlzeitenBeantragt()) {
			return false;
		}

		if (!parameterDTO.getMahlzeitenverguenstigungParameter().isEnabled()) {
			return false;
		}
		return true;
	}

	@Override
	public void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		BigDecimal verguenstigungGemaessEinkommen =
			parameterDTO.getMahlzeitenverguenstigungParameter().getVerguenstigungProMahlzeitWithParam(inputGemeinde.getMassgebendesEinkommen(), inputGemeinde.isSozialhilfeempfaenger());

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigungGemaessEinkommen.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal verguenstigungMitBetreuung = getVerguenstigung(
				verguenstigungGemaessEinkommen,
				inputGemeinde.getTsInputMitBetreuung().getVerpflegungskostenUndMahlzeiten(),
				inputGemeinde.getTsInputMitBetreuung().getVerpflegungskostenUndMahlzeitenZweiWochen(),
				parameterDTO);
			BigDecimal verguenstigungOhneBetreuung = getVerguenstigung(
				verguenstigungGemaessEinkommen,
				inputGemeinde.getTsInputOhneBetreuung().getVerpflegungskostenUndMahlzeiten(),
				inputGemeinde.getTsInputOhneBetreuung().getVerpflegungskostenUndMahlzeitenZweiWochen(),
				parameterDTO);

			if (verguenstigungMitBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputGemeinde.setTsVerpflegungskostenVerguenstigtMitBetreuung(verguenstigungMitBetreuung);
				inputGemeinde.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS, locale, parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit());
			}
			if (verguenstigungOhneBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputGemeinde.setTsVerpflegungskostenVerguenstigtOhneBetreuung(verguenstigungOhneBetreuung);
				inputGemeinde.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS, locale, parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit());
			}
		} else {
			// Bemerkung, wenn keine Verguenstigung aufgrund Einkommen
			inputGemeinde.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS_NEIN, locale);
		}
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {

	}
}

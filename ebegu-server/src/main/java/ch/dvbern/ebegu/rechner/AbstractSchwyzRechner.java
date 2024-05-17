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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

abstract class AbstractSchwyzRechner extends AbstractRechner {
	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();

		var anteilMonat = calculateAnteilMonat(verfuegungZeitabschnitt);
		var obergrenze = parameterDTO.getMaxMassgebendesEinkommen();
		var untergrenze = parameterDTO.getMinMassgebendesEinkommen();
		var minimalTarif = getMinimalTarif(parameterDTO);
		var bgPensumFaktor = EXACT.pctToFraction(input.getBgPensumProzent());
		var effektivesPensumFaktor = EXACT.pctToFraction(input.getBetreuungspensumProzent());
		var anspruchsPensumFaktor = EXACT.pctToFraction(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		var anspruchsberechtigtesEinkommen = input.getMassgebendesEinkommen();
		var geschwisterBonus = calculateGeschwisterBonus(input);
		var effektiveBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, effektivesPensumFaktor, anteilMonat);
		var bgBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, bgPensumFaktor, anteilMonat);
		var anspruchsberechtigteBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, anspruchsPensumFaktor, anteilMonat);

		var normKosten = calculateNormkosten(input, parameterDTO);
		var tagesTarif = calculateTagesTarif(effektiveBetreuungsZeiteinheitProZeitabschnitt, input);
		var tarif = tagesTarif.min(normKosten);

		var u = EXACT.multiply(EXACT.divide(minimalTarif, normKosten), EXACT.subtract(BigDecimal.ONE, geschwisterBonus));
		var z = EXACT.divide(EXACT.subtract(BigDecimal.ONE, u), EXACT.subtract(obergrenze, untergrenze));
		var selbstbehaltFaktor = calculateSelbstbehaltFaktor(u, z, anspruchsberechtigtesEinkommen, untergrenze);

		var selbstbehaltProZeiteinheit = EXACT.multiply(tarif, selbstbehaltFaktor);
		var beitragProZeiteinheitVorAbzug = EXACT.multiply(tarif, EXACT.subtract(BigDecimal.ONE, selbstbehaltFaktor));
		var minimalerBeitragDerErziehungsberechtigtenProZeiteinheit =
			BigDecimal.ZERO.max(EXACT.subtract(minimalTarif, selbstbehaltProZeiteinheit));
		var totalBetreuungsbeitragProZeiteinheit =
			BigDecimal.ZERO.max(EXACT.subtract(
				beitragProZeiteinheitVorAbzug,
				minimalerBeitragDerErziehungsberechtigtenProZeiteinheit));
		var gutschein = EXACT.multiply(totalBetreuungsbeitragProZeiteinheit, bgBetreuungsZeiteinheitProZeitabschnitt);
		var vollkosten = EXACT.multiply(input.getMonatlicheBetreuungskosten(), anteilMonat);
		var gutscheinVorAbzugSelbstbehalt =
			Objects.requireNonNull(EXACT.multiply(beitragProZeiteinheitVorAbzug, bgBetreuungsZeiteinheitProZeitabschnitt));
		var minimalerBeitragProZeitAbschnitt = EXACT.multiply(minimalTarif, bgBetreuungsZeiteinheitProZeitabschnitt);
		var elternbeitrag = EXACT.multiply(selbstbehaltProZeiteinheit, bgBetreuungsZeiteinheitProZeitabschnitt);
		var minimalerElternbeitragGekuerzt =
			EXACT.multiply(minimalerBeitragDerErziehungsberechtigtenProZeiteinheit, bgBetreuungsZeiteinheitProZeitabschnitt);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		// Mapping auf Verfuegung
		// Punkt I
		result.setBetreuungspensumProzent(input.getBetreuungspensumProzent());
		result.setBetreuungspensumZeiteinheit(effektiveBetreuungsZeiteinheitProZeitabschnitt);
		// Punkt II
		result.setAnspruchspensumProzent(input.getAnspruchspensumProzent());
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteBetreuungsZeiteinheitProZeitabschnitt);
		// Punkt III
		result.setBgPensumZeiteinheit(bgBetreuungsZeiteinheitProZeitabschnitt);
		// Punkt IV
		result.setVollkosten(vollkosten);
		// Punkt V
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinVorAbzugSelbstbehalt);
		// Punkt VI
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutscheinVorAbzugSelbstbehalt);
		// Punkt VII
		result.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerzt);
		// Punkt VIII
		result.setVerguenstigung(gutschein);

		result.setElternbeitrag(elternbeitrag);
		result.setMinimalerElternbeitrag(minimalerBeitragProZeitAbschnitt);
		result.setZeiteinheit(getZeiteinheit());

		result.roundAllValues();

		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
		verfuegungZeitabschnitt.setBgCalculationResultGemeinde(result);
	}

	protected static BigDecimal toTageProZeitAbschnitt(
		BigDecimal pensumFaktor,
		BigDecimal anteilMonat,
		BigDecimal oeffnungstageProJahr) {
		var oeffnungsTageProMonat = EXACT.divide(oeffnungstageProJahr, BigDecimal.valueOf(12));
		return Objects.requireNonNull(EXACT.multiply(oeffnungsTageProMonat, anteilMonat, pensumFaktor));
	}

	protected static BigDecimal calculateSelbstbehaltFaktor(
		BigDecimal u,
		BigDecimal z,
		BigDecimal anspruchsberechtigtesEinkommen,
		BigDecimal untergrenze) {
		return MathUtil.minimumMaximum(
			EXACT.add(u, EXACT.multiply(z, EXACT.subtract(anspruchsberechtigtesEinkommen, untergrenze))),
			BigDecimal.ZERO,
			BigDecimal.ONE);
	}

	protected static BigDecimal calculateAnteilMonat(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		return DateUtil.calculateAnteilMonatInklWeekend(
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb(),
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis());
	}

	protected static BigDecimal calculateGeschwisterBonus(BGCalculationInput input) {
		var anzahlGeschwister = BigDecimal.valueOf(input.getAnzahlGeschwister());
		return EXACT.divide(anzahlGeschwister, BigDecimal.TEN);
	}

	protected abstract BigDecimal calculateTagesTarif(
		BigDecimal effektiveBetreuungsZeiteinheitProZeitabschnitt,
		BGCalculationInput input);

	protected abstract BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameterDTO);

	protected abstract BigDecimal toZeiteinheitProZeitabschnitt(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BigDecimal anteilMonat);

	protected abstract BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO);

	protected abstract PensumUnits getZeiteinheit();
}
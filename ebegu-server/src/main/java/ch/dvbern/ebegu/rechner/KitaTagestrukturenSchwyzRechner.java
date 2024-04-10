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

public class KitaTagestrukturenSchwyzRechner extends AbstractRechner {

	static final BigDecimal NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULZEIT = new BigDecimal(65);
	static final BigDecimal NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULFREIEN_ZEIT = new BigDecimal(100);

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();

		var anteilMonat = calculateAnteilMonat(verfuegungZeitabschnitt);
		var obergrenze = parameterDTO.getMaxMassgebendesEinkommen();
		var untergrenze = parameterDTO.getMinMassgebendesEinkommen();
		var minimalTarif = parameterDTO.getMinVerguenstigungProTg();
		var bgPensumFaktor = EXACT.pctToFraction(input.getBgPensumProzent());
		var effektivesPensumFaktor = EXACT.pctToFraction(input.getBetreuungspensumProzent());
		var anspruchsPensumFaktor = EXACT.pctToFraction(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		var anspruchsberechtigtesEinkommen = input.getMassgebendesEinkommen();
		var geschwisterBonus = calculateGeschwisterBonus(input);
		var oeffnungsTageProMonat = EXACT.divide(parameterDTO.getOeffnungstageKita(), BigDecimal.valueOf(12));
		var effektiveBetreuungsTageProZeitabschnitt =
			Objects.requireNonNull(EXACT.multiply(oeffnungsTageProMonat, effektivesPensumFaktor, anteilMonat));
		var bgBetreuungsTageProZeitabschnitt = EXACT.multiply(oeffnungsTageProMonat, bgPensumFaktor, anteilMonat);
		var anspruchsberechtigteBetreuungsTageProZeitabschnitt =
			Objects.requireNonNull(EXACT.multiply(oeffnungsTageProMonat, anspruchsPensumFaktor, anteilMonat));

		var normKosten = calculateNormkosten(input, parameterDTO);
		var tagesTarif = calculateTagesTarif(effektiveBetreuungsTageProZeitabschnitt, input);
		var tarif = tagesTarif.min(normKosten);

		var u = EXACT.multiply(EXACT.divide(minimalTarif, normKosten), EXACT.subtract(BigDecimal.ONE, geschwisterBonus));
		var z = EXACT.divide(EXACT.subtract(BigDecimal.ONE, u), EXACT.subtract(obergrenze, untergrenze));
		var selbstbehaltFaktor = calculateSelbstbehaltFaktor(u, z, anspruchsberechtigtesEinkommen, untergrenze);

		var selbstbehaltProTag = EXACT.multiply(tarif, selbstbehaltFaktor);
		var beitragProTagVorAbzug = EXACT.multiply(tarif, EXACT.subtract(BigDecimal.ONE, selbstbehaltFaktor));
		var minimalerBeitragDerErziehungsberechtigtenProTag =
			BigDecimal.ZERO.max(EXACT.subtract(minimalTarif, selbstbehaltProTag));
		var totalBetreuungsbeitragProTag =
			BigDecimal.ZERO.max(EXACT.subtract(beitragProTagVorAbzug, minimalerBeitragDerErziehungsberechtigtenProTag));
		var gutschein = EXACT.multiply(totalBetreuungsbeitragProTag, bgBetreuungsTageProZeitabschnitt);
		var vollkosten = EXACT.multiply(input.getMonatlicheBetreuungskosten(), anteilMonat);
		var gutscheinVorAbzugSelbstbehalt =
			Objects.requireNonNull(EXACT.multiply(beitragProTagVorAbzug, bgBetreuungsTageProZeitabschnitt));
		var minimalerBeitragProZeitAbschnitt = EXACT.multiply(minimalTarif, bgBetreuungsTageProZeitabschnitt);
		var elternbeitrag = EXACT.multiply(selbstbehaltProTag, bgBetreuungsTageProZeitabschnitt);
		var minimalerElternbeitragGekuerzt =
			EXACT.multiply(minimalerBeitragDerErziehungsberechtigtenProTag, bgBetreuungsTageProZeitabschnitt);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		// Mapping auf Verfuegung
		// Punkt I
		result.setBetreuungspensumProzent(input.getBetreuungspensumProzent());
		result.setBetreuungspensumZeiteinheit(effektiveBetreuungsTageProZeitabschnitt);
		// Punkt II
		result.setAnspruchspensumProzent(input.getAnspruchspensumProzent());
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteBetreuungsTageProZeitabschnitt);
		// Punkt III
		result.setBgPensumZeiteinheit(bgBetreuungsTageProZeitabschnitt);
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

	private static BigDecimal calculateSelbstbehaltFaktor(
		BigDecimal u,
		BigDecimal z,
		BigDecimal anspruchsberechtigtesEinkommen,
		BigDecimal untergrenze) {
		return MathUtil.minimumMaximum(
			EXACT.add(u, EXACT.multiply(z, EXACT.subtract(anspruchsberechtigtesEinkommen, untergrenze))),
			BigDecimal.ZERO,
			BigDecimal.ONE);
	}

	private static BigDecimal calculateAnteilMonat(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		return DateUtil.calculateAnteilMonatInklWeekend(
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb(),
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis());
	}

	private static BigDecimal calculateGeschwisterBonus(BGCalculationInput input) {
		var anzahlGeschwister = BigDecimal.valueOf(input.getAnzahlGeschwister());
		return EXACT.divide(anzahlGeschwister, BigDecimal.TEN);
	}

	protected PensumUnits getZeiteinheit() {
		return PensumUnits.DAYS;
	}

	BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameter) {
		if (input.isBabyTarif()) {
			return parameter.getMaxVerguenstigungVorschuleBabyProTg();
		}

		var eingeschult = input.getEinschulungTyp() != null && input.getEinschulungTyp().isEingeschult();

		if (!eingeschult) {
			return parameter.getMaxVerguenstigungVorschuleKindProTg();
		}

		var betreuungWaehrendSchulzeit = input.isBetreuungWaehrendSchulzeit();

		if (Boolean.TRUE.equals(betreuungWaehrendSchulzeit)) {
			return NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULZEIT;
		}

		return NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULFREIEN_ZEIT;
	}

	BigDecimal calculateTagesTarif(BigDecimal betreuungsTageProZeitabschnitt, BGCalculationInput input) {
		if (betreuungsTageProZeitabschnitt.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return EXACT.divide(input.getMonatlicheBetreuungskosten(), betreuungsTageProZeitabschnitt);
	}
}

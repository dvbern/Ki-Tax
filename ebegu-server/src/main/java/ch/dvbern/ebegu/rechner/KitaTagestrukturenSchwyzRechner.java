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
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

public class KitaTagestrukturenSchwyzRechner extends AbstractRechner {

	// TODO mobj Periodeneinstellungen?
	static final BigDecimal NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULZEIT = new BigDecimal(65);
	static final BigDecimal NORMKOSTEN_PRIMARSTUFE_WAEHREND_SCHULFREIEN_ZEIT = new BigDecimal(100);
	public static final BigDecimal WOCHEN_PRO_MONAT = BigDecimal.valueOf(4.1);
	public static final BigDecimal TAGE_PRO_WOCHE = new BigDecimal(5);

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {
		var input = verfuegungZeitabschnitt.getBgCalculationInputAsiv();

		var anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb(),
			verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis());
		var obergrenze = parameterDTO.getMaxMassgebendesEinkommen();
		var untergrenze = parameterDTO.getMinMassgebendesEinkommen();
		var minimalTarif = parameterDTO.getMinVerguenstigungProTg();
		var anspruchsberechtigtesPensum = EXACT.pctToFraction(input.getBgPensumProzent());
		var anspruchsberechtigtesEinkommen = input.getMassgebendesEinkommen();
		var anzahlGeschwister = BigDecimal.valueOf(input.getAnzahlGeschwister());

		var betreuungsTageProZeitabschnitt =
			EXACT.multiply(TAGE_PRO_WOCHE, WOCHEN_PRO_MONAT, anspruchsberechtigtesPensum, anteilMonat);

		var geschwisterBonus = EXACT.divide(anzahlGeschwister, BigDecimal.TEN);

		var normKosten = calculateNormkosten(input, parameterDTO);
		var tagesTarif = calculateTagesTarif(anspruchsberechtigtesPensum, parameterDTO, input);

		var tarif = tagesTarif.min(normKosten);

		var u = EXACT.multiply(EXACT.divide(minimalTarif, tarif), EXACT.subtract(BigDecimal.ONE, geschwisterBonus));
		var z = EXACT.divide(EXACT.subtract(BigDecimal.ONE, u), EXACT.subtract(obergrenze, untergrenze));
		var selbstbehaltFaktor =
			MathUtil.minimumMaximum(EXACT.add(u, EXACT.multiply(z, EXACT.subtract(anspruchsberechtigtesEinkommen, untergrenze))),
				BigDecimal.ZERO,
				BigDecimal.ONE);

		var selbstbehaltProTag = EXACT.multiply(tarif, selbstbehaltFaktor);
		var beitragProTagVorAbzug = EXACT.multiply(tarif, EXACT.subtract(BigDecimal.ONE, selbstbehaltFaktor));
		var minimalerBeitragDerErziehungsberechtigten = BigDecimal.ZERO.max(EXACT.subtract(minimalTarif, selbstbehaltProTag));
		var totalBetreuungsbeitragProTag =
			BigDecimal.ZERO.max(EXACT.subtract(beitragProTagVorAbzug, minimalerBeitragDerErziehungsberechtigten));
		var gutschein = EXACT.multiply(totalBetreuungsbeitragProTag, betreuungsTageProZeitabschnitt);
		var vollkosten = EXACT.multiply(input.getMonatlicheBetreuungskosten(), anteilMonat);
		var gutscheinVorAbzugSelbstbehalt = Objects.requireNonNull(EXACT.multiply(
			beitragProTagVorAbzug,
			betreuungsTageProZeitabschnitt));

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		// Mapping auf Verfuegung
		// Punkt I
		result.setBetreuungspensumProzent(input.getBetreuungspensumProzent());
		// Punkt II
		result.setAnspruchspensumZeiteinheit(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		// Punkt III
		result.setBgPensumZeiteinheit(input.getBgPensumProzent());
		// Punkt IV
		result.setVollkosten(vollkosten);
		// Punkt V
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinVorAbzugSelbstbehalt);
		// Punkt VI
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutschein);
		// Punkt VII
		result.setMinimalerElternbeitrag(minimalerBeitragDerErziehungsberechtigten);
		// Punkt VIII
		result.setVerguenstigung(gutschein);

		result.roundAllValues();
		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
		verfuegungZeitabschnitt.setBgCalculationResultGemeinde(result);
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

	BigDecimal calculateTagesTarif(
		BigDecimal anspruchsberechtigtesPensum,
		BGRechnerParameterDTO parameter,
		BGCalculationInput input) {
		var oeffnungstageKitaProMonat = EXACT.divide(parameter.getOeffnungstageKita(), BigDecimal.valueOf(12));
		var anzahlAnspruchsberechtigteTage = EXACT.multiply(anspruchsberechtigtesPensum, oeffnungstageKitaProMonat);
		return EXACT.divide(input.getMonatlicheBetreuungskosten(), anzahlAnspruchsberechtigteTage);
	}
}

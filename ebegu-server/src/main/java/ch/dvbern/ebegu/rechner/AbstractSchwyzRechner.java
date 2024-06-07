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
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

abstract class AbstractSchwyzRechner extends AbstractRechner {
	private static final BigDecimal HOHERE_BEITRAG_BASIS_BETRAG_PRO_MONAT = new BigDecimal(352);

	@Override
	@SuppressWarnings("PMD.NcssMethodCount")
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {
		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();

		var normKosten = calculateNormkosten(input, parameterDTO);

		var minimalTarif = getMinimalTarif(parameterDTO);
		var geschwisterBonus = calculateGeschwisterBonus(input);
		var u = EXACT.multiply(EXACT.divide(minimalTarif, normKosten), EXACT.subtract(BigDecimal.ONE, geschwisterBonus));

		var obergrenze = parameterDTO.getMaxMassgebendesEinkommen();
		var untergrenze = parameterDTO.getMinMassgebendesEinkommen();
		var z = EXACT.divide(EXACT.subtract(BigDecimal.ONE, u), EXACT.subtract(obergrenze, untergrenze));

		var effektivesPensumFaktor = EXACT.pctToFraction(input.getBetreuungspensumProzent());
		var tagesTarif = calculateTarifProZeiteinheit(parameterDTO, effektivesPensumFaktor, input);
		var tarif = tagesTarif.min(normKosten);

		var anspruchsberechtigtesEinkommen = input.getMassgebendesEinkommen();
		var selbstbehaltFaktor = calculateSelbstbehaltFaktor(u, z, anspruchsberechtigtesEinkommen, untergrenze);
		var selbstbehaltProZeiteinheit = EXACT.multiply(tarif, selbstbehaltFaktor);

		var minimalerBeitragDerErziehungsberechtigtenProZeiteinheit =
			BigDecimal.ZERO.max(EXACT.subtract(minimalTarif, selbstbehaltProZeiteinheit));
		var beitragProZeiteinheitVorAbzug = EXACT.multiply(tarif, EXACT.subtract(BigDecimal.ONE, selbstbehaltFaktor));

		var totalBetreuungsbeitragProZeiteinheit =
			BigDecimal.ZERO.max(EXACT.subtract(
				beitragProZeiteinheitVorAbzug,
				minimalerBeitragDerErziehungsberechtigtenProZeiteinheit));

		var bgPensumFaktor = EXACT.pctToFraction(input.getBgPensumProzent());
		var anteilMonat = calculateAnteilMonat(verfuegungZeitabschnitt);
		var bgBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, bgPensumFaktor, anteilMonat);

		var hoehererBeitrag = calculateHoereBeitragProZeitAbschnitt(input, bgBetreuungsZeiteinheitProZeitabschnitt, anteilMonat);

		var gutscheinOhneHoehererBeitrag = EXACT.multiply(totalBetreuungsbeitragProZeiteinheit, bgBetreuungsZeiteinheitProZeitabschnitt);
		var gutschein = EXACT.add(hoehererBeitrag, gutscheinOhneHoehererBeitrag);
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

		var effektiveBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, effektivesPensumFaktor, anteilMonat);
		result.setBetreuungspensumZeiteinheit(effektiveBetreuungsZeiteinheitProZeitabschnitt);

		// Punkt II
		result.setAnspruchspensumProzent(input.getAnspruchspensumProzent());

		var anspruchsPensumFaktor = EXACT.pctToFraction(BigDecimal.valueOf(input.getAnspruchspensumProzent()));
		var anspruchsberechtigteBetreuungsZeiteinheitProZeitabschnitt =
			toZeiteinheitProZeitabschnitt(parameterDTO, anspruchsPensumFaktor, anteilMonat);
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteBetreuungsZeiteinheitProZeitabschnitt);

		// Punkt III
		result.setBgPensumZeiteinheit(bgBetreuungsZeiteinheitProZeitabschnitt);
		// Punkt IV
		result.setVollkosten(vollkosten);
		// Punkt V
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(gutscheinVorAbzugSelbstbehalt);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(gutscheinVorAbzugSelbstbehalt);
		// Punkt VI
		result.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerzt);
		// Punkt VII
		result.setVerguenstigung(gutschein);
		// Punkt VIII
		result.setHoehererBeitrag(hoehererBeitrag);
		result.setBedarfsstufe(input.getBedarfsstufe());

		result.setElternbeitrag(elternbeitrag);
		result.setMinimalerElternbeitrag(minimalerBeitragProZeitAbschnitt);
		result.setZeiteinheit(getZeiteinheit());

		result.roundAllValues();

		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
		verfuegungZeitabschnitt.setBgCalculationResultGemeinde(result);
	}

	private BigDecimal calculateHoereBeitragProZeitAbschnitt(
		BGCalculationInput input,
		BigDecimal bgBetreuungsZeiteinheitProZeitabschnitt,
		BigDecimal anteilMonat) {
		if (input.getBedarfsstufe() == null || input.getBedarfsstufe().equals(Bedarfsstufe.KEINE)
		|| bgBetreuungsZeiteinheitProZeitabschnitt.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		var basisBetragProZeitabschnitt = EXACT.multiply(HOHERE_BEITRAG_BASIS_BETRAG_PRO_MONAT, anteilMonat); // nur wenn bgBetreuungsZeiteinheitProZeitabschnitt > 0
		switch (input.getBedarfsstufe()) {
		case BEDARFSSTUFE_1:
			return basisBetragProZeitabschnitt;
		case BEDARFSSTUFE_2:
			return calculateHoereBeitragFuerBedarfsstufeZwei(basisBetragProZeitabschnitt, bgBetreuungsZeiteinheitProZeitabschnitt);
		case BEDARFSSTUFE_3:
			return calculateHoereBeitragFuerBedarfsstufeDrei(basisBetragProZeitabschnitt, bgBetreuungsZeiteinheitProZeitabschnitt);
		}
		return BigDecimal.ZERO;
	}

	private  BigDecimal calculateHoereBeitragFuerBedarfsstufeZwei(BigDecimal basisBetragProZeitabschnitt, BigDecimal bgBetreuungsZeiteinheitProZeitabschnitt) {
		return EXACT.add(basisBetragProZeitabschnitt, EXACT.multiply(bgBetreuungsZeiteinheitProZeitabschnitt, getBedarfsstufeZweiBetragForAngebot()));
	}

	private  BigDecimal calculateHoereBeitragFuerBedarfsstufeDrei(BigDecimal basisBetragProZeitabschnitt, BigDecimal bgBetreuungsZeiteinheitProZeitabschnitt) {
		return EXACT.add(basisBetragProZeitabschnitt, EXACT.multiply(bgBetreuungsZeiteinheitProZeitabschnitt, getBedarfsstufeDreiBetragForAngebot()));
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

	protected BigDecimal calculateTarifProZeiteinheit(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BGCalculationInput input) {
		var effektiveBetreuungsZeiteinheitenProMonat =
			toZeiteinheitProZeitabschnitt(parameterDTO, effektivesPensumFaktor, BigDecimal.ONE);

		if (effektiveBetreuungsZeiteinheitenProMonat.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return EXACT.divide(input.getMonatlicheBetreuungskosten(), effektiveBetreuungsZeiteinheitenProMonat);
	}

	protected abstract BigDecimal calculateNormkosten(BGCalculationInput input, BGRechnerParameterDTO parameterDTO);

	protected abstract BigDecimal toZeiteinheitProZeitabschnitt(
		BGRechnerParameterDTO parameterDTO,
		BigDecimal effektivesPensumFaktor,
		BigDecimal anteilMonat);

	protected abstract BigDecimal getMinimalTarif(BGRechnerParameterDTO parameterDTO);

	protected abstract PensumUnits getZeiteinheit();
	protected abstract BigDecimal getBedarfsstufeZweiBetragForAngebot();

	protected abstract BigDecimal getBedarfsstufeDreiBetragForAngebot();

}

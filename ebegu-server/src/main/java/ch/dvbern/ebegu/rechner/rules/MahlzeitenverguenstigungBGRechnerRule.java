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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGRechnerRule implements RechnerRule {

	private static final MathUtil MATH = MathUtil.DEFAULT;
	private static final BigDecimal MAX_TAGE_MAHLZEITENVERGUENSTIGUNG = MATH.fromNullSafe(20);
	private final Locale locale;

	public MahlzeitenverguenstigungBGRechnerRule(
		@Nonnull Locale locale
	) {
		this.locale = locale;
	}

	private void addBemerkung(
			@Nonnull BGCalculationInput inputData,
			BGRechnerParameterDTO parameterDTO) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG, locale, parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit());
	}

	private boolean validateInput(@Nonnull BGCalculationInput inputData) {
		return (inputData.getAnzahlHauptmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifHauptmahlzeit().compareTo(BigDecimal.ZERO) > 0) ||
			(inputData.getAnzahlNebenmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifNebenmahlzeit().compareTo(BigDecimal.ZERO) > 0);
	}

	@Nonnull
	private BigDecimal berechneMahlzeitenverguenstigung(
		@Nonnull BigDecimal anzahlHauptmahlzeitenUngekuerzt,
		@Nonnull BigDecimal anzahlNebenmahlzeitenUngekuerzt,
		@Nonnull BigDecimal tarifProHauptmahlzeit,
		@Nonnull BigDecimal tarifProNebenmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal verguenstigungMahlzeit,
		@Nonnull BigDecimal bgPensumProzent
	) {
		// Ausgangslage fuer die Berechnung der maximalen Tage ist das BG-Pensum auf 5% gerundet.
		BigDecimal anspruchForCalcMaxTage = MathUtil.roundToFivesUp(bgPensumProzent);
		// Das Maximum der Tage fuer einen 100% Platz muss aufgrund des effektiven Betreuungspensums umgerechnet werden:
		final BigDecimal maxTageForBetreuungspensum = MATH.multiply(
			MAX_TAGE_MAHLZEITENVERGUENSTIGUNG,
			MATH.pctToFraction(anspruchForCalcMaxTage));

		// Ein vollständiger Kita Tag besteht aus 2 Nebenmahlzeiten und 1 Hauptmahlzeit
		BigDecimal anzahlNebenmahlzeitenStandardTag = new BigDecimal("2.00");

		BigDecimal maxVerguenstigungProTagNebenmahlzeiten = getMaxVerguenstigungProTagNebenmahlzeiten(
			tarifProNebenmahlzeit,
			minElternbeitragProTag,
			verguenstigungMahlzeit,
			anzahlNebenmahlzeitenStandardTag);

		// Es duerfen insgesamt max. 20 Tage beruecksichtigt werden. Wir limitieren zuerst die Anzahl Hauptmahlzeiten
		BigDecimal anzahlHauptmahlzeiten = MathUtil
			.maximum(anzahlHauptmahlzeitenUngekuerzt, maxTageForBetreuungspensum);
		// Berechnen, wieviele Tage fuer eventuelle nicht beruecksichtigte Nebenmahlzeiten uebrigbleiben (min 0)
		BigDecimal maxZusaetzlicheTageFuerNebenmahlzeiten =
			MathUtil.minimum(
				MATH.subtract(maxTageForBetreuungspensum, anzahlHauptmahlzeiten),
				BigDecimal.ZERO);

		// Nicht in HMZ berechnete NMZ
		BigDecimal nichtBerechneteNebenmahlzeitenUngekuerzt = getNichtInHauptmahlzeitenBerechneteNebenmahlzeiten(
			anzahlHauptmahlzeiten, anzahlNebenmahlzeitenUngekuerzt, anzahlNebenmahlzeitenStandardTag);
		BigDecimal nichtBerechneteNebenmahlzeiten = MathUtil.maximum(nichtBerechneteNebenmahlzeitenUngekuerzt,
			MATH.multiply(maxZusaetzlicheTageFuerNebenmahlzeiten, anzahlNebenmahlzeitenStandardTag));

		// Tasächlicher Betrag MZV HMZ/ pro Tag
		BigDecimal tatsaechlicherBetragProTagHauptmahlzeiten = getTatsaechlicherBetragProTag(
			tarifProHauptmahlzeit, minElternbeitragProTag, verguenstigungMahlzeit);

		//Minimaler Betrag MZV NMZ/ pro Tag
		BigDecimal minBetragProTagNebenmahlzeiten = getMinimaleVerguenstigungProTagNebenmahlzeiten(
			tarifProNebenmahlzeit, minElternbeitragProTag, maxVerguenstigungProTagNebenmahlzeiten);

		// Vergünstigung HMZ:
		BigDecimal verguenstigungHauptmahlzeiten = getVerguenstigungHauptmahlzeiten(
			anzahlHauptmahlzeiten,
			tatsaechlicherBetragProTagHauptmahlzeiten);

		// Vergünstigung der in HMZ nicht enthaltene NMZ:
		BigDecimal verguenstigungNebenmahlzeiten = BigDecimal.ZERO;
		if (nichtBerechneteNebenmahlzeiten.compareTo(BigDecimal.ZERO ) > 0) {
			verguenstigungNebenmahlzeiten = getVerguenstigungNebenmahlzeiten(
			anzahlNebenmahlzeitenStandardTag,
			nichtBerechneteNebenmahlzeiten,
			maxVerguenstigungProTagNebenmahlzeiten,
			minBetragProTagNebenmahlzeiten);
		}

		// Vergünstigung:
		// =Q2+R2
		// =Q2+R2
		BigDecimal verguenstigung = MATH.addNullSafe(verguenstigungHauptmahlzeiten, verguenstigungNebenmahlzeiten);
		return verguenstigung;
	}

	@Nonnull
	private BigDecimal getMaxVerguenstigungProTagNebenmahlzeiten(
		@Nonnull BigDecimal tarifProNebenmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal verguenstigungMahlzeit,
		@Nonnull BigDecimal anzahlNebenmahlzeitenStandardTag
	) {
		// =WENN(MIN(2*H6-M6;L6)<0;0;MIN(2*H6-M6;L6))
		// =WENN(MIN(2*tarifProNebenmahlzeit-minElternbeitragProTag;verguenstigungMahlzeit)<0;0;MIN(2*tarifProNebenmahlzeit-minElternbeitragProTag;verguenstigungMahlzeit))
		BigDecimal maxVerguenstigungProTagNebenmahlzeiten = MATH.subtract(
			MATH.multiply(
				anzahlNebenmahlzeitenStandardTag,
				tarifProNebenmahlzeit),
			minElternbeitragProTag);
		// aber maximal die definierte Verguenstigung aufgrund Einkommen
		maxVerguenstigungProTagNebenmahlzeiten = MathUtil.maximum(maxVerguenstigungProTagNebenmahlzeiten, verguenstigungMahlzeit);
		// darf nicht negativ sein
		maxVerguenstigungProTagNebenmahlzeiten = MathUtil.minimum(maxVerguenstigungProTagNebenmahlzeiten, BigDecimal.ZERO);
		return maxVerguenstigungProTagNebenmahlzeiten;
	}

	@Nonnull
	private BigDecimal getNichtInHauptmahlzeitenBerechneteNebenmahlzeiten(
		@Nonnull BigDecimal anzahlHauptmahlzeiten,
		@Nonnull BigDecimal anzahlNebenmahlzeiten,
		@Nonnull BigDecimal anzahlNebenmahlzeitenStandardTag
	) {
		// =WENN((I2-G2*2)<0;0;I2-G2*2)
		// =WENN((anzahlNebenmahlzeiten-anzahlHauptmahlzeiten*2)<0;0;anzahlNebenmahlzeiten-anzahlHauptmahlzeiten*2)
		BigDecimal nichtBerechneteNebenmahlzeiten = MATH.subtract(
			anzahlNebenmahlzeiten,
			MATH.multiply(anzahlHauptmahlzeiten, anzahlNebenmahlzeitenStandardTag));
		nichtBerechneteNebenmahlzeiten = MathUtil.minimum(nichtBerechneteNebenmahlzeiten, BigDecimal.ZERO);
		return nichtBerechneteNebenmahlzeiten;
	}

	@Nonnull
	private BigDecimal getTatsaechlicherBetragProTag(
		@Nonnull BigDecimal tarifProHauptmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal maxVerguenstigungProTag
	) {
		// =WENN(MIN(F2-L2;K2)<0;0;MIN(F2-L2;K2))
		// =WENN(MIN(tarifProHauptmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag)<0;0;MIN(tarifProHauptmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag))
		BigDecimal tatsaechlicherBetragProTag = MathUtil.maximum(MATH.subtract(tarifProHauptmahlzeit, minElternbeitragProTag), maxVerguenstigungProTag);
		tatsaechlicherBetragProTag = MathUtil.minimum(tatsaechlicherBetragProTag, BigDecimal.ZERO);
		return tatsaechlicherBetragProTag;
	}

	@Nonnull
	private BigDecimal getMinimaleVerguenstigungProTagNebenmahlzeiten(
		@Nonnull BigDecimal tarifProNebenmahlzeit,
		@Nonnull BigDecimal minElternbeitragProTag,
		@Nonnull BigDecimal maxVerguenstigungProTag
	) {
		// =WENN(MIN(H2-L2;K2)<0;0;MIN(H2-L2;K2))
		// =WENN(MIN(tarifProNebenmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag)<0;0;MIN(tarifProNebenmahlzeit-minElternbeitragProTag;maxVerguenstigungProTag))
		BigDecimal minBetragProTag = MathUtil.maximum(MATH.subtract(tarifProNebenmahlzeit, minElternbeitragProTag), maxVerguenstigungProTag);
		minBetragProTag = MathUtil.minimum(minBetragProTag, BigDecimal.ZERO);
		return minBetragProTag;
	}

	@Nonnull
	private BigDecimal getVerguenstigungHauptmahlzeiten(
		@Nonnull BigDecimal anzahlHauptmahlzeiten,
		@Nonnull BigDecimal tatsaechlicherBetragProTag
	) {
		// =RUNDEN(M2*G2;2)
		// =RUNDEN(tatsaechlicherBetragProTag*anzahlHauptmahlzeiten;2)
		BigDecimal verguenstigungHauptmahlzeiten = MATH.multiply(tatsaechlicherBetragProTag, anzahlHauptmahlzeiten);
		return verguenstigungHauptmahlzeiten;
	}

	@Nonnull
	private BigDecimal getVerguenstigungNebenmahlzeiten(
		@Nonnull BigDecimal anzahlNebenmahlzeitenStandardTag,
		@Nonnull BigDecimal nichtBerechneteNebenmahlzeiten,
		@Nonnull BigDecimal maxBetragProTag,
		@Nonnull BigDecimal minBetragProTag
	) {
		// =WENN(ISTGERADE(J2);O2*J2/2;(O2*(J2-1)/2)+N2)
		// =WENN(ISTGERADE(nichtBerechneteNebenmahlzeiten);maxBetragProTag*nichtBerechneteNebenmahlzeiten/2;(maxBetragProTag*(nichtBerechneteNebenmahlzeiten-1)/2)+N2)
		boolean isEven = MathUtil.isEven(nichtBerechneteNebenmahlzeiten.intValue());

		BigDecimal zuberuecksichtigendeAnzahlNebenmahlzeiten = nichtBerechneteNebenmahlzeiten;
		if (!isEven) {
			zuberuecksichtigendeAnzahlNebenmahlzeiten = MATH.subtract(zuberuecksichtigendeAnzahlNebenmahlzeiten, BigDecimal.ONE);
		}
		BigDecimal verguenstigungNebenmahlzeiten = MATH.divide(
			MATH.multiply(
				zuberuecksichtigendeAnzahlNebenmahlzeiten,
				maxBetragProTag),
			anzahlNebenmahlzeitenStandardTag);
		if (!isEven) {
			verguenstigungNebenmahlzeiten = MATH.add(verguenstigungNebenmahlzeiten, minBetragProTag);
		}
		return verguenstigungNebenmahlzeiten;
	}

	@Override
	public boolean isConfigueredForGemeinde(@Nonnull BGRechnerParameterDTO parameterDTO) {
		return parameterDTO.getMahlzeitenverguenstigungEnabled();
	}

	@Override
	public boolean isRelevantForVerfuegung(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		if(!inputGemeinde.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()){
			return false;
		}

		if (!parameterDTO.getMahlzeitenverguenstigungParameter().isEnabled() ||
			!validateInput(inputGemeinde)) {
			return false;
		}

		if (!inputGemeinde.getVerguenstigungMahlzeitenBeantragt()) {
			inputGemeinde.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG_NEIN, locale);
			return false;
		}

		final BigDecimal massgebendesEinkommen = inputGemeinde.getMassgebendesEinkommen();
		final boolean sozialhilfeempfaenger = inputGemeinde.isSozialhilfeempfaenger();

		// Bemerkung, wenn keine Verguenstigung aufgrund Einkommen
		if (!parameterDTO.getMahlzeitenverguenstigungParameter().hasAnspruch(massgebendesEinkommen, sozialhilfeempfaenger)) {
			inputGemeinde.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG_NEIN, locale);
			return false;
		}

		// Bei Abwesenheiten wird keine MZV ausbezahlt
		if (inputGemeinde.isLongAbwesenheit() && inputGemeinde.isBezahltKompletteVollkosten()) {
			return false;
		}

		return true;
	}

	@Override
	public void prepareParameter(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull RechnerRuleParameterDTO rechnerParameter) {

		final BigDecimal massgebendesEinkommen = inputGemeinde.getMassgebendesEinkommen();
		final boolean sozialhilfeempfaenger = inputGemeinde.isSozialhilfeempfaenger();

		BigDecimal verguenstigungMahlzeit =
			parameterDTO.getMahlzeitenverguenstigungParameter().getVerguenstigungProMahlzeitWithParam(massgebendesEinkommen, sozialhilfeempfaenger);
		BigDecimal selbstbehaltProTag = parameterDTO.getMahlzeitenverguenstigungParameter().getMinimalerElternbeitragMahlzeit();

		final BigDecimal verguenstigung = berechneMahlzeitenverguenstigung(
			inputGemeinde.getAnzahlHauptmahlzeiten(),
			inputGemeinde.getAnzahlNebenmahlzeiten(),
			inputGemeinde.getTarifHauptmahlzeit(),
			inputGemeinde.getTarifNebenmahlzeit(),
			selbstbehaltProTag,
			verguenstigungMahlzeit,
			inputGemeinde.getBgPensumProzent() //das kennt man noch nicht an dieser Stelle, kann noch in mutationsmerger geandert werden
		);

		if (verguenstigung.compareTo(BigDecimal.ZERO) > 0) {
			addBemerkung(inputGemeinde, parameterDTO);
		}

		rechnerParameter.setVerguenstigungMahlzeitenTotal(verguenstigung);
	}

	@Override
	public void resetParameter(@Nonnull RechnerRuleParameterDTO rechnerParameter) {
		rechnerParameter.setVerguenstigungMahlzeitenTotal(BigDecimal.ZERO);
	}
}

/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.isZero;

public class AppenzellRechner extends AbstractRechner {

	private static final double MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE = 2400;
	private static final double MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT = 1900;
	private static final MathUtil EXACT = MathUtil.EXACT;
	private BGCalculationInput input;
	private BGRechnerParameterDTO parameter;

	/*
	In Appenzell wird ein Prozentsatz der Vollkosten ausgezahlt. Der Prozentsatz wird anhand des Massgebenden Einkommen
	bestimmt. Bis zu einem massgebenden Einkommen von 40000 werde 86 % der Kosten übernommen. Von 40001 - 44000 81 % usw.
	 */
	private static final Map<BigDecimal, BigDecimal> EINKOMMEN_PROZENTUALLE_VERGUENSTIGUNG_MAP = new TreeMap<>() {{
		put(BigDecimal.valueOf(40000), BigDecimal.valueOf(0.86));
		put(BigDecimal.valueOf(44000), BigDecimal.valueOf(0.81));
		put(BigDecimal.valueOf(48000), BigDecimal.valueOf(0.76));
		put(BigDecimal.valueOf(52000), BigDecimal.valueOf(0.71));
		put(BigDecimal.valueOf(56000), BigDecimal.valueOf(0.66));
		put(BigDecimal.valueOf(60000), BigDecimal.valueOf(0.61));
		put(BigDecimal.valueOf(64000), BigDecimal.valueOf(0.56));
		put(BigDecimal.valueOf(68000), BigDecimal.valueOf(0.51));
		put(BigDecimal.valueOf(72000), BigDecimal.valueOf(0.46));
		put(BigDecimal.valueOf(76000), BigDecimal.valueOf(0.41));
		put(BigDecimal.valueOf(80000), BigDecimal.valueOf(0.36));
		put(BigDecimal.valueOf(84000), BigDecimal.valueOf(0.31));
		put(BigDecimal.valueOf(88000), BigDecimal.valueOf(0.26));
		put(BigDecimal.valueOf(92000), BigDecimal.valueOf(0.20));
		put(BigDecimal.valueOf(96000), BigDecimal.valueOf(0.14));
		put(BigDecimal.valueOf(100000), BigDecimal.valueOf(0.08));
	}};


	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		parameter = parameterDTO;

		BigDecimal anspruchpensumInStunden = calculateAnspruchpensumInStunden();
		BigDecimal betreuungspensumInStunden = calcualteBetreuungspensumInStunden();
		BigDecimal bgPensumInStunden = anspruchpensumInStunden.min(betreuungspensumInStunden);
		BigDecimal vollkostenGekuerzt = calculateVollkostenGekuerztByPensumAndMonatAnteil(bgPensumInStunden, betreuungspensumInStunden);
		BigDecimal prozentsatzAnVollkosten = getProzentsatzByMassgebendemEinkommen(input.getMassgebendesEinkommen());
		Integer prozentsatzAnVollkostenInteger = EXACT.multiply(prozentsatzAnVollkosten, BigDecimal.valueOf(100)).intValue();
		BigDecimal gutscheinGemaessFormel = calculateGutschein(vollkostenGekuerzt, bgPensumInStunden, prozentsatzAnVollkosten);

		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(this.input, result);
		result.setZeiteinheit(PensumUnits.HOURS);
		result.setAnspruchspensumZeiteinheit(anspruchpensumInStunden);
		result.setBetreuungspensumZeiteinheit(betreuungspensumInStunden);
		result.setBgPensumZeiteinheit(bgPensumInStunden);
		result.setVollkosten(vollkostenGekuerzt);
		result.setVerguenstigung(gutscheinGemaessFormel);
		result.setBeitragshoeheProzent(prozentsatzAnVollkostenInteger);
		result.roundAllValues();

		verfuegungZeitabschnitt.setBgCalculationResultAsiv(result);
	}

	private BigDecimal calculateGutschein(
		BigDecimal vollkostenGekuerzt,
		BigDecimal bgPensumInStunden,
		BigDecimal prozentsatzAnVollkosten
	) {
		if (isZero(vollkostenGekuerzt) || isZero(bgPensumInStunden)) {
			return BigDecimal.ZERO;
		}

		BigDecimal maximalerStundensatz = getMaxStundenAnsatz();
		BigDecimal effektiverStundensatz = EXACT.divideNullSafe(vollkostenGekuerzt, bgPensumInStunden);

		BigDecimal stundensatz = effektiverStundensatz;
		if (isMaxStundenAnsatzRequired()) {
			stundensatz = effektiverStundensatz.min(maximalerStundensatz);
		}
		BigDecimal vollkostenGekuerztMitMaxStundensatz = stundensatz.multiply(bgPensumInStunden);

		return EXACT.multiply(vollkostenGekuerztMitMaxStundensatz, prozentsatzAnVollkosten);
	}

	private BigDecimal getMaxStundenAnsatz() {
		if (input.isBabyTarif()) {
			return this.parameter.getMaxVerguenstigungVorschuleBabyProStd();
		}

		return this.parameter.getMaxVerguenstigungVorschuleKindProStd();
	}

	private boolean isMaxStundenAnsatzRequired() {
		return !input.isBesondereBeduerfnisseBestaetigt();
	}

	private BigDecimal getProzentsatzByMassgebendemEinkommen(BigDecimal massgebendesEinkommen) {
		for (Entry<BigDecimal, BigDecimal> einkommensstufe : EINKOMMEN_PROZENTUALLE_VERGUENSTIGUNG_MAP.entrySet()) {
			if (massgebendesEinkommen.compareTo(einkommensstufe.getKey()) <= 0) {
				return einkommensstufe.getValue();
			}
		}

		return BigDecimal.ZERO;
	}

	private BigDecimal calculateVollkostenGekuerztByPensumAndMonatAnteil(BigDecimal bgPensum, BigDecimal betreuungsPensum) {
		if (isZero(bgPensum)) {
			return bgPensum;
		}

		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			input.getParent().getGueltigkeit().getGueltigAb(),
			input.getParent().getGueltigkeit().getGueltigBis());

		BigDecimal pensumAnteil = EXACT.divideNullSafe(bgPensum, betreuungsPensum);

		return EXACT.multiply(input.getMonatlicheBetreuungskosten(), anteilMonat, pensumAnteil);
	}

	private BigDecimal calculateAnspruchpensumInStunden() {
		return EXACT.multiply(BigDecimal.valueOf(input.getAnspruchspensumProzent()), calculateMaxStundenProMonatAndProzent());
	}

	private BigDecimal calcualteBetreuungspensumInStunden() {
		return EXACT.multiply(input.getBetreuungspensumProzent(), calculateMaxStundenProMonatAndProzent());
	}

	private BigDecimal calculateMaxStundenProMonatAndProzent() {
		return BigDecimal.valueOf(getMaxBetreuungsstundenProJahr() / 100 / 12);
	}

	private double getMaxBetreuungsstundenProJahr() {
		Objects.requireNonNull(input.getEinschulungTyp());

		if (input.getEinschulungTyp().isEingeschultAppenzell()) {
			return MAX_BETREUUNGSSTUNDEN_PRO_JAHR_EINGESCHULT;
		}
		return MAX_BETREUUNGSSTUNDEN_PRO_JAHR_VORSCHULE;
	}
}

/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.TSCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

public class TagesschuleRechner extends AbstractRechner {

	public TagesschuleRechner() {
	}


	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		if (input.getParent().isHasGemeindeSpezifischeBerechnung()) {
			return Optional.of(calculateAsiv(input, parameterDTO));
		}
		return Optional.empty();
	}

	@Nonnull
	@Override
	public BGCalculationResult calculateAsiv(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BGCalculationResult bgResult = input.getParent().getBgCalculationResultAsiv();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, bgResult);

		mitPaedagogischerBetreuung(input, parameterDTO)
			.ifPresent(bgResult::setTsCalculationResultMitPaedagogischerBetreuung);

		ohnePaedagogischerBetreuung(input, parameterDTO)
			.ifPresent(bgResult::setTsCalculationResultOhnePaedagogischerBetreuung);

		// Bei Tagesschulen handelt es sich immer um Hauptmahlzeiten. Wir schreiben das Total auch aufs BGCalculationResult
		BigDecimal verpflegungskostenVerguenstigt = BigDecimal.ZERO;
		if (bgResult.getTsCalculationResultMitPaedagogischerBetreuung() != null) {
			MathUtil.DEFAULT.add(
				verpflegungskostenVerguenstigt,
				bgResult.getTsCalculationResultMitPaedagogischerBetreuung().getVerpflegungskostenVerguenstigt());
		}
		if (bgResult.getTsCalculationResultOhnePaedagogischerBetreuung() != null) {
			MathUtil.DEFAULT.add(
				verpflegungskostenVerguenstigt,
				bgResult.getTsCalculationResultOhnePaedagogischerBetreuung().getVerpflegungskostenVerguenstigt());
		}
		input.setVerguenstigungHauptmahlzeitenTotal(verpflegungskostenVerguenstigt);
		input.setVerguenstigungNebenmahlzeitenTotal(BigDecimal.ZERO);

		return bgResult;
	}

	@Nonnull
	private Optional<TSCalculationResult> mitPaedagogischerBetreuung(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleMitPaedagogischerBetreuung();

		return calculate(input, input.getTsInputMitBetreuung(), maxTarif, parameterDTO);
	}

	@Nonnull
	private Optional<TSCalculationResult> ohnePaedagogischerBetreuung(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BigDecimal maxTarif = parameterDTO.getMaxTarifTagesschuleOhnePaedagogischerBetreuung();

		return calculate(input, input.getTsInputOhneBetreuung(), maxTarif, parameterDTO);
	}

	@Nonnull
	private Optional<TSCalculationResult> calculate(
		@Nonnull BGCalculationInput sharedInput,
		@Nonnull TSCalculationInput input,
		@Nonnull BigDecimal maxTarif,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		if (!input.shouldCalculate()) {
			return Optional.empty();
		}

		BigDecimal gebuehrProStunde = calculateGebuehrProStunde(sharedInput, maxTarif, parameterDTO);
		BigDecimal betreuungsZeit = BigDecimal.valueOf(input.getBetreuungszeitProWoche());
		BigDecimal verpflegungskosten = input.getVerpflegungskosten();
		BigDecimal verpflegungskostenVerguenstigt = input.getVerpflegungskostenVerguenstigt();
		BigDecimal totalKostenProWoche =
			calculateKostenProWoche(gebuehrProStunde, betreuungsZeit, verpflegungskosten, verpflegungskostenVerguenstigt);

		TSCalculationResult result = new TSCalculationResult();
		result.setBetreuungszeitProWoche(betreuungsZeit.intValueExact());
		result.setVerpflegungskosten(verpflegungskosten);
		result.setVerpflegungskostenVerguenstigt(verpflegungskostenVerguenstigt);
		result.setGebuehrProStunde(gebuehrProStunde);
		result.setTotalKostenProWoche(totalKostenProWoche);

		return Optional.of(result);
	}

	/**
	 * Berechnet den Tarif pro Stunde für einen gegebenen Zeitabschnit und für ein Modul. Es werden diverse Parameter
	 * benoetigt diese werden in einem DTO uebergeben.
	 */
	private BigDecimal calculateGebuehrProStunde(
		@Nonnull BGCalculationInput input,
		@Nonnull BigDecimal maxTarif,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Massgebendes Einkommen der Familie. Mit Maximal und Minimalwerten "verrechnen"
		BigDecimal massgebendesEinkommen = input.getMassgebendesEinkommen();
		BigDecimal tarifProStunde = null;

		// Falls der Gesuchsteller die Finanziellen Daten nicht angeben will, bekommt er den Max Tarif
		if (input.isBezahltVollkosten()
			|| input.isZuSpaetEingereicht()
			|| input.getAnspruchspensumProzent() == 0) {
			tarifProStunde = maxTarif;
		} else {
			BigDecimal minTarif = parameterDTO.getMinTarifTagesschule();
			BigDecimal mataMinusMita = MathUtil.EXACT.subtract(maxTarif, minTarif);
			BigDecimal maxmEMinusMinmE = MathUtil.EXACT.subtract(
				parameterDTO.getMaxMassgebendesEinkommen(),
				parameterDTO.getMinMassgebendesEinkommen());
			BigDecimal divided = MathUtil.EXACT.divide(mataMinusMita, maxmEMinusMinmE);

			BigDecimal meMinusMinmE = MathUtil.EXACT.subtract(
				massgebendesEinkommen,
				parameterDTO.getMinMassgebendesEinkommen());

			BigDecimal multiplyDividedMeMinusMinmE = MathUtil.EXACT.multiply(divided, meMinusMinmE);

			tarifProStunde = MathUtil.DEFAULT.addNullSafe(multiplyDividedMeMinusMinmE, minTarif);
			tarifProStunde = MathUtil.minimumMaximum(tarifProStunde, minTarif, maxTarif);
		}

		return tarifProStunde;
	}

	private BigDecimal calculateKostenProWoche(
		BigDecimal gebuehrProStunde,
		BigDecimal betreuungszeitProWoche,
		BigDecimal verpflegungskosten,
		BigDecimal verpflegungskostenVerguenstigt) {

		BigDecimal kostenProWoche = MathUtil.EXACT.multiply(gebuehrProStunde, betreuungszeitProWoche);
		kostenProWoche = MathUtil.EXACT.divide(kostenProWoche, new BigDecimal(60));
		BigDecimal verpflegungsKostenEffektiv = MathUtil.DEFAULT.subtractNullSafe(verpflegungskosten, verpflegungskostenVerguenstigt);
		BigDecimal totalKostenProWoche = MathUtil.DEFAULT.addNullSafe(kostenProWoche, verpflegungsKostenEffektiv);

		return totalKostenProWoche;
	}
}

package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

public class VeraenderungTagesschuleTarifCalculator extends VeraenderungCalculator {

	// pädagogische betreuung
	private Map<DateRange, BigDecimal> vorgaengerGueltikeitTarifeMitBetreuungMap;
	private Map<DateRange, BigDecimal> vorgaengerGueltikeitTarifeOhneBetreuungMap;

	@Override
	public BigDecimal calculateVeraenderung(
		@NotNull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@NotNull Verfuegung vorgaengerVerfuegung) {

		mapGueltigkeitToTarifMitBetreuung(vorgaengerVerfuegung);
		mapGueltigkeitToTarifOhneBetreuung(vorgaengerVerfuegung);

		List<BigDecimal> maxVeraenderungProZeitabschnitt = zeitabschnitte
			.stream()
			.flatMap(zeitabschnitt -> findMaxVeranderungenMitUndOhneBetreuung(zeitabschnitt).stream())
			.collect(Collectors.toList());

		return findMaxAbsoluteValue(maxVeraenderungProZeitabschnitt);
	}

	@Override
	protected boolean isVerfuegungIgnorable(BigDecimal veraenderung) {
		//Wenn der Tagesschuletarif steigt, darf die Verfügung ignoriert werden (TS Veränderung zu Ungusten der Eltern)
		return veraenderung.compareTo(BigDecimal.ZERO) >= 0;
	}

	@Override
	public void calculateKorrekturAusbezahlteVerguenstigung(AbstractPlatz platz) {
		//no-op wir berechnen die korrektur für die tagesschulen noch nicht.
	}

	private void mapGueltigkeitToTarifOhneBetreuung(Verfuegung verfuegung) {
		vorgaengerGueltikeitTarifeOhneBetreuungMap = new HashMap<>();

		verfuegung.getZeitabschnitte()
			.forEach(zeitabschnitt -> {
				BigDecimal tarif = getTarifFromResult(zeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultOhnePaedagogischerBetreuung());
				vorgaengerGueltikeitTarifeOhneBetreuungMap.put(zeitabschnitt.getGueltigkeit(), tarif);
			});
	}

	private void mapGueltigkeitToTarifMitBetreuung(Verfuegung verfuegung) {
		vorgaengerGueltikeitTarifeMitBetreuungMap = new HashMap<>();

		verfuegung.getZeitabschnitte()
			.forEach(zeitabschnitt -> {
				BigDecimal tarif = getTarifFromResult(zeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultMitPaedagogischerBetreuung());
				vorgaengerGueltikeitTarifeMitBetreuungMap.put(zeitabschnitt.getGueltigkeit(), tarif);
			});
	}

	private BigDecimal getTarifFromResult(@Nullable TSCalculationResult calculationResult) {
		if (calculationResult == null) {
			return BigDecimal.ZERO;
		}

		return calculationResult.getGebuehrProStunde();
	}

	private List<BigDecimal> findMaxVeranderungenMitUndOhneBetreuung(VerfuegungZeitabschnitt zeitabschnitt) {
		return Arrays.asList(
			findMaxVeraenderungMitBetreuung(zeitabschnitt),
			findMaxVeranderungOhneBetreuung(zeitabschnitt)
		);
	}

	private BigDecimal findMaxVeranderungOhneBetreuung(VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal aktuellerTarif = getTarifFromResult(zeitabschnitt.getTsCalculationResultOhnePaedagogischerBetreuung());

		List<BigDecimal> veranderungen = calculateVeranderungForRelevanteTarife(
			aktuellerTarif,
			zeitabschnitt.getGueltigkeit(),
			vorgaengerGueltikeitTarifeOhneBetreuungMap);

		return findMaxAbsoluteValue(veranderungen);
	}

	private BigDecimal findMaxVeraenderungMitBetreuung(VerfuegungZeitabschnitt zeitabschnitt) {
		BigDecimal aktuellerTarif = getTarifFromResult(zeitabschnitt.getTsCalculationResultMitPaedagogischerBetreuung());

		List<BigDecimal> veranderungen = calculateVeranderungForRelevanteTarife(
			aktuellerTarif,
			zeitabschnitt.getGueltigkeit(),
			vorgaengerGueltikeitTarifeMitBetreuungMap);

		return findMaxAbsoluteValue(veranderungen);
	}

	private List<BigDecimal> calculateVeranderungForRelevanteTarife(
		BigDecimal aktuellerTarif,
		DateRange gueltigkeit,
		Map<DateRange, BigDecimal> gueltikeitTarifeMap) {

		return findRelevanteTarifeInMap(gueltigkeit, gueltikeitTarifeMap)
			.stream()
			.map(vorgaengerTarif -> aktuellerTarif.subtract(vorgaengerTarif))
			.collect(Collectors.toList());
	}

	private List<BigDecimal> findRelevanteTarifeInMap(DateRange gueltigkeit, Map<DateRange, BigDecimal> gueltikeitTarifeMap) {
		return gueltikeitTarifeMap
			.keySet()
			.stream()
			// relevante zeitabschnitte im vorgänger filtern
			.filter(vorgaengerGuelktigkeit -> vorgaengerGuelktigkeit.intersects(gueltigkeit))
			// für alle relevanten zeitabschnitte die tarife aus der map hohlen
			.map(gueltikeitTarifeMap::get)
			.collect(Collectors.toList());
	}

	private BigDecimal findMaxAbsoluteValue(List<BigDecimal> list) {
		return list.stream()
			.reduce(BigDecimal.ZERO, (cur, prev) -> {
				if (cur.abs().compareTo(prev.abs()) >= 0) {
					return cur;
				}

				return prev;
			});
	}

}

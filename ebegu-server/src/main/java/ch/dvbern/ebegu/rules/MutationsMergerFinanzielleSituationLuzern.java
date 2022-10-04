package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Objects;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public class MutationsMergerFinanzielleSituationLuzern extends AbstractMutationsMergerFinanzielleSituation {

	public MutationsMergerFinanzielleSituationLuzern(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {

		LocalDate finSitGueltigAb = platz.extractGesuch().getFinSitAenderungStartDatum();

		if(!isFinSitGueltigInZeitabschnitt(finSitGueltigAb, inputAktuel.getParent())) {
			//Wenn FinSit Daten noch nicht gültig sind in Zeitabschnitt, sollen die Daten aus dem Vorgänger genommen werden
			setFinSitDataFromResultToInput(inputAktuel, resultVorgaenger);
		}
	}


	private boolean isFinSitGueltigInZeitabschnitt(LocalDate finSitGueltigAb, VerfuegungZeitabschnitt zeitabschnitt) {
		Objects.requireNonNull(finSitGueltigAb);

		//finSitGueltigAb, wird erst im Folgemonat berücksitigt
		LocalDate firstDayWhenFitSitIsGueltig = finSitGueltigAb
			.plusMonths(1)
			.with(TemporalAdjusters.firstDayOfMonth());

		return firstDayWhenFitSitIsGueltig.isBefore(zeitabschnitt.getGueltigkeit().getGueltigAb()) ||
			firstDayWhenFitSitIsGueltig.isEqual(zeitabschnitt.getGueltigkeit().getGueltigAb());
	}
}

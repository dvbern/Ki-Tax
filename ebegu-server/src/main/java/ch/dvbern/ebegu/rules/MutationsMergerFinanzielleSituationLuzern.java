package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;

public class MutationsMergerFinanzielleSituationLuzern extends AbstractMutationsMergerFinanzielleSituation {

	public MutationsMergerFinanzielleSituationLuzern(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		LocalDate mutationsEingansdatum) {

	}
}

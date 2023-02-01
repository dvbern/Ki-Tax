package ch.dvbern.ebegu.rules;
import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;

public class MutationsMergerFinanzielleSituationBernFKJV extends AbstractMutationsMergerFinanzielleSituation {

	public MutationsMergerFinanzielleSituationBernFKJV(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {
	}

}

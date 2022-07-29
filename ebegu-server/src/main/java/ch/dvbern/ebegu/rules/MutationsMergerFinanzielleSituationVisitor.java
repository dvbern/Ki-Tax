package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;
import com.sun.istack.NotNull;

public class MutationsMergerFinanzielleSituationVisitor implements FinanzielleSituationTypVisitor<AbstractMutationsMergerFinanzielleSituation> {

	public AbstractMutationsMergerFinanzielleSituation getMutationsMergerFinanzielleSituation(@NotNull
		FinanzielleSituationTyp finanzielleSituationTyp) {
		return finanzielleSituationTyp.accept(this);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBern() {
		return new MutationsMergerFinanzielleSituationBern();
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBernFKJV() {
		return new MutationsMergerFinanzielleSituationBern();
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitLuzern() {
		return new MutationsMergerFinanzielleSituationLuzern();
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitSolothurn() {
		return new MutationsMergerFinanzielleSituationBern();
	}
}

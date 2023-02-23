package ch.dvbern.ebegu.rules;

import java.util.Locale;

import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;
import com.sun.istack.NotNull;

public class MutationsMergerFinanzielleSituationVisitor implements FinanzielleSituationTypVisitor<AbstractMutationsMergerFinanzielleSituation> {

	private final Locale locale;

	public MutationsMergerFinanzielleSituationVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerFinanzielleSituation getMutationsMergerFinanzielleSituation(@NotNull
		FinanzielleSituationTyp finanzielleSituationTyp) {
		return finanzielleSituationTyp.accept(this);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBern() {
		return new MutationsMergerFinanzielleSituationBern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitBernFKJV() {
		return new MutationsMergerFinanzielleSituationBernFKJV(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitLuzern() {
		return new MutationsMergerFinanzielleSituationLuzern(locale);
	}

	@Override
	public AbstractMutationsMergerFinanzielleSituation visitFinSitSolothurn() {
		return new MutationsMergerFinanzielleSituationBern(locale);
	}
}

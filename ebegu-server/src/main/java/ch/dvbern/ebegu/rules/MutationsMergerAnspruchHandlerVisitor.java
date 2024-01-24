package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

import java.util.Locale;

public class MutationsMergerAnspruchHandlerVisitor implements MandantVisitor<AbstractMutationsMergerAnspruchHandler> {

	private final Locale locale;

	public MutationsMergerAnspruchHandlerVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerAnspruchHandler getAnspruchHandler(@NotNull MandantIdentifier mandant) {
		return mandant.accept(this);
	}
	@Override
	public AbstractMutationsMergerAnspruchHandler visitBern() {
		return new MutationsMergerAnspruchHandlerBern(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitLuzern() {
		return new MutationsMergerAnspruchHandlerLuzern(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitSolothurn() {
		return visitBern();
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitAppenzellAusserrhoden() {
		return visitBern();
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitSchwyz() {
		return this.visitSolothurn();
	}
}

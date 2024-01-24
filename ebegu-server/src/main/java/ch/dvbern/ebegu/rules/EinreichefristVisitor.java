package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class EinreichefristVisitor implements MandantVisitor<AbstractEinreichefristCalculator> {


	public EinreichefristVisitor( ) { }

	public AbstractEinreichefristCalculator getEinreichefristCalculator(@NotNull MandantIdentifier mandant) {
		return mandant.accept(this);
	}

	@Override
	public AbstractEinreichefristCalculator visitBern() {
		return new EinreichefristCalculatorBern();
	}

	@Override
	public AbstractEinreichefristCalculator visitLuzern() {
		return new EinreichefristCalculatorBern();
	}

	@Override
	public AbstractEinreichefristCalculator visitSolothurn() {
		return new EinreichefristCalculatorBern();
	}

	@Override
	public AbstractEinreichefristCalculator visitAppenzellAusserrhoden() {
		return new EinreichefristCalculatorAppenzellAusserrhoden();
	}

	@Override
	public AbstractEinreichefristCalculator visitSchwyz() {
		return this.visitSolothurn();
	}
}

package ch.dvbern.ebegu.util;

import java.util.Comparator;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class BetreuungComparatorVisitor implements MandantVisitor<Comparator<AbstractPlatz>> {

	public Comparator<AbstractPlatz> getComparatorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public Comparator<AbstractPlatz> visitBern() {
		return new BetreuungComparatorBern();
	}

	@Override
	public Comparator<AbstractPlatz> visitLuzern() {
		return new BetreuungComparatorBern();
	}

	@Override
	public Comparator<AbstractPlatz> visitSolothurn() {
		return new BetreuungComparatorBern();
	}

	@Override
	public Comparator<AbstractPlatz> visitAppenzellAusserrhoden() {
		return new BetreuungComparatorAppenzell();
	}

	@Override
	public Comparator<AbstractPlatz> visitSchwyz() {
		return this.visitSolothurn();
	}
}

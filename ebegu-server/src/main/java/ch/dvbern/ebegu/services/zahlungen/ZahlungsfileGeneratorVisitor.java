package ch.dvbern.ebegu.services.zahlungen;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

@Dependent
public class ZahlungsfileGeneratorVisitor implements MandantVisitor<IZahlungsfileGenerator> {

	@Inject
	private ZahlungsfileGeneratorPain painGenerator;

	public IZahlungsfileGenerator getZahlungsfileGenerator(
		@Nonnull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public IZahlungsfileGenerator visitBern() {
		return painGenerator;
	}

	@Override
	public IZahlungsfileGenerator visitLuzern() {
		return painGenerator;
	}

	@Override
	public IZahlungsfileGenerator visitSolothurn() {
		return painGenerator;
	}
}

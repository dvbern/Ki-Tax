package ch.dvbern.ebegu.pdfgenerator.mahnung.zweitemahnung;

import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class ZweiteMahnungPdfGeneratorVisitor implements MandantVisitor<AbstractZweiteMahnungPdfGenerator> {

	private final GemeindeStammdaten stammdaten;
	private final Mahnung mahnung;
	private final Mahnung ersteMahnung;

	public ZweiteMahnungPdfGeneratorVisitor(Mahnung mahnung, Mahnung ersteMahnung, GemeindeStammdaten stammdaten) {
		this.stammdaten = stammdaten;
		this.mahnung = mahnung;
		this.ersteMahnung = ersteMahnung;
	}

	public AbstractZweiteMahnungPdfGenerator getZweiteMahnungPdfGeneratorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitBern() {
		return new ZweiteMahnungPdfGenerator(mahnung, ersteMahnung, stammdaten);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitLuzern() {
		return new ZweiteMahnungPdfGenerator(mahnung, ersteMahnung, stammdaten);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitSolothurn() {
		return new ZweiteMahnungPdfGenerator(mahnung, ersteMahnung, stammdaten);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitAppenzellAusserrhoden() {
		return new ZweiteMahnungPdfGenerator(mahnung, ersteMahnung, stammdaten);
	}

	@Override
	public AbstractZweiteMahnungPdfGenerator visitSchwyz() {
		return new ZweiteMahnungPdfGeneratorSchwyz(mahnung, ersteMahnung, stammdaten);
	}
}

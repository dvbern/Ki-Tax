package ch.dvbern.ebegu.pdfgenerator.mahnung.erstemahnung;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;

import javax.annotation.Nonnull;

public class ErsteMahnungPdfGenerator extends AbstractErsteMahnungPdfGenerator{
	public ErsteMahnungPdfGenerator(@Nonnull Mahnung mahnung, @Nonnull GemeindeStammdaten stammdaten) {
		super(mahnung, stammdaten);
	}
}

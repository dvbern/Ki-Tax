package ch.dvbern.ebegu.pdfgenerator.mahnung.erstemahnung;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import com.lowagie.text.Paragraph;

public class ErsteMahnungPdfGeneratorSchwyz extends AbstractErsteMahnungPdfGenerator{
	public ErsteMahnungPdfGeneratorSchwyz(@Nonnull Mahnung mahnung, @Nonnull GemeindeStammdaten stammdaten) {
		super(mahnung, stammdaten);
	}

	@Override
	@Nonnull
	protected Paragraph getAnrede() {
		return super.createAnrede();
	}
}

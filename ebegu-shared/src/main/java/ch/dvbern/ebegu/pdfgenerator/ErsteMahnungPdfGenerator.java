package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;

import javax.annotation.Nonnull;
import java.util.List;

public class ErsteMahnungPdfGenerator extends AbstractErsteMahnungPdfGenerator{
	public ErsteMahnungPdfGenerator(@Nonnull Mahnung mahnung, @Nonnull GemeindeStammdaten stammdaten) {
		super(mahnung, stammdaten);
	}

	@Override
	protected String getBetreuungsString(KindContainer kindContainer, List<String> betreuungenList) {
		String betreuungStr = kindContainer.getKindJA().getFullName() + " (";
		betreuungStr += String.join(", ", betreuungenList);
		betreuungStr += ")";
		return betreuungStr;
	}
}

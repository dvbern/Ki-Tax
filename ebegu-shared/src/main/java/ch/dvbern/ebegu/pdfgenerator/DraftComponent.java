package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.TextComponent;

public class DraftComponent extends TextComponent {

	public DraftComponent() {
		super(0, 0, 0, 0, OnPage.ALL);
	}
}

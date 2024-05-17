/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import com.lowagie.text.Paragraph;
import org.jetbrains.annotations.NotNull;

public class BegleitschreibenPdfGeneratorSchwyz extends BegleitschreibenPdfGenerator {

	private static final String BEGLEITSCHREIBEN_CONTENT_MIT_GEMEINDE_INFO = "PdfGeneration_Begleitschreiben_Content_Mit_Gemeinde_Info";

	public BegleitschreibenPdfGeneratorSchwyz(
		@NotNull Gesuch gesuch,
		@NotNull GemeindeStammdaten stammdaten) {
		super(gesuch, stammdaten);
	}

	@Override
	protected Paragraph getCustomBegleitschreibenParagraph() {
		return PdfUtil.createParagraph(translate(
			BEGLEITSCHREIBEN_CONTENT_MIT_GEMEINDE_INFO,
			this.gemeindeStammdaten.getTelefonForGesuch(getGesuch()),
			this.gemeindeStammdaten.getEmailForGesuch(getGesuch())), 2);
	}
}

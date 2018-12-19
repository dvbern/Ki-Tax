/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.*;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

public abstract class DokumentAnFamilieGenerator extends KibonPdfGenerator {

	protected static final String ANREDE_FAMILIE = "PdfGeneration_AnredeFamilie";
	private static final String GRUSS = "PdfGeneration_Gruss";
	private static final String SIGNIERT = "PdfGeneration_Signiert";
	private static final String SACHBEARBEITUNG = "PdfGeneration_Sachbearbeitung";

	protected DokumentAnFamilieGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(gesuch, stammdaten);
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		return getFamilieAdresse();
	}

	@Nonnull
	protected Paragraph createParagraphGruss() {
		return PdfUtil.createParagraph(translate(GRUSS), 2);
	}

	@Nonnull
	protected Paragraph createParagraphSignatur() {
		String signiert = getSachbearbeiterSigniert();
		if (signiert != null) {
			return PdfUtil.createParagraph('\n' + signiert + '\n' + translate(SACHBEARBEITUNG), 2);
		}
		return PdfUtil.createParagraph(translate(SACHBEARBEITUNG), 2);
	}

	@Nullable
	private String getSachbearbeiterSigniert() {
		Benutzer hauptVerantwortlicher = getGesuch().getDossier().getHauptVerantwortlicher();
		return hauptVerantwortlicher != null ? translate(SIGNIERT, hauptVerantwortlicher.getFullName()) : null;
	}

	protected void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte, List<String> content) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(8);
		for (int i = 0; i < content.size(); i++) {
			Chunk chunk = new Chunk(Integer.toString(i + 1) + " ", PdfUtilities.createFontWithSize(6));
			chunk.setTextRise(2);
			fz.addText(chunk);
			fz.addText(new Phrase(content.get(i) + "\n", fontWithSize));
		}
		fz.go();
	}
}

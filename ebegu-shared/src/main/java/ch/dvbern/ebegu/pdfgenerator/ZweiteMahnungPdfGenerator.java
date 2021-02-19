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

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.util.Constants;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class ZweiteMahnungPdfGenerator extends MahnungPdfGenerator {

	private static final String ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_1 = "PdfGeneration_ZweiteMahnung_Seite1_Paragraph1";
	private static final String ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_2 = "PdfGeneration_ZweiteMahnung_Seite1_Paragraph2";
	private static final String ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_1 = "PdfGeneration_ZweiteMahnung_Seite2_Paragraph1";
	private static final String ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_2 = "PdfGeneration_ZweiteMahnung_Seite2_Paragraph2";

	private Mahnung ersteMahnung;


	public ZweiteMahnungPdfGenerator(
		@Nonnull Mahnung mahnung,
		@Nonnull Mahnung ersteMahnung,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(mahnung, stammdaten);
		this.ersteMahnung = ersteMahnung;
	}


	@Override
	protected void createSeite1(@Nonnull Document document) {
		document.add(PdfUtil.createParagraph(translate(ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_1, getMahndatum())));
		document.add(PdfUtil.createParagraph(translate(ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_2, getFristdatum())));
	}

	@Override
	protected void createSeite2(@Nonnull Document document, @Nonnull List<Element> seite2Paragraphs) {
		seite2Paragraphs.add(PdfUtil.createParagraph(translate(ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_1)));
		String paragraph2 = translate(
			ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_2,
			gemeindeStammdaten.getTelefonForGesuch(getGesuch()),
			gemeindeStammdaten.getEmailForGesuch(getGesuch())
		);
		seite2Paragraphs.add(PdfUtil.createParagraph(paragraph2));
	}

	@Nonnull
	private String getMahndatum() {
		if (mahnung.getMahnungTyp() == MahnungTyp.ZWEITE_MAHNUNG && ersteMahnung != null && ersteMahnung.getTimestampErstellt() != null) {
			return Constants.DATE_FORMATTER.format(ersteMahnung.getTimestampErstellt());
		}
		return "";
	}
}

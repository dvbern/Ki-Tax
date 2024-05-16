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

import com.lowagie.text.Chunk;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PDFUtilTest {

	private final String TEST = "TEST";

	@Test
	void testCreateParagraphHtml_with_html_bold_tag() {
		String test = "<b>"+TEST+"</b>";
		Paragraph paragraph = PdfUtil.createParagraphHtml(test);
		assertNotNull(paragraph);
		assertEquals(TEST, paragraph.getContent().trim());
		assertEquals("OpenSans-SemiBold",
			((Chunk) paragraph.getChunks().get(1)).getFont().getBaseFont().getPostscriptFontName());
	}

	@Test
	void testCreateParagraphHtml_ohne_html_tag() {
		Paragraph paragraph = PdfUtil.createParagraphHtml(TEST);
		assertNotNull(paragraph);
		assertEquals(TEST, paragraph.getContent().trim());
		assertEquals("OpenSans-Light",
			((Chunk) paragraph.getChunks().get(1)).getFont().getBaseFont().getPostscriptFontName());
	}

	@Test
	void testCreateParagraphHtml_unterstuetzt_kein_TextFormattierung() {
		Paragraph paragraph = PdfUtil.createParagraphHtml(TEST + "\n" + TEST);
		assertNotNull(paragraph);
		assertEquals(TEST + " " + TEST, paragraph.getContent().trim());
	}
}

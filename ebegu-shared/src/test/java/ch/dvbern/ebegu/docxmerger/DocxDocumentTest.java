/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.docxmerger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Assert;
import org.junit.Test;

public class DocxDocumentTest {

	public static byte[] createWordTemplate() throws IOException {
		XWPFDocument document = new XWPFDocument();
		XWPFParagraph paragraph1 = document.createParagraph();
		XWPFRun run = paragraph1.createRun();
		run.setText("The first text with a placeholder: {placeholder}");
		run.setFontSize(18);
		XWPFRun run2 = paragraph1.createRun();
		run2.setText("the second text ({placeholder2}) placeholder in brackets");
		XWPFParagraph paragraph2 = document.createParagraph();
		XWPFRun run3 = paragraph2.createRun();
		run3.setText("{placeholder}");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		document.write(out);
		out.close();
		document.close();

		return out.toByteArray();
	}

	@Test
	public void testReplacePlaceholder() throws IOException {
		DocxDocument docxDocument = new DocxDocument(createWordTemplate());
		docxDocument.replacePlaceholder("{placeholder}", "replaced");
		XWPFDocument xwpfDocument = docxDocument.getXwpfDocument();

		Assert.assertEquals("The first text with a placeholder: replaced", xwpfDocument.getParagraphs().get(0).getRuns().get(0).getText(0));
		// not replaced yet
		Assert.assertEquals("the second text ({placeholder2}) placeholder in brackets", xwpfDocument.getParagraphs().get(0).getRuns().get(1).getText(0));
		Assert.assertEquals("replaced", xwpfDocument.getParagraphs().get(1).getRuns().get(0).getText(0));

		docxDocument.replacePlaceholder("{placeholder2}", "replaced");
		Assert.assertEquals("the second text (replaced) placeholder in brackets", xwpfDocument.getParagraphs().get(0).getRuns().get(1).getText(0));
	}

	@Test(expected = EbeguRuntimeException.class)
	public void testPlaceholderNotFound() throws IOException {
		DocxDocument docxDocument = new DocxDocument(createWordTemplate());
		docxDocument.replacePlaceholder("{placeholder3}", "replaced");
	}

}

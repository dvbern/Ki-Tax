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
import java.util.List;

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

		XWPFParagraph paragraph3 = document.createParagraph();
		XWPFRun run4 = paragraph3.createRun();
		run4.setText("{");
		XWPFRun run5 = paragraph3.createRun();
		run5.setText("placeholder}");

		XWPFParagraph paragraph4 = document.createParagraph();
		XWPFRun run6 = paragraph4.createRun();
		run6.setText("{");
		XWPFRun run7 = paragraph4.createRun();
		run7.setText("placeholder} some other text");

		XWPFParagraph paragraph5 = document.createParagraph();
		XWPFRun run8 = paragraph5.createRun();
		run8.setText("abcd {");
		XWPFRun run9 = paragraph5.createRun();
		run9.setText("place");
		XWPFRun run10 = paragraph5.createRun();
		run10.setText("holder} some other text");



		ByteArrayOutputStream out = new ByteArrayOutputStream();
		document.write(out);
		out.close();
		document.close();

		return out.toByteArray();
	}

	@Test
	public void testCreateDocument() throws IOException {
		byte[] initialDocument = createWordTemplate();
		DocxDocument docxDocument = new DocxDocument(initialDocument);
		byte[] outputDocument = docxDocument.getDocument();
		Assert.assertArrayEquals(initialDocument, outputDocument);
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

		/* test following:
		* run1: "{"
		* run2: "placeholder}"
		* */
		List<XWPFRun> paragraph3Runs = xwpfDocument.getParagraphs().get(2).getRuns();
		Assert.assertEquals("replaced", paragraph3Runs.get(0).getText(0));
		Assert.assertEquals("", paragraph3Runs.get(1).getText(0));

		/* test following:
		 * run1: "{"
		 * run2: "placeholder} some other text"
		 * */
		List<XWPFRun> paragraph4Runs = xwpfDocument.getParagraphs().get(3).getRuns();
		Assert.assertEquals("replaced", paragraph4Runs.get(0).getText(0));
		Assert.assertEquals(" some other text", paragraph4Runs.get(1).getText(0));

		/* test following:
		 * run1: "abcd {"
		 * run2: "place"
		 * run3: "holder} some other text"
		 * */
		List<XWPFRun> paragraph5Runs = xwpfDocument.getParagraphs().get(4).getRuns();
		Assert.assertEquals("abcd replaced", paragraph5Runs.get(0).getText(0));
		Assert.assertEquals("", paragraph5Runs.get(1).getText(0));
		Assert.assertEquals(" some other text", paragraph5Runs.get(2).getText(0));
	}

	@Test(expected = EbeguRuntimeException.class)
	public void testPlaceholderNotFound() throws IOException {
		DocxDocument docxDocument = new DocxDocument(createWordTemplate());
		docxDocument.replacePlaceholder("{placeholder3}", "replaced");
	}

}

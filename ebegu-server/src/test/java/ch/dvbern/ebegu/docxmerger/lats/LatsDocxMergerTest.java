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

package ch.dvbern.ebegu.docxmerger.lats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import ch.dvbern.ebegu.docxmerger.DocxDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Assert;
import org.junit.Test;

public class LatsDocxMergerTest {

	public static byte[] createWordTemplate() throws IOException {
		XWPFDocument document = new XWPFDocument();
		XWPFParagraph paragraph1 = document.createParagraph();
		XWPFRun run = paragraph1.createRun();
		run.setText("The first text with a placeholder: {gemeindeName}");
		run.setFontSize(18);
		XWPFRun run2 = paragraph1.createRun();
		run2.setText("the second text ({geleisteteBetreuungsstunden}) placeholder in brackets");
		XWPFParagraph paragraph2 = document.createParagraph();
		XWPFRun run3 = paragraph2.createRun();
		run3.setText("{gemeindeName}");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		document.write(out);
		out.close();
		document.close();

		return out.toByteArray();
	}

	@Test
	public void testMerge() throws IOException {
		DocxDocument docxDocument = new DocxDocument(createWordTemplate());
		LatsDocxMerger merger = new LatsDocxMerger(docxDocument);

		LatsDocxDTO dto = new LatsDocxDTO("London", new BigDecimal("123.456"));
		merger.addMergeFields(dto);

		merger.merge();

		XWPFDocument xwpfDocument = docxDocument.getXwpfDocument();

		Assert.assertEquals("The first text with a placeholder: London", xwpfDocument.getParagraphs().get(0).getRuns().get(0).getText(0));
		Assert.assertEquals("the second text (123.46) placeholder in brackets", xwpfDocument.getParagraphs().get(0).getRuns().get(1).getText(0));
		Assert.assertEquals("London", xwpfDocument.getParagraphs().get(1).getRuns().get(0).getText(0));
	}

}

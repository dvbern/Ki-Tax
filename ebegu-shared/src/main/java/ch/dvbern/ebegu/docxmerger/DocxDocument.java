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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class DocxDocument {
	@Nonnull private byte[] template;
	private XWPFDocument xwpfDocument;

	public DocxDocument(@Nonnull byte[] template) {
		this.template = template;
		this.createDocument();
	}

	private void createDocument() {
		try {
			this.xwpfDocument = new XWPFDocument(new ByteArrayInputStream(template));
		} catch (IOException e) {
			throw new EbeguRuntimeException("createDocument", "could not create document", e);
		}
	}

	public void replacePlaceholder(String placeholder, String replacement) {
		boolean found = false;
		for (XWPFParagraph paragraph : xwpfDocument.getParagraphs()) {
			for (XWPFRun run : paragraph.getRuns()) {
				if (run.getText(0) != null) {
					String text = run.getText(0);
					String replaced = text.replace(placeholder, replacement);
					run.setText(replaced, 0);
					if (!text.equals(replaced)) {
						found = true;
					}
				}
			}
		}
		if (!found) {
			throw new EbeguRuntimeException("replacePlaceholder", "placeholder not found in text: " + placeholder);
		}
	}

	public byte[] getDocument() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			xwpfDocument.write(out);
			out.close();
			xwpfDocument.close();
		} catch (IOException e) {
			throw new EbeguRuntimeException("getDocument", "Error while converting xwpfDocument to byte array", e);
		}
		return out.toByteArray();
	}
}

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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class DocxDocument {
	@Nonnull private final byte[] template;
	private XWPFDocument xwpfDocument;

	public DocxDocument(@Nonnull byte[] template) {
		this.template = Arrays.copyOf(template, template.length);
		this.createDocument();
	}

	private void createDocument() {
		try {
			this.xwpfDocument = new XWPFDocument(new ByteArrayInputStream(template));
		} catch (IOException e) {
			throw new EbeguRuntimeException("createDocument", "could not create document", e);
		}
	}

	public void replacePlaceholder(@Nonnull String placeholder, @Nonnull String replacement) {
		boolean replacedInParagraphs = replaceInParagraphs(placeholder, replacement);
		boolean replacedInTables = replaceInTables(placeholder, replacement);
		if (!(replacedInParagraphs || replacedInTables)) {
			throw new EbeguRuntimeException(
				"replacePlaceholder",
				"placeholder not found in text: " + placeholder,
				ErrorCodeEnum.ERROR_VERFUEGUNG_PLACEHOLDER_NOT_FOUND,
				placeholder
			);
		}
	}

	private boolean replaceInParagraphs(String placeholder, String replacement) {
		boolean found = false;
		for (XWPFParagraph paragraph : xwpfDocument.getParagraphs()) {
			boolean foundInThisRun = replaceInRuns(placeholder, replacement, paragraph.getRuns());
			found = foundInThisRun || found;
		}
		return found;
	}

	private boolean replaceInTables(@Nonnull String placeholder, @Nonnull String replacement) {
		boolean replaced = false;
		for (XWPFTable tbl : xwpfDocument.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						boolean replacedInThisParagraph = replaceInRuns(placeholder, replacement, p.getRuns());
						replaced = replacedInThisParagraph || replaced;
					}
				}
			}
		}
		return replaced;
	}

	/**
	 * A word document has the following structure:
	 *
	 * * Paragraphs
	 * * * Texts
	 * * * * Runs
	 *
	 * Runs are like span Objects and separate text parts with different styles.
	 * It's possible that the placeholder is separated in different runs, e.g. like this: run1: '{', run2: 'placeholder', run3: '}'.
	 * To address this problem, this algorithm was created
	 */
	private boolean replaceInRuns(String placeholder, String replacement, List<XWPFRun> runs) {
		// concat all runs to one text to check, if placeholder exists in these runs
		StringBuilder paragraphText = new StringBuilder();
		for (XWPFRun run : runs) {
			if (run.getText(0) != null) {
				paragraphText.append(run.getText(0));
			}
		}
		String paragraphStr = paragraphText.toString();
		int placeholderIndex = paragraphStr.indexOf(placeholder);
		// placeholder does not exist
		if (placeholderIndex == -1) {
			return false;
		}
		int currStringIndex = 0;
		for (int i = 0; i < runs.size(); i++) {
			XWPFRun run = runs.get(i);
			if (run.getText(0) == null) {
				continue;
			}
			String textOfRun = run.getText(0);
			// store initial run text length
			int textOfRunLength = textOfRun.length();
			// check if placeholder starts in current run
			if (currStringIndex + textOfRunLength >= placeholderIndex) {
				// starting point could be at index 0 or somewhere in the middle
				String textUntilPlaceholder = textOfRun.substring(0, placeholderIndex - currStringIndex);
				// end point of placeholder can be in this run, but it does not have to be
				int placeholderEndIndex = placeholderIndex - currStringIndex + placeholder.length();
				int restOfPlaceholderSize =  placeholderEndIndex - textOfRun.length();
				String textAfterPlaceholder = textOfRun.substring(Math.min(
					placeholderEndIndex,
					textOfRun.length()
				));
				String replaced = textUntilPlaceholder + replacement + textAfterPlaceholder;
				run.setText(replaced, 0);

				removeRestOfPlaceholderFromFollowingRuns(runs, i, restOfPlaceholderSize);

				// recursive check for other placeholders of same type in this runs
				replaceInRuns(placeholder, replacement, runs);
				return true;
			}
			currStringIndex += textOfRunLength;
		}
		return false;
	}

	private void removeRestOfPlaceholderFromFollowingRuns(List<XWPFRun> runs, int i, int restOfPlaceholderSize) {
		for (int j = i + 1; j < runs.size(); j++) {
			if (restOfPlaceholderSize <= 0) {
				break;
			}
			String runText = runs.get(j).getText(0);
			int placeholderPartInThisRun = Math.min(restOfPlaceholderSize, runText.length());
			runText = runText.substring(placeholderPartInThisRun);
			restOfPlaceholderSize -= placeholderPartInThisRun;
			runs.get(j).setText(runText, 0);
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

	public XWPFDocument getXwpfDocument() {
		return xwpfDocument;
	}
}

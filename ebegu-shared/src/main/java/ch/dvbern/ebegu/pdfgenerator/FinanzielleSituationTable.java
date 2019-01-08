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

import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.*;

public class FinanzielleSituationTable {

	private static final Logger LOG = LoggerFactory.getLogger(FinanzielleSituationTable.class);

	private static final String ORIGINAL_VALUE = "PdfGeneration_OriginalValue";

	private float[] columnWidths;
	private int[] alignement;
	private int numberOfTitleRows = 1;
	private boolean lastLineBold = false;
	private List<FinanzielleSituationRow> rows = new ArrayList<>();

	private boolean hasSecondGesuchsteller;

	public FinanzielleSituationTable(boolean hasSecondGesuchsteller, boolean lastLineBold) {
		this(hasSecondGesuchsteller);
		this.lastLineBold = lastLineBold;
	}

	public FinanzielleSituationTable(boolean hasSecondGesuchsteller) {
		this.hasSecondGesuchsteller = hasSecondGesuchsteller;
		final float[] width1Gs = {14,4};
		final float[] width2Gs = {10,4,4};
		final int[] alignement1Gs = { Element.ALIGN_LEFT,Element.ALIGN_RIGHT};
		final int[] alignement2Gs = {Element.ALIGN_LEFT,Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};
		if (this.hasSecondGesuchsteller) {
			this.columnWidths =  width2Gs;
			this.alignement = alignement2Gs;
		} else {
			this.columnWidths = width1Gs;
			this.alignement = alignement1Gs;
		}
	}


	public void addRow(@Nonnull FinanzielleSituationRow row) {
		this.rows.add(row);
	}

	@Nonnull
	public PdfPTable createTable() {
		int numberOfColumns = columnWidths.length;
		PdfPTable table = new PdfPTable(numberOfColumns);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage());
		}
		table.setWidthPercentage(FULL_WIDTH);
		table.setHeaderRows(numberOfTitleRows);
		for (int i = 0; i < rows.size(); i++) {
			boolean isHeader = i < numberOfTitleRows;
			boolean isFooter = lastLineBold && i == rows.size() - 1;
			Color bgColor = isHeader ? Color.LIGHT_GRAY : Color.WHITE;
			Font font = isFooter ? DEFAULT_FONT_BOLD : DEFAULT_FONT;

			FinanzielleSituationRow row = rows.get(i);
			addRow(table, row, font, bgColor);
		}
		return table;
	}

	private void addRow(@Nonnull PdfPTable table, @Nonnull FinanzielleSituationRow row, @Nonnull Font font, @Nonnull Color bgColor) {
		addCell(table, row.getLabel(), row.getSupertext(), null, font, bgColor, alignement[0]);
		addCell(table, row.getGs1(), null, row.getGs1ValueOfGS(), font, bgColor, alignement[1]);
		if (hasSecondGesuchsteller) {
			addCell(table, row.getGs2(), null, row.getGs2ValueOfGS(), font, bgColor, alignement[2]);
		}
	}

	private void addCell(@Nonnull PdfPTable table, @Nullable String value, @Nullable String supertext, @Nullable String originalValue, @Nonnull Font font, @Nonnull Color bgColor, int alignment) {
		final Phrase phrase = new Phrase(value, font);
		if (supertext != null) {
			phrase.add(PdfUtil.createSuperTextInText(supertext));
		}
		if (originalValue != null) {
			Font fontWithSize = PdfUtilities.createFontWithSize(6);
			fontWithSize.setColor(Color.GRAY);
			phrase.add(
				new Chunk(
					'\n' + ServerMessageUtil.getMessage(ORIGINAL_VALUE) + ' ' + originalValue,
					fontWithSize
				)
			);
		}
		PdfPCell cell = new PdfPCell(phrase);
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(alignment);
		cell.setLeading(0.0F, PdfUtil.DEFAULT_CELL_LEADING);
		table.addCell(cell);
	}
}

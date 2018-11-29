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

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_BOLD;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;

public class FinSitTable {

	private static final Logger LOG = LoggerFactory.getLogger(FinSitTable.class);

	private float[] columnWidths;
	private int[] alignement;
	private int numberOfTitleRows = 1;
	private boolean lastLineBold = false;
	private int emptyLinesAfter = 1;
	private List<FinSitRow> rows = new ArrayList<>();

	private boolean hasSecondGesuchsteller;


	public FinSitTable(boolean hasSecondGesuchsteller) {
		this.hasSecondGesuchsteller = hasSecondGesuchsteller;
		final float[] width1Gs = {12,2};
		final float[] width2Gs = {10,2,2};
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


	public void addRow(@Nonnull FinSitRow row) {
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
			boolean isFooter = lastLineBold && i == numberOfColumns - 1;
			Color bgColor = isHeader ? Color.LIGHT_GRAY : Color.WHITE;
			Font font = isFooter ? DEFAULT_FONT_BOLD : DEFAULT_FONT;

			FinSitRow row = rows.get(i);
			addRow(table, row, font, bgColor);
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * emptyLinesAfter);
		return table;
	}

	private void addRow(@Nonnull PdfPTable table, @Nonnull FinSitRow row, @Nonnull Font font, @Nonnull Color bgColor) {
		addCell(table, row.getLabel(), font, bgColor, alignement[0]);
		addCell(table, row.getGs1(), font, bgColor, alignement[1]);
		if (hasSecondGesuchsteller) {
			addCell(table, row.getGs2(), font, bgColor, alignement[2]);
		}
	}

	private void addCell(@Nonnull PdfPTable table, @Nullable String value, @Nonnull Font font, @Nonnull Color bgColor, int alignment) {
		PdfPCell cell = new PdfPCell(new Phrase(value, font));
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(alignment);
		cell.setLeading(0.0F, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.addCell(cell);
	}

	static class FinSitRow {

		@Nonnull
		private String label;

		@Nonnull
		private String gs1;

		@Nullable
		private String gs2;


		public FinSitRow(@Nonnull String label, @Nonnull String gs1) {
			this.label = label;
			this.gs1 = gs1;
		}

		public FinSitRow(@Nonnull String label, @Nullable BigDecimal gs1) {
			this.label = label;
			this.gs1 = PdfUtil.printBigDecimal(gs1);
		}

		@Nonnull
		public String getLabel() {
			return label;
		}

		@Nonnull
		public String getGs1() {
			return gs1;
		}

		@Nullable
		public String getGs2() {
			return gs2;
		}

		public void setGs2(@Nullable String gs2) {
			this.gs2 = gs2;
		}

		public void setGs1(@Nullable BigDecimal gs1) {
			this.gs1 = PdfUtil.printBigDecimal(gs1);
		}

		public void setGs2(@Nullable BigDecimal gs2) {
			this.gs2 = PdfUtil.printBigDecimal(gs2);
		}
	}
}

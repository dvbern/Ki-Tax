/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinanzielleSituationTable {

	private static final Logger LOG = LoggerFactory.getLogger(FinanzielleSituationTable.class);

	private final float[] columnWidths;
	private final int[] alignment;
	private final List<FinanzielleSituationRow> rows = new ArrayList<>();
	private final PageConfiguration pageConfiguration;

	private final boolean hasSecondGesuchsteller;
	private final boolean isKorrekturmodusGemeinde;

	public FinanzielleSituationTable(
		PageConfiguration pageConfiguration,
		boolean hasSecondGesuchsteller,
		boolean isKorrekturmodusGemeinde
	) {
		this.pageConfiguration = pageConfiguration;
		this.hasSecondGesuchsteller = hasSecondGesuchsteller;
		this.isKorrekturmodusGemeinde = isKorrekturmodusGemeinde;
		if (this.hasSecondGesuchsteller) {
			this.columnWidths = new float[] { 10, 4, 4 };
			this.alignment = new int[] { Element.ALIGN_LEFT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT };
		} else {
			this.columnWidths = new float[] { 14, 4 };
			this.alignment = new int[] { Element.ALIGN_LEFT, Element.ALIGN_RIGHT };
		}
	}

	public void addRow(@Nonnull FinanzielleSituationRow row) {
		this.rows.add(row);
	}

	@CanIgnoreReturnValue
	public FinanzielleSituationTable addRows(@Nonnull FinanzielleSituationRow... rows) {
		Arrays.stream(rows)
			.filter(Objects::nonNull)
			.forEach(this::addRow);

		return this;
	}

	@Nonnull
	public PdfPTable createTable() {
		int numberOfColumns = columnWidths.length;
		PdfPTable table = new PdfPTable(numberOfColumns);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage(), e);
		}
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		int numberOfTitleRows = 1;
		table.setHeaderRows(numberOfTitleRows);
		for (int i = 0; i < rows.size(); i++) {
			boolean isHeader = i < numberOfTitleRows;
			Color bgColor = isHeader ? Color.LIGHT_GRAY : Color.WHITE;

			addRow(table, rows.get(i), bgColor);
		}
		return table;
	}

	private void addRow(
		@Nonnull PdfPTable table,
		@Nonnull FinanzielleSituationRow row,
		@Nonnull Color bgColor
	) {
		Font font = row.isBold() ? pageConfiguration.getFonts().getFontBold() : pageConfiguration.getFonts().getFont();
		addCell(table, row.getLabel(), row.getSupertext(), null, font, bgColor, alignment[0]);
		addCell(table, row.getGs1(), null, row.getGs1Urspruenglich(), font, bgColor, alignment[1]);
		if (hasSecondGesuchsteller) {
			addCell(table, row.getGs2(), null, row.getGs2Urspruenglich(), font, bgColor, alignment[2]);
		}
	}

	private void addCell(
		@Nonnull PdfPTable table,
		@Nullable String value,
		@Nullable String supertext,
		@Nullable String originalValue,
		@Nonnull Font font,
		@Nonnull Color bgColor,
		int alignment
	) {
		Phrase phrase = new Phrase(value, font);
		if (supertext != null) {
			phrase.add(PdfUtil.createSuperTextInText(supertext));
		}
		if (originalValue != null && isKorrekturmodusGemeinde) {
			Font fontWithSize = PdfUtil.createFontWithSize(pageConfiguration.getFonts().getFont(), 6);
			fontWithSize.setColor(Color.GRAY);
			phrase.add(new Chunk(originalValue, fontWithSize));
		}
		PdfPCell cell = new PdfPCell(phrase);
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(alignment);
		cell.setLeading(0.0F, PdfUtil.DEFAULT_CELL_LEADING);
		table.addCell(cell);
	}
}

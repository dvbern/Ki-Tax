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

package ch.dvbern.ebegu.pdfgenerator.verfuegung;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

@RequiredArgsConstructor
public class VerfuegungTable {

	final List<VerfuegungTableColumnGroup> groups = new ArrayList<>();
	final List<VerfuegungZeitabschnitt> zeitabschnitte;
	final PageConfiguration pageConfiguration;
	final boolean hasRomanNumberTitel;

	private Font fontTabelle = null;
	private Font fontTabelleBold = null;


	private static final Logger LOG = LoggerFactory.getLogger(VerfuegungTable.class);

	VerfuegungTable add(VerfuegungTableColumn column) {
		this.groups.add(
			VerfuegungTableColumnGroup.builder()
				.title("")
				.columns(List.of(column))
				.build()
		);
		return this;
	}

	VerfuegungTable add(VerfuegungTableColumnGroup group) {
		this.groups.add(group);
		return this;
	}

	private float[] calculateVerfuegungColumnWidths() {
		return ArrayUtils.toPrimitive(getColumnsOfGroups().stream().map(column -> column.width).toArray(Float[]::new));
	}

	@Nonnull
	private List<VerfuegungTableColumn> getColumnsOfGroups() {
		return groups.stream().flatMap(g -> g.columns.stream()).collect(Collectors.toList());
	}

	public PdfPTable build() {
		PdfPTable table = new PdfPTable(getColumnsOfGroups().size());
		try {
			table.setWidths(calculateVerfuegungColumnWidths());
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage(), e);
		}
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * getFontTabelle().getSize() * 2);

		if (hasRomanNumberTitel && hasAnyRomanNumber()) {
			buildRomanNumberRow(table);
		}
		buildFirstHeaderRow(table);
		buildSecondHeaderRow(table);
		buildContent(table);

		return table;
	}

	private boolean hasAnyRomanNumber() {
		return getColumnsOfGroups().stream().anyMatch(col -> !Objects.equals(col.romanNumber, ""));

	}

	private void buildContent(PdfPTable table) {
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			for (VerfuegungTableColumnGroup group : groups) {
				for (VerfuegungTableColumn column : group.columns) {
					table.addCell(createCell(
						column.contentAlignment,
						column.dataExtractor.apply(verfuegungZeitabschnitt),
						column.bgColor,
						column.boldContent ? getBoldFontTabelle() : getFontTabelle()
					));
				}
			}
		}
	}

	private Font getFontTabelle() {
		if (fontTabelle == null) {
			fontTabelle = PdfUtil.createFontWithSize(pageConfiguration.getFonts().getFont(), 8.0f);
		}
		return fontTabelle;
	}

	private Font getBoldFontTabelle() {
		if (fontTabelleBold == null) {
			fontTabelleBold = PdfUtil.createFontWithSize(pageConfiguration.getFonts().getFontBold(), 8.0f);
		}
		return fontTabelleBold;
	}

	private void buildRomanNumberRow(PdfPTable table) {
		for (VerfuegungTableColumnGroup group : groups) {
			for (VerfuegungTableColumn column : group.columns) {
				table.addCell(createHeaderCell(
					Element.ALIGN_CENTER,
					column.romanNumber,
					column.bgColor,
					getFontTabelle(),
					1,
					1));
			}
		}
	}

	private void buildFirstHeaderRow(PdfPTable table) {
		for (VerfuegungTableColumnGroup group : groups) {
			if (group.columns.size() == 1) {
				var column = group.columns.get(0);
				table.addCell(createHeaderCell(
					column.headerAlignment,
					column.title,
					column.bgColor,
					getFontTabelle(),
					1,
					2)
				);
			} else {
				table.addCell(createHeaderCell(
					group.headerAlignment,
					group.title,
					group.bgColor,
					getFontTabelle(),
					group.columns.size(),
					1
				));
			}
		}
	}


	private void buildSecondHeaderRow(PdfPTable table) {
		for (VerfuegungTableColumnGroup group : groups) {
			if (group.columns.size() == 1) {
				continue;
			}
			for (VerfuegungTableColumn column : group.columns) {
					table.addCell(createHeaderCell(
						column.headerAlignment,
						column.title,
						column.bgColor,
						getFontTabelle(),
						1,
						1));
			}
		}
	}

	private PdfPCell createHeaderCell(
		int alignment,
		String value,
		@Nullable Color bgColor,
		@Nullable Font font,
		int colspan,
		int rowspan
	) {
		PdfPCell cell = createDefaultCell(alignment, value, bgColor, font, rowspan, colspan);
		cell.setBorderWidthTop(0.0f);
		return cell;
	}

	private PdfPCell createCell(
		int alignment,
		String value,
		@Nullable Color bgColor,
		@Nullable Font font
	) {
		final PdfPCell cell = createDefaultCell(alignment, value, bgColor, font, 1, 1);
		cell.setBorderWidthTop(0.5f);
		return cell;
	}

	@Nonnull
	private static PdfPCell createDefaultCell(
		int alignment,
		String value,
		@Nullable Color bgColor,
		@Nullable Font font,
		int rowspan,
		int colspan) {
		PdfPCell cell;
		cell = new PdfPCell(new Phrase(value, font));
		cell.setHorizontalAlignment(alignment);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		if (bgColor != null) {
			cell.setBackgroundColor(bgColor);
		}
		cell.setRowspan(rowspan);
		cell.setColspan(colspan);

		cell.setBorderWidthBottom(0.0f);
		cell.setBorderWidthLeft(0.0f);
		cell.setBorderWidthRight(0.0f);

		cell.setLeading(0.0F, PdfUtil.DEFAULT_CELL_LEADING);
		cell.setPadding(0.0f);
		cell.setPaddingTop(2.0f);
		cell.setPaddingBottom(2.0f);
		return cell;
	}
}

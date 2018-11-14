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
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_BOLD;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.NEWLINE;

public final class PdfUtil {

	private static final Pattern DOUBLE_LINE_BREAK = Pattern.compile("\n\n");
	private static final Logger LOG = LoggerFactory.getLogger(PdfUtil.class);

	private PdfUtil() {
		// nop
	}

	public static <T> float getFloat(
		@Nullable T block,
		@Nonnull Function<T, BigDecimal> getter,
		float defaultValue) {

		if (block == null) {
			return defaultValue;
		}

		BigDecimal value = getter.apply(block);

		return value == null ? defaultValue : value.floatValue();
	}

	@Nonnull
	public static PdfPCell createTitleCell(@Nonnull String title) {
		PdfPCell cell = new PdfPCell(new Phrase(title, DEFAULT_FONT));
		cell.setBackgroundColor(Color.LIGHT_GRAY);
		return  cell;
	}

	public static Paragraph createTitle(@Nonnull String title) {
		Paragraph paragraph = new Paragraph(title, PdfUtilities.TITLE_FONT);
		paragraph.setLeading(0.0F, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.add(NEWLINE);
		paragraph.setSpacingAfter(PdfUtilities.DEFAULT_FONT_SIZE * 2 * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createSubTitle(@Nonnull String string) {
		Paragraph paragraph = new Paragraph(string, PdfUtilities.DEFAULT_FONT_BOLD);
		paragraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingBefore(1 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingAfter(1 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static PdfPTable createKeepTogetherTable(@Nonnull java.util.List<Element> elements, final int emptyLinesBetween, final int emptyLinesAfter) {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setPaddingBottom(emptyLinesBetween * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		elements.forEach(element->{
			if (element instanceof  List) {
				PdfPCell phraseCell = new PdfPCell();
				phraseCell.setBorder(Rectangle.NO_BORDER);
				phraseCell.addElement(element);
				table.addCell(phraseCell);
			}
			if (element instanceof  PdfPTable) {
				table.addCell((PdfPTable) element);
			}
			if (element instanceof  Paragraph) {
				table.addCell((Paragraph) element);
			}
		});
		table.setSpacingAfter(emptyLinesAfter * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return table;
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string, final int emptyLinesAfter) {
		return createParagraph(string, emptyLinesAfter, PdfUtilities.DEFAULT_FONT);
	}

	@Nonnull
	public static Paragraph createBoldParagraph(@Nonnull String string, final int emptyLinesAfter) {
		return createParagraph(string, emptyLinesAfter, PdfUtilities.DEFAULT_FONT_BOLD);
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string, final int emptyLinesAfter, final Font font) {
		Paragraph paragraph = new Paragraph(string, font);
		paragraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.setSpacingAfter(emptyLinesAfter * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string) {
		return createParagraph(string, 1);
	}

	@Nonnull
	public static com.lowagie.text.List createList(java.util.List<String> list) {
		final com.lowagie.text.List itextList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
		list.forEach(item->itextList.add(createListItem(item)));
		return itextList;
	}

	@Nonnull
	public static PdfPTable creatreIntroTable(@Nonnull final String[][] intro) {
		PdfPTable table = new PdfPTable(2);
		try {
			float[] columnWidths = {1, 4};
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
		table.setSpacingBefore(0);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		for (int i = 0; i < intro.length; i++) {
			table.addCell(new Phrase(intro[i][0], DEFAULT_FONT));
			table.addCell(new Phrase(intro[i][1], DEFAULT_FONT));
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * 2);
		return table;
	}

	public static ListItem createListItem(@Nonnull final String string) {
		ListItem listItem = new ListItem(string, PdfUtilities.DEFAULT_FONT);
		return listItem;
	}

	@Nonnull
	public static PdfPTable createTable(final String[][]values, final float[] columnWidths, final int[] alignement, final int emptyLinesAfter) { {
		return createTable(values, columnWidths, alignement,emptyLinesAfter, false);
	}}


	@Nonnull
	public static PdfPTable createTable(final String[][]values, final float[] columnWidths, final int[] alignement, final int emptyLinesAfter , boolean lastLineBold) {
		PdfPTable table = new PdfPTable(values[0].length);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage());
		}
		table.setWidthPercentage(FULL_WIDTH);
		table.setHeaderRows(1);
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < values[i].length; j++) {
				PdfPCell cell;
				if (i == 0) {
					cell = PdfUtil.createTitleCell(values[i][j]);
				} else {
					cell = new PdfPCell(new Phrase(values[i][j], lastLineBold && i == values.length - 1 ? DEFAULT_FONT_BOLD : DEFAULT_FONT));
				}
				cell.setHorizontalAlignment(alignement[j]);
				cell.setLeading(0.0F, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				table.addCell(cell);
			}
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * emptyLinesAfter);
		return table;
	}

}

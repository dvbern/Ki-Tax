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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.NEWLINE;

public final class PdfUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PdfUtil.class);
	private static final String WATERMARK = "PdfGeneration_Watermark";
	private static final float FONT_SIZE_WATERMARK = 40.0f;
	public static final float DEFAULT_CELL_LEADING = 1.0F;

	private PdfUtil() {
		// nop
	}

	@Nonnull
	public static PdfPCell createTitleCell(@Nonnull String title) {
		PdfPCell cell = new PdfPCell(new Phrase(title, DEFAULT_FONT));
		cell.setBackgroundColor(Color.LIGHT_GRAY);
		return cell;
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
	public static PdfPTable createKeepTogetherTable(
		@Nonnull java.util.List<Element> elements,
		final int emptyLinesBetween,
		final int emptyLinesAfter) {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell()
			.setPaddingBottom(emptyLinesBetween
				* PdfUtilities.DEFAULT_FONT_SIZE
				* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		elements.forEach(element -> {
			if (element instanceof List) {
				PdfPCell phraseCell = new PdfPCell();
				phraseCell.setBorder(Rectangle.NO_BORDER);
				phraseCell.addElement(element);
				table.addCell(phraseCell);
			}
			if (element instanceof PdfPTable) {
				table.addCell((PdfPTable) element);
			}
			if (element instanceof Paragraph) {
				table.addCell((Paragraph) element);
			}
		});
		table.setSpacingAfter(emptyLinesAfter
			* PdfUtilities.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
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
		paragraph.setSpacingAfter(emptyLinesAfter
			* PdfUtilities.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return paragraph;
	}

	@Nonnull
	public static Paragraph createParagraph(@Nonnull String string) {
		return createParagraph(string, 1);
	}

	@Nonnull
	public static Paragraph createListInParagraph(java.util.List<String> list) {
		Paragraph paragraph = new Paragraph();
		final List itextList = createList(list);
		paragraph.add(itextList);
		return paragraph;
	}

	@Nonnull
	public static List createList(java.util.List<String> list) {
		final List itextList = new List(List.UNORDERED);
		list.forEach(item -> itextList.add(createListItem(item)));
		return itextList;
	}

	@Nonnull
	public static Paragraph createListInParagraph(java.util.List<String> list, final int emptyLinesAfter) {
		Paragraph paragraph = new Paragraph();
		final List itextList = createList(list);
		paragraph.setSpacingAfter(emptyLinesAfter
			* PdfUtilities.DEFAULT_FONT_SIZE
			* PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		paragraph.add(itextList);
		return paragraph;
	}

	@Nonnull
	public static PdfPTable creatreIntroTable(@Nonnull java.util.List<TableRowLabelValue> entries) {
		PdfPTable table = new PdfPTable(2);
		try {
			float[] columnWidths = { 1, 4 };
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Error while creating intro table: {}", e.getMessage());
		}
		setTableDefaultStyles(table);

		for (TableRowLabelValue entry : entries) {
			table.addCell(new Phrase(entry.getLabel(), DEFAULT_FONT));
			table.addCell(new Phrase(entry.getValue(), DEFAULT_FONT));
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * 2);
		return table;
	}

	public static void setTableDefaultStyles(PdfPTable table) {
		table.setSpacingBefore(0);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
	}

	public static ListItem createListItem(@Nonnull final String string) {
		ListItem listItem = new ListItem(string, PdfUtilities.DEFAULT_FONT);
		return listItem;
	}

	@Nonnull
	public static PdfPTable createTable(
		java.util.List<String[]> values,
		final float[] columnWidths,
		final int[] alignement,
		final int emptyLinesAfter) {
		PdfPTable table = new PdfPTable(columnWidths.length);
		try {
			table.setWidths(columnWidths);
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage());
		}
		table.setWidthPercentage(FULL_WIDTH);
		table.setHeaderRows(1);
		boolean first = true;
		for (String[] value : values) {
			for (int j = 0; j < value.length; j++) {
				PdfPCell cell;
				if (first) {
					cell = PdfUtil.createTitleCell(value[j]);
				} else {
					cell = new PdfPCell(new Phrase(value[j], DEFAULT_FONT));
				}
				cell.setHorizontalAlignment(alignement[j]);
				cell.setLeading(0.0F, DEFAULT_CELL_LEADING);
				table.addCell(cell);
			}
			first = false;
		}
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * emptyLinesAfter);
		return table;
	}

	@Nonnull
	public static String printString(@Nullable String stringOrNull) {
		if (stringOrNull != null) {
			return stringOrNull;
		}
		return "";
	}

	@Nonnull
	public static String printBigDecimal(@Nullable BigDecimal valueAsBigDecimal) {
		if (valueAsBigDecimal != null) {
			return Constants.CURRENCY_FORMAT.format(valueAsBigDecimal);
		}
		return "";
	}

	@Nonnull
	public static String printBigDecimalOneNachkomma(@Nullable BigDecimal valueAsBigDecimal) {
		if (valueAsBigDecimal != null) {
			return MathUtil.EINE_NACHKOMMASTELLE.from(valueAsBigDecimal).toString();
		}
		return "";
	}

	@Nonnull
	public static String printLocalDate(@Nullable LocalDate dateValue) {
		if (dateValue != null) {
			return Constants.DATE_FORMATTER.format(dateValue);
		}
		return "";
	}

	@Nonnull
	public static String printPercent(int percent) {
		return MathUtil.DEFAULT.from(percent) + " %";
	}

	@Nonnull
	public static String printPercent(@Nullable BigDecimal percent) {
		if (percent != null) {
			return percent + "%";
		}
		return "";
	}

	/**
	 * Merge multiple pdf into one pdf
	 *
	 * @param pdfToMergeList of pdf input stream
	 * @param outputStream output file output stream
	 * @param addOddPages when true it creates a blank page after each single document if this has an odd number of
	 * pages. This is useful
	 * when the resulting pdf is printed as thoguh each single document was printed separately
	 */
	public static void doMerge(
		java.util.List<InputStream> pdfToMergeList, OutputStream outputStream, boolean addOddPages
	) throws DocumentException, IOException {

		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();
		PdfContentByte cb = writer.getDirectContent();

		for (InputStream in : pdfToMergeList) {
			PdfReader reader = new PdfReader(in);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				//import the page from source pdf
				PdfImportedPage page = writer.getImportedPage(reader, i);
				//add the page to the destination pdf
				document.setPageSize(page.getBoundingBox());
				document.newPage();
				cb.addTemplate(page, 0, 0);
			}
			if (addOddPages && !MathUtil.isEven(writer.getPageNumber())) {
				document.newPage();
				writer.setPageEmpty(false); // Use this method to make sure a page is added, even if it's empty.
			}
		}
		outputStream.flush();
		document.close();
		outputStream.close();
	}

	/**
	 * Setzt ein Wasserzeichen auf jede Seite des PDF
	 */
	public static byte[] addEntwurfWatermark(byte[] content) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(new ByteArrayInputStream(content));
		ByteArrayOutputStream destOutputStream = new ByteArrayOutputStream();

		PdfStamper stamper = new PdfStamper(reader, destOutputStream);
		stamper.setRotateContents(true); // Im Querformat (Massg. Eink) soll der Text auch gedreht werden!
		// Auf jeder Seite setzen
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			Rectangle pagesize = reader.getPageSizeWithRotation(i);
			PdfContentByte over = stamper.getOverContent(i);
			over.saveState();
			over.setGrayFill(0.5f);
			over.setFontAndSize(PdfUtilities.DEFAULT_FONT_BOLD.getBaseFont(), FONT_SIZE_WATERMARK);
			over.showTextAligned(1, ServerMessageUtil.getMessage(WATERMARK),
				pagesize.getWidth() / 2.0F, pagesize.getHeight() / 2.0F, 45.0F);
			over.restoreState();
		}
		stamper.close();
		reader.close();
		return destOutputStream.toByteArray();
	}

	public static Chunk createSuperTextInText(final  String supertext) {
		final Chunk chunk = new Chunk(supertext, PdfUtilities.createFontWithSize(5));
		chunk.setTextRise(3);
		return chunk;
	}
}

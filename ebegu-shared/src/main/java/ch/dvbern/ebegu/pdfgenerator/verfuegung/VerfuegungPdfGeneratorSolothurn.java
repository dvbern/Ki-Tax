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

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public class VerfuegungPdfGeneratorSolothurn extends AbstractVerfuegungPdfGenerator {

	private static final String TITLE_ERWAEGUNGEN = "PdfGeneration_Titel_Erwaegungen";
	private static final String TITLE_SACHVERHALT = "PdfGeneration_Titel_Sachverhalt";
	private static final String NICHT_EINTRETEN_CONTENT_9 = "PdfGeneration_NichtEintreten_Content_9";

	public VerfuegungPdfGeneratorSolothurn(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration,
		boolean isHoehereBeitraegeConfigured) {
		super(betreuung, stammdaten, art, vefuegungPdfGeneratorKonfiguration, isHoehereBeitraegeConfigured);
	}

	@Nonnull
	@Override
	protected PdfPTable createAlternativSignatureTable() {
		PdfPTable table = new PdfPTable(2);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		PdfPCell titelCell = new PdfPCell(new Phrase(gemeindeStammdaten.getStandardDokTitle(),
			getPageConfiguration().getFonts().getFontBold()));
		titelCell.setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * 2);
		titelCell.setPaddingLeft(0);
		titelCell.setBorder(0);
		table.addCell(titelCell);
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(new Phrase("", defaultFont));
		table.addCell(createCellZeroPaddingLeftAndBorder(gemeindeStammdaten.getStandardDokUnterschriftName(), defaultFont));
		table.addCell(createCellZeroPaddingLeftAndBorder(gemeindeStammdaten.getStandardDokUnterschriftName2(), defaultFont));
		table.addCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftTitel(),
			defaultFont));
		table.addCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftTitel2(),
			defaultFont));
		return table;
	}

	private PdfPCell createCellZeroPaddingLeftAndBorder(@Nullable String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setPaddingLeft(0);
		cell.setBorder(0);
		return cell;
	}

	@Override
	protected void createDokumentNichtEintretten(
		@Nonnull Document document,
		@Nonnull PdfGenerator generator) {

		createFusszeileNichtEintreten(generator);
		document.add(createAnrede());
		document.add(createNichtEingetretenParagraph1());

		document.add(createParagraphTitle(translate(TITLE_SACHVERHALT)));
		document.add(createAntragEingereichtAmParagraph());
		document.add(createNichtEintretenUnterlagenUnvollstaendigParagraph());

		document.add(createParagraphTitle(translate(TITLE_ERWAEGUNGEN)));
		document.add(createParagraphErwaegungenNichtEintretten());

		document.newPage();

		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_7)));
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_9)));
		document.add(createAntragNichtEintreten());
		addZusatzTextIfAvailable(document);
	}

	@Override
	protected void addTitleGutscheinProStunde(PdfPTable table) {
		//defualt no-op: wird nur in Luzern angezeigt
	}

	@Override
	protected void addValueGutscheinProStunde(
		PdfPTable table,
		@Nullable BigDecimal verguenstigungProZeiteinheit) {
		//defualt no-op: wird nur in Luzern angezeigt
	}


	@Override
	protected float[] getVerfuegungColumnWidths() {
		return DEFAULT_COLUMN_WIDTHS_VERFUEGUNG_TABLE;
	}

	private Element createParagraphErwaegungenNichtEintretten() {
		Paragraph paragraph = PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_4));
		paragraph.add(PdfUtil.createSuperTextInText("1"));
		paragraph.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_5)));
		return paragraph;
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelle;
	}

	private Paragraph createParagraphTitle(String title) {
		return PdfUtil.createBoldParagraph(title, 1);
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfGenerator generator) throws DocumentException {
		createFusszeile(
			generator.getDirectContent(),
			Lists.newArrayList(translate(FUSSZEILE_1_NICHT_EINTRETEN))
		);
	}
}

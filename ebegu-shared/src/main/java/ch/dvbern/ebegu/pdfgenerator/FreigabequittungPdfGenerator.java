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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public class FreigabequittungPdfGenerator extends DokumentAnGemeindeGenerator {

	private static final String FREIGABEQUITTUNG_TITLE = "PdfGeneration_Freigabequittung_Title";
	private static final String GESUCHSTELLER = "PdfGeneration_Gesuchsteller";
	private static final String BETREUUNGSANGEBOTE = "PdfGeneration_Betreuungsangebote";
	private static final String BETREUUNG_KIND = "PdfGeneration_Kind";
	private static final String BENOETIGTE_UNTERLAGEN = "PdfGeneration_BenoetigteUnterlagen";
	private static final String EINWILLIGUNG_STEUERDATEN_TITLE = "PdfGeneration_EinwilligungSteuerdaten_Title";
	private static final String EINWILLIGUNG_STEUERDATEN_CONTENT = "PdfGeneration_EinwilligungSteuerdaten_Content";
	private static final String VOLLSTAENDIGKEIT_TITLE = "PdfGeneration_Vollstaendigkeit_Title";
	private static final String VOLLSTAENDIGKEIT_CONTENT = "PdfGeneration_Vollstaendigkeit_Content";
	private static final String EINGEREICHT = "PdfGeneration_Eingereicht";
	private static final String UNTERSCHRIFTEN_ORT_DATUM = "PdfGeneration_UnterschriftenOrtDatum";

	private static final Logger LOG = LoggerFactory.getLogger(FreigabequittungPdfGenerator.class);

	@Nonnull
	private final List<DokumentGrund> benoetigteUnterlagen;

	public FreigabequittungPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen) {
		super(gesuch, stammdaten);
		this.benoetigteUnterlagen = benoetigteUnterlagen;
	}

	@Override
	@Nonnull
	protected String getDocumentTitle() {
		return translate(FREIGABEQUITTUNG_TITLE, getGesuch().getGesuchsperiode().getGesuchsperiodeString());
	}

	@Override
	@Nonnull
	protected CustomGenerator getCustomGenerator() {
		final List<String> dokumente = KibonPrintUtil.getBenoetigteDokumenteAsList(benoetigteUnterlagen, gesuch,
			sprache);
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			addBarcode(document);
			document.add(createGesuchstellerTable());
			document.add(PdfUtil.createSubTitle(translate(BETREUUNGSANGEBOTE)));
			document.add(createBetreuungsangeboteTable());
			document.add(PdfUtil.createSubTitle(translate(BENOETIGTE_UNTERLAGEN)));
			Paragraph dokumenteParagraph = new Paragraph();
			dokumenteParagraph.setSpacingAfter(1 * DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
			dokumenteParagraph.add(PdfUtil.createListInParagraph(dokumente));
			document.add(dokumenteParagraph);
			List<Element> seite2Paragraphs = Lists.newArrayList();
			seite2Paragraphs.add(PdfUtil.createSubTitle(translate(EINWILLIGUNG_STEUERDATEN_TITLE)));
			seite2Paragraphs.add(PdfUtil.createParagraph(
				translate(EINWILLIGUNG_STEUERDATEN_CONTENT, gesuch.getDossier().getGemeinde().getName())
			));
			seite2Paragraphs.add(new Paragraph());
			seite2Paragraphs.add(PdfUtil.createSubTitle(translate(VOLLSTAENDIGKEIT_TITLE)));
			seite2Paragraphs.add(PdfUtil.createParagraph(translate(VOLLSTAENDIGKEIT_CONTENT)));
			seite2Paragraphs.add(createUnterschriftenTable());
			seite2Paragraphs.add(PdfUtil.createParagraph(translate(EINGEREICHT)));
			document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
		};
	}

	@Nonnull
	public PdfPTable createGesuchstellerTable() {
		PdfPTable table = new PdfPTable(3);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		// Row: Referenznummer
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(new Phrase(translate(REFERENZNUMMER), defaultFont));
		table.addCell(new Phrase(getGesuch().getJahrFallAndGemeindenummer(), defaultFont));
		table.addCell(new Phrase());
		// Row: Gesuchersteller-Adressen
		table.addCell(new Phrase(translate(GESUCHSTELLER), defaultFont));
		String gs1 = KibonPrintUtil.getGesuchstellerWithAddressAsString(getGesuch().getGesuchsteller1());
		String gs2 = KibonPrintUtil.getGesuchstellerWithAddressAsString(getGesuch().getGesuchsteller2());
		table.addCell(new Phrase(gs1, defaultFont));
		table.addCell(new Phrase(gs2, defaultFont));
		return table;
	}

	public void addBarcode(Document document) {
		try {
			DataMatrixBean dataMatrixBean = new DataMatrixBean();
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(
				bytesOut, "image/x-png", 175, BufferedImage.TYPE_BYTE_BINARY, false, 0);
			dataMatrixBean.generateBarcode(canvas,
				"§FREIGABE|OPEN|" + getGesuch().getId() + '|' + getGesuch().getAnzahlGesuchZurueckgezogen() + '§');
			canvas.finish();
			Image image = Image.getInstance(bytesOut.toByteArray());
			image.setAbsolutePosition(document.leftMargin(),
				document.getPageSize().getHeight() - 2 * Utilities.millimetersToPoints(PdfLayoutConfiguration.BARCODE_TOP_IN_MM));
			document.add(image);
		} catch (IOException | DocumentException e) {
			LOG.error("Failed to read the Barcode: {}", e.getMessage(), e);
		}
	}

	@Nonnull
	public PdfPTable createBetreuungsangeboteTable() throws DocumentException {
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 50, 20 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell(translate(BETREUUNG_KIND)));
		table.addCell(PdfUtil.createTitleCell(translate(BETREUUNG_INSTITUTION)));
		table.addCell(PdfUtil.createTitleCell(translate(REFERENZNUMMER)));

		getGesuch().extractAllPlaetze().forEach(platz -> {
			final Font defaultFont = getPageConfiguration().getFonts().getFont();
			table.addCell(new Phrase(platz.getKind().getKindJA().getFullName(), defaultFont));
			table.addCell(new Phrase(platz.getInstitutionAndBetreuungsangebottyp(sprache), defaultFont));
			table.addCell(new Phrase(platz.getBGNummer(), defaultFont));
		});
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		return table;
	}

	@Nonnull
	public PdfPTable createUnterschriftenTable() {
		PdfPTable table = new PdfPTable(2);
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingTop(3 * DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		GesuchstellerContainer gesuchsteller1 = getGesuch().getGesuchsteller1();
		GesuchstellerContainer gesuchsteller2 = getGesuch().getGesuchsteller2();
		if (gesuchsteller1 != null) {
			addGesuchstellerToUnterschriften(table, gesuchsteller1);
		}
		if (gesuchsteller2 != null) {
			addGesuchstellerToUnterschriften(table, gesuchsteller2);
		}
		table.setSpacingAfter(2 * DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return table;
	}

	private void addGesuchstellerToUnterschriften(@Nonnull PdfPTable table,
		@Nonnull GesuchstellerContainer gesuchsteller) {
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(new Phrase(translate(UNTERSCHRIFTEN_ORT_DATUM), defaultFont));
		table.addCell(new Phrase(gesuchsteller.extractFullName(), defaultFont));

		table.addCell(new Phrase());
		table.addCell(new Phrase());

		table.addCell(createCellWithBottomLine());
		table.addCell(createCellWithBottomLine());
	}

	private PdfPCell createCellWithBottomLine() {
		LineSeparator dottedline = new LineSeparator();
		dottedline.setOffset(-10);
		PdfPCell pdfPCell = new PdfPCell();
		pdfPCell.setBorderWidth(0);
		pdfPCell.addElement(dottedline);
		return pdfPCell;
	}
}

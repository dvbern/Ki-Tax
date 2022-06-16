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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

public class VerfuegungPdfGeneratorSolothurn extends AbstractVerfuegungPdfGenerator {

	private static final String TITLE_ERWAEGUNGEN = "PdfGeneration_Titel_Erwaegungen";
	private static final String TITLE_SACHVERHALT = "PdfGeneration_Titel_Sachverhalt";
	private static final String NICHT_EINTRETEN_CONTENT_9 = "PdfGeneration_NichtEintreten_Content_9";

	public VerfuegungPdfGeneratorSolothurn(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte);
	}

	@Override
	protected void addGruesseElements(@Nonnull List<Element> gruesseElements) {
		Paragraph gruss = PdfUtil.createParagraph(translate(GRUSS));
		gruss.add(Chunk.NEWLINE);
		gruss.add(PdfUtil.createBoldParagraph(gemeindeStammdaten.getGemeinde().getName(), 2));

		gruesseElements.add(gruss);
		gruesseElements.add(createParagraphSignatur());
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
	protected void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		//no-op: wird in Solothurn nicht angezeigt
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

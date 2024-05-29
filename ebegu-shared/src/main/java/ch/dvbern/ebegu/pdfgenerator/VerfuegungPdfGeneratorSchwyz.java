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

import java.awt.Color;
import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

public class VerfuegungPdfGeneratorSchwyz extends AbstractVerfuegungPdfGenerator {
	private static final String NICHT_EINTRETEN_CONTENT_9 = "PdfGeneration_NichtEintreten_Content_9";

	private static final float[] COLUMN_WIDTHS = { 90, 100, 88, 88, 88, 100, 110, 110, 110 };

	public VerfuegungPdfGeneratorSchwyz(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte,
		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}

	@Override
	protected String getDocumentTitle() {
		if (art == Art.NICHT_EINTRETTEN) {
			return translate(VERFUEGUNG_NICHT_EINTRETEN_TITLE);
		}
		return translate(VERFUEGUNG_TITLE);
	}

	@Override
	protected void createDokumentNichtEintretten(
		@Nonnull Document document,
		@Nonnull PdfGenerator generator) {

		document.add(createAnrede());
		document.add(createNichtEingetretenParagraph1());

		document.add(createNichtEintretenUnterlagenUnvollstaendigParagraph());
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_4)));

		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_5)));
		document.newPage();
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_9)));
		document.add(createAntragNichtEintreten());
		addZusatzTextIfAvailable(document);
	}

	@Override
	protected Element createNichtEingetretenParagraph1() {
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();

		return PdfUtil.createParagraph(translate(
			NICHT_EINTRETEN_CONTENT_1,
			Constants.DATE_FORMATTER.format(getEingangsdatum()),
			Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
			Constants.DATE_FORMATTER.format(gp.getGueltigBis())));
	}

	@Override
	protected void addTitleGutscheinProStunde(PdfPTable table) {
		//defualt no-op: wird nur in Luzern angezeigt
	}

	@Override
	protected void addTitleBerechneterGutschein(PdfPTable table) {
		//no-op die Spalte soll in Schwyz nicht angezeigt werden
	}

	@Override
	protected void addValueBerechneterGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		//no-op die Spalte soll in Schwyz nicht angezeigt werden
	}

	@Override
	protected void addValueGutscheinProStunde(
		PdfPTable table,
		@Nullable BigDecimal verguenstigungProZeiteinheit) {
		//default no-op: wird nur in Luzern angezeigt
	}

	@Override
	protected float[] getVerfuegungColumnWidths() {
		return COLUMN_WIDTHS;
	}

	@Override
	protected void addReferenzNummerCells(PdfPTable table) {
		table.addCell(createCell(true, Element.ALIGN_CENTER, "", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "I", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "II", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "III", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "IV", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "V", Color.LIGHT_GRAY, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "VI", Color.LIGHT_GRAY, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "VII", Color.LIGHT_GRAY, fontTabelle, 1, 1));
	}

	@Override
	@Nonnull
	protected Paragraph createFirstParagraph(Kind kind) {
		return PdfUtil.createParagraph(translate(
			VERFUEGUNG_CONTENT_1,
			kind.getFullName(),
			Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2);
	}

	@Override
	protected String getRechtsmittelbelehrungContent(@Nonnull GemeindeStammdaten stammdaten) {
		Adresse beschwerdeAdresse = stammdaten.getBeschwerdeAdresse();
		if (beschwerdeAdresse == null) {
			beschwerdeAdresse = stammdaten.getAdresseForGesuch(getGesuch());
		}
		return translate(RECHTSMITTELBELEHRUNG_CONTENT, beschwerdeAdresse.getAddressAsStringInOneLine(), stammdaten.getGemeinde().getName());
	}

	@Override
	protected void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		// no-op: wird nicht in Schwyz verwendet
	}

	@Override
	protected void createFusszeileKeinAnspruch(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		// no-op: wird nicht in Schwyz verwendet
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelle;
	}

	@Override
	protected void addSuperTextForKeinAnspruchAbschnitt(Paragraph paragraph) {
		// wird nicht beim Schwyz angezeigt als keine Fussnotiz
	}
}

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
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

public class VerfuegungPdfGeneratorLuzern extends AbstractVerfuegungPdfGenerator {

	private final float[] COLUMN_WIDTHS_DEFAULT = { 90, 100, 88, 88, 88, 100, 110 };
	private final float[] COLUMN_WIDTHS_TFO = { 90, 100, 88, 88, 88, 100, 110, 110 };

	private static final String GUTSCHEIN_PRO_STUNDE = "PdfGeneration_Verfuegung_GutscheinProStunde";

	private boolean isBetreuungTagesfamilie = false;

	protected static final String VERFUEGUNG_NICHT_EINTRETEN_TITLE = "PdfGeneration_Verfuegung_NichtEintreten_Title";

	public VerfuegungPdfGeneratorLuzern(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte
	) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte);
		isBetreuungTagesfamilie = betreuung.isAngebotTagesfamilien();
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

		createDokumentNichtEintrettenDefault(document, generator);
	}

	@Override
	protected float[] getVerfuegungColumnWidths() {
		return this.isBetreuungTagesfamilie ? COLUMN_WIDTHS_TFO : COLUMN_WIDTHS_DEFAULT;
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelleBold;
	}

	@Override
	protected Font getBgColorForBetreuungsgutscheinCell() {
		return fontTabelle;
	}

	@Override
	protected void addTitleGutscheinProStunde(PdfPTable table) {
		if (isBetreuungTagesfamilie) {
			table.addCell(createCell(
				true,
				Element.ALIGN_RIGHT,
				translate(GUTSCHEIN_PRO_STUNDE),
				Color.LIGHT_GRAY,
				fontTabelle,
				2,
				1));
		}
	}

	@Override
	protected void addReferenzNummerCells(PdfPTable table) {
		//no-op die Zeile mit den RefernzNummern soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addTitleBerechneterGutschein(PdfPTable table) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addTitleBetreuungsGutschein(PdfPTable table) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addTitleNrElternBeitrag(PdfPTable table) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addValueBerechneterGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addValueBetreuungsGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addValueElternBeitrag(PdfPTable table, BigDecimal minimalerElternbeitragGekuerzt) {
		//no-op die Spalte soll in Luzern nicht angezeigt werden
	}

	@Override
	protected void addValueGutscheinProStunde(
		PdfPTable table,
		@Nullable BigDecimal verguenstigungProZeiteinheit) {
		if (this.isBetreuungTagesfamilie) {
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				PdfUtil.printBigDecimal(verguenstigungProZeiteinheit),
				Color.LIGHT_GRAY,
				getBgColorForUeberwiesenerBetragCell(),
				1,
				1));
		}
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> getVerfuegungZeitabschnitt() {
		if (!this.isBetreuungTagesfamilie) {
			return super.getVerfuegungZeitabschnitt();
		}

		//Für TFOs sollen die Zeitabschnitte, welche kein Betreuungspensum haben nicht aus der Liste entfernt werden
		return super.getZeitabschnitteOrderByGueltigAb(false);
	}

	@Override
	protected void createDokumentKeinAnspruchTFO(Document document, PdfGenerator generator) {
		super.createDokumentNormal(document, generator);
	}
}

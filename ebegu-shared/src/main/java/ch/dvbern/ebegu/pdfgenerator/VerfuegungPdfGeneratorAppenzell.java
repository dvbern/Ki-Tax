/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

public class VerfuegungPdfGeneratorAppenzell extends AbstractVerfuegungPdfGenerator {

	private final float[] COLUMN_WIDTHS = { 90, 100, 88, 88, 88, 100, 110, 110, 110 };

	private static final String GUTSCHEIN_PRO_STUNDE = "PdfGeneration_Verfuegung_GutscheinProStunde";

	private boolean isBetreuungTagesfamilie = false;

	protected static final String VERFUEGUNG_NICHT_EINTRETEN_TITLE = "PdfGeneration_Verfuegung_NichtEintreten_Title";
	private static final String BEITRAGSHOHE_PROZENT = "PdfGeneration_Verfuegung_Beitragshoehe_Prozent";
	private static final String SELBSTBEHALT_PROZENT = "PdfGeneration_Verfuegung_Selbstbehalt_Prozent";
	private static final String ZUSATZTEXT_1 = "PdfGeneration_Verfuegung_Zusatztext_AR_1";
	private static final String ZUSATZTEXT_2 = "PdfGeneration_Verfuegung_Zusatztext_AR_2";
	private static final String ZUSATZTEXT_3 = "PdfGeneration_Verfuegung_Zusatztext_AR_3";

	public VerfuegungPdfGeneratorAppenzell(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte,
		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp
	) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
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
		return COLUMN_WIDTHS;
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
	protected void addAngebotToIntro(List<TableRowLabelValue> intro) {
		//no-op, wird in Appenzell nicht angezeigt
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
		//no-op die Zeile mit den RefernzNummern soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addTitleBerechneterGutschein(PdfPTable table) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addTitleBetreuungsGutschein(PdfPTable table) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addTitleNrElternBeitrag(PdfPTable table) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addTitleBeitraghoheUndSelbstbehaltInProzent(PdfPTable table) {
		table.addCell(createCell(
				true,
				Element.ALIGN_RIGHT,
				translate(SELBSTBEHALT_PROZENT),
				Color.LIGHT_GRAY,
				fontTabelle,
				2,
				1));

		table.addCell(createCell(
				true,
				Element.ALIGN_RIGHT,
				translate(BEITRAGSHOHE_PROZENT),
				Color.LIGHT_GRAY,
				fontTabelle,
				2,
				1));
	}

	@Override
	protected void addValueBerechneterGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addValueBetreuungsGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addValueElternBeitrag(PdfPTable table, BigDecimal minimalerElternbeitragGekuerzt) {
		//no-op die Spalte soll in Appenzell nicht angezeigt werden
	}

	@Override
	protected void addValueaBeitraghoheUndSelbstbehaltInProzent(PdfPTable table, Integer beitraghoheInProzent) {
		BigDecimal beitragHoeheGanzzahl = MathUtil.GANZZAHL.from(beitraghoheInProzent);
		BigDecimal selbstbehalt = MathUtil.GANZZAHL.subtract(BigDecimal.valueOf(100), beitragHoeheGanzzahl);

		table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				PdfUtil.printPercent(selbstbehalt),
				Color.LIGHT_GRAY,
				getBgColorForBetreuungsgutscheinCell(),
				1,
				1));
		table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				PdfUtil.printPercent(beitragHoeheGanzzahl),
				Color.LIGHT_GRAY,
				getBgColorForBetreuungsgutscheinCell(),
				1,
				1));
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
		return super.getZeitabschnitteOrderByGueltigAb(false);
	}

	protected void createDokumentKeinAnspruch(Document document, PdfGenerator generator) {
		// bei Appenzell wird auch bei keinem Anspruch die Verfügung generiert.
		super.createDokumentNormal(document, generator);
	}

	@Override
	protected void createDokumentKeinAnspruchTFO(Document document, PdfGenerator generator) {
		super.createDokumentNormal(document, generator);
	}

	@Override
	protected void removeLeadingZeitabschnitteWithNoPositivBetreuungsPensum(List<VerfuegungZeitabschnitt> result) {
		//no-op in Appenzell sollen immer alle Zeitabschnitte angezeigt werden
	}

	@Override
	protected void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws
			DocumentException {
		//no-op: wird in Solothurn nicht angezeigt
	}

	@Override
	protected Paragraph createFirstParagraph(Kind kind) {
		return PdfUtil.createParagraph(translate(
				VERFUEGUNG_CONTENT_1,
				kind.getFullName(),
				Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2);
	}

	@Override
	protected void addZusatzTextIfAvailable(Document document) {
		document.add(PdfUtil.createParagraph(translate(ZUSATZTEXT_1)));
		document.add(PdfUtil.createBoldParagraph(translate(ZUSATZTEXT_2), 1));
		document.add(PdfUtil.createParagraph(translate(ZUSATZTEXT_3), 2));
		super.addZusatzTextIfAvailable(document);
	}
}

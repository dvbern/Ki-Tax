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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public abstract class AbstractVerfuegungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String NAME_KIND = "PdfGeneration_NameKind";
	private static final String BEMERKUNG = "PdfGeneration_Bemerkung";
	private static final String ERSETZT_VERFUEGUNG = "PdfGeneration_Ersetzt_Verfuegung";
	private static final String VERFUEGUNG_TITLE = "PdfGeneration_Verfuegung_Title";
	private static final String ANGEBOT = "PdfGeneration_Betreuungsangebot";
	private static final String GEMEINDE = "PdfGeneration_Gemeinde";
	private static final String VERFUEGUNG_CONTENT_1 = "PdfGeneration_Verfuegung_Content_1";
	private static final String VERFUEGUNG_CONTENT_2 = "PdfGeneration_Verfuegung_Content_2";
	private static final String VERFUEGUNG_ERKLAERUNG_FEBR = "PdfGeneration_Verfuegung_Erklaerung_FEBR";
	private static final String VON = "PdfGeneration_Verfuegung_Von";
	private static final String BIS = "PdfGeneration_Verfuegung_Bis";
	private static final String PENSUM_TITLE = "PdfGeneration_Verfuegung_PensumTitle";
	private static final String PENSUM_TITLE_TFO = "PdfGeneration_Verfuegung_PensumTitleTFO";
	private static final String PENSUM_BETREUUNG = "PdfGeneration_Verfuegung_Betreuungspensum";
	private static final String PENSUM_ANSPRUCH = "PdfGeneration_Verfuegung_Anspruchspensum";
	private static final String PENSUM_BG = "PdfGeneration_Verfuegung_BgPensum";
	private static final String VOLLKOSTEN = "PdfGeneration_Verfuegung_Vollkosten";
	private static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN =
		"PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungVollkosten";
	private static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG =
		"PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungMinimalbeitrag";
	protected static final String GUTSCHEIN_AN_INSTITUTION = "PdfGeneration_Verfuegung_Gutschein_Institution";
	protected static final String GUTSCHEIN_AN_ELTERN = "PdfGeneration_Verfuegung_Gutschein_Eltern";
	private static final String ELTERNBEITRAG = "PdfGeneration_Verfuegung_MinimalerElternbeitrag";
	private static final String KEIN_ANSPRUCH_CONTENT_1 = "PdfGeneration_KeinAnspruch_Content_1";
	private static final String KEIN_ANSPRUCH_CONTENT_2 = "PdfGeneration_KeinAnspruch_Content_2";
	private static final String KEIN_ANSPRUCH_CONTENT_3 = "PdfGeneration_KeinAnspruch_Content_3";
	private static final String KEIN_ANSPRUCH_CONTENT_4 = "PdfGeneration_KeinAnspruch_Content_4";
	private static final String NICHT_EINTRETEN_CONTENT_1 = "PdfGeneration_NichtEintreten_Content_1";
	private static final String NICHT_EINTRETEN_CONTENT_2 = "PdfGeneration_NichtEintreten_Content_2";
	private static final String NICHT_EINTRETEN_CONTENT_3 = "PdfGeneration_NichtEintreten_Content_3";
	protected static final String NICHT_EINTRETEN_CONTENT_4 = "PdfGeneration_NichtEintreten_Content_4";
	protected static final String NICHT_EINTRETEN_CONTENT_5 = "PdfGeneration_NichtEintreten_Content_5";
	private static final String NICHT_EINTRETEN_CONTENT_5_FKJV = "PdfGeneration_NichtEintreten_Content_5_FKJV";
	private static final String NICHT_EINTRETEN_CONTENT_6 = "PdfGeneration_NichtEintreten_Content_6";
	protected static final String NICHT_EINTRETEN_CONTENT_7 = "PdfGeneration_NichtEintreten_Content_7";
	private static final String NICHT_EINTRETEN_CONTENT_8 = "PdfGeneration_NichtEintreten_Content_8";
	private static final String BEMERKUNGEN = "PdfGeneration_Verfuegung_Bemerkungen";
	private static final String RECHTSMITTELBELEHRUNG_TITLE = "PdfGeneration_Rechtsmittelbelehrung_Title";
	private static final String RECHTSMITTELBELEHRUNG_CONTENT = "PdfGeneration_Rechtsmittelbelehrung_Content";
	protected static final String FUSSZEILE_1_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile1";
	private static final String FUSSZEILE_2_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile2";
	private static final String FUSSZEILE_2_NICHT_EINTRETEN_FKJV = "PdfGeneration_NichtEintreten_Fusszeile2_FKJV";
	private static final String FUSSZEILE_1_VERFUEGUNG = "PdfGeneration_Verfuegung_Fusszeile1";
	private static final String FUSSZEILE_1_VERFUEGUNG_FKJV = "PdfGeneration_Verfuegung_Fusszeile1_FKJV";
	private static final String VERWEIS_KONTINGENTIERUNG = "PdfGeneration_Verweis_Kontingentierung";
	public static final String UNKNOWN_INSTITUTION_NAME = "?";

	private static final Logger LOG = LoggerFactory.getLogger(AbstractVerfuegungPdfGenerator.class);

	protected final Font fontTabelle = PdfUtil.createFontWithSize(getPageConfiguration().getFonts().getFont(), 8.0f);
	protected final Font fontTabelleBold = PdfUtil.createFontWithSize(getPageConfiguration().getFonts().getFontBold(), 8.0f);
	private final Font fontRed = PdfUtil.createFontWithColor(getPageConfiguration().getFonts().getFont(), Color.RED);

	protected final float[] DEFAULT_COLUMN_WIDTHS_VERFUEGUNG_TABLE = { 90, 100, 88, 88, 88, 100, 100, 100, 108, 110 };

	public enum Art {
		NORMAL,
		KEIN_ANSPRUCH,
		NICHT_EINTRETTEN
	}

	protected final Betreuung betreuung;
	private final boolean kontingentierungEnabledAndEntwurf;
	private final boolean stadtBernAsivConfigured;
	private final boolean isFKJVTexte;

	@Nonnull
	private final Art art;

	public AbstractVerfuegungPdfGenerator(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte
	) {
		super(betreuung.extractGesuch(), stammdaten);

		this.betreuung = betreuung;
		this.art = art;
		this.kontingentierungEnabledAndEntwurf = kontingentierungEnabledAndEntwurf;
		this.stadtBernAsivConfigured = stadtBernAsivConfigured;
		this.isFKJVTexte = isFKJVTexte;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(VERFUEGUNG_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createIntroAndInfoKontingentierung());
			createContent(document, generator);
		};
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	public void createContent(
		@Nonnull final Document document,
		@Nonnull PdfGenerator generator) throws DocumentException {

		Kind kind = betreuung.getKind().getKindJA();
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();
		LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
		Paragraph paragraphWithSupertext;
		switch (art) {
		case NORMAL:
			createFusszeileNormaleVerfuegung(generator.getDirectContent());
			paragraphWithSupertext = PdfUtil.createParagraph(translate(
				VERFUEGUNG_CONTENT_1,
				kind.getFullName(),
				Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2);
			paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
			paragraphWithSupertext.add(new Chunk(' ' + translate(VERFUEGUNG_CONTENT_2)));
			document.add(paragraphWithSupertext);
			document.add(createVerfuegungTable());

			// Erklaerungstext zu FEBR: Falls Stadt Bern und das Flag ist noch nicht gesetzt
			if (!stadtBernAsivConfigured && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeindeStammdaten.getGemeinde())) {
				document.add(createErklaerungstextFEBR());
			}

			addBemerkungenIfAvailable(document);
			addZusatzTextIfAvailable(document);
			break;
		case KEIN_ANSPRUCH:
			createFusszeileKeinAnspruch(generator.getDirectContent());
			document.add(PdfUtil.createParagraph(translate(
				KEIN_ANSPRUCH_CONTENT_1,
				Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
				Constants.DATE_FORMATTER.format(gp.getGueltigBis()),
				kind.getFullName(),
				betreuung.getInstitutionStammdaten().getInstitution().getName(),
				betreuung.getBGNummer())));
			document.add(PdfUtil.createParagraph(translate(
				KEIN_ANSPRUCH_CONTENT_2,
				Constants.DATE_FORMATTER.format(eingangsdatum))));
			addBemerkungenIfAvailable(document, false);
			addZusatzTextIfAvailable(document);
			paragraphWithSupertext = PdfUtil.createParagraph(translate(
				KEIN_ANSPRUCH_CONTENT_3,
				kind.getFullName(),
				Constants.DATE_FORMATTER.format(kind.getGeburtsdatum()),
				Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
				Constants.DATE_FORMATTER.format(gp.getGueltigBis())), 2);
			paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
			paragraphWithSupertext.add(new Chunk(' ' + translate(KEIN_ANSPRUCH_CONTENT_4)));
			document.add(paragraphWithSupertext);
			break;
		case NICHT_EINTRETTEN:
			createDokumentNichtEintretten(document, generator);
			break;
		}
		List<Element> gruesseElements = Lists.newArrayList();
		addGruesseElements(gruesseElements);
		document.add(PdfUtil.createKeepTogetherTable(gruesseElements, 2, 0));
		document.add(createRechtsmittelBelehrung());
	}

	protected abstract void addGruesseElements(@Nonnull List<Element> gruesseElements);

	protected abstract void createDokumentNichtEintretten(@Nonnull final Document document, @Nonnull PdfGenerator generator);


	protected void createDokumentNichtEintrettenDefault(@Nonnull final Document document, @Nonnull PdfGenerator generator) {
		createFusszeileNichtEintreten(generator.getDirectContent());
		document.add(createNichtEingetretenParagraph1());
		document.add(createAntragEingereichtAmParagraph());
		document.add(createNichtEintretenUnterlagenUnvollstaendigParagraph());

		Paragraph paragraphWithSupertext;
		paragraphWithSupertext = PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_4));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
		paragraphWithSupertext.add(new Chunk(getContent5NichtEintreten()));
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("2"));
		paragraphWithSupertext.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_6)));
		document.add(paragraphWithSupertext);

		document.newPage();
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_7)));
		document.add(createAntragNichtEintreten());
		addZusatzTextIfAvailable(document);
	}

	protected Element createAntragNichtEintreten() {
		LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
		return PdfUtil.createBoldParagraph(translate(
			NICHT_EINTRETEN_CONTENT_8,
			Constants.DATE_FORMATTER.format(eingangsdatum)), 2);
	}

	protected Element createNichtEintretenUnterlagenUnvollstaendigParagraph() {
		return PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_3));
	}

	protected Element createAntragEingereichtAmParagraph() {
		LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();

		return PdfUtil.createParagraph(translate(
			NICHT_EINTRETEN_CONTENT_2,
			Constants.DATE_FORMATTER.format(eingangsdatum)));
	}

	private String getContent5NichtEintreten() {
		if (isFKJVTexte) {
			return translate(NICHT_EINTRETEN_CONTENT_5_FKJV);
		}
		return translate(NICHT_EINTRETEN_CONTENT_5);
	}

	private void addBemerkungenIfAvailable(Document document, boolean showTitle) {
		List<Element> bemerkungenElements = Lists.newArrayList();
		final List<String> bemerkungen = getBemerkungen();
		if (!bemerkungen.isEmpty()) {
			if (showTitle) {
				bemerkungenElements.add(PdfUtil.createParagraph(translate(BEMERKUNGEN)));
			}
			bemerkungenElements.add(PdfUtil.createList(bemerkungen));
			document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
		}
	}

	private void addBemerkungenIfAvailable(Document document) {
		addBemerkungenIfAvailable(document, true);
	}

	protected void addZusatzTextIfAvailable(Document document) {
		if (getGemeindeStammdaten().getHasZusatzText()) {
			document.add(PdfUtil.createParagraph(Objects.requireNonNull(gemeindeStammdaten.getZusatzText())));
		}
	}
 	@Override
	public boolean isVerfuegung() {
		return true;
	}

	@Nonnull
	private PdfPTable createIntroAndInfoKontingentierung() {
		float[] columnWidths = { 30,22 };
		PdfPTable table = new PdfPTable(columnWidths);
		PdfUtil.setTableDefaultStyles(table);
		table.addCell(createIntro());
		if (kontingentierungEnabledAndEntwurf) {
			table.addCell(createInfoKontingentierung());
		} else {
			table.addCell("");
		}
		return table;
	}


	@Nonnull
	private PdfPTable createIntro() {

		//für unbekannte Institutionen soll ein Fragezeichen auf die Verfügung aufgedruckt werden
		final String institutionName = betreuung.getInstitutionStammdaten().getInstitution().isUnknownInstitution()
			? UNKNOWN_INSTITUTION_NAME
			: betreuung.getInstitutionStammdaten().getInstitution().getName();

		final String gemeinde = getGemeindeStammdaten().getGemeinde().getName();

		List<TableRowLabelValue> intro = new ArrayList<>();
		intro.add(new TableRowLabelValue(REFERENZNUMMER, betreuung.getBGNummer()));
		intro.add(new TableRowLabelValue(NAME_KIND, betreuung.getKind().getKindJA().getFullName()));
		if (betreuung.getVorgaengerVerfuegung() != null) {
			Objects.requireNonNull(betreuung.getVorgaengerVerfuegung().getTimestampErstellt());
			intro.add(new TableRowLabelValue(BEMERKUNG, translate(ERSETZT_VERFUEGUNG,
				Constants.DATE_FORMATTER.format(betreuung.getVorgaengerVerfuegung().getTimestampErstellt()))));
		}
		intro.add(new TableRowLabelValue(ANGEBOT, translateEnumValue(betreuung.getBetreuungsangebotTyp())));
		intro.add(new TableRowLabelValue(BETREUUNG_INSTITUTION, institutionName));
		intro.add(new TableRowLabelValue(GEMEINDE, gemeinde));
		return PdfUtil.createIntroTable(intro, sprache, mandant);
	}

	@Nonnull
	private Paragraph createInfoKontingentierung() {
		String gemeinde = getGemeindeStammdaten().getGemeinde().getName();
		String telefon = getGemeindeStammdaten().getTelefonForGesuch(getGesuch());
		String mail = getGemeindeStammdaten().getEmailForGesuch(getGesuch());
		Object[] args = { gemeinde, telefon, mail };
		return PdfUtil.createParagraph(translate(VERWEIS_KONTINGENTIERUNG, args), 0, fontRed);
	}

	@Nonnull
	private PdfPTable createVerfuegungTable() {

		// Tabelle initialisieren
		PdfPTable table = new PdfPTable(getVerfuegungColumnWidths().length);
		try {
			table.setWidths(getVerfuegungColumnWidths());
		} catch (DocumentException e) {
			LOG.error("Failed to set the width: {}", e.getMessage(), e);
		}
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * fontTabelle.getSize() * 2);

		// Referenznummern
		addReferenzNummerCells(table);

		// Spaltentitel, Row 1
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(VON), null, fontTabelle, 2, 1));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(BIS), null, fontTabelle, 2, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, translate(getPensumTitle()), null, fontTabelle, 1, 3));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(VOLLKOSTEN), null, fontTabelle, 2, 1));

		addTitleBerechneterGutschein(table);
		addTitleBetreuungsGutschein(table);
		addTitleNrElternBeitrag(table);
		addTitleNrUeberweiesenerBetrag(table);

		// Spaltentitel, Row 2
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_BETREUUNG), null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_ANSPRUCH), null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_BG), null, fontTabelle, 1, 1));

		// Inhalte (Werte)
		for (VerfuegungZeitabschnitt abschnitt : getVerfuegungZeitabschnitt()) {
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigAb()),
				null,
				fontTabelle,
				1,
				1));
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigBis()),
				null,
				fontTabelle,
				1,
				1));
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				printEffektiv(abschnitt),
				null,
				fontTabelle,
				1,
				1));
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				printAnspruch(abschnitt),
				null,
				fontTabelle,
				1,
				1));
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				printVerguenstigt(abschnitt),
				null,
				fontTabelle,
				1,
				1));
			table.addCell(createCell(
				false,
				Element.ALIGN_RIGHT,
				PdfUtil.printBigDecimal(abschnitt.getVollkosten()),
				null,
				fontTabelle,
				1,
				1));
			addValueBerechneterGutschein(table, abschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten());
			addValueBetreuungsGutschein(table, abschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
			addValueElternBeitrag(table, abschnitt.getMinimalerElternbeitragGekuerzt());
			addValueUeberweiesenerBetrag(table, abschnitt.getVerguenstigung());
		}
		return table;
	}

	protected void addReferenzNummerCells(PdfPTable table) {
		table.addCell(createCell(true, Element.ALIGN_CENTER, "", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "I", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "II", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "III", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "IV", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "V", null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "VI", Color.LIGHT_GRAY, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "VII", Color.LIGHT_GRAY, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_CENTER, "VIII", Color.LIGHT_GRAY, fontTabelle, 1, 1));
	}

	protected void addTitleBerechneterGutschein(PdfPTable table) {
		table.addCell(createCell(
			true,
			Element.ALIGN_RIGHT,
			translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN),
			null,
			fontTabelle,
			2,
			1));
	}

	protected void addTitleBetreuungsGutschein(PdfPTable table) {
		table.addCell(createCell(
			true,
			Element.ALIGN_RIGHT,
			translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG),
			Color.LIGHT_GRAY,
			fontTabelle,
			2,
			1));
	}

	protected void addTitleNrElternBeitrag(PdfPTable table) {
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(ELTERNBEITRAG), Color.LIGHT_GRAY, fontTabelle, 2, 1));
	}

	protected void addTitleNrUeberweiesenerBetrag(PdfPTable table) {
		table.addCell(createCell(true, Element.ALIGN_RIGHT, getTextGutschein(), Color.LIGHT_GRAY, fontTabelle, 2, 1));
	}

	protected void addValueBerechneterGutschein(PdfPTable table, BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(verguenstigungOhneBeruecksichtigungVollkosten),
			null,
			fontTabelle,
			1,
			1));
	}

	protected void addValueBetreuungsGutschein(
		PdfPTable table,
		BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(verguenstigungOhneBeruecksichtigungMinimalbeitrag),
			Color.LIGHT_GRAY,
			getBgColorForBetreuungsgutscheinCell(),
			1,
			1));
		}

	protected void addValueElternBeitrag(PdfPTable table, BigDecimal minimalerElternbeitragGekuerzt) {
		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(minimalerElternbeitragGekuerzt),
			Color.LIGHT_GRAY,
			fontTabelle,
			1,
			1));
	}

	protected void addValueUeberweiesenerBetrag(PdfPTable table, BigDecimal verguenstigung) {
		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(verguenstigung),
			Color.LIGHT_GRAY,
			getBgColorForUeberwiesenerBetragCell(),
			1,
			1));
	}

	protected abstract float[] getVerfuegungColumnWidths();

	protected PdfPCell createCell(
		boolean isHeaderRow,
		int alignment,
		String value,
		@Nullable Color bgColor,
		@Nullable Font font,
		int rowspan,
		int colspan
	) {
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
		cell.setBorderWidthTop(isHeaderRow ? 0.0f : 0.5f);
		cell.setBorderWidthLeft(0.0f);
		cell.setBorderWidthRight(0.0f);

		cell.setLeading(0.0F, PdfUtil.DEFAULT_CELL_LEADING);
		cell.setPadding(0.0f);
		cell.setPaddingTop(2.0f);
		cell.setPaddingBottom(2.0f);
		return cell;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getVerfuegungZeitabschnitt() {
		Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
		if (verfuegung == null) {
			return Collections.emptyList();
		}
		// first of all we get all Zeitabschnitte and create a List of VerfuegungZeitabschnittPrintImpl, then we remove
		// all Zeitabschnitte with Pensum == 0 that we find at the beginning and at the end of the list. All
		// Zeitabschnitte
		// between two valid values will remain: 0, 0, 30, 40, 0, 30, 0, 0 ==> 30, 40, 0, 30
		List<VerfuegungZeitabschnitt> result = verfuegung.getZeitabschnitte().stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR.reversed())
			.collect(Collectors.toList());

		@SuppressWarnings("Duplicates")
		ListIterator<VerfuegungZeitabschnitt> listIteratorBeginning = result.listIterator();
		while (listIteratorBeginning.hasNext()) {
			VerfuegungZeitabschnitt zeitabschnitt = listIteratorBeginning.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuungspensumProzent())) {
				listIteratorBeginning.remove();
			} else {
				break;
			}
		}
		Collections.reverse(result);
		ListIterator<VerfuegungZeitabschnitt> listIteratorEnd = result.listIterator();
		while (listIteratorEnd.hasNext()) {
			VerfuegungZeitabschnitt zeitabschnitt = listIteratorEnd.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuungspensumProzent())) {
				listIteratorEnd.remove();
			} else {
				break;
			}
		}
		return result;
	}

	@Nonnull
	private List<String> getBemerkungen() {
		Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
		if (verfuegung != null && verfuegung.getManuelleBemerkungen() != null) {
			return splitBemerkungen(verfuegung.getManuelleBemerkungen());
		}
		return Collections.emptyList();
	}

	@Nonnull
	private List<String> splitBemerkungen(@Nonnull String bemerkungen) {
		if (Strings.isNullOrEmpty(bemerkungen)) {
			return Collections.emptyList();
		}
		String[] splitBemerkungenNewLine = bemerkungen.split('[' + System.getProperty("line.separator") + "]+");
		return Arrays.stream(splitBemerkungenNewLine)
			.filter(s -> !Strings.isNullOrEmpty(s))
			.collect(Collectors.toList());
	}

	@Nonnull
	public PdfPTable createRechtsmittelBelehrung() {
		GemeindeStammdaten stammdaten = getGemeindeStammdaten();
		Adresse beschwerdeAdresse = stammdaten.getBeschwerdeAdresse();
		if (beschwerdeAdresse == null) {
			beschwerdeAdresse = stammdaten.getAdresseForGesuch(getGesuch());
		}

		String rechtsmittelbelehrung = translate(RECHTSMITTELBELEHRUNG_CONTENT, beschwerdeAdresse.getAddressAsStringInOneLine());
		if (!stammdaten.getStandardRechtsmittelbelehrung()
			&& stammdaten.getRechtsmittelbelehrung() != null) {
			String belehrungInSprache = stammdaten.getRechtsmittelbelehrung().findTextByLocale(sprache);
			if (belehrungInSprache != null) {
				rechtsmittelbelehrung = belehrungInSprache;
			}
		}

		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createBoldParagraph(translate(RECHTSMITTELBELEHRUNG_TITLE), 0));
		innerTable.addCell(PdfUtil.createParagraph(rechtsmittelbelehrung));
		table.addCell(innerTable);
		return table;
	}

	protected Element createNichtEingetretenParagraph1() {
		Kind kind = betreuung.getKind().getKindJA();
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();

		return PdfUtil.createParagraph(translate(
			NICHT_EINTRETEN_CONTENT_1,
			Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
			Constants.DATE_FORMATTER.format(gp.getGueltigBis()),
			kind.getFullName(),
			betreuung.getInstitutionStammdaten().getInstitution().getName(),
			betreuung.getBGNummer()));
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1_NICHT_EINTRETEN), getFusszeile2NichtEintreten())
		);
	}

	private String getFusszeile2NichtEintreten() {
		if (isFKJVTexte) {
			return translate(FUSSZEILE_2_NICHT_EINTRETEN_FKJV);
		}
		return translate(FUSSZEILE_2_NICHT_EINTRETEN);
	}

	private void createFusszeileKeinAnspruch(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(getFusszeile2NichtEintreten())
		);
	}

	private void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(getFusszeile1Verfuegung())
		);
	}

	private String getFusszeile1Verfuegung() {
		if (isFKJVTexte) {
			return translate(FUSSZEILE_1_VERFUEGUNG_FKJV);
		}
		return translate(FUSSZEILE_1_VERFUEGUNG);
	}

	private boolean isTFO() {
		return betreuung.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN;
	}

	private String getPensumTitle() {
		if (isTFO()) {
			return PENSUM_TITLE_TFO;
		}
		return PENSUM_TITLE;
	}

	private String printEffektiv(VerfuegungZeitabschnitt abschnitt) {
		if (isTFO()) {
			return PdfUtil.printBigDecimal(abschnitt.getBetreuungspensumZeiteinheit());
		}
		return PdfUtil.printPercent(abschnitt.getBetreuungspensumProzent());
	}

	private String printAnspruch(VerfuegungZeitabschnitt abschnitt) {
		if (isTFO()) {
			return PdfUtil.printBigDecimal(abschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten());
		}
		return PdfUtil.printPercent(abschnitt.getAnspruchberechtigtesPensum());
	}

	private String printVerguenstigt(VerfuegungZeitabschnitt abschnitt) {
		if (isTFO()) {
			return PdfUtil.printBigDecimal(abschnitt.getVerfuegteAnzahlZeiteinheiten());
		}
		return PdfUtil.printPercent(abschnitt.getBgPensum());
	}

	@Nonnull
	private PdfPTable createErklaerungstextFEBR() {
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createParagraph(translate(VERFUEGUNG_ERKLAERUNG_FEBR), 2));
		table.addCell(innerTable);
		table.setSpacingAfter(15);
		return table;
	}

	protected abstract Font getBgColorForUeberwiesenerBetragCell();

	protected Font getBgColorForBetreuungsgutscheinCell() {
		return fontTabelleBold;
	};

	protected abstract String getTextGutschein();
}

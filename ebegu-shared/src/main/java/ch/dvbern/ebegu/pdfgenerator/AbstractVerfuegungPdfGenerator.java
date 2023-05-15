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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public abstract class AbstractVerfuegungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String NAME_KIND = "PdfGeneration_NameKind";
	private static final String BEMERKUNG = "PdfGeneration_Bemerkung";
	private static final String ERSETZT_VERFUEGUNG = "PdfGeneration_Ersetzt_Verfuegung";
	protected static final String VERFUEGUNG_TITLE = "PdfGeneration_Verfuegung_Title";
	private static final String ANGEBOT = "PdfGeneration_Betreuungsangebot";
	private static final String GEMEINDE = "PdfGeneration_Gemeinde";
	protected static final String VERFUEGUNG_CONTENT_1 = "PdfGeneration_Verfuegung_Content_1";
	protected static final String VERFUEGUNG_CONTENT_2 = "PdfGeneration_Verfuegung_Content_2";
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
	private static final String GUTSCHEIN_AN_INSTITUTION = "PdfGeneration_Verfuegung_Gutschein_Institution";
	private static final String GUTSCHEIN_AN_ELTERN = "PdfGeneration_Verfuegung_Gutschein_Eltern";
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
	protected static final String NICHT_EINTRETEN_CONTENT_6 = "PdfGeneration_NichtEintreten_Content_6";
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
		KEIN_ANSCHRUCH_TFO,
		NICHT_EINTRETTEN
	}

	protected final Betreuung betreuung;
	private final boolean kontingentierungEnabledAndEntwurf;
	private final boolean stadtBernAsivConfigured;
	private final boolean isFKJVTexte;
	private final BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp;
	private boolean showColumnAnElternAuszahlen;
	private boolean showColumnAnInsitutionenAuszahlen;
	private List<VerfuegungZeitabschnitt> abschnitte;

	@Nonnull
	protected final Art art;

	public AbstractVerfuegungPdfGenerator(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte,
		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp
	) {
		super(betreuung.extractGesuch(), stammdaten);

		this.betreuung = betreuung;
		this.art = art;
		this.kontingentierungEnabledAndEntwurf = kontingentierungEnabledAndEntwurf;
		this.stadtBernAsivConfigured = stadtBernAsivConfigured;
		this.isFKJVTexte = isFKJVTexte;
		this.betreuungspensumAnzeigeTyp = betreuungspensumAnzeigeTyp;
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

		switch (art) {
		case NORMAL:
			createDokumentNormal(document, generator);
			break;
		case KEIN_ANSPRUCH:
			createDokumentKeinAnspruch(document, generator);
			break;
		case KEIN_ANSCHRUCH_TFO:
			createDokumentKeinAnspruchTFO(document, generator);
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

	protected void createDokumentNormal(Document document, PdfGenerator generator) {
		Kind kind = betreuung.getKind().getKindJA();

		createFusszeileNormaleVerfuegung(generator.getDirectContent());
		Paragraph firstParagraph = createFirstParagraph(kind);
		document.add(firstParagraph);
		document.add(createVerfuegungTable());

		// Erklaerungstext zu FEBR: Falls Stadt Bern und das Flag ist noch nicht gesetzt
		if (!stadtBernAsivConfigured && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gemeindeStammdaten.getGemeinde())) {
			document.add(createErklaerungstextFEBR());
		}

		addBemerkungenIfAvailable(document);
		addZusatzTextIfAvailable(document);
	}

	@Nonnull
	protected Paragraph createFirstParagraph(Kind kind) {
		Paragraph paragraphWithSupertext = PdfUtil.createParagraph(translate(
			VERFUEGUNG_CONTENT_1,
			kind.getFullName(),
			Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2);
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
		paragraphWithSupertext.add(new Chunk(' ' + translate(VERFUEGUNG_CONTENT_2)));
		return paragraphWithSupertext;
	}

	protected void createDokumentKeinAnspruch(Document document, PdfGenerator generator) {
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();
		LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
		Kind kind = betreuung.getKind().getKindJA();

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
		Paragraph paragraphWithSupertext = PdfUtil.createParagraph(translate(
			KEIN_ANSPRUCH_CONTENT_3,
			kind.getFullName(),
			Constants.DATE_FORMATTER.format(kind.getGeburtsdatum()),
			Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
			Constants.DATE_FORMATTER.format(gp.getGueltigBis())), 2);
		paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
		paragraphWithSupertext.add(new Chunk(' ' + translate(KEIN_ANSPRUCH_CONTENT_4)));
		document.add(paragraphWithSupertext);
	}

	protected void createDokumentKeinAnspruchTFO(Document document, PdfGenerator generator) {
		createDokumentKeinAnspruch(document, generator);
	}

	private void addGruesseElements(@Nonnull List<Element> gruesseElements) {
		gruesseElements.add(createParagraphGruss());
		gruesseElements.add(createParagraphSignatur());
	}

	protected abstract void createDokumentNichtEintretten(@Nonnull final Document document, @Nonnull PdfGenerator generator);


	protected void createDokumentNichtEintrettenDefault(@Nonnull final Document document, @Nonnull PdfGenerator generator) {
		createFusszeileNichtEintreten(generator.getDirectContent());
		document.add(createNichtEingetretenParagraph1());
		document.add(createAntragEingereichtAmParagraph());
		document.add(createNichtEintretenUnterlagenUnvollstaendigParagraph());

		Paragraph paragraphWithSupertext;
		final String textWithFussnote1 = translate(NICHT_EINTRETEN_CONTENT_4);
		paragraphWithSupertext = PdfUtil.createParagraph(textWithFussnote1);
		if (StringUtils.isNotEmpty(textWithFussnote1)) {
			paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
		}
		final String textWithFussnote2 = getContent5NichtEintreten();
		paragraphWithSupertext.add(new Chunk(textWithFussnote2));
		if (StringUtils.isNotEmpty(textWithFussnote2)) {
			paragraphWithSupertext.add(PdfUtil.createSuperTextInText("2"));
		}
		paragraphWithSupertext.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_6)));
		document.add(paragraphWithSupertext);

		var eingangsdatum = getEingangsdatum();

		document.newPage();
		document.add(PdfUtil.createParagraph(translate(
			NICHT_EINTRETEN_CONTENT_7,
			Constants.DATE_FORMATTER.format(eingangsdatum)
		)));
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
		var eingangsdatum = getEingangsdatum();
		return PdfUtil.createParagraph(translate(
			NICHT_EINTRETEN_CONTENT_2,
			Constants.DATE_FORMATTER.format(eingangsdatum)));
	}

	protected LocalDate getEingangsdatum() {
		return gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
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
		if (getGemeindeStammdaten().getHasZusatzTextVerfuegung()) {
			document.add(PdfUtil.createParagraph(Objects.requireNonNull(gemeindeStammdaten.getZusatzTextVerfuegung())));
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
		addAngebotToIntro(intro);
		addInstitutionToIntro(institutionName, intro);
		intro.add(new TableRowLabelValue(GEMEINDE, gemeinde));
		return PdfUtil.createIntroTable(intro, sprache, mandant);
	}

	protected void addAngebotToIntro(List<TableRowLabelValue> intro) {
		intro.add(new TableRowLabelValue(ANGEBOT, translateEnumValue(betreuung.getBetreuungsangebotTyp())));
	}

	protected void addInstitutionToIntro(String institutionName, List<TableRowLabelValue> intro) {
		intro.add(new TableRowLabelValue(BETREUUNG_INSTITUTION, institutionName));
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
		this.abschnitte = getVerfuegungZeitabschnitt();
		this.showColumnAnElternAuszahlen = showColumnAnElternAuszahlen();
		this.showColumnAnInsitutionenAuszahlen = showColumnAnInsitutionenAuszahlen();

		// Tabelle initialisieren
		PdfPTable table = new PdfPTable(calculateVerfuegungColumnWidths().length);
		try {
			table.setWidths(calculateVerfuegungColumnWidths());
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

		addTitleBeitraghoheUndSelbstbehaltInProzent(table);
		addTitleBerechneterGutschein(table);
		addTitleBetreuungsGutschein(table);
		addTitleNrElternBeitrag(table);
		addTitleGutscheinProStunde(table);
		addTitleNrUeberweiesenerBetragInstitution(table);
		addTitleNrUeberweiesenerBetragEltern(table);

		// Spaltentitel, Row 2
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_BETREUUNG), null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_ANSPRUCH), null, fontTabelle, 1, 1));
		table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(PENSUM_BG), null, fontTabelle, 1, 1));

		// Inhalte (Werte)
		for (VerfuegungZeitabschnitt abschnitt : abschnitte) {
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
			addValueaBeitraghoheUndSelbstbehaltInProzent(table, abschnitt.getBeitraghoheProzent());
			addValueBerechneterGutschein(table, abschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten());
			addValueBetreuungsGutschein(table, abschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
			addValueElternBeitrag(table, abschnitt.getMinimalerElternbeitragGekuerzt());
			addValueGutscheinProStunde(table, abschnitt.getVerguenstigungProZeiteinheit());
			addValueUeberweiesenerBetragInstitution(table, abschnitt);
			addValueUeberweiesenerBetragEltern(table, abschnitt);
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

	protected abstract void addTitleGutscheinProStunde(PdfPTable table);

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

	private void addTitleNrUeberweiesenerBetragEltern(PdfPTable table) {
		if (showColumnAnElternAuszahlen) {
			table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(GUTSCHEIN_AN_ELTERN), Color.LIGHT_GRAY, fontTabelle, 2, 1));
		}
	}

	protected void addTitleNrUeberweiesenerBetragInstitution(PdfPTable table) {
		if (showColumnAnInsitutionenAuszahlen) {
			table.addCell(createCell(true, Element.ALIGN_RIGHT, translate(GUTSCHEIN_AN_INSTITUTION), Color.LIGHT_GRAY, fontTabelle, 2, 1));
		}
	}

	protected void addTitleBeitraghoheUndSelbstbehaltInProzent(PdfPTable table) {
		//no-op ausser in Appenzell
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

	protected abstract void addValueGutscheinProStunde(PdfPTable table, @Nullable BigDecimal verguenstigungProZeiteinheit);

	protected void addValueUeberweiesenerBetragInstitution(PdfPTable table, VerfuegungZeitabschnitt zeitabschnitt) {
		if (!showColumnAnInsitutionenAuszahlen) {
			return;
		}

		BigDecimal verguenstigungAnInstitution = zeitabschnitt.isAuszahlungAnEltern() ? BigDecimal.ZERO : zeitabschnitt.getVerguenstigung();

		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(verguenstigungAnInstitution),
			Color.LIGHT_GRAY,
			getBgColorForUeberwiesenerBetragCell(),
			1,
			1));

	}

	private void addValueUeberweiesenerBetragEltern(PdfPTable table, VerfuegungZeitabschnitt zeitabschnitt) {
		if (!showColumnAnElternAuszahlen) {
			return;
		}

		BigDecimal verguenstigungAnEltern = zeitabschnitt.isAuszahlungAnEltern() ? zeitabschnitt.getVerguenstigung() : BigDecimal.ZERO;

		table.addCell(createCell(
			false,
			Element.ALIGN_RIGHT,
			PdfUtil.printBigDecimal(verguenstigungAnEltern),
			Color.LIGHT_GRAY,
			getBgColorForUeberwiesenerBetragCell(),
			1,
			1));
	}

	protected void addValueaBeitraghoheUndSelbstbehaltInProzent(PdfPTable table, Integer beitraghoheInProzent) {
		//no-op ausser in Appenzell
	}

	protected abstract float[] getVerfuegungColumnWidths();

	private float[] calculateVerfuegungColumnWidths() {
		float[] columnwidths = getVerfuegungColumnWidths();

		//Wenn beide Columns angezeigt werden, muss eine zusätzliche Spalten-Breite dem Array hinzugefügt werden
		if(showColumnAnInsitutionenAuszahlen && showColumnAnElternAuszahlen) {
			float[] columnwidthsExtended = new float[columnwidths.length + 1];
			System.arraycopy(columnwidths, 0, columnwidthsExtended, 0, columnwidths.length);
			columnwidthsExtended[columnwidthsExtended.length-1] = 110;
			return columnwidthsExtended;
		}

		return columnwidths;
	}

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
	protected List<VerfuegungZeitabschnitt> getVerfuegungZeitabschnitt() {
		// first of all we get all Zeitabschnitte and create a List of VerfuegungZeitabschnittPrintImpl
		List<VerfuegungZeitabschnitt> result = getZeitabschnitteOrderByGueltigAb(true);
		// then we remove
		// all Zeitabschnitte with Pensum == 0 that we find at the beginning of the list.
		// 0, 0, 30, 40, 0, 30, 0, 0 ==> 30, 40, 0, 30, 0, 0
		removeLeadingZeitabschnitteWithNoPositivBetreuungsPensum(result);
		// then we remove
		// all Zeitabschnitte with Pensum == 0 that we find at the end of the list. All Zeitabschnitte
		// between two valid values will remain: 0, 0, 30, 40, 0, 30, 0, 0 ==> 30, 40, 0, 30
		Collections.reverse(result);
		removeLeadingZeitabschnitteWithNoPositivBetreuungsPensum(result);
		return result;
	}

	protected void removeLeadingZeitabschnitteWithNoPositivBetreuungsPensum(List<VerfuegungZeitabschnitt> result) {
		ListIterator<VerfuegungZeitabschnitt> listIterator = result.listIterator();
		while (listIterator.hasNext()) {
			VerfuegungZeitabschnitt zeitabschnitt = listIterator.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuungspensumProzent())) {
				listIterator.remove();
			} else {
				break;
			}
		}
	}

	protected List<VerfuegungZeitabschnitt> getZeitabschnitteOrderByGueltigAb(boolean reversed) {
		Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
		if (verfuegung == null) {
			return Collections.emptyList();
		}

		Comparator<Gueltigkeit> comparator = reversed ?
			Gueltigkeit.GUELTIG_AB_COMPARATOR.reversed() :
			Gueltigkeit.GUELTIG_AB_COMPARATOR;

		return verfuegung.getZeitabschnitte().stream()
			.sorted(comparator)
			.collect(Collectors.toList());
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

	protected void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
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

	private boolean isStunden() {
		return (betreuungspensumAnzeigeTyp.equals(BetreuungspensumAnzeigeTyp.ZEITEINHEIT_UND_PROZENT) &&
			betreuung.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN)
			|| betreuungspensumAnzeigeTyp.equals(BetreuungspensumAnzeigeTyp.NUR_STUNDEN);
	}

	private String getPensumTitle() {
		if (isStunden()) {
			return PENSUM_TITLE_TFO;
		}
		return PENSUM_TITLE;
	}

	private String printEffektiv(VerfuegungZeitabschnitt abschnitt) {
		if (isStunden()) {
			return PdfUtil.printBigDecimal(abschnitt.getBetreuungspensumZeiteinheit());
		}
		return PdfUtil.printPercent(abschnitt.getBetreuungspensumProzent());
	}

	private String printAnspruch(VerfuegungZeitabschnitt abschnitt) {
		if (isStunden()) {
			return PdfUtil.printBigDecimal(abschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten());
		}
		return PdfUtil.printPercent(abschnitt.getAnspruchberechtigtesPensum());
	}

	private String printVerguenstigt(VerfuegungZeitabschnitt abschnitt) {
		if (isStunden()) {
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

	private boolean showColumnAnElternAuszahlen() {
		return this.abschnitte
			.stream()
			.anyMatch(VerfuegungZeitabschnitt::isAuszahlungAnEltern);
	}

	private boolean showColumnAnInsitutionenAuszahlen() {
		return this.abschnitte
			.stream()
			.anyMatch(abschnitt -> !abschnitt.isAuszahlungAnEltern());
	}

	protected abstract Font getBgColorForUeberwiesenerBetragCell();

	protected Font getBgColorForBetreuungsgutscheinCell() {
		return fontTabelleBold;
	};
}

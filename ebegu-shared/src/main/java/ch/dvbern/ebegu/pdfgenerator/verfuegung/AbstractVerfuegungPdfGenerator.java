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

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
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

public abstract class AbstractVerfuegungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String NAME_KIND = "PdfGeneration_NameKind";
	private static final String BEMERKUNG = "PdfGeneration_Bemerkung";
	private static final String ERSETZT_VERFUEGUNG = "PdfGeneration_Ersetzt_Verfuegung";
	protected static final String VERFUEGUNG_TITLE = "PdfGeneration_Verfuegung_Title";
	protected static final String VERFUEGUNG_NICHT_EINTRETEN_TITLE = "PdfGeneration_Verfuegung_NichtEintreten_Title";
	private static final String ANGEBOT = "PdfGeneration_Betreuungsangebot";
	private static final String GEMEINDE = "PdfGeneration_Gemeinde";
	protected static final String VERFUEGUNG_CONTENT_1 = "PdfGeneration_Verfuegung_Content_1";
	protected static final String VERFUEGUNG_CONTENT_2 = "PdfGeneration_Verfuegung_Content_2";
	private static final String VERFUEGUNG_ERKLAERUNG_FEBR = "PdfGeneration_Verfuegung_Erklaerung_FEBR";
	private static final String VON = "PdfGeneration_Verfuegung_Von";
	private static final String BIS = "PdfGeneration_Verfuegung_Bis";
	private static final String PENSUM_TITLE = "PdfGeneration_Verfuegung_PensumTitle";
	private static final String PENSUM_TITLE_TFO = "PdfGeneration_Verfuegung_PensumTitleTFO";
	protected static final String PENSUM_BETREUUNG = "PdfGeneration_Verfuegung_Betreuungspensum";
	protected static final String PENSUM_ANSPRUCH = "PdfGeneration_Verfuegung_Anspruchspensum";
	protected static final String PENSUM_BG = "PdfGeneration_Verfuegung_BgPensum";
	protected static final String VOLLKOSTEN = "PdfGeneration_Verfuegung_Vollkosten";
	protected static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN =
		"PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungVollkosten";
	protected static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG =
		"PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungMinimalbeitrag";
	protected static final String GUTSCHEIN_AN_INSTITUTION = "PdfGeneration_Verfuegung_Gutschein_Institution";
	private static final String HOEHERER_GUTSCHEIN = "PdfGeneration_Verfuegung_Hoeherer_Gutschein";
	protected static final String GUTSCHEIN_AN_ELTERN = "PdfGeneration_Verfuegung_Gutschein_Eltern";
	protected static final String ELTERNBEITRAG = "PdfGeneration_Verfuegung_MinimalerElternbeitrag";
	private static final String KEIN_ANSPRUCH_CONTENT_1 = "PdfGeneration_KeinAnspruch_Content_1";
	private static final String KEIN_ANSPRUCH_CONTENT_2 = "PdfGeneration_KeinAnspruch_Content_2";
	private static final String KEIN_ANSPRUCH_CONTENT_3 = "PdfGeneration_KeinAnspruch_Content_3";
	private static final String KEIN_ANSPRUCH_CONTENT_4 = "PdfGeneration_KeinAnspruch_Content_4";
	protected static final String NICHT_EINTRETEN_CONTENT_1 = "PdfGeneration_NichtEintreten_Content_1";
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
	protected static final String RECHTSMITTELBELEHRUNG_CONTENT = "PdfGeneration_Rechtsmittelbelehrung_Content";
	protected static final String FUSSZEILE_1_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile1";
	private static final String FUSSZEILE_2_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile2";
	private static final String FUSSZEILE_2_NICHT_EINTRETEN_FKJV = "PdfGeneration_NichtEintreten_Fusszeile2_FKJV";
	private static final String FUSSZEILE_1_VERFUEGUNG = "PdfGeneration_Verfuegung_Fusszeile1";
	private static final String FUSSZEILE_1_VERFUEGUNG_FKJV = "PdfGeneration_Verfuegung_Fusszeile1_FKJV";
	private static final String VERWEIS_KONTINGENTIERUNG = "PdfGeneration_Verweis_Kontingentierung";

	public static final String UNKNOWN_INSTITUTION_NAME = "?";

	protected final Font fontTabelle = PdfUtil.createFontWithSize(getPageConfiguration().getFonts().getFont(), 8.0f);
	protected final Font fontTabelleBold = PdfUtil.createFontWithSize(getPageConfiguration().getFonts().getFontBold(), 8.0f);
	private final Font fontRed = PdfUtil.createFontWithColor(getPageConfiguration().getFonts().getFont(), Color.RED);

	public enum Art {
		NORMAL,
		KEIN_ANSPRUCH,
		KEIN_ANSCHRUCH_TFO,
		NICHT_EINTRETTEN
	}

	protected final Betreuung betreuung;
	protected final VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration;

	@Nonnull
	protected final Art art;

	protected AbstractVerfuegungPdfGenerator(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration
	) {
		super(betreuung.extractGesuch(), stammdaten);
		this.betreuung = betreuung;
		this.art = art;
		this.verfuegungPdfGeneratorKonfiguration = verfuegungPdfGeneratorKonfiguration;
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
		if (!verfuegungPdfGeneratorKonfiguration.isStadtBernAsivConfigured() && KitaxUtil.isGemeindeWithKitaxUebergangsloesung(
			gemeindeStammdaten.getGemeinde())) {
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
			betreuung.getReferenzNummer())));
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
		addSuperTextForKeinAnspruchAbschnitt(paragraphWithSupertext);
		paragraphWithSupertext.add(new Chunk(' ' + translate(KEIN_ANSPRUCH_CONTENT_4)));
		document.add(paragraphWithSupertext);
	}

	protected void addSuperTextForKeinAnspruchAbschnitt(Paragraph paragraph) {
		paragraph.add(PdfUtil.createSuperTextInText("1"));
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
		LocalDate eingangsdatum = getEingangsdatum();
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
		if (verfuegungPdfGeneratorKonfiguration.isFKJVTexte()) {
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
		float[] columnWidths = { 30, 22 };
		PdfPTable table = new PdfPTable(columnWidths);
		PdfUtil.setTableDefaultStyles(table);
		table.addCell(createIntro());
		if (verfuegungPdfGeneratorKonfiguration.isKontingentierungEnabledAndEntwurf()) {
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
		intro.add(new TableRowLabelValue(REFERENZ_NUMMER, betreuung.getReferenzNummer()));
		intro.add(new TableRowLabelValue(NAME_KIND, betreuung.getKind().getKindJA().getFullName()));
		if (betreuung.getVorgaengerVerfuegung() != null) {
			Objects.requireNonNull(betreuung.getVorgaengerVerfuegung().getTimestampErstellt());
			intro.add(new TableRowLabelValue(BEMERKUNG, translate(
				ERSETZT_VERFUEGUNG,
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
	protected PdfPTable createVerfuegungTable() {
		final List<VerfuegungZeitabschnitt> zeitabschnitte = getVerfuegungZeitabschnitt();
		VerfuegungTable verfuegungTable = new VerfuegungTable(
			zeitabschnitte,
			getPageConfiguration(),
			true
		);
		verfuegungTable
			.add(createVonColumn())
			.add(createBisColumn())
			.add(createPensumGroup())
			.add(createVollkostenColumn())
			.add(createGutscheinOhneVollkostenColumn())
			.add(createGutscheinOhneMinimalbeitragColumn())
			.add(createElternbeitragColumn());

		addEinstellungDependingColumns(verfuegungTable, zeitabschnitte);

		return verfuegungTable.build();
	}

	protected void addEinstellungDependingColumns(VerfuegungTable verfuegungTable,
		List<VerfuegungZeitabschnitt> zeitabschnitte) {
		var hasHoeherenBeitrag =
			zeitabschnitte.stream().anyMatch(z -> z.getBedarfsstufe() != null && z.getBedarfsstufe() != Bedarfsstufe.KEINE);
		if (verfuegungPdfGeneratorKonfiguration.isHoehereBeitraegeConfigured && hasHoeherenBeitrag) {
			verfuegungTable.add(createHoehererGutscheinColumn());
		}

		if (showColumnAnInsitutionenAuszahlen(zeitabschnitte)) {
			verfuegungTable.add(createGutscheinInstitutionColumn());
		}

		if (showColumnAnElternAuszahlen(zeitabschnitte)) {
			verfuegungTable.add(createGutscheinElternColumn());
		}
	}

	@Nonnull
	protected VerfuegungTableColumnGroup createPensumGroup() {
		return VerfuegungTableColumnGroup.builder()
			.title(translate(getPensumTitle()))
			.columns(List.of(
				createPensumBetreuungColumn(),
				createPensumAnspruchColumn(),
				createPensumAnspruchKonkretColumn()
			))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createGutscheinElternColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_ELTERN))
			.romanNumber("VIII")
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnEltern(abschnitt)))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createGutscheinInstitutionColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_INSTITUTION))
			.romanNumber("VIII")
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnInstitution(abschnitt)))
			.build();
	}

	@Nonnull
	private VerfuegungTableColumn createHoehererGutscheinColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(HOEHERER_GUTSCHEIN))
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getHoehererBeitrag()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createElternbeitragColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(ELTERNBEITRAG))
			.romanNumber("VII")
			.bgColor(Color.LIGHT_GRAY)
			.width(108)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getMinimalerElternbeitragGekuerzt()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createGutscheinOhneMinimalbeitragColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG))
			.romanNumber("VI")
			.bgColor(Color.LIGHT_GRAY)
			.width(100)
			.boldContent(true)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createGutscheinOhneVollkostenColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN))
			.romanNumber("V")
			.width(100)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createVollkostenColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(VOLLKOSTEN))
			.romanNumber("IV")
			.width(88)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVollkosten()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createPensumAnspruchKonkretColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_BG))
			.romanNumber("III")
			.width(88)
			.dataExtractor(this::printVerguenstigt)
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createPensumAnspruchColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_ANSPRUCH))
			.romanNumber("II")
			.width(88)
			.dataExtractor(this::printAnspruch)
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createPensumBetreuungColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_BETREUUNG))
			.romanNumber("I")
			.width(88)
			.dataExtractor(this::printEffektiv)
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createBisColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(BIS))
			.width(100)
			.dataExtractor(abschnitt -> Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigBis()))
			.build();
	}

	@Nonnull
	protected VerfuegungTableColumn createVonColumn() {
		return VerfuegungTableColumn.builder()
			.width(90)
			.title(translate(VON))
			.dataExtractor(abschnitt -> Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigAb()))
			.build();
	}

	@Nonnull
	protected static BigDecimal getVerguenstigungAnInstitution(VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.isAuszahlungAnEltern() ? BigDecimal.ZERO : zeitabschnitt.getVerguenstigung();
	}

	@Nonnull
	protected static BigDecimal getVerguenstigungAnEltern(VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.isAuszahlungAnEltern() ? zeitabschnitt.getVerguenstigung() : BigDecimal.ZERO;
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
		String rechtsmittelbelehrung = getRechtsmittelbelehrungContent(stammdaten);
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

	protected String getRechtsmittelbelehrungContent(@Nonnull GemeindeStammdaten stammdaten) {
		Adresse beschwerdeAdresse = stammdaten.getBeschwerdeAdresse();
		if (beschwerdeAdresse == null) {
			beschwerdeAdresse = stammdaten.getAdresseForGesuch(getGesuch());
		}
		return translate(RECHTSMITTELBELEHRUNG_CONTENT, beschwerdeAdresse.getAddressWithOrganisationAsStringInOneLine());
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
			betreuung.getReferenzNummer()));
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1_NICHT_EINTRETEN), getFusszeile2NichtEintreten())
		);
	}

	private String getFusszeile2NichtEintreten() {
		if (verfuegungPdfGeneratorKonfiguration.isFKJVTexte()) {
			return translate(FUSSZEILE_2_NICHT_EINTRETEN_FKJV);
		}
		return translate(FUSSZEILE_2_NICHT_EINTRETEN);
	}

	protected void createFusszeileKeinAnspruch(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
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
		if (verfuegungPdfGeneratorKonfiguration.isFKJVTexte()) {
			return translate(FUSSZEILE_1_VERFUEGUNG_FKJV);
		}
		return translate(FUSSZEILE_1_VERFUEGUNG);
	}

	private boolean isStunden() {
		return (verfuegungPdfGeneratorKonfiguration.getBetreuungspensumAnzeigeTyp()
			== BetreuungspensumAnzeigeTyp.ZEITEINHEIT_UND_PROZENT &&
			betreuung.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN)
			|| verfuegungPdfGeneratorKonfiguration.getBetreuungspensumAnzeigeTyp() == BetreuungspensumAnzeigeTyp.NUR_STUNDEN;
	}

	private String getPensumTitle() {
		if (isStunden()) {
			return PENSUM_TITLE_TFO;
		}
		return PENSUM_TITLE;
	}

	protected String printEffektiv(VerfuegungZeitabschnitt abschnitt) {
		if (isStunden()) {
			return PdfUtil.printBigDecimal(abschnitt.getBetreuungspensumZeiteinheit());
		}
		return PdfUtil.printPercent(abschnitt.getBetreuungspensumProzent());
	}

	protected String printAnspruch(VerfuegungZeitabschnitt abschnitt) {
		if (isStunden()) {
			return PdfUtil.printBigDecimal(abschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten());
		}
		return PdfUtil.printPercent(abschnitt.getAnspruchberechtigtesPensum());
	}

	protected String printVerguenstigt(VerfuegungZeitabschnitt abschnitt) {
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

	protected boolean showColumnAnElternAuszahlen(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte
			.stream()
			.anyMatch(VerfuegungZeitabschnitt::isAuszahlungAnEltern);
	}

	protected boolean showColumnAnInsitutionenAuszahlen(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte
			.stream()
			.anyMatch(abschnitt -> !abschnitt.isAuszahlungAnEltern());
	}

}

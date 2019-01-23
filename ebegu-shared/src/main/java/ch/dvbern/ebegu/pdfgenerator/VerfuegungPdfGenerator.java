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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;

public class VerfuegungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String NAME_KIND = "PdfGeneration_NameKind";
	private static final String VERFUEGUNG_TITLE = "PdfGeneration_Verfuegung_Title";
	private static final String ANGEBOT = "PdfGeneration_Betreuungsangebot";
	private static final String VERFUEGUNG_CONTENT = "PdfGeneration_Verfuegung_Content";
	private static final String VON = "PdfGeneration_Verfuegung_Von";
	private static final String BIS = "PdfGeneration_Verfuegung_Bis";
	private static final String PENSUM_BETREUUNG = "PdfGeneration_Verfuegung_Betreuungspensum";
	private static final String PENSUM_ANSPRUCH = "PdfGeneration_Verfuegung_Anspruchspensum";
	private static final String PENSUM_BG = "PdfGeneration_Verfuegung_BgPensum";
	private static final String VOLLKOSTEN = "PdfGeneration_Verfuegung_Vollkosten";
	private static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN = "PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungVollkosten";
	private static final String GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG = "PdfGeneration_Verfuegung_GutscheinOhneBeruecksichtigungMinimalbeitrag";
	private static final String GUTSCHEIN = "PdfGeneration_Verfuegung_Gutschein";
	private static final String ELTERNBEITRAG = "PdfGeneration_Verfuegung_MinimalerElternbeitrag";
	private static final String KEIN_ANSPRUCH_CONTENT = "PdfGeneration_KeinAnspruch_Content";
	private static final String NICHT_EINTRETEN_CONTENT_1 = "PdfGeneration_NichtEintreten_Content_1";
	private static final String NICHT_EINTRETEN_CONTENT_2 = "PdfGeneration_NichtEintreten_Content_2";
	private static final String NICHT_EINTRETEN_CONTENT_3 = "PdfGeneration_NichtEintreten_Content_3";
	private static final String NICHT_EINTRETEN_CONTENT_4 = "PdfGeneration_NichtEintreten_Content_4";
	private static final String NICHT_EINTRETEN_CONTENT_5 = "PdfGeneration_NichtEintreten_Content_5";
	private static final String NICHT_EINTRETEN_CONTENT_6 = "PdfGeneration_NichtEintreten_Content_6";
	private static final String NICHT_EINTRETEN_CONTENT_7 = "PdfGeneration_NichtEintreten_Content_7";
	private static final String NICHT_EINTRETEN_CONTENT_8 = "PdfGeneration_NichtEintreten_Content_8";
	private static final String NICHT_EINTRETEN_CONTENT_9 = "PdfGeneration_NichtEintreten_Content_9";
	private static final String NICHT_EINTRETEN_CONTENT_10 = "PdfGeneration_NichtEintreten_Content_10";
	private static final String BEMERKUNGEN = "PdfGeneration_Verfuegung_Bemerkungen";
	private static final String RECHTSMITTELBELEHRUNG_TITLE = "PdfGeneration_Rechtsmittelbelehrung_Title";
	private static final String RECHTSMITTELBELEHRUNG_CONTENT = "PdfGeneration_Rechtsmittelbelehrung_Content";
	private static final String FUSSZEILE_1_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile1";
	private static final String FUSSZEILE_2_NICHT_EINTRETEN = "PdfGeneration_NichtEintreten_Fusszeile2";
	private static final String FUSSZEILE_1_VERFUEGUNG = "PdfGeneration_Verfuegung_Fusszeile1";

	private static final String VERWEIS_KONTINGENTIERUNG = "PdfGeneration_Verweis_Kontingentierung";

	public enum Art {
		NORMAL,
		KEIN_ANSPRUCH,
		NICHT_EINTRETTEN
	}

	private final Betreuung betreuung;
	private final boolean kontingentierungEnabledAndEntwurf;

	@Nonnull
	private final Art art;

	public VerfuegungPdfGenerator(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		boolean kontingentierungEnabledAndEntwurf
	) {
		super(betreuung.extractGesuch(), stammdaten);

		this.betreuung = betreuung;
		this.art = art;
		this.kontingentierungEnabledAndEntwurf = kontingentierungEnabledAndEntwurf;
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

	public void createContent(@Nonnull final Document document, @Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator) throws DocumentException {
		List<Element> gruesseElements = Lists.newArrayList();
		Kind kind = betreuung.getKind().getKindJA();
		DateRange gp = gesuch.getGesuchsperiode().getGueltigkeit();
		switch (art) {
			case NORMAL:
				createFusszeileNormaleVerfuegung(generator.getDirectContent());
				document.add(PdfUtil.createParagraph(translate(VERFUEGUNG_CONTENT,
					kind.getFullName(),
					Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2));
				document.add(createVerfuegungTable());
				addBemerkungenIfAvailable(document);
				break;
			case KEIN_ANSPRUCH:
				document.add(PdfUtil.createParagraph(translate(KEIN_ANSPRUCH_CONTENT,
					kind.getFullName(),
					Constants.DATE_FORMATTER.format(kind.getGeburtsdatum()),
					Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
					Constants.DATE_FORMATTER.format(gp.getGueltigBis())), 2));
				addBemerkungenIfAvailable(document);
				break;
			case NICHT_EINTRETTEN:
				createFusszeileNichtEintreten(generator.getDirectContent());
				LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_1,
					Constants.DATE_FORMATTER.format(gp.getGueltigAb()),
					Constants.DATE_FORMATTER.format(gp.getGueltigBis()),
					kind.getFullName(),
					betreuung.getInstitutionStammdaten().getInstitution().getName(),
					betreuung.getBGNummer())));
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_2,
					Constants.DATE_FORMATTER.format(eingangsdatum))));
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_3)));

				Paragraph paragraphWithSupertext = PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_4));
				paragraphWithSupertext.add(PdfUtil.createSuperTextInText("1"));
				paragraphWithSupertext.add(new Chunk(translate(NICHT_EINTRETEN_CONTENT_5), PdfUtilities.DEFAULT_FONT));
				paragraphWithSupertext.add(PdfUtil.createSuperTextInText("2"));
				paragraphWithSupertext.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_6)));
				document.add(paragraphWithSupertext);
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_7)));
				document.newPage();
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_8)));
				document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_9)));
				document.add(PdfUtil.createBoldParagraph(translate(NICHT_EINTRETEN_CONTENT_10,
					Constants.DATE_FORMATTER.format(eingangsdatum)), 2));
				break;
		}
		gruesseElements.add(createParagraphGruss());
		gruesseElements.add(createParagraphSignatur());
		document.add(PdfUtil.createKeepTogetherTable(gruesseElements, 2, 0));
		document.add(createRechtsmittelBelehrung());
	}

	private void addBemerkungenIfAvailable(Document document) {
		List<Element> bemerkungenElements = Lists.newArrayList();
		final List<String> bemerkungen = getBemerkungen();
		if (!bemerkungen.isEmpty()) {
			bemerkungenElements.add(PdfUtil.createParagraph(translate(BEMERKUNGEN)));
			bemerkungenElements.add(PdfUtil.createList(getBemerkungen()));
			document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
		}
	}

	@Nonnull
	private PdfPTable createIntroAndInfoKontingentierung() {
		float[] columnWidths = { 1,1 };
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
		List<TableRowLabelValue> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new TableRowLabelValue(REFERENZNUMMER, betreuung.getBGNummer()));
		introBasisjahr.add(new TableRowLabelValue(NAME_KIND, betreuung.getKind().getKindJA().getFullName()));
		introBasisjahr.add(new TableRowLabelValue(ANGEBOT, translateEnumValue(betreuung.getBetreuungsangebotTyp())));
		introBasisjahr.add(new TableRowLabelValue(BETREUUNG_INSTITUTION, betreuung.getInstitutionStammdaten().getInstitution().getName()));
		return PdfUtil.creatreIntroTable(introBasisjahr, sprache);
	}

	@Nonnull
	private Paragraph createInfoKontingentierung() {
		String gemeinde = getGemeindeStammdaten().getGemeinde().getName();
		String telefon = getGemeindeStammdaten().getTelefon();
		String mail = getGemeindeStammdaten().getMail();
		Object[] args = {gemeinde, telefon, mail};
		return PdfUtil.createParagraph(translate(VERWEIS_KONTINGENTIERUNG, args), 0, PdfUtil.FONT_RED);
	}

	@Nonnull
	private PdfPTable createVerfuegungTable() {
		Objects.requireNonNull(betreuung.getVerfuegung());
		List<String[]> values = new ArrayList<>();
		String[] titles = {
			translate(VON),
			translate(BIS),
			translate(PENSUM_BETREUUNG),
			translate(PENSUM_ANSPRUCH),
			translate(PENSUM_BG),
			translate(VOLLKOSTEN),
			translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN),
			translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG),
			translate(ELTERNBEITRAG),
			translate(GUTSCHEIN),

		};
		values.add(titles);
		for (VerfuegungZeitabschnitt abschnitt : getVerfuegungZeitabschnitt()) {
			String[] data = {
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigAb()),
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigBis()),
				PdfUtil.printPercent(abschnitt.getBetreuungspensum()),
				PdfUtil.printPercent(abschnitt.getAnspruchberechtigtesPensum()),
				PdfUtil.printPercent(abschnitt.getBgPensum()),
				PdfUtil.printBigDecimal(abschnitt.getVollkosten()),
				PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten()),
				PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()),
				PdfUtil.printBigDecimal(abschnitt.getMinimalerElternbeitragGekuerzt()),
				PdfUtil.printBigDecimal(abschnitt.getVerguenstigung()),
			};
			values.add(data);
		}
		float[] columnWidths = {10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
		int[] alignement = {Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT };
		return PdfUtil.createTable(values, columnWidths, alignement, 2);
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getVerfuegungZeitabschnitt() {
		Verfuegung verfuegung = betreuung.getVerfuegung();
		if (verfuegung == null) {
			return Collections.emptyList();
		}
		// first of all we get all Zeitabschnitte and create a List of VerfuegungZeitabschnittPrintImpl, then we remove
		// all Zeitabschnitte with Pensum == 0 that we find at the beginning and at the end of the list. All Zeitabschnitte
		// between two valid values will remain: 0, 0, 30, 40, 0, 30, 0, 0 ==> 30, 40, 0, 30
		List<VerfuegungZeitabschnitt> result = verfuegung.getZeitabschnitte().stream()
				.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR.reversed())
				.collect(Collectors.toList());

		@SuppressWarnings("Duplicates")
		ListIterator<VerfuegungZeitabschnitt> listIteratorBeginning = result.listIterator();
		while (listIteratorBeginning.hasNext()) {
			VerfuegungZeitabschnitt zeitabschnitt = listIteratorBeginning.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuungspensum())) {
				listIteratorBeginning.remove();
			} else {
				break;
			}
		}
		Collections.reverse(result);
		ListIterator<VerfuegungZeitabschnitt> listIteratorEnd = result.listIterator();
		while (listIteratorEnd.hasNext()) {
			VerfuegungZeitabschnitt zeitabschnitt = listIteratorEnd.next();
			if (!MathUtil.isPositive(zeitabschnitt.getBetreuungspensum())) {
				listIteratorEnd.remove();
			} else {
				break;
			}
		}
		return result;
	}

	@Nonnull
	private List<String> getBemerkungen() {
		// Wenn die Betreuung VERFUEGT ist -> manuelle Bemerkungen Wenn die Betreuung noch nicht VERFUEGT ist ->
		// generated Bemerkungen
		Verfuegung verfuegung = betreuung.getVerfuegung();
		if (verfuegung != null) {
			String bemerkungenAsString = gesuch.getStatus().isAnyStatusOfVerfuegt() ? verfuegung.getManuelleBemerkungen()
				: verfuegung.getGeneratedBemerkungen();
			if (bemerkungenAsString != null) {
				return splitBemerkungen(bemerkungenAsString);
			}
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
		Adresse beschwerdeAdresse = getGemeindeStammdaten().getBeschwerdeAdresse();
		if (beschwerdeAdresse == null) {
			beschwerdeAdresse = getGemeindeStammdaten().getAdresse();
		}

		PdfPTable table = new PdfPTable(1);
		table.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.setWidthPercentage(FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createBoldParagraph(translate(RECHTSMITTELBELEHRUNG_TITLE), 0));
		innerTable.addCell(PdfUtil.createParagraph(translate(RECHTSMITTELBELEHRUNG_CONTENT, beschwerdeAdresse.getAddressAsStringInOneLine())));
		table.addCell(innerTable);
		return table;
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1_NICHT_EINTRETEN), translate(FUSSZEILE_2_NICHT_EINTRETEN))
		);
	}

	private void createFusszeileNormaleVerfuegung(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1_VERFUEGUNG))
		);
	}
}

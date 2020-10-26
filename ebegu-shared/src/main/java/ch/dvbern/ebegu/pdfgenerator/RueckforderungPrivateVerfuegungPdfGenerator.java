/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.lang3.StringUtils;

public class RueckforderungPrivateVerfuegungPdfGenerator extends MandantPdfGenerator {

	private static final String VERFUEGUNG_TITLE =
		"PdfGeneration_VerfuegungNotrecht_Title";
	private static final String VERFUEGUNG_INTRO =
		"PdfGeneration_Verfuegung_Intro";
	private static final String BEGRUESSUNG =
		"PdfGeneration_Verfuegung_Begruessung";
	private static final String INHALT_1A =
		"PdfGeneration_Verfuegung_Inhalt_1A";
	private static final String INHALT_1B =
		"PdfGeneration_Verfuegung_Inhalt_1B";
	private static final String INHALT_1C =
		"PdfGeneration_Verfuegung_Inhalt_1C";
	private static final String INHALT_1D =
		"PdfGeneration_Verfuegung_Inhalt_1D";
	private static final String INHALT_1E =
		"PdfGeneration_Verfuegung_Inhalt_1E";
	private static final String INHALT_1F =
		"PdfGeneration_Verfuegung_Inhalt_1F";
	private static final String INHALT_1G =
		"PdfGeneration_Verfuegung_Inhalt_1G";
	private static final String INHALT_2A =
		"PdfGeneration_Verfuegung_Inhalt_2A";
	private static final String INHALT_2B =
		"PdfGeneration_Verfuegung_Inhalt_2B";
	private static final String INHALT_2C =
		"PdfGeneration_Verfuegung_Inhalt_2C";
	private static final String INHALT_2C_POSITIV =
		"PdfGeneration_Verfuegung_Inhalt_2C_Positiv";
	private static final String INHALT_2C_NEGATIV =
		"PdfGeneration_Verfuegung_Inhalt_2C_Negativ";
	private static final String FUSSZEILE_1 =
		"PdfGeneration_Verfuegung_Fusszeile_1";
	private static final String FUSSZEILE_2 =
		"PdfGeneration_Verfuegung_Fusszeile_2";
	private static final String FUSSZEILE_3 =
		"PdfGeneration_Verfuegung_Fusszeile_3";
	private static final String FUSSZEILE_4 =
		"PdfGeneration_Verfuegung_Fusszeile_4";
	private static final String FUSSZEILE_5 =
		"PdfGeneration_Verfuegung_Fusszeile_5";
	private static final String FUSSZEILE_6 =
		"PdfGeneration_Verfuegung_Fusszeile_6";
	private static final String FUSSZEILE_7 =
		"PdfGeneration_Verfuegung_Fusszeile_7";
	private static final String GRUNDEN =
		"PdfGeneration_Verfuegung_Grunden";
	private static final String VERFUEGT =
		"PdfGeneration_Verfuegung_Verfuegt";
	private static final String GRUND_1 =
		"PdfGeneration_Verfuegung_Grund_1";
	private static final String GRUND_2_POSITIV =
		"PdfGeneration_Verfuegung_Grund_Positiv";
	private static final String GRUND_2_NEGATIV =
		"PdfGeneration_Verfuegung_Grund_Negativ";
	private static final String BEGRUESSUNG_ENDE =
		"PdfGeneration_Verfuegung_Begruessung_End";
	private static final String BEGRUESSUNG_AMT =
		"PdfGeneration_Verfuegung_Begruessung_Amt";
	private static final String VORSTEHERIN =
		"PdfGeneration_Verfuegung_Vorsteherin";
	private static final String RECHTSMITTELBELEHRUNG_TITLE =
		"PdfGeneration_Verfuegung_Rechtmittelbelehrung_Title";
	private static final String RECHTSMITTELBELEHRUNG =
		"PdfGeneration_Verfuegung_Rechtmittelbelehrung";
	private static final String EMPFAENGER_ADRESSE_GSI =
		"PdfGeneration_Empfaenger_Adresse_GSI";
	private static final String STANDARD_VERFUEGUNGSBEMERKUNG =
		"PdfGeneration_Standard_Verfuegungsbemerkung";

	private final RueckforderungFormular rueckforderungFormular;
	private final InstitutionStammdaten institutionStammdaten;
	private static final int superTextSize = 6;
	private static final int superTextRise = 4;
	private final String nameVerantwortlichePerson;
	private BigDecimal voraussichtlicheAusfallentschaedigung; // A
	private BigDecimal gewaehrteAusfallentschaedigung; // B
	private BigDecimal entschaedigungStufe1; // C
	private BigDecimal relevanterBetrag; // D

	public RueckforderungPrivateVerfuegungPdfGenerator(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull String nameVerantwortlichePerson
	) {
		super(rueckforderungFormular.getKorrespondenzSprache());
		this.institutionStammdaten = rueckforderungFormular.getInstitutionStammdaten();
		this.rueckforderungFormular = rueckforderungFormular;
		this.nameVerantwortlichePerson = nameVerantwortlichePerson;

		// sollten nicht null sein, es handelt sich aber einer gewissen stufe um pflichtfelder
		Objects.requireNonNull(rueckforderungFormular.getStufe2VoraussichtlicheBetrag());
		Objects.requireNonNull(rueckforderungFormular.getStufe2VerfuegungBetrag());
		Objects.requireNonNull(rueckforderungFormular.getStufe1FreigabeBetrag());

		this.voraussichtlicheAusfallentschaedigung = rueckforderungFormular.getStufe2VoraussichtlicheBetrag();
		this.gewaehrteAusfallentschaedigung = rueckforderungFormular.getStufe2VerfuegungBetrag();
		this.entschaedigungStufe1 = rueckforderungFormular.getStufe1FreigabeBetrag();
		this.relevanterBetrag = MathUtil.DEFAULT.subtractNullSafe(gewaehrteAusfallentschaedigung, entschaedigungStufe1);
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return "";
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		final List<String> empfaengerAdresse = new ArrayList<>();

		//Es muss bei der Kanton Adresse anfangen: 4 leere Zeilen
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add(translate(EMPFAENGER_ADRESSE_GSI));
		empfaengerAdresse.add("");
		empfaengerAdresse.add(translate(EINSCHREIBEN));
		empfaengerAdresse.add("");
		Adresse adresse = institutionStammdaten.getAdresse();
		empfaengerAdresse.add(adresse.getAddressAsString());
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add(Constants.DATE_FORMATTER.format(LocalDate.now()));
		return empfaengerAdresse;
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			createContent(document, generator);
		};
	}

	private void createContent(
		@Nonnull final Document document,
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator) throws DocumentException {

		createErsteSeite(document);
		createFusszeileErsteSeite(generator.getDirectContent());

		document.newPage();

		createHeaderSecondPage(generator.getDirectContent());
		if (MathUtil.isNegative(relevanterBetrag)) {
			// der negative betrag wird positiv, da es sich nun um eine rückzahlung handelt
			relevanterBetrag = MathUtil.DEFAULT.multiply(relevanterBetrag, BigDecimal.valueOf(-1));
			createZweiteSeiteRueckzahlung(document);
		} else {
			createZweiteSeite(document);
		}
		createSignatur(document);

		document.newPage();

		createHeaderSecondPage(generator.getDirectContent());
		createDritteSeite(document);
		createFusszeileDritteSeite(generator.getDirectContent());
	}

	private void createSignatur(Document document) {

		if (sprache.equals(Locale.GERMAN)) {
			Paragraph empty = PdfUtil.createParagraph("", 2);
			Paragraph begruessungEnde = PdfUtil.createParagraph(translate(BEGRUESSUNG_ENDE));
			Paragraph begruessungAmt = PdfUtil.createParagraph(translate(BEGRUESSUNG_AMT), 4);

			document.add(empty);
			document.add(begruessungEnde);
			document.add(begruessungAmt);
		} else {
			Paragraph begruessungEnde = PdfUtil.createParagraph(translate(BEGRUESSUNG_ENDE), 3);
			Paragraph begruessungAmt = PdfUtil.createParagraph(
				translate(BEGRUESSUNG_AMT),
				4
			);
			document.add(begruessungEnde);
			document.add(begruessungAmt);
		}

		Paragraph signaturEnde = PdfUtil.createParagraph(nameVerantwortlichePerson + "\n" + translate(VORSTEHERIN));
		document.add(signaturEnde);
	}

	private void createHeaderSecondPage(PdfContentByte directContent) {
		createContentWhereIWant(directContent, "Kanton Bern", 765, 20,
			getPageConfiguration().getFontBold()
			, 10);
		createContentWhereIWant(directContent, "Canton de Berne", 755, 20,
			getPageConfiguration().getFontBold()
			, 10);
		createContentWhereIWant(directContent, translate(VERFUEGUNG_TITLE), 765, 122,
			getPageConfiguration().getFont(),
			6.5f);
		createContentWhereIWant(directContent, translate(VERFUEGUNG_INTRO), 750, 122, getPageConfiguration().getFont(),
			6.5f);
	}

	private void createErsteSeite(Document document) {
		Paragraph title = PdfUtil.createParagraph(translate(VERFUEGUNG_TITLE), 2, PdfUtil.FONT_H2);
		title.setSpacingAfter(15);
		Paragraph intro = PdfUtil.createParagraph(translate(VERFUEGUNG_INTRO), 2, PdfUtil.FONT_H2);
		intro.setSpacingAfter(30);
		document.add(title);
		document.add(intro);
		document.add(PdfUtil.createParagraph(translate(BEGRUESSUNG)));
		// Absatz 1 mit Fusszeilen erstellen
		Paragraph paragraph1 = PdfUtil.createParagraph(translate(INHALT_1A), 1);
		paragraph1.add(PdfUtil.createSuperTextInText("1", superTextSize, superTextRise));
		paragraph1.add(new Chunk(translate(INHALT_1B)));
		paragraph1.add(PdfUtil.createSuperTextInText("2", superTextSize, superTextRise));
		document.add(paragraph1);

		Paragraph paragraph2 = PdfUtil.createParagraph(translate(INHALT_1C), 1);
		paragraph2.add(PdfUtil.createSuperTextInText("3", superTextSize, superTextRise));
		document.add(paragraph2);

		Paragraph paragraph3 = PdfUtil.createParagraph(translate(INHALT_1D), 1);
		paragraph3.add(PdfUtil.createSuperTextInText("4", superTextSize, superTextRise));
		paragraph3.add(new Chunk(translate(INHALT_1E)));
		paragraph3.add(PdfUtil.createSuperTextInText("5", superTextSize, superTextRise));
		paragraph3.add(new Chunk(translate(INHALT_1F)));
		document.add(paragraph3);

		Paragraph paragraph4 = PdfUtil.createParagraph(translate(INHALT_1G), 1);
		paragraph4.add(PdfUtil.createSuperTextInText("6", superTextSize, superTextRise));
		document.add(paragraph4);
	}

	private void createZweiteSeite(Document document) {

		document.add(PdfUtil.createParagraph(translate(INHALT_2A, this.voraussichtlicheAusfallentschaedigung)));

		String verfuegungsbemerkung;
		if (StringUtils.isNotEmpty(rueckforderungFormular.getBemerkungFuerVerfuegung())) {
			verfuegungsbemerkung = rueckforderungFormular.getBemerkungFuerVerfuegung();
		} else {
			verfuegungsbemerkung = translate(STANDARD_VERFUEGUNGSBEMERKUNG);
		}
		document.add(PdfUtil.createParagraph(verfuegungsbemerkung, 1));

		document.add(PdfUtil.createParagraph(translate(INHALT_2B, gewaehrteAusfallentschaedigung), 1));

		final Paragraph paragraph = PdfUtil.createParagraph(translate(INHALT_2C, entschaedigungStufe1, relevanterBetrag));
		paragraph.add(new Chunk(translate(INHALT_2C_POSITIV)));
		document.add(paragraph);

		document.add(PdfUtil.createParagraph(translate(GRUNDEN), 0));
		document.add(PdfUtil.createParagraph(translate(VERFUEGT), 1, PdfUtil.DEFAULT_FONT_BOLD));

		List<String> verfuegtList = new ArrayList();
		verfuegtList.add(translate(GRUND_1, this.gewaehrteAusfallentschaedigung));
		verfuegtList.add(translate(GRUND_2_POSITIV, this.relevanterBetrag));
		document.add(PdfUtil.createListOrdered(verfuegtList));

		document.add(PdfUtil.createParagraph("", 2));
	}

	private void createZweiteSeiteRueckzahlung(Document document) {

		document.add(PdfUtil.createParagraph(translate(INHALT_2A, voraussichtlicheAusfallentschaedigung), 1));

		String verfuegungsbemerkung;
		if (StringUtils.isNotEmpty(rueckforderungFormular.getBemerkungFuerVerfuegung())) {
			verfuegungsbemerkung = rueckforderungFormular.getBemerkungFuerVerfuegung();
		} else {
			verfuegungsbemerkung = translate(STANDARD_VERFUEGUNGSBEMERKUNG);
		}
		document.add(PdfUtil.createParagraph(verfuegungsbemerkung, 1));

		document.add(PdfUtil.createParagraph(translate(INHALT_2B, gewaehrteAusfallentschaedigung), 1));

		final Paragraph paragraph = PdfUtil.createParagraph(translate(INHALT_2C, entschaedigungStufe1, relevanterBetrag));
		paragraph.add(new Chunk(translate(INHALT_2C_NEGATIV)));
		document.add(paragraph);

		document.add(PdfUtil.createParagraph(translate(GRUNDEN)));
		document.add(PdfUtil.createParagraph(translate(VERFUEGT), 1, PdfUtil.DEFAULT_FONT_BOLD));

		List<String> verfuegtList = new ArrayList();
		verfuegtList.add(translate(GRUND_1, this.gewaehrteAusfallentschaedigung));
		verfuegtList.add(translate(GRUND_2_NEGATIV, this.relevanterBetrag));
		document.add(PdfUtil.createListOrdered(verfuegtList));

		document.add(PdfUtil.createParagraph("", 2));
	}

	private void createDritteSeite(Document document) {
		Paragraph title = PdfUtil.createBoldParagraph(translate(RECHTSMITTELBELEHRUNG_TITLE),0);
		Paragraph belehrung = PdfUtil.createParagraph(translate(RECHTSMITTELBELEHRUNG), 2);
		belehrung.add(PdfUtil.createSuperTextInText("7", superTextSize, superTextRise));
		document.add(title);
		document.add(belehrung);
	}

	private void createFusszeileErsteSeite(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1), translate(FUSSZEILE_2), translate(FUSSZEILE_3),
				translate(FUSSZEILE_4), translate(FUSSZEILE_5), translate(FUSSZEILE_6)),
			0, 0
		);
	}

	private void createFusszeileDritteSeite(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_7)),
			0, 6
		);
	}
}

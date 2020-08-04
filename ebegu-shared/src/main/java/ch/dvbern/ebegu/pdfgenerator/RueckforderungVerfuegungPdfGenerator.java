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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

public class RueckforderungVerfuegungPdfGenerator extends MandantPdfGenerator {

	private static final String PROVISORISCHE_VERFUEGUNG_TITLE =
		"PdfGeneration_ProvisorischeVerfuegung_Title";
	private static final String VERFUEGUNG_INTRO =
		"PdfGeneration_ProvisorischeVerfuegung_Intro";
	private static final String BEGRUESSUNG =
		"PdfGeneration_ProvisorischeVerfuegung_Begruessung";
	private static final String INHALT_1 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_1";
	private static final String INHALT_2 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_2";
	private static final String INHALT_3 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_3";
	private static final String INHALT_4 =
		"PdfGeneration_ProvisorischeVerfuegung_Inhalt_4";
	private static final String FUSSZEILE_1 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_1";
	private static final String FUSSZEILE_2 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_2";
	private static final String FUSSZEILE_3 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_3";
	private static final String FUSSZEILE_4 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_4";
	private static final String FUSSZEILE_5 =
		"PdfGeneration_ProvisorischeVerfuegung_Fusszeile_5";

	private final RueckforderungFormular rueckforderungFormular;
	private final InstitutionStammdaten institutionStammdaten;
	private final boolean isProvisorisch;

	public RueckforderungVerfuegungPdfGenerator(RueckforderungFormular rueckforderungFormular,
		boolean provisorisch) {
		super();
		this.institutionStammdaten = rueckforderungFormular.getInstitutionStammdaten();
		this.rueckforderungFormular = rueckforderungFormular;
		this.isProvisorisch = provisorisch;
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
		if (!isProvisorisch) {
			empfaengerAdresse.add(translate(EINSCHREIBEN));
		}
		//Es muss bei der Kanton Adresse anfangen: 4 leere Zeilen
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add("");
		empfaengerAdresse.add(institutionStammdaten.getInstitution().getName());
		Adresse adresse = institutionStammdaten.getAdresse();
		empfaengerAdresse.add(adresse.getAddressAsString());
		empfaengerAdresse.add("");
		empfaengerAdresse.add("Versand per Email an " + this.institutionStammdaten.getMail());
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

	public void createContent(
		@Nonnull final Document document,
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator) throws DocumentException {

		createErsteSeite(document);
		createFusszeileNichtEintreten(generator.getDirectContent());
		document.newPage();

		createHeaderSecondPage(generator.getDirectContent());

		document.add(PdfUtil.createParagraph(translate("Auf diesen Gründen wird")));


	}

	private void createHeaderSecondPage(PdfContentByte directContent) {
		createContentWhereIWant(directContent, "Kanton Bern", 775, 20,
			getPageConfiguration().getFontBold()
			, 10);
		createContentWhereIWant(directContent, "Canton de Berne", 765, 20,
			getPageConfiguration().getFontBold()
			, 10);
		createContentWhereIWant(directContent, translate(PROVISORISCHE_VERFUEGUNG_TITLE), 775, 122, getPageConfiguration().getFont(),
			6.5f);
		createContentWhereIWant(directContent, translate(VERFUEGUNG_INTRO), 760, 122, getPageConfiguration().getFont(),
			6.5f);
	}

	private void createErsteSeite(Document document) {
		Paragraph title = new Paragraph(translate(PROVISORISCHE_VERFUEGUNG_TITLE), PdfUtil.FONT_H2);
		title.setSpacingAfter(15);
		Paragraph intro = new Paragraph(translate(VERFUEGUNG_INTRO), PdfUtil.FONT_H2);
		intro.setSpacingAfter(30);
		document.add(title);
		document.add(intro);
		document.add(PdfUtil.createParagraph(translate(BEGRUESSUNG)));
		document.add(PdfUtil.createParagraph(translate(INHALT_1)));
		document.add(PdfUtil.createParagraph(translate(INHALT_2)));
		document.add(PdfUtil.createParagraph(translate(INHALT_3,
			this.rueckforderungFormular.getStufe2VoraussichtlicheBetrag())));
		document.add(PdfUtil.createParagraph(translate(INHALT_4,
			this.rueckforderungFormular.getStufe1FreigabeBetrag())));
	}

	private void createFusszeileNichtEintreten(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(FUSSZEILE_1), translate(FUSSZEILE_2), translate(FUSSZEILE_3),
				translate(FUSSZEILE_4), translate(FUSSZEILE_5))
		);
	}
}

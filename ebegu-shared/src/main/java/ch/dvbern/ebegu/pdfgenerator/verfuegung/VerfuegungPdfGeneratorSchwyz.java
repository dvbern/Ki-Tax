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

import java.awt.Color;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
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

public class VerfuegungPdfGeneratorSchwyz extends AbstractVerfuegungPdfGenerator {
	private static final String NICHT_EINTRETEN_CONTENT_9 = "PdfGeneration_NichtEintreten_Content_9";

	public VerfuegungPdfGeneratorSchwyz(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration) {
		super(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}

	@Nonnull
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

	@Nonnull
	@Override
	protected PdfPTable createVerfuegungTable() {
		final List<VerfuegungZeitabschnitt> zeitabschnitte = getVerfuegungZeitabschnitt();
		VerfuegungTable verfuegungTable = new VerfuegungTable(
			zeitabschnitte,
			getPageConfiguration()
		);

		verfuegungTable
			.add(createVonColumn())
			.add(createBisColumn())
			.add(createPensumGroup())
			.add(createVollkostenColumn())
			.add(createGutscheinOhneMinimalbeitragColumn())
			.add(createElternbeitragColumn());

		addEinstellungDependingColumns(verfuegungTable, zeitabschnitte);

		return verfuegungTable.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createVollkostenColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(VOLLKOSTEN))
			.romanNumber("IV")
			.width(100)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVollkosten()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinOhneMinimalbeitragColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_MINIMALBEITRAG))
			.romanNumber("V")
			.bgColor(Color.LIGHT_GRAY)
			.boldContent(true)
			.width(100)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createElternbeitragColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(ELTERNBEITRAG))
			.romanNumber("VI")
			.bgColor(Color.LIGHT_GRAY)
			.width(108)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getMinimalerElternbeitragGekuerzt()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinInstitutionColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_INSTITUTION))
			.romanNumber("VII")
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnInstitution(abschnitt)))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinElternColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_ELTERN))
			.romanNumber("VII")
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnEltern(abschnitt)))
			.build();
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

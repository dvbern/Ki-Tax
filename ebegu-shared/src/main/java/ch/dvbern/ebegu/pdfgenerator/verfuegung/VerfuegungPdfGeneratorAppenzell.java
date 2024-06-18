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
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

public class VerfuegungPdfGeneratorAppenzell extends AbstractVerfuegungPdfGenerator {

	private boolean auszahlungAusserhalbKibon = false;

	protected static final String VERFUEGUNG_NICHT_EINTRETEN_TITLE = "PdfGeneration_Verfuegung_NichtEintreten_Title";
	private static final String BEITRAGSHOHE_PROZENT = "PdfGeneration_Verfuegung_Beitragshoehe_Prozent";
	private static final String SELBSTBEHALT_PROZENT = "PdfGeneration_Verfuegung_Selbstbehalt_Prozent";
	private static final String ZUSATZTEXT_1 = "PdfGeneration_Verfuegung_Zusatztext_AR_1";
	private static final String ZUSATZTEXT_1_AUSSERHALB_KIBON = "PdfGeneration_Verfuegung_Zusatztext_AR_1_AUSSERHALB_KIBON";
	private static final String ZUSATZTEXT_2 = "PdfGeneration_Verfuegung_Zusatztext_AR_2";
	private static final String ZUSATZTEXT_3 = "PdfGeneration_Verfuegung_Zusatztext_AR_3";

	public VerfuegungPdfGeneratorAppenzell(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration
	) {
		super(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
		auszahlungAusserhalbKibon = betreuung.extractGesuch().getFamiliensituationContainer().getFamiliensituationJA().isAuszahlungAusserhalbVonKibon();
	}

	@Override
	protected void createDokumentNichtEintretten(
		@Nonnull Document document,
		@Nonnull PdfGenerator generator) {
		document.add(createNichtEingetretenParagraph1());
		document.add(createAntragEingereichtAmParagraph());
		document.add(createNichtEintretenUnterlagenUnvollstaendigParagraph());

		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_4, 2)));
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_5, 2)));
		document.add(PdfUtil.createParagraph(translate(NICHT_EINTRETEN_CONTENT_6, 2)));

		var eingangsdatum = getEingangsdatum();

		document.add(PdfUtil.createParagraph(translate(
				NICHT_EINTRETEN_CONTENT_7,
				Constants.DATE_FORMATTER.format(eingangsdatum)
		)));
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
			.add(createSelbstbehaltColumn())
			.add(createBeitragshoeheColumn())
			.add(createGutscheinInstitutionColumn());

		return verfuegungTable.build();
	}

	@Nonnull
	private VerfuegungTableColumn createSelbstbehaltColumn() {
		return VerfuegungTableColumn.builder()
			.width(100)
			.title(translate(SELBSTBEHALT_PROZENT))
			.bgColor(Color.LIGHT_GRAY)
			.dataExtractor(zeitabschnitt -> PdfUtil.printPercent(
					MathUtil.GANZZAHL.subtract(BigDecimal.valueOf(100),
						MathUtil.GANZZAHL.from(zeitabschnitt.getBeitraghoheProzent()))))
			.build();
	}

	@Nonnull
	private VerfuegungTableColumn createBeitragshoeheColumn() {
		return VerfuegungTableColumn.builder()
			.width(100)
			.bgColor(Color.LIGHT_GRAY)
			.title(translate(BEITRAGSHOHE_PROZENT))
			.dataExtractor(zeitabschnitt -> PdfUtil.printPercent(MathUtil.GANZZAHL.from(zeitabschnitt.getBeitraghoheProzent())))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinInstitutionColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_INSTITUTION))
			.bgColor(Color.LIGHT_GRAY)
			.width(110)
			.boldContent(true)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnInstitution(abschnitt)))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createPensumAnspruchKonkretColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_BG))
			.width(88)
			.dataExtractor(this::printVerguenstigt)
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createPensumAnspruchColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_ANSPRUCH))
			.width(88)
			.dataExtractor(this::printAnspruch)
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createPensumBetreuungColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(PENSUM_BETREUUNG))
			.width(88)
			.dataExtractor(this::printEffektiv)
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createElternbeitragColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(ELTERNBEITRAG))
			.bgColor(Color.LIGHT_GRAY)
			.width(108)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getElternbeitrag()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinOhneVollkostenColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_OHNE_BERUECKSICHTIGUNG_VOLLKOSTEN))
			.width(100)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVerguenstigungOhneBeruecksichtigungVollkosten()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createVollkostenColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(VOLLKOSTEN))
			.width(88)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVollkosten()))
			.build();
	}


	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelleBold;
	}

	@Override
	protected void addAngebotToIntro(List<TableRowLabelValue> intro) {
		//no-op, wird in Appenzell nicht angezeigt
	}

	@Override
	protected void addInstitutionToIntro(String institutionName, List<TableRowLabelValue> intro) {
		if (auszahlungAusserhalbKibon) {
			return;
		}
		intro.add(new TableRowLabelValue(BETREUUNG_INSTITUTION, institutionName));
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> getVerfuegungZeitabschnitt() {
		return super.getZeitabschnitteOrderByGueltigAb(false);
	}

	@Override
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
		//no-op: wird in Appenzell nicht angezeigt
	}

	@Nonnull
	@Override
	protected Paragraph createFirstParagraph(Kind kind) {
		return PdfUtil.createParagraph(translate(
				VERFUEGUNG_CONTENT_1,
				kind.getFullName(),
				Constants.DATE_FORMATTER.format(kind.getGeburtsdatum())), 2);
	}

	@Override
	protected void addZusatzTextIfAvailable(Document document) {
		if (auszahlungAusserhalbKibon) {
			document.add(PdfUtil.createParagraph(translate(ZUSATZTEXT_1_AUSSERHALB_KIBON)));
		} else {
			document.add(PdfUtil.createParagraph(translate(ZUSATZTEXT_1)));
		}
		document.add(PdfUtil.createBoldParagraph(translate(ZUSATZTEXT_2), 1));
		document.add(PdfUtil.createParagraph(translate(ZUSATZTEXT_3), 2));
		super.addZusatzTextIfAvailable(document);
	}
}

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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;

public class VerfuegungPdfGeneratorLuzern extends AbstractVerfuegungPdfGenerator {

	private static final String GUTSCHEIN_PRO_STUNDE = "PdfGeneration_Verfuegung_GutscheinProStunde";

	private boolean isBetreuungTagesfamilie = false;

	public VerfuegungPdfGeneratorLuzern(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration
	) {
		super(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
		isBetreuungTagesfamilie = betreuung.isAngebotTagesfamilien();
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

		createDokumentNichtEintrettenDefault(document, generator);
	}


	@Nonnull
	@Override
	protected PdfPTable createVerfuegungTable() {
		final List<VerfuegungZeitabschnitt> zeitabschnitte = getVerfuegungZeitabschnitt();
		VerfuegungTable verfuegungTable = new VerfuegungTable(
			zeitabschnitte,
			getPageConfiguration(),
			false
		);

		verfuegungTable
			.add(createVonColumn())
			.add(createBisColumn())
			.add(createPensumGroup())
			.add(createVollkostenColumn());

		if (isBetreuungTagesfamilie) {
			verfuegungTable.add(createGutscheinProStundeColumn());
		}


		addEinstellungDependingColumns(verfuegungTable, zeitabschnitte);

		return verfuegungTable.build();
	}

	@Nonnull
	private VerfuegungTableColumn createGutscheinProStundeColumn() {
		return VerfuegungTableColumn.builder()
			.width(100)
			.title(translate(GUTSCHEIN_PRO_STUNDE))
			.bgColor(Color.LIGHT_GRAY)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(abschnitt.getVerguenstigungProZeiteinheit()))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinElternColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_ELTERN))
			.bgColor(Color.LIGHT_GRAY)
			.width(108)
			.boldContent(true)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnEltern(abschnitt)))
			.build();
	}

	@Override
	@Nonnull
	protected VerfuegungTableColumn createGutscheinInstitutionColumn() {
		return VerfuegungTableColumn.builder()
			.title(translate(GUTSCHEIN_AN_INSTITUTION))
			.bgColor(Color.LIGHT_GRAY)
			.boldContent(true)
			.width(108)
			.dataExtractor(abschnitt -> PdfUtil.printBigDecimal(getVerguenstigungAnInstitution(abschnitt)))
			.build();
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

	@Override
	protected void addSuperTextForKeinAnspruchAbschnitt(Paragraph paragraph) {
		//no-op in Luzern soll kein SuperText angezeigt werden, da keine Fussnote existiert
	}
}

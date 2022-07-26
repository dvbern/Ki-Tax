/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;

public class FerienbetreuungReportPdfGenerator extends MandantPdfGenerator {

	protected static final String REFERENZNUMMER = "PdfGeneration_Referenznummer";
	protected static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	protected static final String EINSCHREIBEN = "PdfGeneration_VerfuegungEingeschrieben";
	protected static final String BETREUUNG_INSTITUTION = "PdfGeneration_Institution";


	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	protected final GemeindeAntrag gemeindeAntrag;

	@Nonnull
	private GemeindeStammdaten gemeindeStammdaten;

	protected Locale sprache;

	public FerienbetreuungReportPdfGenerator(@Nonnull GemeindeAntrag gemeindeAntrag, @Nonnull GemeindeStammdaten gemeindeStammdaten) {
		super(Sprache.DEUTSCH, gemeindeAntrag.getGemeinde().getMandant());
		this.gemeindeAntrag = gemeindeAntrag;
		this.gemeindeStammdaten = gemeindeStammdaten;
		initLocale(gemeindeStammdaten);
		initGenerator(gemeindeStammdaten);
	}

	private void initLocale(@Nonnull GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length > 0) {
			sprache = korrespondenzsprachen[0].getLocale();
		}
	}

	private void initGenerator(@Nonnull GemeindeStammdaten stammdaten) {
		this.pdfGenerator = PdfGenerator.create(stammdaten.getGemeindeStammdatenKorrespondenz(), getAbsenderAdresse(), false);
	}

	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.addAll(getGemeindeAdresse());
		absender.addAll(getGemeindeKontaktdaten());
		return absender;
	}

	@Nonnull
	protected List<String> getGemeindeAdresse() {
		List<String> gemeindeHeader = Arrays.asList(
				""
		);
		return gemeindeHeader;
	}

	@Nonnull
	protected List<String> getGemeindeKontaktdaten() {
		return Arrays.asList(
				"",
				""
		);
	}

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	@Nonnull
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(PdfUtil.createParagraph("hello"));
		};
	}
	@Nonnull
	protected String getDocumentTitle() {
		return "test";
	}

	@Nonnull
	protected List<String> getEmpfaengerAdresse() {
		return List.of("");
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}
}

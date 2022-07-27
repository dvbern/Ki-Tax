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
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.pdfTable.SimplePDFTable;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang.StringUtils;

public class FerienbetreuungReportPdfGenerator extends MandantPdfGenerator {

	protected static final String AM_ANGEBOT_BETEILIGTE_GEMEINDEN = "PdfGeneration_amAngebotBeteiligteGemeinden";
	protected static final String STAMMDATEN = "PdfGeneration_stammdaten";
	protected static final String SEIT_WANN_FB = "PdfGeneration_seitWannFerienbetreuungen";
	protected static final String TRAEGERSCHAFT = "Reports_traegerschaftTitle";
	protected static final String KONTAKTPERSON = "PdfGeneration_kontaktperson";
	protected static final String ADRESSE = "PdfGeneration_adresse";
	protected static final String AUSZAHLUNG = "PdfGeneration_auszahlung";

	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	protected final FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer;

	@Nonnull
	private GemeindeStammdaten gemeindeStammdaten;

	protected Locale sprache;

	public FerienbetreuungReportPdfGenerator(@Nonnull FerienbetreuungAngabenContainer gemeindeAntrag, @Nonnull GemeindeStammdaten gemeindeStammdaten) {
		super(Sprache.DEUTSCH, gemeindeAntrag.getGemeinde().getMandant());
		this.ferienbetreuungAngabenContainer = gemeindeAntrag;
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

	private PdfPTable createTableStammdaten() {
		FerienbetreuungAngabenStammdaten stammdaten = (Objects.requireNonNull(ferienbetreuungAngabenContainer.isInBearbeitungGemeinde() ?
				ferienbetreuungAngabenContainer.getAngabenDeklaration() :
				ferienbetreuungAngabenContainer.getAngabenKorrektur())).getFerienbetreuungAngabenStammdaten();
		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(STAMMDATEN, mandant), "");
		table.addRow(
				translate(AM_ANGEBOT_BETEILIGTE_GEMEINDEN, mandant),
				stammdaten.getAmAngebotBeteiligteGemeinden().stream().reduce((a, b) -> a + ", " + b).get());
		table.addRow(
				translate(SEIT_WANN_FB, mandant),
				stammdaten.getSeitWannFerienbetreuungen() != null ? stammdaten.getSeitWannFerienbetreuungen().toString() : ""
		);
		table.addRow(
				translate(TRAEGERSCHAFT, mandant),
				stammdaten.getTraegerschaft()
		);
		table.addRow(
				translate(ADRESSE, mandant),
				stammdaten.getStammdatenAdresse() != null ? stammdaten.getStammdatenAdresse().getAddressAsString() : ""
		);
		table.addRow(
				translate(KONTAKTPERSON, mandant),
				getKontaktpersonAsString(stammdaten)
		);
		table.addRow(
				translate(AUSZAHLUNG, mandant),
				getFBAuszahlungAsString(stammdaten)
		);

		return table.createTable();
	}

	private String getFBAuszahlungAsString(FerienbetreuungAngabenStammdaten stammdaten) {
		if (stammdaten.getAuszahlungsdaten() == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(stammdaten.getAuszahlungsdaten().getAuszahlungsdatenAsString());
		if (StringUtils.isNotEmpty(stammdaten.getVermerkAuszahlung())) {
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getVermerkAuszahlung());
		}
		return sb.toString();
	}

	public String getKontaktpersonAsString(FerienbetreuungAngabenStammdaten stammdaten) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(stammdaten.getStammdatenKontaktpersonVorname())) {
			sb.append(stammdaten.getStammdatenKontaktpersonVorname());
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getStammdatenKontaktpersonNachname());
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getStammdatenKontaktpersonFunktion());
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getStammdatenKontaktpersonEmail());
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getStammdatenKontaktpersonTelefon());
		}
		return sb.toString();
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
			document.add(this.createTableStammdaten());
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

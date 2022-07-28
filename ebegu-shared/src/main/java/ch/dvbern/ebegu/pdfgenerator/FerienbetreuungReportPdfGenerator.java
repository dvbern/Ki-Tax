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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
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

	protected static final String ANGEBOT = "PdfGeneration_angebot";
	protected static final String NAME_ANGEBOT = "PdfGeneration_nameAngebot";
	protected static final String FERIENWOCHEN_HERBST = "PdfGeneration_ferienwochenHerbst";
	protected static final String FERIENWOCHEN_WINTER = "PdfGeneration_ferienwochenWinter";
	protected static final String FERIENWOCHEN_SPORT = "PdfGeneration_ferienwochenSport";
	protected static final String FERIENWOCHEN_FRUEHLING = "PdfGeneration_ferienwochenFruehling";
	protected static final String FERIENWOCHEN_SOMMER = "PdfGeneration_ferienwochenSommer";
	protected static final String TAGE_IM_SCHULJAHR = "PdfGeneration_tageImSchuljahr";
	protected static final String ANGEBOT_BEMERKUNG = "PdfGeneration_angebotBemerkung";
	protected static final String STUNDEN_PRO_BETREUUNGSTAG = "PdfGeneration_stundenProBetreuungstag";
	protected static final String BETREUUNG_AUSSCHLIESSLICH_TAGSUEBER =
			"PdfGeneration_betreungAusschliesslichTagsueber";
	protected static final String OEFFNUNGSZEITEN_BEMEKERUNG = "PdfGeneration_oeffnungszeitenBemerkung";
	protected static final String KOOPERATION_GEMEINDEN = "PdfGeneration_kooperationGemeinden";
	protected static final String GEMEINDE_FUEHRT_SELBER = "PdfGeneration_gemeindeFuehrtSelber";
	protected static final String GEMEINDE_FUEHRT_KOOPERATION = "PdfGeneration_gemeindeFuehrtKooperation";
	protected static final String GEMEINDE_BEAUFTRAGT = "PdfGeneration_gemeindeBeauftragt";
	protected static final String GEMEINDE_INTEGRIERT = "PdfGeneration_gemeindeIntegriert";
	protected static final String KOOPERATION_BEMERKUNG = "PdfGeneration_kooperationBemerkung";
	protected static final String LEITUNG_AUSGEBILDET = "PdfGeneration_leitungAusgebildet";
	protected static final String BETREUUNGSPERSONEN_EIGNUNG = "PdfGeneration_betreuungspersonenEignung";
	protected static final String ANZAHL_KINDER_ANGEMESSEN = "PdfGeneration_anzahlKinderAngemessen";
	protected static final String FERIENBETREUUNG_SCHLUESSEL = "PdfGeneration_ferienbetreuungSchluessel";
	protected static final String PERSONAL_QUALITAET_BEMERKUNG = "PdfGeneration_personalQualitaetBemerkung";
	protected static final String UNTERSCHIEDLICHE_TARIFSYSTEME = "PdfGeneration_unterschiedlicheTarifsysteme";
	protected static final String RABATT_WIRTSCHAFTLICHE_SITUATION = "PdfGeneration_rabattWirtschaftlicheSituation";
	protected static final String TARIF_TS_FUER_FB = "PdfGeneration_tarifTsFuerFB";
	protected static final String TARIF_FB_AUS_TS_AGELEITET = "PdfGeneration_tarifFbAusTsAgeleitet";
	protected static final String ANDERER_TARIF_ANDERE_GEMEINDE = "PdfGeneration_andererTarifAndereGemeinde";
	protected static final String TARIFSYSTEM_BEMERKUNGEN = "PdfGeneration_tarifsystemBemerkungen";
	private static final float TABLE_SPACING_AFTER = 20;

	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	protected final FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer;

	@Nonnull
	private GemeindeStammdaten gemeindeStammdaten;

	protected Locale sprache;

	public FerienbetreuungReportPdfGenerator(
			@Nonnull FerienbetreuungAngabenContainer gemeindeAntrag,
			@Nonnull GemeindeStammdaten gemeindeStammdaten) {
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
		this.pdfGenerator =
				PdfGenerator.create(stammdaten.getGemeindeStammdatenKorrespondenz(), getAbsenderAdresse(), false);
	}

	private PdfPTable createTableStammdaten() {
		FerienbetreuungAngabenStammdaten stammdaten =
				(Objects.requireNonNull(ferienbetreuungAngabenContainer.isInBearbeitungGemeinde() ?
						ferienbetreuungAngabenContainer.getAngabenDeklaration() :
						ferienbetreuungAngabenContainer.getAngabenKorrektur())).getFerienbetreuungAngabenStammdaten();
		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(STAMMDATEN, mandant), "");
		table.addRow(
				translate(AM_ANGEBOT_BETEILIGTE_GEMEINDEN, mandant),
				stammdaten.getAmAngebotBeteiligteGemeinden().stream().reduce((a, b) -> a + ", " + b).get());
		table.addRow(
				translate(SEIT_WANN_FB, mandant),
				stammdaten.getSeitWannFerienbetreuungen() != null ?
						stammdaten.getSeitWannFerienbetreuungen().toString() :
						""
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

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private PdfPTable createTableAngebot() {
		FerienbetreuungAngabenAngebot angebot =
				(Objects.requireNonNull(ferienbetreuungAngabenContainer.isInBearbeitungGemeinde() ?
						ferienbetreuungAngabenContainer.getAngabenDeklaration() :
						ferienbetreuungAngabenContainer.getAngabenKorrektur())).getFerienbetreuungAngabenAngebot();
		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(ANGEBOT, mandant), "");
		table.addRow(
				translate(NAME_ANGEBOT, mandant),
				angebot.getAngebot());
		table.addRow(
				translate(KONTAKTPERSON, mandant),
				getKontaktpersonAsString(angebot));
		table.addRow(
				translate(FERIENWOCHEN_HERBST, mandant),
				getIntValue(angebot.getAnzahlFerienwochenHerbstferien()));
		table.addRow(
				translate(FERIENWOCHEN_WINTER, mandant),
				getIntValue(angebot.getAnzahlFerienwochenWinterferien()));
		table.addRow(
				translate(FERIENWOCHEN_SPORT, mandant),
				getIntValue(angebot.getAnzahlFerienwochenSportferien()));
		table.addRow(
				translate(FERIENWOCHEN_FRUEHLING, mandant),
				getIntValue(angebot.getAnzahlFerienwochenFruehlingsferien()));
		table.addRow(
				translate(FERIENWOCHEN_SOMMER, mandant),
				getIntValue(angebot.getAnzahlFerienwochenSommerferien()));
		table.addRow(
				translate(TAGE_IM_SCHULJAHR, mandant),
				getIntValue(angebot.getAnzahlTage()));
		table.addRow(
				translate(ANGEBOT_BEMERKUNG, mandant),
				angebot.getBemerkungenAnzahlFerienwochen());

		table.addRow(
				translate(STUNDEN_PRO_BETREUUNGSTAG, mandant),
				angebot.getAnzahlStundenProBetreuungstag());
		table.addRow(
				translate(BETREUUNG_AUSSCHLIESSLICH_TAGSUEBER, mandant),
				getBooleanAsString(angebot.getBetreuungErfolgtTagsueber()));
		table.addRow(
				translate(OEFFNUNGSZEITEN_BEMEKERUNG, mandant),
				angebot.getBemerkungenOeffnungszeiten());

		table.addRow(
				translate(AM_ANGEBOT_BETEILIGTE_GEMEINDEN, mandant),
				angebot.getFinanziellBeteiligteGemeinden().stream().reduce((a, b) -> a + ", " + b).orElse(""));
		table.addRow(
				translate(GEMEINDE_FUEHRT_SELBER, mandant),
				getBooleanAsString(angebot.getGemeindeFuehrtAngebotSelber()));
		table.addRow(
				translate(GEMEINDE_FUEHRT_KOOPERATION, mandant),
				getBooleanAsString(angebot.getGemeindeFuehrtAngebotInKooperation()));
		table.addRow(
				translate(GEMEINDE_BEAUFTRAGT, mandant),
				getBooleanAsString(angebot.getGemeindeBeauftragtExterneAnbieter()));
		table.addRow(
				translate(GEMEINDE_INTEGRIERT, mandant),
				getBooleanAsString(angebot.getAngebotVereineUndPrivateIntegriert()));
		table.addRow(
				translate(KOOPERATION_BEMERKUNG, mandant),
				angebot.getBemerkungenKooperation());

		table.addRow(
				translate(LEITUNG_AUSGEBILDET, mandant),
				getBooleanAsString(angebot.getLeitungDurchPersonMitAusbildung()));
		table.addRow(
				translate(BETREUUNGSPERSONEN_EIGNUNG, mandant),
				getBooleanAsString(angebot.getBetreuungDurchPersonenMitErfahrung()));
		table.addRow(
				translate(ANZAHL_KINDER_ANGEMESSEN, mandant),
				getBooleanAsString(angebot.getAnzahlKinderAngemessen()));
		table.addRow(
				translate(FERIENBETREUUNG_SCHLUESSEL, mandant),
				angebot.getBetreuungsschluessel());
		table.addRow(
				translate(PERSONAL_QUALITAET_BEMERKUNG, mandant),
				angebot.getBemerkungenPersonal());

		table.addRow(
				translate(UNTERSCHIEDLICHE_TARIFSYSTEME, mandant),
				getBooleanAsString(angebot.getEinkommensabhaengigerTarifKinderDerGemeinde()));
		table.addRow(
				translate(RABATT_WIRTSCHAFTLICHE_SITUATION, mandant),
				getBooleanAsString(angebot.getFixerTarifKinderDerGemeinde()));
		table.addRow(
				translate(TARIF_TS_FUER_FB, mandant),
				getBooleanAsString(angebot.getTagesschuleTarifGiltFuerFerienbetreuung()));
		table.addRow(
				translate(TARIF_FB_AUS_TS_AGELEITET, mandant),
				getBooleanAsString(angebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet()));
		table.addRow(
				translate(ANDERER_TARIF_ANDERE_GEMEINDE, mandant),
				angebot.getKinderAusAnderenGemeindenZahlenAnderenTarif() != null ?
						translate("KinderAusAnderenGemeindenZahlenAnderenTarifAnswer_" + angebot.getKinderAusAnderenGemeindenZahlenAnderenTarif().name(), mandant) :
						"");
		table.addRow(
				translate(TARIFSYSTEM_BEMERKUNGEN, mandant),
				angebot.getBemerkungenTarifsystem()
		);

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	@Nullable
	private Integer getIntValue(@Nullable BigDecimal value) {
		return value == null ? null : value.intValue();
	}

	private String getBooleanAsString(@Nullable Boolean value) {
		if (value == null) {
			return "";
		}
		if (Boolean.TRUE.equals(value)) {
			return translate("label_true", mandant);
		}
		return translate("label_false", mandant);
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

	public String getKontaktpersonAsString(FerienbetreuungAngabenAngebot stammdaten) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(stammdaten.getAngebotKontaktpersonVorname())) {
			sb.append(stammdaten.getAngebotKontaktpersonVorname());
			sb.append(Constants.LINE_BREAK);
			sb.append(stammdaten.getAngebotKontaktpersonNachname());
			if (stammdaten.getAngebotAdresse() != null) {
				sb.append(Constants.LINE_BREAK);
				sb.append(stammdaten.getAngebotAdresse().getAddressAsString());
			}
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
			document.add(this.createTableAngebot());
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

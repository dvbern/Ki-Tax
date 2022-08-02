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
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.pdfTable.SimplePDFTable;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang.StringUtils;

public class FerienbetreuungReportPdfGenerator extends MandantPdfGenerator {

	protected static final String FERIENBETREUUNG_TITLE = "PdfGeneration_ferienbetruungTitle";

	protected static final String AM_ANGEBOT_BETEILIGTE_GEMEINDEN = "PdfGeneration_amAngebotBeteiligteGemeinden";
	protected static final String STAMMDATEN = "PdfGeneration_stammdaten";
	protected static final String SEIT_WANN_FB = "PdfGeneration_seitWannFerienbetreuungen";
	protected static final String TRAEGERSCHAFT = "Reports_traegerschaftTitle";
	protected static final String KONTAKTPERSON = "PdfGeneration_kontaktperson";
	protected static final String AUSZAHLUNG = "PdfGeneration_auszahlung";
	protected static final String GEMEINDE = "Reports_gemeindeTitle";
	protected static final String ADRESSE_GEMEINDE = "PdfGeneration_adresseGemeinde";

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
	protected static final String TARIFSYSTEM = "PdfGeneration_tarifsystem";
	protected static final String PERSONAL_UND_QUALITAET = "PdfGeneration_personalUndQualitaet";
	protected static final String KOOPERATION = "PdfGeneration_kooperation";
	protected static final String OEFFNUNGSZEITEN = "PdfGeneration_oeffnungszeiten";

	protected static final String NUTZUNG = "PdfGeneration_nutzung";
	protected static final String ANZAHL_BETREUUNGSTAGE = "PdfGeneration_anzahlBetreuungstage";
	protected static final String DAVON_AUS_ANBIETENDER_GEMEINDE = "PdfGeneration_davonAusAnbietenderGemeinde";
	protected static final String DAVON_BEDARF_VOLKSSCHULANGEBOT = "PdfGeneration_davonBedarfVolksschulangebot";
	protected static final String DAVON_AUS_ANDERER_GEMEINDE = "PdfGeneration_davonAusAndererGemeinde";
	protected static final String ANZAHL_BETREUTE_KINDER = "PdfGeneration_anzahlBetreuteKinder";
	protected static final String DAVON_BEDARF_KINDER_VOLKSSCHULANGEBOT = "PdfGeneration_davonBedarfKinderVolksschulangebot";
	protected static final String DAVON_1_ZYKLUS = "PdfGeneration_davon1Zyklus";
	protected static final String DAVON_2_ZYKLUS = "PdfGeneration_davon2Zyklus";
	protected static final String DAVON_3_ZYKLUS = "PdfGeneration_davon3Zyklus";
	protected static final String BETREUUNGSTAGE = "PdfGeneration_betreuungstage";
	protected static final String KINDER = "Reports_kinderTitle";

	protected static final String KOSTEN_EINNAHMEN = "PdfGeneration_kostenEinnahmen";
	protected static final String EINNAHMEN = "PdfGeneration_einnahmen";
	protected static final String KOSTEN = "PdfGeneration_kosten";
	protected static final String PERSONALKOSTEN = "PdfGeneration_personalkosten";
	protected static final String DAVON_LEITUNG_ADMINISTRATION = "PdfGeneration_davonLeitungAdministration";
	protected static final String SACHKOSTEN = "PdfGeneration_sachkosten";
	protected static final String VERPFLEGUNGSKOSTEN = "PdfGeneration_verpflegungskosten";
	protected static final String WEITERE_KOSTEN = "PdfGeneration_weitereKosten";
	protected static final String BEMERKUNGEN_KOSTEN = "PdfGeneration_bemerkungenKosten";
	protected static final String EINNAHMEN_ELTERNBEITRAEGE = "PdfGeneration_einnahmenElternbeitraege";
	protected static final String WEITERE_EINNAHMEN = "PdfGeneration_weitereEinnahmen";


	private static final float TABLE_SPACING_AFTER = 20;
	private static final float SUB_HEADER_SPACING_AFTER = 10;

	@Nonnull
	private NoAdressPdfGenerator pdfGenerator;

	@Nonnull
	protected final FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer;

	protected Locale sprache;

	public FerienbetreuungReportPdfGenerator(
			@Nonnull FerienbetreuungAngabenContainer gemeindeAntrag,
			@Nonnull GemeindeStammdaten gemeindeStammdaten) {
		super(Sprache.DEUTSCH, gemeindeAntrag.getGemeinde().getMandant());
		this.ferienbetreuungAngabenContainer = gemeindeAntrag;
		initLocale(gemeindeStammdaten);
		initGenerator();
	}

	private void initLocale(@Nonnull GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length > 0) {
			sprache = korrespondenzsprachen[0].getLocale();
		}
	}

	private void initGenerator() {
		this.pdfGenerator =
				NoAdressPdfGenerator.create();
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
				translate(ADRESSE_GEMEINDE, mandant),
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

		table.addHeaderRow(" ", "");
		table.addRow(
				translate(NAME_ANGEBOT, mandant),
				angebot.getAngebot());
		table.addHeaderRow(translate(KONTAKTPERSON, mandant), "");
		table.addRow(
				translate(KONTAKTPERSON, mandant),
				getKontaktpersonAsString(angebot));
		table.addHeaderRow(translate(ANGEBOT, mandant), "");
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

		table.addHeaderRow(translate(OEFFNUNGSZEITEN, mandant), "");
		table.addRow(
				translate(STUNDEN_PRO_BETREUUNGSTAG, mandant),
				angebot.getAnzahlStundenProBetreuungstag());
		table.addRow(
				translate(BETREUUNG_AUSSCHLIESSLICH_TAGSUEBER, mandant),
				getBooleanAsString(angebot.getBetreuungErfolgtTagsueber()));
		table.addRow(
				translate(OEFFNUNGSZEITEN_BEMEKERUNG, mandant),
				angebot.getBemerkungenOeffnungszeiten());

		table.addHeaderRow(translate(KOOPERATION, mandant), "");
		table.addRow(
				translate(KOOPERATION_GEMEINDEN, mandant),
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

		table.addHeaderRow(translate(PERSONAL_UND_QUALITAET, mandant), "");
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

		table.addHeaderRow(translate(TARIFSYSTEM, mandant), "");
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

	private PdfPTable createTableNutzung() {
		FerienbetreuungAngabenNutzung nutzung =
				(Objects.requireNonNull(ferienbetreuungAngabenContainer.isInBearbeitungGemeinde() ?
						ferienbetreuungAngabenContainer.getAngabenDeklaration() :
						ferienbetreuungAngabenContainer.getAngabenKorrektur())).getFerienbetreuungAngabenNutzung();

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);

		table.addHeaderRow(translate(BETREUUNGSTAGE, mandant), "");
		table.addRow(
				translate(ANZAHL_BETREUUNGSTAGE, mandant),
				nutzung.getAnzahlBetreuungstageKinderBern());
		table.addRow(
				translate(DAVON_AUS_ANBIETENDER_GEMEINDE, mandant),
				nutzung.getBetreuungstageKinderDieserGemeinde());
		table.addRow(
				translate(DAVON_BEDARF_VOLKSSCHULANGEBOT, mandant),
				nutzung.getBetreuungstageKinderDieserGemeindeSonderschueler());
		table.addRow(
				translate(DAVON_AUS_ANDERER_GEMEINDE, mandant),
				nutzung.getDavonBetreuungstageKinderAndererGemeinden());
		table.addRow(
				translate(DAVON_BEDARF_VOLKSSCHULANGEBOT, mandant),
				nutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());

		table.addHeaderRow(translate(KINDER, mandant), "");
		table.addRow(
				translate(ANZAHL_BETREUTE_KINDER, mandant),
				getIntValue(nutzung.getAnzahlBetreuteKinder()));
		table.addRow(
				translate(DAVON_BEDARF_KINDER_VOLKSSCHULANGEBOT, mandant),
				getIntValue(nutzung.getAnzahlBetreuteKinderSonderschueler()));
		table.addRow(
				translate(DAVON_1_ZYKLUS, mandant),
				getIntValue(nutzung.getAnzahlBetreuteKinder1Zyklus()));
		table.addRow(
				translate(DAVON_2_ZYKLUS, mandant),
				getIntValue(nutzung.getAnzahlBetreuteKinder2Zyklus()));
		table.addRow(
				translate(DAVON_3_ZYKLUS, mandant),
				getIntValue(nutzung.getAnzahlBetreuteKinder3Zyklus()));

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private PdfPTable createTableKostenEinnahmen() {
		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen =
				(Objects.requireNonNull(ferienbetreuungAngabenContainer.isInBearbeitungGemeinde() ?
						ferienbetreuungAngabenContainer.getAngabenDeklaration() :
						ferienbetreuungAngabenContainer.getAngabenKorrektur())).getFerienbetreuungAngabenKostenEinnahmen();

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);

		table.addHeaderRow(translate(EINNAHMEN, mandant), "");

		table.addRow(
				translate(PERSONALKOSTEN, mandant),
				kostenEinnahmen.getPersonalkosten());
		table.addRow(
				translate(DAVON_LEITUNG_ADMINISTRATION, mandant),
				kostenEinnahmen.getPersonalkostenLeitungAdmin());
		table.addRow(
				translate(SACHKOSTEN, mandant),
				kostenEinnahmen.getSachkosten());
		table.addRow(
				translate(VERPFLEGUNGSKOSTEN, mandant),
				kostenEinnahmen.getVerpflegungskosten());
		table.addRow(
				translate(WEITERE_KOSTEN, mandant),
				kostenEinnahmen.getWeitereKosten());
		table.addRow(
				translate(BEMERKUNGEN_KOSTEN, mandant),
				kostenEinnahmen.getBemerkungenKosten());

		table.addHeaderRow(translate(KOSTEN, mandant), "");
		table.addRow(
				translate(EINNAHMEN_ELTERNBEITRAEGE, mandant),
				kostenEinnahmen.getElterngebuehren());
		table.addRow(
				translate(WEITERE_EINNAHMEN, mandant),
				kostenEinnahmen.getWeitereEinnahmen());

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

	private String getKontaktpersonAsString(FerienbetreuungAngabenStammdaten stammdaten) {
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

	private String getKontaktpersonAsString(FerienbetreuungAngabenAngebot stammdaten) {
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

			Paragraph stammdatenHeader = new Paragraph(translate(STAMMDATEN, mandant));
			stammdatenHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(stammdatenHeader);
			document.add(this.createTableStammdaten());

			Paragraph angebotHeader = new Paragraph(translate(ANGEBOT, mandant));
			angebotHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(angebotHeader);
			document.add(this.createTableAngebot());

			Paragraph nutzungHeader = new Paragraph(translate(NUTZUNG, mandant));
			nutzungHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(nutzungHeader);
			document.add(this.createTableNutzung());

			Paragraph kostenEinnahmenHeader = new Paragraph(translate(KOSTEN_EINNAHMEN, mandant));
			kostenEinnahmenHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(kostenEinnahmenHeader);
			document.add(this.createTableKostenEinnahmen());
		};
	}

	@Nonnull
	protected String getDocumentTitle() {
		return translate(
				FERIENBETREUUNG_TITLE,
				mandant,
				ferienbetreuungAngabenContainer.getGemeinde().getName(),
				ferienbetreuungAngabenContainer.getGesuchsperiode().getGesuchsperiodeString());
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

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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.pdfTable.SimplePDFTable;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

public class LATSReportPdfGenerator extends MandantPdfGenerator {

	protected static final String LATS_TITLE = "PdfGeneration_latsTitle";

	protected static final String ANGABEN_GEMEINDE = "PdfGeneration_angabenGemeinde";
	protected static final String ALLE_TS_ANMELDUNGEN_IN_KIBON = "PdfGeneration_alleTsAnmeldungenInKibon";
	protected static final String ALLGEMEINE_ANGABEN_GEMEINDE = "PdfGeneration_allgemeineAngabenGemeinde";
	protected static final String BEDARF_TS_ANGEBOT_ELTERN_ABGEKLAERT = "Reports_bedarfTsAngebotElternAbgeklaert";
	protected static final String BESTEHT_ANGEBOT = "PdfGeneration_bestehtAngebot";
	protected static final String ANGEBOT_ALLEN_OFFEN = "PdfGeneration_angebotAllenOffen";

	protected static final String ABRECHNUNG = "PdfGeneration_abrechnung";
	protected static final String BETREUUNGSSTUNDEN_OHNE_BESONDERE_ANFORDERUNGEN =
			"PdfGeneration_betreuungsstundenOhneAnforderungen";
	protected static final String BETREUUNGSSTUNDEN_MIT_BESONDERE_ANFORDERUNGEN =
			"PdfGeneration_betreuungsstundenMitAnforderungen";
	protected static final String BETREUUNGSSTUNDEN_VOLKSSCHULANGEBOT =
			"PdfGeneration_betreuungsstundenVolksschulangebot";
	protected static final String LASTENAUSGLEICHBERECHTIGTE_BETREUUNGSSTUNDEN =
			"PdfGeneration_lastenasugleichsberechtigteBetreuungsstunden";
	protected static final String ZU_NORMLOHNKOSTEN_HOCH = "PdfGeneration_zuNormlohnkostenHoch";
	protected static final String ZU_NORMLOHNKOSTEN_TIEF = "PdfGeneration_zuNormlohnkostenTief";
	protected static final String NORMLOHNKOSTEN_BETREUUNG = "PdfGeneration_normlohnkostenBetreuung";
	protected static final String TATSACHLICHE_EINNAHMEN_ELTERNGEBUEHREN =
			"PdfGeneration_tatsachlicheEinnahmenElterngebuehren";

	protected static final String LATS_BETRAG = "PdfGeneration_latsBetrag";
	protected static final String ERSTE_RATE = "PdfGeneration_ersteRate";
	protected static final String SCHLUSSZAHLUNG = "PdfGeneration_schlusszahlung";

	protected static final String KOSTENBETEILIGUNG_GEMEINDE = "PdfGeneration_kostenbeteiligungGemeinde";
	protected static final String GESAMTKOSTEN_TS = "PdfGeneration_gesamtkostenTS";
	protected static final String EINNAHMEN_LASTENAUSGLEICH = "PdfGeneration_einnahmenLastenausgleich";
	protected static final String EINNAHMEN_ELTERNGEBUEHREN = "PdfGeneration_einnahmenElterngebuehren";
	protected static final String EINNAHMEN_VERPFLEGUNG = "PdfGeneration_einnahmenVerpflegung";
	protected static final String EINNAHMEN_SUBVENTIONEN = "PdfGeneration_einnahmenSubventionen";
	protected static final String KOSTENBEITRAG_GEMEINDE = "PdfGeneration_kostenbeitragGemeinde";
	protected static final String ERTRAGSUBERSCHUSS_GEMEINDE = "PdfGeneration_ertragsuberschussGemeinde";
	protected static final String ERWARTETER_KOSTENBEITRAG_GEMEINDE = "PdfGeneration_erwarteterKostenbeitragGemeinde";
	protected static final String UBERSCHUSS_VORJAHR = "PdfGeneration_uberschussVorjahr";

	protected static final String WEITERE_KOSTEN_ETRAGE = "PdfGeneration_weitereKostenErtrage";
	protected static final String BEMERKUNGEN_KOSTEN_ETRAGE = "PdfGeneration_bemerkungenKostenErtrage";

	protected static final String KONTROLLFRAGEN = "PdfGeneration_kontrollfragen";
	protected static final String BETREUUNGSSTUNDEN_DOKUMENTIERT = "PdfGeneration_betreuungsstundenDokumentiert";
	protected static final String BEMERKUNG = "PdfGeneration_bemerkung";
	protected static final String ELTERNGEBUHREN_GEMAESS_TSVERORDNUNG =
			"PdfGeneration_elterngebuhrenGemaessTSVerordnung";
	protected static final String ELTERN_BELEGE = "PdfGeneration_elternBelege";
	protected static final String ELTERN_MAXTARIF = "PdfGeneration_elternMaxtarif";
	protected static final String HAELFTE_AUSGEBILDET = "PdfGeneration_haelfteAusgebildet";
	protected static final String AUSBILDUNGEN_ZERTIFIZIERT = "PdfGeneration_ausbildungenZertifiziert";
	protected static final String BEMERKUNGEN = "Reports_bemerkungTitle";

	private static final float TABLE_SPACING_AFTER = 20;
	private static final float SUB_HEADER_SPACING_AFTER = 10;

	@Nonnull
	private NoAdressPdfGenerator pdfGenerator;

	@Nonnull
	private final LastenausgleichTagesschuleAngabenGemeindeContainer
			lastenausgleichTagesschuleAngabenGemeindeContainer;

	public LATSReportPdfGenerator(
			@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeAntrag,
			@Nonnull GemeindeStammdaten gemeindeStammdaten) {
		super(Sprache.DEUTSCH, gemeindeAntrag.getGemeinde().getMandant());
		this.lastenausgleichTagesschuleAngabenGemeindeContainer = gemeindeAntrag;
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

	@Override
	@Nonnull
	protected String getDocumentTitle() {
		return translate(
				LATS_TITLE,
				mandant,
				lastenausgleichTagesschuleAngabenGemeindeContainer.getGemeinde().getName(),
				lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode().getGesuchsperiodeString());
	}

	@Override
	@Nonnull
	protected List<String> getEmpfaengerAdresse() {
		return List.of("");
	}

	@Override
	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Override
	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	@Override
	@Nonnull
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			document.add(this.createStatus());

			Paragraph angabenGemeindeHeaeder = new Paragraph(translate(ANGABEN_GEMEINDE, mandant));
			angabenGemeindeHeaeder.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(angabenGemeindeHeaeder);
			document.add(this.createTableAllgemeineAngaben());

			Paragraph abrechnungHeader = new Paragraph(translate(ABRECHNUNG, mandant));
			abrechnungHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(abrechnungHeader);
			document.add(this.createTableAbrechnung());

			Paragraph kostenbeteilugungHeader = new Paragraph(translate(KOSTENBETEILIGUNG_GEMEINDE, mandant));
			kostenbeteilugungHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(kostenbeteilugungHeader);
			document.add(this.createTableKostenbeteiligung());

			Paragraph weitereKostenEtraegeHeader = new Paragraph(translate(WEITERE_KOSTEN_ETRAGE, mandant));
			weitereKostenEtraegeHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(weitereKostenEtraegeHeader);
			document.add(this.createTableWeitereKostenEtraege());

			Paragraph kontrollfragenHeader = new Paragraph(translate(KONTROLLFRAGEN, mandant));
			kontrollfragenHeader.setSpacingAfter(SUB_HEADER_SPACING_AFTER);
			document.add(kontrollfragenHeader);
			document.add(this.createTableKontrollfragen());
		};
	}

	@Nonnull
	private Element createStatus() {
		Paragraph paragraph = new Paragraph(translate("LastenausgleichTagesschuleAngabenGemeindeStatus_"
				+ this.lastenausgleichTagesschuleAngabenGemeindeContainer.getStatusString(), mandant));
		paragraph.setSpacingAfter(TABLE_SPACING_AFTER);
		return paragraph;
	}

	private Element createTableAllgemeineAngaben() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde =
				(Objects.requireNonNull(lastenausgleichTagesschuleAngabenGemeindeContainer.isInBearbeitungGemeinde() ?
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenDeklaration() :
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur()));

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(ALLGEMEINE_ANGABEN_GEMEINDE, mandant), "");
		table.addRow(
				translate(BEDARF_TS_ANGEBOT_ELTERN_ABGEKLAERT, mandant),
				getBooleanAsString(angabenGemeinde.getBedarfBeiElternAbgeklaert()));
		table.addRow(
				translate(BESTEHT_ANGEBOT, mandant, getNextSchuljahrAsString()),
				getBooleanAsString(angabenGemeinde.getAngebotFuerFerienbetreuungVorhanden()));
		table.addRow(
				translate(ANGEBOT_ALLEN_OFFEN, mandant),
				getBooleanAsString(angabenGemeinde.getAngebotVerfuegbarFuerAlleSchulstufen()));

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private Element createTableAbrechnung() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde =
				(Objects.requireNonNull(lastenausgleichTagesschuleAngabenGemeindeContainer.isInBearbeitungGemeinde() ?
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenDeklaration() :
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur()));

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(ABRECHNUNG, mandant), "");
		table.addRow(
				translate(ALLE_TS_ANMELDUNGEN_IN_KIBON, mandant),
				getBooleanAsString(lastenausgleichTagesschuleAngabenGemeindeContainer.getAlleAngabenInKibonErfasst()));
		table.addRow(
				translate(BETREUUNGSSTUNDEN_OHNE_BESONDERE_ANFORDERUNGEN, mandant, getSchuljahrAsString()),
				angabenGemeinde.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse());
		table.addRow(
				translate(BETREUUNGSSTUNDEN_MIT_BESONDERE_ANFORDERUNGEN, mandant, getSchuljahrAsString()),
				angabenGemeinde.getGeleisteteBetreuungsstundenBesondereBeduerfnisse());
		table.addRow(
				translate(BETREUUNGSSTUNDEN_VOLKSSCHULANGEBOT, mandant, getSchuljahrBasisjahrAsString()),
				angabenGemeinde.getGeleisteteBetreuungsstundenBesondereVolksschulangebot());
		table.addRow(
				translate(LASTENAUSGLEICHBERECHTIGTE_BETREUUNGSSTUNDEN, mandant),
				angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden());
		table.addRow(
				translate(ZU_NORMLOHNKOSTEN_HOCH, mandant),
				angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete());
		table.addRow(
				translate(ZU_NORMLOHNKOSTEN_TIEF, mandant),
				angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete());
		table.addRow(
				translate(NORMLOHNKOSTEN_BETREUUNG, mandant),
				angabenGemeinde.getNormlohnkostenBetreuungBerechnet());
		table.addRow(
				translate(TATSACHLICHE_EINNAHMEN_ELTERNGEBUEHREN, mandant, getSchuljahrAsString()),
				angabenGemeinde.getEinnahmenElterngebuehren());

		table.addRow(
				translate(LATS_BETRAG, mandant, getSchuljahrAsString()),
				angabenGemeinde.getLastenausgleichsberechtigerBetrag());
		table.addRow(
				translate(ERSTE_RATE, mandant, getSchuljahrBasisjahrAsString()),
				angabenGemeinde.getErsteRateAusbezahlt());
		table.addRow(
				translate(SCHLUSSZAHLUNG, mandant, getSchuljahrBasisjahrPlus1AsString()),
				angabenGemeinde.getSchlusszahlung());

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private Element createTableKostenbeteiligung() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde =
				(Objects.requireNonNull(lastenausgleichTagesschuleAngabenGemeindeContainer.isInBearbeitungGemeinde() ?
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenDeklaration() :
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur()));

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(KOSTENBETEILIGUNG_GEMEINDE, mandant), "");
		table.addRow(
				translate(GESAMTKOSTEN_TS, mandant, getSchuljahrAsString()),
				angabenGemeinde.getGesamtKostenTagesschule());
		table.addRow(translate(EINNAHMEN_LASTENAUSGLEICH, mandant), "");
		table.addRow(translate(EINNAHMEN_ELTERNGEBUEHREN, mandant), angabenGemeinde.getEinnahmenElterngebuehren());
		table.addRow(translate(EINNAHMEN_VERPFLEGUNG, mandant), angabenGemeinde.getEinnnahmenVerpflegung());
		table.addRow(translate(EINNAHMEN_SUBVENTIONEN, mandant), angabenGemeinde.getEinnahmenSubventionenDritter());
		table.addRow(translate(KOSTENBEITRAG_GEMEINDE, mandant), angabenGemeinde.getKostenbeitragGemeinde());
		table.addRow(translate(ERTRAGSUBERSCHUSS_GEMEINDE, mandant), angabenGemeinde.getUeberschussVerwendung());
		table.addRow(
				translate(ERWARTETER_KOSTENBEITRAG_GEMEINDE, mandant),
				angabenGemeinde.getErwarteterKostenbeitragGemeinde());
		table.addRow(
				translate(UBERSCHUSS_VORJAHR, mandant, getPreviousSchuljahrAsString()),
				getBooleanAsString(angabenGemeinde.getUeberschussErzielt()));

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private Element createTableWeitereKostenEtraege() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde =
				(Objects.requireNonNull(lastenausgleichTagesschuleAngabenGemeindeContainer.isInBearbeitungGemeinde() ?
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenDeklaration() :
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur()));

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(WEITERE_KOSTEN_ETRAGE, mandant), "");
		table.addRow(
				translate(BEMERKUNGEN_KOSTEN_ETRAGE, mandant),
				angabenGemeinde.getBemerkungenWeitereKostenUndErtraege());

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private Element createTableKontrollfragen() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde =
				(Objects.requireNonNull(lastenausgleichTagesschuleAngabenGemeindeContainer.isInBearbeitungGemeinde() ?
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenDeklaration() :
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur()));

		SimplePDFTable table = new SimplePDFTable(pdfGenerator.getConfiguration(), false);
		table.addHeaderRow(translate(KONTROLLFRAGEN, mandant), "");

		table.addRow(
				translate(BETREUUNGSSTUNDEN_DOKUMENTIERT, mandant),
				getBooleanAsString(angabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueft()));
		if (Boolean.FALSE.equals(angabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueft())) {
			table.addRow(
					translate(BEMERKUNG, mandant),
					angabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueftBemerkung());
		}

		table.addRow(
				translate(ELTERNGEBUHREN_GEMAESS_TSVERORDNUNG, mandant),
				getBooleanAsString(angabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnet()));
		if (Boolean.FALSE.equals(angabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnet())) {
			table.addRow(
					translate(BEMERKUNG, mandant),
					angabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnetBemerkung());
		}

		table.addRow(
				translate(ELTERN_BELEGE, mandant),
				getBooleanAsString(angabenGemeinde.getEinkommenElternBelegt()));
		if (Boolean.FALSE.equals(angabenGemeinde.getEinkommenElternBelegt())) {
			table.addRow(translate(BEMERKUNG, mandant), angabenGemeinde.getEinkommenElternBelegtBemerkung());
		}

		table.addRow(translate(ELTERN_MAXTARIF, mandant), getBooleanAsString(angabenGemeinde.getMaximalTarif()));
		if (Boolean.FALSE.equals(angabenGemeinde.getMaximalTarif())) {
			table.addRow(translate(BEMERKUNG, mandant), angabenGemeinde.getMaximalTarifBemerkung());
		}

		table.addRow(
				translate(HAELFTE_AUSGEBILDET, mandant),
				getBooleanAsString(angabenGemeinde.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal()));
		if (Boolean.FALSE.equals(angabenGemeinde.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal())) {
			table.addRow(
					translate(BEMERKUNG, mandant),
					angabenGemeinde.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung());
		}

		table.addRow(
				translate(AUSBILDUNGEN_ZERTIFIZIERT, mandant),
				getBooleanAsString(angabenGemeinde.getAusbildungenMitarbeitendeBelegt()));
		if (Boolean.FALSE.equals(angabenGemeinde.getAusbildungenMitarbeitendeBelegt())) {
			table.addRow(translate(BEMERKUNG, mandant), angabenGemeinde.getAusbildungenMitarbeitendeBelegtBemerkung());
		}

		PdfPTable pdfPTable = table.createTable();
		pdfPTable.setSpacingAfter(TABLE_SPACING_AFTER);

		return pdfPTable;
	}

	private String getPreviousSchuljahrAsString() {
		return this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode().getBasisJahr()
				+ "/"
				+ String.valueOf(this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
				.getBasisJahrPlus1()).substring(2);
	}

	private String getNextSchuljahrAsString() {
		return this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode().getBasisJahrPlus2()
				+ "/"
				+ String.valueOf(this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
				.getBasisJahrPlus2() + 1).substring(2);
	}

	private String getSchuljahrAsString() {
		return this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode().getGesuchsperiodeString();
	}

	private String getSchuljahrBasisjahrAsString() {
		return String.valueOf(this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
				.getBasisJahrPlus1());
	}

	private String getSchuljahrBasisjahrPlus1AsString() {
		return String.valueOf(this.lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
				.getBasisJahrPlus2());
	}

	@Nonnull
	private String getBooleanAsString(@Nullable Boolean value) {
		if (value == null) {
			return "";
		}
		if (Boolean.TRUE.equals(value)) {
			return translate("label_true", mandant);
		}
		return translate("label_false", mandant);
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

}

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
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.pdfTable.SimplePDFTable;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.lang.StringUtils;

public class LATSReportPdfGenerator extends MandantPdfGenerator {

	protected static final String LATS_TITLE = "PdfGeneration_latsTitle";

	protected static final String ANGABEN_GEMEINDE = "PdfGeneration_angabenGemeinde";
	protected static final String ALLE_TS_ANMELDUNGEN_IN_KIBON = "PdfGeneration_alleTsAnmeldungenInKibon";
	protected static final String ALLGEMEINE_ANGABEN_GEMEINDE = "PdfGeneration_allgemeineAngabenGemeinde";
	protected static final String BEDARF_TS_ANGEBOT_ELTERN_ABGEKLAERT = "Reports_bedarfTsAngebotElternAbgeklaert";
	protected static final String BESTEHT_ANGEBOT = "PdfGeneration_bestehtAngebot";
	protected static final String ANGEBOT_ALLEN_OFFEN = "PdfGeneration_angebotAllenOffen";

	protected static final String ABRECHNUNG = "PdfGeneration_abrechnung";
	protected static final String BETREUUNGSSTUNDEN_OHNE_BESONDERE_ANFORDERUNGEN = "PdfGeneration_betreuungsstundenOhneAnforderungen";
	protected static final String BETREUUNGSSTUNDEN_MIT_BESONDERE_ANFORDERUNGEN = "PdfGeneration_betreuungsstundenMitAnforderungen";
	protected static final String BETREUUNGSSTUNDEN_VOLKSSCHULANGEBOT = "PdfGeneration_betreuungsstundenVolksschulangebot";
	protected static final String LASTENAUSGLEICHBERECHTIGTE_BETREUUNGSSTUNDEN = "PdfGeneration_lastenasugleichsberechtigteBetreuungsstunden";
	protected static final String ZU_NORMLOHNKOSTEN_HOCH = "PdfGeneration_zuNormlohnkostenHoch";
	protected static final String ZU_NORMLOHNKOSTEN_TIEF = "PdfGeneration_zuNormlohnkostenTief";
	protected static final String NORMLOHNKOSTEN_BETREUUNG = "PdfGeneration_normlohnkostenBetreuung";
	protected static final String TATSACHLICHE_EINNAHMEN_ELTERNGEBUEHREN = "PdfGeneration_tatsachlicheEinnahmenElterngebuehren";

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
	protected static final String ELTERNGEBUHREN_GEMAESS_TSVERORDNUNG = "PdfGeneration_elterngebuhrenGemaessTSVerordnung";
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
	protected final LastenausgleichTagesschuleAngabenGemeindeContainer
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
		};
	}

	@Nullable
	private Integer getIntValue(@Nullable BigDecimal value) {
		return value == null ? null : value.intValue();
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

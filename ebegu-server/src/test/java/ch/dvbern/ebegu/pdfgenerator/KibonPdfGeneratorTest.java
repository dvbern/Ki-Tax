/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.pdfgenerator.AbstractVerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.FinanzielleSituationPdfGeneratorBern;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.TagesschuleBernRechner;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Erstellt pro Brief ein Exemplar für Einzelpersonen und Paare. Jeweils das Beispiel für Alleinstehende wird als Draft
 * generiert.
 */
@SuppressWarnings("JUnitTestMethodWithNoAssertions") // some tests will check that the file is created. no assertion is needed
public class KibonPdfGeneratorTest extends AbstractBGRechnerTest {

	private GemeindeStammdaten stammdaten;
	private List<DokumentGrund> benoetigteUnterlagen;
	private Gesuch gesuch_alleinstehend;
	private Gesuch gesuch_verheiratet;
	private Gesuch gesuch_tagesschule;

	private Mahnung mahnung_1_Alleinstehend;
	private Mahnung mahnung_1_Verheiratet;
	private Mahnung mahnung_2_Alleinstehend;
	private Mahnung mahnung_2_Verheiratet;

	private final String pfad = FileUtils.getTempDirectoryPath() + "/generated/";

	private static final boolean STADT_BERN_ASIV_CONFIGUERED = false;

	private final TagesschuleBernRechner rechner = new TagesschuleBernRechner();


	@BeforeEach
	public void init() throws IOException {
		final InputStream inputStream = KibonPdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png");
		assertNotNull(inputStream);
		final byte[] gemeindeLogo = IOUtils.toByteArray(inputStream);
		stammdaten = TestDataUtil.createGemeindeWithStammdaten();
		stammdaten.getGemeindeStammdatenKorrespondenz().setLogoContent(gemeindeLogo);
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		Benutzer defaultBenutzer = TestDataUtil.createDefaultBenutzer();
		gesuch_alleinstehend = TestDataUtil.createTestgesuchDagmar(new FinanzielleSituationBernRechner());
		gesuch_verheiratet = TestDataUtil.createTestgesuchYvonneFeuz(new FinanzielleSituationBernRechner());
		gesuch_alleinstehend.getDossier().setVerantwortlicherBG(defaultBenutzer);
		gesuch_verheiratet.getDossier().setVerantwortlicherBG(defaultBenutzer);
		benoetigteUnterlagen = new ArrayList<>();
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.FINANZIELLESITUATION, DokumentTyp.STEUERERKLAERUNG));
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.ERWERBSPENSUM, DokumentTyp.NACHWEIS_ERWERBSPENSUM));
		benoetigteUnterlagen.add(new DokumentGrund(DokumentGrundTyp.ERWEITERTE_BETREUUNG, DokumentTyp.BESTAETIGUNG_ARZT));

		mahnung_1_Alleinstehend =
			TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch_alleinstehend, LocalDate.now().plusDays(10), 5);
		mahnung_1_Verheiratet =
			TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch_verheiratet, LocalDate.now().plusDays(10), 5);
		mahnung_2_Alleinstehend =
			TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch_alleinstehend, LocalDate.now().plusDays(20), 4);
		mahnung_2_Verheiratet =
			TestDataUtil.createMahnung(MahnungTyp.ZWEITE_MAHNUNG, gesuch_verheiratet, LocalDate.now().plusDays(20), 4);

		// Verzeichnis pro Mandant erstellen
		FileUtils.forceMkdir(new File(pfad));
		for (MandantIdentifier mandant : MandantIdentifier.values()) {
			FileUtils.forceMkdir(new File(pfad + '/' + mandant.name()));
		}

		gesuch_tagesschule = TestDataUtil.createTestfall11_SchulamtOnly();
		gesuch_tagesschule.getDossier().setVerantwortlicherTS(defaultBenutzer);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void freigabequittungTest(@Nonnull MandantIdentifier mandant) throws IOException, InvoiceGeneratorException {
		createFreigabequittung(mandant, gesuch_alleinstehend, Sprache.DEUTSCH, "Freigabequittung_alleinstehend_de.pdf");
		createFreigabequittung(mandant, gesuch_alleinstehend, Sprache.FRANZOESISCH,  "Freigabequittung_alleinstehend_fr.pdf");
		createFreigabequittung(mandant, gesuch_verheiratet, Sprache.DEUTSCH,  "Freigabequittung_verheiratet_de.pdf");
		createFreigabequittung(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH,  "Freigabequittung_verheiratet_fr.pdf");
	}

	private void createFreigabequittung(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws InvoiceGeneratorException, IOException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final FreigabequittungPdfGenerator generator = new FreigabequittungPdfGenerator(gesuch, stammdaten, benoetigteUnterlagen);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void musterPdfTest(@Nonnull MandantIdentifier mandant) throws IOException, InvoiceGeneratorException {
		createMusterPdf(mandant);
	}

	public void createMusterPdf(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		assertNotNull(gesuch_alleinstehend.getGesuchsteller1());
		final MusterPdfGenerator generator = new MusterPdfGenerator(stammdaten);
		generateTestDocument(generator, mandant, "MusterPdf");
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void begleitschreibenTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createBegleitschreiben(mandant, gesuch_alleinstehend, Sprache.DEUTSCH, "Begleitschreiben_alleinstehend_de.pdf");
		createBegleitschreiben(mandant, gesuch_alleinstehend, Sprache.FRANZOESISCH,  "Begleitschreiben_alleinstehend_fr.pdf");
		createBegleitschreiben(mandant, gesuch_verheiratet, Sprache.DEUTSCH,  "Begleitschreiben_verheiratet_de.pdf");
		createBegleitschreiben(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH,  "Begleitschreiben_verheiratet_fr.pdf");
	}

	private void createBegleitschreiben(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException, InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final BegleitschreibenPdfGenerator generator = new BegleitschreibenPdfGenerator(gesuch, stammdaten);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void normaleVerfuegungTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createNormaleVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.DEUTSCH, "Verfügung_alleinstehend_de.pdf");
		createNormaleVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "Verfügung_alleinstehend_fr.pdf");
		createNormaleVerfuegung(mandant, gesuch_verheiratet, false, Sprache.DEUTSCH, "Verfügung_verheiratet_de.pdf");
		createNormaleVerfuegung(mandant, gesuch_verheiratet, false, Sprache.FRANZOESISCH, "Verfügung_verheiratet_fr.pdf");
	}

	private void createNormaleVerfuegung(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		evaluator.evaluate(gesuch, getParameter(), TestDataUtil.geKitaxUebergangsloesungParameter(), Constants.DEFAULT_LOCALE);
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			Objects.requireNonNull(betreuung.getVerfuegungOrVerfuegungPreview());
			betreuung.getVerfuegungOrVerfuegungPreview().setManuelleBemerkungen("Dies ist eine Test-Bemerkung");
		}
		final VerfuegungPdfGeneratorBern generator = new VerfuegungPdfGeneratorBern(
			getFirstBetreuung(gesuch), stammdaten, AbstractVerfuegungPdfGenerator.Art.NORMAL, entwurfMitKontingentierung, STADT_BERN_ASIV_CONFIGUERED, false);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void keinAnspruchVerfuegungTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createKeinAnspruchVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.DEUTSCH, "KeinAnspruchVerfügung_alleinstehend_de.pdf");
		createKeinAnspruchVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "KeinAnspruchVerfügung_alleinstehend_fr.pdf");
		createKeinAnspruchVerfuegung(mandant, gesuch_verheiratet, false, Sprache.DEUTSCH, "KeinAnspruchVerfügung_verheiratet_de.pdf");
		createKeinAnspruchVerfuegung(mandant, gesuch_verheiratet, false, Sprache.FRANZOESISCH, "KeinAnspruchVerfügung_verheiratet_fr.pdf");
	}

	private void createKeinAnspruchVerfuegung(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final VerfuegungPdfGeneratorBern generator = new VerfuegungPdfGeneratorBern(
			getFirstBetreuung(gesuch), stammdaten, Art.KEIN_ANSPRUCH, entwurfMitKontingentierung, STADT_BERN_ASIV_CONFIGUERED, false);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void nichtEintretenVerfuegungTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createNichtEintretenVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.DEUTSCH, "NichtEintretenVerfügung_alleinstehend_de.pdf");
		createNichtEintretenVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "NichtEintretenVerfügung_alleinstehend_fr.pdf");
		createNichtEintretenVerfuegung(mandant, gesuch_verheiratet, false, Sprache.DEUTSCH, "NichtEintretenVerfügung_verheiratet_de.pdf");
		createNichtEintretenVerfuegung(mandant, gesuch_verheiratet, false, Sprache.FRANZOESISCH, "NichtEintretenVerfügung_verheiratet_fr.pdf");
	}

	private void createNichtEintretenVerfuegung(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale,
		@Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final VerfuegungPdfGeneratorBern generator = new VerfuegungPdfGeneratorBern(
			getFirstBetreuung(gesuch), stammdaten, Art.NICHT_EINTRETTEN, entwurfMitKontingentierung, STADT_BERN_ASIV_CONFIGUERED, false);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void finanzielleSituationTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		// FinSit Typ Bern basic
		createFinanzielleSituation(mandant, gesuch_alleinstehend, Sprache.DEUTSCH, "FinanzielleSituation_alleinstehend_de.pdf");
		createFinanzielleSituation(mandant, gesuch_alleinstehend, Sprache.FRANZOESISCH, "FinanzielleSituation_alleinstehend_fr.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_de.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fr.pdf");
		gesuch_verheiratet.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		// FinSit Typ Bern FKJV, FKJV Feldern sind null
		assertNotNull(gesuch_verheiratet.getGesuchsteller1());
		assertNotNull(gesuch_verheiratet.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch_verheiratet.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettoVermoegen(new BigDecimal(1000));
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS1_de.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS1_fr.pdf");
		assertNotNull(gesuch_verheiratet.getGesuchsteller2());
		assertNotNull(gesuch_verheiratet.getGesuchsteller2().getFinanzielleSituationContainer());
		gesuch_verheiratet.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettoVermoegen(new BigDecimal(1000));
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS1GS2_de.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS1GS2_fr.pdf");
		gesuch_verheiratet.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettoVermoegen(null);
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS2_de.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fkjv_nettolohnGS2_fr.pdf");
	}

	private void createFinanzielleSituation(@Nonnull MandantIdentifier mandant, @Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final FinanzielleSituationPdfGeneratorBern generator = new FinanzielleSituationPdfGeneratorBern(gesuch, getFamiliensituationsVerfuegung(gesuch), stammdaten,  Constants.START_OF_TIME,
			new FinanzielleSituationBernRechner());
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void mahnung1Test(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createMahnung1(mandant, mahnung_1_Alleinstehend, Sprache.DEUTSCH, "Mahnung1_alleinstehend_de.pdf");
		createMahnung1(mandant, mahnung_1_Alleinstehend, Sprache.FRANZOESISCH, "Mahnung1_alleinstehend_fr.pdf");
		createMahnung1(mandant, mahnung_1_Verheiratet, Sprache.DEUTSCH, "Mahnung1_verheiratet_de.pdf");
		createMahnung1(mandant, mahnung_1_Verheiratet, Sprache.FRANZOESISCH, "Mahnung1_verheiratet_fr.pdf");
	}

	private void createMahnung1(@Nonnull MandantIdentifier mandant, @Nonnull Mahnung mahnung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(mahnung.getGesuch().getGesuchsteller1());
		mahnung.getGesuch().getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final MahnungPdfGenerator generator = new ErsteMahnungPdfGenerator(mahnung, stammdaten);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void mahnung2Test(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		createMahnung2(mandant, mahnung_2_Alleinstehend, Sprache.DEUTSCH, "Mahnung2_alleinstehend_de.pdf");
		createMahnung2(mandant, mahnung_2_Alleinstehend, Sprache.FRANZOESISCH, "Mahnung2_alleinstehend_fr.pdf");
		createMahnung2(mandant, mahnung_2_Verheiratet, Sprache.DEUTSCH, "Mahnung2_verheiratet_de.pdf");
		createMahnung2(mandant, mahnung_2_Verheiratet, Sprache.FRANZOESISCH, "Mahnung2_verheiratet_fr.pdf");
	}

	private void createMahnung2(@Nonnull MandantIdentifier mandant, @Nonnull Mahnung mahnung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(mahnung.getGesuch().getGesuchsteller1());
		mahnung.getGesuch().getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final MahnungPdfGenerator generator = new ZweiteMahnungPdfGenerator(mahnung, mahnung_1_Alleinstehend, stammdaten);
		generateTestDocument(generator, mandant, dokumentname);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void createAnmeldebestaetigungenTagesschule(@Nonnull MandantIdentifier mandant) throws FileNotFoundException, InvoiceGeneratorException {
		createAnmeldebestaetigungenTagesschule(mandant, Sprache.DEUTSCH, AnmeldebestaetigungTSPDFGenerator.Art.OHNE_TARIF, "Anmeldebestaetigung_test_ohneTarif_de.pdf");
		createAnmeldebestaetigungenTagesschule(mandant, Sprache.FRANZOESISCH, AnmeldebestaetigungTSPDFGenerator.Art.OHNE_TARIF, "Anmeldebestaetigung_test_ohneTarif_fr.pdf");
		createAnmeldebestaetigungenTagesschule(mandant, Sprache.DEUTSCH, AnmeldebestaetigungTSPDFGenerator.Art.MIT_TARIF, "Anmeldebestaetigung_test_mitTarif_de.pdf");
		createAnmeldebestaetigungenTagesschule(mandant, Sprache.FRANZOESISCH, AnmeldebestaetigungTSPDFGenerator.Art.MIT_TARIF, "Anmeldebestaetigung_test_mitTarif_fr.pdf");
	}

	public void createAnmeldebestaetigungenTagesschule(@Nonnull MandantIdentifier mandant, @Nonnull Sprache locale, @Nonnull AnmeldebestaetigungTSPDFGenerator.Art art, @Nonnull String dokumentname) throws FileNotFoundException, InvoiceGeneratorException {
		AnmeldungTagesschule anmeldungTagesschule = prepareAnmeldungTagesschuleWithModule();
		assertNotNull(gesuch_tagesschule.getGesuchsteller1());
		gesuch_tagesschule.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final AnmeldebestaetigungTSPDFGenerator generator = new AnmeldebestaetigungTSPDFGenerator(gesuch_tagesschule,
			stammdaten, art, anmeldungTagesschule, false);
		generateTestDocument(generator, mandant, dokumentname);
	}

	private AnmeldungTagesschule prepareAnmeldungTagesschuleWithModule() {
		KindContainer kindContainer = gesuch_tagesschule.getKindContainers().iterator().next();
		AnmeldungTagesschule anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschuleWithModules(kindContainer, gesuch_tagesschule.getGesuchsperiode());
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(anmeldungTagesschule);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			rechner.calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputAsiv(), getParameter());
		}
		Verfuegung verfuegungPreview = new Verfuegung();
		verfuegungPreview.setZeitabschnitte(zeitabschnitte);
		anmeldungTagesschule.setVerfuegungPreview(verfuegungPreview);
		return anmeldungTagesschule;
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void normaleVerfuegungFusszeileTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		stammdaten.setStandardDokSignature(false);
		stammdaten.setStandardDokTitle("RESSORT SOZIALES");
		stammdaten.setStandardDokUnterschriftTitel("Departementsvorsteher Soziales");
		stammdaten.setStandardDokUnterschriftName("Pascal Lerch");
		stammdaten.setStandardDokUnterschriftTitel2("Höhere Sachbearbeiterin Soziales");
		stammdaten.setStandardDokUnterschriftName2("Katja Furrer");
		createNormaleVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.DEUTSCH, "Verfügung_alternativ_fusszeile_de.pdf");
		createNormaleVerfuegung(mandant, gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "Verfügung_alternativ_fusszeile_fr.pdf");
		stammdaten.setStandardDokSignature(true);
	}

	private Betreuung getFirstBetreuung(@Nonnull Gesuch gesuch) {
		return gesuch.extractAllBetreuungen().get(0);
	}

	private Verfuegung getFamiliensituationsVerfuegung(@Nonnull Gesuch gesuch) {
		return evaluator.evaluateFamiliensituation(gesuch, Constants.DEFAULT_LOCALE);
	}

	private void generateTestDocument(@NonNull KibonPdfGenerator generator, @NonNull MandantIdentifier mandant, @NonNull String dokumentname)
		throws FileNotFoundException, InvoiceGeneratorException {
		generator.generate(new FileOutputStream(pfad + mandant + '/' +  dokumentname));
	}
}


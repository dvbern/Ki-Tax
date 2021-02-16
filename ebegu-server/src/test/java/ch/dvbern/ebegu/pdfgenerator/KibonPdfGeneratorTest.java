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
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.VerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.TagesschuleRechner;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

	private String pfad = FileUtils.getTempDirectoryPath() + "/generated/";

	private boolean stadtBernAsivConfiguered = false;

	private TagesschuleRechner rechner = new TagesschuleRechner();


	@Before
	public void init() throws IOException {
		final byte[] gemeindeLogo = IOUtils.toByteArray(KibonPdfGeneratorTest.class.getResourceAsStream("Moosseedorf_gross.png"));
		stammdaten = TestDataUtil.createGemeindeWithStammdaten();
		stammdaten.setLogoContent(gemeindeLogo);
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		Benutzer defaultBenutzer = TestDataUtil.createDefaultBenutzer();
		gesuch_alleinstehend = TestDataUtil.createTestgesuchDagmar();
		gesuch_verheiratet = TestDataUtil.createTestgesuchYvonneFeuz();
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
		FileUtils.forceMkdir(new File(pfad));

		gesuch_tagesschule = TestDataUtil.createTestfall11_SchulamtOnly();
		gesuch_tagesschule.getDossier().setVerantwortlicherTS(defaultBenutzer);
	}

	/*@Test
	public void freigabequittungTest() throws InvoiceGeneratorException, IOException {
		createFreigabequittung(gesuch_alleinstehend, Sprache.DEUTSCH, "Freigabequittung_alleinstehend_de.pdf");
		createFreigabequittung(gesuch_alleinstehend, Sprache.FRANZOESISCH, "Freigabequittung_alleinstehend_fr.pdf");
		createFreigabequittung(gesuch_verheiratet, Sprache.DEUTSCH, "Freigabequittung_verheiratet_de.pdf");
		createFreigabequittung(gesuch_verheiratet, Sprache.FRANZOESISCH, "Freigabequittung_verheiratet_fr.pdf");
	}*/

	private void createFreigabequittung(@Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws InvoiceGeneratorException, IOException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final FreigabequittungPdfGenerator generator = new FreigabequittungPdfGenerator(gesuch, stammdaten, benoetigteUnterlagen);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void begleitschreibenTest() throws InvoiceGeneratorException, IOException {
		createBegleitschreiben(gesuch_alleinstehend, Sprache.DEUTSCH, "Begleitschreiben_alleinstehend_de.pdf");
		createBegleitschreiben(gesuch_alleinstehend, Sprache.FRANZOESISCH, "Begleitschreiben_alleinstehend_fr.pdf");
		createBegleitschreiben(gesuch_verheiratet, Sprache.DEUTSCH, "Begleitschreiben_verheiratet_de.pdf");
		createBegleitschreiben(gesuch_verheiratet, Sprache.FRANZOESISCH, "Begleitschreiben_verheiratet_fr.pdf");
	}

	private void createBegleitschreiben(@Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException, InvoiceGeneratorException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final BegleitschreibenPdfGenerator generator = new BegleitschreibenPdfGenerator(gesuch, stammdaten);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void normaleVerfuegungTest() throws InvoiceGeneratorException, IOException {
		createNormaleVerfuegung(gesuch_alleinstehend, true, Sprache.DEUTSCH, "Verfügung_alleinstehend_de.pdf");
		createNormaleVerfuegung(gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "Verfügung_alleinstehend_fr.pdf");
		createNormaleVerfuegung(gesuch_verheiratet, false, Sprache.DEUTSCH, "Verfügung_verheiratet_de.pdf");
		createNormaleVerfuegung(gesuch_verheiratet, false, Sprache.FRANZOESISCH, "Verfügung_verheiratet_fr.pdf");
	}

	private void createNormaleVerfuegung(@Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		evaluator.evaluate(gesuch, getParameter(), TestDataUtil.geKitaxUebergangsloesungParameter(), Constants.DEFAULT_LOCALE);
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			Objects.requireNonNull(betreuung.getVerfuegungOrVerfuegungPreview());
			betreuung.getVerfuegungOrVerfuegungPreview().setManuelleBemerkungen("Dies ist eine Test-Bemerkung");
		}
		final VerfuegungPdfGenerator generator = new VerfuegungPdfGenerator(
			getFirstBetreuung(gesuch), stammdaten, VerfuegungPdfGenerator.Art.NORMAL, entwurfMitKontingentierung, stadtBernAsivConfiguered);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void keinAnspruchVerfuegungTest() throws InvoiceGeneratorException, IOException {
		createKeinAnspruchVerfuegung(gesuch_alleinstehend, true, Sprache.DEUTSCH, "KeinAnspruchVerfügung_alleinstehend_de.pdf");
		createKeinAnspruchVerfuegung(gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "KeinAnspruchVerfügung_alleinstehend_fr.pdf");
		createKeinAnspruchVerfuegung(gesuch_verheiratet, false, Sprache.DEUTSCH, "KeinAnspruchVerfügung_verheiratet_de.pdf");
		createKeinAnspruchVerfuegung(gesuch_verheiratet, false, Sprache.FRANZOESISCH, "KeinAnspruchVerfügung_verheiratet_fr.pdf");
	}

	private void createKeinAnspruchVerfuegung(@Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final VerfuegungPdfGenerator generator = new VerfuegungPdfGenerator(
			getFirstBetreuung(gesuch), stammdaten, Art.KEIN_ANSPRUCH, entwurfMitKontingentierung, stadtBernAsivConfiguered);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void nichtEintretenVerfuegungTest() throws InvoiceGeneratorException, IOException {
		createNichtEintretenVerfuegung(gesuch_alleinstehend, true, Sprache.DEUTSCH, "NichtEintretenVerfügung_alleinstehend_de.pdf");
		createNichtEintretenVerfuegung(gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "NichtEintretenVerfügung_alleinstehend_fr.pdf");
		createNichtEintretenVerfuegung(gesuch_verheiratet, false, Sprache.DEUTSCH, "NichtEintretenVerfügung_verheiratet_de.pdf");
		createNichtEintretenVerfuegung(gesuch_verheiratet, false, Sprache.FRANZOESISCH, "NichtEintretenVerfügung_verheiratet_fr.pdf");
	}

	private void createNichtEintretenVerfuegung(@Nonnull Gesuch gesuch, boolean entwurfMitKontingentierung, @Nonnull Sprache locale,
		@Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final VerfuegungPdfGenerator generator = new VerfuegungPdfGenerator(
			getFirstBetreuung(gesuch), stammdaten, Art.NICHT_EINTRETTEN, entwurfMitKontingentierung, stadtBernAsivConfiguered);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void finanzielleSituationTest() throws InvoiceGeneratorException, IOException {
		createFinanzielleSituation(gesuch_alleinstehend, Sprache.DEUTSCH, "FinanzielleSituation_alleinstehend_de.pdf");
		createFinanzielleSituation(gesuch_alleinstehend, Sprache.FRANZOESISCH, "FinanzielleSituation_alleinstehend_fr.pdf");
		createFinanzielleSituation(gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_de.pdf");
		createFinanzielleSituation(gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fr.pdf");
	}

	private void createFinanzielleSituation(@Nonnull Gesuch gesuch, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final FinanzielleSituationPdfGenerator generator = new FinanzielleSituationPdfGenerator(gesuch, getFamiliensituationsVerfuegung(gesuch), stammdaten,  Constants.START_OF_TIME);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void mahnung1Test() throws InvoiceGeneratorException, IOException {
		createMahnung1(mahnung_1_Alleinstehend, Sprache.DEUTSCH, "Mahnung1_alleinstehend_de.pdf");
		createMahnung1(mahnung_1_Alleinstehend, Sprache.FRANZOESISCH, "Mahnung1_alleinstehend_fr.pdf");
		createMahnung1(mahnung_1_Verheiratet, Sprache.DEUTSCH, "Mahnung1_verheiratet_de.pdf");
		createMahnung1(mahnung_1_Verheiratet, Sprache.FRANZOESISCH, "Mahnung1_verheiratet_fr.pdf");
	}

	private void createMahnung1(@Nonnull Mahnung mahnung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(mahnung.getGesuch().getGesuchsteller1());
		mahnung.getGesuch().getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final MahnungPdfGenerator generator = new ErsteMahnungPdfGenerator(mahnung, stammdaten);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void mahnung2Test() throws InvoiceGeneratorException, IOException {
		createMahnung2(mahnung_2_Alleinstehend, Sprache.DEUTSCH, "Mahnung2_alleinstehend_de.pdf");
		createMahnung2(mahnung_2_Alleinstehend, Sprache.FRANZOESISCH, "Mahnung2_alleinstehend_fr.pdf");
		createMahnung2(mahnung_2_Verheiratet, Sprache.DEUTSCH, "Mahnung2_verheiratet_de.pdf");
		createMahnung2(mahnung_2_Verheiratet, Sprache.FRANZOESISCH, "Mahnung2_verheiratet_fr.pdf");
	}

	private void createMahnung2(@Nonnull Mahnung mahnung, @Nonnull Sprache locale, @Nonnull String dokumentname) throws FileNotFoundException,
		InvoiceGeneratorException {
		Assert.assertNotNull(mahnung.getGesuch().getGesuchsteller1());
		mahnung.getGesuch().getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final MahnungPdfGenerator generator = new ZweiteMahnungPdfGenerator(mahnung, mahnung_1_Alleinstehend, stammdaten);
		generator.generate(new FileOutputStream(pfad + dokumentname));
	}

	@Test
	public void createAnmeldebestaetigungenTagesschule() throws FileNotFoundException, InvoiceGeneratorException {
		createAnmeldebestaetigungenTagesschule(Sprache.DEUTSCH, AnmeldebestaetigungTSPDFGenerator.Art.OHNE_TARIF, "Anmeldebestaetigung_test_ohneTarif_de.pdf");
		createAnmeldebestaetigungenTagesschule(Sprache.FRANZOESISCH, AnmeldebestaetigungTSPDFGenerator.Art.OHNE_TARIF, "Anmeldebestaetigung_test_ohneTarif_fr.pdf");
		createAnmeldebestaetigungenTagesschule(Sprache.DEUTSCH, AnmeldebestaetigungTSPDFGenerator.Art.MIT_TARIF, "Anmeldebestaetigung_test_mitTarif_de.pdf");
		createAnmeldebestaetigungenTagesschule(Sprache.FRANZOESISCH, AnmeldebestaetigungTSPDFGenerator.Art.MIT_TARIF, "Anmeldebestaetigung_test_mitTarif_fr.pdf");
	}

	public void createAnmeldebestaetigungenTagesschule(@Nonnull Sprache locale, @Nonnull AnmeldebestaetigungTSPDFGenerator.Art art, @Nonnull String dokumentname) throws FileNotFoundException, InvoiceGeneratorException {
		AnmeldungTagesschule anmeldungTagesschule = prepareAnmeldungTagesschuleWithModule();
		Assert.assertNotNull(gesuch_tagesschule.getGesuchsteller1());
		gesuch_tagesschule.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final AnmeldebestaetigungTSPDFGenerator generator = new AnmeldebestaetigungTSPDFGenerator(gesuch_tagesschule,
			stammdaten, art, anmeldungTagesschule, false);
		generator.generate(new FileOutputStream(pfad + dokumentname));
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

	@Test
	public void normaleVerfuegungFusszeileTest() throws InvoiceGeneratorException, IOException {
		stammdaten.setStandardDokSignature(false);
		stammdaten.setStandardDokTitle("RESSORT SOZIALES");
		stammdaten.setStandardDokUnterschriftTitel("Departementsvorsteher Soziales");
		stammdaten.setStandardDokUnterschriftName("Pascal Lerch");
		stammdaten.setStandardDokUnterschriftTitel2("Höhere Sachbearbeiterin Soziales");
		stammdaten.setStandardDokUnterschriftName2("Katja Furrer");
		createNormaleVerfuegung(gesuch_alleinstehend, true, Sprache.DEUTSCH, "Verfügung_alternativ_fusszeile_de.pdf");
		createNormaleVerfuegung(gesuch_alleinstehend, true, Sprache.FRANZOESISCH, "Verfügung_alternativ_fusszeile_fr.pdf");
		stammdaten.setStandardDokSignature(true);
	}

	private Betreuung getFirstBetreuung(@Nonnull Gesuch gesuch) {
		return gesuch.extractAllBetreuungen().get(0);
	}

	private Verfuegung getFamiliensituationsVerfuegung(@Nonnull Gesuch gesuch) {
		return evaluator.evaluateFamiliensituation(gesuch, Constants.DEFAULT_LOCALE);
	}
}


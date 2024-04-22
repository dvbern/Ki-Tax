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

package ch.dvbern.ebegu.pdfgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.FinanzielleSituationPdfGeneratorBern;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FinanzielleSituationPdfTest extends AbstractPDFGeneratorTest {

	private GemeindeStammdaten stammdaten;
	private Gesuch gesuch_alleinstehend;
	private Gesuch gesuch_verheiratet;

	private final String pfad = FileUtils.getTempDirectoryPath() + "/generated/";

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

		// Verzeichnis pro Mandant erstellen
		FileUtils.forceMkdir(new File(pfad));
		for (MandantIdentifier mandant : MandantIdentifier.values()) {
			FileUtils.forceMkdir(new File(pfad + '/' + mandant.name()));
		}

		Gesuch gesuch_tagesschule = TestDataUtil.createTestfall11_SchulamtOnly();
		gesuch_tagesschule.getDossier().setVerantwortlicherTS(defaultBenutzer);
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	public void finanzielleSituationTest(@Nonnull MandantIdentifier mandant) throws InvoiceGeneratorException, IOException {
		// FinSit Typ Bern basic
		createFinanzielleSituation(mandant, gesuch_alleinstehend, Sprache.DEUTSCH, "FinanzielleSituation_alleinstehend_de.pdf");
		createFinanzielleSituation(
			mandant,
			gesuch_alleinstehend,
			Sprache.FRANZOESISCH,
			"FinanzielleSituation_alleinstehend_fr.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.DEUTSCH, "FinanzielleSituation_verheiratet_de.pdf");
		createFinanzielleSituation(mandant, gesuch_verheiratet, Sprache.FRANZOESISCH, "FinanzielleSituation_verheiratet_fr.pdf");
		gesuch_verheiratet.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		// FinSit Typ Bern FKJV, FKJV Feldern sind null
		assertNotNull(gesuch_verheiratet.getGesuchsteller1());
		assertNotNull(gesuch_verheiratet.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch_verheiratet.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettoVermoegen(new BigDecimal(1000));
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.DEUTSCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS1_de.pdf");
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.FRANZOESISCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS1_fr.pdf");
		assertNotNull(gesuch_verheiratet.getGesuchsteller2());
		assertNotNull(gesuch_verheiratet.getGesuchsteller2().getFinanzielleSituationContainer());
		gesuch_verheiratet.getGesuchsteller2()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettoVermoegen(new BigDecimal(1000));
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.DEUTSCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS1GS2_de.pdf");
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.FRANZOESISCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS1GS2_fr.pdf");
		gesuch_verheiratet.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettoVermoegen(null);
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.DEUTSCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS2_de.pdf");
		createFinanzielleSituation(
			mandant,
			gesuch_verheiratet,
			Sprache.FRANZOESISCH,
			"FinanzielleSituation_verheiratet_fkjv_nettolohnGS2_fr.pdf");
	}

	private void createFinanzielleSituation(
		@Nonnull MandantIdentifier mandant,
		@Nonnull Gesuch gesuch,
		@Nonnull Sprache locale,
		@Nonnull String dokumentname) throws
		FileNotFoundException,
		InvoiceGeneratorException {
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(locale);
		final FinanzielleSituationPdfGeneratorBern generator = new FinanzielleSituationPdfGeneratorBern(gesuch,
			getFamiliensituationsVerfuegung(gesuch),
			stammdaten,
			Constants.START_OF_TIME,
			new FinanzielleSituationBernRechner());
		generateTestDocument(generator, mandant, dokumentname);
	}

	private Verfuegung getFamiliensituationsVerfuegung(@Nonnull Gesuch gesuch) {
		return evaluator.evaluateFamiliensituation(gesuch, Constants.DEFAULT_LOCALE);
	}

	private void generateTestDocument(
		@Nonnull KibonPdfGenerator generator,
		@Nonnull MandantIdentifier mandant,
		@Nonnull String dokumentname)
		throws FileNotFoundException, InvoiceGeneratorException {
		generator.generate(new FileOutputStream(pfad + mandant + '/' + dokumentname));
	}
}

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.FinanzielleSituationPdfGeneratorFactory;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.mandant.MandantFactory;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.tests.util.PdfUnitTestUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.hamcrest.MatcherAssert.assertThat;

public class FinanzielleSituationPdfTest {

	private static final String PATH_PREFIX = FileUtils.getTempDirectoryPath() + "/kiBon/FinanzielleSituation/";

	@BeforeAll
	static void beforeAll() throws IOException {
		 FileUtils.deleteDirectory(new File(PATH_PREFIX));

		Arrays.stream(MandantIdentifier.values())
			.map(FinanzielleSituationPdfTest::getOutputPath)
			.forEach(path -> {
				//noinspection ResultOfMethodCallIgnored
				path.toFile().mkdirs();
			});
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	void testAlleinstehend(MandantIdentifier identifier) {
		Mandant mandant = MandantFactory.fromIdentifier(identifier);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = setupGesuch(testFall, gesuchsperiode);

		File pdf = generatePdf(gesuch, "WaeltiDagmar.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		assertThat(text, Matchers.stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
	}

	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	void testVerheirated(MandantIdentifier identifier) {
		Mandant mandant = MandantFactory.fromIdentifier(identifier);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall02_FeutzYvonne(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = setupGesuch(testFall, gesuchsperiode);

		File pdf = generatePdf(gesuch, "FeutzYvonne.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		assertThat(text, Matchers.stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
	}

	private File generatePdf(Gesuch gesuch, String dokumentname) {
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

		// there are no easy-to-use Mandant rules for test cases, so we create our own...
		Verfuegung verfuegung = new Verfuegung();
		VerfuegungZeitabschnitt single =
			new VerfuegungZeitabschnitt(new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb()).withFullMonths());
		single.getRelevantBgCalculationResult().setEinkommensjahr(gesuchsperiode.getBasisJahr());

		verfuegung.setZeitabschnitte(Collections.singletonList(single));

		GemeindeStammdaten gemeindeStammdaten = TestDataUtil.createGemeindeStammdaten(gesuch.extractGemeinde());
		var generator =
			FinanzielleSituationPdfGeneratorFactory.getGenerator(gesuch, verfuegung, gemeindeStammdaten,
				Constants.START_OF_TIME);
		MandantIdentifier mandant = gesuch.extractMandant().getMandantIdentifier();

		try {
			Path path = Path.of(getOutputPath(mandant).toString(), dokumentname);
			generator.generate(Files.newOutputStream(path));

			return path.toFile();
		} catch (IOException | InvoiceGeneratorException e) {
			throw new IllegalStateException("Failed to generate PDF", e);
		}
	}

	private Gesuch setupGesuch(AbstractTestfall testFall, Gesuchsperiode gesuchsperiode) {
		testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
		testFall.fillInGesuch();

		return testFall.getGesuch();
	}

	static Path getOutputPath(MandantIdentifier mandant) {
		return Path.of(PATH_PREFIX, mandant.name());
	}
}

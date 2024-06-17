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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.pdfgenerator.verfuegung.AbstractVerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.pdfgenerator.verfuegung.VerfuegungPdfGeneratorKonfiguration;
import ch.dvbern.ebegu.pdfgenerator.verfuegung.VerfuegungPdfGeneratorVisitor;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.mandant.MandantFactory;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.tests.util.PdfUnitTestUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(SnapshotExtension.class)
class VerfuegungPdfTest {

	private static final String PATH_PREFIX = FileUtils.getTempDirectoryPath() + "/kiBon/Verfuegung/";
	private static final Pattern PATTERN = Pattern.compile("\\r");

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	private Expect expect;

	@BeforeAll
	static void beforeAll() throws IOException {
		FileUtils.deleteDirectory(new File(PATH_PREFIX));

		Arrays.stream(MandantIdentifier.values())
			.map(VerfuegungPdfTest::getOutputPath)
			.forEach(path -> {
				//noinspection ResultOfMethodCallIgnored
				path.toFile().mkdirs();
			});
	}

	@SnapshotName("verfuegung-bruennen")
	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	void testBruennen(MandantIdentifier identifier) {
		Mandant mandant = MandantFactory.fromIdentifier(identifier);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = testFall.setupGesuch();
		verfuegen(gesuch);

		var bruennen = gesuch.getKindContainers().stream().findFirst().get().getBetreuungen().stream().findFirst().get();
		File pdf = generatePdf(bruennen, VerfuegungPdfGeneratorKonfiguration.builder()
			.FKJVTexte(true)
			.betreuungspensumAnzeigeTyp(BetreuungspensumAnzeigeTyp.NUR_PROZENT)
			.kontingentierungEnabledAndEntwurf(false)
			.stadtBernAsivConfigured(false)
			.isHoehereBeitraegeConfigured(false)
			.build(), "WaeltiDagmar.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		expect.scenario(identifier.name())
			.toMatchSnapshot(textForSnapshot(text));

		assertThat(text, stringContainsInOrder("Verfügung"));

	}

	@Test
	@SnapshotName("verfuegung-bruennen-hoeherer-beitrag")
	void testHoehererBeitrag() {
		Mandant mandant = MandantFactory.fromIdentifier(MandantIdentifier.SCHWYZ);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = testFall.setupGesuch();
		verfuegen(gesuch);

		var bruennen = gesuch.getKindContainers().stream().findFirst().get().getBetreuungen().stream().findFirst().get();
		File pdf = generatePdf(bruennen, VerfuegungPdfGeneratorKonfiguration.builder()
			.FKJVTexte(true)
			.betreuungspensumAnzeigeTyp(BetreuungspensumAnzeigeTyp.NUR_PROZENT)
			.kontingentierungEnabledAndEntwurf(false)
			.stadtBernAsivConfigured(false)
			.isHoehereBeitraegeConfigured(true)
			.build(), "WaeltiDagmar_hoehererBeitrag.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		expect.scenario(MandantIdentifier.SCHWYZ.name())
			.toMatchSnapshot(textForSnapshot(text));

		assertThat(text, stringContainsInOrder("Höherer", "Beitrag"));
	}

	private void verfuegen(Gesuch gesuch) {
		gesuch.getKindContainers().forEach(kindContainer -> kindContainer.getBetreuungen().forEach(betreuung -> {
			final Verfuegung verfuegung = new Verfuegung();
			final VerfuegungZeitabschnitt zeitabschnitt =
				new VerfuegungZeitabschnitt(gesuch.getGesuchsperiode().getGueltigkeit());
			zeitabschnitt.getRelevantBgCalculationResult().setBetreuungspensumProzent(BigDecimal.valueOf(80));
			zeitabschnitt.getRelevantBgCalculationResult().setHoehererBeitrag(BigDecimal.valueOf(10));
			verfuegung.setZeitabschnitte(List.of(zeitabschnitt));
			betreuung.setVerfuegung(verfuegung);
			betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		}));
	}

	private File generatePdf(Betreuung betreuung, VerfuegungPdfGeneratorKonfiguration konfiguration, String fileName) {
		Gesuch gesuch = betreuung.extractGesuch();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

		// there are no easy-to-use Mandant rules for test cases, so we create our own...
		Verfuegung verfuegung = new Verfuegung();
		VerfuegungZeitabschnitt single =
			new VerfuegungZeitabschnitt(new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb()).withFullMonths());
		single.getRelevantBgCalculationResult().setEinkommensjahr(gesuchsperiode.getBasisJahr());

		verfuegung.setZeitabschnitte(Collections.singletonList(single));

		GemeindeStammdaten gemeindeStammdaten = TestDataUtil.createGemeindeStammdaten(gesuch.extractGemeinde());
		var generator =
			new VerfuegungPdfGeneratorVisitor(
				betreuung,
				gemeindeStammdaten,
				Art.NORMAL,
				konfiguration
			);
		Mandant mandant = gesuch.extractMandant();

		try {
			Path path = Path.of(getOutputPath(mandant.getMandantIdentifier()).toString(), fileName);
			generator.getVerfuegungPdfGeneratorForMandant(mandant).generate(Files.newOutputStream(path));

			return path.toFile();
		} catch (IOException | InvoiceGeneratorException e) {
			throw new IllegalStateException("Failed to generate PDF", e);
		}
	}

	@Nonnull
	private String textForSnapshot(String text) {
		return PATTERN.matcher(text.replaceAll(Constants.DATE_FORMATTER.format(LocalDate.now()), "<TODAY>"))
			.replaceAll("");
	}

	static Path getOutputPath(MandantIdentifier mandant) {
		return Path.of(PATH_PREFIX, mandant.name());
	}

}

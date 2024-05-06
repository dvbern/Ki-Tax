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
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.FinanzielleSituationPdfGeneratorFactory;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.mandant.MandantFactory;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall_EmptyGesuch;
import ch.dvbern.ebegu.testfaelle.dataprovider.SchwyzTestfallDataProvider;
import ch.dvbern.ebegu.tests.util.PdfUnitTestUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.stringContainsInOrder;

@ExtendWith(SnapshotExtension.class)
class FinanzielleSituationPdfTest {

	private static final String PATH_PREFIX = FileUtils.getTempDirectoryPath() + "/kiBon/FinanzielleSituation/";
	private static final Pattern PATTERN = Pattern.compile("\\r");

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	private Expect expect;

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

	@SnapshotName("finsit-testAlleinstehend")
	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	void testAlleinstehend(MandantIdentifier identifier) {
		Mandant mandant = MandantFactory.fromIdentifier(identifier);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = testFall.setupGesuch();

		File pdf = generatePdf(gesuch, "WaeltiDagmar.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		expect.scenario(identifier.name())
			.toMatchSnapshot(textForSnapshot(text));

		assertThat(text, stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
	}

	@SnapshotName("finsit-testVerheirated")
	@ParameterizedTest
	@EnumSource(value = MandantIdentifier.class, mode = Mode.MATCH_ALL)
	void testVerheirated(MandantIdentifier identifier) {
		Mandant mandant = MandantFactory.fromIdentifier(identifier);
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
		Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

		TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
		var testFall = new Testfall02_FeutzYvonne(gesuchsperiode, true, gemeinde, institution);
		Gesuch gesuch = testFall.setupGesuch();

		File pdf = generatePdf(gesuch, "FeutzYvonne.pdf");
		String text = PdfUnitTestUtil.getText(pdf);

		expect.scenario(identifier.name()).toMatchSnapshot(textForSnapshot(text));

		assertThat(text, stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
	}

	@Nested
	class SchwyzTest {

		@Nested
		class WhenSingleGesuchsteller {

			@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized", "InnerClassFieldHidesOuterClassField" })
			private Expect expect;

			@SnapshotName("finsit-schwyz-single-veranlagt")
			@Test
			void alleinerziehend_veranlagt() {
				var gesuch = setupSingleGesuchsteller(
					SchwyzTestfallDataProvider::createAlleinerziehend,
					SchwzyTestData::createFinanzielleSituationVeranlagt
				);

				File pdf = generatePdf(gesuch, "single-veranlagt.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder(
					"Berechnung der finanziellen Verhältnisse",
					"Einkünfte", "Tim Tester",
					"Reineinkommen", "88’000.00",
					"Einkäufe berufliche Vorsorge (BVG / 2. Säule)", "5’000.00",
					"Abzüge",
					"Ausserordentlicher Liegenschaftsaufwand"
				));
			}

			@SnapshotName("finsit-schwyz-single-quellenbesteuert")
			@Test
			void alleinerziehend_quellenbesteuert() {
				var gesuch = setupSingleGesuchsteller(
					SchwyzTestfallDataProvider::createAlleinerziehend,
					s -> s.createFinanzielleSituationQuellenbesteuert(BigDecimal.valueOf(123_456))
				);

				File pdf = generatePdf(gesuch, "single-quellenbesteuert.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder(
					"Berechnung der finanziellen Verhältnisse",
					"Bruttolohn"
				));
			}

			Gesuch setupSingleGesuchsteller(
				Function<SchwzyTestData, Familiensituation> famSit,
				Function<SchwzyTestData, FinanzielleSituation> finSit
			) {
				var gesuch = setUpTestgesuch();
				var schwyzTestData = new SchwzyTestData(gesuch.getGesuchsperiode());

				var gesuchsteller = createGesuchsteller(finSit.apply(schwyzTestData));
				gesuch.setGesuchsteller1(gesuchsteller);

				addFamiliensituation(famSit.apply(schwyzTestData), gesuch);

				return gesuch;
			}
		}

		@Nested
		class WhenMultipleGesuchsteller {

			@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized", "InnerClassFieldHidesOuterClassField" })
			private Expect expect;

			@SnapshotName("finsit-schwyz-gemeinsam-veranlagt")
			@Test
			void gemeinsamVeranlagt() {
				var gesuch = setupGemeinsameSteuererklaerung(
					SchwzyTestData::createFinanzielleSituationVeranlagt
				);

				File pdf = generatePdf(gesuch, "gemeinsamVeranlagt.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
			}

			@Test
			void beideQuellenbesteuert() {
				var gesuch = setupIndividuelleSteuererklaerungen(
					s -> s.createFinanzielleSituationQuellenbesteuert(BigDecimal.valueOf(33_333)),
					s -> s.createFinanzielleSituationQuellenbesteuert(BigDecimal.valueOf(55_555))
				);

				File pdf = generatePdf(gesuch, "beideQuellenbesteuert.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder(
					"Berechnung der finanziellen Verhältnisse",
					"Einkünfte", "Tim Tester",
					"Bruttolohn", "33’333.00",
					"20% Bruttopauschale vom Bruttoeinkommen", "6’667.00",
					"Massgebendes Einkommen in CHF", "26’666.00",
					"Einkünfte", "Hanna Tester",
					"Bruttolohn", "55’555.00",
					"20% Bruttopauschale vom Bruttoeinkommen", "11’111.00",
					"Massgebendes Einkommen in CHF", "44’444.00",
					"Zusammenzug",
					"Massgebendes Einkommen in CHF", "71’110.00"
				));
			}

			@Test
			void mixed() {
				var gesuch = setupIndividuelleSteuererklaerungen(
					s -> s.createFinanzielleSituationQuellenbesteuert(BigDecimal.valueOf(80_000)),
					SchwzyTestData::createFinanzielleSituationVeranlagt
				);

				File pdf = generatePdf(gesuch, "mixed.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
			}

			@Test
			void withEinkommensverschlaechterung() {
				var gesuch = setupIndividuelleSteuererklaerungen(
					SchwzyTestData::createFinanzielleSituationVeranlagt,
					s -> s.createFinanzielleSituationQuellenbesteuert(BigDecimal.valueOf(80_000))
				);

				EinkommensverschlechterungContainer ekv1 =
					addEinkommensVerschlechterung(requireNonNull(gesuch.getGesuchsteller1()));

				Einkommensverschlechterung ekvJABasisJahrPlus1 = new Einkommensverschlechterung();
				SchwyzTestfallDataProvider.applyVerfuegt(
					ekvJABasisJahrPlus1,
					BigDecimal.valueOf(90_000),
					BigDecimal.valueOf(75_000),
					BigDecimal.valueOf(2_000),
					BigDecimal.valueOf(4_000)
				);
				ekv1.setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);

				Einkommensverschlechterung ekvJABasisJahrPlus2 = new Einkommensverschlechterung();
				SchwyzTestfallDataProvider.applyVerfuegt(
					ekvJABasisJahrPlus2,
					BigDecimal.valueOf(85_000),
					BigDecimal.valueOf(70_000),
					BigDecimal.valueOf(500),
					BigDecimal.valueOf(2_000)
				);
				ekv1.setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

				EinkommensverschlechterungContainer ekv2 =
					addEinkommensVerschlechterung(requireNonNull(gesuch.getGesuchsteller2()));

				Einkommensverschlechterung ekv2JABasisJahrPlus1 = new Einkommensverschlechterung();
				ekv2JABasisJahrPlus1.setBruttoLohn(BigDecimal.valueOf(70_000));
				ekv2.setEkvJABasisJahrPlus1(ekv2JABasisJahrPlus1);

				Einkommensverschlechterung ekv2JABasisJahrPlus2 = new Einkommensverschlechterung();
				ekv2JABasisJahrPlus2.setBruttoLohn(BigDecimal.valueOf(74_000));
				ekv2.setEkvJABasisJahrPlus2(ekv2JABasisJahrPlus2);

				EinkommensverschlechterungInfoContainer infoContainer = new EinkommensverschlechterungInfoContainer();
				EinkommensverschlechterungInfo info = new EinkommensverschlechterungInfo();
				info.setEkvFuerBasisJahrPlus1(true);
				info.setEkvFuerBasisJahrPlus2(true);
				infoContainer.setEinkommensverschlechterungInfoJA(info);
				gesuch.setEinkommensverschlechterungInfoContainer(infoContainer);

				File pdf = generatePdf(gesuch, "einkommensverschlaechterung.pdf");
				String text = PdfUnitTestUtil.getText(pdf);

				expect.toMatchSnapshot(textForSnapshot(text));

				assertThat(text, stringContainsInOrder("Berechnung der finanziellen Verhältnisse"));
			}

			Gesuch setupGemeinsameSteuererklaerung(
				Function<SchwzyTestData, FinanzielleSituation> finSitGs
			) {
				return setupMultipleGesuchsteller(true, finSitGs, a -> Optional.empty());
			}

			Gesuch setupIndividuelleSteuererklaerungen(
				Function<SchwzyTestData, FinanzielleSituation> finSitGs1,
				Function<SchwzyTestData, FinanzielleSituation> finSitGs2
			) {
				return setupMultipleGesuchsteller(false, finSitGs1, finSitGs2.andThen(Optional::of));
			}

			Gesuch setupMultipleGesuchsteller(
				boolean gemeinsameSteuererklaerung,
				Function<SchwzyTestData, FinanzielleSituation> finSitGs1,
				Function<SchwzyTestData, Optional<FinanzielleSituation>> finSitGs2
			) {
				var gesuch = setUpTestgesuch();
				var schwyzTestData = new SchwzyTestData(gesuch.getGesuchsperiode());

				gesuch.setGesuchsteller1(createGesuchsteller(finSitGs1.apply(schwyzTestData)));

				GesuchstellerContainer gesuchsteller2 = finSitGs2.apply(schwyzTestData)
					.map(SchwyzTest.this::createGesuchsteller)
					.orElseGet(TestDataUtil::createDefaultGesuchstellerContainer);
				gesuchsteller2.getGesuchstellerJA().setVorname("Hanna");
				gesuch.setGesuchsteller2(gesuchsteller2);

				// nur in diesem Fall darf es 2 Gesuchsteller geben (Verheiratet / Konkubinat ist ebenbürtig)
				Familiensituation verheiratet = schwyzTestData.createVerheiratet();
				verheiratet.setGemeinsameSteuererklaerung(gemeinsameSteuererklaerung);
				addFamiliensituation(verheiratet, gesuch);

				return gesuch;
			}
		}

		private void addFamiliensituation(Familiensituation familiensituation, Gesuch gesuch) {
			FamiliensituationContainer famSitContainer = new FamiliensituationContainer();
			famSitContainer.setFamiliensituationJA(familiensituation);

			gesuch.setFamiliensituationContainer(famSitContainer);
		}

		private Gesuch setUpTestgesuch() {
			Mandant mandant = TestDataUtil.getMandantSchwyz();

			Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
			Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

			TestDataInstitutionStammdatenBuilder institution = new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
			var testFall = new Testfall_EmptyGesuch(gesuchsperiode, true, gemeinde, institution);

			return testFall.setupGesuch();
		}

		class SchwzyTestData extends SchwyzTestfallDataProvider {

			protected SchwzyTestData(Gesuchsperiode gesuchsperiode) {
				super(gesuchsperiode);
			}

			FinanzielleSituation createFinanzielleSituationQuellenbesteuert(BigDecimal bruttolohn) {
				FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
				finanzielleSituation.setQuellenbesteuert(true);
				finanzielleSituation.setBruttoLohn(bruttolohn);

				return finanzielleSituation;
			}

			FinanzielleSituation createFinanzielleSituationVeranlagt() {
				BigDecimal vermoegen = BigDecimal.valueOf(123_456);
				BigDecimal einkommen = BigDecimal.valueOf(88_000);
				var finanzielleSituation = createFinanzielleSituation(vermoegen, einkommen);
				finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.valueOf(5_000));
				finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.valueOf(1_000));

				return finanzielleSituation;
			}
		}

		GesuchstellerContainer createGesuchsteller(FinanzielleSituation finSit) {
			GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();

			FinanzielleSituationContainer finSitContainer = new FinanzielleSituationContainer();
			finSitContainer.setFinanzielleSituationJA(finSit);
			gesuchsteller.setFinanzielleSituationContainer(finSitContainer);

			return gesuchsteller;
		}

		EinkommensverschlechterungContainer addEinkommensVerschlechterung(GesuchstellerContainer gesuchsteller) {
			EinkommensverschlechterungContainer ekvContainer = new EinkommensverschlechterungContainer();
			gesuchsteller.setEinkommensverschlechterungContainer(ekvContainer);

			return ekvContainer;
		}
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

	static Path getOutputPath(MandantIdentifier mandant) {
		return Path.of(PATH_PREFIX, mandant.name());
	}

	@Nonnull
	private String textForSnapshot(String text) {
		return PATTERN.matcher(text.replaceAll(Constants.DATE_FORMATTER.format(LocalDate.now()), "<TODAY>"))
			.replaceAll("");
	}
}

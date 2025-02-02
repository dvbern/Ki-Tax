/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.reporting;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerDataRow;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenDataRow;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.tests.util.UnitTestTempFolder;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({ "InstanceMethodNamingConvention", "MethodParameterNamingConvention", "InstanceVariableNamingConvention" })
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/reportTestData.xml")
@Transactional(TransactionMode.DISABLED)
public class ReportServiceBeanTest extends AbstractEbeguLoginTest {

	private static final int ANZAHL_FAELLE = 8;
	private static final int ANZAHL_GESUCHE = 11;
	private static final int ANZAHL_BETREUUNGEN = 17; // Davon 1 nicht verfuegt

	@Rule
	public final UnitTestTempFolder unitTestTempfolder = new UnitTestTempFolder();

	@Inject
	private ReportService reportService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private ReportKinderMitZemisNummerService kinderMitZemisNummerService;

	@Before
	public void init() {
		// Aufbau der Testdaten:
		// 7 Fälle, 10 Gesuche (d.h. 3 Gesuch mit Mutation)
		// Je einmal Mahnung, Beschwerde, Nicht-Freigegeben, Nicht-Eintreten, Abwesenheit
		Collection<Mandant> allMandant = criteriaQueryHelper.getAll(Mandant.class);
		Collection<Gesuchsperiode> allGesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class);
		Collection<Fall> allFall = criteriaQueryHelper.getAll(Fall.class);
		Collection<Gesuch> allGesuch = criteriaQueryHelper.getAll(Gesuch.class);

		Assert.assertEquals(1, allMandant.size());
		Assert.assertEquals(1, allGesuchsperiode.size());
		Assert.assertEquals(ANZAHL_FAELLE, allFall.size());
		Assert.assertEquals(ANZAHL_GESUCHE, allGesuch.size());
	}

	@Test
	public void testGetReportDataGesuchStichtag() throws Exception {
		List<GesuchStichtagDataRow> reportData = reportService.getReportDataGesuchStichtag(LocalDate.now(), null,
				getDummySuperadmin().getMandant());

		List<GesuchStichtagDataRow> rowsSorted = reportData
			.stream()
			.sorted(Comparator.comparing(GesuchStichtagDataRow::getReferenzNummer).thenComparing(GesuchStichtagDataRow::getGesuchLaufNr))
			.collect(Collectors.toList());

		assertNotNull(rowsSorted);
		// Wir haben je 1 Gesuch mit Mahnung, 1 Gesuch mit Beschwerde
		assertEquals(2, rowsSorted.size());
		// Fall 103: Mahnung
		assertGesuchStichtagDataRow(rowsSorted.get(0), "17.000103.1.1", 0, 1, 0);
		// Fall 104: Beschwerde
		assertGesuchStichtagDataRow(rowsSorted.get(1), "17.000104.1.1", 0, 0, 1);
	}

	@Test
	public void testGetReportDataGesuchZeitraumTest() throws Exception {
		List<GesuchZeitraumDataRow> reportData = reportService.getReportDataGesuchZeitraum(
			LocalDate.of(2016, Month.JANUARY, 1),
			LocalDate.of(2017, Month.DECEMBER, 31),
			DatumTyp.EINREICHEDATUM,
			null, getDummySuperadmin().getMandant());

		List<GesuchZeitraumDataRow> rowsSorted = reportData
			.stream()
			.sorted(Comparator.comparing(GesuchZeitraumDataRow::getReferenzNummer).thenComparing(GesuchZeitraumDataRow::getGesuchLaufNr))
			.collect(Collectors.toList());

		assertNotNull(rowsSorted);
		assertEquals(ANZAHL_BETREUUNGEN, rowsSorted.size());

		// 101 Verfuegt mit Nicht eintreten
		// 102 Normal Verfuegt
		// 103 Mahnung
		// 104: Verfuegt mit Beschwerde
		// 105: Neues Kind mit Betreuung
		// 106: Abwesenheit
		// 107: Kind Name

		// Fall 101: Verfuegt mit nichtEintreten
		// Betreuung 1: Verfuegt mit Nicht-Eintreten
		assertGesuchZeitraumDataRow(rowsSorted.get(0), "17.000101.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);
		// Betreuung 2: Verfuegt normal
		assertGesuchZeitraumDataRow(rowsSorted.get(1), "17.000101.1.2", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);

		// Fall 102: Normal verfuegt, 2 Kinder
		assertGesuchZeitraumDataRow(rowsSorted.get(2), "17.000102.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);
		assertGesuchZeitraumDataRow(rowsSorted.get(3), "17.000102.2.1", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);

		// Fall 103: Mahnung
		assertGesuchZeitraumDataRow(rowsSorted.get(4), "17.000103.1.1", 0, 0, 0, 1, 0, 0, 0, 1, 1, 0);

		// Fall 104: Verfuegt, mit Beschwerde
		assertGesuchZeitraumDataRow(rowsSorted.get(5), "17.000104.1.1", 0, 0, 0, 0, 1, 0, 0, 1, 0, 0);

		// Fall 105:
		// Erstgesuch
		assertGesuchZeitraumDataRow(rowsSorted.get(6), "17.000105.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);
		// Mutation
		// Kind 1: Schon im Erstgesuch vorhanden, in Mutation unverändert
		assertGesuchZeitraumDataRow(rowsSorted.get(7), "17.000105.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
		// Kind 2: Neu auf Mutation inkl. Betreuung
		assertGesuchZeitraumDataRow(rowsSorted.get(8), "17.000105.2.1", 0, 1, 1, 0, 0, 0, 0, 1, 0, 0);

		// Fall 106: Mutation mit Abwesenheit
		// Erstgesuch
		assertGesuchZeitraumDataRow(rowsSorted.get(9), "17.000106.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 1, 0);
		// Mutation
		assertGesuchZeitraumDataRow(rowsSorted.get(10), "17.000106.1.1", 1, 0, 0, 0, 0, 0, 0, 1, 1, 0);

		// Fall 107: Mutation Kind Name
		// Betreuung 1
		assertGesuchZeitraumDataRow(rowsSorted.get(11), "17.000107.1.1", 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
		assertGesuchZeitraumDataRow(rowsSorted.get(12), "17.000107.1.1", 0, 0, 1, 0, 0, 0, 0, 1, 0, 0);
		// Betreuung 2
		assertGesuchZeitraumDataRow(rowsSorted.get(13), "17.000107.1.2", 0, 0, 0, 0, 0, 0, 0, 1, 0, 1);
		assertGesuchZeitraumDataRow(rowsSorted.get(14), "17.000107.1.2", 0, 0, 1, 0, 0, 0, 0, 1, 0, 0);

		// Fall 108: Verfügt, Beschwerde, Beschwerde aufgehoben, STV, STV geprüft
		assertGesuchZeitraumDataRow(rowsSorted.get(15), "17.000108.1.1", 0, 0, 0, 0, 1, 1, 1, 1, 1, 0);
		assertGesuchZeitraumDataRow(rowsSorted.get(16), "17.000108.2.1", 0, 0, 0, 0, 1, 1, 1, 0, 0, 0);

	}

	private void assertGesuchStichtagDataRow(
		GesuchStichtagDataRow row,
		String referenzNummer,
		Integer nichtFreigegeben,
		Integer mahnung,
		Integer beschwerde
	) {
		assertNotNull(row);
		assertEquals(referenzNummer, row.getReferenzNummer());
		assertEquals(nichtFreigegeben, row.getNichtFreigegeben());
		assertEquals(mahnung, row.getMahnungen());
		assertEquals(beschwerde, row.getBeschwerde());
	}

	private void assertGesuchZeitraumDataRow(GesuchZeitraumDataRow row, String referenzNummer,
		Integer anzahlMutationAbwesenheit, Integer anzahlMutationBetreuung, Integer anzahlMutationKind,
		Integer anzahlMahnungen, Integer anzahlBeschwerde,
		Integer anzahlSteueramtAusgeloest, Integer anzahlSteueramtGeprueft,
		Integer anzahlVerfuegungen,
		Integer anzahlVerfuegungenNormal, Integer anzahlVerfuegungenNichtEintreten) {
		assertNotNull(row);
		assertEquals(referenzNummer, row.getReferenzNummer());
		assertEquals(anzahlMutationAbwesenheit, row.getAnzahlMutationAbwesenheit());
		assertEquals(anzahlMutationBetreuung, row.getAnzahlMutationBetreuung());
		assertEquals(anzahlMutationKind, row.getAnzahlMutationKinder());
		assertEquals(anzahlMahnungen, row.getAnzahlMahnungen());
		assertEquals(anzahlBeschwerde, row.getAnzahlBeschwerde());
		assertEquals(anzahlSteueramtAusgeloest, row.getAnzahlSteueramtAusgeloest());
		assertEquals(anzahlSteueramtGeprueft, row.getAnzahlSteueramtGeprueft());
		assertEquals(anzahlVerfuegungen, row.getAnzahlVerfuegungen());
		assertEquals(anzahlVerfuegungenNormal, row.getAnzahlVerfuegungenNormal());
		assertEquals(anzahlVerfuegungenNichtEintreten, row.getAnzahlVerfuegungenNichtEintreten());
	}

	@Test
	public void generateExcelReportGesuchStichtagDE() throws Exception {
		UploadFileInfo uploadFileInfo = reportService.generateExcelReportGesuchStichtag(
			LocalDate.now(),
			null,
			Constants.DEUTSCH_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportGesuchStichtag_DE.xlsx");
	}

	@Test
	public void generateExcelReportGesuchStichtagFR() throws Exception {
		UploadFileInfo uploadFileInfo = reportService.generateExcelReportGesuchStichtag(
			LocalDate.now(),
			null,
			Constants.FRENCH_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportGesuchStichtag_FR.xlsx");
	}

	@Test
	public void generateExcelReportGesuchZeitraumDE() throws Exception {
		UploadFileInfo uploadFileInfo = reportService.generateExcelReportGesuchZeitraum(
			LocalDate.of(2016, Month.JANUARY, 1),
			LocalDate.of(2017, Month.DECEMBER, 31),
			DatumTyp.EINREICHEDATUM,
			null,
			Constants.DEUTSCH_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportGesuchZeitraum_DE.xlsx");
	}

	@Test
	public void generateExcelReportGesuchZeitraumFR() throws Exception {
		UploadFileInfo uploadFileInfo = reportService.generateExcelReportGesuchZeitraum(
			LocalDate.of(2016, Month.JANUARY, 1),
			LocalDate.of(2017, Month.DECEMBER, 31),
			DatumTyp.EINREICHEDATUM,
			null,
			Constants.FRENCH_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportGesuchZeitraum_FR.xlsx");
	}

	@Test
	public void generateExcelReportZahlungAuftrag() throws Exception {
		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportZahlungAuftrag("5a1fde7d-991a-4aef-8de2-43387db4f87d", Constants.DEFAULT_LOCALE);

		assertNotNull(uploadFileInfo);
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportZahlungAuftrag.xlsx");
	}

	@Test
	public void generateExcelReportKantonOhneSelbstbehalt() throws Exception {
		Collection<Gesuch> allGesuche = gesuchService.getAllGesuche();
		for (Gesuch gesuch : allGesuche) {
			if (gesuch.getStatus().isAnyStatusOfVerfuegt()) {
				System.out.println("verfuegtes Gesuch: " + gesuch.getId());
			}
		}
		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportKanton(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, null, Constants.DEFAULT_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportKantonOhneSelbstbehalt.xlsx");
	}

	@Test
	public void generateExcelReportKantonMitSelbstBehalt() throws Exception {
		Collection<Gesuch> allGesuche = gesuchService.getAllGesuche();
		for (Gesuch gesuch : allGesuche) {
			if (gesuch.getStatus().isAnyStatusOfVerfuegt()) {
				System.out.println("verfuegtes Gesuch: " + gesuch.getId());
			}
		}
		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportKanton(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, new BigDecimal(1000), Constants.DEFAULT_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportKantonMitSelbstbehalt.xlsx");
	}

	@Test
	public void testGenerateExcelReportMitarbeiterinnen() throws Exception {
		gesuchService.getAllGesuche();
		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportMitarbeiterinnen(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, Constants.DEFAULT_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportMitarbeiterinnen.xlsx");
	}

	@Test
	public void testGetReportMitarbeiterinnen() throws Exception {
		final List<MitarbeiterinnenDataRow> reportMitarbeiterinnen = reportService
			.getReportMitarbeiterinnen(LocalDate.of(1000, 1, 1), LocalDate.now(), getDummySuperadmin().getMandant());

		Assert.assertEquals(3, reportMitarbeiterinnen.size());

		//case with only Gesuche als Verantwortlicher
		MitarbeiterinnenDataRow maBlaser = reportMitarbeiterinnen.get(0);
		Assert.assertEquals("Blaser", maBlaser.getName());
		Assert.assertEquals(BigDecimal.valueOf(2), maBlaser.getVerantwortlicheGesuche());
		Assert.assertEquals(BigDecimal.ZERO, maBlaser.getVerfuegungenAusgestellt());

		//case with only verfuegte Gesuche
		MitarbeiterinnenDataRow maBrogabante = reportMitarbeiterinnen.get(1);
		Assert.assertEquals("Bogabante", maBrogabante.getName());
		Assert.assertEquals(BigDecimal.ZERO, maBrogabante.getVerantwortlicheGesuche());
		Assert.assertEquals(BigDecimal.ONE, maBrogabante.getVerfuegungenAusgestellt());

		// case with no Gesuche at all are not shown

		// Der Benutzer System kommt nicht auf der Liste weil er SUPERADMIN ist
	}

	@Test
	public void generateExcelReportGesuchstellerKinderBetreuung() throws Exception {
		UploadFileInfo uploadFileInfo = reportService.generateExcelReportGesuchstellerKinderBetreuung(
			LocalDate.of(2016, Month.JANUARY, 1),
			LocalDate.of(2018, Month.DECEMBER, 31),
			null,
			Constants.DEFAULT_LOCALE, getDummySuperadmin().getMandant());

		assertNotNull(uploadFileInfo.getBytes());
		unitTestTempfolder.writeToTempDir(uploadFileInfo.getBytes(), "ExcelReportGesuchstellerKinderBetreuung.xlsx");
	}

	@Test
	public void generateExcelReportBenutzer() {
		final List<BenutzerDataRow> reportDataBenutzer = reportService.getReportDataBenutzer(Constants.DEFAULT_LOCALE,
				getDummySuperadmin().getMandant());

		assertNotNull(reportDataBenutzer);
		assertEquals(7, reportDataBenutzer.size()); // anonymous is a user too

		// Admin benutzer
		assertTrue(reportDataBenutzer.stream().anyMatch(benutzerDataRow -> benutzerDataRow.getUsername().equals("blku")));
		reportDataBenutzer.stream()
			.filter(benutzerDataRow -> benutzerDataRow.getUsername().equals("blku"))
			.forEach(row -> {
				assertEquals("Administrator/in BG", row.getRole());
				assertEquals(BenutzerStatus.AKTIV, row.getStatus());
				assertNull(row.getTraegerschaft());
				assertNull(row.getInstitution());
			});

		// Institution benutzer
		assertTrue(reportDataBenutzer.stream().anyMatch(benutzerDataRow -> benutzerDataRow.getUsername().equals("inst1")));
		reportDataBenutzer.stream()
			.filter(benutzerDataRow -> benutzerDataRow.getUsername().equals("inst1"))
			.forEach(row -> {
				assertEquals("Sachbearbeiter/in Institution", row.getRole());
				assertEquals(BenutzerStatus.AKTIV, row.getStatus());
				assertNotNull(row.getTraegerschaft());
				assertNotNull(row.getInstitution());
				assertTrue(row.isKita());
				assertFalse(row.isTagesfamilien());
				assertFalse(row.isTagesschule());
				assertFalse(row.isFerieninsel());
			});
	}

	@Test
	public void generateZemisExcel() throws ExcelMergeException, IOException {

		int lastenausgleichJahr = 2018;

		List<Gesuch> gesuchList = gesuchService.findGesucheForZemisList(lastenausgleichJahr);

		// nur gültige gesuche mit kind mit Zemis Nummer
		assertEquals(gesuchList.size(), 1);

		UploadFileInfo uploadFileInfo = kinderMitZemisNummerService.generateZemisReport(lastenausgleichJahr, Constants.DEUTSCH_LOCALE);
		assertNotNull(uploadFileInfo.getBytes());
	}
}

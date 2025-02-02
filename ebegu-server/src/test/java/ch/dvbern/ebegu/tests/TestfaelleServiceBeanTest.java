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

package ch.dvbern.ebegu.tests;

import java.io.File;
import java.time.LocalDate;
import java.time.Month;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.data.VerfuegungZeitabschnittData;
import ch.dvbern.ebegu.test.data.VerfuegungszeitabschnitteData;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration Test mit den Testfällen.
 * Alle Verfügungsdaten - Ergebnisse werden in den .xml Files unter /src/test/resources/VerfuegungResult/ gespeichert
 * und bei jedem Test mit den aktuellen Berechnungsergebnisse verglichen.
 * <p>
 * Die gespeicherten Daten können mit writeToFile = true neu generiert werden.
 */
@SuppressWarnings({ "CdiInjectionPointsInspection", "JUnit3StyleTestMethodInJUnit4Class" })
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class TestfaelleServiceBeanTest extends AbstractEbeguLoginTest {

	private static final Logger LOG = LoggerFactory.getLogger(TestfaelleServiceBeanTest.class);
	private static final int BASISJAHR_PLUS_1 = 2017;
	private static final int BASISJAHR_PLUS_2 = 2018;
	private static final Pattern COMPILE = Pattern.compile("[^a-zA-Z0-9.-]");

	@Inject
	private TestfaelleService testfaelleService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private Persistence persistence;



	/**
	 * Wenn true werden die Testergebnisse neu in die Testfiles geschrieben. Muss für testen immer false sein!
	 */
	private static final boolean WRITE_TO_FILE = false;

	private String gemeinde = null;
	private Gesuchsperiode gesuchsperiode = null;
	private Mandant mandant;

	@Before
	public void init() {
		gesuchsperiode = createGesuchsperiode();
		mandant = insertInstitutionen();
		createBenutzer(mandant);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		gemeinde = TestDataUtil.getGemeindeParis(persistence).getId();
		Locale.setDefault(Constants.DEFAULT_LOCALE);
	}

	@Test
	public void testVerfuegung_WaeltiDagmar() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.WAELTI_DAGMAR, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_FeutzIvonne() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.FEUTZ_IVONNE, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_BeckerNora() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.BECKER_NORA, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_LuethiMeret() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.LUETHI_MERET, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_PerreiraMarcia() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.PERREIRA_MARCIA, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_WaltherLaura() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.WALTHER_LAURA, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_MeierMeret() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.MEIER_MERET, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_UmzugAusInAusBern() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.UMZUG_AUS_IN_AUS_BERN, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_UmzugVorGesuchsperiode() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.UMZUG_VOR_GESUCHSPERIODE, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_Abwesenheit() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.ABWESENHEIT, true, true, gemeinde, gesuchsperiode, mandant);
		ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
	}

	@Test
	public void testVerfuegung_SchulamtOnly() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.SCHULAMT_ONLY, true, true, gemeinde, gesuchsperiode, mandant);
		//ueberpruefeVerfuegungszeitabschnitte(gesuch, null);
		Assert.assertNotNull(gesuch);
		Assert.assertTrue(gesuch.hasOnlyBetreuungenOfSchulamt());
	}

	@Test
	public void testVerfuegung_WaeltiDagmar_mutationHeirat() {
		//waelti dagmar arbeitet 80%, Heirat am 15.1. -> gilt ab 1.2 (Eingereicht am 15.12.)
		// Partner arbeitet 90% -> zusammen 170 -> 90% Anspruch
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.WAELTI_DAGMAR, true, true, gemeinde, gesuchsperiode, mandant);
		assert gesuch != null;
		final Gesuch mutieren = testfaelleService.mutierenHeirat(gesuch.getDossier().getId(),
			gesuch.getGesuchsperiode().getId(), LocalDate.of(BASISJAHR_PLUS_1, Month.DECEMBER, 15),
			LocalDate.of(BASISJAHR_PLUS_2, Month.JANUARY, 15), true);
		ueberpruefeVerfuegungszeitabschnitte(mutieren, "MutationHeirat");
	}

	@Test
	public void testVerfuegung_BeckerNora_mutationHeirat() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.BECKER_NORA, true, true, gemeinde, gesuchsperiode, mandant);
		assert gesuch != null;
		final Gesuch mutieren = testfaelleService.mutierenHeirat(gesuch.getDossier().getId(),
			gesuch.getGesuchsperiode().getId(), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 15), LocalDate.of(BASISJAHR_PLUS_2, Month.FEBRUARY, 15), true);
		ueberpruefeVerfuegungszeitabschnitte(mutieren, "MutationHeirat");
	}

	@Test
	public void testVerfuegung_PerreiraMarcia_mutationScheidung() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.PERREIRA_MARCIA, true, true, gemeinde, gesuchsperiode, mandant);
		assert gesuch != null;
		final Gesuch mutieren = testfaelleService.mutierenScheidung(gesuch.getDossier().getId(),
			gesuch.getGesuchsperiode().getId(), LocalDate.of(BASISJAHR_PLUS_1, Month.SEPTEMBER, 30), LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 15), true);
		ueberpruefeVerfuegungszeitabschnitte(mutieren, "MutationScheidung");
	}

	/**
	 * Diese Scheidung wurde zu spät eingereicht!
	 */
	@Test
	public void testVerfuegung_MeierMeret_mutationScheidung() {
		Gesuch gesuch = testfaelleService.createAndSaveTestfaelle(TestfaelleService.MEIER_MERET, true, true, gemeinde, gesuchsperiode, mandant);
		assert gesuch != null;
		//		final Gesuch correctedGesuch = TestDataUtil.correctTimestampVerfuegt(gesuch, LocalDateTime.MAX, persistence);
		LocalDate eingangsdatum = LocalDate.of(BASISJAHR_PLUS_1, Month.NOVEMBER, 15);
		LocalDate aenderungPer = LocalDate.of(BASISJAHR_PLUS_1, Month.OCTOBER, 15);
		final Gesuch mutation = testfaelleService.mutierenScheidung(gesuch.getDossier().getId(),
			gesuch.getGesuchsperiode().getId(), eingangsdatum, aenderungPer, true);
		ueberpruefeVerfuegungszeitabschnitte(mutation, "MutationScheidung");
	}

	/**
	 * Ueberprüfen der Verfügungszeitabschnitte
	 */
	private void ueberpruefeVerfuegungszeitabschnitte(@Nullable Gesuch gesuch, @Nullable String addText) {
		Assert.assertNotNull(gesuch);

		gesuch.getKindContainers().forEach(kindContainer -> kindContainer.getBetreuungen().forEach(betreuung -> {
			Assert.assertNotNull(betreuung.getVerfuegungOrVerfuegungPreview());
			writeResultsToFile(
				betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte(),
				kindContainer.getKindJA().getFullName(),
				betreuung.getInstitutionStammdaten().getInstitution().getName(),
				betreuung.getBetreuungNummer(),
				addText
			);
			compareWithDataInFile(
				betreuung.getVerfuegungOrVerfuegungPreview().getZeitabschnitte(),
				kindContainer.getKindJA().getFullName(),
				betreuung.getInstitutionStammdaten().getInstitution().getName(),
				betreuung.getBetreuungNummer(),
				addText
			);
		})
		);
	}

	/**
	 * Holt die gespeicherten Verfügungszeitabschnitte und vergleicht diese mit den berechneten
	 */
	private void compareWithDataInFile(List<VerfuegungZeitabschnitt> zeitabschnitte, String fullName, String betreuung, Integer betreuungNummer, @Nullable String addText) {
		final VerfuegungszeitabschnitteData expectedVerfuegungszeitabschnitt = getExpectedVerfuegungszeitabschnitt(fullName, betreuung, betreuungNummer, addText);
		final VerfuegungszeitabschnitteData calculatedVerfuegungszeitabschnitt = generateVzd(zeitabschnitte, fullName, betreuung, betreuungNummer);

		Assert.assertNotNull(expectedVerfuegungszeitabschnitt);
		final Iterator<VerfuegungZeitabschnittData> iteratorExpected = expectedVerfuegungszeitabschnitt.getVerfuegungszeitabschnitte().iterator();
		final Iterator<VerfuegungZeitabschnittData> iteratorCalculated = calculatedVerfuegungszeitabschnitt.getVerfuegungszeitabschnitte().iterator();
		while (iteratorExpected.hasNext() &&
			iteratorCalculated.hasNext()) {
			doCompare(iteratorExpected.next(),
				iteratorCalculated.next(), fullName, betreuung);
		}
	}

	/**
	 * Vergleicht die einzelnen Werte der Verfuegungszeitabschnitte
	 */
	private void doCompare(VerfuegungZeitabschnittData expected, VerfuegungZeitabschnittData calculated, String fullName, String betreuung) {
		String fehlerString = "Unterschiedliches Resultat beim gespeicherten und berechnetem Wert " +
			"fuer Kind " + fullName + " bei Betreuung " + betreuung + " bei(m): ";

		Assert.assertEquals(fehlerString + "GueltigAb", expected.getGueltigAb(), calculated.getGueltigAb());
		Assert.assertEquals(fehlerString + "Gueltig Bis", expected.getGueltigBis(), calculated.getGueltigBis());
		Assert.assertEquals(fehlerString + "Abzug der Familiengroesse", expected.getAbzugFamGroesse(), calculated.getAbzugFamGroesse());
		Assert.assertEquals(fehlerString + "Elternbeitrag", expected.getElternbeitrag(), calculated.getElternbeitrag());
		Assert.assertEquals(fehlerString + "Anspruchberechtigtes Pensum", expected.getAnspruchberechtigtesPensum(), calculated.getAnspruchberechtigtesPensum());
		Assert.assertEquals(fehlerString + "Anzahl Bemerkungen", expected.getBemerkungenList().size(), calculated.getBemerkungenList().size());

		for(String bemerkung : expected.getBemerkungenList()) {
			Assert.assertTrue(fehlerString + "Erwartete Bemerkung nicht vorhanden", calculated.getBemerkungenList().contains(bemerkung));
		}

		Assert.assertEquals(fehlerString + "BetreuungspensumProzent",
			MathUtil.DEFAULT.from(expected.getBetreuungspensumProzent()),
			MathUtil.DEFAULT.from(calculated.getBetreuungspensumProzent()));
		Assert.assertEquals(fehlerString + "Vollkosten", expected.getVollkosten(), calculated.getVollkosten());
	}

	/**
	 * Holt die gespeicherten Werte aus den Files
	 */
	@Nullable
	private VerfuegungszeitabschnitteData getExpectedVerfuegungszeitabschnitt(String fullName, String betreuung, Integer betreuungNummer, @Nullable String addText) {

		final String fileNamePath = getFileNamePath(fullName, betreuung, betreuungNummer, addText);
		final File resultFile = new File(fileNamePath);
		VerfuegungszeitabschnitteData expectedVerfuegungszeitabschnitteData = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(VerfuegungszeitabschnitteData.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			expectedVerfuegungszeitabschnitteData = (VerfuegungszeitabschnitteData) jaxbUnmarshaller.unmarshal(resultFile);
		} catch (JAXBException e) {
			LOG.error("Es ist ein Fehler aufgetreten", e);
		}
		return expectedVerfuegungszeitabschnitteData;
	}

	/**
	 * Schreibt die berechneten Werte in die Files wenn writeToFile true ist
	 */
	private void writeResultsToFile(final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts, String fullName, String betreuung, Integer betreuungNummer,
		@Nullable String addText) {

		if (WRITE_TO_FILE) {
			VerfuegungszeitabschnitteData eventResults = generateVzd(verfuegungZeitabschnitts, fullName, betreuung, betreuungNummer);

			String pathname = getFileNamePath(fullName, betreuung, betreuungNummer, addText);

			try {
				File file = new File(pathname);
				JAXBContext jaxbContext = JAXBContext.newInstance(VerfuegungszeitabschnitteData.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

				// output pretty printed
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				jaxbMarshaller.marshal(eventResults, file);

			} catch (JAXBException e) {
				LOG.error("Es ist ein Fehler aufgetreten", e);
			}
		}
	}

	/**
	 * Generiert den Pfad für die Files zum speichern der Daten
	 */
	private String getFileNamePath(String fullName, String betreuung, Integer betreungsnummer, @Nullable String addText) {
		String storePath = "./src/test/resources/VerfuegungResult/";

		String filename = fullName + betreuung + betreungsnummer;
		if (addText != null) {
			filename += addText;
		}
		return storePath + COMPILE.matcher(filename).replaceAll("_") + ".xml";
	}

	/**
	 * Schreibt die berechneten Daten in VerfuegungszeitabschnitteData objekt
	 */
	private VerfuegungszeitabschnitteData generateVzd(final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts, String fullName, String betreuung, Integer betreuungNummer) {
		VerfuegungszeitabschnitteData verfuegungszeitabschnitteData = new VerfuegungszeitabschnitteData();

		verfuegungszeitabschnitteData.setNameBetreung(betreuung);
		verfuegungszeitabschnitteData.setNameKind(fullName);
		verfuegungszeitabschnitteData.setNummer(betreuungNummer);

		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegungZeitabschnitts) {
			VerfuegungZeitabschnittData verfuegungZeitabschnittData = new VerfuegungZeitabschnittData(verfuegungZeitabschnitt);
			verfuegungszeitabschnitteData.getVerfuegungszeitabschnitte().add(verfuegungZeitabschnittData);
		}

		return verfuegungszeitabschnitteData;
	}

	/**
	 * Helper für init. Speichert Gesuchsperiode in DB
	 */
	@Override
	protected Gesuchsperiode createGesuchsperiode() {
		Gesuchsperiode customGesuchsperiode = TestDataUtil.createCustomGesuchsperiode(BASISJAHR_PLUS_1, BASISJAHR_PLUS_2);
		customGesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		TestDataUtil.saveMandantIfNecessary(persistence, customGesuchsperiode.getMandant());
		gesuchsperiodeService.saveGesuchsperiode(customGesuchsperiode);
		return customGesuchsperiode;
	}
}


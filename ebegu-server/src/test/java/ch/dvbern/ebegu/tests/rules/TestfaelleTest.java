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

package ch.dvbern.ebegu.tests.rules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.ebegu.testfaelle.Testfall05_LuethiMeret;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test der die vom JA	 gemeldeten Testfaelle ueberprueft.
 */
public class TestfaelleTest extends AbstractBGRechnerTest {

	private static final List<InstitutionStammdaten> INSTITUTIONS_STAMMDATEN_LIST = new ArrayList<>();

	private KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter = TestDataUtil.geKitaxUebergangsloesungParameter();

	@BeforeClass
	public static void setup() {
		INSTITUTIONS_STAMMDATEN_LIST.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		INSTITUTIONS_STAMMDATEN_LIST.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		INSTITUTIONS_STAMMDATEN_LIST.add(TestDataUtil.createInstitutionStammdatenTagesfamilien());
	}

	@Test
	public void testfall01_WaeltiDagmar() {
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall01WaeltiDagmar(gesuch);
	}

	@Test
	public void testfall02_FeutzYvonne() {
		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall02FeutzYvonne(gesuch);
	}

	@Test
	public void testfall03_PerreiraMarcia() {
		Testfall03_PerreiraMarcia testfall = new Testfall03_PerreiraMarcia(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall03PerreiraMarcia(gesuch);
	}

	@Test
	public void testfall04_WaltherLaura() {
		Testfall04_WaltherLaura testfall = new Testfall04_WaltherLaura(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall04WaltherLaura(gesuch);
	}

	@Test
	public void testfall05_LuethiMeret() {
		Testfall05_LuethiMeret testfall = new Testfall05_LuethiMeret(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall05LuethiMeret(gesuch);
	}

	@Test
	public void testfall06_BeckerNora() {
		Testfall06_BeckerNora testfall = new Testfall06_BeckerNora(TestDataUtil.createGesuchsperiode1718(),
			INSTITUTIONS_STAMMDATEN_LIST);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		checkTestfall06BeckerNora(gesuch);
	}
}

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall_DoppelKita_Regel1;
import ch.dvbern.ebegu.testfaelle.Testfall_DoppelKita_Regel2;
import ch.dvbern.ebegu.testfaelle.Testfall_DoppelKita_Regel3;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.testfaelle.AbstractTestfall.ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA;

/**
 * Test der die vom JA gemeldeten Testfaelle für Doppelkitas ueberprueft.
 * Siehe https://support.dvbern.ch/browse/EBEGU-561
 */
public class DoppelkitaTest extends AbstractBGRechnerTest {

	/**
	 * Testet Regel 1: Bei gleichzeitigem Beginn gewinnt die Kita mit dem höheren Pensum
	 */
	@Test
	public void testfall_Doppelkita_01() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall_DoppelKita_Regel1 testfall = new Testfall_DoppelKita_Regel1(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter());

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				if (betreuung.getInstitutionStammdaten().getId().equals(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA)) {
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// August
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal(40), 10, new BigDecimal(10));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(50), 20, new BigDecimal(20));
				} else {     //KITA Bruennen
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// August
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal(50), 60, new BigDecimal(50));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(40), 60, new BigDecimal(40));
				}
			}
		}
	}

	/**
	 * Testet Regel 2: Die Kita, deren Betreuung früher beginnt, gewinnt unabhängig von der Höhe des Pensums
	 */
	@Test
	public void testfall_Doppelkita_02() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall_DoppelKita_Regel2 testfall = new Testfall_DoppelKita_Regel2(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter());

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				if (betreuung.getInstitutionStammdaten().getId().equals(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA)) {
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// Erster Monat
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal(40), 60, new BigDecimal(40));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(50), 60, new BigDecimal(50));
				} else {     //KITA Bruennen
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// Erster Monat
					VerfuegungZeitabschnitt okbober = verfuegung.getZeitabschnitte().get(2); // Oktober
					assertZeitabschnitt(okbober, new BigDecimal(50), 20, new BigDecimal(20));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(40), 10, new BigDecimal(10));
				}
			}
		}
	}

	/**
	 * Bei gleichzeitigem Beginn und gleichem Pensum wird die erst eingegebene Kita zuerst bedient
	 */
	@Test
	public void testfall_Doppelkita_03() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall_DoppelKita_Regel3 testfall = new Testfall_DoppelKita_Regel3(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		evaluator.evaluate(gesuch, getParameter());

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Verfuegung verfuegung = betreuung.getVerfuegung();
				Assert.assertNotNull(verfuegung);
				if (betreuung.getInstitutionStammdaten().getId().equals(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA)) {
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// Erster Monat
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal(40), 60, new BigDecimal(40));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(30), 60, new BigDecimal(30));
				} else {     //KITA Bruennen
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					// Erster Monat
					VerfuegungZeitabschnitt august = verfuegung.getZeitabschnitte().get(0);
					assertZeitabschnitt(august, new BigDecimal(40), 20, new BigDecimal(20));
					// Januar
					VerfuegungZeitabschnitt januar = verfuegung.getZeitabschnitte().get(5);
					assertZeitabschnitt(januar, new BigDecimal(50), 30, new BigDecimal(30));
				}
			}
		}
	}
}

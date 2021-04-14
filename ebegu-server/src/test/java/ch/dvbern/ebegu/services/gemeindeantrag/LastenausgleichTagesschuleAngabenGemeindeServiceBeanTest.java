/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.List;

import javax.ejb.EJBException;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
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

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class LastenausgleichTagesschuleAngabenGemeindeServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService angabenGemeindeService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode1920;
	private Gesuchsperiode gesuchsperiode2021;
	private Gemeinde gemeindeParis;

	@Before
	public void setUp() {
		gesuchsperiode1920 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2019, 2020);
		TestDataUtil.prepareParameters(gesuchsperiode1920, persistence);
		gesuchsperiode2021 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2020, 2021);
		TestDataUtil.prepareParameters(gesuchsperiode2021, persistence);
		gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		TestDataUtil.getGemeindeLondon(persistence);
	}

	@Test
	public void createLastenausgleichTagesschuleGemeinde() {
		List<? extends GemeindeAntrag> gemeindeAntragList =
			angabenGemeindeService.createLastenausgleichTagesschuleGemeinde(gesuchsperiode1920);
		Assert.assertNotNull(gemeindeAntragList);
		Assert.assertEquals("Wir erwarten keinen Antrag für die GP 19/20", 0, gemeindeAntragList.size());

		gemeindeAntragList =
			angabenGemeindeService.createLastenausgleichTagesschuleGemeinde(gesuchsperiode2021);
		Assert.assertNotNull(gemeindeAntragList);
		Assert.assertEquals("Wir erwarten einen Antrag für die GP 20/21", 1, gemeindeAntragList.size());


		final GemeindeAntrag gemeindeAntrag = gemeindeAntragList.get(0);
		Assert.assertTrue(
			"Der erstelle Antrag muss vom Typ LastenausgleichTagesschuleAngabenGemeindeContainer sein",
			gemeindeAntrag instanceof LastenausgleichTagesschuleAngabenGemeindeContainer);
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer = (LastenausgleichTagesschuleAngabenGemeindeContainer) gemeindeAntrag;
		Assert.assertEquals(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU, latsGemeindeContainer.getStatus());
	}

	@Test
	public void saveLastenausgleichTagesschuleGemeinde() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode2021, gemeindeParis);
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);

		Assert.assertEquals(
			"Das Objekt wurde neu erstellt: Timestamp erstellt und mutiert muessen gleich sein",
			latsGemeindeContainer.getTimestampErstellt(), latsGemeindeContainer.getTimestampMutiert());

		latsGemeindeContainer.setAlleAngabenInKibonErfasst(true);
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);

		Assert.assertEquals(true, latsGemeindeContainer.getAlleAngabenInKibonErfasst());
		Assert.assertNotEquals(
			"Nach dem zweiten Speichern sollen TimestampErstellt und TimestampMutiert nicht mehr gleich sein",
			latsGemeindeContainer.getTimestampErstellt(),
			latsGemeindeContainer.getTimestampMutiert());
	}

	@Test
	public void lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben_ok() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode2021, gemeindeParis);
		latsGemeindeContainer.setAlleAngabenInKibonErfasst(true);
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);

		Assert.assertEquals(
			LastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
			latsGemeindeContainer.getStatus());

		latsGemeindeContainer = angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(latsGemeindeContainer);

		Assert.assertEquals(
			LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			latsGemeindeContainer.getStatus());
	}

	@Test(expected = EJBException.class)
	public void lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben_nokFalscherStatus() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode2021, gemeindeParis);
		latsGemeindeContainer.setAlleAngabenInKibonErfasst(true);
		latsGemeindeContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);

		// Der Status ist falsch
		Assert.assertEquals(
			LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			latsGemeindeContainer.getStatus());
		// Wir erwarten eine Exception
		angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(latsGemeindeContainer);
	}

	@Test(expected = EJBException.class)
	public void lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben_nokRequiredFields() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode2021, gemeindeParis);
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);

		// Die zwingenden Felder sind nicht erfasst
		Assert.assertNull(latsGemeindeContainer.getAlleAngabenInKibonErfasst());
		// Wir erwarten eine Exception
		angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(latsGemeindeContainer);
	}

	@Test
	public void lastenausgleichTagesschuleGemeindeEinreichen_ok() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode2021, gemeindeParis);
		latsGemeindeContainer.setAlleAngabenInKibonErfasst(true);
		latsGemeindeContainer.setAngabenDeklaration(TestDataUtil.createLastenausgleichTagesschuleAngabenGemeinde());
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);
		latsGemeindeContainer = angabenGemeindeService.lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(latsGemeindeContainer);

		// Eingaben der Gemeinde
		latsGemeindeContainer.setAngabenKorrektur(TestDataUtil.createLastenausgleichTagesschuleAngabenGemeinde());
		latsGemeindeContainer = angabenGemeindeService.saveLastenausgleichTagesschuleGemeinde(latsGemeindeContainer);
		Assert.assertEquals(
			LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			latsGemeindeContainer.getStatus());

		// Einreichen
		angabenGemeindeService.lastenausgleichTagesschuleGemeindeEinreichen(latsGemeindeContainer);
		Assert.assertEquals(
			LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON,
			latsGemeindeContainer.getStatus());
		// Jetzt sollten beide Container vorhanden sein
		Assert.assertNotNull(latsGemeindeContainer.getAngabenKorrektur());
		Assert.assertNotNull(latsGemeindeContainer.getAngabenDeklaration());
	}
}

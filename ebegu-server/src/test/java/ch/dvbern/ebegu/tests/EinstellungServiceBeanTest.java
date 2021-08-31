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

package ch.dvbern.ebegu.tests;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.errors.NoEinstellungFoundException;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
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

import static ch.dvbern.ebegu.test.TestDataUtil.SEQUENCE;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class EinstellungServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode1617;
	private Gesuchsperiode gesuchsperiode1718;

	private Mandant kantonBern;
	private Mandant kantonLuzern;

	private Gemeinde gemeindeParis;
	private Gemeinde gemeindeLondon;
	private Gemeinde gemeindeLuzern;

	@Before
	public void setUp() {
		kantonBern = TestDataUtil.createDefaultMandant();
		kantonBern.setName("Kanton Bern");
		kantonBern = persistence.merge(kantonBern);

		gesuchsperiode1617 = TestDataUtil.createGesuchsperiode1617();
		gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();

		gesuchsperiode1617.setMandant(kantonBern);
		gesuchsperiode1718.setMandant(kantonBern);

		persistence.merge(gesuchsperiode1617);
		persistence.merge(gesuchsperiode1718);

		kantonLuzern = TestDataUtil.createDefaultMandant();
		kantonLuzern.setName("Kanton Luzern");
		kantonLuzern = persistence.merge(kantonLuzern);

		gemeindeParis = TestDataUtil.createGemeindeParis();
		gemeindeParis.setMandant(kantonBern);
		gemeindeLondon = TestDataUtil.createGemeindeLondon();
		gemeindeLondon.setMandant(kantonBern);
		gemeindeLuzern = TestDataUtil.createGemeindeParis();
		gemeindeLuzern.setId("65a0c4a3-80a1-48cd-80af-6bb9fc403f7d");
		gemeindeLuzern.setBfsNummer(SEQUENCE.incrementAndGet());
		gemeindeLuzern.setName("Luzern");
		gemeindeLuzern.setMandant(kantonLuzern);
		persistence.merge(gemeindeParis);
		persistence.merge(gemeindeLondon);
		persistence.merge(gemeindeLuzern);
	}

	@Test
	public void saveEinstellung() {
		Einstellung einstellung = new Einstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, "Value", gesuchsperiode1617, null, null);
		Einstellung mergedEinstellung = einstellungService.saveEinstellung(einstellung);
		Assert.assertNotNull(mergedEinstellung);
		Assert.assertNotNull(mergedEinstellung.getTimestampErstellt());
	}

	@SuppressWarnings("ReuseOfLocalVariable")
	@Test
	public void findEinstellung() {
		// Einstellung für 16/17, ohne Gemeinde, ohne Mandant
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, "System_1617", gesuchsperiode1617, null, null));

		// Suche fuer 16/17: Paris -> ok
		Einstellung einstellungFound =
			einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis,
			gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> dasselbe Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Paris -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}

		// Einstellung für 16/17, Gemeinde Paris, ohne Mandant
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
			"GmdeParis_1617", gesuchsperiode1617, null, gemeindeParis));

		// Suche fuer 16/17: Paris -> Das spezifische Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("GmdeParis_1617", einstellungFound.getValue());
		// Suche fuer 16/17: London -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
			gemeindeLondon, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Paris -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}

		// Einstellung für 16/17, Mandant KantonBern
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, "KantonBern_1617", gesuchsperiode1617, kantonBern, null));

		// Suche fuer 16/17: Bern -> Das spezifische Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("GmdeParis_1617", einstellungFound.getValue());
		// Suche fuer 16/17: London -> der Kantons-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
			gemeindeLondon, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("KantonBern_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Paris -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}
	}

	@Test(expected = NoEinstellungFoundException.class)
	public void findEinstellung_NotFound() {
		// Noch keine Einstellungen
		einstellungService.findEinstellung(EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED, gemeindeParis, gesuchsperiode1617);
	}
}

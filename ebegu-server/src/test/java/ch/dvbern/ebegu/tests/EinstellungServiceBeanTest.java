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
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ch.dvbern.ebegu.test.TestDataUtil.SEQUENCE;

@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
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

	private Gemeinde gemeindeBern;
	private Gemeinde gemeindeOstermundigen;
	private Gemeinde gemeindeLuzern;

	@Before
	public void setUp() {
		gesuchsperiode1617 = TestDataUtil.createGesuchsperiode1617();
		gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();
		persistence.merge(gesuchsperiode1617);
		persistence.merge(gesuchsperiode1718);

		kantonBern = TestDataUtil.createDefaultMandant();
		kantonBern.setName("Kanton Bern");
		kantonBern = persistence.merge(kantonBern);

		kantonLuzern = TestDataUtil.createDefaultMandant();
		kantonLuzern.setName("Kanton Luzern");
		kantonLuzern = persistence.merge(kantonLuzern);

		gemeindeBern = TestDataUtil.createGemeindeBern();
		gemeindeBern.setMandant(kantonBern);
		gemeindeOstermundigen = TestDataUtil.createGemeindeOstermundigen();
		gemeindeOstermundigen.setMandant(kantonBern);
		gemeindeLuzern = TestDataUtil.createGemeindeBern();
		gemeindeLuzern.setId("65a0c4a3-80a1-48cd-80af-6bb9fc403f7d");
		gemeindeLuzern.setBfsNummer(SEQUENCE.incrementAndGet());
		gemeindeLuzern.setName("Luzern");
		gemeindeLuzern.setMandant(kantonLuzern);
		persistence.merge(gemeindeBern);
		persistence.merge(gemeindeOstermundigen);
		persistence.merge(gemeindeLuzern);
	}

	@Test
	public void saveEinstellung() {
		Einstellung einstellung = new Einstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, "Value", gesuchsperiode1617, null, null);
		Einstellung mergedEinstellung = einstellungService.saveEinstellung(einstellung);
		Assert.assertNotNull(mergedEinstellung);
		Assert.assertNotNull(mergedEinstellung.getTimestampErstellt());
	}

	@SuppressWarnings("ReuseOfLocalVariable")
	@Test
	public void findEinstellung() {
		// Einstellung für 16/17, ohne Gemeinde, ohne Mandant
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, "System_1617", gesuchsperiode1617, null, null));

		// Suche fuer 16/17: Bern -> ok
		Einstellung einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern,
			gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> dasselbe Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Bern -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}

		// Einstellung für 16/17, Gemeinde BERN, ohne Mandant
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, "GmdeBern_1617", gesuchsperiode1617, null, gemeindeBern));

		// Suche fuer 16/17: Bern -> Das spezifische Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("GmdeBern_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Ostermundigen -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeOstermundigen, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Bern -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}

		// Einstellung für 16/17, Mandant KantonBern
		einstellungService.saveEinstellung(new Einstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, "KantonBern_1617", gesuchsperiode1617, kantonBern, null));

		// Suche fuer 16/17: Bern -> Das spezifische Resultat
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("GmdeBern_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Ostermundigen -> der Kantons-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeOstermundigen, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("KantonBern_1617", einstellungFound.getValue());
		// Suche fuer 16/17: Luzern -> immer noch der System-Wert
		einstellungFound = einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeLuzern, gesuchsperiode1617);
		Assert.assertNotNull(einstellungFound);
		Assert.assertEquals("System_1617", einstellungFound.getValue());
		// Suche fuer 17/18: Bern -> nok
		try {
			einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1718);
		} catch (NoEinstellungFoundException nefe) {
			Assert.assertNotNull(nefe);
		}
	}

	@Test(expected = NoEinstellungFoundException.class)
	public void findEinstellung_NotFound() {
		// Noch keine Einstellungen
		einstellungService.findEinstellung(EinstellungKey.KONTINGENTIERUNG_ENABLED, gemeindeBern, gesuchsperiode1617);
	}
}

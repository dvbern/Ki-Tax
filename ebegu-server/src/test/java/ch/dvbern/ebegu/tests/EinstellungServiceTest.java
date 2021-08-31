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

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
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

/**
 * Testet den EinstellungService.
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class EinstellungServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private Persistence persistence;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	private static final EinstellungKey PARAM_KEY = EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
	private Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1617();
	private Gemeinde gemeinde = null;

	@Before
	public void setUp() {
		gesuchsperiode = gesuchsperiodeService.saveGesuchsperiode(gesuchsperiode);
		gemeinde = TestDataUtil.getGemeindeParis(persistence);;
	}

	@Test
	public void createEinstellungTest() {
		Assert.assertNotNull(einstellungService);
		Einstellung insertedEinstellung = createAndPersistParameter(PARAM_KEY, gesuchsperiode);

		Collection<Einstellung> allEinstellung = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Assert.assertEquals(1, allEinstellung.size());
		Einstellung nextEinstellung = allEinstellung.iterator().next();
		Assert.assertEquals(insertedEinstellung.getKey(), nextEinstellung.getKey());
		Assert.assertEquals(insertedEinstellung.getValue(), nextEinstellung.getValue());
	}

	@Test
	public void createEinstellungDuplicateTest() {
		createAndPersistParameter(PARAM_KEY, gesuchsperiode);
		try {
			createAndPersistParameter(PARAM_KEY, gesuchsperiode);
			Assert.fail("It cannot create the same Einstellung twice. An Exception should've been thrown");
		} catch(Exception e) {
			//nop
		}
	}

	@Test
	public void updateEinstellungTest() {
		Assert.assertNotNull(einstellungService);
		Einstellung insertedEinstellung = createAndPersistParameter(PARAM_KEY, gesuchsperiode);

		Optional<Einstellung> EinstellungOptional = einstellungService.findEinstellung(insertedEinstellung.getId());
		Assert.assertTrue(EinstellungOptional.isPresent());
		Einstellung persistedInstStammdaten = EinstellungOptional.get();
		Assert.assertEquals(insertedEinstellung.getValue(), persistedInstStammdaten.getValue());

		persistedInstStammdaten.setValue("Mein Test Wert");
		Einstellung updatedEinstellung = einstellungService.saveEinstellung(persistedInstStammdaten);
		Assert.assertEquals(persistedInstStammdaten.getValue(), updatedEinstellung.getValue());
	}


	@Test
	public void saveEinstellung() {
		// Noch keine Params
		Collection<Einstellung> allParameter = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Einstellung currentParameter = null;
		Assert.assertTrue(allParameter.isEmpty());
		try {
			einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);
			Assert.fail("Dieser Aufruf muss zu einer Exception führen");
		} catch (Exception ignored) {
			// nop, expected
		}

		Einstellung param1 = TestDataUtil.createDefaultEinstellung(PARAM_KEY, gesuchsperiode);
		einstellungService.saveEinstellung(param1);

		allParameter = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		currentParameter = einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);

		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertNotNull(currentParameter);

		Assert.assertEquals(gesuchsperiode, currentParameter.getGesuchsperiode());
	}

	@Test
	public void getEinstellungByGesuchsperiode() {
		Collection<Einstellung> allParameter = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Assert.assertTrue(allParameter.isEmpty());

		createAndPersistParameter(PARAM_KEY, gesuchsperiode);

		allParameter = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(1, allParameter.size());
	}

	@Test
	public void copyEinstellungListToNewGesuchsperiode() {

		createAndPersistParameter(PARAM_KEY, gesuchsperiode);
		Collection<Einstellung> allParameter = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(1, allParameter.size());

		Gesuchsperiode gesuchsperiode18 = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);

		einstellungService.copyEinstellungenToNewGesuchsperiode(gesuchsperiode18, gesuchsperiode);

		Collection<Einstellung> allParameter2 = einstellungService.getAllEinstellungenBySystem(gesuchsperiode);
		Assert.assertFalse(allParameter2.isEmpty());
		Assert.assertEquals(1, allParameter2.size());

		Collection<Einstellung> allParameter18 = einstellungService.getAllEinstellungenBySystem(gesuchsperiode18);
		Assert.assertFalse(allParameter18.isEmpty());
		Assert.assertEquals(1, allParameter18.size());
		Assert.assertEquals(gesuchsperiode18, allParameter18.iterator().next().getGesuchsperiode());
	}

	@Test
	public void getEinstellungByKeyGemeindeAndGesuchsperiode() {
		Einstellung param1 = TestDataUtil.createDefaultEinstellung(PARAM_KEY, gesuchsperiode);
		einstellungService.saveEinstellung(param1);

		Einstellung einstellungFound = einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);
		Assert.assertNotNull(einstellungFound);
	}

	private Einstellung createAndPersistParameter(EinstellungKey paramAnzalTageMaxKita, Gesuchsperiode gesuchsperiode) {
		Einstellung parameter = TestDataUtil.createDefaultEinstellung(paramAnzalTageMaxKita, gesuchsperiode);
		return einstellungService.saveEinstellung(parameter);
	}
}

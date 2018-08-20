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
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testet den EinstellungService.
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class EinstellungServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private Persistence persistence;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	private static final EinstellungKey PARAM_KEY = EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA;
	private Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1617();;
	private final Gemeinde gemeinde = TestDataUtil.getGemeindeBern(persistence);;

	@Before
	public void setUp() {
		gesuchsperiode = gesuchsperiodeService.saveGesuchsperiode(gesuchsperiode);
	}

	@Test
	public void createEinstellungTest() {
		Assert.assertNotNull(einstellungService);
		Einstellung insertedEinstellung = createAndPersistParameter(PARAM_KEY, gesuchsperiode);

		Collection<Einstellung> allEinstellung = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Assert.assertEquals(1, allEinstellung.size());
		Einstellung nextEinstellung = allEinstellung.iterator().next();
		Assert.assertEquals(insertedEinstellung.getKey(), nextEinstellung.getKey());
		Assert.assertEquals(insertedEinstellung.getValue(), nextEinstellung.getValue());
	}

	@Test
	public void createEinstellungDuplicateTest() {
		createAndPersistParameter(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);
		try {
			createAndPersistParameter(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);
			Assert.fail("It cannot create the same Einstellung twice. An Exception should've been thrown");
		} catch(Exception e) {
			//nop
		}
	}

	@Test
	public void updateEinstellungTest() {
		Assert.assertNotNull(einstellungService);
		Einstellung insertedEinstellung = createAndPersistParameter(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);

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
		Collection<Einstellung> allParameter = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Einstellung currentParameter = null;
		Assert.assertTrue(allParameter.isEmpty());
		try {
			einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);
			Assert.fail("Dieser Aufruf muss zu einer Exception führen");
		} catch (Exception ignored) {
			// nop, expected
		}

		Einstellung param1 = TestDataUtil.createDefaultEinstellung(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);
		einstellungService.saveEinstellung(param1);

		allParameter = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		currentParameter = einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);

		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertNotNull(currentParameter);

		Assert.assertEquals(gesuchsperiode, currentParameter.getGesuchsperiode());
	}

	@Test
	public void getEinstellungByGesuchsperiode() {
		Collection<Einstellung> allParameter = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Assert.assertTrue(allParameter.isEmpty());

		createAndPersistParameter(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);

		allParameter = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(1, allParameter.size());
	}

	@Test
	public void copyEinstellungListToNewGesuchsperiode() {

		createAndPersistParameter(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);
		Collection<Einstellung> allParameter = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(1, allParameter.size());

		Gesuchsperiode gesuchsperiode18 = TestDataUtil.createGesuchsperiode1718();
		gesuchsperiode18.setGueltigkeit(Constants.GESUCHSPERIODE_18_19);

		einstellungService.copyEinstellungenToNewGesuchsperiode(gesuchsperiode, gesuchsperiode18);

		Collection<Einstellung> allParameter2 = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode);
		Assert.assertFalse(allParameter2.isEmpty());
		Assert.assertEquals(1, allParameter2.size());

		Collection<Einstellung> allParameter18 = einstellungService.getEinstellungenByGesuchsperiode(gesuchsperiode18);
		Assert.assertFalse(allParameter18.isEmpty());
		Assert.assertEquals(1, allParameter18.size());
		Assert.assertEquals(gesuchsperiode18, allParameter18.iterator().next().getGesuchsperiode());
	}

	@Test
	public void getEinstellungByKeyGemeindeAndGesuchsperiode() {
		Einstellung param1 = TestDataUtil.createDefaultEinstellung(EinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode);
		einstellungService.saveEinstellung(param1);

		Einstellung einstellungFound = einstellungService.findEinstellung(EinstellungServiceTest.PARAM_KEY, gemeinde, gesuchsperiode);
		Assert.assertNotNull(einstellungFound);
	}

	private Einstellung createAndPersistParameter(EinstellungKey paramAnzalTageMaxKita, Gesuchsperiode gesuchsperiode) {
		Einstellung parameter = TestDataUtil.createDefaultEinstellung(paramAnzalTageMaxKita, gesuchsperiode);
		parameter.setGesuchsperiode(gesuchsperiode);
		return einstellungService.saveEinstellung(parameter);
	}
}

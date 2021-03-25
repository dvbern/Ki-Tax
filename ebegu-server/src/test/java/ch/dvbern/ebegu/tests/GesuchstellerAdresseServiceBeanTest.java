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

import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.services.GesuchstellerAdresseService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests fuer die Klasse AdresseService
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class GesuchstellerAdresseServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private GesuchstellerAdresseService adresseService;

	@Inject
	private Persistence persistence;

	@Test
	public void createAdresseTogetherWithGesuchstellerTest() {
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		GesuchstellerContainer storedGesuchsteller = persistence.persist(gesuchsteller);
		Assert.assertNotNull(storedGesuchsteller.getAdressen());
		Assert.assertTrue(storedGesuchsteller.getAdressen().stream().findAny().isPresent());

	}

	@Test
	public void updateAdresseTest() {
		Assert.assertNotNull(adresseService);
		GesuchstellerAdresseContainer insertedAdresses = insertNewEntity();
		Optional<GesuchstellerAdresseContainer> adresse = adresseService.findAdresse(insertedAdresses.getId());
		Assert.assertTrue(adresse.isPresent());
		Assert.assertEquals("21", adresse.get().extractHausnummer());
		GesuchstellerAdresse gesuchstellerAdresseJA = adresse.get().getGesuchstellerAdresseJA();
		Assert.assertNotNull(gesuchstellerAdresseJA);
		gesuchstellerAdresseJA.setHausnummer("99");
		GesuchstellerAdresseContainer updatedAdr = adresseService.updateAdresse(adresse.get());
		Assert.assertEquals("99", updatedAdr.extractHausnummer());
		Assert.assertEquals("99", adresseService.findAdresse(updatedAdr.getId()).get().extractHausnummer());
	}

	@Test
	public void findKorrAndRechnungsAddresse() {
		Assert.assertNotNull(adresseService);
		final GesuchstellerContainer gesuchstellerContainer = insertNewEntityWithKorrespondenzAndRechnungsAdresse();
		Optional<GesuchstellerAdresseContainer> korrespondenzAdr = adresseService.getKorrespondenzAdr(gesuchstellerContainer.getId());
		Optional<GesuchstellerAdresseContainer> rechnungsAdr = adresseService.getRechnungsAdr(gesuchstellerContainer.getId());
		Assert.assertTrue(korrespondenzAdr.isPresent());
		Assert.assertTrue(rechnungsAdr.isPresent());
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Test
	public void removeAdresseTest() {
		Assert.assertNotNull(adresseService);
		// 1 Adresse wird schon mit dem Gesuch erstellt. Wir fuegen eine zweite ein:
		GesuchstellerAdresseContainer insertedAdresses = insertNewEntity();
		Assert.assertEquals(2, adresseService.getAllAdressen().size());
		adresseService.removeAdresse(insertedAdresses);
		Assert.assertEquals(1, adresseService.getAllAdressen().size());
	}

	// Help Methods
	private GesuchstellerAdresseContainer insertNewEntity() {
		TestDataUtil.createAndPersistGesuch(persistence);
		GesuchstellerContainer pers = TestDataUtil.createDefaultGesuchstellerContainer();
		GesuchstellerContainer storedPers = persistence.persist(pers);
		return storedPers.getAdressen().stream().findAny().orElseThrow(() -> new IllegalStateException("Testdaten nicht korrekt aufgesetzt"));
	}

	private GesuchstellerContainer insertNewEntityWithKorrespondenzAndRechnungsAdresse() {
		GesuchstellerContainer pers = TestDataUtil.createDefaultGesuchstellerContainer();
		GesuchstellerContainer storedPers = persistence.persist(pers);

		GesuchstellerAdresseContainer korrAddr = TestDataUtil.createDefaultGesuchstellerAdresseContainer(storedPers);
		GesuchstellerAdresse gesuchstellerAdresseJA = korrAddr.getGesuchstellerAdresseJA();
		Assert.assertNotNull(gesuchstellerAdresseJA);
		gesuchstellerAdresseJA.setAdresseTyp(AdresseTyp.KORRESPONDENZADRESSE);
		storedPers.addAdresse(korrAddr);
		GesuchstellerAdresse gsKorrAddresse = gesuchstellerAdresseJA.copyGesuchstellerAdresse(new GesuchstellerAdresse(), AntragCopyType.MUTATION);
		korrAddr.setGesuchstellerAdresseGS(gsKorrAddresse);
		korrAddr.setGesuchstellerAdresseJA(null);

		GesuchstellerAdresseContainer rechnungsAddr = TestDataUtil.createDefaultGesuchstellerAdresseContainer(storedPers);
		GesuchstellerAdresse rechnungsAdresseJA = rechnungsAddr.getGesuchstellerAdresseJA();
		Assert.assertNotNull(rechnungsAdresseJA);
		rechnungsAdresseJA.setAdresseTyp(AdresseTyp.RECHNUNGSADRESSE);
		storedPers.addAdresse(rechnungsAddr);
		GesuchstellerAdresse gsRechnungsAddresse = rechnungsAdresseJA.copyGesuchstellerAdresse(new GesuchstellerAdresse(), AntragCopyType.MUTATION);
		rechnungsAddr.setGesuchstellerAdresseGS(gsRechnungsAddresse);
		rechnungsAddr.setGesuchstellerAdresseJA(null);

		return persistence.merge(storedPers);
	}

}

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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests fuer die Klasse GesuchstellerService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class GesuchstellerServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private Persistence persistence;

	@Test
	public void createGesuchsteller() {
		Assert.assertNotNull(gesuchstellerService);
		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer(gesuch);

		gesuchstellerService.saveGesuchsteller(gesuchsteller, TestDataUtil.createDefaultGesuch(), 1, false);
		Collection<GesuchstellerContainer> allGesuchsteller = gesuchstellerService.getAllGesuchsteller();
		Assert.assertEquals(1, allGesuchsteller.size());
		GesuchstellerContainer nextGesuchsteller = allGesuchsteller.iterator().next();
		Assert.assertEquals("Tester", nextGesuchsteller.extractNachname());
		Assert.assertEquals("tim.tester@mailbucket.dvbern.ch", nextGesuchsteller.getGesuchstellerJA().getMail());
	}

	@Test
	public void updateGesuchstellerTest() {
		Assert.assertNotNull(gesuchstellerService);
		GesuchstellerContainer insertedGesuchsteller = insertNewEntity();
		Optional<GesuchstellerContainer> gesuchstellerOptional = gesuchstellerService.findGesuchsteller(insertedGesuchsteller.getId());
		Assert.assertTrue(gesuchstellerOptional.isPresent());
		GesuchstellerContainer gesuchsteller = gesuchstellerOptional.get();
		Assert.assertEquals("tim.tester@mailbucket.dvbern.ch", gesuchsteller.getGesuchstellerJA().getMail());

		gesuchsteller.getGesuchstellerJA().setMail("fritz.mueller@mailbucket.dvbern.ch");
		GesuchstellerContainer updatedGesuchsteller = gesuchstellerService.saveGesuchsteller(gesuchsteller, TestDataUtil.createDefaultGesuch(), 1, false);
		Assert.assertEquals("fritz.mueller@mailbucket.dvbern.ch", updatedGesuchsteller.getGesuchstellerJA().getMail());
	}

	@Test
	public void removeGesuchstellerTest() {
		Assert.assertNotNull(gesuchstellerService);
		GesuchstellerContainer insertedGesuchsteller = insertNewEntity();
		Assert.assertEquals(1, gesuchstellerService.getAllGesuchsteller().size());

		gesuchstellerService.removeGesuchsteller(insertedGesuchsteller);
		Assert.assertEquals(0, gesuchstellerService.getAllGesuchsteller().size());
	}

	@Test
	public void createGesuchstellerWithEinkommensverschlechterung() {
		Assert.assertNotNull(gesuchstellerService);
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerWithEinkommensverschlechterung();

		gesuchstellerService.saveGesuchsteller(gesuchsteller, TestDataUtil.createDefaultGesuch(), 1, false);
		Collection<GesuchstellerContainer> allGesuchsteller = gesuchstellerService.getAllGesuchsteller();
		Assert.assertEquals(1, allGesuchsteller.size());

		GesuchstellerContainer nextGesuchsteller = allGesuchsteller.iterator().next();
		final EinkommensverschlechterungContainer einkommensverschlechterungContainer = nextGesuchsteller.getEinkommensverschlechterungContainer();
		Assert.assertNotNull(einkommensverschlechterungContainer);

		final Einkommensverschlechterung ekvGSBasisJahrPlus1 = einkommensverschlechterungContainer.getEkvGSBasisJahrPlus1();
		Assert.assertNotNull(ekvGSBasisJahrPlus1);
		Assert.assertEquals(0, ekvGSBasisJahrPlus1.getNettolohn().compareTo(BigDecimal.ONE));

		final Einkommensverschlechterung ekvGSBasisJahrPlus2 = einkommensverschlechterungContainer.getEkvGSBasisJahrPlus2();
		Assert.assertNotNull(ekvGSBasisJahrPlus2);
		Assert.assertEquals(0, ekvGSBasisJahrPlus2.getNettolohn().compareTo(BigDecimal.valueOf(2)));

		final Einkommensverschlechterung ekvJABasisJahrPlus1 = einkommensverschlechterungContainer.getEkvJABasisJahrPlus1();
		Assert.assertNotNull(ekvJABasisJahrPlus1);
		Assert.assertEquals(0, ekvJABasisJahrPlus1.getNettolohn().compareTo(BigDecimal.valueOf(3)));

		final Einkommensverschlechterung ekvJABasisJahrPlus2 = einkommensverschlechterungContainer.getEkvJABasisJahrPlus2();
		Assert.assertNotNull(ekvJABasisJahrPlus2);
		Assert.assertEquals(0, ekvJABasisJahrPlus2.getNettolohn().compareTo(BigDecimal.valueOf(4)));

	}

	@Test
	public void testSaveGesuchsteller2Mutation() {
		GesuchstellerContainer gesuchsteller = insertNewEntity();
		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setTyp(AntragTyp.MUTATION);

		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer(gesuch));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(TestDataUtil.createFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(TestDataUtil.createDefaultEinkommensverschlechterungsContainer());

		final GesuchstellerContainer savedGesuchsteller = gesuchstellerService.saveGesuchsteller(gesuchsteller, gesuch, 2, false);

		Assert.assertNotNull(savedGesuchsteller.getFinanzielleSituationContainer());
		Assert.assertNotNull(savedGesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA());
		Assert.assertFalse(savedGesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA().getSteuererklaerungAusgefuellt());
		Assert.assertFalse(savedGesuchsteller.getFinanzielleSituationContainer().getFinanzielleSituationJA().getSteuerveranlagungErhalten());
		Assert.assertEquals(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getJahr(),
			savedGesuchsteller.getFinanzielleSituationContainer().getJahr());

		Assert.assertNotNull(savedGesuchsteller.getEinkommensverschlechterungContainer());
		Assert.assertNotNull(savedGesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		Assert.assertFalse(savedGesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().getSteuererklaerungAusgefuellt());
		Assert.assertFalse(savedGesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().getSteuerveranlagungErhalten());
	}

	// Helper Methods

	private GesuchstellerContainer insertNewEntity() {
		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer(gesuch);
		persistence.persist(gesuchsteller);
		return gesuchsteller;
	}

}

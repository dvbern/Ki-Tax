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
import ch.dvbern.ebegu.services.EinkommensverschlechterungService;
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
 * Tests fuer die Klasse FinanzielleSituationService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class EinkommensverschlechterungServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private EinkommensverschlechterungService einkommensverschlechterungService;

	@Inject
	private Persistence persistence;

	@Test
	public void createEinkommensverschlechterungContainerTest() {
		Assert.assertNotNull(einkommensverschlechterungService);

		EinkommensverschlechterungContainer container = getEinkommensverschlechterungContainer();

		einkommensverschlechterungService.saveEinkommensverschlechterungContainer(container, null);

		Collection<EinkommensverschlechterungContainer> allEinkommensverschlechterungContainer = einkommensverschlechterungService.getAllEinkommensverschlechterungContainer();
		Assert.assertEquals(1, allEinkommensverschlechterungContainer.size());
		EinkommensverschlechterungContainer einkommensverschlechterungContainer = allEinkommensverschlechterungContainer.iterator().next();
		Assert.assertEquals(0, einkommensverschlechterungContainer.getEkvGSBasisJahrPlus1().getNettolohn().compareTo(BigDecimal.ONE));
	}

	private EinkommensverschlechterungContainer getEinkommensverschlechterungContainer() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller = persistence.persist(gesuchsteller);

		final Einkommensverschlechterung einkommensverschlechterung = TestDataUtil.createDefaultEinkommensverschlechterung();

		EinkommensverschlechterungContainer container = TestDataUtil.createDefaultEinkommensverschlechterungsContainer();
		container.setGesuchsteller(gesuchsteller);
		return container;
	}

	/**
	 * 1. Create a container
	 * 2. Store created Container on DB
	 * 3. get Stored Container from DB by calling getAll
	 * 4. Change Stored Container
	 * 5. Update Container on DB
	 * 6. get Stored Container from DB by calling find
	 * <p>
	 * Expected result: new result must be the updated value
	 */
	@Test
	public void updateAndFindEinkommensverschlechterungContainerTest() {
		Assert.assertNotNull(einkommensverschlechterungService);

		EinkommensverschlechterungContainer container = getEinkommensverschlechterungContainer();

		einkommensverschlechterungService.saveEinkommensverschlechterungContainer(container, null);

		Collection<EinkommensverschlechterungContainer> allEinkommensverschlechterungContainer = einkommensverschlechterungService.getAllEinkommensverschlechterungContainer();
		EinkommensverschlechterungContainer einkommensverschlechterungContainer = allEinkommensverschlechterungContainer.iterator().next();

		einkommensverschlechterungContainer.getEkvGSBasisJahrPlus1().setNettolohn(BigDecimal.TEN);

		einkommensverschlechterungService.saveEinkommensverschlechterungContainer(einkommensverschlechterungContainer, null);

		final Optional<EinkommensverschlechterungContainer> einkommensverschlechterungContainerUpdated = einkommensverschlechterungService.findEinkommensverschlechterungContainer(einkommensverschlechterungContainer.getId());

		if (einkommensverschlechterungContainerUpdated.isPresent()) {
			final EinkommensverschlechterungContainer container1 = einkommensverschlechterungContainerUpdated.get();
			Assert.assertNotNull(container1);
			Assert.assertEquals(0, container1.getEkvGSBasisJahrPlus1().getNettolohn().compareTo(BigDecimal.TEN));
		} else {
			Assert.fail("Einkommensverschlechterungsinfo konnte nicht aktualisiert werden");
		}
	}

	@Test
	public void removeEinkommensverschlechterungContainerTest() {
		Assert.assertNotNull(einkommensverschlechterungService);
		Assert.assertEquals(0, einkommensverschlechterungService.getAllEinkommensverschlechterungContainer().size());

		EinkommensverschlechterungContainer container = getEinkommensverschlechterungContainer();
		einkommensverschlechterungService.saveEinkommensverschlechterungContainer(container, null);
		Assert.assertEquals(1, einkommensverschlechterungService.getAllEinkommensverschlechterungContainer().size());

		einkommensverschlechterungService.removeEinkommensverschlechterungContainer(container);
		Assert.assertEquals(0, einkommensverschlechterungService.getAllEinkommensverschlechterungContainer().size());
	}

	@Test
	public void calculateProzentualeDifferenz() {
		Assert.assertEquals("-0", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(0), BigDecimal.valueOf(0)));
		Assert.assertEquals("-0", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(100), BigDecimal.valueOf(100)));
		Assert.assertEquals("+100", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(100), BigDecimal.valueOf(200)));
		Assert.assertEquals("-50", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(200), BigDecimal.valueOf(100)));
		Assert.assertEquals("-90", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(200), BigDecimal.valueOf(20)));
		Assert.assertEquals("-81", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(59720), BigDecimal.valueOf(11230)));
		Assert.assertEquals("-100", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(59720), BigDecimal.valueOf(0)));
		Assert.assertEquals("+100", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(0), BigDecimal.valueOf(59720)));
		Assert.assertEquals("-20", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(70000), BigDecimal.valueOf(56000)));
		Assert.assertEquals("-20", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(70000), BigDecimal.valueOf(55999)));
		Assert.assertEquals("-19", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(146874)));
		Assert.assertEquals("-22", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(140668)));
		Assert.assertEquals("-20", einkommensverschlechterungService.calculateProzentualeDifferenz(BigDecimal.valueOf(181760), BigDecimal.valueOf(144336)));
	}
}

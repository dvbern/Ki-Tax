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

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
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
public class FinanzielleSituationServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	private Gesuch gesuch;

	@Test
	public void createFinanzielleSituation() {
		Assert.assertNotNull(finanzielleSituationService);

		FinanzielleSituation finanzielleSituation = TestDataUtil.createDefaultFinanzielleSituation();
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller = persistence.persist(gesuchsteller);

		FinanzielleSituationContainer container = TestDataUtil.createFinanzielleSituationContainer();
		container.setFinanzielleSituationGS(finanzielleSituation);
		container.setGesuchsteller(gesuchsteller);
		gesuch.setGesuchsteller1(gesuchsteller);
		persistence.merge(gesuch);

		finanzielleSituationService.saveFinanzielleSituation(container, gesuch.getId());
		Collection<FinanzielleSituationContainer> allFinanzielleSituationen =
			criteriaQueryHelper.getAll(FinanzielleSituationContainer.class);
		Assert.assertEquals(1, allFinanzielleSituationen.size());
		FinanzielleSituationContainer nextFinanzielleSituation = allFinanzielleSituationen.iterator().next();
		Assert.assertNotNull(nextFinanzielleSituation.getFinanzielleSituationGS().getNettolohn());
		Assert.assertEquals(100000L, nextFinanzielleSituation.getFinanzielleSituationGS().getNettolohn().longValue());
	}

	@Test
	public void updateFinanzielleSituationTest() {
		Assert.assertNotNull(finanzielleSituationService);
		FinanzielleSituationContainer insertedFinanzielleSituations = insertNewEntity();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Optional<FinanzielleSituationContainer> finanzielleSituationOptional = finanzielleSituationService.findFinanzielleSituation(insertedFinanzielleSituations.getId());
		Assert.assertTrue(finanzielleSituationOptional.isPresent());
		FinanzielleSituationContainer finanzielleSituation = finanzielleSituationOptional.get();
		finanzielleSituation.setFinanzielleSituationGS(TestDataUtil.createDefaultFinanzielleSituation());

		Assert.assertNotNull(gesuch.getGesuchsteller1());
		FinanzielleSituationContainer updatedCont = finanzielleSituationService.saveFinanzielleSituation(
			finanzielleSituation, gesuch.getId());
		Assert.assertNotNull(updatedCont.getFinanzielleSituationGS().getNettolohn());
		Assert.assertEquals(100000L, updatedCont.getFinanzielleSituationGS().getNettolohn().longValue());

		updatedCont.getFinanzielleSituationGS().setNettolohn(new BigDecimal(200000));
		FinanzielleSituationContainer contUpdTwice = finanzielleSituationService.saveFinanzielleSituation(
			updatedCont, gesuch.getId());
		Assert.assertNotNull(contUpdTwice.getFinanzielleSituationGS().getNettolohn());
		Assert.assertEquals(200000L, contUpdTwice.getFinanzielleSituationGS().getNettolohn().longValue());
	}

	private FinanzielleSituationContainer insertNewEntity() {
		this.gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		FinanzielleSituationContainer container = TestDataUtil.createFinanzielleSituationContainer();
		gesuchsteller.setFinanzielleSituationContainer(container);
		this.gesuch.setGesuchsteller1(gesuchsteller);
		persistence.merge(gesuch);
		return gesuchsteller.getFinanzielleSituationContainer();
	}
}

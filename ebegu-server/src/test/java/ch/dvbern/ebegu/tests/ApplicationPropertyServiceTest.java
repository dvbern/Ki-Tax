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

import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.mocks.CriteriaQueryHelperMock;
import ch.dvbern.ebegu.mocks.PersistenceMock;
import ch.dvbern.ebegu.services.ApplicationPropertyServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.needle4j.annotation.InjectIntoMany;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("unused")
public class ApplicationPropertyServiceTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
	@ObjectUnderTest
	private ApplicationPropertyServiceBean applicationPropertyService;

	@InjectIntoMany
	private CriteriaQueryHelperMock criteriaQueryHelper = new CriteriaQueryHelperMock();

	@InjectIntoMany
	private PersistenceMock persistence = new PersistenceMock();


	@Test
	public void saveOrUpdateApplicationPropertyTest() {
		Assert.assertNotNull(applicationPropertyService);
		Mandant mandant = TestDataUtil.getMandantKantonBern(persistence);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, "testValue", mandant);
		Assert.assertEquals(1, applicationPropertyService.getAllApplicationProperties().size());
		Optional<ApplicationProperty> propertyOptional = applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED);
		Assert.assertTrue(propertyOptional.isPresent());
		Assert.assertEquals("testValue", propertyOptional.get().getValue());
	}

	@Test
	public void removeApplicationPropertyTest() {
		insertNewEntity();
		applicationPropertyService.removeApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED);
		Assert.assertEquals(0, applicationPropertyService.getAllApplicationProperties().size());
	}

	@Test
	public void updateApplicationPropertyTest() {
		Mandant mandant = TestDataUtil.getMandantKantonBern(persistence);
		insertNewEntity();
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, "changed", mandant);
		Optional<ApplicationProperty> propertyOptional = applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED);
		Assert.assertTrue(propertyOptional.isPresent());
		Assert.assertEquals("changed", propertyOptional.get().getValue());
	}

	private void insertNewEntity() {
		Mandant mandant = TestDataUtil.getMandantKantonBern(persistence);
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, "testValue", mandant);
		Assert.assertEquals(1, applicationPropertyService.getAllApplicationProperties().size());
		Optional<ApplicationProperty> propertyOptional = applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED);
		Assert.assertNotNull(propertyOptional);
		Assert.assertTrue(propertyOptional.isPresent());
		Assert.assertEquals("testValue", propertyOptional.get().getValue());
	}
}

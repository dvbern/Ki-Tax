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

import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.mocks.CriteriaQueryHelperMock;
import ch.dvbern.ebegu.mocks.PersistenceMock;
import ch.dvbern.ebegu.services.TraegerschaftServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.needle4j.annotation.InjectIntoMany;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests fuer die Klasse TraegerschaftService
 */
public class TraegerschaftServiceTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@ObjectUnderTest
	private TraegerschaftServiceBean traegerschaftService;

	@InjectIntoMany
	private final PersistenceMock persistence = new PersistenceMock();

	@InjectIntoMany
	private final CriteriaQueryHelperMock criteriaQueryHelper = new CriteriaQueryHelperMock();


	@Test
	public void createTraegerschaft() {
		Assert.assertNotNull(traegerschaftService);
		Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();

		traegerschaftService.saveTraegerschaft(traegerschaft);
		Optional<Traegerschaft> traegerschaftOpt = traegerschaftService.findTraegerschaft(traegerschaft.getId());
		Assert.assertTrue(traegerschaftOpt.isPresent());
		Assert.assertEquals("Traegerschaft1", traegerschaftOpt.get().getName());
	}

	@Test
	public void removeTraegerschaft() {
		Assert.assertNotNull(traegerschaftService);
		Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();

		traegerschaftService.saveTraegerschaft(traegerschaft);
		Assert.assertTrue(traegerschaftService.findTraegerschaft(traegerschaft.getId()).isPresent());
		traegerschaftService.removeTraegerschaft(traegerschaft.getId());
		Assert.assertFalse(traegerschaftService.findTraegerschaft(traegerschaft.getId()).isPresent());
	}
}

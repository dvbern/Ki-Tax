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

import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
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
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class EinkommensverschlechterungInfoServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private EinkommensverschlechterungInfoService einkommensverschlechterungInfoService;

	@Inject
	private Persistence persistence;

	@Test
	public void createEinkommensverschlechterungInfoTest() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);

		TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(gesuch);
		Assert.assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
		einkommensverschlechterungInfoService.createEinkommensverschlechterungInfo(gesuch.getEinkommensverschlechterungInfoContainer());

		Assert.assertNotNull(gesuch);
		Assert.assertNotNull(gesuch.extractEinkommensverschlechterungInfo());

		Collection<EinkommensverschlechterungInfoContainer> allEinkommensverschlechterungInfo = einkommensverschlechterungInfoService.getAllEinkommensverschlechterungInfo();
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfo = allEinkommensverschlechterungInfo.iterator().next();
		Assert.assertNotNull(einkommensverschlechterungInfo);
	}

	private EinkommensverschlechterungInfoContainer persistAndGetEinkommensverschlechterungInfoOnGesuch() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.IN_BEARBEITUNG_JA);
		gesuch.setEinkommensverschlechterungInfoContainer(TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(gesuch));
		gesuch = persistence.merge(gesuch);

		Assert.assertNotNull(gesuch.getEinkommensverschlechterungInfoContainer());
		return gesuch.getEinkommensverschlechterungInfoContainer();
	}

	/**
	 * 1. Create a info
	 * 2. Store created Info on DB
	 * 3. get Stored Info from DB by calling getAll
	 * 4. Change Stored Info
	 * 5. Update Info on DB
	 * 6. get Stored Info from DB by calling find
	 * <p>
	 * Expected result: new result must be the updated value
	 */
	@Test
	public void updateAndFindEinkommensverschlechterungInfoTest() {
		Assert.assertNotNull(einkommensverschlechterungInfoService);

		persistAndGetEinkommensverschlechterungInfoOnGesuch();

		Collection<EinkommensverschlechterungInfoContainer> allEinkommensverschlechterungInfo = einkommensverschlechterungInfoService.getAllEinkommensverschlechterungInfo();
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfo = allEinkommensverschlechterungInfo.iterator().next();
		Assert.assertFalse(einkommensverschlechterungInfo.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus2());

		//Add EKV_BasisJahr2
		einkommensverschlechterungInfo.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		einkommensverschlechterungInfoService.updateEinkommensVerschlechterungInfoAndGesuch(einkommensverschlechterungInfo
			.getGesuch(), null, einkommensverschlechterungInfo);

		final Optional<EinkommensverschlechterungInfoContainer> ekvInfoUpdated = einkommensverschlechterungInfoService.findEinkommensverschlechterungInfo(einkommensverschlechterungInfo.getId());
		Assert.assertTrue(ekvInfoUpdated.isPresent());
		final EinkommensverschlechterungInfoContainer info1 = ekvInfoUpdated.get();
		Assert.assertNotNull(info1);
		Assert.assertTrue(info1.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus2());

		//Remove EKV_BasisJahr2
		info1.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(false);
		einkommensverschlechterungInfoService.updateEinkommensVerschlechterungInfoAndGesuch(info1.getGesuch(), null, info1);

		final Optional<EinkommensverschlechterungInfoContainer> ekvInfoUpdated2 = einkommensverschlechterungInfoService.findEinkommensverschlechterungInfo(info1.getId());
		Assert.assertTrue(ekvInfoUpdated2.isPresent());
		final EinkommensverschlechterungInfoContainer info2 = ekvInfoUpdated2.get();
		Assert.assertNotNull(info2);
		Assert.assertFalse(info2.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus2());
	}

	@Test
	public void removeEinkommensverschlechterungInfoTest() {
		Assert.assertNotNull(einkommensverschlechterungInfoService);
		Assert.assertEquals(0, einkommensverschlechterungInfoService.getAllEinkommensverschlechterungInfo().size());

		EinkommensverschlechterungInfoContainer info = persistAndGetEinkommensverschlechterungInfoOnGesuch();
		Assert.assertEquals(1, einkommensverschlechterungInfoService.getAllEinkommensverschlechterungInfo().size());

		einkommensverschlechterungInfoService.removeEinkommensverschlechterungInfo(info);
		Assert.assertEquals(0, einkommensverschlechterungInfoService.getAllEinkommensverschlechterungInfo().size());

	}

}

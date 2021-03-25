/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class FerienbetreuungServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode1920;
	private Gemeinde gemeindeParis;

	@Before
	public void setUp() {
		gesuchsperiode1920 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2018, 2019);
		TestDataUtil.prepareParameters(gesuchsperiode1920, persistence);
		insertInstitutionen();
		gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		TestDataUtil.getGemeindeLondon(persistence);
	}

	@Test
	public void createFerienbetreuungAntrag() {
		final FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
			ferienbetreuungService.createFerienbetreuungAntrag(gemeindeParis, gesuchsperiode1920);
		Assert.assertNotNull(ferienbetreuungAngabenContainer);
		Assert.assertNotNull(ferienbetreuungAngabenContainer.getAngabenDeklaration());
		Assert.assertNull(ferienbetreuungAngabenContainer.getAngabenKorrektur());
		Assert.assertEquals("Bern", ferienbetreuungAngabenContainer.getGemeinde().getName());
		Assert.assertEquals("IN_BEARBEITUNG_GEMEINDE", ferienbetreuungAngabenContainer.getStatusString());
		Assert.assertEquals(2017, ferienbetreuungAngabenContainer.getGesuchsperiode().getBasisJahr());
	}

	@Test
	public void saveKommentar() {
		final FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
			ferienbetreuungService.createFerienbetreuungAntrag(gemeindeParis, gesuchsperiode1920);

		ferienbetreuungService.saveKommentar(ferienbetreuungAngabenContainer.getId(), "abcd");
		Optional<FerienbetreuungAngabenContainer> persistedOpt =
			ferienbetreuungService.findFerienbetreuungAngabenContainer(ferienbetreuungAngabenContainer.getId());
		Assert.assertTrue(persistedOpt.isPresent());
		FerienbetreuungAngabenContainer persisted = persistedOpt.get();
		Assert.assertEquals(persisted.getInternerKommentar(), "abcd");
		// should not change status
		Assert.assertEquals("IN_BEARBEITUNG_GEMEINDE", persisted.getStatusString());
	}

}

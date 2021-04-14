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

import java.util.List;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.test.IntegrationTest;
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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class GemeindeAntragServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private GemeindeAntragService gemeindeAntragService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode2021;

	private Gemeinde gemeindeLondon;

	@Before
	public void init() {
		gesuchsperiode2021 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2020, 2021);
		TestDataUtil.prepareParameters(gesuchsperiode2021, persistence);
		insertInstitutionen();
		TestDataUtil.getGemeindeParis(persistence);
		gemeindeLondon = TestDataUtil.getGemeindeLondon(persistence);
	}

	@Test
	public void createAllGemeindeAntraege() {
		final List<? extends GemeindeAntrag> gemeindeAntragList = gemeindeAntragService.createAllGemeindeAntraege(
			gesuchsperiode2021,
			GemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN);
		Assert.assertNotNull(gemeindeAntragList);
		Assert.assertEquals("Wir erwarten einen Antrag für die GP 20/21", 1, gemeindeAntragList.size());
	}

	@Test
	public void createGemeindeAntrag() {
		final FerienbetreuungAngabenContainer ferienbetreuungAntrag = (FerienbetreuungAngabenContainer) gemeindeAntragService.createGemeindeAntrag(
			gemeindeLondon,
			gesuchsperiode2021,
			GemeindeAntragTyp.FERIENBETREUUNG);
		Assert.assertNotNull(ferienbetreuungAntrag);
		Assert.assertEquals("Ostermundigen", ferienbetreuungAntrag.getGemeinde().getName());
		Assert.assertEquals("IN_BEARBEITUNG_GEMEINDE", ferienbetreuungAntrag.getStatusString());
		Assert.assertEquals(2019, ferienbetreuungAntrag.getGesuchsperiode().getBasisJahr());
		Assert.assertNotNull(ferienbetreuungAntrag.getAngabenDeklaration());
	}
}

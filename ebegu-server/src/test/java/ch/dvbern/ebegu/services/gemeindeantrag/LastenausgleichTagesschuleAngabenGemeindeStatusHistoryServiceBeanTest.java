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

import java.util.Collection;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
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
public class LastenausgleichTagesschuleAngabenGemeindeStatusHistoryServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	private Gesuchsperiode gesuchsperiode1920;
	private Gemeinde gemeindeParis;

	@Before
	public void setUp() {
		gesuchsperiode1920 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2018, 2019);
		TestDataUtil.prepareParameters(gesuchsperiode1920, persistence);
		gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		TestDataUtil.getGemeindeLondon(persistence);
	}

	@Test
	public void saveLastenausgleichTagesschuleStatusChange() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode1920, gemeindeParis);
		latsGemeindeContainer = persistence.persist(latsGemeindeContainer);

		Collection<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> allHistories =
			criteriaQueryHelper.getAll(LastenausgleichTagesschuleAngabenGemeindeStatusHistory.class);

		Assert.assertEquals(
			"Die History-Tabelle sollte jetzt noch leer sein",
			0, allHistories.size());

		historyService.saveLastenausgleichTagesschuleStatusChange(latsGemeindeContainer);

		allHistories =
			criteriaQueryHelper.getAll(LastenausgleichTagesschuleAngabenGemeindeStatusHistory.class);

		Assert.assertEquals(
			"Jetzt sollte 1 Eintrag vorhanden sein",
			1, allHistories.size());
	}
}

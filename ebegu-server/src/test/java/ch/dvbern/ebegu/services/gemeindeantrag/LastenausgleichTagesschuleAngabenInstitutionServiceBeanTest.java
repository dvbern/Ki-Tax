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
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;
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
public class LastenausgleichTagesschuleAngabenInstitutionServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode1920;
	private Gemeinde gemeindeParis;
	private Institution tagesschuleParis;

	@Before
	public void setUp() {
		gesuchsperiode1920 = TestDataUtil.createAndPersistCustomGesuchsperiode(persistence, 2018, 2019);
		TestDataUtil.prepareParameters(gesuchsperiode1920, persistence);
		gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		TestDataUtil.getGemeindeLondon(persistence);

		InstitutionStammdaten instStammdaten =
			TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode1920);
		Assert.assertNotNull(instStammdaten.getInstitutionStammdatenTagesschule());
		instStammdaten.getInstitutionStammdatenTagesschule().setGemeinde(gemeindeParis);
		tagesschuleParis = instStammdaten.getInstitution();
		Assert.assertNotNull(gemeindeParis.getMandant());
		tagesschuleParis.setMandant(gemeindeParis.getMandant());
		tagesschuleParis.setTraegerschaft(null);
		tagesschuleParis = persistence.persist(tagesschuleParis);
		instStammdaten.setInstitution(tagesschuleParis);
		persistence.persist(instStammdaten);
	}

	@Test
	public void createLastenausgleichTagesschuleInstitution() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode1920, gemeindeParis);
		latsGemeindeContainer = persistence.persist(latsGemeindeContainer);

		angabenInstitutionService.createLastenausgleichTagesschuleInstitution(latsGemeindeContainer);
		Assert.assertNotNull(latsGemeindeContainer.getAngabenInstitutionContainers());
		Assert.assertEquals(
			"Es sollte jetzt genau einen InstitutionsContainer geben",
			1, latsGemeindeContainer.getAngabenInstitutionContainers().size());
	}

	@Test
	public void saveLastenausgleichTagesschuleInstitution() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode1920, gemeindeParis);
		latsGemeindeContainer = persistence.persist(latsGemeindeContainer);
		angabenInstitutionService.createLastenausgleichTagesschuleInstitution(latsGemeindeContainer);
		final Optional<LastenausgleichTagesschuleAngabenInstitutionContainer> optional =
			latsGemeindeContainer.getAngabenInstitutionContainers().stream().findFirst();
		Assert.assertTrue(optional.isPresent());
		LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitution = optional.get();
		Assert.assertNotNull(latsAngabenInstitution);
		Assert.assertEquals(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN, latsAngabenInstitution.getStatus());
		Assert.assertEquals(
			"Das Objekt wurde neu erstellt: Timestamp erstellt und mutiert muessen gleich sein",
			latsGemeindeContainer.getTimestampErstellt(), latsGemeindeContainer.getTimestampMutiert());

		// Eingaben der Tagesschule
		latsAngabenInstitution.setAngabenKorrektur(TestDataUtil.createLastenausgleichTagesschuleAngabenInstitution());
		latsAngabenInstitution = angabenInstitutionService.saveLastenausgleichTagesschuleInstitution(latsAngabenInstitution);

		Assert.assertNotEquals(
			"Nach dem zweiten Speichern sollen TimestampErstellt und TimestampMutiert nicht mehr gleich sein",
			latsAngabenInstitution.getTimestampErstellt(),
			latsAngabenInstitution.getTimestampMutiert());
	}

	@Test
	public void lastenausgleichTagesschuleInstitutionFreigeben() {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsGemeindeContainer =
			TestDataUtil.createLastenausgleichTagesschuleAngabenGemeindeContainer(gesuchsperiode1920, gemeindeParis);
		latsGemeindeContainer = persistence.persist(latsGemeindeContainer);
		angabenInstitutionService.createLastenausgleichTagesschuleInstitution(latsGemeindeContainer);
		LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitution =
			latsGemeindeContainer.getAngabenInstitutionContainers().stream().findFirst().get();

		// Eingaben der Tagesschule
		latsAngabenInstitution.setAngabenKorrektur(TestDataUtil.createLastenausgleichTagesschuleAngabenInstitution());
		latsAngabenInstitution = angabenInstitutionService.saveLastenausgleichTagesschuleInstitution(latsAngabenInstitution);

		Assert.assertEquals(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN, latsAngabenInstitution.getStatus());

		latsAngabenInstitution = angabenInstitutionService.lastenausgleichTagesschuleInstitutionFreigeben(latsAngabenInstitution);

		Assert.assertEquals(LastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE, latsAngabenInstitution.getStatus());
	}
}

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests fuer die Klasse KindService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class KindServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private KindService kindService;

	@Inject
	private Persistence persistence;

	@Inject
	private FallService fallService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private TestfaelleService testfaelleService;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	@Test
	public void createAndUpdatekindTest() {
		assertNotNull(kindService);
		Gesuch erstgesuch = createTestGesuch();
		final KindContainer persistedKind = erstgesuch.getKindContainers().iterator().next();

		Optional<KindContainer> optKind = kindService.findKind(persistedKind.getId());
		assertTrue(optKind.isPresent());
		KindContainer foundKind = optKind.get();

		assertNotNull(persistedKind.getKindJA());
		assertNotNull(foundKind.getKindJA());
		assertEquals(persistedKind.getKindJA().getNachname(), foundKind.getKindJA().getNachname());
		assertNotEquals("Neuer Name", foundKind.getKindJA().getNachname());

		foundKind.getKindJA().setNachname("Neuer Name");
		persistence.merge(foundKind);

		Optional<KindContainer> optUpdatedKind = kindService.findKind(foundKind.getId());
		assertTrue(optUpdatedKind.isPresent());
		KindContainer kindContainer = optUpdatedKind.get();
		assertNotNull(kindContainer.getKindJA());
		assertEquals("Neuer Name", kindContainer.getKindJA().getNachname());
		assertEquals(new Integer(3), kindContainer.getNextNumberBetreuung());
		assertEquals(new Integer(1), kindContainer.getKindNummer());

		Optional<Fall> fallOptional = fallService.findFall(erstgesuch.getFall().getId());
		assertTrue(fallOptional.isPresent());
		assertEquals(new Integer(2), fallOptional.get().getNextNumberKind());
	}

	@Test
	public void addKindInMutationTest() {
		Gesuch erstgesuch = createTestGesuch();
		erstgesuch.setGueltig(true);
		erstgesuch.setTimestampVerfuegt(LocalDateTime.now());
		erstgesuch = gesuchService.updateGesuch(erstgesuch, true, null);
		assertEquals(1, erstgesuch.getKindContainers().size());

		Gesuch mutation = testfaelleService.antragMutieren(erstgesuch, LocalDate.of(1980, Month.MARCH, 25));

		assertEquals(1, mutation.getKindContainers().size());

		WizardStep wizardStepFromGesuch = wizardStepService.findWizardStepFromGesuch(mutation.getId(), WizardStepName.KINDER);
		assertEquals(WizardStepStatus.OK, wizardStepFromGesuch.getWizardStepStatus());

		KindContainer neuesKindInMutation = TestDataUtil.createKindContainerWithoutFachstelle();

		neuesKindInMutation.setGesuch(mutation);
		kindService.saveKind(neuesKindInMutation);
		assertEquals(2, kindService.findAllKinderFromGesuch(mutation.getId()).size());

		wizardStepFromGesuch = wizardStepService.findWizardStepFromGesuch(mutation.getId(), WizardStepName.KINDER);
		assertEquals(WizardStepStatus.MUTIERT, wizardStepFromGesuch.getWizardStepStatus());
	}

	@Test
	public void findKinderFromGesuch() {
		assertNotNull(kindService);
		Gesuch gesuch = createTestGesuch();
		final KindContainer persitedKind1 = persistKind(gesuch);
		final KindContainer persitedKind2 = persistKind(gesuch);

		final Gesuch otherGesuch = createTestGesuch();
		persistKind(otherGesuch);

		final List<KindContainer> allKinderFromGesuch = kindService.findAllKinderFromGesuch(gesuch.getId());

		assertEquals(3, allKinderFromGesuch.size());
		assertTrue(allKinderFromGesuch.contains(persitedKind1));
		assertTrue(allKinderFromGesuch.contains(persitedKind2));

	}

	// HELP METHODS

	@Nonnull
	private KindContainer persistKind(Gesuch gesuch) {
		KindContainer kindContainer = TestDataUtil.createDefaultKindContainer();
		kindContainer.setGesuch(gesuch);
		assertNotNull(kindContainer.getKindGS());
		assertNotNull(kindContainer.getKindGS().getPensumFachstelle());
		assertNotNull(kindContainer.getKindJA().getPensumFachstelle());
		persistence.persist(kindContainer.getKindGS().getPensumFachstelle().getFachstelle());
		persistence.persist(kindContainer.getKindJA().getPensumFachstelle().getFachstelle());
		persistence.persist(kindContainer.getKindGS());
		persistence.persist(kindContainer.getKindJA());

		final KindContainer persistedKind = kindService.saveKind(kindContainer);
		return persistedKind;
	}

	@Nonnull
	private Gesuch createTestGesuch() {
		return TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			institutionService,
			persistence,
			LocalDate.of(1980, Month.MARCH, 25),
			AntragStatus.VERFUEGT,
			gesuchsperiode);
	}
}

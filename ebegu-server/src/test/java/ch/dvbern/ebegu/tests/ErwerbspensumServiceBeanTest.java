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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
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

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Test fuer Erwerbspensum Service
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class ErwerbspensumServiceBeanTest extends AbstractEbeguLoginTest {

	@Inject
	private ErwerbspensumService erwerbspensumService;

	@Inject
	private InstitutionService instService;

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
	}

	@Test
	public void createFinanzielleSituation() {
		Assert.assertNotNull(erwerbspensumService);

		final Gesuch gesuch = insertNewEntity();
		Assert.assertNotNull(gesuch.getGesuchsteller1());

		ErwerbspensumContainer ewpCont = TestDataUtil.createErwerbspensumContainer();
		ewpCont.setGesuchsteller(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getErwerbspensenContainers().add(ewpCont);

		erwerbspensumService.saveErwerbspensum(ewpCont, gesuch);
		Collection<ErwerbspensumContainer> allErwerbspensenenContainer =
			criteriaQueryHelper.getAll(ErwerbspensumContainer.class);
		Assert.assertEquals(3, allErwerbspensenenContainer.size());
		Optional<ErwerbspensumContainer> storedContainer = erwerbspensumService.findErwerbspensum(ewpCont.getId());
		Assert.assertTrue(storedContainer.isPresent());
		ErwerbspensumContainer erwerbspensumContainer = storedContainer.get();
		Assert.assertFalse(erwerbspensumContainer.isNew());
		Assert.assertTrue(allErwerbspensenenContainer.contains(erwerbspensumContainer));
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumJA());
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumJA().getTaetigkeit());
		Assert.assertNotNull(ewpCont.getErwerbspensumJA());
		Assert.assertEquals(ewpCont.getErwerbspensumJA().getTaetigkeit(), erwerbspensumContainer.getErwerbspensumJA().getTaetigkeit());
	}

	@Test
	public void updateFinanzielleSituationTest() {
		final Gesuch gesuch = insertNewEntity();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		ErwerbspensumContainer insertedEwpCont = gesuch.getGesuchsteller1().getErwerbspensenContainers().iterator().next();
		Optional<ErwerbspensumContainer> ewpContOpt = erwerbspensumService.findErwerbspensum(insertedEwpCont.getId());
		Assert.assertTrue(ewpContOpt.isPresent());
		ErwerbspensumContainer erwPenCont = ewpContOpt.get();
		Assert.assertNotNull(erwPenCont.getErwerbspensumJA());
		erwPenCont.getErwerbspensumJA().setGueltigkeit(new DateRange(LocalDate.now(), LocalDate.now().plusDays(80)));

		ErwerbspensumContainer updatedCont =
			erwerbspensumService.saveErwerbspensum(erwPenCont, gesuch);
		Assert.assertNotNull(updatedCont.getErwerbspensumJA());
		Assert.assertEquals(LocalDate.now(), updatedCont.getErwerbspensumJA().getGueltigkeit().getGueltigAb());
	}

	@Test
	public void removeFinanzielleSituationTest() {
		Assert.assertNotNull(erwerbspensumService);
		Assert.assertEquals(0, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());

		final Gesuch gesuch = insertNewEntity();
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		ErwerbspensumContainer insertedEwpCont = gesuch.getGesuchsteller1().getErwerbspensenContainers().iterator().next();
		Assert.assertEquals(2, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());

		erwerbspensumService.removeErwerbspensum(insertedEwpCont.getId(), gesuch);
		Assert.assertEquals(1, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());
	}

	@Test
	public void isErwerbspensumRequired_KITA_Required() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);

		Assert.assertTrue(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_KITA_TAGESELTERNKLEINKIND_Required() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();
		final Betreuung betreuung = kind.getBetreuungen().iterator().next();
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESFAMILIEN);
		persistence.merge(betreuung.getInstitutionStammdaten());

		Assert.assertTrue(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_TAGESSCHULE_NotRequired() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();
		kind.getBetreuungen().forEach(betreuung -> {
			betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
			persistence.merge(betreuung.getInstitutionStammdaten());
		});

		Assert.assertFalse(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_Fachstelle_NotRequired() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();
		final PensumFachstelle pensumFachstelle = TestDataUtil.createDefaultPensumFachstelle(kind.getKindJA());
		TestDataUtil.saveMandantIfNecessary(persistence, Objects.requireNonNull(pensumFachstelle.getFachstelle()).getMandant());
		kind.getKindJA().getPensumFachstelle().add(pensumFachstelle);
		pensumFachstelle.setKind(kind.getKindJA());
		persistence.persist(pensumFachstelle.getFachstelle());
		persistence.persist(pensumFachstelle);

		Assert.assertFalse(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_KitaUndSchulamtAngebot_Required() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();
		Assert.assertEquals(2, kind.getBetreuungen().size());

		Betreuung betreuungTagesschule = kind.getBetreuungen().iterator().next();
		betreuungTagesschule.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);

		Betreuung betreuungKita = kind.getBetreuungen().iterator().next();
		betreuungKita.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		Assert.assertTrue(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_KitaOhneFachstelle_Required() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();
		kind.getBetreuungen().forEach(betreuung -> {
			betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
			persistence.merge(betreuung.getInstitutionStammdaten());
		});

		Assert.assertTrue(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	@Test
	public void isErwerbspensumRequired_ErweiterteBeduerfnisse_Required() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			LocalDate.now(),
			null,
			gesuchsperiode);
		final KindContainer kind = gesuch.getKindContainers().iterator().next();

		kind.getBetreuungen().forEach(betreuung -> {
			if (betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() == null) {
				betreuung.getErweiterteBetreuungContainer().setErweiterteBetreuungJA(new ErweiterteBetreuung());
			}
			betreuung.getErweiterteBetreuungContainer()
				.getErweiterteBetreuungJA()
				.setErweiterteBeduerfnisse(true);
			persistence.merge(betreuung);
		});

		Assert.assertTrue(erwerbspensumService.isErwerbspensumRequired(gesuch));
	}

	private Gesuch insertNewEntity() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence, LocalDate.now(), AntragStatus.IN_BEARBEITUNG_JA, gesuchsperiode);
		GesuchstellerContainer gesuchsteller = gesuch.getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller);
		ErwerbspensumContainer container = TestDataUtil.createErwerbspensumContainer();
		gesuchsteller.addErwerbspensumContainer(container);
		persistence.merge(gesuchsteller);
		return gesuch;
	}
}

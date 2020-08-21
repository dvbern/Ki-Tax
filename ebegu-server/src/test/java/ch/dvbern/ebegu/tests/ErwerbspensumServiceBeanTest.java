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
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.InstitutionService;
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
import org.junit.runner.RunWith;

/**
 * Test fuer Erwerbspensum Service
 */
@RunWith(Arquillian.class)
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

		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		Erwerbspensum erwerbspensumData = TestDataUtil.createErwerbspensumData();
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuchsteller = persistence.persist(gesuchsteller);

		ErwerbspensumContainer ewpCont = TestDataUtil.createErwerbspensumContainer();
		ewpCont.setErwerbspensumGS(erwerbspensumData);
		ewpCont.setGesuchsteller(gesuchsteller);

		erwerbspensumService.saveErwerbspensum(ewpCont, TestDataUtil.createDefaultGesuch());
		Collection<ErwerbspensumContainer> allErwerbspensenenContainer =
			criteriaQueryHelper.getAll(ErwerbspensumContainer.class);
		Assert.assertEquals(1, allErwerbspensenenContainer.size());
		Optional<ErwerbspensumContainer> storedContainer = erwerbspensumService.findErwerbspensum(ewpCont.getId());
		Assert.assertTrue(storedContainer.isPresent());
		ErwerbspensumContainer erwerbspensumContainer = storedContainer.get();
		Assert.assertFalse(erwerbspensumContainer.isNew());
		Assert.assertEquals(erwerbspensumContainer, allErwerbspensenenContainer.iterator().next());
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumGS());
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumGS().getTaetigkeit());
		Assert.assertEquals(
			erwerbspensumData.getTaetigkeit(),
			erwerbspensumContainer.getErwerbspensumGS().getTaetigkeit());
	}

	@Test
	public void updateFinanzielleSituationTest() {
		ErwerbspensumContainer insertedEwpCont = insertNewEntity();
		Optional<ErwerbspensumContainer> ewpContOpt = erwerbspensumService.findErwerbspensum(insertedEwpCont.getId());
		Assert.assertTrue(ewpContOpt.isPresent());
		ErwerbspensumContainer erwPenCont = ewpContOpt.get();
		Erwerbspensum changedData = TestDataUtil.createErwerbspensumData();
		changedData.setGueltigkeit(new DateRange(LocalDate.now(), LocalDate.now().plusDays(80)));
		erwPenCont.setErwerbspensumGS(changedData);

		ErwerbspensumContainer updatedCont =
			erwerbspensumService.saveErwerbspensum(erwPenCont, TestDataUtil.createDefaultGesuch());
		Assert.assertNotNull(updatedCont.getErwerbspensumGS());
		Assert.assertEquals(LocalDate.now(), updatedCont.getErwerbspensumGS().getGueltigkeit().getGueltigAb());
	}

	@Test
	public void removeFinanzielleSituationTest() {
		Assert.assertNotNull(erwerbspensumService);
		Assert.assertEquals(0, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());

		ErwerbspensumContainer insertedEwpCont = insertNewEntity();
		Assert.assertEquals(1, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());

		erwerbspensumService.removeErwerbspensum(insertedEwpCont.getId(), TestDataUtil.createDefaultGesuch());
		Assert.assertEquals(0, criteriaQueryHelper.getAll(ErwerbspensumContainer.class).size());
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
		final PensumFachstelle pensumFachstelle = TestDataUtil.createDefaultPensumFachstelle();
		kind.getKindJA().setPensumFachstelle(pensumFachstelle);
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

	private ErwerbspensumContainer insertNewEntity() {
		GesuchstellerContainer gesuchsteller = TestDataUtil.createDefaultGesuchstellerContainer();
		ErwerbspensumContainer container = TestDataUtil.createErwerbspensumContainer();
		gesuchsteller.addErwerbspensumContainer(container);
		gesuchsteller = persistence.persist(gesuchsteller);
		return gesuchsteller.getErwerbspensenContainers().iterator().next();
	}
}

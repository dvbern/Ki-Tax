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

package ch.dvbern.ebegu.rest.test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.api.resource.BetreuungResource;
import ch.dvbern.ebegu.api.resource.DossierResource;
import ch.dvbern.ebegu.api.resource.FachstelleResource;
import ch.dvbern.ebegu.api.resource.FallResource;
import ch.dvbern.ebegu.api.resource.GesuchResource;
import ch.dvbern.ebegu.api.resource.KindResource;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.PensumFachstelleService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testet BetreuungResource
 */
@SuppressWarnings("JUnit3StyleTestMethodInJUnit4Class")
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
//@ServerSetup(InstallPicketLinkFileBasedSetupTask.class)
public class BetreuungResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private BetreuungService betreuungService;
	@Inject
	private BetreuungResource betreuungResource;
	@Inject
	private KindResource kindResource;
	@Inject
	private GesuchResource gesuchResource;
	@Inject
	private FallResource fallResource;
	@Inject
	private DossierResource dossierResource;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private FachstelleResource fachstelleResource;
	@Inject
	private PensumFachstelleService pensumFachstelleService;
	@Inject
	private JaxBConverter converter;
	@Inject
	private Persistence persistence;

	@Test
	public void createBetreuung() throws EbeguException {
		KindContainer returnedKind = persistKindAndDependingObjects();
		Betreuung testBetreuung = TestDataUtil.createDefaultBetreuung();
		persistStammdaten(testBetreuung.getInstitutionStammdaten());
		JaxBetreuung testJaxBetreuung = converter.betreuungToJAX(testBetreuung);

		JaxBetreuung jaxBetreuung = betreuungResource.saveBetreuung(converter.toJaxId(returnedKind), testJaxBetreuung, false, DUMMY_URIINFO, DUMMY_RESPONSE);
		Assert.assertEquals(Integer.valueOf(1), jaxBetreuung.getBetreuungNummer());
		Assert.assertNotNull(jaxBetreuung);
	}

	private void persistStammdaten(InstitutionStammdaten institutionStammdaten) {
		persistence.persist(institutionStammdaten.getInstitution().getMandant());
		persistence.persist(institutionStammdaten.getInstitution().getTraegerschaft());
		persistence.persist(institutionStammdaten.getInstitution());
		persistence.persist(institutionStammdaten);
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void updateBetreuungTest() throws EbeguException {
		Betreuung initialBetr = this.storeInitialBetreung();
		//im moment haben wir kein find fuer einen einzelnen Container
		Set<JaxBetreuung> betreuungenBeforeUpdate = kindResource.findKind(converter.toJaxId(initialBetr.getKind())).getBetreuungen();
		Assert.assertEquals(1, betreuungenBeforeUpdate.size());
		JaxBetreuung betreuung = betreuungenBeforeUpdate.iterator().next();
		Assert.assertEquals(0, betreuung.getBetreuungspensumContainers().size());

		JaxBetreuungspensumContainer containerToAdd = TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().getYear());

		betreuung.getBetreuungspensumContainers().add(containerToAdd);
		JaxBetreuung updatedBetr = betreuungResource.saveBetreuung(converter.toJaxId(initialBetr.getKind()), betreuung, false, DUMMY_URIINFO, DUMMY_RESPONSE);
		Assert.assertEquals(1, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);
	}

	/**
	 * Testet, dass das entfernen eines Betreuungspensums auf dem Client dieses aus der Liste auf dem Server loescht.
	 */
	@Test
	public void updateShouldRemoveBetreuungspensumContainerTest() throws EbeguException {
		Betreuung initialBetr = this.storeInitialBetreung();
		//im moment haben wir kein find fuer einen einzelnen Container
		JaxKindContainer jaxKindContainer = kindResource.findKind(converter.toJaxId(initialBetr.getKind()));
		Assert.assertNotNull(jaxKindContainer);
		Set<JaxBetreuung> betreuungenBeforeUpdate = jaxKindContainer.getBetreuungen();
		Assert.assertEquals(1, betreuungenBeforeUpdate.size());
		JaxBetreuung betreuung = betreuungenBeforeUpdate.iterator().next();

		betreuung.getBetreuungspensumContainers().add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().minusYears(1).getYear()));
		betreuung.getBetreuungspensumContainers().add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().getYear()));
		betreuung.getBetreuungspensumContainers().add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().plusYears(1).getYear()));
		JaxBetreuung updatedBetr = betreuungResource.saveBetreuung(converter.toJaxId(initialBetr.getKind()), betreuung, false, DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(updatedBetr.getBetreuungspensumContainers());
		Assert.assertEquals(3, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);

		updatedBetr.getBetreuungspensumContainers().clear(); //alle bestehenden entfernen
		updatedBetr.getBetreuungspensumContainers().add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().plusYears(2).getYear())); //einen neuen einfuegen

		updatedBetr = betreuungResource.saveBetreuung(converter.toJaxId(initialBetr.getKind()), updatedBetr, false, DUMMY_URIINFO, DUMMY_RESPONSE);
		Assert.assertEquals(1, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);

	}

	// HELP

	private KindContainer persistKindAndDependingObjects() {
		JaxGesuch jaxGesuch = TestJaxDataUtil.createTestJaxGesuch();
		Mandant persistedMandant = persistence.persist(converter.mandantToEntity(TestJaxDataUtil.createTestMandant(), new Mandant()));
		jaxGesuch.getDossier().getVerantwortlicherBG().setMandant(converter.mandantToJAX(persistedMandant));
		benutzerService.saveBenutzer(converter.authLoginElementToBenutzer(jaxGesuch.getDossier().getVerantwortlicherBG(), new Benutzer()));
		JaxFall returnedFall = fallResource.saveFall(jaxGesuch.getDossier().getFall(), DUMMY_URIINFO, DUMMY_RESPONSE);
		jaxGesuch.getDossier().setFall(returnedFall);
		JaxDossier returnedDossier = (JaxDossier) dossierResource.create(jaxGesuch.getDossier(), DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();
		jaxGesuch.setGesuchsperiode(saveGesuchsperiodeInStatusAktiv(jaxGesuch.getGesuchsperiode()));
		jaxGesuch.setDossier(returnedDossier);
		JaxGesuch returnedGesuch = (JaxGesuch) gesuchResource.create(jaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();

		KindContainer returnedKind = TestDataUtil.createDefaultKindContainer();
		returnedKind.setKindNummer(1);
		returnedKind.setGesuch(converter.gesuchToEntity(returnedGesuch, new Gesuch()));
		JaxKindContainer jaxKind = converter.kindContainerToJAX(returnedKind);
		JaxPensumFachstelle jaxPensumFachstelle = jaxKind.getKindGS().getPensumFachstelle();
		Assert.assertNotNull(jaxPensumFachstelle);
		jaxPensumFachstelle.setFachstelle(fachstelleResource.saveFachstelle(jaxPensumFachstelle.getFachstelle(), DUMMY_URIINFO, DUMMY_RESPONSE));
		PensumFachstelle returnedPensumFachstelle = pensumFachstelleService.savePensumFachstelle(
			converter.pensumFachstelleToEntity(jaxPensumFachstelle, new PensumFachstelle()));
		JaxPensumFachstelle convertedPensumFachstelle = converter.pensumFachstelleToJax(returnedPensumFachstelle);
		jaxKind.getKindGS().setPensumFachstelle(convertedPensumFachstelle);
		jaxKind.getKindJA().setPensumFachstelle(convertedPensumFachstelle);
		kindResource.saveKind(converter.toJaxId(returnedGesuch), jaxKind, DUMMY_URIINFO, DUMMY_RESPONSE);
		return returnedKind;
	}

	private Betreuung storeInitialBetreung() throws EbeguException {
		KindContainer returnedKind = persistKindAndDependingObjects();
		Betreuung testBetreuung = TestDataUtil.createDefaultBetreuung();
		persistStammdaten(testBetreuung.getInstitutionStammdaten());
		testBetreuung.setKind(returnedKind);
		Betreuung betreuung = betreuungService.saveBetreuung(testBetreuung, false);
		checkNextNumberBetreuung(converter.toJaxId(betreuung.getKind()), 2);
		return betreuung;
	}

	@SuppressWarnings("SameParameterValue")
	private void checkNextNumberBetreuung(JaxId kindId, Integer number) {
		final JaxKindContainer updatedKind = kindResource.findKind(kindId);
		Assert.assertNotNull(updatedKind);
		Assert.assertEquals(number, updatedKind.getNextNumberBetreuung());
	}

	@Test
	public void testNewBetreuungIsDuplicate() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung= converter.betreuungToJAX(newBetreuung);
		Assert.assertTrue(betreuungResource.hasDuplicate(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsNotDuplicate() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();

		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung= converter.betreuungToJAX(newBetreuung);
		Assert.assertFalse(betreuungResource.hasDuplicate(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsDuplicateFerieninsel() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung= converter.betreuungToJAX(newBetreuung);
		Assert.assertTrue(betreuungResource.hasDuplicate(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsNotDuplicateFerieninsel() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung = converter.betreuungToJAX(newBetreuung);
		Assert.assertNotNull(jaxNewBetreuung.getBelegungFerieninsel());
		jaxNewBetreuung.getBelegungFerieninsel().setFerienname(Ferienname.SOMMERFERIEN);

		Assert.assertFalse(betreuungResource.hasDuplicate(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsDuplicateFerieninselButStorniert() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung= converter.betreuungToJAX(newBetreuung);
		existingBetreuung1.setBetreuungsstatus(Betreuungsstatus.STORNIERT);

		Assert.assertFalse(betreuungResource.hasDuplicate(jaxNewBetreuung, betreuungen));
	}
}

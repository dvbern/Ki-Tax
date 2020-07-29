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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDateRangedDTO;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheit;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheitContainer;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.api.resource.BetreuungResource;
import ch.dvbern.ebegu.api.resource.DossierResource;
import ch.dvbern.ebegu.api.resource.FachstelleResource;
import ch.dvbern.ebegu.api.resource.FallResource;
import ch.dvbern.ebegu.api.resource.GesuchResource;
import ch.dvbern.ebegu.api.resource.GesuchstellerResource;
import ch.dvbern.ebegu.api.resource.KindResource;
import ch.dvbern.ebegu.api.resource.util.BetreuungUtil;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.test.TestDataUtil;
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
	private JaxBConverter converter;
	@Inject
	private Persistence persistence;
	@Inject
	private GesuchstellerResource gesuchstellerResource;
	@Inject
	private GesuchService gesuchService;

	@Test
	public void createBetreuung() {
		KindContainer returnedKind = persistKindAndDependingObjects();
		Betreuung testBetreuung = TestDataUtil.createDefaultBetreuung();
		persistStammdaten(testBetreuung.getInstitutionStammdaten());
		testBetreuung.setKind(returnedKind);
		JaxBetreuung testJaxBetreuung = converter.betreuungToJAX(testBetreuung);
		Assert.assertNotNull(testJaxBetreuung.getKindId());

		JaxBetreuung jaxBetreuung = betreuungResource.saveBetreuung(testJaxBetreuung,
			false,
			DUMMY_URIINFO,
			DUMMY_RESPONSE);
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
	public void updateBetreuungTest() {
		Betreuung initialBetr = this.storeInitialBetreung();

		//im moment haben wir kein find fuer einen einzelnen Container
		Set<JaxBetreuung> betreuungenBeforeUpdate =
			kindResource.findKind(converter.toJaxId(initialBetr.getKind())).getBetreuungen();
		Assert.assertEquals(1, betreuungenBeforeUpdate.size());
		JaxBetreuung betreuung = betreuungenBeforeUpdate.iterator().next();
		Assert.assertEquals(0, betreuung.getBetreuungspensumContainers().size());

		JaxBetreuungspensumContainer containerToAdd =
			TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().getYear());

		betreuung.getBetreuungspensumContainers().add(containerToAdd);
		JaxBetreuung updatedBetr = betreuungResource.saveBetreuung(betreuung,
			false,
			DUMMY_URIINFO,
			DUMMY_RESPONSE);
		Assert.assertEquals(1, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);
	}

	/**
	 * Testet, dass das entfernen eines Betreuungspensums auf dem Client dieses aus der Liste auf dem Server loescht.
	 */
	@Test
	public void updateShouldRemoveBetreuungspensumContainerTest() {
		Betreuung initialBetr = this.storeInitialBetreung();

		//im moment haben wir kein find fuer einen einzelnen Container
		JaxKindContainer jaxKindContainer = kindResource.findKind(converter.toJaxId(initialBetr.getKind()));
		Assert.assertNotNull(jaxKindContainer);
		Set<JaxBetreuung> betreuungenBeforeUpdate = jaxKindContainer.getBetreuungen();
		Assert.assertEquals(1, betreuungenBeforeUpdate.size());
		JaxBetreuung betreuung = betreuungenBeforeUpdate.iterator().next();

		betreuung.getBetreuungspensumContainers()
			.add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().minusYears(1).getYear()));
		betreuung.getBetreuungspensumContainers()
			.add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().getYear()));
		betreuung.getBetreuungspensumContainers()
			.add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now().plusYears(1).getYear()));
		JaxBetreuung updatedBetr = betreuungResource.saveBetreuung(
			betreuung,
			false,
			DUMMY_URIINFO,
			DUMMY_RESPONSE);

		Assert.assertNotNull(updatedBetr.getBetreuungspensumContainers());
		Assert.assertEquals(3, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);

		updatedBetr.getBetreuungspensumContainers().clear(); //alle bestehenden entfernen
		updatedBetr.getBetreuungspensumContainers()
			.add(TestJaxDataUtil.createBetreuungspensumContainer(LocalDate.now()
				.plusYears(2)
				.getYear())); //einen neuen einfuegen

		updatedBetr = betreuungResource.saveBetreuung(
			updatedBetr,
			false,
			DUMMY_URIINFO,
			DUMMY_RESPONSE);
		Assert.assertEquals(1, updatedBetr.getBetreuungspensumContainers().size());
		Assert.assertEquals(Integer.valueOf(1), updatedBetr.getBetreuungNummer());
		checkNextNumberBetreuung(converter.toJaxId(initialBetr.getKind()), 2);

	}

	// HELP
	private KindContainer persistKindAndDependingObjects() {
		final Gesuchsperiode gesuchsperiode1718 = persistence.merge(TestDataUtil.createGesuchsperiode1718());
		final Gemeinde gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		JaxGesuch jaxGesuch = TestJaxDataUtil.createTestJaxGesuch(
			converter.gesuchsperiodeToJAX(gesuchsperiode1718),
			converter.gemeindeToJAX(gemeindeParis)
		);
		TestDataUtil.prepareParameters(gesuchsperiode1718, persistence);

		JaxGemeinde persistedGemeinde = converter.gemeindeToJAX(TestDataUtil.getGemeindeParis(persistence));
		Mandant persistedMandant =
			persistence.persist(converter.mandantToEntity(TestJaxDataUtil.createTestMandant(), new Mandant()));
		jaxGesuch.getDossier().getVerantwortlicherBG().setMandant(converter.mandantToJAX(persistedMandant));
		jaxGesuch.getDossier()
			.getVerantwortlicherBG()
			.getBerechtigungen()
			.iterator()
			.next()
			.getGemeindeList()
			.add(persistedGemeinde);
		benutzerService.saveBenutzer(converter.jaxBenutzerToBenutzer(
			jaxGesuch.getDossier().getVerantwortlicherBG(),
			new Benutzer()));
		JaxFall returnedFall = fallResource.saveFall(jaxGesuch.getDossier().getFall(), DUMMY_URIINFO, DUMMY_RESPONSE);
		jaxGesuch.getDossier().setFall(returnedFall);
		jaxGesuch.getDossier().setGemeinde(persistedGemeinde);
		JaxDossier returnedDossier =
			(JaxDossier) dossierResource.create(jaxGesuch.getDossier(), DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();
		jaxGesuch.setGesuchsperiode(saveGesuchsperiodeInStatusAktiv(jaxGesuch.getGesuchsperiode()));
		jaxGesuch.setDossier(returnedDossier);
		JaxGesuch returnedGesuch =
			(JaxGesuch) gesuchResource.create(jaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();

		returnedGesuch.setGesuchsteller1(gesuchstellerResource.saveGesuchsteller(converter.toJaxId(returnedGesuch), 1, false,
			TestJaxDataUtil.createTestJaxGesuchsteller(),
			DUMMY_URIINFO, DUMMY_RESPONSE));

		returnedGesuch = gesuchResource.update(returnedGesuch, DUMMY_URIINFO, DUMMY_RESPONSE);


		KindContainer returnedKind = TestDataUtil.createDefaultKindContainer();
		returnedKind.setKindNummer(1);
		JaxKindContainer jaxKind = converter.kindContainerToJAX(returnedKind);
		JaxPensumFachstelle jaxPensumFachstelle = jaxKind.getKindGS().getPensumFachstelle();
		Assert.assertNotNull(jaxPensumFachstelle);
		persistence.persist(converter.fachstelleToEntity(jaxPensumFachstelle.getFachstelle(), new Fachstelle()));
		jaxPensumFachstelle.setFachstelle(jaxPensumFachstelle.getFachstelle());
		PensumFachstelle returnedPensumFachstelle = persistence.merge(
			converter.pensumFachstelleToEntity(jaxPensumFachstelle, new PensumFachstelle()));
		JaxPensumFachstelle convertedPensumFachstelle = converter.pensumFachstelleToJax(returnedPensumFachstelle);
		jaxKind.getKindGS().setPensumFachstelle(convertedPensumFachstelle);
		jaxKind.getKindJA().setPensumFachstelle(convertedPensumFachstelle);

		assert returnedGesuch != null;
		kindResource.saveKind(converter.toJaxId(returnedGesuch), jaxKind, DUMMY_URIINFO, DUMMY_RESPONSE);
		assert returnedGesuch.getId() != null;
		returnedKind.setGesuch(gesuchService.findGesuch(returnedGesuch.getId()).get());
		return returnedKind;
	}

	private Betreuung storeInitialBetreung() {
		KindContainer returnedKind = persistKindAndDependingObjects();
		Betreuung testBetreuung = TestDataUtil.createDefaultBetreuung();
		persistStammdaten(testBetreuung.getInstitutionStammdaten());
		testBetreuung.setKind(returnedKind);
		Betreuung betreuung = betreuungService.saveBetreuung(testBetreuung, false);
		checkNextNumberBetreuung(converter.toJaxId(betreuung.getKind()), 2);
		return betreuung;
	}

	@SuppressWarnings({ "SameParameterValue", "NonBooleanMethodNameMayNotStartWithQuestion" })
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

		JaxBetreuung jaxNewBetreuung = converter.betreuungToJAX(newBetreuung);
		Assert.assertTrue(BetreuungUtil.hasDuplicateBetreuung(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsNotDuplicate() {

		Betreuung existingBetreuung1 = TestDataUtil.createDefaultBetreuung();
		Betreuung existingBetreuung2 = TestDataUtil.createDefaultBetreuung();
		Betreuung newBetreuung = TestDataUtil.createDefaultBetreuung();

		Set<Betreuung> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung = converter.betreuungToJAX(newBetreuung);
		Assert.assertFalse(BetreuungUtil.hasDuplicateBetreuung(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsDuplicateFerieninsel() {

		AnmeldungFerieninsel existingBetreuung1 = TestDataUtil.createDefaultAnmeldungFerieninsel();
		AnmeldungFerieninsel existingBetreuung2 = TestDataUtil.createDefaultAnmeldungFerieninsel();
		AnmeldungFerieninsel newBetreuung = TestDataUtil.createDefaultAnmeldungFerieninsel();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<AnmeldungFerieninsel> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung = converter.anmeldungFerieninselToJAX(newBetreuung);
		Assert.assertTrue(BetreuungUtil.hasDuplicateAnmeldungFerieninsel(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsNotDuplicateFerieninsel() {

		AnmeldungFerieninsel existingBetreuung1 = TestDataUtil.createDefaultAnmeldungFerieninsel();
		AnmeldungFerieninsel newBetreuung = TestDataUtil.createDefaultAnmeldungFerieninsel();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<AnmeldungFerieninsel> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);

		JaxBetreuung jaxNewBetreuung = converter.anmeldungFerieninselToJAX(newBetreuung);
		Assert.assertNotNull(jaxNewBetreuung.getBelegungFerieninsel());
		jaxNewBetreuung.getBelegungFerieninsel().setFerienname(Ferienname.SOMMERFERIEN);

		Assert.assertFalse(BetreuungUtil.hasDuplicateAnmeldungFerieninsel(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testNewBetreuungIsDuplicateFerieninselButStorniert() {

		AnmeldungFerieninsel existingBetreuung1 = TestDataUtil.createDefaultAnmeldungFerieninsel();
		AnmeldungFerieninsel existingBetreuung2 = TestDataUtil.createDefaultAnmeldungFerieninsel();
		AnmeldungFerieninsel newBetreuung = TestDataUtil.createDefaultAnmeldungFerieninsel();
		final InstitutionStammdaten institutionStammdatenFI = existingBetreuung1.getInstitutionStammdaten();
		institutionStammdatenFI.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.FRUEHLINGSFERIEN);
		existingBetreuung1.setBelegungFerieninsel(belegungFerieninsel);

		newBetreuung.setInstitutionStammdaten(existingBetreuung1.getInstitutionStammdaten());
		newBetreuung.setBelegungFerieninsel(belegungFerieninsel);
		Set<AnmeldungFerieninsel> betreuungen = new HashSet<>();
		betreuungen.add(existingBetreuung1);
		betreuungen.add(existingBetreuung2);

		JaxBetreuung jaxNewBetreuung = converter.anmeldungFerieninselToJAX(newBetreuung);
		existingBetreuung1.setBetreuungsstatus(Betreuungsstatus.STORNIERT);

		Assert.assertFalse(BetreuungUtil.hasDuplicateAnmeldungFerieninsel(jaxNewBetreuung, betreuungen));
	}

	@Test
	public void testStoreAbwesenheit() {

		Betreuung initialBetr = this.storeInitialBetreung();

		final Set<AbwesenheitContainer> abwenseheitContList = new HashSet<>();
		final AbwesenheitContainer abwesenheit1 =
			TestDataUtil.createShortAbwesenheitContainer(initialBetr.extractGesuchsperiode());
		abwenseheitContList.add(abwesenheit1);

		//neue lange Abwesenheit die 3 Monate spaeter stattfindet
		final AbwesenheitContainer lateAbwesenheit =
			TestDataUtil.createLongAbwesenheitContainer(initialBetr.extractGesuchsperiode());
		lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigAb(
			lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigAb().plusMonths(3));
		lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().setGueltigBis(
			lateAbwesenheit.getAbwesenheitJA().getGueltigkeit().getGueltigBis().plusMonths(3));
		abwenseheitContList.add(lateAbwesenheit);
		initialBetr.setAbwesenheitContainers(abwenseheitContList);
		JaxBetreuung jaxNewBetreuung = converter.betreuungToJAX(initialBetr);
		initialBetr.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		List<JaxBetreuung> abwesenheitsbetr = new ArrayList<>();
		abwesenheitsbetr.add(jaxNewBetreuung);
		final List<JaxBetreuung> storedBetr = betreuungResource.saveAbwesenheiten(abwesenheitsbetr, Boolean.TRUE,
			DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertEquals(1, storedBetr.size());
		final JaxBetreuung jaxBetreuung = storedBetr.get(0);
		Assert.assertEquals(2, jaxBetreuung.getAbwesenheitContainers().size());
		Comparator<JaxAbwesenheit> comp = Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb);
		Optional<JaxAbwesenheit> firstAbwOpt =
			jaxBetreuung.getAbwesenheitContainers().stream()
				.map(JaxAbwesenheitContainer::getAbwesenheitJA).min(comp);

		Assert.assertTrue(firstAbwOpt.isPresent());
		final JaxAbwesenheit firstAbw = firstAbwOpt.get();

		Assert.assertNotNull(firstAbw);
		Assert.assertEquals(
			abwesenheit1.getAbwesenheitJA().getGueltigkeit().getGueltigAb(),
			firstAbw.getGueltigAb()
		);

		Assert.assertEquals(
			abwesenheit1.getAbwesenheitJA().getGueltigkeit().getGueltigBis(),
			firstAbw.getGueltigBis()
		);

	}

}

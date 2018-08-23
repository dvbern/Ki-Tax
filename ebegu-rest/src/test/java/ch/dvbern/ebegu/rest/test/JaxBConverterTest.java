/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import java.time.LocalDateTime;

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.resource.BetreuungResource;
import ch.dvbern.ebegu.api.resource.GesuchResource;
import ch.dvbern.ebegu.api.resource.InstitutionResource;
import ch.dvbern.ebegu.api.resource.InstitutionStammdatenResource;
import ch.dvbern.ebegu.api.resource.KindResource;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.services.TestdataCreationService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.TestfallName;
import ch.dvbern.ebegu.util.testdata.ErstgesuchConfig;
import ch.dvbern.ebegu.util.testdata.TestdataSetupConfig;
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
 * Tests fuer den JaxBConverter. Insbesondere wird geprüft, dass beim Speichern von Gesuchsdaten keine Stammdaten verändert werden dürfen.
 * Gesuchsperiode
 * - beim Speichern von Gesuch => gesuchSpeichernDarfGesuchsperiodeNichtUpdaten
 * Mandant
 * - beim Speichern von Institution => institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten
 * Trägerschaft
 * - beim Speichern von Institution => institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten
 * Institution
 *  - beim Speichern von InstitutionsStammdaten => institutionsStammdatenSpeichernDarfInstitutionNichtUpdaten
 * InstitutionsStammdaten
 * - beim Speichern von Betreuung => betreuungSpeichernDarfInstitutionsStammdatenNichtUpdaten
 * Fachstelle
 * - beim Speichern von PensumFachstelle => pensumFachstelleSpeichernDarfFachstelleNichtUpdaten
 */
@SuppressWarnings({ "LocalVariableNamingConvention", "InstanceMethodNamingConvention", "InstanceVariableNamingConvention" })
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
public class JaxBConverterTest extends AbstractEbeguRestLoginTest {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private TestdataCreationService testdataCreationService;

	@Inject
	private GesuchResource gesuchResource;

	@Inject
	private InstitutionResource institutionResource;

	@Inject
	private InstitutionStammdatenResource institutionStammdatenResource;

	@Inject
	private BetreuungResource betreuungResource;

	@Inject
	private KindResource kindResource;

	@Inject
	private Persistence persistence;

	private final JaxBConverter converter = new JaxBConverter();

	@Before
	public void init() {
		// Tests initialisieren
		Gesuchsperiode gesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();
		final InstitutionStammdaten kitaAaregg = TestDataUtil.createInstitutionStammdatenKitaWeissenstein();
		final InstitutionStammdaten kitaBruennen = TestDataUtil.createInstitutionStammdatenKitaBruennen();
		final InstitutionStammdaten tagiAaregg = TestDataUtil.createInstitutionStammdatenTagiWeissenstein();
		Mandant mandant = TestDataUtil.createDefaultMandant();
		TestdataSetupConfig setupConfig = new TestdataSetupConfig(mandant, kitaBruennen, kitaAaregg, tagiAaregg, gesuchsperiode);
		testdataCreationService.setupTestdata(setupConfig);
	}

	@Transactional(TransactionMode.DEFAULT)
	@Test
	public void gesuchSpeichernDarfGesuchsperiodeNichtUpdaten() {
		Gesuchsperiode gesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();
		Assert.assertEquals(GesuchsperiodeStatus.AKTIV, gesuchsperiode.getStatus());

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());
		testdataCreationService.insertParametersForTestfaelle(config);

		Gesuch gesuch = testdataCreationService.createErstgesuch(config);
		JaxGesuch jaxGesuch = TestJaxDataUtil.createTestJaxGesuch();
		jaxGesuch.setDossier(converter.dossierToJAX(gesuch.getDossier()));
		jaxGesuch.setGesuchsperiode(converter.gesuchsperiodeToJAX(gesuchsperiode));
		jaxGesuch.getGesuchsperiode().setStatus(GesuchsperiodeStatus.INAKTIV);
		gesuchResource.create(jaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE);

		Gesuchsperiode loadedGesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();
		Assert.assertEquals(GesuchsperiodeStatus.AKTIV, loadedGesuchsperiode.getStatus());
	}

	@Test
	public void institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten() {
		Mandant mandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();
		traegerschaft = persistence.persist(traegerschaft);
		Assert.assertEquals("TestMandantDBUnit", mandant.getName());
		Assert.assertEquals("Traegerschaft1", traegerschaft.getName());

		Institution institution = TestDataUtil.createDefaultInstitution();
		institution.setTraegerschaft(traegerschaft);
		institution.setMandant(mandant);
		JaxInstitution jaxInstitution = converter.institutionToJAX(institution);
		jaxInstitution.getTraegerschaft().setName("ChangedTraegerschaft");
		jaxInstitution.getMandant().setName("ChangedMandant");
		institutionResource.createInstitution(jaxInstitution, DUMMY_URIINFO, DUMMY_RESPONSE);

		Mandant loadedMandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		Traegerschaft loadedTraegerschaft = criteriaQueryHelper.getAll(Traegerschaft.class).iterator().next();
		Assert.assertEquals("TestMandantDBUnit", loadedMandant.getName());
		Assert.assertEquals("Traegerschaft1", loadedTraegerschaft.getName());
	}

	@Test
	public void institutionsStammdatenSpeichernDarfInstitutionNichtUpdaten() {
		Mandant mandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		final Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();
		persistence.persist(gesuchsperiode1718);
		Institution institution = TestDataUtil.createDefaultInstitution();
		institution.setMandant(mandant);
		institution.setTraegerschaft(null);
		institution = persistence.persist(institution);
		Assert.assertEquals("Institution1", institution.getName());

		JaxInstitutionStammdaten jaxStammdaten = TestJaxDataUtil.createTestJaxInstitutionsStammdaten();
		jaxStammdaten.setInstitution(converter.institutionToJAX(institution));
		jaxStammdaten.getInstitution().setName("ChangedInstitution");
		final JaxInstitutionStammdaten updatedInstitution = institutionStammdatenResource.saveInstitutionStammdaten(jaxStammdaten, DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(updatedInstitution);
		Assert.assertEquals("Institution1", updatedInstitution.getInstitution().getName());
	}

	@Test
	public void betreuungSpeichernDarfInstitutionsStammdatenNichtUpdaten() {
		InstitutionStammdaten kitaBruennen = TestDataUtil.createInstitutionStammdatenKitaBruennen();
		Assert.assertEquals(Constants.START_OF_TIME, kitaBruennen.getGueltigkeit().getGueltigAb());

		Gesuchsperiode gesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());

		testdataCreationService.insertParametersForTestfaelle(config);

		Gesuch gesuch = testdataCreationService.createErstgesuch(config);
		Betreuung betreuung = gesuch.extractAllBetreuungen().get(0);
		JaxBetreuung jaxBetreuung = converter.betreuungToJAX(betreuung);
		jaxBetreuung.setInstitutionStammdaten(converter.institutionStammdatenToJAX(kitaBruennen));
		jaxBetreuung.getInstitutionStammdaten().setGueltigAb(LocalDate.now());
		betreuungResource.saveBetreuung(converter.toJaxId(betreuung.getKind()), jaxBetreuung, false, DUMMY_URIINFO, DUMMY_RESPONSE);

		InstitutionStammdaten loadedKitaBruennen = criteriaQueryHelper.getAll(InstitutionStammdaten.class).iterator().next();
		Assert.assertEquals(Constants.START_OF_TIME, loadedKitaBruennen.getGueltigkeit().getGueltigAb());
	}

	@Test
	public void pensumFachstelleSpeichernDarfFachstelleNichtUpdaten() {
		Fachstelle fachstelle = TestDataUtil.createDefaultFachstelle();
		fachstelle = persistence.persist(fachstelle);
		Assert.assertEquals("Fachstelle1", fachstelle.getName());

		Gesuchsperiode gesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());

		testdataCreationService.insertParametersForTestfaelle(config);

		Gesuch gesuch = testdataCreationService.createErstgesuch(config);
		KindContainer kindContainer = gesuch.getKindContainers().iterator().next();
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setFachstelle(fachstelle);
		pensumFachstelle.setPensum(50);
		kindContainer.getKindJA().setPensumFachstelle(pensumFachstelle);
		kindContainer = persistence.merge(kindContainer);
		JaxKindContainer jaxKindContainer = converter.kindContainerToJAX(kindContainer);
		Assert.assertNotNull(jaxKindContainer.getKindJA().getPensumFachstelle());
		jaxKindContainer.getKindJA().getPensumFachstelle().getFachstelle().setName("FachstelleChanged");
		kindResource.saveKind(converter.toJaxId(gesuch), jaxKindContainer, DUMMY_URIINFO, DUMMY_RESPONSE);

		Fachstelle loadedFachstelle = criteriaQueryHelper.getAll(Fachstelle.class).iterator().next();
		Assert.assertEquals("Fachstelle1", loadedFachstelle.getName());
	}
}

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

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.api.resource.*;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.errors.EbeguFingerWegException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.services.TestdataCreationService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests fuer den JaxBConverter. Insbesondere wird geprüft, dass beim Speichern von Gesuchsdaten keine Stammdaten
 * verändert werden dürfen.
 * Gesuchsperiode
 * - beim Speichern von Gesuch => gesuchSpeichernDarfGesuchsperiodeNichtUpdaten
 * Mandant
 * - beim Speichern von Institution => institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten
 * Trägerschaft
 * - beim Speichern von Institution => institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten
 * Institution
 * - beim Speichern von InstitutionsStammdaten => institutionsStammdatenSpeichernDarfInstitutionNichtUpdaten
 * InstitutionsStammdaten
 * - beim Speichern von Betreuung => betreuungSpeichernDarfInstitutionsStammdatenNichtUpdaten
 * Fachstelle
 * - beim Speichern von PensumFachstelle => pensumFachstelleSpeichernDarfFachstelleNichtUpdaten
 */
@SuppressWarnings({ "LocalVariableNamingConvention", "InstanceMethodNamingConvention",
	"InstanceVariableNamingConvention" })
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
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

	@Inject
	private JaxBConverter converter;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void init() {
		// Tests initialisieren
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		final InstitutionStammdaten kitaAaregg = TestDataUtil.createInstitutionStammdatenKitaWeissenstein();
		final InstitutionStammdaten kitaBruennen = TestDataUtil.createInstitutionStammdatenKitaBruennen();
		final InstitutionStammdaten tagesfamilien = TestDataUtil.createInstitutionStammdatenTagesfamilien();
		final InstitutionStammdaten tagesschule = TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode);
		Mandant mandant = TestDataUtil.getMandantKantonBernAndPersist(persistence);
		TestdataSetupConfig setupConfig =
			new TestdataSetupConfig(mandant, kitaBruennen, kitaAaregg, tagesfamilien, tagesschule, gesuchsperiode);
		testdataCreationService.setupTestdata(setupConfig);
	}

	@Transactional(TransactionMode.DEFAULT)
	@Test
	public void gesuchSpeichernDarfGesuchsperiodeNichtUpdaten() {
		assertEquals(GesuchsperiodeStatus.AKTIV, gesuchsperiode.getStatus());

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());

		Gesuch gesuch = testdataCreationService.createErstgesuch(config, gesuchsperiode.getMandant());
		JaxGesuch jaxGesuch = TestJaxDataUtil.createTestJaxGesuch(null, null);
		jaxGesuch.setTyp(AntragTyp.MUTATION);
		jaxGesuch.setDossier(converter.dossierToJAX(gesuch.getDossier()));
		jaxGesuch.setGesuchsperiode(converter.gesuchsperiodeToJAX(gesuchsperiode));
		jaxGesuch.getGesuchsperiode().setStatus(GesuchsperiodeStatus.INAKTIV);
		gesuchResource.create(jaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE);

		Gesuchsperiode loadedGesuchsperiode = criteriaQueryHelper.getAll(Gesuchsperiode.class).iterator().next();
		assertEquals(GesuchsperiodeStatus.AKTIV, loadedGesuchsperiode.getStatus());
	}

	@Test
	public void institutionSpeichernDarfMandantUndTraegerschaftNichtUpdaten() {
		LocaleThreadLocal.set(Constants.DEFAULT_LOCALE);
		Mandant mandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft(mandant);
		String nameTraegerschaft = traegerschaft.getName();
		traegerschaft = persistence.persist(traegerschaft);
		assertEquals("Kanton Bern", mandant.getName());
		assertEquals(nameTraegerschaft, traegerschaft.getName());

		Institution institution = TestDataUtil.createDefaultInstitution();
		institution.setTraegerschaft(traegerschaft);
		institution.setMandant(mandant);
		JaxInstitution jaxInstitution = converter.institutionToJAX(institution);
		jaxInstitution.getTraegerschaft().setName("ChangedTraegerschaft");
		jaxInstitution.getMandant().setName("ChangedMandant");
		institutionResource.createInstitution(jaxInstitution, "2020-01-01",
			BetreuungsangebotTyp.KITA, "mail@example.com", null, DUMMY_URIINFO, DUMMY_RESPONSE);

		Mandant loadedMandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		final Traegerschaft loadedTraegerschaft = persistence.find(Traegerschaft.class, traegerschaft.getId());
		assertEquals("Kanton Bern", loadedMandant.getName());
		assertEquals(nameTraegerschaft, loadedTraegerschaft.getName());
	}

	/**
	 * Institution ist trotzdem updated wenn:
	 * Der Name ist geaendert
	 * Der Traegerschaft ist geaendert
	 * Der Status ist geandert
	 */
	@Test
	public void institutionsStammdatenSpeichernDarfInstitutionNichtUpdaten() {
		Mandant mandant = criteriaQueryHelper.getAll(Mandant.class).iterator().next();
		Institution institution = TestDataUtil.createDefaultInstitution();
		institution.setMandant(mandant);
		institution.setTraegerschaft(null);
		institution = persistence.persist(institution);
		assertEquals("Institution1", institution.getName());

		JaxInstitutionStammdaten jaxStammdaten = TestJaxDataUtil.createTestJaxInstitutionsStammdaten();
		jaxStammdaten.setInstitution(converter.institutionToJAX(institution));
		jaxStammdaten.getInstitution().setStammdatenCheckRequired(true);
		JaxInstitutionUpdate jaxUpdate = new JaxInstitutionUpdate();
		jaxUpdate.setStammdaten(jaxStammdaten);

		JaxId id = new JaxId(institution.getId());
		JaxInstitutionStammdaten updatedInstitution = institutionResource.updateInstitutionAndStammdaten(id, jaxUpdate);

		assertNotNull(updatedInstitution);
		assertEquals(false, updatedInstitution.getInstitution().isStammdatenCheckRequired());
	}

	@Test
	public void betreuungSpeichernDarfInstitutionsStammdatenNichtUpdaten() {
		InstitutionStammdaten kitaBruennen = TestDataUtil.createInstitutionStammdatenKitaBruennen();
		assertEquals(Constants.START_OF_TIME, kitaBruennen.getGueltigkeit().getGueltigAb());

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());

		Gesuch gesuch = testdataCreationService.createErstgesuch(config, gesuchsperiode.getMandant());
		Betreuung betreuung = gesuch.extractAllBetreuungen().get(0);
		JaxBetreuung jaxBetreuung = converter.betreuungToJAX(betreuung);
		jaxBetreuung.setInstitutionStammdaten(converter.institutionStammdatenSummaryToJAX(
			kitaBruennen,
			new JaxInstitutionStammdatenSummary()));
		jaxBetreuung.getInstitutionStammdaten().setGueltigAb(LocalDate.now());
		betreuungResource.saveBetreuung(jaxBetreuung, false, DUMMY_URIINFO, DUMMY_RESPONSE);

		InstitutionStammdaten loadedKitaBruennen =
			criteriaQueryHelper.getAll(InstitutionStammdaten.class).iterator().next();
		assertEquals(Constants.START_OF_TIME, loadedKitaBruennen.getGueltigkeit().getGueltigAb());
	}

	@Test
	@Transactional(TransactionMode.DEFAULT) //to load lazy zeitabschnitte we keep a session
	public void pensumFachstelleSpeichernDarfFachstelleNichtUpdaten() {
		Fachstelle fachstelle = TestDataUtil.createDefaultFachstelle();
		TestDataUtil.persistFachstelle(persistence, fachstelle);
		assertEquals(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE, fachstelle.getName());

		final ErstgesuchConfig config = ErstgesuchConfig.createErstgesuchVerfuegt(
			TestfallName.BECKER_NORA, gesuchsperiode, LocalDate.now(), LocalDateTime.now());

		Gesuch gesuch = testdataCreationService.createErstgesuch(config, gesuchsperiode.getMandant());
		KindContainer kindContainer = gesuch.getKindContainers().iterator().next();
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setFachstelle(fachstelle);
		pensumFachstelle.setPensum(50);
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setKind(kindContainer.getKindJA());
		kindContainer.getKindJA().addPensumFachstelle(pensumFachstelle);
		kindContainer = persistence.persist(kindContainer);
		JaxKindContainer jaxKindContainer = converter.kindContainerToJAX(kindContainer);
		assertNotNull(jaxKindContainer.getKindJA().getPensumFachstellen());
		jaxKindContainer.getKindJA()
			.getPensumFachstellen()
			.stream().findFirst().orElseThrow()
			.getFachstelle()
			.setName(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE);
		kindResource.saveKind(converter.toJaxId(gesuch), jaxKindContainer, DUMMY_URIINFO, DUMMY_RESPONSE);

		Fachstelle loadedFachstelle = criteriaQueryHelper.getAll(Fachstelle.class).iterator().next();
		assertEquals(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE, loadedFachstelle.getName());
	}

	@Test
	public void lastenausgleichGrundlatenToEntityImmutable() {
		try {
			converter.lastenausgleichGrundlagenToEntity();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}

	@Test
	public void lastenausgleichGrundlagenToJAXImmutable() {
		try {
			converter.lastenausgleichGrundlagenToJAX();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}

	@Test
	public void lastenausgleichDetailListToEntityImmutable() {
		try {
			converter.lastenausgleichDetailListToEntity();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}

	@Test
	public void lastenausgleichDetailListToJaxImmutable() {
		try {
			converter.lastenausgleichDetailListToJax();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}

	@Test
	public void lastenausgleichDetailToEntityImmutable() {
		try {
			converter.lastenausgleichDetailToEntity();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}

	@Test
	public void lastenausgleichDetailToJAXImmutable() {
		try {
			converter.lastenausgleichDetailToJAX();
			Assert.fail("Die Methode darf nicht verwendet werden");
		} catch (EbeguFingerWegException dummy) {
			// expected
		}
	}
}

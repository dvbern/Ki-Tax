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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.AdresseService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SchulungService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test fuer den SchulungsService
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class SchulungServiceBeanTest extends AbstractEbeguLoginTest {

	private final int anzahlUserSchonVorhanden = 1;
	private final int anzahlGesuchsteller = 16;
	private final int anzahlInstitutionsBenutzer = 2;

	@Inject
	private SchulungService schulungService;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private AdresseService adresseService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private MandantService mandantService;

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Test
	public void resetSchulungsdaten() {
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.saveGesuchsperiode(TestDataUtil.createAndPersistGesuchsperiode1718(persistence));
		createAndSaveInstitutionStammdatenForTestfaelle();
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);

		Gemeinde paris = TestDataUtil.getGemeindeParis(persistence);
		assert paris.getMandant() != null;
		Mandant mandant = paris.getMandant();
		assertEmpty(mandant);
		schulungService.createSchulungsdaten();

		Assert.assertEquals(95, adresseService.getAllAdressen().size());
		Assert.assertEquals(6, institutionStammdatenService.getAllInstitutionStammdaten().size());
		Assert.assertEquals(6, institutionService.getAllInstitutionen(mandant).size());
		Assert.assertEquals(1, traegerschaftService.getAllTraegerschaften().size());
		Assert.assertEquals(anzahlUserSchonVorhanden + anzahlGesuchsteller + anzahlInstitutionsBenutzer,
			criteriaQueryHelper.getAll(Benutzer.class).size());

		schulungService.deleteSchulungsdaten();
		assertEmpty(mandant);
	}

	@Test
	public void deleteSchulungsdaten() {
		// Es muss auch "geloescht" werden koennen, wenn es schon (oder teilweise) geloescht ist
		schulungService.deleteSchulungsdaten();
		schulungService.deleteSchulungsdaten();
	}

	private void assertEmpty(Mandant testMandant) {
		Assert.assertEquals(3, institutionStammdatenService.getAllInstitutionStammdaten().size());
		Assert.assertEquals(3, institutionService.getAllInstitutionen(testMandant).size());
		Assert.assertTrue(traegerschaftService.getAllTraegerschaften().isEmpty());
		Assert.assertEquals(anzahlUserSchonVorhanden, criteriaQueryHelper.getAll(Benutzer.class).size());
	}

	private List<InstitutionStammdaten> createAndSaveInstitutionStammdatenForTestfaelle() {
		Mandant mandant = mandantService.getMandantBern();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagesfamilien());
		for (InstitutionStammdaten institutionStammdaten : institutionStammdatenList) {
			institutionStammdaten.getInstitution().setTraegerschaft(null);
			institutionStammdaten.getInstitution().setMandant(mandant);
			if (!institutionService.findInstitution(institutionStammdaten.getInstitution().getId(), true).isPresent()) {
				institutionService.createInstitution(institutionStammdaten.getInstitution());
			}
			institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
		}
		return institutionStammdatenList;
	}
}

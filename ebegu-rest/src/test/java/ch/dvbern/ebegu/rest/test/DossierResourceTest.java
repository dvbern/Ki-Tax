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
import java.time.Month;

import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.resource.DossierResource;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.services.InstitutionService;
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
 * Testet DossierResource
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class DossierResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private DossierResource dossierResource;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private Persistence persistence;
	@Inject
	private JaxBConverter converter;


	@Test
	public void updateVerantwortlicherUserForDossier() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(institutionService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);
		changeStatusToWarten(gesuch.getKindContainers().iterator().next());
		Benutzer sachbearbeiter = TestDataUtil.createAndPersistJABenutzer(persistence);

		JaxDossier foundDossier = dossierResource.findDossier(converter.toJaxId(gesuch.getDossier()));

		Assert.assertNotNull(foundDossier);
		Assert.assertNotNull(foundDossier.getVerantwortlicherBG());
		Assert.assertNotEquals(sachbearbeiter.getUsername(), foundDossier.getVerantwortlicherBG().getUsername());

		JaxBenutzerNoDetails userToSet = converter.benutzerToJaxBenutzerNoDetails(sachbearbeiter);
		foundDossier.setVerantwortlicherBG(userToSet);
		JaxDossier updatedDossier = (JaxDossier) dossierResource.create(foundDossier, DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();
		Assert.assertNotNull(updatedDossier);
		Assert.assertNotNull(updatedDossier.getVerantwortlicherBG());
		Assert.assertEquals(sachbearbeiter.getUsername(), updatedDossier.getVerantwortlicherBG().getUsername());
	}

	// HELP METHODS

	private void changeStatusToWarten(KindContainer kindContainer) {
		for (Betreuung betreuung : kindContainer.getBetreuungen()) {
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
			persistence.merge(betreuung);
		}
	}
}

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

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.api.resource.DossierResource;
import ch.dvbern.ebegu.api.resource.FallResource;
import ch.dvbern.ebegu.api.resource.GesuchResource;
import ch.dvbern.ebegu.api.resource.KindResource;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.rest.test.util.TestJaxDataUtil;
import ch.dvbern.ebegu.services.BenutzerService;
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

import javax.inject.Inject;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testet KindResource
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
@UsingDataSet("datasets/mandant-dataset.xml")
public class KindResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private KindResource kindResource;
	@Inject
	private GesuchResource gesuchResource;
	@Inject
	private FallResource fallResource;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private DossierResource dossierResource;

	@Inject
	private JaxBConverter converter;
	@Inject
	private Persistence persistence;

	@Test
	public void createKindTest() {
		final Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		JaxGesuch jaxGesuch = TestJaxDataUtil.createTestJaxGesuch(converter.gesuchsperiodeToJAX(gesuchsperiode1718), null);

		TestDataUtil.prepareParameters(gesuchsperiode1718, persistence);

		final Gemeinde gemeindeParis = TestDataUtil.getGemeindeParis(persistence);
		JaxGemeinde persistedGemeinde = converter.gemeindeToJAX(gemeindeParis);
		jaxGesuch.getDossier().getVerantwortlicherBG().getGemeindeIds().add(persistedGemeinde.getId());
		Benutzer benutzer = TestDataUtil.createDefaultBenutzer();
		benutzer.setUsername(jaxGesuch.getDossier().getVerantwortlicherBG().getUsername());
		benutzer.setVorname(jaxGesuch.getDossier().getVerantwortlicherBG().getVorname());
		benutzer.setNachname(jaxGesuch.getDossier().getVerantwortlicherBG().getNachname());
		benutzer.setMandant(Objects.requireNonNull(gesuchsperiode1718.getMandant()));
		benutzer.getCurrentBerechtigung().setRole(UserRole.ADMIN_BG);
		benutzer.getCurrentBerechtigung().getGemeindeList().add(gemeindeParis);
		benutzerService.saveBenutzer(benutzer);
		JaxFall returnedFall = fallResource.saveFall(jaxGesuch.getDossier().getFall(), DUMMY_URIINFO, DUMMY_RESPONSE);
		jaxGesuch.getDossier().setFall(returnedFall);
		jaxGesuch.getDossier().setGemeinde(persistedGemeinde);
		JaxDossier returnedDossier = (JaxDossier) dossierResource.create(jaxGesuch.getDossier(), DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();
		jaxGesuch.setDossier(returnedDossier);

		Assert.assertNotNull(returnedFall);
		JaxGesuchsperiode returnedGesuchsperiode = saveGesuchsperiodeInStatusAktiv(jaxGesuch.getGesuchsperiode());
		jaxGesuch.getDossier().setFall(returnedFall);
		jaxGesuch.setGesuchsperiode(returnedGesuchsperiode);
		JaxGesuch returnedGesuch = (JaxGesuch) gesuchResource.create(jaxGesuch, DUMMY_URIINFO, DUMMY_RESPONSE).getEntity();

		JaxKindContainer testJaxKindContainer = TestJaxDataUtil.createTestJaxKindContainer();

		testJaxKindContainer.getKindGS().getPensumFachstellen().forEach(fachstellenPensum -> {
			assertThat(fachstellenPensum.getFachstelle(), notNullValue());
			final Fachstelle fachstelle = persistence.persist(converter.fachstelleToEntity(fachstellenPensum.getFachstelle(), new Fachstelle()));
			fachstellenPensum.setFachstelle(converter.fachstelleToJAX(fachstelle));
		});

		testJaxKindContainer.getKindJA().getPensumFachstellen().forEach(fachstellenPensum -> {
			assertThat(fachstellenPensum.getFachstelle(), notNullValue());
			final Fachstelle fachstelle = persistence.persist(converter.fachstelleToEntity(fachstellenPensum.getFachstelle(), new Fachstelle()));
			fachstellenPensum.setFachstelle(converter.fachstelleToJAX(fachstelle));
		});

		JaxKindContainer jaxKindContainer = kindResource.saveKind(converter.toJaxId(returnedGesuch), testJaxKindContainer, DUMMY_URIINFO, DUMMY_RESPONSE);

		Assert.assertNotNull(jaxKindContainer);
		Assert.assertEquals(Integer.valueOf(1), jaxKindContainer.getKindNummer());
		Assert.assertEquals(Integer.valueOf(1), jaxKindContainer.getNextNumberBetreuung());
		Assert.assertNotNull(jaxKindContainer.getKindGS().getPensumFachstellen());

		JaxGesuch updatedGesuch = gesuchResource.findGesuch(converter.toJaxId(returnedGesuch));
		Assert.assertNotNull(updatedGesuch);
		Assert.assertEquals(Integer.valueOf(2), updatedGesuch.getDossier().getFall().getNextNumberKind());
		Assert.assertEquals(1, updatedGesuch.getKindContainers().size());
		Assert.assertEquals(getFirstJaxPensumFachstelle(testJaxKindContainer).getPensum(), getFirstJaxPensumFachstelle(jaxKindContainer).getPensum());
	}

	private JaxPensumFachstelle getFirstJaxPensumFachstelle(JaxKindContainer jaxKindContainer) {
		return jaxKindContainer.getKindGS().getPensumFachstellen().stream().findFirst().orElseThrow();
	}
}

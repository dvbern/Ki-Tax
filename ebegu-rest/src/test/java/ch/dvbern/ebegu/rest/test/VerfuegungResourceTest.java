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
import java.time.Month;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegung;
import ch.dvbern.ebegu.api.resource.VerfuegungResource;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.test.TestDataUtil;
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
 * Testet VerfuegungResource
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class VerfuegungResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private VerfuegungResource verfuegungResource;
	@Inject
	private InstitutionService instService;
	@Inject
	private Persistence persistence;

	private Gesuchsperiode gesuchsperiode;

	private static final String DEFAULT_BEMERKUNG = "01.08.2017 - 31.07.2018: Für diesen Zeitraum ergibt sich ein anspruchsberechtigtes Pensum aufgrund des "
		+ "Beschäftigungspensums (Angestellt - Art. 34d ASIV).";

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		TestDataUtil.getGemeindeParis(persistence);
	}

	@Test
	public void saveVerfuegungTest() {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();

		String manuelleBemerkung = "manuelleBemerkung";

		final JaxVerfuegung persistedVerfuegung = verfuegungResource.saveVerfuegung(new JaxId(gesuch.getId()), new JaxId(betreuung.getId()), false, manuelleBemerkung);

		assert persistedVerfuegung != null;
		Assert.assertEquals(DEFAULT_BEMERKUNG, persistedVerfuegung.getGeneratedBemerkungen());
		Assert.assertEquals(manuelleBemerkung, persistedVerfuegung.getManuelleBemerkungen());

	}

	@Test
	public void nichtEintretenTest() {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);
		Betreuung betreuung = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next();
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		Betreuung storedBetr = persistence.merge(betreuung);

		JaxId gesuchId = new JaxId(gesuch.getId());
		JaxId betreuungId = new JaxId(storedBetr.getId());

		final JaxVerfuegung persistedVerfuegung = verfuegungResource.schliessenNichtEintreten(gesuchId, betreuungId);

		Assert.assertNotNull(persistedVerfuegung);
		persistedVerfuegung.getZeitabschnitte().forEach(jaxVerfZeitabsch -> Assert.assertEquals(0, jaxVerfZeitabsch.getAnspruchberechtigtesPensum()));
		Betreuung storedBetreuung = persistence.find(Betreuung.class, betreuung.getId());
		Assert.assertEquals(Betreuungsstatus.NICHT_EINGETRETEN, storedBetreuung.getBetreuungsstatus());
	}

	@Test
	public void testCalculateVerfuegung() {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);

		//noinspection ConstantConditions
		Response response = verfuegungResource.calculateVerfuegung(new JaxId(gesuch.getId()), null, null);

		Assert.assertNotNull(response);
	}
}

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

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.DownloadResource;
import ch.dvbern.ebegu.api.resource.VerfuegungResource;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.GeneratedDokument_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Testet BetreuungResource
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class DownloadResourceTest extends AbstractEbeguRestLoginTest {

	@Inject
	private DownloadResource downloadResource;
	@Inject
	private VerfuegungResource verfuegungResource;
	@Inject
	private InstitutionService instService;
	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper queryHelper;
	@Inject
	private JaxBConverter converter;

	private Gesuchsperiode gesuchsperiode;

	@Before
	public void setUp() {
		gesuchsperiode = TestDataUtil.createAndPersistGesuchsperiode1718(persistence);
		TestDataUtil.prepareParameters(gesuchsperiode, persistence);
		TestDataUtil.getGemeindeParis(persistence);
	}

	@Test
	public void getVerfuegungDokumentAccessTokenGeneratedDokumentTest() throws MergeDocException, IOException, MimeTypeParseException {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);


		Assert.assertNotNull(gesuch.getKindContainers().iterator().next().getBetreuungen());
		@SuppressWarnings("ConstantConditions") // Wird oben geprueft
		final String betreuungId = gesuch.getKindContainers().iterator().next().getBetreuungen().iterator().next().getId();

		HttpServletRequest request = mockRequest();
		UriInfo uri = new ResteasyUriInfo("uri", "query", "path");

		final Response dokumentResponse = downloadResource
			.getVerfuegungDokumentAccessTokenGeneratedDokument(new JaxId(gesuch.getId()), new JaxId(betreuungId), true, "", request, uri);

		assertResults(gesuch, dokumentResponse.getEntity(), GeneratedDokumentTyp.VERFUEGUNG);
	}

	@Test
	public void getDokumentAccessTokenGeneratedDokumentBEGLEITSCHREIBENTest() throws MergeDocException, MimeTypeParseException {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);


		HttpServletRequest request = mockRequest();
		UriInfo uri = new ResteasyUriInfo("uri", "query", "path");

		final Response dokumentResponse = downloadResource
			.getBegleitschreibenDokumentAccessTokenGeneratedDokument(new JaxId(gesuch.getId()), request, uri);

		assertResults(gesuch, dokumentResponse.getEntity(), GeneratedDokumentTyp.BEGLEITSCHREIBEN);
	}

	@Test
	public void getDokumentAccessTokenGeneratedDokumentFINANZIELLE_SITUATIONTest() throws MergeDocException, MimeTypeParseException {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);

		HttpServletRequest request = mockRequest();
		UriInfo uri = new ResteasyUriInfo("uri", "query", "path");

		final Response dokumentResponse = downloadResource
			.getFinSitDokumentAccessTokenGeneratedDokument(new JaxId(gesuch.getId()), request, uri);

		assertResults(gesuch, dokumentResponse.getEntity(), GeneratedDokumentTyp.FINANZIELLE_SITUATION);
	}

	@Transactional(TransactionMode.DEFAULT)
	@Test
	public void getMahnungDokumentAccessTokenGeneratedDokumentTest() throws MergeDocException, IOException, MimeTypeParseException {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);

		Mahnung mahnung = TestDataUtil.createMahnung(MahnungTyp.ERSTE_MAHNUNG, gesuch);

		HttpServletRequest request = mockRequest();
		UriInfo uri = new ResteasyUriInfo("uri", "query", "path");

		final Response dokumentResponse = downloadResource
			.getMahnungDokumentAccessTokenGeneratedDokument(converter.mahnungToJAX(mahnung), request, uri);

		assertResults(gesuch, dokumentResponse.getEntity(), GeneratedDokumentTyp.MAHNUNG);
	}

	@Test
	public void getNichteintretenDokumentAccessTokenGeneratedDokumentTest() throws MergeDocException, IOException, MimeTypeParseException {
		final Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence,
			LocalDate.of(1980, Month.MARCH, 25), null, gesuchsperiode);

		JaxId gesuchId = new JaxId(gesuch.getId());
		JaxId betreuungID = new JaxId(gesuch.extractAllBetreuungen().get(0).getId());

		verfuegungResource.schliessenNichtEintreten(gesuchId, betreuungID);

		HttpServletRequest request = mockRequest();
		UriInfo uri = new ResteasyUriInfo("uri", "query", "path");

		final Response dokumentResponse = downloadResource
			.getNichteintretenDokumentAccessTokenGeneratedDokument(betreuungID, true, request, uri);

		assertResults(gesuch, dokumentResponse.getEntity(), GeneratedDokumentTyp.NICHTEINTRETEN);
	}

	// HELP METHODS

	@Nonnull
	private HttpServletRequest mockRequest() {
		HttpServletRequest request = createMock(HttpServletRequest.class);
		expect(request.getHeader("X-FORWARDED-FOR")).andReturn("1.1.1.1").anyTimes();
		replay(request);
		return request;
	}

	private void assertResults(Gesuch gesuch, Object entity, GeneratedDokumentTyp typ) {
		final Collection<GeneratedDokument> generatedDokumente = queryHelper
			.getEntitiesByAttribute(GeneratedDokument.class, gesuch, GeneratedDokument_.gesuch);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(generatedDokumente);
		Assert.assertEquals(1, generatedDokumente.size());
		Assert.assertEquals(typ, generatedDokumente.iterator().next().getTyp());
	}

}

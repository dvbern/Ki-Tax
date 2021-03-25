/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.personensuche;

import java.time.LocalDate;

import javax.inject.Inject;

import ch.bedag.geres.schemas._20180101.geresechtypes.IdType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoParametersType;
import ch.dvbern.ebegu.cdi.Geres;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.ws.ewk.IGeresWebService;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: homa
 * Date: 1/10/20
 * comments homa
 */
@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
@Ignore
public class GeresWebServiceTest extends AbstractEbeguLoginTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeresWebServiceTest.class);

	@Inject
	@Geres
	private IGeresWebService geresWebService;
	private String oldValForSoapLogging = "false";

	@Before
	public  void enableSoapLOGGER(){
		oldValForSoapLogging = System.getProperty("org.apache.cxf.logging.enabled", "false");
		System.setProperty("org.apache.cxf.logging.enabled", "true");

		//to run tets against real service set the following properties
		//	System.setProperty("ebegu.personensuche.sts.keystore.path", "/home/homa/java/ideaprojects/jugendamt/kibon/ebegu/ebegu-server/target/wildfly-14.0.1.Final/rkb1-svbern-sts-ks-u.jks");
		//	System.setProperty("ebegu.personensuche.sts.keystore.pw", "MySecretPassword");

	}

	@After
	public void resetSoapLOGGER(){
		System.setProperty("org.apache.cxf.logging.enabled", oldValForSoapLogging);
	}

	@Test
	public void testGeres() throws STSZertifikatServiceException, PersonenSucheServiceException {

		final String test = geresWebService.test();
		Assert.assertEquals("OK", test);
	}


	@Test
	public void testGeresPersonensuche() throws STSZertifikatServiceException, PersonenSucheServiceException {
		ResidentInfoParametersType testparam = new ResidentInfoParametersType();
		testparam.setFirstName("Markus");
		testparam.setMunicipalityNumber(351);
		testparam.setOfficialName("Hofstetter");
		testparam.setDateOfBirth(LocalDate.of(1984,9,12));
		testparam.setSex(1);
		LocalDate validityDate = LocalDate.now();
		Integer searchMax = 20;
		final EWKResultat ewkResultat = geresWebService.residentInfoFast(testparam, validityDate, searchMax);
		Assert.assertNotNull(ewkResultat);
	}

	@Test
	public void testGeresPersonensucheDavidWeibel() throws STSZertifikatServiceException, PersonenSucheServiceException {

		ResidentInfoParametersType testparam = new ResidentInfoParametersType();
		testparam.setFirstName("David");
		testparam.setMunicipalityNumber(355);
		testparam.setOfficialName("Weibel");
		testparam.setDateOfBirth(LocalDate.of(1978,3,23));
		testparam.setSex(1);
		LocalDate validityDate = LocalDate.now();
		Integer searchMax = 20;
		final EWKResultat ewkResultat = geresWebService.residentInfoFast(testparam, validityDate, searchMax);
		Assert.assertNotNull(ewkResultat);
	}



	@Test
	public void testGeresPersonensucheFull() throws STSZertifikatServiceException, PersonenSucheServiceException {

		ResidentInfoParametersType testparam = new ResidentInfoParametersType();
		testparam.setFirstName("Markus");
		testparam.setMunicipalityNumber(351);
		testparam.setOfficialName("Hofstetter");
		testparam.setDateOfBirth(LocalDate.of(1984,9,12));
		testparam.setSex(1);
		LocalDate validityDate = LocalDate.now();
		Integer searchMax = 20;
		final EWKResultat ewkResultat = geresWebService.residentInfoFull(testparam, validityDate, searchMax);
		Assert.assertNotNull(ewkResultat);
	}

	@Test
	public void testGeresPersonensucheFullById() throws STSZertifikatServiceException, PersonenSucheServiceException {

		ResidentInfoParametersType testparam = new ResidentInfoParametersType();

		IdType id = new IdType();
		id.setPersonId("7565929841835");
		id.setPersonIdCategory("CH.VN");
		testparam.setPersonId(id);

		LocalDate validityDate = LocalDate.now();
		Integer searchMax = 20;
		final EWKResultat ewkResultat = geresWebService.residentInfoFull(testparam, validityDate, searchMax);
		Assert.assertNotNull(ewkResultat);
	}

	@Test
	public void testGeresPersonensucheFullByWohnungsId() throws STSZertifikatServiceException, PersonenSucheServiceException {

		ResidentInfoParametersType testparam = new ResidentInfoParametersType();
		testparam.setEgid(190197872L);
		testparam.setEwid(4L);

		LocalDate validityDate = LocalDate.now();
		Integer searchMax = 20;
		final EWKResultat ewkResultat = geresWebService.residentInfoFull(testparam, validityDate, searchMax);
		Assert.assertNotNull(ewkResultat);
	}

}

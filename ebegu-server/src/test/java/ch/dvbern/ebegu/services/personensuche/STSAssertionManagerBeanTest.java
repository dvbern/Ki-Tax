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

import javax.inject.Inject;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.TransformerException;

import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.ebegu.tests.AbstractEbeguLoginTest;
import ch.dvbern.ebegu.ws.ewk.sts.SAMLAuthenticationUtil;
import ch.dvbern.ebegu.ws.ewk.sts.STSAssertionManager;
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
public class STSAssertionManagerBeanTest  extends AbstractEbeguLoginTest {

	@Inject
	private STSAssertionManager stsAssertionManager;


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
	public void getValidSTSAssertionForPersonensucheTest() throws STSZertifikatServiceException, PersonenSucheServiceException, TransformerException {
		final SOAPElement validSTSAssertionForPersonensuche = stsAssertionManager.getValidSTSAssertionForPersonensuche();
		Assert.assertNotNull(validSTSAssertionForPersonensuche);
		String s = SAMLAuthenticationUtil.nodeToString(validSTSAssertionForPersonensuche);
		Assert.assertTrue(s.contains(":ds=\"http://www.w3.org/2000/09/xmldsig#"));

	}

	@Test
	public void forceRenewalTest() throws STSZertifikatServiceException, PersonenSucheServiceException {
		final SOAPElement validSTSAssertionForPersonensuche = stsAssertionManager.getValidSTSAssertionForPersonensuche();
		Assert.assertNotNull(validSTSAssertionForPersonensuche);
		SOAPElement renewdAssertion = stsAssertionManager.forceRenewalOfCurrentAssertion();
		Assert.assertNotNull(validSTSAssertionForPersonensuche);
		Assert.assertNotSame(validSTSAssertionForPersonensuche, renewdAssertion);

	}
	@Test
	public void forceReinitializationOfAssertionTest() throws STSZertifikatServiceException, PersonenSucheServiceException {
		final SOAPElement validSTSAssertionForPersonensuche = stsAssertionManager.getValidSTSAssertionForPersonensuche();
		Assert.assertNotNull(validSTSAssertionForPersonensuche);
		SOAPElement anotherAssertion = stsAssertionManager.forceReinitializationOfCurrentAssertion();
		Assert.assertNotNull(validSTSAssertionForPersonensuche);
		Assert.assertNotSame(validSTSAssertionForPersonensuche, anotherAssertion);
//		Assert.assertNotEquals(validSTSAssertionForPersonensuche.getAssertionID(), anotherAssertion.getAssertionID());

	}
}

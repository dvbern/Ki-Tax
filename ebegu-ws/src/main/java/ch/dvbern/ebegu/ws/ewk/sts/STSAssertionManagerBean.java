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
package ch.dvbern.ebegu.ws.ewk.sts;

import java.time.LocalDateTime;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.xml.soap.SOAPElement;

import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible to store the currently issued SAML1-Assertion that will be used
 * when calling the GERES Webservice.
 *
 * Clients should usually use the getValidSTSAssertionForPersonensuche Method to get the current Assertion.
 * In case there was no Assertion issued yet or the Assertion is no longer valid the manager will try to
 * obtain one
 */
@Singleton
public class STSAssertionManagerBean implements STSAssertionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(STSAssertionManagerBean.class.getSimpleName());
	public static final int GRACE_PERIOD = 5;

	private SOAPElement currentAssertionElement;
	private LocalDateTime notOnOrAfter;
	private LocalDateTime notBefore;

	private String renewalToken = null;
	private LocalDateTime maxRenewalTime;

	@Inject
	private STSWebService stsWebService;

	@Inject
	private RenewalAssertionWebService renewalAssertionWebService;


	@Override
	public SOAPElement getValidSTSAssertionForPersonensuche() throws STSZertifikatServiceException {
		//check assertion present
		if (currentAssertionElement == null) {
			issueSTSSamlAssertion();
			// assertion should  be set by WSSSecurityAssertionExtractionHandler

		} else if(!isAssertionPeriodValid()) {
			renewAssertion();
		}

		return currentAssertionElement;
	}

	@Override
	public SOAPElement forceRenewalOfCurrentAssertion() throws STSZertifikatServiceException {
		renewAssertion();
		return currentAssertionElement;
	}

	@Override
	public SOAPElement forceReinitializationOfCurrentAssertion() throws STSZertifikatServiceException {
		//noinspection ConstantConditions
		this.currentAssertionElement = null;
		return getValidSTSAssertionForPersonensuche();

	}

	@Override
	public void handleUpdatedAssertion(STSAssertionExtractionResult stsAssertionExtractionResult) {
		this.currentAssertionElement = stsAssertionExtractionResult.getAssertionXMLElement();
		this.renewalToken = stsAssertionExtractionResult.getRenewalToken();
		this.notOnOrAfter = stsAssertionExtractionResult.getNotOnOrAfter();
		this.notBefore = stsAssertionExtractionResult.getNotBefore();
		this.maxRenewalTime = stsAssertionExtractionResult.getMaxRenewalTime();
	}


	private boolean isAssertionPeriodValid() {

		final LocalDateTime now = LocalDateTime.now();
		return now.isAfter(this.notBefore.minusSeconds(GRACE_PERIOD)) && now.isBefore(this.notOnOrAfter);
	}

	private void issueSTSSamlAssertion() throws STSZertifikatServiceException {
		//this service will call our handleUpdateAssertion method. This is not very nice but allows us to use
		//the generated service with a response handler to extract the smal1 assertion as is
		stsWebService.getSamlAssertionForBatchuser();

	}

	private void renewAssertion() throws STSZertifikatServiceException {
		Validate.notNull(currentAssertionElement, "Managed current Assertion must be set if renewAssertion is triggerd");
		Validate.notNull(renewalToken, "Managed current renewal Token must be set if renewAssertion is triggerd");

		if (LocalDateTime.now().isBefore(maxRenewalTime)) {

			final STSAssertionExtractionResult stsAssertionExtractionResult = renewalAssertionWebService.renewAssertion(this.currentAssertionElement,
				renewalToken);
			handleUpdatedAssertion(stsAssertionExtractionResult);
		} else {
			LOGGER.info("Assertion can not be renewd anymore. Trigger a reinitialization");
			forceReinitializationOfCurrentAssertion(); // should use renewal service
		}
	}
}

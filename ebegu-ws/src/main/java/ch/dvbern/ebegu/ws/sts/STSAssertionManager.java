/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.ws.sts;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.xml.soap.SOAPElement;

import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dieser Service managed die Assertion die verwendet wird um eine EWK Abfrage zu machen.
 * KiBon verwendet eine einzelne Assertion um auf den EWK Service zuzugreiffen. Es ist
 * die Aufgabe dieses Service sich diese Assertion zu beschaffen. Sollte die Zeitspanne in der
 * die Assertion valid ist abgelaufen sein so ist es ebenfalls Aufgabe diesese Service die
 * Assertion zu erneuern
 */
public abstract class STSAssertionManager {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass().getSimpleName());

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


	public SOAPElement getValidSTSAssertionForWebserviceType(WebserviceType webserviceType) throws STSZertifikatServiceException {
		//check assertion present
		if (currentAssertionElement == null) {
			issueSTSSamlAssertion(webserviceType);
			// assertion should  be set by WSSSecurityAssertionExtractionHandler

		} else if(!isAssertionPeriodValid()) {
			renewAssertion(webserviceType);
		}

		return currentAssertionElement;
	}

	public SOAPElement forceRenewalOfCurrentAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException {
		renewAssertion(webserviceType);
		return currentAssertionElement;
	}

	public SOAPElement forceReinitializationOfCurrentAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException {
		//noinspection ConstantConditions
		this.currentAssertionElement = null;
		return getValidSTSAssertionForWebserviceType(webserviceType);

	}

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

	private void issueSTSSamlAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException {
		//this service will call our handleUpdateAssertion method. This is not very nice but allows us to use
		//the generated service with a response handler to extract the smal1 assertion as is
		stsWebService.getSamlAssertionForBatchuser(webserviceType);

	}

	private void renewAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException {
		Validate.notNull(currentAssertionElement, "Managed current Assertion must be set if renewAssertion is triggerd");
		Validate.notNull(renewalToken, "Managed current renewal Token must be set if renewAssertion is triggerd");

		if (LocalDateTime.now().isBefore(maxRenewalTime)) {

			final STSAssertionExtractionResult stsAssertionExtractionResult = renewalAssertionWebService.renewAssertion(this.currentAssertionElement,
				renewalToken, webserviceType);
			handleUpdatedAssertion(stsAssertionExtractionResult);
		} else {
			LOGGER.info("Assertion can not be renewd anymore. Trigger a reinitialization");
			forceReinitializationOfCurrentAssertion(webserviceType); // should use renewal service
		}
	}
}

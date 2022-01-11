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

import javax.xml.soap.SOAPElement;

/**
 * DTO Klasse fuer die Assertion und das Renew Token welches aus dem STS Service zurueckkommt.
 * Wird als unveraendertes SOAP Element gespeichert
 */
public class STSAssertionExtractionResult {
	private final String renewalToken;
	private final SOAPElement assertionXMLElement;
	private  LocalDateTime notBefore;
	private  LocalDateTime notAtOrAfter;
	private  LocalDateTime maxRenewalTime;

	public STSAssertionExtractionResult(SOAPElement assertionXMLElement, String renewalToken) {
		this.assertionXMLElement = assertionXMLElement;
		this.renewalToken = renewalToken;


	}

	public STSAssertionExtractionResult(SOAPElement assertionElement, String renewalToken, LocalDateTime notBefore, LocalDateTime notAtOrAfter, LocalDateTime maxRenewalTime) {
		this(assertionElement, renewalToken);
		this.notBefore = notBefore;
		this.notAtOrAfter = notAtOrAfter;
		this.maxRenewalTime = maxRenewalTime;
	}

	public String getRenewalToken() {
		return renewalToken;
	}

	public SOAPElement getAssertionXMLElement() {
		return assertionXMLElement;
	}

	public LocalDateTime getNotBefore() {
		return notBefore;
	}

	public LocalDateTime getNotOnOrAfter() {
		return notAtOrAfter;
	}

	public LocalDateTime getMaxRenewalTime() {
		return maxRenewalTime;
	}
}

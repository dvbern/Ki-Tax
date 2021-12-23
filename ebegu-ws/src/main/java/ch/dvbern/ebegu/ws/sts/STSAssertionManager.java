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

import javax.xml.soap.SOAPElement;

import ch.dvbern.ebegu.errors.STSZertifikatServiceException;

/**
 * Dieser Service managed die Assertion die verwendet wird um eine EWK Abfrage zu machen.
 * KiBon verwendet eine einzelne Assertion um auf den EWK Service zuzugreiffen. Es ist
 * die Aufgabe dieses Service sich diese Assertion zu beschaffen. Sollte die Zeitspanne in der
 * die Assertion valid ist abgelaufen sein so ist es ebenfalls Aufgabe diesese Service die
 * Assertion zu erneuern
 */
public interface STSAssertionManager {

	/**
	 * Gibt die noch gültige, falls vorhanden, Assertion zurück oder erneuert diese oder erstellt eine neue
	 */
	SOAPElement getValidSTSAssertionForWebserviceType(WebserviceType webserviceType) throws STSZertifikatServiceException;

	SOAPElement forceRenewalOfCurrentAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException;

	SOAPElement forceReinitializationOfCurrentAssertion(WebserviceType webserviceType) throws STSZertifikatServiceException;

	/**
	 * Schreibt die erneuerte oder neue Assertion in den Manager
	 */
	void handleUpdatedAssertion(STSAssertionExtractionResult stsAssertionExtractionResult);
}

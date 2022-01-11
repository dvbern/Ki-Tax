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

import oasis.names.tc.saml._1_0.assertion.AssertionType;

/**
 * DTO Klasse fuer die Assertion und das Renew Token welches aus dem STS Service zurueckkommt
 */
public class STSWebServiceResult {
	private final String renewalToken;
	private final AssertionType assertion;

	public STSWebServiceResult(AssertionType assertion, String renewalToken) {

		this.assertion = assertion;
		this.renewalToken = renewalToken;
	}

	public String getRenewalToken() {
		return renewalToken;
	}

	public AssertionType getAssertion() {
		return assertion;
	}
}

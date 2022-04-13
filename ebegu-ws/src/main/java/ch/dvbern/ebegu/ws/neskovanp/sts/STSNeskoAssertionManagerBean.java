/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.ws.neskovanp.sts;

import javax.ejb.Singleton;

import ch.dvbern.ebegu.ws.sts.STSAssertionManager;

/**
 * This class is responsible to store the currently issued SAML1-Assertion that will be used
 * when calling the GERES Webservice.
 *
 * Clients should usually use the getValidSTSAssertionForPersonensuche Method to get the current Assertion.
 * In case there was no Assertion issued yet or the Assertion is no longer valid the manager will try to
 * obtain one
 */
@Singleton
public class STSNeskoAssertionManagerBean extends STSAssertionManager {
}

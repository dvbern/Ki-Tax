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

package ch.dvbern.ebegu.errors;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;

/**
 * Exception, welche geworfen wird wenn beim Aufruf des Webservices zum Abfragen eines OIDC-Tokens ein Fehler passiert
 */
public class OIDCServiceException extends EbeguException {

	public OIDCServiceException(final String methodName, final String message) {
		super(methodName, message, ErrorCodeEnum.ERROR_KIBON_ANFRAGE_TECHNICAL);
	}

	public OIDCServiceException(final String methodName, final String message, final Throwable cause) {
		super(methodName, message, ErrorCodeEnum.ERROR_KIBON_ANFRAGE_TECHNICAL, cause);
	}
}

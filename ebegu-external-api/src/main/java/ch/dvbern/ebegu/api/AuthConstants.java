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

package ch.dvbern.ebegu.api;

/**
 * Constants that are important in connection with Authentication stuff (cookie, headerparams etc)
 */
public final class AuthConstants {
	public static final String COOKIE_PATH = "/";
	public static final String COOKIE_PRINCIPAL = "authId";
	public static final String COOKIE_AUTH_TOKEN = "authToken";
	public static final String PARAM_XSRF_TOKEN = "X-XSRF-TOKEN";
	public static final String COOKIE_XSRF_TOKEN = "XSRF-TOKEN";
	public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
	public static final String COOKIE_MANDANT = "mandant";
	public static final String COOKIE_MANDANT_REDIRECT = "mandantRedirect";

	public static final String COOKIE_AUTHORIZATION_HEADER = "Authorization";
	public static final int COOKIE_TIMEOUT_SECONDS = 60 * 60 * 12; //aktuell 12h
	/**
	 * Path to locallogin page (relative to base path) that will be used if no login connector api is specified
	 */
	public static final String LOCALLOGIN_PATH = "/#/locallogin";
}

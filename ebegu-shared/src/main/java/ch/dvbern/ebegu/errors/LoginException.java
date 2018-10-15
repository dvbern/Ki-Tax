/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.errors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;

public class LoginException extends EbeguRuntimeException {

	private static final long serialVersionUID = 6434204197010381768L;

	// TODO does not get mapped from FedletServlet (results in an "500 internal error")
	public LoginException(
		@Nonnull ErrorCodeEnum errorCodeEnum,
		@Nonnull String persistedEmail,
		@Nonnull String externalEmail) {
		super(null, errorCodeEnum, persistedEmail, externalEmail);
	}
}

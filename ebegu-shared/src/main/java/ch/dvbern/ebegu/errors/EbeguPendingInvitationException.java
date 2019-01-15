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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.ApplicationException;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import org.slf4j.event.Level;

@ApplicationException(rollback = true)
public class EbeguPendingInvitationException extends RuntimeException {

	private static final long serialVersionUID = 306424922900479199L;

	private final String methodName;
	private final List<Serializable> args;
	@Nullable
	private final ErrorCodeEnum errorCodeEnum;
	@Nullable
	private final String customMessage;

	private Level logLevel = Level.WARN; // Defaultmaessig loggen wir im WARN-level

	public EbeguPendingInvitationException(
		@Nullable String methodeName,
		@Nonnull String message,
		@Nonnull Serializable... messageArgs) {

		super(message);
		methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		errorCodeEnum = null;
		customMessage = null;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<Serializable> getArgs() {
		return args;
	}

	@Nullable
	public ErrorCodeEnum getErrorCodeEnum() {
		return errorCodeEnum;
	}

	@Nullable
	public String getCustomMessage() {
		return customMessage;
	}

	@Nonnull
	public Level getLogLevel() {
		return logLevel;
	}
}

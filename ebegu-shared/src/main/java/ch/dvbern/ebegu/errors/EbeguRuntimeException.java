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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.ApplicationException;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;

/**
 * Created by imanol on 02.03.16.
 * Oberklasse fuer Runtime Exceptions in ebegu
 */
@SuppressWarnings("OverloadedVarargsMethod")
@ApplicationException(rollback = true)
public class EbeguRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 306424922900479199L;

	private final String methodName;
	private final List<Serializable> args;
	@Nullable
	private final ErrorCodeEnum errorCodeEnum;
	@Nullable
	private final String customMessage;
	@Nullable
	private Mandant mandant;

	private KibonLogLevel logLevel = KibonLogLevel.WARN; // Defaultmaessig loggen wir im WARN-level

	public EbeguRuntimeException(
		@Nullable String methodeName,
		@Nonnull String message,
		@Nonnull Serializable... messageArgs) {

		super(message);
		methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		errorCodeEnum = null;
		customMessage = null;
	}

	public EbeguRuntimeException(
		@Nullable String methodeName,
		@Nonnull String message,
		@Nullable String customMessage,
		@Nonnull Serializable... messageArgs) {

		super(message);
		methodName = methodeName;
		this.customMessage = customMessage;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		errorCodeEnum = null;
	}

	public EbeguRuntimeException(
		@Nonnull KibonLogLevel logLevel,
		@Nullable String methodeName,
		@Nonnull String message,
		@Nullable String customMessage,
		@Nonnull Serializable... messageArgs) {

		super(message);
		this.logLevel = logLevel;
		this.methodName = methodeName;
		this.customMessage = customMessage;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		this.errorCodeEnum = null;
	}

	public EbeguRuntimeException(
		@Nonnull KibonLogLevel logLevel,
		@Nullable String methodeName,
		@Nonnull String message,
		@Nonnull Serializable... messageArgs) {

		super(message);
		this.logLevel = logLevel;
		this.methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		this.errorCodeEnum = null;
		this.customMessage = null;
	}

	public EbeguRuntimeException(
		@Nullable String methodeName,
		@Nullable String message,
		@Nullable Throwable cause,
		@Nonnull Serializable... messageArgs) {

		super(message, cause);
		this.methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		errorCodeEnum = null;
		customMessage = null;
	}

	protected EbeguRuntimeException(
		@Nullable String methodeName,
		@Nullable String message,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nullable Throwable cause,
		@Nonnull Serializable... messageArgs) {

		super(message, cause);
		this.errorCodeEnum = errorCodeEnum;
		this.methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		customMessage = null;
	}

	protected EbeguRuntimeException(
		@Nullable String methodeName,
		@Nullable String message,
		@Nullable String customMessage,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nullable Throwable cause,
		@Nonnull Serializable... messageArgs) {

		super(message, cause);
		this.errorCodeEnum = errorCodeEnum;
		this.customMessage = customMessage;
		this.methodName = methodeName;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
	}

	public EbeguRuntimeException(
		@Nullable String methodeName,
		@Nullable String message,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nonnull Serializable... messageArgs) {

		super(message);
		methodName = methodeName;
		this.errorCodeEnum = errorCodeEnum;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		customMessage = null;
	}

	public EbeguRuntimeException(
		@Nonnull KibonLogLevel logLevel,
		@Nullable String methodeName,
		@Nullable String message,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nonnull Serializable... messageArgs) {

		super(message);
		this.methodName = methodeName;
		this.errorCodeEnum = errorCodeEnum;
		this.logLevel = logLevel;
		this.args = Collections.unmodifiableList(Arrays.asList(messageArgs));
		this.customMessage = null;
	}

	public EbeguRuntimeException(
		@Nullable String methodName,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nonnull Serializable... args) {

		super(errorCodeEnum != null ? errorCodeEnum.name() : null);
		this.methodName = methodName;
		this.errorCodeEnum = errorCodeEnum;
		this.args = Collections.unmodifiableList(Arrays.asList(args));
		customMessage = null;
	}

	public EbeguRuntimeException(
		@Nullable String methodName,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nullable Mandant mandant,
		@Nonnull Serializable... args) {

		super(errorCodeEnum != null ? errorCodeEnum.name() : null);
		this.methodName = methodName;
		this.errorCodeEnum = errorCodeEnum;
		this.args = Collections.unmodifiableList(Arrays.asList(args));
		this.mandant = mandant;
		customMessage = null;
	}

	public EbeguRuntimeException(
		@Nonnull KibonLogLevel logLevel,
		@Nullable String methodName,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nonnull Serializable... args) {

		super(errorCodeEnum != null ? errorCodeEnum.name() : null);
		this.methodName = methodName;
		this.errorCodeEnum = errorCodeEnum;
		this.logLevel = logLevel;
		this.args = Collections.unmodifiableList(Arrays.asList(args));
		this.customMessage = null;
	}

	public EbeguRuntimeException(
		@Nullable String methodName,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nullable Throwable cause,
		@Nonnull Serializable... args) {

		super(cause);
		this.methodName = methodName;
		this.errorCodeEnum = errorCodeEnum;
		this.args = Collections.unmodifiableList(Arrays.asList(args));
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
	public KibonLogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(KibonLogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Nullable
	public Mandant getMandant() { return mandant;}
}

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

package ch.dvbern.ebegu.api.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.resteasy.api.validation.Validation;

import static ch.dvbern.ebegu.util.ServerMessageUtil.translateEnumValue;

/**
 * Created by imanol on 02.03.16.
 * Dies ist die Reportklasse fuer {@link EbeguException} und {@link EbeguRuntimeException}
 */
@XmlRootElement(name = "ebeguReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class EbeguExceptionReport {

	@Nullable
	private String exceptionName;
	@Nullable
	private String methodName;
	@Nullable
	private String translatedMessage;
	@Nullable
	private String customMessage;
	@Nullable
	private ErrorCodeEnum errorCodeEnum;
	private String stackTrace = null;
	@Nullable
	private String objectId; // das ID vom betroffenen Objekt, wenn es eins gibt
	@Nullable
	private List<Serializable> argumentList = new ArrayList<>();

	//	public EbeguExceptionReport(EbeguException exception) {
	//		if (exception != null) {
	//			this.exceptionName = exception.getClass().getSimpleName();
	//			this.translatedMessage = exception.getMessage();
	//			this.customMessage = exception.getCustomMessage();
	//			this.methodName = exception.getMethodName();
	//			this.argumentList.addAll(exception.getArgs());
	//		}
	//	}
	//
	//	public EbeguExceptionReport(EbeguRuntimeException exception) {
	//		if (exception != null) {
	//			this.exceptionName = exception.getClass().getSimpleName();
	//			this.translatedMessage = exception.getMessage();
	//			this.customMessage = exception.getCustomMessage();
	//			this.methodName = exception.getMethodName();
	//			this.argumentList.addAll(exception.getArgs());
	//		}
	//	}

	public EbeguExceptionReport(
		@Nullable String exceptionName,
		@Nullable ErrorCodeEnum errorCodeEnum,
		@Nullable String methodName,
		@Nullable String translatedMessage,
		@Nullable String customMessage,
		@Nullable String objectId,
		@Nullable List<Serializable> argumentList) {

		this.exceptionName = exceptionName;
		this.errorCodeEnum = errorCodeEnum;
		this.methodName = methodName;
		this.translatedMessage = translatedMessage;
		this.customMessage = customMessage;
		this.argumentList = argumentList;
		this.objectId = objectId;
	}

	@Nullable
	public String getExceptionName() {
		return exceptionName;
	}

	public void setExceptionName(@Nullable String exceptionName) {
		this.exceptionName = exceptionName;
	}

	@Nullable
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(@Nullable String methodName) {
		this.methodName = methodName;
	}

	@Nullable
	public String getTranslatedMessage() {
		return translatedMessage;
	}

	public void setTranslatedMessage(@Nullable String translatedMessage) {
		this.translatedMessage = translatedMessage;
	}

	@Nullable
	public List<Serializable> getArgumentList() {
		return argumentList;
	}

	@Nullable
	public String getCustomMessage() {
		return customMessage;
	}

	public void setCustomMessage(@Nullable String customMessage) {
		this.customMessage = customMessage;
	}

	@Nullable
	public ErrorCodeEnum getErrorCodeEnum() {
		return errorCodeEnum;
	}

	public void setErrorCodeEnum(@Nullable ErrorCodeEnum errorCodeEnum) {
		this.errorCodeEnum = errorCodeEnum;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public void setArgumentList(@Nullable List<Serializable> argumentList) {
		this.argumentList = argumentList;
	}

	@Nullable
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(@Nullable String objectId) {
		this.objectId = objectId;
	}

	@Nonnull
	public static Response buildResponse(
		Response.Status status,
		EbeguException ex,
		Locale localeFromHeader,
		Mandant mandant,
		boolean addDebugInfo) {

		Response.ResponseBuilder builder = setResponseHeaderAndStatus(status);
		Object[] args = ex.getArgs().toArray();
		String translatedEnumMessage = translateEnumValue(ex.getErrorCodeEnum(), localeFromHeader, mandant, args);

		EbeguExceptionReport exceptionReport = new EbeguExceptionReport(
			ex.getClass().getSimpleName(),
			ex.getErrorCodeEnum(),
			ex.getMethodName(),
			translatedEnumMessage,
			ex.getCustomMessage(),
			null,
			ex.getArgs());

		if (addDebugInfo) {
			addDevelopmentDebugInformation(exceptionReport, ex);
		}

		return builder.entity(exceptionReport).build();
	}

	@Nonnull
	public static Response buildResponse(
		Response.Status status,
		EbeguRuntimeException ex,
		Locale localeFromHeader,
		Mandant mandant,
		boolean addDebugInfo) {

		Response.ResponseBuilder builder = setResponseHeaderAndStatus(status);

		String objectId = null;
		if (ex instanceof EbeguExistingAntragException) {
			objectId = ((EbeguExistingAntragException) ex).getGesuchId();
		}

		Object[] args = ex.getArgs().toArray();
		String translatedEnumMessage = translateEnumValue(ex.getErrorCodeEnum(), localeFromHeader, mandant, args);
		EbeguExceptionReport exceptionReport = new EbeguExceptionReport(
			ex.getClass().getSimpleName(),
			ex.getErrorCodeEnum(),
			ex.getMethodName(),
			translatedEnumMessage,
			ex.getCustomMessage(),
			objectId,
			ex.getArgs());

		if (addDebugInfo) {
			addDevelopmentDebugInformation(exceptionReport, ex);
		}

		return builder.entity(exceptionReport).build();
	}

	private static void addDevelopmentDebugInformation(EbeguExceptionReport exceptionReport, Exception e) {
		exceptionReport.setStackTrace(ExceptionUtils.getStackTrace(e));
	}

	@Nonnull
	private static Response.ResponseBuilder setResponseHeaderAndStatus(Response.Status status) {
		Response.ResponseBuilder builder = Response.status(status);
		builder.header(Validation.VALIDATION_HEADER, "true");
		builder.type(MediaType.APPLICATION_JSON_TYPE);
		return builder;
	}
}

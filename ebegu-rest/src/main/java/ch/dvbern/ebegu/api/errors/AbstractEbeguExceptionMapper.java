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

package ch.dvbern.ebegu.api.errors;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.Constants;
import org.jboss.resteasy.api.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by imanol on 02.03.16.
 * Basis Exception Mapper
 *
 * @see <a href="https://samaxes.com/2014/04/jaxrs-beanvalidation-javaee7-wildfly/" >https://samaxes.com/2014/04/jaxrs-beanvalidation-javaee7-wildfly</a>
 */
public abstract class AbstractEbeguExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractEbeguExceptionMapper.class.getSimpleName());
	private static final String EXCEPTION_OCCURED = "Exception occured: ";

	@Context
	private HttpHeaders headers;

	@Inject
	@SuppressWarnings("checkstyle:VisibilityModifier")
	protected EbeguConfiguration configuration;

	@Inject
	@SuppressWarnings("checkstyle:VisibilityModifier")
	protected MandantService mandantService;

	protected Response buildResponse(Object entity, String mediaType, Response.Status status) {
		Response.ResponseBuilder builder = Response.status(status).entity(entity);
		builder.type(mediaType);
		builder.header(Validation.VALIDATION_HEADER, "true");
		return builder.build();
	}

	@Nullable
	protected abstract Response buildViolationReportResponse(E exception, Response.Status status);

	protected String unwrapException(Throwable t) {
		StringBuffer sb = new StringBuffer();
		doUnwrapException(sb, t);
		return sb.toString();
	}

	/**
	 * unwrapped alle causes und fuegt sie zum Stringbuffer hinzu
	 *
	 * @param sb buffer to append to
	 * @param t throwable
	 */
	private void doUnwrapException(StringBuffer sb, Throwable t) {
		if (t == null) {
			return;
		}
		sb.append(t.toString());
		if (t.getCause() != null && t != t.getCause()) {
			sb.append('[');
			doUnwrapException(sb, t.getCause());
			sb.append(']');
		}
	}

	/**
	 * @param accept Liste mit Accepted media types
	 * @return Gibt den ersten von uns unterstuetzten MediaType zurueck
	 */
	@Nullable
	protected MediaType getAcceptMediaType(List<MediaType> accept) {
		for (MediaType mt : accept) {
			if (MediaType.APPLICATION_JSON_TYPE.getType().equals(mt.getType())
				&& MediaType.APPLICATION_JSON_TYPE.getSubtype().equals(mt.getSubtype())) {
				return MediaType.APPLICATION_JSON_TYPE;
			}
			if (MediaType.APPLICATION_XML_TYPE.getType().equals(mt.getType())
				&& MediaType.APPLICATION_XML_TYPE.getSubtype().equals(mt.getSubtype())) {
				return MediaType.APPLICATION_XML_TYPE;
			}
		}
		LOG.debug("Minor Warning: AcceptedMediaType list for resourcecall does not contain xml or json types");
		return null;
	}

	@SuppressWarnings("PMD.EmptyIfStmt") // Wir wollen explizit NONE behandeln und WARN als default
	protected void logException(Exception exception) {
		// Falls es eine Exception von uns ist, und wir ein Level angegeben haben, loggen wir mit diesem
		// ansonsten defaultmässig WARN
		if (exception instanceof EbeguRuntimeException) {
			EbeguRuntimeException ebeguException = (EbeguRuntimeException) exception;
			KibonLogLevel logLevel = ebeguException.getLogLevel();
			if (logLevel == KibonLogLevel.ERROR) {
				LOG.error(EXCEPTION_OCCURED, exception);
			} else if (logLevel == KibonLogLevel.INFO) {
				LOG.info(EXCEPTION_OCCURED, exception);
			} else if (logLevel == KibonLogLevel.DEBUG) {
				LOG.debug(EXCEPTION_OCCURED, exception);
			} else if (logLevel == KibonLogLevel.NONE) {
				// ignore: Diesen Fehler wollen wir nicht loggen
			} else {
				LOG.warn(EXCEPTION_OCCURED, exception);
			}
		} else {
			LOG.warn(EXCEPTION_OCCURED, exception);
		}
	}

	protected Locale getLocaleFromHeader() {
		if (!headers.getAcceptableLanguages().isEmpty()) {
			return headers.getAcceptableLanguages().get(0);
		}
		return Constants.DEFAULT_LOCALE;
	}
}

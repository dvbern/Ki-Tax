/*
 * Copyright (C)  2020 DV Bern AG, Switzerland
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
 *
 */
package ch.dvbern.ebegu.ws.ewk;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import ch.bedag.geres.schemas._20180101.geresechtypes.TestResponse;
import ch.bedag.geres.schemas._20180101.geresresidentinforesponse.BaseDeliveryType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InfrastructureFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InvalidArgumentsFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.PermissionDeniedFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoParametersType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.cdi.Geres;
import ch.dvbern.ebegu.cdi.Prod;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.PersonensucheAuditLog;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.services.PersonenSucheAuditLogService;
import ch.dvbern.ebegu.ws.ewk.sts.WSSSecurityGeresAssertionOutboundHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zum aufrufen des WebService Batch-STS welcher eine SAML Assertion fuer den
 * Batchuser der durch den mitgeschickten privateKey identifiziert wird abholt
 */
@Dependent
@Geres
@Prod
public class GeresWebService implements IGeresWebService {

	private static final String TARGET_NAME_SPACE = "http://geres.bedag.ch/schemas/20180101/GeresResidentInfoService";
	private static final String SERVICE_NAME = "GeresResidentInfoService";

	private static final Logger LOGGER = LoggerFactory.getLogger(GeresWebService.class.getSimpleName());
	public static final String METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT = "initGeresResidentInfoServicePort";

	private static final Integer SEARCH_MAX = 100;


	@Inject
	private EbeguConfiguration config;

	@Inject
	private WSSSecurityGeresAssertionOutboundHandler wssUsernameTokenSecurityHandler;

	@Inject
	private PersonenSucheAuditLogService personenSucheAuditLogService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	private ResidentInfoPortType port;

	@Override
	public String test() throws PersonenSucheServiceException {
		try {

			final TestResponse test = getService().test();
			return test.getTestResponse();
		} catch (InfrastructureFault infrastructureFault) {
			String msg = createInfrastructureFaultLogmessage("GeresWebService#test", infrastructureFault);
			LOGGER.error(msg);
			throw new PersonenSucheServiceException("test", msg, infrastructureFault);
		} catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createPermissionDeniedFaultLogmessage("GeresWebService#test", permissionDeniedFault);
			LOGGER.error(msg);
			throw new PersonenSucheServiceException("test", msg, permissionDeniedFault);
		}

	}

	@Override
	public EWKResultat residentInfoFull(ResidentInfoParametersType residentInfoParameters, LocalDate validityDate, Integer searchMax) throws PersonenSucheServiceException {
		final String methodName = "GeresWebService#residentInfoFull";
		LocalDateTime startDate = LocalDateTime.now();
		BaseDeliveryType baseDeliveryType = null;
		Exception exceptionReceived = null;

		try {

			baseDeliveryType = getService().residentInfoFull(residentInfoParameters, validityDate, searchMax);
			final EWKResultat ewkResultat = GeresConverter.convertFromGeresFullResult(baseDeliveryType);
			return ewkResultat;
		} catch (InvalidArgumentsFault invalidArgumentsFault) {
			String msg = createInvalidArgumentFaultLogmessage(methodName, invalidArgumentsFault);
			LOGGER.error(msg);
			exceptionReceived = invalidArgumentsFault;
			throw new PersonenSucheServiceException(methodName, msg, invalidArgumentsFault);
		} catch (InfrastructureFault infrastructureFault) {
			String msg = createInfrastructureFaultLogmessage(methodName, infrastructureFault);
			LOGGER.error(msg);
			exceptionReceived = infrastructureFault;
			throw new PersonenSucheServiceException(methodName, msg, infrastructureFault);
		} catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createPermissionDeniedFaultLogmessage(methodName, permissionDeniedFault);
			exceptionReceived = permissionDeniedFault;
			LOGGER.error(msg);
			throw new PersonenSucheServiceException(methodName, msg, permissionDeniedFault);
		} finally {
			writeAuditLogForGeresCall(methodName, residentInfoParameters, validityDate, startDate, exceptionReceived, baseDeliveryType, null);
		}

	}


	@Override
	public EWKResultat residentInfoFast(ResidentInfoParametersType residentInfoParameters, LocalDate validityDate, Integer searchMax) throws PersonenSucheServiceException {
		final String methodName = "GeresWebService#residentInfoFast";
		LocalDateTime startDate = LocalDateTime.now();
		BaseDeliveryType baseDeliveryType = null;
		Integer totalResultNum = null;
		Exception exceptionReceived = null;

		try {
			Holder<Integer> totalNumberOfResults = new Holder<>();
			Holder<Integer> numberOfDeliveredResults = new Holder<>();

			Holder<BaseDeliveryType> baseDelivery = new Holder<>();

			getService().residentInfoFast(residentInfoParameters, validityDate, searchMax, totalNumberOfResults, numberOfDeliveredResults, baseDelivery);
			baseDeliveryType = baseDelivery.value;
			totalResultNum = totalNumberOfResults.value;

			EWKResultat result = GeresConverter.convertFromGeresFastResult(totalNumberOfResults.value, numberOfDeliveredResults.value, baseDeliveryType);
			return result;
		} catch (InvalidArgumentsFault invalidArgumentsFault) {
			String msg = createInvalidArgumentFaultLogmessage(methodName, invalidArgumentsFault);
			exceptionReceived = invalidArgumentsFault;
			LOGGER.error(msg);
			throw new PersonenSucheServiceException(methodName, msg, invalidArgumentsFault);

		} catch (InfrastructureFault infrastructureFault) {
			String msg = createInfrastructureFaultLogmessage(methodName, infrastructureFault);
			exceptionReceived = infrastructureFault;
			LOGGER.error(msg);
			throw new PersonenSucheServiceException(methodName, msg, infrastructureFault);
		} catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createPermissionDeniedFaultLogmessage(methodName, permissionDeniedFault);
			exceptionReceived = permissionDeniedFault;
			LOGGER.error(msg);
			throw new PersonenSucheServiceException(methodName, msg, permissionDeniedFault);
		} finally {
			writeAuditLogForGeresCall(methodName, residentInfoParameters, validityDate, startDate,  exceptionReceived, baseDeliveryType, totalResultNum);
		}

	}

	private String createInvalidArgumentFaultLogmessage(String methodName, InvalidArgumentsFault invalidArgumentsFault) {
		return String.format("Call to %s failed with invalidArgument Fault '%s', user-message '%s', technical-message '%s', error-code: '%s'",
			methodName,
			invalidArgumentsFault.getMessage(),
			invalidArgumentsFault.getFaultInfo().getUserMessage(),
			invalidArgumentsFault.getFaultInfo().getTechnicalMessage(),
			invalidArgumentsFault.getFaultInfo().getErrorCode());

	}

	private String createInfrastructureFaultLogmessage(String methodName, InfrastructureFault infrastructureFault) {
		return String.format("Call to %s failed with infrastructure Fault '%s', user-message '%s', technical-message '%s', error-code: '%s'",
			methodName,
			infrastructureFault.getMessage(),
			infrastructureFault.getFaultInfo().getUserMessage(),
			infrastructureFault.getFaultInfo().getTechnicalMessage(),
			infrastructureFault.getFaultInfo().getErrorCode());
	}

	private String createPermissionDeniedFaultLogmessage(String methodName, PermissionDeniedFault permissionDeniedFault) {
		return String.format("Call to %s failed with permissionDenied Fault '%s', user-message '%s', technical-message '%s', error-code: '%s'",
			methodName,
			permissionDeniedFault.getMessage(),
			permissionDeniedFault.getFaultInfo().getUserMessage(),
			permissionDeniedFault.getFaultInfo().getTechnicalMessage(),
			permissionDeniedFault.getFaultInfo().getErrorCode());
	}

	private void writeAuditLogForGeresCall(String methodName, ResidentInfoParametersType residentInfoParameters, LocalDate validityDate,
		LocalDateTime startTime, @Nullable Exception exceptionReceived, @Nullable BaseDeliveryType baseDeliveryType, Integer totanNumRes) {

		PersonensucheAuditLog personensucheAuditLogEntry = new PersonensucheAuditLog(
			methodName,
			convertSearchParamForLog(residentInfoParameters),
			validityDate,
			exceptionReceived != null ?  exceptionReceived.getClass().getSimpleName() : null,
			totanNumRes != null ? Long.valueOf(totanNumRes): null,
			baseDeliveryType != null ? baseDeliveryType.getNumberOfMessages().longValue() : null,
			startTime,
			LocalDateTime.now()
		);
		final PersonensucheAuditLog logEntry = personenSucheAuditLogService.savePersonenSucheAuditLog(personensucheAuditLogEntry);
		LOGGER.trace("Webservice call to Gers complete: Auditinfo {}", logEntry);
	}

	private String convertSearchParamForLog(ResidentInfoParametersType residentInfoParameters) {

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(residentInfoParameters);
		} catch (JsonProcessingException e) {
			return residentInfoParameters.getFirstName() + ' ' + DateTimeFormatter.ISO_DATE.format(residentInfoParameters.getDateOfBirth());
		}


	}


	/**
	 * initialisiert den Service Port wenn noetig oder gibt ihn zurueck.
	 *
	 * @throws PersonenSucheServiceException, if the service cannot be initialised
	 */
	private ResidentInfoPortType getService() throws PersonenSucheServiceException {
		if (port == null) {
			initGeresResidentInfoServicePort();
		}
		return port;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void initGeresResidentInfoServicePort() throws PersonenSucheServiceException {
		LOGGER.info("Initialising GeresResidentInfoService:");
		if (port == null) {
			String endpointURL = config.getEbeguPersonensucheGERESEndpoint();
			String wsdlURL = config.getEbeguPersonensucheGERESWsdl();
			if (StringUtils.isEmpty(endpointURL)) {
				throw new PersonenSucheServiceException(METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT, "Es wurde keine Endpunkt URL definiert fuer den "
					+ "GeresResidentInfoService");
			}

			LOGGER.info("GeresResidentInfoService Endpoint: {}", endpointURL);

			URL url = null;
			if (wsdlURL != null) {
				try {
					// Test der neu mitgeteilten WSDL-URL:
					url = new URL(wsdlURL);
					LOGGER.info("GeresResidentInfoService WSDL: {}", url);
					Object content = url.getContent();
					LOGGER.info("GeresResidentInfoService WSDL-Content: {}", content);
				} catch (IOException e) {
					url = null;
					LOGGER.error("GeresResidentInfoService WSDL not found at url : " + wsdlURL, e);
				}
			}

			try {
				if (url == null) {
					// WSDL url wurde nicht  mitgeliefert. Die EndpointURL?wsdl geht also nicht und wir nehmen ein fixes.
					url = GeresWebService.class.getResource("/wsdl/geres/GeresResidentInfo_v1801.wsdl");
					Objects.requireNonNull(url, "WSDL konnte unter der angegebenen URI nicht gefunden werden. Kann Service-Port nicht erstellen");
					LOGGER.info("GeresResidentInfo_v1801 WSDL URL: {}", url);
				}
				LOGGER.info("GeresResidentInfoService TargetNameSpace: " + TARGET_NAME_SPACE);
				LOGGER.info("GeresResidentInfoService ServiceName: " + SERVICE_NAME);
				final QName qname = new QName(TARGET_NAME_SPACE, SERVICE_NAME);
				LOGGER.info("GeresResidentInfoService QName: {}", qname);
				final Service service = Service.create(url, qname);
				service.setHandlerResolver(portInfo -> Collections.singletonList(wssUsernameTokenSecurityHandler)); // handler that adds assertion to header
				LOGGER.info("GeresResidentInfoService created: {}", service);
				port = service.getPort(ResidentInfoPortType.class);
				LOGGER.info("ResidentInfoPortType Port created: {}", port);
				final BindingProvider bp = (BindingProvider) port;

				bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

			} catch (RuntimeException e) {
				port = null;
				throw new PersonenSucheServiceException(METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT,
					"Could not create service-port GeresResidentInfoService for endpoint " + endpointURL, e);
			}
		}
		LOGGER.info("GeresResidentInfoService erfolgreich initialisiert");
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, long bfsNummer) throws PersonenSucheServiceException {
		EWKResultat ewkResultat = suchePerson(name, vorname, geburtsdatum, geschlecht, bfsNummer);
		if (ewkResultat.getPersonen().isEmpty()) {
			ewkResultat = suchePerson(name, null, geburtsdatum, geschlecht, bfsNummer);
		}
		return ewkResultat;
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException {
		EWKResultat ewkResultat = suchePerson(name, vorname, geburtsdatum, geschlecht, null);
		if (ewkResultat.getPersonen().isEmpty()) {
			ewkResultat = suchePerson(name, null, geburtsdatum, geschlecht, null);
		}
		return ewkResultat;
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonenInHaushalt(Long wohnungsId, Long gebaeudeId) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		ResidentInfoParametersType parameters = new ResidentInfoParametersType();
		parameters.setEgid(gebaeudeId);
		parameters.setEwid(wohnungsId);
		LocalDate validityDate = LocalDate.now();
		final EWKResultat ewkResultat = this.residentInfoFull(parameters, validityDate, SEARCH_MAX);
		return ewkResultat;
	}

	@Nonnull
	private EWKResultat suchePerson(@Nonnull String name, String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, Long bfsNummer) throws PersonenSucheServiceException {
		ResidentInfoParametersType parameters = new ResidentInfoParametersType();
		if (vorname != null) {
			parameters.setFirstName(vorname);
		}
		if (bfsNummer != null) {
			parameters.setMunicipalityNumber(bfsNummer.intValue());
		}
		parameters.setOfficialName(name);
		parameters.setDateOfBirth(geburtsdatum);
		parameters.setSex(Geschlecht.MAENNLICH == geschlecht ? 1 : 2);
		LocalDate validityDate = LocalDate.now();
		final EWKResultat ewkResultat = this.residentInfoFull(parameters, validityDate, SEARCH_MAX);
		return ewkResultat;
	}
}

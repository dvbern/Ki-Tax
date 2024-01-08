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

package ch.dvbern.ebegu.ws.neskovanp;

import ch.be.fin.sv.schemas.base._20070131.exceptioninfo.FaultBase;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.*;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.SteuerdatenAnfrageLog;
import ch.dvbern.ebegu.entities.SteuerdatenRequest;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.services.SteuerdatenAnfrageLogService;
import ch.dvbern.ebegu.ws.neskovanp.oicd.OIDCTokenManagerBean;
import ch.dvbern.ebegu.ws.oicd.OIDCToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Dependent
public class KibonAnfrageWebService implements IKibonAnfrageWebService {

	private static final String TARGET_NAME_SPACE = "http://sv.fin.be.ch/schemas/NESKOVANP/20211119/KiBonAnfrageService";
	private static final String SERVICE_NAME = "KiBonAnfrageService";

	private static final Logger LOGGER = LoggerFactory.getLogger(KibonAnfrageWebService.class.getSimpleName());

	@Inject
	private OIDCTokenManagerBean OIDCTokenManagerBean;

	@Inject
	private EbeguConfiguration config;

	private KiBonAnfragePort port;

	@Inject
	private SteuerdatenAnfrageLogService steuerdatenAnfrageLogService;

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Override
	public SteuerdatenResponse getSteuerDaten(
		Integer zpvNummer,
		LocalDate geburtsdatum,
		String gesuchId,
		Integer gesuchsperiodeBeginnJahr) throws KiBonAnfrageServiceException {
		final String methodName = "KibonAnfrageService#getSteuerdaten";
		SteuerdatenResponse steuerdatenResponse = null;
		Exception exceptionReceived = null;
		LocalDateTime startDate = LocalDateTime.now();

		try {
			SteuerDatenResponseType steuerDatenResponseType = getServicePort().getSteuerdaten(zpvNummer, geburtsdatum,
					gesuchId, gesuchsperiodeBeginnJahr);
			steuerdatenResponse = KibonAnfrageConverter.convertFromKibonAnfrage(steuerDatenResponseType);
			return steuerdatenResponse;
		}
		catch(BusinessFault businessFault) {
			String msg = createFaultLogmessage("BusinessFault" ,methodName, businessFault.getMessage(), businessFault.getFaultInfo());
			LOGGER.debug(msg);
			exceptionReceived = businessFault;
			throw new KiBonAnfrageServiceException(methodName, msg, businessFault.getFaultInfo().getErrorCode(), businessFault.getFaultInfo().getUserMessage());
		}
		catch(InfrastructureFault infrastructureFault) {
			String msg = createFaultLogmessage("InfrastructureFault" ,methodName, infrastructureFault.getMessage(), infrastructureFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = infrastructureFault;
			throw new KiBonAnfrageServiceException(methodName, msg, infrastructureFault.getFaultInfo().getErrorCode(), infrastructureFault.getFaultInfo().getUserMessage());
		}
		catch (InvalidArgumentsFault invalidArgumentsFault) {
			String msg = createFaultLogmessage("InvalidArgumentsFault" ,methodName, invalidArgumentsFault.getMessage(), invalidArgumentsFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = invalidArgumentsFault;
			throw new KiBonAnfrageServiceException(methodName, msg, invalidArgumentsFault.getFaultInfo().getErrorCode(), invalidArgumentsFault.getFaultInfo().getUserMessage());
		}
		catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createFaultLogmessage("PermissionDeniedFault" ,methodName, permissionDeniedFault.getMessage(), permissionDeniedFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = permissionDeniedFault;
			throw new KiBonAnfrageServiceException(methodName, msg, permissionDeniedFault.getFaultInfo().getErrorCode(), permissionDeniedFault.getFaultInfo().getUserMessage());
		}
		catch (Exception e) {
			exceptionReceived = e;
			LOGGER.error(e.getMessage());
			throw new KiBonAnfrageServiceException(methodName, "Einen unerwartete Fehler ist aufgetretten", e);
		}
		finally {
			writeAuditLogForKibonAnfrageCall(zpvNummer, geburtsdatum,
					gesuchId, gesuchsperiodeBeginnJahr, startDate, steuerdatenResponse, exceptionReceived);
		}
	}

	private void writeAuditLogForKibonAnfrageCall(
		Integer zpvNummer,
		LocalDate geburtsdatum,
		String kibonAntragId,
		Integer gesuchsperiodeBeginnJahr,
		LocalDateTime startDate,
		SteuerdatenResponse steuerDatenResponse,
		@Nullable Exception exceptionReceived) {

		SteuerdatenRequest request =
			new SteuerdatenRequest(zpvNummer, geburtsdatum, kibonAntragId, gesuchsperiodeBeginnJahr);
		SteuerdatenAnfrageStatus status =
			exceptionReceived == null && steuerDatenResponse.getVeranlagungsstand() != null ?
				SteuerdatenAnfrageStatus.valueOf(steuerDatenResponse.getVeranlagungsstand().name()) :
				SteuerdatenAnfrageStatus.FAILED;
		String faultReceived = exceptionReceived != null ? exceptionReceived.getMessage() : null;
		SteuerdatenAnfrageLog anfrageLog =
			new SteuerdatenAnfrageLog(startDate, status, faultReceived, request, steuerDatenResponse);
		steuerdatenAnfrageLogService.saveSteuerdatenAnfrageLog(anfrageLog);
	}

	private KiBonAnfragePort getServicePort() throws KiBonAnfrageServiceException {
		if (port == null) {
			initKiBonAnfragePort();
		}
		initAuthorizationForKibonAnfrageService();
		return port;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void initKiBonAnfragePort() throws KiBonAnfrageServiceException {
		LOGGER.info("Initialising GeresResidentInfoService:");
		if (port == null) {
			String endpointURL = config.getKibonAnfrageEndpoint();
			if (StringUtils.isEmpty(endpointURL)) {
				throw new KiBonAnfrageServiceException("initKiBonAnfragePort", "Es wurde keine Endpunkt URL definiert fuer den "
					+ "KibonAnfrageService");
			}

			LOGGER.info("KibonAnfrageService Endpoint: {}", endpointURL);

			try {
				URL url = KibonAnfrageWebService.class.getResource("/wsdl/neskovanp/kibonanfrage/KiBonAnfrageService.wsdl");
				Objects.requireNonNull(url, "WSDL konnte unter der angegebenen URI nicht gefunden werden. Kann Service-Port nicht erstellen");
				LOGGER.info("KiBonAnfrageService WSDL URL: {}", url);

				LOGGER.info("KibonAnfrageService TargetNameSpace: " + TARGET_NAME_SPACE);
				LOGGER.info("KibonAnfrageService ServiceName: " + SERVICE_NAME);
				final QName qname = new QName(TARGET_NAME_SPACE, SERVICE_NAME);
				LOGGER.info("KibonAnfrageService QName: {}", qname);
				final Service service = Service.create(url, qname);
				LOGGER.info("KibonAnfrageService created: {}", service);
				port = service.getPort(KiBonAnfragePort.class);
				LOGGER.info("KibonAnfrageService Port created: {}", port);
				final BindingProvider bp = (BindingProvider) port;
				bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			} catch (RuntimeException e) {
				port = null;
				throw new KiBonAnfrageServiceException("initKiBonAnfragePort",
					"Could not create service-port KibonAnfrageService for endpoint " + endpointURL, e);
			}
		}
		LOGGER.info("KibonAnfrageService erfolgreich initialisiert");
	}

	private void initAuthorizationForKibonAnfrageService() throws KiBonAnfrageServiceException {
		try {
			OIDCToken authToken = OIDCTokenManagerBean.getValidOICDToken();
			Map<String, List<String>> requestHeaders = new HashMap<>();
			requestHeaders.put(HttpHeaders.AUTHORIZATION, Collections.singletonList(authToken.getAuthToken()));

			final BindingProvider bp = (BindingProvider) port;
			bp.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
		} catch (Exception e) {
			port = null;
			LOGGER.error("Could not initialze the Autorziation Token for KibonAnfrage Serivce", e);
			throw new KiBonAnfrageServiceException(
				"initAuthorizationForKibonAnfrageService",
				"Could not initialze the Autorziation Token for KibonAnfrage Serivce",
				e);
		}
	}


	private String createFaultLogmessage(String exceptionName, String methodName, String message, FaultBase fault) {
		return String.format("Call to %s failed with %s Fault '%s', user-message '%s', technical-message '%s', error-code: '%s'",
			methodName,
			exceptionName,
			message,
			fault.getUserMessage(),
			fault.getTechnicalMessage(),
			fault.getErrorCode());

	}
}

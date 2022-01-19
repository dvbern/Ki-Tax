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

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import ch.be.fin.sv.schemas.base._20070131.exceptioninfo.FaultBase;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.BusinessFault;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.InfrastructureFault;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.InvalidArgumentsFault;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.KiBonAnfragePort;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.PermissionDeniedFault;
import ch.be.fin.sv.schemas.neskovanp._20211119.kibonanfrageservice.SteuerDatenResponseType;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.SteuerdatenAnfrageLog;
import ch.dvbern.ebegu.entities.SteuerdatenRequest;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.services.SteuerdatenAnfrageLogService;
import ch.dvbern.ebegu.ws.neskovanp.sts.WSSSecurityKibonAnfrageAssertionOutboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class KibonAnfrageWebService implements IKibonAnfrageWebService {

	private static final String TARGET_NAME_SPACE = "http://sv.fin.be.ch/schemas/NESKOVANP/20211119/KiBonAnfrageService";
	private static final String SERVICE_NAME = "KiBonAnfrageService";

	private static final Logger LOGGER = LoggerFactory.getLogger(KibonAnfrageWebService.class.getSimpleName());

	@Inject
	private WSSSecurityKibonAnfrageAssertionOutboundHandler wssUsernameTokenSecurityHandler;

	@Inject
	private EbeguConfiguration config;

	private KiBonAnfragePort port;

	@Inject
	private SteuerdatenAnfrageLogService steuerdatenAnfrageLogService;

	@Override
	public SteuerdatenResponse getSteuerDaten(
		Integer zpvNummer,
		LocalDate geburtsdatum,
		String kibonAntragId,
		Integer gesuchsperiodeBeginnJahr) throws KiBonAnfrageServiceException {
		final String methodName = "KibonAnfrageService#getSteuerdaten";
		SteuerdatenResponse steuerdatenResponse = null;
		Exception exceptionReceived = null;
		LocalDateTime startDate = LocalDateTime.now();

		try {
			SteuerDatenResponseType steuerDatenResponseType = getServicePort().getSteuerdaten(zpvNummer, geburtsdatum, kibonAntragId, gesuchsperiodeBeginnJahr);
			steuerdatenResponse = KibonAnfrageConverter.convertFromKibonAnfrage(steuerDatenResponseType);
			return steuerdatenResponse;
		}
		catch(BusinessFault businessFault) {
			String msg = createFaultLogmessage("BusinessFault" ,methodName, businessFault.getMessage(), businessFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = businessFault;
			throw new KiBonAnfrageServiceException(methodName, msg, businessFault);
		}
		catch(InfrastructureFault infrastructureFault) {
			String msg = createFaultLogmessage("InfrastructureFault" ,methodName, infrastructureFault.getMessage(), infrastructureFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = infrastructureFault;
			throw new KiBonAnfrageServiceException(methodName, msg, infrastructureFault);
		}
		catch (InvalidArgumentsFault invalidArgumentsFault) {
			String msg = createFaultLogmessage("InvalidArgumentsFault" ,methodName, invalidArgumentsFault.getMessage(), invalidArgumentsFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = invalidArgumentsFault;
			throw new KiBonAnfrageServiceException(methodName, msg, invalidArgumentsFault);
		}
		catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createFaultLogmessage("PermissionDeniedFault" ,methodName, permissionDeniedFault.getMessage(), permissionDeniedFault.getFaultInfo());
			LOGGER.error(msg);
			exceptionReceived = permissionDeniedFault;
			throw new KiBonAnfrageServiceException(methodName, msg, permissionDeniedFault);
		}
		catch (Exception e) {
			exceptionReceived = e;
			throw e;
		}
		finally {
			writeAuditLogForKibonAnfrageCall(zpvNummer, geburtsdatum, kibonAntragId, gesuchsperiodeBeginnJahr, startDate, steuerdatenResponse, exceptionReceived);
		}
	}

	private void writeAuditLogForKibonAnfrageCall(
		Integer zpvNummer,
		LocalDate geburtsdatum,
		String kibonAntragId,
		Integer gesuchsperiodeBeginnJahr,
		LocalDateTime startDate,
		SteuerdatenResponse steuerDatenResponse,
		@Nullable  Exception exceptionReceived) {

		SteuerdatenRequest request = new SteuerdatenRequest(zpvNummer, geburtsdatum, kibonAntragId, gesuchsperiodeBeginnJahr);
		SteuerdatenAnfrageStatus status = exceptionReceived == null ? SteuerdatenAnfrageStatus.SUCCESS : SteuerdatenAnfrageStatus.FAILED;
		String faultReceived = exceptionReceived != null ? exceptionReceived.getMessage() : null;
		SteuerdatenAnfrageLog anfrageLog = new SteuerdatenAnfrageLog(startDate, status, faultReceived, request, steuerDatenResponse);
		steuerdatenAnfrageLogService.saveSteuerdatenAnfrageLog(anfrageLog);
	}

	private KiBonAnfragePort getServicePort() throws KiBonAnfrageServiceException {
		if(port == null){
			initKiBonAnfragePort();
		}
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
				service.setHandlerResolver(portInfo -> Collections.singletonList(wssUsernameTokenSecurityHandler)); // handler that adds assertion to header, we need to check how it need to be apadted for this interface
				//Ich hoffe eigentlich das die STS Server fur kiBonAnfrage ist gleich als die von Geres, wenn nicht muss man die STS Spezifikation anschauen und adaptieren der Handler entspechend
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

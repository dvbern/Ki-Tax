/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
abstract class AbstractGeresPortFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGeresPortFactory.class.getSimpleName());

	public static final String METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT = "initGeresResidentInfoServicePort";
	private static final String TARGET_NAME_SPACE = "http://geres.bedag.ch/schemas/20180101/GeresResidentInfoService";
	private static final String SERVICE_NAME = "GeresResidentInfoService";

	@Getter
	private final EbeguConfiguration config;

	public ResidentInfoPortType getPort() throws PersonenSucheServiceException {
		LOGGER.info("Initialising GeresResidentInfoService:");
		String endpointURL = getEndpointURL();

		try {
			LOGGER.info("GeresResidentInfoService Endpoint: {}", endpointURL);
			var url = getWsdlUrl();
			LOGGER.info("GeresResidentInfoService TargetNameSpace: " + TARGET_NAME_SPACE);
			LOGGER.info("GeresResidentInfoService ServiceName: " + SERVICE_NAME);
			final QName qname = new QName(TARGET_NAME_SPACE, SERVICE_NAME);
			LOGGER.info("GeresResidentInfoService QName: {}", qname);
			final Service service = Service.create(url, qname);
			customizeService(service);

			LOGGER.info("GeresResidentInfoService created: {}", service);
			var port = service.getPort(ResidentInfoPortType.class);

			LOGGER.info("ResidentInfoPortType Port created: {}", port);
			final BindingProvider bp = (BindingProvider) port;
			bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
			customizeRequestContext(bp.getRequestContext());

			LOGGER.info("GeresResidentInfoService erfolgreich initialisiert");
			return port;
		} catch (RuntimeException e) {
			throw new PersonenSucheServiceException(METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT,
				"Could not create service-port GeresResidentInfoService for endpoint " + endpointURL, e);
		}

	}

	private String getEndpointURL() throws PersonenSucheServiceException {
		String endpointURL = getGeresUrl();
		if (StringUtils.isEmpty(endpointURL)) {
			throw new PersonenSucheServiceException(
				METHOD_NAME_INIT_GERES_WEB_SERVICE_PORT,
				"Es wurde keine Endpunkt URL definiert fuer den GeresResidentInfoService");
		}
		return endpointURL;
	}

	private URL getWsdlUrl() {
		String wsdlURL = getConfig().getEbeguPersonensucheGERESWsdl();

		if (wsdlURL != null) {
			try {
				LOGGER.info("GeresResidentInfoService WSDL: {}", wsdlURL);
				URL urlFromConfig = new URL(wsdlURL);
				Object content = urlFromConfig.getContent();
				LOGGER.debug("GeresResidentInfoService WSDL-Content: {}", content);
				return urlFromConfig;
			} catch (IOException e) {
				LOGGER.warn("Geres WSDL from config not found at: {}", wsdlURL);
			}
		}

		var defaultUrl = AbstractGeresPortFactory.class.getResource("/wsdl/geres/GeresResidentInfo_v1801.wsdl");
		LOGGER.info("Using default Geres WSDL at {}", defaultUrl);

		return Objects.requireNonNull(
			defaultUrl,
			"WSDL konnte unter der angegebenen URI nicht gefunden werden. Kann Service-Port nicht erstellen");
	}

	protected abstract String getGeresUrl();

	protected abstract void customizeService(Service service);

	protected abstract void customizeRequestContext(Map<String, Object> requestContext);
}

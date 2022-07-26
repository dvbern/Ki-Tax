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
package ch.dvbern.ebegu.ws.sts;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import ch.be.fin.sv.schemas.a7s.securityservice._20071010.zertstsservice.AuthenticationFault;
import ch.be.fin.sv.schemas.a7s.securityservice._20071010.zertstsservice.BusinessFault;
import ch.be.fin.sv.schemas.a7s.securityservice._20071010.zertstsservice.ZertSTSService;
import ch.dvbern.ebegu.config.EbeguConfigurationImpl;
import ch.dvbern.ebegu.errors.STSZertifikatServiceException;
import ch.dvbern.ebegu.ws.ewk.sts.WSSSecurityGeresAssertionExtractionHandler;
import ch.dvbern.ebegu.ws.neskovanp.sts.WSSSecurityKibonAnfrageAssertionExtractionHandler;
import oasis.names.tc.saml._1_0.assertion.AssertionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zum aufrufen des WebService Batch-STS welcher eine SAML Assertion fuer den
 * Batchuser der durch den mitgeschickten privateKey identifiziert wird abholt
 */
@Dependent
public class STSWebService {

	private static final String TARGET_NAME_SPACE =
		"http://sv.fin.be.ch/schemas/A7S/securityService/20071010/ZertSTSService";
	private static final String SERVICE_NAME = "ZertSTSWebService";

	private static final Logger LOGGER = LoggerFactory.getLogger(STSWebService.class.getSimpleName());
	public static final String METHOD_NAME_INIT_STS_WEB_SERVICE_PORT = "initSTSWebServicePort";

	/**
	 * Fixed text expected as the prefix and algorithm of the signed security test string.
	 */
	public static final String SECURITY_PREFIX_FOR_SIGNATURE = "ZertSTSRequest";
	public static final String DATE_SIGNATURE_PATTERN = "yyyy.MM.dd HH:mm:ss";
		// the Signature MUST be constructed using this pattern

	@Inject
	private STSConfigManager stsConfigManager;

	@Inject
	private WSSSecurityGeresAssertionExtractionHandler wssSecurityGeresAssertionExtractionHandler;

	@Inject
	private WSSSecurityKibonAnfrageAssertionExtractionHandler wssSecurityNeskoAssertionExtractionHandler;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	private ZertSTSService port;
	private PrivateKey privateKey = null;

	public STSWebServiceResult getSamlAssertionForBatchuser(WebserviceType webserviceType)
		throws STSZertifikatServiceException {
		LocalDateTime requestTime = LocalDateTime.now();

		Holder<String> renewalTokenHolder = new Holder<>();
		Holder<AssertionType> assertionHolder = new Holder<>();
		String applicationName = stsConfigManager.getEbeguSTSPrivateKeyAlias(webserviceType);
		byte[] signature = getSignatureValue(applicationName, requestTime, webserviceType);

		try {
			// call webservice
			getService(webserviceType).issueAssertion(
				applicationName,
				requestTime,
				//request Time is passed along in the soap request. Will be converted to a DateString in UTC TimeZone
				signature,
				applicationName,
				renewalTokenHolder,
				assertionHolder
			);

		} catch (AuthenticationFault | BusinessFault fault) {
			throw new STSZertifikatServiceException("getSamlAssertionForBatchuser",
				"Could not get a Saml Assertion from STS because of " + fault.getMessage(), fault);
		}

		return new STSWebServiceResult(assertionHolder.value, renewalTokenHolder.value);

	}

	private byte[] getSignatureValue(String applicationName, LocalDateTime requestTime, WebserviceType webserviceType)
		throws STSZertifikatServiceException {

		PrivateKey privateSTSKey = getSTSPrivateKeyLazy(webserviceType);

		STSWebServiceSignatureGenerator sigGenerator =
			new STSWebServiceSignatureGenerator(SECURITY_PREFIX_FOR_SIGNATURE,
				DATE_SIGNATURE_PATTERN, privateSTSKey);
		try {
			return sigGenerator.getSignatureValue(sigGenerator.getRequestProof(applicationName, requestTime));
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new STSZertifikatServiceException(
				"getSignatureValue",
				"Could not sign message for STS-Webservice call",
				e);
		}
	}

	private PrivateKey getSTSPrivateKeyLazy(WebserviceType webserviceType) throws STSZertifikatServiceException {
		if (this.privateKey == null) {
			this.privateKey = loadSTSPrivateKeyFromFile(webserviceType);

		}
		return this.privateKey;
	}

	private PrivateKey loadSTSPrivateKeyFromFile(WebserviceType webserviceType) throws STSZertifikatServiceException {
		String keyStorePW = stsConfigManager.getEbeguSTSKeystorePW(webserviceType);
		if (keyStorePW == null) {
			LOGGER.error(
				"Password for STS KeyStore was not set. Please set it using the {} property ",
				webserviceType.equals(WebserviceType.GERES) ?
					EbeguConfigurationImpl.EBEGU_PERSONENSUCHE_STS_KEYSTORE_PW :
					EbeguConfigurationImpl.EBEGU_KIBON_ANFRAGE_STS_KEYSTORE_PW);
		}
		final KeyStore keyStore =
			readKeystoreFromFile(stsConfigManager.getEbeguSTSKeystorePath(webserviceType), keyStorePW);

		try {
			final String pkAlias = stsConfigManager.getEbeguSTSPrivateKeyAlias(webserviceType);
			final boolean pkExists = keyStore.entryInstanceOf(pkAlias, PrivateKeyEntry.class);
			if (!pkExists) {
				String msg = String.format("keystore does not contain privateKey entry for alias %s", pkAlias);
				LOGGER.error(msg);
				throw new IllegalArgumentException(msg);
			}

			return (PrivateKey) keyStore.getKey(
				pkAlias,
				stsConfigManager.getEbeguSTSPrivateKeyPW(webserviceType).toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			throw new STSZertifikatServiceException(
				"getSTSPrivateKeyLazy",
				"Problem beim lesen des PrivateKey fuer den STS Webservice zur GERES Abfrage",
				e);
		}
	}

	private static KeyStore readKeystoreFromFile(String pathToKeyStore, String keyStorePassword)
		throws STSZertifikatServiceException {
		InputStream inputStream = null;
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			if (!Files.exists(Paths.get(pathToKeyStore))) {
				LOGGER.warn(
					"Keystore for GERES seems does not exists, did you set the relevant System Property correctly? "
						+ "ebegu.personensuche.sts.keystore.path ");
			}
			inputStream = Files.newInputStream(Paths.get(pathToKeyStore));
			keystore.load(inputStream, keyStorePassword.toCharArray());
			inputStream.close();
			return keystore;
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | RuntimeException e) {
			throw new STSZertifikatServiceException(
				"readKeyStoreFromFile",
				"Something went wrong reading keystore from " + pathToKeyStore,
				e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignore) {
				}
			}
		}
	}

	/**
	 * initialisiert den Service Port wenn noetig oder gibt ihn zurueck.
	 *
	 * @throws STSZertifikatServiceException, if the service cannot be initialised
	 */
	private ZertSTSService getService(WebserviceType webserviceType) throws STSZertifikatServiceException {
		if (port == null) {
			initSTSWebServicePort(webserviceType);
		}
		return port;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void initSTSWebServicePort(WebserviceType webserviceType) throws STSZertifikatServiceException {
		LOGGER.info("Initialising ZertSTSService:");
		if (port == null) {
			String endpointURL = stsConfigManager.getEbeguSTSEndpoint(webserviceType);
			String wsdlURL = stsConfigManager.getEbeguSTSWsdl(webserviceType);
			if (StringUtils.isEmpty(endpointURL)) {
				throw new STSZertifikatServiceException(
					METHOD_NAME_INIT_STS_WEB_SERVICE_PORT,
					"Es wurde keine Endpunkt URL definiert fuer den "
						+ "ZertSTSService");
			}

			LOGGER.info("PersonenSucheSTSService Endpoint: {}", endpointURL);

			URL url = null;
			if (wsdlURL != null) {
				try {
					// Test der neu mitgeteilten WSDL-URL:
					url = new URL(wsdlURL);
					LOGGER.info("PersonenSucheSTSService WSDL: {}", url);
					Object content = url.getContent();
					LOGGER.info("PersonenSucheSTSService WSDL-Content: {}", content);
				} catch (IOException e) {
					url = null;
					LOGGER.error("PersonenSucheSTSService WSDL not found at url : {}", wsdlURL, e);
				}
			}

			try {
				if (url == null) {
					// WSDL url wurde nicht  mitgeliefert. Die EndpointURL?wsdl geht also nicht und wir nehmen ein
					// fixes.
					url = STSWebService.class.getResource("/wsdl/sts/ZertSTSWebservice.wsdl");
					Objects.requireNonNull(
						url,
						"WSDL konnte unter der angegebenen URI nicht gefunden werden. Kann Service-Port nicht "
							+ "erstellen");
					LOGGER.info("PersonenSucheService WSDL URL: {}", url);
				}
				LOGGER.info("PersonenSucheSTSService TargetNameSpace: " + TARGET_NAME_SPACE);
				LOGGER.info("PersonenSucheSTSService ServiceName: " + SERVICE_NAME);
				final QName qname = new QName(TARGET_NAME_SPACE, SERVICE_NAME);
				LOGGER.info("PersonenSucheSTSService QName: {}", qname);
				final Service service = Service.create(url, qname);
				service.setHandlerResolver(portInfo -> Collections.singletonList(webserviceType.equals(WebserviceType.GERES) ?
					wssSecurityGeresAssertionExtractionHandler :
					wssSecurityNeskoAssertionExtractionHandler));

				LOGGER.info("PersonenSucheSTSService Service created: {}", service);
				port = service.getPort(ZertSTSService.class);
				LOGGER.info("PersonenSucheSTSService Port created: {}", port);
				final BindingProvider bp = (BindingProvider) port;

				bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

			} catch (RuntimeException e) {
				port = null;
				throw new STSZertifikatServiceException(METHOD_NAME_INIT_STS_WEB_SERVICE_PORT,
					"Could not create service-port ZertSTSService for endpoint " + endpointURL, e);
			}
		}
		LOGGER.info("ZertSTSService erfolgreich initialisiert");
	}
}

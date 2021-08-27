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

package ch.dvbern.ebegu.config;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Konfiguration von kiBon. Liest system Properties aus
 */
@Dependent
public class EbeguConfigurationImpl extends SystemConfiguration implements EbeguConfiguration, Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(EbeguConfigurationImpl.class.getSimpleName());

	private static final long serialVersionUID = 463057263479503486L;
	public static final String EBEGU_DEVELOPMENT_MODE = "ebegu.development.mode";
	private static final String EBEGU_DOCUMENT_FILE_PATH = "ebegu.document.file.path";
	private static final String EBEGU_CLIENT_USING_HTTPS = "ebegu.client.using.https";
	private static final String EBEGU_MAIL_DISABLED = "ebegu.mail.disabled";
	private static final String EBEGU_MAIL_SMTP_FROM = "ebegu.mail.smtp.from";
	private static final String EBEGU_MAIL_SMTP_HOST = "ebegu.mail.smtp.host";
	private static final String EBEGU_MAIL_SMTP_PORT = "ebegu.mail.smtp.port";
	private static final String EBEGU_HOSTNAME = "ebegu.hostname";
	private static final String EBEGU_DUMMY_LOGIN_ENABLED = "ebegu.dummy.login.enabled";
	private static final String EBEGU_ZAHLUNGEN_TEST_MODE = "ebegu.zahlungen.test.mode";
	private static final String EBEGU_PERSONENSUCHE_DISABLED = "ebegu.personensuche.disabled";
	private static final String EBEGU_PERSONENSUCHE_USE_DUMMY_SERVICE = "ebegu.personensuche.use.dummyservice";
	private static final String EBEGU_PERSONENSUCHE_ENDPOINT = "ebegu.personensuche.endpoint";
	private static final String EBEGU_PERSONENSUCHE_WSDL = "ebegu.personensuche.wsdl";
	private static final String EBEGU_PERSONENSUCHE_USERNAME = "ebegu.personensuche.username";
	private static final String EBEGU_PERSONENSUCHE_PASSWORD = "ebegu.personensuche.password";
	public static final String EBEGU_PERSONENSUCHE_STS_KEYSTORE_PATH = "ebegu.personensuche.sts.keystore.path";
	public static final String EBEGU_PERSONENSUCHE_STS_KEYSTORE_PW = "ebegu.personensuche.sts.keystore.pw";
	public static final String EBEGU_PERSONENSUCHE_STS_PRIVATE_KEY_ALIAS = "ebegu.personensuche.sts.private.key.alias";

	public static final String EBEGU_PERSONENSUCHE_STS_BASE_PATH = "ebegu.personensuche.sts.base.path";
	public static final String EBEGU_PERSONENSUCHE_STS_WSDL = "ebegu.personensuche.sts.wsdl";
	public static final String EBEGU_PERSONENSUCHE_STS_ENDPOINT = "ebegu.personensuche.sts.endpoint";
	public static final String EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_WSDL = "ebegu.personensuche.sts.renewal.assertion.wsdl";
	public static final String EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_ENDPOINT = "ebegu.personensuche.sts.renewal.assertion.endpoint";
	public static final String EBEGU_PERSONENSUCHE_GERES_ENDPOINT = "ebegu.personensuche.geres.endpoint";
	public static final String EBEGU_PERSONENSUCHE_GERES_WSDL = "ebegu.personensuche.geres.wsdl";
	public static final String EBEGU_GEOADMIN_SEARCHSERVER_URL = "ebegu.geoadmin.searchserver.url";
	public static final String EBEGU_GEOADMIN_MAPSERVER_URL = "ebegu.geoadmin.mapserver.url";


	public static final String EBEGU_KITAX_HOST = "ebegu.kitax.host";
	public static final String EBEGU_KITAX_ENDPOINT = "ebegu.kitax.endpoint";

	public static final String EBEGU_LOGIN_PROVIDER_API_URL = "ebegu.login.provider.api.url";
	private static final String EBEGU_LOGIN_API_ALLOW_REMOTE = "ebegu.login.api.allow.remote";
	private static final String EBEGU_LOGIN_API_INTERNAL_USER = "ebegu.login.api.internal.user";
	private static final String EBEGU_LOGIN_API_INTERNAL_PASSWORD = "ebegu.login.api.internal.password";
	private static final String EBEGU_FORCE_COOKIE_SECURE_FLAG = "ebegu.force.cookie.secure.flag";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_CLIENT = "ebegu.login.api.keycloack.client";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_PASSWORD = "ebegu.login.api.keycloack.password";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_AUTHSERVER = "ebegu.login.api.keycloack.authserver";
	private static final String EBEGU_TESTFAELLE_ENABLED = "ebegu.testfaelle.enabled";
	private static final String EBEGU_ADMINISTRATOR_MAIL = "ebegu.admin.mail";
	private static final String EBEGU_PORTAL_ACCOUNT_CREATION_LINK = "ebegu.portal.account.creation.link";
	private static final String SENTRY_ENVIRONMENT = "sentry.environment"; //use same property as sentry logger
	private static final String EBEGU_SUPERUSER_MAIL = "ebegu.superuser.mail";
	private static final String EBEGU_SUPPORT_MAIL = "ebegu.support.mail";

	private static final String KIBON_KAFKA_URL = "kibon.kafka.url";
	private static final String KIBON_SCHEMA_REGISTRY_URL = "kibon.schemaregistry.url";
	private static final String KIBON_EXCHANGE_BETREUUNGANFRAGE_ENABLED = "kibon.exchange.betreuunganfrage.enabled";
	private static final String KIBON_KAFKA_CONSUMER_ENABLED = "kibon.kafka.consumer.enabled";
	private static final String KIBON_KAFKA_CONSUMER_GROUP_ID = "kibon.kafka.consumer.group.id";

	private static final String CLAMAV_HOST = "ebegu.clamav.host";
	private static final String CLAMAV_PORT = "ebegu.clamav.port";
	private static final String CLAMAV_DISABLED = "ebegu.clamav.disabled";

	private static final String NOTVERORDNUNG_UNTERSCHRIFT_PATH = "ebegu.notverordnung.unterschrift.path";
	private static final String NOTVERORDNUNG_UNTERSCHRIFT_NAME = "ebegu.notverordnung.unterschrift.name";
	private static final String NOTVERORDNUNG_EMPFAENGER_MAIL = "ebegu.notverordnung.empfaenger.mail";

	private static final String MASSENMUTATION_EMPFAENGER_MAIL = "ebegu.massenmutation.empfaenger.mail";


	@Inject
	private ApplicationPropertyService applicationPropertyService;


	public EbeguConfigurationImpl() {

	}

	@Override
	public boolean getIsDevmode() {
		return getBoolean(EBEGU_DEVELOPMENT_MODE, true);
	}

	@Override
	public String getDocumentFilePath() {
		return getString(EBEGU_DOCUMENT_FILE_PATH, getString("jboss.server.data.dir"));
	}

	@Override
	public boolean isClientUsingHTTPS() {
		return getBoolean(EBEGU_CLIENT_USING_HTTPS, false);
	}

	@Override
	public boolean isSendingOfMailsDisabled() {
		return getBoolean(EBEGU_MAIL_DISABLED, getIsDevmode());
	}

	@Override
	public String getSMTPHost() {
		return getString(EBEGU_MAIL_SMTP_HOST, null);
	}

	@Override
	public int getSMTPPort() {
		return getInt(EBEGU_MAIL_SMTP_PORT, 25);
	}

	@Override
	public String getSenderAddress() {
		return getString(EBEGU_MAIL_SMTP_FROM, null);
	}

	@Override
	public String getHostname() {
		return getString(EBEGU_HOSTNAME, null);
	}

	@Override
	public boolean isDummyLoginEnabled() {
		// Um das Dummy Login einzuschalten, muss sowohl das DB Property wie auch das System Property gesetzt sein. Damit
		// ist eine zusätzliche Sicherheit eingebaut, dass nicht aus Versehen z.B. mit einem Produktionsdump das Dummy Login
		// automatisch ausgeschaltet ist.
		Boolean flagFromDB = applicationPropertyService.findApplicationPropertyAsBoolean(ApplicationPropertyKey.DUMMY_LOGIN_ENABLED, false);
		Boolean flagFromServerConfig = getBoolean(EBEGU_DUMMY_LOGIN_ENABLED, false);
		return flagFromDB && flagFromServerConfig;
	}

	@Override
	public boolean getIsZahlungenTestMode() {
		return getBoolean(EBEGU_ZAHLUNGEN_TEST_MODE, false) && getIsDevmode();
	}

	@Override
	public boolean isPersonenSucheDisabled() {
		return getBoolean(EBEGU_PERSONENSUCHE_DISABLED, true);
	}

	@Override
	public boolean usePersonenSucheDummyService() {
		return getBoolean(EBEGU_PERSONENSUCHE_USE_DUMMY_SERVICE, true);
	}

	@Override
	public String getPersonenSucheEndpoint() {
		return getString(EBEGU_PERSONENSUCHE_ENDPOINT);
	}

	@Override
	public String getPersonenSucheWsdl() {
		return getString(EBEGU_PERSONENSUCHE_WSDL);
	}

	@Override
	public String getPersonenSucheUsername() {
		return getString(EBEGU_PERSONENSUCHE_USERNAME);
	}

	@Override
	public String getPersonenSuchePassword() {
		return getString(EBEGU_PERSONENSUCHE_PASSWORD);
	}

	@Override
	public String getLoginProviderAPIUrl() {
		return getString(EBEGU_LOGIN_PROVIDER_API_URL);
	}

	@Override
	public boolean isRemoteLoginConnectorAllowed() {
		return getBoolean(EBEGU_LOGIN_API_ALLOW_REMOTE, false);
	}

	@Override
	public String getInternalAPIUser() {
		String user = getString(EBEGU_LOGIN_API_INTERNAL_USER);
		if (StringUtils.isEmpty(user)) {
			LOG.warn("Internal API User  must be set in the properties (key: {}) to use the LoginConnector API ",
				EBEGU_LOGIN_API_INTERNAL_USER);

		}
		return user;
	}

	@Override
	public String getInternalAPIPassword() {
		String internalUserPW = getString(EBEGU_LOGIN_API_INTERNAL_PASSWORD);
		if (StringUtils.isEmpty(internalUserPW)) {
			LOG.warn("Internal API password must be set in the properties (key: {}) to use the LoginConnector API ",
				EBEGU_LOGIN_API_INTERNAL_PASSWORD);
		}
		return internalUserPW;
	}

	@Override
	public String getKeycloackClient() {
		return getString(EBEGU_LOGIN_API_KEYCLOACK_CLIENT);
	}

	@Override
	public String getKeycloackPassword() {
		return getString(EBEGU_LOGIN_API_KEYCLOACK_PASSWORD);
	}

	@Override
	public String getKeycloackAuthServer() {
		return getString(EBEGU_LOGIN_API_KEYCLOACK_AUTHSERVER);
	}

	@Override
	public boolean forceCookieSecureFlag() {
		return getBoolean(EBEGU_FORCE_COOKIE_SECURE_FLAG, false);
	}

	@Override
	public boolean isTestfaelleEnabled() {
		return getBoolean(EBEGU_TESTFAELLE_ENABLED, false);
	}

	@Override
	public String getAdministratorMail() {
		return getString(EBEGU_ADMINISTRATOR_MAIL);
	}

	@Override
	public String getPortalAccountCreationPageLink() {
		return getString(EBEGU_PORTAL_ACCOUNT_CREATION_LINK, "https://beloginportal-replica.fin.be.ch/emaillogin/gui/registration/createmaillogin");
	}

	@Override
	public String getSentryEnv() {
		return getString(SENTRY_ENVIRONMENT, "unspecified");
	}

	@Override
	public KibonLogLevel getDefaultLogLevel() {
		return this.getIsDevmode() ? KibonLogLevel.INFO : KibonLogLevel.ERROR;
	}

	@Override
	public String getSuperuserMail() {
		return getString(EBEGU_SUPERUSER_MAIL);
	}

	@Override
	public String getSupportMail() {
		return getString(EBEGU_SUPPORT_MAIL, "support@kibon.ch");
	}

	@Nonnull
	@Override
	public Optional<String> getKafkaURL() {
		return Optional.ofNullable(getString(KIBON_KAFKA_URL));
	}

	@Nonnull
	@Override
	public String getSchemaRegistryURL() {
		return getString(KIBON_SCHEMA_REGISTRY_URL, "");
	}

	@Override
	public boolean isBetreuungAnfrageApiEnabled() {
		return getBoolean(KIBON_EXCHANGE_BETREUUNGANFRAGE_ENABLED, false);
	}

	@Override
	public boolean isKafkaConsumerEnabled() {
		return getBoolean(KIBON_KAFKA_CONSUMER_ENABLED, false);
	}

	@Override
	public String getEbeguPersonensucheSTSKeystorePath() {

		String jbossHome =  System.getProperty("jboss.home.dir");
		String defaultPathToJKS =  jbossHome + "/rkb1-svbern-sts-ks-u.jks";

		return getString(EBEGU_PERSONENSUCHE_STS_KEYSTORE_PATH, defaultPathToJKS);

	}

	@Override
	public String getEbeguPersonensucheSTSKeystorePW() {
		return getString(EBEGU_PERSONENSUCHE_STS_KEYSTORE_PW);
	}

	@Override
	public String getEbeguPersonensucheSTSPrivateKeyAlias() {
		return getString(EBEGU_PERSONENSUCHE_STS_PRIVATE_KEY_ALIAS, "rkb1");
	}

	@Override
	public String getEbeguPersonensucheSTSPrivateKeyPW() {
		return getEbeguPersonensucheSTSKeystorePW();
	}


	@Override
	public String getEbeguPersonensucheSTSBasePath(){
		return getString(EBEGU_PERSONENSUCHE_STS_BASE_PATH, "https://a6hu-www-sts-b.be.ch/securityService"); //test
//		return getString(EBEGU_PERSONENSUCHE_STS_BASE_PATH, "https://a6ha-www-sts-b.be.ch/securityService"); //prod
	}

	@Override
	public String getEbeguPersonensucheSTSWsdl() {
		return getString(EBEGU_PERSONENSUCHE_STS_WSDL);
	}

	@Override
	public String getEbeguPersonensucheSTSEndpoint() {
		return getString(EBEGU_PERSONENSUCHE_STS_ENDPOINT, getEbeguPersonensucheSTSBasePath() +  "/zertsts/services/ZertSTSWebservice");
	}

	@Override
	public String getEbeguPersonensucheSTSRenewalAssertionWsdl() {
		return getString(EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_WSDL);
	}

	@Override
	public String getEbeguPersonensucheSTSRenewalAssertionEndpoint() {
		return getString(EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_ENDPOINT, getEbeguPersonensucheSTSBasePath() +  "/samlrenew/services/RenewAssertionWebService");
	}

	@Override
	public String getEbeguPersonensucheGERESEndpoint() {
		return getString(EBEGU_PERSONENSUCHE_GERES_ENDPOINT, "https://testv3-geres.be.ch/ech/services/GeresResidentInfoService_v1801");
//		return getString(EBEGU_PERSONENSUCHE_GERES_ENDPOINT, "https://geres.be.ch/ech/services/GeresResidentInfoService_v1801");     // produktion


	}

	@Override
	public String getEbeguPersonensucheGERESWsdl() {
		return getString(EBEGU_PERSONENSUCHE_GERES_WSDL);
	}

	@Override
	public String getEbeguGeoadminSearchServerUrl() {
		return getString(EBEGU_GEOADMIN_SEARCHSERVER_URL, "https://api3.geo.admin.ch/rest/services/api/SearchServer");
	}

	@Override
	public String getEbeguGeoadminMapServerUrl() {
		return getString(EBEGU_GEOADMIN_MAPSERVER_URL, "https://api3.geo.admin.ch/rest/services/api/MapServer");
	}

	@Override
	public String getKitaxHost() {
		return getString(EBEGU_KITAX_HOST, "https://ebegu.dvbern.ch");
	}

	@Override
	public String getKitaxEndpoint() {
		return getString(EBEGU_KITAX_ENDPOINT, "/ebegu/api/v1/kibon/lookup");
	}

	@Override
	public String getClamavHost() {
		return getString(CLAMAV_HOST, "localhost");
	}

	@Override
	public int getClamavPort() {
		return getInt(CLAMAV_PORT, 3310);
	}

	@Override
	public boolean isClamavDisabled() {
		return getBoolean(CLAMAV_DISABLED, true);
	}

	@Override
	public String getNotverordnungUnterschriftName() {
		return getString(NOTVERORDNUNG_UNTERSCHRIFT_NAME);
	}

	@Override
	public String getNotverordnungUnterschriftPath() {
		return getString(NOTVERORDNUNG_UNTERSCHRIFT_PATH);
	}

	@Override
	public String getNotverordnungEmpfaengerMail() {
		return getString(NOTVERORDNUNG_EMPFAENGER_MAIL);
	}

	@Override
	public String getMassenmutationEmpfaengerMail() {
		return getString(MASSENMUTATION_EMPFAENGER_MAIL);
	}

	@Override
	public String getKafkaConsumerGroupId() {
		return getString(KIBON_KAFKA_CONSUMER_GROUP_ID, "dev");
	}
}

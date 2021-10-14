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

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.errors.KibonLogLevel;

/**
 * Konfiguration von kiBon
 */
public interface EbeguConfiguration {

	/**
	 * @return true wenn sich die Applikation im Entwicklungsmodus befindet, false sonst
	 */
	boolean getIsDevmode();

	String getDocumentFilePath();

	/**
	 * Gibt an ob die Client Applikation https verwendet. Wenn true werden cookies nur bei https clients gesetzt
	 */
	boolean isClientUsingHTTPS();

	/**
	 * Gibt zurueck, ob E-Mails versendet werden sollen. Falls nicht, wird der entsprechende Text auf der Console ausgegeben
	 */
	boolean isSendingOfMailsDisabled();

	/**
	 * Gibt einen Host zurück welcher zum Verschicken eines Mails verwendet wird.
	 *
	 * @return einen Hostnamen, oder {@code null}
	 */
	String getSMTPHost();

	/**
	 * Gibt den Port zurück welcher zum Verschicken eines Mails verwendet wird.
	 *
	 * @return einen Port, 25 wenn nichts konfiguriert.
	 */
	int getSMTPPort();

	/**
	 * Gibt die Absender-Adresse fuer mails zurück.
	 *
	 * @return die Absender-Adresse oder {@code null}
	 */
	String getSenderAddress();

	/**
	 * Gibt den Hostname des Servers zurück.
	 *
	 * @return den Hostname oder {@code null}
	 */
	String getHostname();

	/**
	 * Gibt die Hostdomain des Servers zurück.
	 *
	 * @return die Hostdomain oder {@code null}
	 */
	String getHostdomain();

	/**
	 * Gibt zurueck ob es moeglich sein soll mit den dummy useren einzulaggen
	 *
	 * @return true oder false
	 * @see "dummy-users.properties" and AuthResource#login
	 */
	boolean isDummyLoginEnabled();

	/**
	 * @return true wenn sich die Applikation im Testmodus fuer Zahlungen befindet, false sonst
	 */
	boolean getIsZahlungenTestMode();

	/**
	 * Gibt zurueck, ob die Personensuche über GERES generell angeboten werden soll
	 */
	boolean isPersonenSucheDisabled();

	/**
	 * Gibt zurueck, ob der Dummy-Service für die EWK-Abfragen benutzt werden soll.
	 */
	boolean usePersonenSucheDummyService();

	/**
	 * Gibt den Endpoint des EWK-Services zurueck.
	 */
	String getPersonenSucheEndpoint();

	/**
	 * URL des WSDLs des EWK-Services
	 */
	String getPersonenSucheWsdl();

	/**
	 * Gibt den Usernamen für den EWK-Service zurueck.
	 */
	String getPersonenSucheUsername();

	/**
	 * Gibt das Passwort für den EWK-Service zurueck.
	 */
	String getPersonenSuchePassword();

	/**
	 * Gibt die URL des API Endpunkt des LoginConnectors zurueck.
	 * Ueber diesen list  KI-TAX die URLS zum single-login und single-logout
	 *
	 * @return REST API Endpunkt ueber den Ki-TAX die URLS fuer login/logout requests lesen kann
	 */
	String getLoginProviderAPIUrl();

	/**
	 * @return true if LoginConnector may access REST interface remotly, otherwise only local access is allowed
	 */
	boolean isRemoteLoginConnectorAllowed();

	/**
	 * @return den Benutzernamen des internen API users
	 */
	String getInternalAPIUser();

	/**
	 * @return das Benutzerpasswort fuer den internen API USER
	 */
	String getInternalAPIPassword();

	/**
	 * @return den Benutzernamen des Schulamt API users
	 */
	String getKeycloackClient();

	/**
	 * @return das Benutzerpasswort fuer den Schulamt API USER
	 */
	String getKeycloackPassword();

	/**
	 * @return die URL fuer den Keycloak Server
	 */
	String getKeycloackAuthServer();

	/**
	 * @return by default the secure flag of cookies will be set based on the incoming request. To force the application
	 * to only set cookies with the secure flag this property can be set to true (default is false)
	 */
	boolean forceCookieSecureFlag();

	/**
	 * Property, welches festlegt, ob die vordefinierten Testfaelle fuer diese Umgebung verwendet werden duerfen.
	 * Achtung, dieses Property wird vom Dummy-Login Property übersteuert, d.h. es müssen beide gesetzt sein!
	 */
	boolean isTestfaelleEnabled();

	/**
	 * Admin-Email: An diese Adresse wird z.B. die Zahlungskontrolle gesendet.
	 */
	String getAdministratorMail();

	/**
	 *
	 * @return a full link to the page where users can create new logins for the portal
	 */
	String getPortalAccountCreationPageLink();

	/**
	 * read sentry env from system properties
	 */
	String getSentryEnv();

	/**
	 * Returns the LogLevel by default. INFO for Dev-Mode and ERROR for others (production)
	 */
	KibonLogLevel getDefaultLogLevel();

	/**
	 * Gibt die E-Mail des (ersten) Superusers zurueck
	 */
	String getSuperuserMail();

	/**
	 * Gibt die E-Mail des Kibon-Supports zurück
	 */
	String getSupportMail();

	@Nonnull
	Optional<String> getKafkaURL();

	@Nonnull
	String getSchemaRegistryURL();

	/**
	 * @return TRUE, falls neue Betreuungen an den Exchange Service exportiert werden sollen.
	 */
	boolean isBetreuungAnfrageApiEnabled();

	/**
	 * @return TRUE, falls neue Betreuungen an den Exchange Service exportiert werden sollen.
	 */
	boolean isAnmeldungTagesschuleApiEnabled();


	/**
	 * @return TRUE, falls Daten Kafka gelesen werden dürfen.
	 */
	boolean isKafkaConsumerEnabled();


	/**
	 * @return Filepath zum Keystore in dem der Private Key fuer den Secure-Token-Service Webservice liegt,
	 * sollte in einer Form sein die den Pfad vom  resource root her aufloest ist zb "/prod/sts-webservice.jks"
	 * The Idea is to read the keystore from a specific place in the file System
	 */
	String getEbeguPersonensucheSTSKeystorePath();

	/**
	 * Passwort fuer den Keystore in dem der PrivateKey zum abholen der SAMLAssertion beim STS Webservice fuer den Batchuser mit dem wir EWK Abfragen machn liegt
	 * @return Passwort fuer den Keystore
	 */
	String getEbeguPersonensucheSTSKeystorePW();

	/**
	 *
	 * @return alias des PrivateKeys der verwendet wird um den STS Service aufzurufen
	 */
	String getEbeguPersonensucheSTSPrivateKeyAlias();

	/**
	 *
	 * @return Passwort um den PrivateKey zu entsperren
	 */
	String getEbeguPersonensucheSTSPrivateKeyPW();

	/**
	 *
	 * @return Basisurl des Webserivce Endpunkts fuer den Secure Token Service (STS) der zum erstellen eines
	 * Asserton Tokens fuer den GERES Service gebraucht wird. Diese Angabe wird verwendet um die
	 * finale URL des  STSToken und  des RenewalAssertion Services zu erstellen. Ausser wenn fuer diese beiden
	 * ebenfalls eine Explizite Absolute URL angegeben wird.
	 * So kann einfach zwischen Test und Produktion gewechselt werden
	 * Beispiel: "https://a6hu-www-sts-b.be.ch:443/securityService"
	 */
	String getEbeguPersonensucheSTSBasePath();

	/**
	 * OPTIONALE Angabe zum herunterladen des WSDL Files. Wenn nicht angegeben wird die im System
	 * mit der Applikation deployte Version des WSDL verwendet
	 * @return null oder wsdl url
	 */
	String getEbeguPersonensucheSTSWsdl();

	/**
	 * OPTIONALE Angabe zur genauen Spezifikation der Endpunktangabe des STS Service. Wenn nicht angegeben
	 * wird mit dem getEbeguPersonensucheSTSBasePath und dem aktuell bekannten Serivcepfad die URL per
	 * default zusammengesetzt
	 * @return Absolute URL fuer den Endpoint des zu verwendenden STS Service.
	 */
	String getEbeguPersonensucheSTSEndpoint();

	/**
	 * OPTIONALE Angabe zum herunterladen des WSDL Files des Renewal Assertion Service. Wenn nicht angegeben wird die im System
	 * mit der Applikation deployte Version des WSDL verwendet
	 * @return null oder wsdl url
	 */
	String getEbeguPersonensucheSTSRenewalAssertionWsdl();

	/**
	 * OPTIONALE Angabe zur genauen Spezifikation der Endpunktangabe des STS Renewal Assertion Service. Wenn nicht angegeben
	 * wird mit dem getEbeguPersonensucheSTSBasePath und dem aktuell bekannten Serivcepfad die URL per
	 * default zusammengesetzt
	 * @return Absolute URL fuer den Endpoint des zu verwendenden STS Service.
	 */
	String getEbeguPersonensucheSTSRenewalAssertionEndpoint();

	/**
	 *
	 * @return GERES Personensuche Webservice Endpoint
	 * Beispiel: https://testv3-geres.be.ch/ech/services/GeresResidentInfoService_v1801
	 */
	String getEbeguPersonensucheGERESEndpoint();

	/**
	 *
	 * OPTIONALE Angabe zum herunterladen des WSDL Files des GeresWebService (ResidentInfo). Wenn nicht angegeben wird die im System
	 * mit der Applikation deployte Version des WSDL verwendet
	 * @return null oder wsdl url
	 */
	String getEbeguPersonensucheGERESWsdl();

	/**
	 * @return GEOADMIN Webservice Endpoint für den SearchServer
	 * Beispiel: https://api3.geo.admin.ch/rest/services/api/SearchServer
	 * Siehe GeoAdmin Dokumentation: https://api3.geo.admin.ch/services/sdiservices.html
	 */
	String getEbeguGeoadminSearchServerUrl();

	/**
	 * @return GeoAdmin Webservice Endpoint für den MapServer
	 * Beispiel: "https://api3.geo.admin.ch/rest/services/api/MapServer"
	 * Siehe GeoAdmin Dokumentation: https://api3.geo.admin.ch/services/sdiservices.html
	 */
	String getEbeguGeoadminMapServerUrl();

	String getKitaxHost();

	String getKitaxEndpoint();

	String getClamavHost();

	int getClamavPort();

	boolean isClamavDisabled();

	String getNotverordnungUnterschriftName();

	String getNotverordnungUnterschriftPath();

	String getNotverordnungEmpfaengerMail();

	String getKafkaConsumerGroupId();

	String getMassenmutationEmpfaengerMail();

	Boolean getMultimandantEnabled();
}

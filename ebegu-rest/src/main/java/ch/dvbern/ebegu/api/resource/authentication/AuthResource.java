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

package ch.dvbern.ebegu.api.resource.authentication;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxAuthAccessElementCookieData;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.authentication.AuthAccessElement;
import ch.dvbern.ebegu.authentication.AuthLoginElement;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AuthorisierterBenutzer;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.AuthService;
import ch.dvbern.ebegu.services.BenutzerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource has functions to login or logout
 */
@Stateless
@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

	private static final Logger LOG = LoggerFactory.getLogger(AuthResource.class);

	@Inject // @EJB
	private AuthService authService;

	@Context
	private HttpServletRequest request;
	@Inject
	private BenutzerService benutzerService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private UsernameRoleChecker usernameRoleChecker;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private LoginProviderInfoRestService loginProviderInfoRestService;

	@Inject
	private PrincipalBean principal;

	@Path("/portalAccountPage")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	@PermitAll
	public Response getPortalAccountCreationPageLink() {
		String url = configuration.getPortalAccountCreationPageLink();
		return Response.ok(url).build();
	}

	@Path("/connectorPing")
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll
	public Response connectorPing() {
		final String response = this.loginProviderInfoRestService.pingLoginProvider();
		return Response.ok(response).build();

	}

	@Path("/singleSignOn")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	@PermitAll
	public Response initSSOLogin(@Nullable @QueryParam("relayPath") String relayPath) {

		String url = this.loginProviderInfoRestService.getSSOLoginInitURL(relayPath);
		LOG.debug("Received URL to initialize singleSignOn login '{}'", url);
		return Response.ok(url).build();
	}

	@Path("/singleLogout")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	@PermitAll
	public Response initSingleLogout(
		@Nullable @QueryParam("relayPath") String relayPath,
		@CookieParam(AuthConstants.COOKIE_AUTH_TOKEN) Cookie authTokenCookie
	) {

		if (authTokenCookie != null && authTokenCookie.getValue() != null) {
			Optional<AuthorisierterBenutzer> currentAuthOpt = authService
				.validateAndRefreshLoginToken(authTokenCookie.getValue(), false);
			if (currentAuthOpt.isPresent()) {
				String logoutUrl = loginProviderInfoRestService.getSingleLogoutURL();
				LOG.debug("Received URL to initialize Logout URL '{}'", logoutUrl);
				return Response.ok(logoutUrl).build();
			}
		}

		return Response.ok("").build(); //dummy
	}

	/**
	 * extrahiert die Daten aus dem DTO und versucht einzuloggen. Fuer das einloggen
	 * Fuer das Login schreiben wir selber eine Logik die direkt ohne Loginmodul ueber den Service einloggt.
	 * Dabei checken wir unser property File und suchen die gegebene Username/PW kombination
	 *
	 * @param loginElement Benutzer Identifikation (Benutzername/Passwort)
	 * @return im Erfolgsfall eine HTTP Response mit Cookies
	 */
	@Nullable
	@POST
	@Path("/login")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public Response login(
			@Nonnull JaxBenutzer loginElement,
			@CookieParam(AuthConstants.COOKIE_AUTH_TOKEN) Cookie authTokenCookie) {
		if (configuration.isDummyLoginEnabled()) {

			// zuerst im Container einloggen, sonst schlaegt in den Entities die Mandanten-Validierung fehl
			if (!usernameRoleChecker.checkLogin(loginElement.getUsername(), loginElement.getPassword())) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			//wir machen kein rollenmapping sondern versuchen direkt in enum zu transformieren
			String roleString = usernameRoleChecker.getSingleRole(loginElement.getUsername());
			UserRole validRole = UserRole.valueOf(roleString);

			AuthLoginElement login = new AuthLoginElement(loginElement.getUsername(), loginElement.getPassword(),
				loginElement.getNachname(), loginElement.getVorname(), loginElement.getEmail(), validRole);

			// Der Benutzer wird gesucht. Wenn er noch nicht existiert wird er erstellt und wenn ja dann aktualisiert
			Benutzer benutzer = null;
			Optional<Benutzer> optBenutzer = benutzerService.findAndLockBenutzer(loginElement.getUsername());
			if (optBenutzer.isPresent()) {
				benutzer = optBenutzer.get();
				// Damit wird kein neues Element erstellt, sondern das bestehende "verändert". Führt sonst zu einem
				// Löschen und Wiedereinfügen in der History-Tabelle
				loginElement.getBerechtigungen().iterator().next().setId(benutzer.getCurrentBerechtigung().getId());
			} else {
				benutzer = new Benutzer();
				Berechtigung localloginBerechtigung = new Berechtigung();
				// Wir sind hier im locallogin: Die dafür erstellte Berechtigung ist defaultmässig aktiv
				localloginBerechtigung.setBenutzer(benutzer);
				benutzer.getBerechtigungen().add(localloginBerechtigung);
			}
			// Achtung: Damit wird der bereits vorhandene Benutzer wieder mit den Daten aus dem LocalLogin überschrieben!
			// Dies ist aber gewünschtes Verhalten: Wenn wir uns mit dem Admin-Link einloggen, wollen wir immer Admin sein.
			benutzerService.saveBenutzer(converter.jaxBenutzerToBenutzer(loginElement, benutzer));

			Optional<AuthAccessElement> accessElement = authService.login(login);
			if (!accessElement.isPresent()) {
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			AuthAccessElement access = accessElement.get();
			JaxAuthAccessElementCookieData element = convertToJaxAuthAccessElement(access);
			boolean cookieSecure = isCookieSecure();

			String domain = configuration.getHostdomain();

			// Cookie to store auth_token, HTTP-Only Cookie --> Protection from XSS
			NewCookie authCookie = new NewCookie(AuthConstants.COOKIE_AUTH_TOKEN, access.getAuthToken(),
				AuthConstants.COOKIE_PATH, domain, "authentication",
				AuthConstants.COOKIE_TIMEOUT_SECONDS, cookieSecure, true);
			// Readable Cookie for XSRF Protection (the Cookie can only be read from our Domain)
			NewCookie xsrfCookie = new NewCookie(AuthConstants.COOKIE_XSRF_TOKEN, access.getXsrfToken(),
				AuthConstants.COOKIE_PATH, domain, "XSRF",
				AuthConstants.COOKIE_TIMEOUT_SECONDS, cookieSecure, false);
			// Readable Cookie storing user data
			NewCookie principalCookie = new NewCookie(AuthConstants.COOKIE_PRINCIPAL, encodeAuthAccessElement(element),
				AuthConstants.COOKIE_PATH, domain, "principal",
				AuthConstants.COOKIE_TIMEOUT_SECONDS, cookieSecure, false);
			// Readable Cookie storing the mandant
			NewCookie mandantCokie = new NewCookie(AuthConstants.COOKIE_MANDANT,
					benutzer.getMandant() != null ? benutzer.getMandant().getName() : null,
					AuthConstants.COOKIE_PATH, domain, "mandant",
					60 * 60 * 24 * 365 * 2, cookieSecure, false);

			return Response.noContent()
					.cookie(authCookie,
							xsrfCookie,
							principalCookie,
							mandantCokie)
					.build();
		}

		LOG.warn("Dummy Login is disabled, returning 410");
		return Response.status(Response.Status.GONE).build();
	}

	/**
	 * convert to dto that can be passed to login-connector
	 *
	 * @param access AuthAccessElement to convert
	 * @return DTO used to create cookie in login-connector
	 */
	private JaxAuthAccessElementCookieData convertToJaxAuthAccessElement(AuthAccessElement access) {
		return new JaxAuthAccessElementCookieData(
			access.getAuthId(),
			access.getNachname(),
			access.getVorname(),
			access.getEmail(),
			String.valueOf(access.getRole()));
	}

	private boolean isCookieSecure() {
		final boolean forceCookieSecureFlag = configuration.forceCookieSecureFlag();
		return isRequestProtocolSecure() || forceCookieSecureFlag;
	}

	private boolean isRequestProtocolSecure() {
		// get protocol of original request if present
		final String originalProtocol = request.getHeader(AuthConstants.X_FORWARDED_PROTO);
		if (originalProtocol != null) {
			return originalProtocol.startsWith("https");
		}
		return request.isSecure();
	}

	@POST
	@Path("/logout")
	@PermitAll
	@Produces(MediaType.TEXT_PLAIN)
	public Response logout(
			@CookieParam(AuthConstants.COOKIE_AUTH_TOKEN) Cookie authTokenCookie) {
		try {
			if (authTokenCookie != null ) {
				String authToken = Objects.requireNonNull(authTokenCookie.getValue());
				if (!authService.logoutAndDelete(authToken)) {
					LOG.debug("Could not remove authToken in database");
				}
			}
			// Always Respond with expired cookies
			boolean cookieSecure = isCookieSecure();

			NewCookie authCookie = expireCookie(AuthConstants.COOKIE_AUTH_TOKEN, cookieSecure, true);
			NewCookie xsrfCookie = expireCookie(AuthConstants.COOKIE_XSRF_TOKEN, cookieSecure, false);
			NewCookie principalCookie = expireCookie(AuthConstants.COOKIE_PRINCIPAL, cookieSecure, false);
			return Response.noContent().cookie(authCookie, xsrfCookie, principalCookie).build();
		} catch (NoSuchElementException e) {
			LOG.info("Token Decoding from Cookies failed", e);
			return Response.noContent().build();
		}
	}

	@Nonnull
	private NewCookie expireCookie(@Nonnull String name, boolean secure, boolean httpOnly) {
		return new NewCookie(name, "", AuthConstants.COOKIE_PATH, configuration.getHostdomain(), "", 0, secure, httpOnly);
	}

	/**
	 * @param element zu codirendes AuthAccessElement
	 * @return Base64 encoded JSON representation
	 */
	private String encodeAuthAccessElement(JaxAuthAccessElementCookieData element) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			String s = Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(element));
			return URLEncoder.encode(s, StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			throw new IllegalStateException("UTF-8 encoding must be available", e);
		}
	}
}

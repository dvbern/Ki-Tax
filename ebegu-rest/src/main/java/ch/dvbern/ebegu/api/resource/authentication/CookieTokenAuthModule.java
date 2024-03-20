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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.message.AuthStatus;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.EbeguApplicationV1;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import io.sentry.Sentry;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.util.BasicAuthHelper;
import org.omnifaces.security.jaspic.core.AuthParameters;
import org.omnifaces.security.jaspic.core.HttpMsgContext;
import org.omnifaces.security.jaspic.core.HttpServerAuthModule;
import org.omnifaces.security.jaspic.user.TokenAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static ch.dvbern.ebegu.api.AuthConstants.COOKIE_AUTHORIZATION_HEADER;
import static ch.dvbern.ebegu.util.Constants.LOGINCONNECTOR_USER_USERNAME;
import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.omnifaces.security.cdi.Beans.getReferenceOrNull;
import static org.omnifaces.security.jaspic.Utils.isEmpty;

/**
 * Authentication module / Loginmodule that authenticates based on a token in a cookie the request.
 * <p>
 * <p>
 * Token to username/roles mapping is delegated to an implementation of {@link TokenAuthenticator}, which
 * should be registered as CDI bean.
 * <p>
 * <p>
 * <b>NOTE:</b> This module makes the simplifying assumption that CDI is available in a SAM. Unfortunately
 * this is not true for every implementation. See https://java.net/jira/browse/JASPIC_SPEC-14
 *
 * @author Arjan Tijms
 */
public class CookieTokenAuthModule extends HttpServerAuthModule {

	private static final Logger LOG = LoggerFactory.getLogger(CookieTokenAuthModule.class);

	private final String internalApiUser;
	private final String internalApiPassword;
	private final String keycloackClient;
	private final String keycloackPassword;
	private final String keycloackAuthServer;

	@SuppressWarnings("PMD.UnusedFormalParameter")
	public CookieTokenAuthModule(String loginModuleStackName) {
		//this is unused, just checked if this could be used to declare this module through standalone.xml instead of
		//SamRegistrationListener
		this();
	}

	public CookieTokenAuthModule(@Nullable String internalUser, @Nullable String internalPassword,
		@Nullable String keycloackClient, @Nullable String keycloackPassword, @Nullable String keycloackAuthServer) {
		//this is unused, just checked if this could be used to declare this module through standalone.xml instead of
		//SamRegistrationListener
		this.internalApiUser = internalUser;
		this.internalApiPassword = internalPassword;
		if (internalPassword == null || internalUser == null) {
			throw new EbeguRuntimeException("CookieTokenAuthModule initialization", "Internal API User must be set");
		}
		this.keycloackClient = keycloackClient;
		this.keycloackPassword = keycloackPassword;
		this.keycloackAuthServer = keycloackAuthServer;
	}

	public CookieTokenAuthModule(@Nullable String keycloackClient, @Nullable String keycloackPassword,
		@Nullable String keycloackAuthServer) {
		internalApiUser = null;
		internalApiPassword = null;
		this.keycloackClient = keycloackClient;
		this.keycloackPassword = keycloackPassword;
		this.keycloackAuthServer = keycloackAuthServer;
	}

	public CookieTokenAuthModule() {
		internalApiUser = null;
		internalApiPassword = null;
		keycloackClient = null;
		keycloackPassword = null;
		keycloackAuthServer = null;
	}

	@Override
	@SuppressWarnings({ "checkstyle:CyclomaticComplexity", "checkstyle:BooleanExpressionComplexity" })
	public AuthStatus validateHttpRequest(HttpServletRequest request, HttpServletResponse response,
		HttpMsgContext httpMsgContext) {
		prepareLogvars(httpMsgContext);
		//maybe we should do a logout first?
		//		try {
		//			request.logout();
		//		} catch (ServletException e) {
		//			LOG.error("Unexpected exception during Logout", e);
		//			return setResponseUnauthorised(request, httpMsgContext);
		//		}

		//Exceptional paths that do not require a login (they must also be added to web.xml security filter exceptions)
		String apiBasePath = request.getContextPath() + EbeguApplicationV1.API_ROOT_PATH;
		String path = request.getRequestURI();
		AuthDataUtil.getBasePath(request);
		if (path.startsWith(apiBasePath + "/auth/login")
			|| path.startsWith(apiBasePath + "/connector/heartbeat")
			|| path.startsWith(apiBasePath + "/schulamt/heartbeat")
			|| path.startsWith(apiBasePath + "/auth/portalAccountPage")
			|| path.startsWith(apiBasePath + "/auth/singleSignOn")
			|| path.startsWith(apiBasePath + "/auth/connectorPing")
			|| path.startsWith(apiBasePath + "/auth/singleLogout")
			|| path.startsWith(apiBasePath + "/swagger.json")
			|| path.startsWith(request.getContextPath() + "/ebeguTestLogin.jsp")
			|| path.startsWith(request.getContextPath() + "/logout.jsp")
			|| path.startsWith(request.getContextPath() + "/samlinfo.jsp")
			|| path.startsWith(request.getContextPath() + "/saml2/jsp/")
			|| path.startsWith(request.getContextPath() + "/fedletapplication")
			|| path.startsWith(request.getContextPath() + "/fedletSloInit")
			|| path.startsWith(request.getContextPath() + "/fedletlogout")
			|| path.startsWith(request.getContextPath() + "/fedletSloRedirect")
			|| "OPTIONS".equals(request.getMethod())) {
			// Beim Login Request gibt es noch nichts abzufangen
			return httpMsgContext.doNothing();
		}

		if (path.startsWith(apiBasePath + "/connector")) {
			return checkAuthorizationForInternalApiAccess(request, httpMsgContext);
		}

		if (path.startsWith(apiBasePath + "/schulamt")) {
			return checkAuthorizationForSchulamtApiAccess(request, httpMsgContext);
		}

		//pages that do not fall under de security-context that was defined in webx.xml
		if (!httpMsgContext.isProtected()) {
			return httpMsgContext.doNothing();
		}

		// Verify that XSRF-Token from HTTP-Header matches Cookie-XSRF-Token
		if (!verifyXSFRHeader(request)) {
			return setResponseUnauthorised(httpMsgContext);
		}
		try {
			// Get AuthId (=loginname, actually not needed) and AuthToken from Cookies.
			String authToken = AuthDataUtil.getAuthTokenFomCookie(request).get();
			String authId = AuthDataUtil.getAuthAccessElement(request).get().getAuthId();
			if (!isEmpty(authToken)) {

				// authId ist der Loginname (z.B. Email)
				MDC.put(Constants.LOG_MDC_EBEGUUSER, authId);
				TokenAuthenticator tokenAuthenticator = getReferenceOrNull(TokenAuthenticator.class);
				if (tokenAuthenticator != null) {

					// In einigen Faellen wollen wir bei einem Request nicht automatisch das Login verlaengern, z.B.
					// wenn ein Request ueber einen Timer ausgeloest war (z.B. Posteingang)
					// Da das Interface authenticate() mit einem Parameter vorgegeben ist, haengen wir in diesem Fall
					// einen Suffix an das Login-Token
					if (path.contains(Constants.PATH_DESIGNATOR_NO_TOKEN_REFRESH)) {
						authToken = authToken + Constants.AUTH_TOKEN_SUFFIX_FOR_NO_TOKEN_REFRESH_REQUESTS;
					}
					if (tokenAuthenticator.authenticate(authToken)) {
						LOG.debug("successfully logged in user: {}", tokenAuthenticator.getUserName());
						return httpMsgContext.notifyContainerAboutLogin(tokenAuthenticator.getUserName(),
							tokenAuthenticator.getApplicationRoles());
					}

					// Token Verification Failed
					LOG.debug("Token verification failed for {}", tokenAuthenticator.getUserName());
					return setResponseUnauthorised(httpMsgContext);
				}

				LOG.warn("No Authenticator found with CDI:  {} all auth attempts will be refused",
					TokenAuthenticator.class.getSimpleName());
			}

		} catch (NoSuchElementException e) {
			LOG.info("Login with Token failed", e);
			return setResponseUnauthorised(httpMsgContext);
		}

		if (httpMsgContext.isProtected()) {
			LOG.debug("Access to protected path denied");
			return httpMsgContext.responseNotFound();
		}

		return httpMsgContext.doNothing();
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private AuthStatus checkAuthorizationForInternalApiAccess(HttpServletRequest request,
		HttpMsgContext httpMsgContext) {
		if (!isInternalApiActive()) {
			LOG.error("Call to connector API even though the properties for username and password were not defined "
				+ " in ebegu. Please check that the system properties for username/password for the internal api are"
				+ " set");
			return setResponseUnauthorised(httpMsgContext);
		}

		return checkAuthorizationViaBasicAuth(request, httpMsgContext, this.internalApiUser, this.internalApiPassword);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private AuthStatus checkAuthorizationForSchulamtApiAccess(HttpServletRequest request,
		HttpMsgContext httpMsgContext) {
		if (!isSchulamtApiActive()) {
			LOG.error("Call to Schulamt API even though the properties for username and password were not defined "
				+ " in ebegu. Please check that the system properties for username/password for the schulamt api are "
				+ "set");
			return setResponseUnauthorised(httpMsgContext);
		}
		Response response = null;
		try {
			String header = request.getHeader(COOKIE_AUTHORIZATION_HEADER);
			final String[] strings = BasicAuthHelper.parseHeader(header);

			if (strings == null || strings.length != 2) {
				// Basic Auth without username/password
				return setResponseUnauthorised(httpMsgContext);
			}

			final String scolarisGemeindeUsername = strings[0];
			final String scolarisGemeindePasswort = strings[1];

			Form form = new Form()
				.param("grant_type", "password")
				.param("username", scolarisGemeindeUsername)
				.param("password", scolarisGemeindePasswort);

			response = ClientBuilder.newClient()
				.target(this.keycloackAuthServer)
				.request(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON)
				.header(COOKIE_AUTHORIZATION_HEADER, BasicAuthHelper.createHeader(this.keycloackClient, this.keycloackPassword))
				.buildPost(Entity.form(form))
				.invoke();

			boolean validLogin = response.getStatus() == Status.OK.getStatusCode();
			return getAuthStatus(httpMsgContext, validLogin);
		} catch (RuntimeException e) {
			LOG.error("Call to Schulamt API had an unrecoverable error: {}", e.getMessage());
			throw e;
		} catch (Exception ex) {
			return setResponseUnauthorised(httpMsgContext);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nonnull
	private AuthStatus checkAuthorizationViaBasicAuth(
		@Nonnull HttpServletRequest request,
		@Nonnull HttpMsgContext httpMsgContext,
		@Nonnull String expectedUser,
		@Nonnull String expectedPassword) {

		String header = request.getHeader(COOKIE_AUTHORIZATION_HEADER);
		final String[] strings = BasicAuthHelper.parseHeader(header);

		if (strings != null && strings.length == 2) {
			final String username = strings[0];
			final String password = strings[1];

			boolean validLogin = username.equals(expectedUser) && password.equals(expectedPassword);
			return getAuthStatus(httpMsgContext, validLogin);
		}

		LOG.error("Call to {} without BasicAuth header credentials", request.getRequestURI());
		return setResponseUnauthorised(httpMsgContext);
	}

	private AuthStatus getAuthStatus(HttpMsgContext httpMsgContext, boolean validLogin) {
		if (validLogin) {
			//note: no actual container login is performed currently
			List<String> roles = new ArrayList<>();
			roles.add(UserRoleName.SUPER_ADMIN);
			MDC.put(Constants.LOG_MDC_EBEGUUSER, LOGINCONNECTOR_USER_USERNAME);
			return httpMsgContext.notifyContainerAboutLogin(LOGINCONNECTOR_USER_USERNAME, roles);
		}

		LOG.error("Call to connector api with invalid BasicAuth header credentials");
		return setResponseUnauthorised(httpMsgContext);
	}

	private void prepareLogvars(HttpMsgContext msgContext) {
		clearUserinfoFromLogvars();

		if (LOG.isDebugEnabled()) {
			AuthParameters authParameters = msgContext.getAuthParameters();
			if (authParameters != null && Boolean.FALSE.equals(authParameters.getNoPassword())) {
				LOG.debug("Username {}", authParameters.getUsername());
				LOG.debug("Password was passed in request");
			}
		}
	}

	private void clearUserinfoFromLogvars() {
		MDC.put(Constants.LOG_MDC_EBEGUUSER, "unknown");
		MDC.put(Constants.LOG_MDC_AUTHUSERID, "unknown");
		Sentry.getContext().setUser(null);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private boolean verifyXSFRHeader(HttpServletRequest request) {
		String xsrfTokenHeader = request.getHeader(AuthConstants.PARAM_XSRF_TOKEN);

		Cookie xsrfTokenCookie = AuthDataUtil.extractCookie(request.getCookies(), AuthConstants.COOKIE_XSRF_TOKEN);
		boolean isValidFileDownload = StringUtils.isEmpty(xsrfTokenHeader)
			&& xsrfTokenCookie != null
			&& RestUtil.isFileDownloadRequest(request);
		if (!request.getRequestURI().contains("/migration/")) { //migration ist ausgenommen
			if (!isValidFileDownload && !AuthDataUtil.isValidXsrfParam(xsrfTokenHeader, xsrfTokenCookie)) {
				LOG.debug("Could not match XSRF Token from Header and Cookie. Header:{} cookie {}", xsrfTokenHeader,
					xsrfTokenCookie);
				return false;
			}
		}
		return true;
	}

	private AuthStatus setResponseUnauthorised(HttpMsgContext httpMsgContext) {
		clearUserinfoFromLogvars();
		try {
			httpMsgContext.getResponse().sendError(SC_UNAUTHORIZED);
		} catch (IOException e) {
			String message = "Error when trying to send 401 back because of missing Authorization";
			throw new IllegalStateException(message, e);
		}
		return SEND_FAILURE;
	}

	private boolean isInternalApiActive() {
		return internalApiPassword != null && internalApiUser != null;
	}

	private boolean isSchulamtApiActive() {
		return keycloackClient != null && keycloackPassword != null && keycloackAuthServer != null;
	}
}

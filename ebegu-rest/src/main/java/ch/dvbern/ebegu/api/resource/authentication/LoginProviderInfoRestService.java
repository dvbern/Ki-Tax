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

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.Null;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.client.ClientRequestLogger;
import ch.dvbern.ebegu.api.client.ClientResponseLogger;
import ch.dvbern.ebegu.api.connector.clientinfo.ILoginProviderInfoResource;
import ch.dvbern.ebegu.api.resource.auth.LocalhostChecker;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.LogConsolidated;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.config.EbeguConfigurationImpl.EBEGU_LOGIN_PROVIDER_API_URL;

/**
 * Service managing a REST Client {@link ILoginProviderInfoResource} that can grab infos from the LoginProvider that is used
 */
@Stateless
public class LoginProviderInfoRestService {

	@SuppressWarnings("checkstyle:MemberName")
	private static final Logger LOG = LoggerFactory.getLogger(LoginProviderInfoRestService.class);
	public static final int CONNECTION_TIMEOUT = 10;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private LocalhostChecker localhostChecker;

	private ILoginProviderInfoResource loginProviderInfoRESTService;

	public String getSingleLogoutURL(String tenant) {
		return getLoginProviderInfoProxClient().getSingleLogoutURL(tenant);
	}

	public String getSSOLoginInitURL(@Nullable String relayPath, @Nullable String tenant) {
		if (!this.isConnectorEndpointSpecified()) {
			LOG.debug("No external Login connector specified, redirecting to locallogin");
			return AuthConstants.LOCALLOGIN_PATH;
		}
		return getLoginProviderInfoProxClient().getSSOLoginInitURL(relayPath, tenant);
	}

	public String pingLoginProvider() {
		return this.getLoginProviderInfoProxClient().getHeartBeat();
	}

	private ILoginProviderInfoResource getLoginProviderInfoProxClient() {
		if (loginProviderInfoRESTService == null) {
			String baseURL = determineConnectorApiBaseURL();
			ResteasyClient client = buildClient();
			ResteasyWebTarget target = client.target(baseURL);
			this.loginProviderInfoRESTService = target.proxy(ILoginProviderInfoResource.class);
			LOG.debug("Creating REST Proxy for Login Provider");
			try {
				final String responseMsg = loginProviderInfoRESTService.getHeartBeat();
				LOG.debug("version {}", responseMsg);
			} catch (RuntimeException ex) {
				LOG.error("Failure during REST client construction for Connector. Could not create client for URL '{}'. Check configuration ", baseURL, ex);
				throw ex;
			}

		}
		return loginProviderInfoRESTService;
	}

	private String determineConnectorApiBaseURL() {
		String baseURL = configuration.getLoginProviderAPIUrl();
		if (baseURL == null) {
			final String errMsg = "Can not construct LoginConnectorService because API-URI of connector is not specified via property. The required URI "
				+ "must be specified using the property " + EBEGU_LOGIN_PROVIDER_API_URL;
			throw new IllegalStateException(errMsg);
		}

		try {
			final String domainName = LocalhostChecker.getDomainName(baseURL);
			if ("localhost".equals(domainName) || "127.0.0.1".equals(domainName)) {
				String logmsg = String.format("Configured Connector API Url %s seems to be localhost. Since wildfly is only bound to the actual ip we try "
					+ "to replace localhost with the real ip of the server", baseURL);
				LogConsolidated.warning(LOG, Long.MAX_VALUE, logmsg , null );
				final String localIp = localhostChecker.findLocalIp();
				baseURL = LocalhostChecker.replaceHostInUrl(baseURL, localIp);
				LogConsolidated.warning(LOG, Long.MAX_VALUE, "Changed configured host for connector api to to " + baseURL , null );
			}
		} catch (URISyntaxException e) {
			LOG.error("Invalid configured connector API Url: '{}'", baseURL);
			throw new EbeguRuntimeException("determineConnectorApiBaseURL", "Could not parse url", ErrorCodeEnum.ERROR_INVALID_CONFIGURATION, e);
		}

		LOG.debug("Creating REST Client for URL {}", baseURL);
		return baseURL;
	}



	private boolean isConnectorEndpointSpecified() {
		return !StringUtils.isEmpty(configuration.getLoginProviderAPIUrl());
	}

	/**
	 * erstellt einen neuen ResteasyClient
	 */
	private ResteasyClient buildClient() {
		ResteasyClientBuilder builder = new ResteasyClientBuilder()
			.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);

		if (configuration.getIsDevmode() || LOG.isDebugEnabled()) {
			// wenn debug oder dev mode dann loggen wir den request
			builder.register(new ClientRequestLogger());
			builder.register(new ClientResponseLogger());
		}
		return builder.build();
	}
}


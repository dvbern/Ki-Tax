/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.util.health;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Checks if there is a connection to Keycloak
 */
@Health
@ApplicationScoped
public class DVKeycloakHealthCheck implements HealthCheck {

	private static final String NAME = "dv-keycloak-connection-check";
	private static final String OPENID_CONFIGURATION_API = "/realms/kibon/.well-known/openid-configuration";
	private static final String AUTH_PATH = "/auth";
	private static final Pattern AUTH_SERVER_PATTERN = Pattern.compile("(.+)" + AUTH_PATH + "(?:.*)");

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	private Client client = null;

	@PostConstruct
	public void postConstruct() {
		client = ClientBuilder.newClient();
	}

	@PreDestroy
	public void preDestroy() {
		client.close();
	}

	@Override
	@SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "Health Check reports reason")
	public HealthCheckResponse call() {
		String keycloackAuthServer = ebeguConfiguration.getKeycloackAuthServer();

		if (StringUtils.isBlank(keycloackAuthServer)) {
			return HealthCheckResponse.named(NAME).down().withData("reason", "AuthServer URL not configured").build();
		}

		HealthCheckResponseBuilder builder = HealthCheckResponse.named(NAME).up();
		try {
			String authServer = getAuthServerURL(keycloackAuthServer);
			Response response = client.target(authServer + OPENID_CONFIGURATION_API)
				.request(MediaType.APPLICATION_JSON)
				.get();

			boolean state = response.getStatus() == Status.OK.getStatusCode();

			return builder.state(state)
				.withData("response status", response.getStatus())
				.withData("AuthServer", authServer)
				.build();
		} catch (Exception e) {
			return builder.down().withData("reason", e.getMessage()).build();
		}
	}

	/**
	 * Das System Property geht leider direkt auf die openid-connect/token URL, z.B:
	 * https://DOMAIN/auth/realms/kibon/protocol/openid-connect/token, wir brauchen aber einen anderen endpoint.
	 *
	 * Der Regex hier unten liefert den Teil vor <strong>/auth</strong> zurück und kann auch mit einer URL umgehen,
	 * welche nur auf den Auth Server zeigt, also z.B. https://DOMAIN/auth
	 */
	@Nonnull
	private String getAuthServerURL(@Nonnull String keycloackAuthServer) {
		// das property geht leider direkt auf die openid-connect/token URL, statt nur auf den AuthServer...
		Matcher matcher = AUTH_SERVER_PATTERN.matcher(keycloackAuthServer);
		if (matcher.matches()) {
			return matcher.group(1) + AUTH_PATH;
		}

		return keycloackAuthServer;
	}
}

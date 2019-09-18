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

package ch.dvbern.ebegu.api.resource.auth;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heloer to check if a request originates from localhost
 */
@ApplicationScoped
public class LocalhostChecker {

	private static final Logger LOG = LoggerFactory.getLogger(LocalhostChecker.class.getSimpleName());
	private final Set<String> localAddresses = new HashSet<>();

	@Nullable
	private String localIp = null;

	@SuppressWarnings({ "OverlyBroadCatchBlock", "PMD.UnusedPrivateMethod" })
	@PostConstruct
	void init() {
		try {
			localAddresses.add(InetAddress.getLocalHost().getHostAddress());
			for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
				localAddresses.add(inetAddress.getHostAddress());
			}
		} catch (IOException e) {
			throw new EbeguRuntimeException("init localhost checker", "Unable to lookup local addresses", e);
		}
	}

	public boolean isAddressLocalhost(String localhost) {
		final boolean isLocalAccess = localAddresses.contains(localhost);

		if (!isLocalAccess) {
			final String localhosts = StringUtils.join(localAddresses, ';');
			LOG.warn("Access is not considered local. Local addesses are : '{}'", localhosts);
		}
		return isLocalAccess;

	}

	/**
	 * @return should return the ip of the machine that this jvm makes connections from
	 */
	public String findLocalIp() {
		if (this.localIp == null) {

			try (DatagramSocket socket = new DatagramSocket()) {
				socket.connect(InetAddress.getByName("8.8.8.8"), 10002); // connect to google dns to test our ip
				this.localIp = socket.getLocalAddress().getHostAddress();
			} catch (SocketException | UnknownHostException e) {
				throw new EbeguRuntimeException("findLocalIp", "Error while trying to determine the ip of the localhost",
					ErrorCodeEnum.ERROR_INVALID_CONFIGURATION, e);
			}
		}
		return this.localIp;
	}

	/**
	 * @return hostname of passed url
	 */
	public static String getDomainName(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	/**
	 * replaces the host part and maybe the port of a given url
	 *
	 * @param originalURL original url, may or may not connect port
	 * @param newHost new host, may optionally specify port in the form testhost:port
	 * @return the originalUrl where the host was replaced
	 */
	public static String replaceHostInUrl(String originalURL, String newHost) throws URISyntaxException {

		URI uri = new URI(originalURL);
		final int oldPort = uri.getPort();
		String newAuthority = newHost.contains(":") || oldPort == -1 ? newHost : newHost + ':' + oldPort;
		uri = new URI(uri.getScheme().toLowerCase(Locale.US), newAuthority,
			uri.getPath(), uri.getQuery(), uri.getFragment());

		return uri.toString();
	}
}

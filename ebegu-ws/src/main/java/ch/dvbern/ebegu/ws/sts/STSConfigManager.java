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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;

@Dependent
public class STSConfigManager {

	@Inject
	private EbeguConfiguration config;

	public String getEbeguSTSPrivateKeyAlias(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSPrivateKeyAlias() : config.getEbeguKibonAnfrageSTSPrivateKeyAlias();
	}

	public String getEbeguSTSPrivateKeyPW(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSPrivateKeyPW() : config.getEbeguKibonAnfrageSTSPrivateKeyPW();
	}

	public String getEbeguSTSKeystorePW(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSKeystorePW() : config.getEbeguKibonAnfrageSTSKeystorePW();
	}

	public String getEbeguSTSKeystorePath(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSKeystorePath() : config.getEbeguKibonAnfrageSTSKeystorePath();

	}

	public String getEbeguSTSEndpoint(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSEndpoint() : config.getEbeguKibonAnfrageSTSEndpoint();
	}

	public String getEbeguSTSWsdl(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSWsdl() : config.getEbeguKibonAnfrageSTSWsdl();
	}

	public String getEbeguSTSRenewalAssertionEndpoint(WebserviceType webserviceType) {
		return webserviceType.equals(WebserviceType.GERES) ? config.getEbeguPersonensucheSTSRenewalAssertionEndpoint() : config.getEbeguKibonAnfrageSTSRenewalAssertionEndpoint();
	}
}

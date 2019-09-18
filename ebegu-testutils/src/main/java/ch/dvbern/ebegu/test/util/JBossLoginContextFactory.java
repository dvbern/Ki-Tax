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
package ch.dvbern.ebegu.test.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.security.ClientLoginModule;
import org.jboss.security.SecurityContext;
import org.jboss.security.auth.spi.UsersRolesLoginModule;

/**
 * Provides a {@link LoginContext} for use by unit tests. It is driven by users.properties and roles.properties files as
 * described in <a href="https://community.jboss.org/wiki/UsersRolesLoginModule">UsersRolesLoginModule</a>
 */
public final class JBossLoginContextFactory {

	private JBossLoginContextFactory() {
		// util
	}

	static class NamePasswordCallbackHandler implements CallbackHandler {
		private final String username;
		private final String password;

		private NamePasswordCallbackHandler(String username, String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (Callback current : callbacks) {
				if (current instanceof NameCallback) {
					((NameCallback) current).setName(username);
				} else if (current instanceof PasswordCallback) {
					((PasswordCallback) current).setPassword(password.toCharArray());
				} else {
					throw new UnsupportedCallbackException(current);
				}
			}
		}
	}

	static class JBossJaasConfiguration extends Configuration {
		private final String configurationName;

		JBossJaasConfiguration(String configurationName) {
			this.configurationName = configurationName;
		}

		@Override
		public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
			if (!configurationName.equals(name)) {
				throw new IllegalArgumentException("Unexpected configuration name '" + name + '\'');
			}

			return new AppConfigurationEntry[] {

				createUsersRolesLoginModuleConfigEntry(),

				createClientLoginModuleConfigEntry(),

			};
		}

		/**
		 * The {@link UsersRolesLoginModule} creates the association between users and
		 * roles.
		 */
		private AppConfigurationEntry createUsersRolesLoginModuleConfigEntry() {
			Map<String, String> options = new HashMap<String, String>();
			return new AppConfigurationEntry("org.jboss.security.auth.spi.UsersRolesLoginModule",
				AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
		}

		/**
		 * The {@link ClientLoginModule} associates the user credentials with the
		 * {@link SecurityContext} where the JBoss security runtime can find it.
		 */
		private AppConfigurationEntry createClientLoginModuleConfigEntry() {
			Map<String, String> options = new HashMap<String, String>();
			options.put("multi-threaded", "true");
			options.put("restore-login-identity", "true");

			return new AppConfigurationEntry("org.jboss.security.ClientLoginModule",
				AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
		}
	}

	/**
	 * Obtain a LoginContext configured for use with the ClientLoginModule.
	 *
	 * @return the configured LoginContext.
	 */
	public static LoginContext createLoginContext(final String username, final String password) throws LoginException {
		final String configurationName = "Arquillian Testing";

		CallbackHandler cbh = new JBossLoginContextFactory.NamePasswordCallbackHandler(username, password);
		Configuration config = new JBossJaasConfiguration(configurationName);

		return new LoginContext(configurationName, new Subject(), cbh, config);
	}

}

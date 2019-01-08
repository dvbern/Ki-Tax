/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.validation;

import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import ch.dvbern.ebegu.validation.LocaleAwareMessageInterpolator;
import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.spi.validation.GeneralValidator;

@Provider
public class ValidationConfigurationContextResolver implements ContextResolver<GeneralValidator> {

	/**
	 * Get a context of type {@code GeneralValidator} that is applicable to the supplied type.
	 *
	 * @param type the class of object for which a context is desired
	 * @return a context for the supplied type or {@code null} if a context for the supplied type is not available from
	 *         this provider.
	 */
	@Override
	public GeneralValidator getContext(Class<?> type) {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();

		config.messageInterpolator(new LocaleAwareMessageInterpolator(config
			.getDefaultMessageInterpolator()));

		return new GeneralValidatorImpl(config.buildValidatorFactory(),
			bootstrapConfiguration.isExecutableValidationEnabled(),
			bootstrapConfiguration.getDefaultValidatedExecutableTypes());
	}
}

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

package ch.dvbern.ebegu.api.resource;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.services.I18nService;
import ch.dvbern.ebegu.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Resource for internationalization of the server
 */
@Path("i18n")
@Stateless
@Api(description = "Resource für Anträge (Erstgesuch oder Mutation)")
public class I18nResource {


	private static final Logger LOG = LoggerFactory.getLogger(I18nResource.class.getSimpleName());

	@Inject
	private I18nService i18nService;

	@ApiOperation("Changes the language to the given one")
	@Nonnull
	@PUT
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response update(
		@Nonnull String language,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		requireNonNull(language);

		Locale locale = Constants.DEFAULT_LOCALE;

		if (language.equalsIgnoreCase("fr")) {
			locale = Constants.FRENCH_LOCALE;

		} else if (language.equalsIgnoreCase("de")) {
			locale = Constants.DEUTSCH_LOCALE;

		} else {
			LOG.error("The selected language {} is not supported, default language (DE) used instead", language);
		}

		i18nService.changeLanguage(locale);

		return Response.ok().build();
	}
}

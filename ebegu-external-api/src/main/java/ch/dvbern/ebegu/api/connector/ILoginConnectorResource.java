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

package ch.dvbern.ebegu.api.connector;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.dtos.JaxBenutzerResponseWrapper;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthAccessElement;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthorisierterBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxExternalBenutzer;

@Path("/connector")
public interface ILoginConnectorResource {

	/**
	 * this service should be callable without authentication and can serve as a smoke test to see
	 * if the deploymentw as ok
	 */
	@GET
	@Path("/heartbeat")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	String getHeartBeat();

	/**
	 * Service to create or Update a Benutzer in Ki-TAX from an external login module. If the user is
	 * already found by its unique username we update the existing entry, otherwise we create a new one
	 *
	 * @param externalBenutzer User to update/store
	 * @return stored object
	 */
	@POST
	@Path("/benutzer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	JaxBenutzerResponseWrapper updateOrStoreBenutzer(@Nonnull JaxExternalBenutzer externalBenutzer);

	/**
	 * Service to update a Benutzer from an external login module. The main purpose is to handle users that have
	 * created a login in the external login system after being invited via email. The benutzerId was passed along
	 * in the email. Once the user has registered the connector calls this method and the state of the user is
	 * updated (from Eingeladen to Aktiv). The user must have used the same email, if not the system returens
	 * an errormessage in the responsewrapper
	 *
	 *
	 * @param benutzerId  The UserID serves as a unique identfier
	 * @param externalBenutzer User to update/store
	 * @return wrapper containing either the stored object or a translated error Message
	 */
	@PUT
	@Path("/benutzer/{benutzerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Nonnull
	JaxBenutzerResponseWrapper updateBenutzer(
		@Nonnull @NotNull @PathParam("benutzerId") String benutzerId,
		@Nonnull @NotNull @Valid JaxExternalBenutzer externalBenutzer);

	/**
	 * @return the first and only Mandant that currently exists
	 */
	@Nonnull
	@GET
	@Path("/mandant")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	String getMandant();

	/**
	 * This service exists to allow external login modules to create logins in Ki-Tax
	 *
	 * @param jaxExtAuthUser the login entry to create
	 * @return Object containing the information that is relevant for the Cookie
	 */
	@POST
	@Path("/extauth")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	JaxExternalAuthAccessElement createLogin(@Nonnull JaxExternalAuthorisierterBenutzer jaxExtAuthUser);
}

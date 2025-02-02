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

package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.authentication.LoginProviderInfoRestService;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.MandantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;
import java.util.Optional;

import static ch.dvbern.ebegu.api.resource.authentication.ConnectorUtil.toConnectorTenant;
import static ch.dvbern.ebegu.enums.UserRoleName.*;


/**
 * REST Resource fuer Gesuchsteller
 */
@Path("gesuchsteller")
@Stateless
@Api(description = "Resource für Gesuchsteller")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GesuchstellerResource {

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private ResourceHelper resourceHelper;

	@Inject
	private JaxBConverter converter;

	@Inject
	private MailService mailService;

	@Inject
	private LoginProviderInfoRestService loginProviderInfoRestService;

	@Inject
	private MandantService mandantService;

	@ApiOperation(value = "Updates a Gesuchsteller or creates it if it doesn't exist in the database. The transfer " +
		"object also has a relation to adressen (wohnadresse, umzugadresse, korrespondenzadresse, rechnungsadresse) " +
		"these are stored in the database as well. Note that wohnadresse and umzugadresse are both stored as consecutive " +
		"wohnadressen in the database. Umzugs flag wird gebraucht, um WizardSteps richtig zu setzen.",
		response = JaxGesuchstellerContainer.class)
	@Nullable
	@PUT
	@Path("/{gesuchId}/gsNumber/{gsNumber}/{umzug}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public JaxGesuchstellerContainer saveGesuchsteller(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchContJAXPId,
		@Nonnull @NotNull @PathParam("gsNumber") Integer gsNumber,
		@Nonnull @NotNull @PathParam("umzug") Boolean umzug,
		@Nonnull @NotNull @Valid JaxGesuchstellerContainer gesuchstellerJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch gesuch = gesuchService.findGesuch(gesuchContJAXPId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("createGesuchsteller", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchContJAXPId.getId()));

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		GesuchstellerContainer gesuchstellerToMerge = new GesuchstellerContainer();
		if (gesuchstellerJAXP.getId() != null) {
			Optional<GesuchstellerContainer> optional = gesuchstellerService.findGesuchsteller(gesuchstellerJAXP.getId());
			gesuchstellerToMerge = optional.orElse(new GesuchstellerContainer());
		}

		GesuchstellerContainer convertedGesuchsteller = converter.gesuchstellerContainerToEntity(gesuchstellerJAXP, gesuchstellerToMerge);
		GesuchstellerContainer persistedGesuchsteller = this.gesuchstellerService.saveGesuchsteller(convertedGesuchsteller, gesuch, gsNumber, umzug);

		return converter.gesuchstellerContainerToJAX(persistedGesuchsteller);
	}

	@ApiOperation(value = "Sucht den Gesuchsteller mit der uebergebenen Id in der Datenbank.",
		response = JaxGesuchstellerContainer.class)
	@Nullable
	@GET
	@Path("/id/{gesuchstellerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxGesuchstellerContainer findGesuchsteller(
		@Nonnull @NotNull @PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> optional = gesuchstellerService.findGesuchsteller(gesuchstellerID);

		if (!optional.isPresent()) {
			return null;
		}
		GesuchstellerContainer gesuchstellerToReturn = optional.get();

		return converter.gesuchstellerContainerToJAX(gesuchstellerToReturn);
	}

	@ApiOperation(value = "Send mail to provided email to init connecting GS with ZPV Nr from BE-Login.",
		response = JaxGesuchstellerContainer.class)
	@Nullable
	@GET
	@Path("/initZPVNr/{gesuchstellerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxGesuchstellerContainer initZPVNr(
			@Nonnull @QueryParam("email") @Email String email,
			@Nonnull @QueryParam("language") String korrespondenzSprache,
			@Nonnull @QueryParam("relayPath") String relayPath,
			@Nonnull @PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId,
			@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> optional = gesuchstellerService.findGesuchsteller(gesuchstellerID);


		if (optional.isEmpty()) {
			throw new EbeguEntityNotFoundException("initZPVNr", gesuchstellerID);
		}

		GesuchstellerContainer gesuchstellerContainer = optional.get();
		Mandant mandant;
		if (mandantCookie == null) {
			mandant = mandantService.getMandantBern();
		} else {
			mandant = mandantService.findMandantByCookie(mandantCookie);
		}

		String ssoLoginInitURL = this.loginProviderInfoRestService.getSSOLoginInitURL(relayPath, toConnectorTenant(mandant));

		mailService.sendInitGSZPVNr(ssoLoginInitURL, gesuchstellerContainer, email, korrespondenzSprache);

		return converter.gesuchstellerContainerToJAX(gesuchstellerContainer);
	}
}

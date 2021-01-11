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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxGemeindeAntrag;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeAntragService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Gemeindeantraege
 */
@Path("gemeindeantrag")
@Stateless
@Api(description = "Resource fuer Gemeindeantraege")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GemeindeAntragResource {

	@Inject
	private GemeindeAntragService gemeindeAntragService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation("Erstellt fuer jede aktive Gemeinde einen Gemeindeantrag des gewuenschten Typs fuer die gewuenschte Periode")
	@POST
	@Path("/create/{gemeindeAntragTyp}/gesuchsperiode/{gesuchsperiodeId}")
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public List<JaxGemeindeAntrag> createGemeindeAntrag(
		@Nonnull @Valid @PathParam("gemeindeAntragTyp") GemeindeAntragTyp gemeindeAntragTyp,
		@Nonnull @Valid @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) {
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeAntragTyp);

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).
			orElseThrow(() -> new EbeguEntityNotFoundException("createGemeindeAntrag", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsperiodeId));

		final List<GemeindeAntrag> gemeindeAntragList = gemeindeAntragService.createGemeindeAntrag(gesuchsperiode, gemeindeAntragTyp);
		return converter.gemeindeAntragListToJax(gemeindeAntragList);
	}

	@ApiOperation("Gibt alle Gemeindeanträge zurück, die die Benutzerin sehen kann")
	@GET
	@Path("")
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public List<JaxGemeindeAntrag> getAllGemeindeAntraege() {
		return converter.gemeindeAntragListToJax(
			(List<GemeindeAntrag>) gemeindeAntragService.getGemeindeAntraege()
		);
	}
}

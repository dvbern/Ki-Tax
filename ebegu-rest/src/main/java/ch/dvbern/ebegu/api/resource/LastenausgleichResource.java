/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleich;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.LastenausgleichService;
import ch.dvbern.ebegu.util.MathUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Lastenausgleiche
 */
@Path("lastenausgleich")
@Stateless
@Api(description = "Resource zum Verwalten von Lastenausgleichen")
public class LastenausgleichResource {

	@Inject
	private LastenausgleichService lastenausgleichService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Gibt alle Lastenausgleiche zurueck.", responseContainer = "List", response = JaxLastenausgleich.class)
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public List<JaxLastenausgleich> getAllLastenausgleiche() {
		return lastenausgleichService.getAllLastenausgleiche().stream()
			.map(lastenausgleich -> converter.lastenausgleichToJAX(lastenausgleich))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Erstellt einen neuen Lastenausgleich und speichert die Grundlagen", response = JaxLastenausgleich.class)
	@Nullable
	@GET
	@Path("/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public JaxLastenausgleich createLastenausgleich(
		@QueryParam("jahr") String sJahr,
		@QueryParam("selbstbehaltPro100ProzentPlatz") String sSelbstbehaltPro100ProzentPlatz
	) throws EbeguRuntimeException {

		int jahr = Integer.parseInt(sJahr);
		BigDecimal selbstbehaltPro100ProzentPlatz = MathUtil.DEFAULT.from(sSelbstbehaltPro100ProzentPlatz);

		Lastenausgleich lastenausgleich = lastenausgleichService.createLastenausgleich(jahr, selbstbehaltPro100ProzentPlatz);
		return converter.lastenausgleichToJAX(lastenausgleich);
	}
}

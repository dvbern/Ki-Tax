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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxFachstelle;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.FachstelleService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer Fachstellen
 */
@Path("fachstellen")
@Stateless
@Api(description = "Resource zur Verwaltung von Fachstellen")
@PermitAll // Alles oeffentliche Daten
public class FachstelleResource {

	@Inject
	private FachstelleService fachstelleService;

	@Inject
	private JaxBConverter converter;

	@Inject
	GesuchsperiodeService gesuchsperiodeService;


	@ApiOperation(value = "Returns Anspruch Fachstellen", responseContainer = "List", response = JaxFachstelle.class)
	@Nonnull
	@GET
	@Path("/anspruch")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxFachstelle> getAnspruchFachstellen(
			@NotNull @QueryParam("gesuchsperiodeId") String gesuchsperiodeId
	){
		Gesuchsperiode gesuchsperiode = findGesuchsperiodeFromIdOrThrow(gesuchsperiodeId);
		return fachstelleService.getAllFachstellen(gesuchsperiode.getMandant()).stream()
			.filter(Fachstelle::isFachstelleAnspruch)
			.filter(fachstelle -> fachstelle.isGueltigForGesuchsperiode(gesuchsperiode))
			.filter(fachstelle -> fachstelle.getName() != FachstelleName.KINDES_ERWACHSENEN_SCHUTZBEHOERDE)
			.map(ap -> converter.fachstelleToJAX(ap))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns erweiterte Betreuung Fachstellen", responseContainer = "List", response = JaxFachstelle.class)
	@Nonnull
	@GET
	@Path("/erweiterteBetreuung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxFachstelle> getErweiterteBetreuungFachstellen(
			@NotNull @QueryParam("gesuchsperiodeId") String gesuchsperiodeId
	){
		Gesuchsperiode gesuchsperiode = findGesuchsperiodeFromIdOrThrow(gesuchsperiodeId);
		return fachstelleService.getAllFachstellen(gesuchsperiode.getMandant()).stream()
			.filter(Fachstelle::isFachstelleErweiterteBetreuung)
			.filter(fachstelle -> fachstelle.isGueltigForGesuchsperiode(gesuchsperiode))
			.filter(fachstelle -> fachstelle.getName() != FachstelleName.KINDES_ERWACHSENEN_SCHUTZBEHOERDE)
			.map(ap -> converter.fachstelleToJAX(ap))
			.collect(Collectors.toList());
	}

	private Gesuchsperiode findGesuchsperiodeFromIdOrThrow(@NotNull String gesuchsperiodeId) {
		return gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(()
				-> new EbeguRuntimeException(
				"getAnspruchFachstellen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));
	}
}

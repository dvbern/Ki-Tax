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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDateRangedDTO;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer Gesuchsperiode
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Path("gesuchsperioden")
@Stateless
@Api(description = "Resource welche zum bearbeiten der Gesuchsperiode dient")
public class GesuchsperiodeResource {

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Erstellt eine neue Gesuchsperiode in der Datenbank", response = JaxGesuchsperiode.class)
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGesuchsperiode saveGesuchsperiode(
		@Nonnull @NotNull @Valid JaxGesuchsperiode gesuchsperiodeJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		if (gesuchsperiodeJAXP.getId() != null) {
			Optional<Gesuchsperiode> optional = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeJAXP.getId());
			gesuchsperiode = optional.orElseGet(Gesuchsperiode::new);
		}
		// Überprüfen, ob der Statusübergang zulässig ist
		GesuchsperiodeStatus gesuchsperiodeStatusBisher = gesuchsperiode.getStatus();

		Gesuchsperiode convertedGesuchsperiode = converter.gesuchsperiodeToEntity(gesuchsperiodeJAXP, gesuchsperiode);
		Gesuchsperiode persistedGesuchsperiode =
			this.gesuchsperiodeService.saveGesuchsperiode(convertedGesuchsperiode, gesuchsperiodeStatusBisher);

		return converter.gesuchsperiodeToJAX(persistedGesuchsperiode);
	}

	@ApiOperation(value = "Sucht die Gesuchsperiode mit der uebergebenen Id in der Datenbank",
		response = JaxGesuchsperiode.class)
	@Nullable
	@GET
	@Path("/gesuchsperiode/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGesuchsperiode findGesuchsperiode(
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId) {

		Objects.requireNonNull(gesuchsperiodeJAXPId.getId());
		String gesuchsperiodeID = converter.toEntityId(gesuchsperiodeJAXPId);
		Optional<Gesuchsperiode> optional = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeID);

		return optional.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode)).orElse(null);
	}

	@ApiOperation(value = "Gibt die neuste Gesuchsperiode zurueck anhand des Datums gueltigBis",
		response = JaxGesuchsperiode.class)
	@Nullable
	@GET
	@Path("/newestGesuchsperiode/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGesuchsperiode findNewestGesuchsperiode() {
		Optional<Gesuchsperiode> optional = gesuchsperiodeService.findNewestGesuchsperiode();
		return optional.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode)).orElse(null);
	}

	@ApiOperation("Loescht die Gesuchsperiode mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeGesuchsperiode(
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchsperiodeJAXPId.getId());
		gesuchsperiodeService.removeGesuchsperiode(converter.toEntityId(gesuchsperiodeJAXPId));
		return Response.ok().build();
	}

	@ApiOperation(value = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck.",
		responseContainer = "List", response = JaxGesuchsperiode.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllGesuchsperioden() {
		return gesuchsperiodeService.getAllGesuchsperioden().stream()
			.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb).reversed())
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck, welche im Status AKTIV "
		+ "sind",
		responseContainer = "List", response = JaxGesuchsperiode.class)
	@Nonnull
	@GET
	@Path("/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllActiveGesuchsperioden() {
		return gesuchsperiodeService.getAllActiveGesuchsperioden().stream()
			.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle in der Datenbank vorhandenen Gesuchsperioden zurueck, welche im Status AKTIV " +
		"oder INAKTIV sind", responseContainer = "List", response = JaxGesuchsperiode.class)
	@Nonnull
	@GET
	@Path("/unclosed")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllNichtAbgeschlosseneGesuchsperioden() {
		return gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden().stream()
			.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb).reversed())
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle Gesuchsperioden zurueck, die im Status AKTIV oder INAKTIV sind und für die der " +
		"angegebene Fall noch kein Gesuch freigegeben hat.",
		responseContainer = "List", response = JaxGesuchsperiode.class)
	@SuppressWarnings("InstanceMethodNamingConvention")
	@Nonnull
	@GET
	@Path("/unclosed/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllNichtAbgeschlosseneNichtVerwendeteGesuchsperioden(
		@Nonnull @PathParam("dossierId") String dossierId) {

		return gesuchsperiodeService.getAllNichtAbgeschlosseneNichtVerwendeteGesuchsperioden(dossierId).stream()
			.map(gesuchsperiode -> converter.gesuchsperiodeToJAX(gesuchsperiode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb).reversed())
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Gibt alle Gesuchsperioden zurück, welche AKTIV oder INAKTIV sind und nach dem " +
		"BetreuungsgutscheineStartdatum der Gemeinde liegen.",
		responseContainer = "List",
		response = JaxGesuchsperiode.class)
	@Nonnull
	@GET
	@Path("/gemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllPeriodenForGemeinde(
		@Nonnull @PathParam("gemeindeId") String gemeindeId,
		@Nullable @QueryParam("dossierId") String dossierId) {

		Collection<Gesuchsperiode> perioden = dossierId == null
			? gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden()
			: gesuchsperiodeService.getAllNichtAbgeschlosseneNichtVerwendeteGesuchsperioden(dossierId);

		return extractValidGesuchsperiodenForGemeinde(gemeindeId, perioden);
	}

	@ApiOperation(value = "Gibt alle Gesuchsperioden zurück, welche AKTIV sind und nach dem " +
		"BetreuungsgutscheineStartdatum der Gemeinde liegen.",
		responseContainer = "List",
		response = JaxGesuchsperiode.class)
	@Nonnull
	@GET
	@Path("/aktive/gemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGesuchsperiode> getAllAktivePeriodenForGemeinde(
		@Nonnull @PathParam("gemeindeId") String gemeindeId,
		@Nullable @QueryParam("dossierId") String dossierId) {

		Collection<Gesuchsperiode> perioden = dossierId == null
			? gesuchsperiodeService.getAllActiveGesuchsperioden()
			: gesuchsperiodeService.getAllAktiveNichtVerwendeteGesuchsperioden(dossierId);

		return extractValidGesuchsperiodenForGemeinde(gemeindeId, perioden);
	}

	private List<JaxGesuchsperiode> extractValidGesuchsperiodenForGemeinde(
		@Nonnull String gemeindeId,
		@Nonnull Collection<Gesuchsperiode> perioden
	) {
		LocalDate startdatum = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"extractValidGesuchsperiodenForGemeinde",
				String.format("Keine Gemeinde für ID %s", gemeindeId)))
			.getBetreuungsgutscheineStartdatum();

		return perioden.stream()
			.filter(periode -> periode.getGueltigkeit().endsAfterOrSame(startdatum))
			.map(periode -> converter.gesuchsperiodeToJAX(periode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb).reversed())
			.collect(Collectors.toList());
	}
}

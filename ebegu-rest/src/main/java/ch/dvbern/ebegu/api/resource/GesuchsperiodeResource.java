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

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDateRangedDTO;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Gesuchsperiode
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Path("gesuchsperioden")
@Stateless
@Api(description = "Resource welche zum bearbeiten der Gesuchsperiode dient")
@PermitAll
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
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

		requireNonNull(gesuchsperiodeJAXPId.getId());
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
	@RolesAllowed(SUPER_ADMIN)
	public Response removeGesuchsperiode(
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(gesuchsperiodeJAXPId.getId());
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
	public List<JaxGesuchsperiode> getAllAktivUndInaktivGesuchsperioden() {
		return gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden().stream()
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
	public List<JaxGesuchsperiode> getAllAktivInaktivNichtVerwendeteGesuchsperioden(
		@Nonnull @PathParam("dossierId") String dossierId) {

		return gesuchsperiodeService.getAllAktivInaktivNichtVerwendeteGesuchsperioden(dossierId).stream()
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
			? gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden()
			: gesuchsperiodeService.getAllAktivInaktivNichtVerwendeteGesuchsperioden(dossierId);

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

	@Nullable
	@DELETE
	@Path("/gesuchsperiodeDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeGesuchsperiodeDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response) {

		requireNonNull(gesuchsperiodeId);
		gesuchsperiodeService.removeGesuchsperiodeDokument(gesuchsperiodeId, sprache, dokumentTyp);
		return Response.ok().build();

	}

	@ApiOperation(value = "retuns true id the VerfuegungErlaeuterung exists for the given language",
		response = boolean.class)
	@GET
	@Path("/existDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public boolean existDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);
		return gesuchsperiodeService.existDokument(gesuchsperiodeId, sprache, dokumentTyp);
	}

	@ApiOperation("return the VerfuegungErlaeuterung for the given language")
	@GET
	@Path("/downloadGesuchsperiodeDokument/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadGesuchsperiodeDokument(
		@Nonnull @PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);

		final byte[] content = gesuchsperiodeService.downloadGesuchsperiodeDokument(gesuchsperiodeId, sprache, dokumentTyp);

		if (content != null && content.length > 0) {
			try {
				if(dokumentTyp.equals(DokumentTyp.ERLAUTERUNG_ZUR_VERFUEGUNG)) {
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(true, "erlaeuterung" + sprache + ".pdf",
						"application/octet-stream", content);
				}
				else if (dokumentTyp.equals(DokumentTyp.VORLAGE_MERKBLATT_TS)){
					//noinspection StringConcatenationMissingWhitespace
					return RestUtil.buildDownloadResponse(true, "vorlageMerkblattTS" + sprache + ".docx",
						"application/octet-stream", content);
				}
			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND)
					.entity("Gesuchsperiode Dokument: " + dokumentTyp.toString() + " kann nicht gelesen werden")
					.build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}

	private List<JaxGesuchsperiode> extractValidGesuchsperiodenForGemeinde(
		@Nonnull String gemeindeId,
		@Nonnull Collection<Gesuchsperiode> perioden
	) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"extractValidGesuchsperiodenForGemeinde",
				String.format("Keine Gemeinde für ID %s", gemeindeId)));

		return perioden.stream()
			.filter(gemeinde::isGesuchsperiodeRelevantForGemeinde)
			.map(periode -> converter.gesuchsperiodeToJAX(periode))
			.filter(periode -> periode.getGueltigAb() != null)
			.sorted(Comparator.comparing(JaxAbstractDateRangedDTO::getGueltigAb).reversed())
			.collect(Collectors.toList());
	}

}

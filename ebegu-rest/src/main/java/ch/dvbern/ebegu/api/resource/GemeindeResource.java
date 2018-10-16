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

import java.util.List;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.GemeindeJaxBConverter;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.api.resource.util.MultipartFormToFileConverter;
import ch.dvbern.ebegu.api.resource.util.TransferFile;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/**
 * Resource fuer Gemeinde
 */
@Path("gemeinde")
@Stateless
@Api(description = "Resource für Gemeinden")
public class GemeindeResource {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private GemeindeJaxBConverter gemeindeConverter;

	@ApiOperation(value = "Erstellt eine neue Gemeinde in der Datenbank", response = JaxTraegerschaft.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde createGemeinde(
		@Nonnull @NotNull @Valid JaxGemeinde gemeindeJAXP,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Nonnull @NotNull @Valid @QueryParam("date") String stringDateBeguBietenAb,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gemeinde convertedGemeinde = gemeindeConverter.gemeindeToEntity(gemeindeJAXP, new Gemeinde());

		Gemeinde persistedGemeinde = this.gemeindeService.createGemeinde(convertedGemeinde);

		final Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
			.orElseGet(() -> benutzerService.createAdminGemeindeByEmail(adminMail, persistedGemeinde));

		benutzer.getCurrentBerechtigung().getGemeindeList().add(persistedGemeinde);

		benutzerService.einladen(Einladung.forGemeinde(benutzer, persistedGemeinde));

		return gemeindeConverter.gemeindeToJAX(persistedGemeinde);
	}

	@ApiOperation(value = "Speichert eine Gemeinde in der Datenbank", response = JaxGemeinde.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde saveGemeinde(
		@Nonnull @NotNull @Valid JaxGemeinde gemeindeJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gemeinde gemeinde = Optional.ofNullable(gemeindeJAXP.getId())
			.flatMap(id -> gemeindeService.findGemeinde(id))
			.orElseGet(Gemeinde::new);

		Gemeinde convertedGemeinde = gemeindeConverter.gemeindeToEntity(gemeindeJAXP, gemeinde);
		Gemeinde persistedGemeinde = this.gemeindeService.saveGemeinde(convertedGemeinde);
		JaxGemeinde jaxGemeinde = gemeindeConverter.gemeindeToJAX(persistedGemeinde);

		return jaxGemeinde;
	}

	@ApiOperation(value = "Returns all Gemeinden", responseContainer = "Collection", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGemeinde> getAllGemeinden() {
		return gemeindeService.getAllGemeinden().stream()
			.map(gemeinde -> gemeindeConverter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns all Gemeinden with Status AKTIV",
		responseContainer = "Collection",
		response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxGemeinde> getAktiveGemeinden() {
		return gemeindeService.getAktiveGemeinden().stream()
			.map(gemeinde -> gemeindeConverter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns the Gemeinde with the given Id.", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/id/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde findGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);

		return gemeindeService.findGemeinde(gemeindeId)
			.map(gemeinde -> gemeindeConverter.gemeindeToJAX(gemeinde))
			.orElse(null);
	}

	@ApiOperation(value = "Returns the Gemeinde with the given name.", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/name/{gemeindeName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde findGemeindeByName(
		@Nonnull @NotNull @PathParam("gemeindeName") String name) {

		return gemeindeService.findGemeindeByName(name)
			.map(gemeinde -> gemeindeConverter.gemeindeToJAX(gemeinde))
			.orElse(null);
	}

	@ApiOperation(value = "Returns the GemeindeStammdaten with the given GemeindeId.", response = JaxGemeindeStammdaten.class)
	@Nullable
	@GET
	@Path("/stammdaten/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeindeStammdaten getGemeindeStammdaten(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);

		Optional<GemeindeStammdaten> stammdatenFromDB = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId);
		if (!stammdatenFromDB.isPresent()) {
			stammdatenFromDB = initGemeindeStammdaten(gemeindeId);
		}
		return stammdatenFromDB
			.map(stammdaten -> converter.gemeindeStammdatenToJAX(stammdaten))
			.orElse(null);
	}

	private Optional<GemeindeStammdaten> initGemeindeStammdaten(String gemeindeId) {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		Optional<Gemeinde> gemeinde = gemeindeService.findGemeinde(gemeindeId);
		stammdaten.setGemeinde(gemeinde.orElse(new Gemeinde()));
		stammdaten.setAdresse(getInitAdresse());
		stammdaten.setMail("");
		return Optional.of(stammdaten);
	}

	private Adresse getInitAdresse() {
		Adresse a = new Adresse();
		a.setStrasse("");
		a.setPlz("");
		a.setOrt("");
		return a;
	}

	@ApiOperation(value = "Speichert die GemeindeStammdaten", response = JaxGemeindeStammdaten.class)
	@Nullable
	@PUT
	@Path("/stammdaten")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeindeStammdaten saveGemeindeStammdaten(
		@Nonnull @NotNull @Valid JaxGemeindeStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		GemeindeStammdaten stammdaten;
		if (jaxStammdaten.getId() != null) {
			Optional<GemeindeStammdaten> optional = gemeindeService.getGemeindeStammdaten(jaxStammdaten.getId());
			stammdaten = optional.orElse(new GemeindeStammdaten());
		} else {
			stammdaten = new GemeindeStammdaten();
		}
		if (stammdaten.isNew()) {
			stammdaten.setAdresse(new Adresse());
		}
		GemeindeStammdaten convertedStammdaten = converter.gemeindeStammdatenToEntity(jaxStammdaten, stammdaten);

		// Statuswechsel
		if (stammdaten.getGemeinde().getStatus() == GemeindeStatus.EINGELADEN) {
			stammdaten.getGemeinde().setStatus(GemeindeStatus.AKTIV);
		}

		GemeindeStammdaten persistedStammdaten = gemeindeService.saveGemeindeStammdaten(convertedStammdaten);

		return converter.gemeindeStammdatenToJAX(persistedStammdaten);

	}

	@POST
	@Path("/logo/{gemeindeId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadLogo(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull MultipartFormDataInput input) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);

		Validate.notEmpty(fileList, "Need to upload something");

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		GemeindeStammdaten stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId).orElseThrow(
			() -> new EbeguEntityNotFoundException("uploadLogo", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId)
		);
		stammdaten.setLogoContent(fileList.get(0).getContent());
		gemeindeService.saveGemeindeStammdaten(stammdaten);
		return Response.ok().build();
	}
}

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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBfsGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxEinstellung;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeKonfiguration;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.api.resource.util.MultipartFormToFileConverter;
import ch.dvbern.ebegu.api.resource.util.TransferFile;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.MandantService;
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
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private MandantService mandantService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Erstellt eine neue Gemeinde in der Datenbank", response = JaxTraegerschaft.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeinde createGemeinde(
		@Nonnull @NotNull @Valid JaxGemeinde gemeindeJAXP,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gemeinde convertedGemeinde = converter.gemeindeToEntity(gemeindeJAXP, new Gemeinde());

		Gemeinde persistedGemeinde = this.gemeindeService.createGemeinde(convertedGemeinde);

		// Aufgrund der Flags entscheiden, in welcher Rolle der Admin eingeladen werden soll
		UserRole roleOfAdmin = getUserRoleForGemeindeAdmin(persistedGemeinde);
		final Benutzer benutzer = benutzerService.findBenutzerByEmail(adminMail)
			.orElseGet(() -> benutzerService.createAdminGemeindeByEmail(adminMail, roleOfAdmin, persistedGemeinde));

		benutzer.getCurrentBerechtigung().getGemeindeList().add(persistedGemeinde);

		benutzerService.einladen(Einladung.forGemeinde(benutzer, persistedGemeinde));

		return converter.gemeindeToJAX(persistedGemeinde);
	}

	private UserRole getUserRoleForGemeindeAdmin(@Nonnull Gemeinde gemeinde) {
		boolean hasBG = gemeinde.isAngebotBG();
		boolean hasTS = gemeinde.isAngebotTS() || gemeinde.isAngebotFI();
		if (!hasBG) {
			return UserRole.ADMIN_TS;
		}
		if (!hasTS) {
			return UserRole.ADMIN_BG;
		}
		return UserRole.ADMIN_GEMEINDE;
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

		Gemeinde convertedGemeinde = converter.gemeindeToEntity(gemeindeJAXP, gemeinde);
		Gemeinde persistedGemeinde = this.gemeindeService.saveGemeinde(convertedGemeinde);
		JaxGemeinde jaxGemeinde = converter.gemeindeToJAX(persistedGemeinde);

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
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
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
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
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
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.orElse(null);
	}

	@ApiOperation(value = "Returns the GemeindeStammdaten with the given GemeindeId.",
		response = JaxGemeindeStammdaten.class)
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

		// Konfiguration
		// Die Konfiguratoin kann bearbeitet werden, bis die Periode geschlossen ist.
		boolean eingeladen = GemeindeStatus.EINGELADEN == jaxStammdaten.getGemeinde().getStatus();
		jaxStammdaten.getKonfigurationsListe().forEach(konfiguration -> {
			if (eingeladen) {
				// KIBON-360: die Konfiguration in der aktuellen und in allen zukünftigen Gesuchsperioden speichern
				saveAllFutureJaxGemeindeKonfiguration(stammdaten.getGemeinde(), konfiguration);
			} else if (GesuchsperiodeStatus.GESCHLOSSEN != konfiguration.getGesuchsperiode().getStatus()) {
				saveJaxGemeindeKonfiguration(stammdaten.getGemeinde(), konfiguration);
			}
		});

		// Statuswechsel
		if (convertedStammdaten.getGemeinde().getStatus() == GemeindeStatus.EINGELADEN) {
			convertedStammdaten.getGemeinde().setStatus(GemeindeStatus.AKTIV);
		}

		GemeindeStammdaten persistedStammdaten = gemeindeService.saveGemeindeStammdaten(convertedStammdaten);

		return converter.gemeindeStammdatenToJAX(persistedStammdaten);

	}

	private void saveJaxGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull JaxGemeindeKonfiguration konfiguration
	) {
		Objects.requireNonNull(konfiguration);
		Objects.requireNonNull(konfiguration.getGesuchsperiode());
		Objects.requireNonNull(konfiguration.getGesuchsperiode().getId());

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(konfiguration.getGesuchsperiode().getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveJaxGemeindeKonfiguration", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		for (JaxEinstellung jaxKonfig : konfiguration.getKonfigurationen()) {
			saveEinstellung(gemeinde, gesuchsperiode, jaxKonfig);
		}
	}

	private void saveAllFutureJaxGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull JaxGemeindeKonfiguration konfiguration
	) {
		Objects.requireNonNull(konfiguration);
		Objects.requireNonNull(konfiguration.getGesuchsperiode());
		Objects.requireNonNull(konfiguration.getGesuchsperiode().getId());

		Collection<Gesuchsperiode> gesuchsperioden =
			gesuchsperiodeService.findThisAndFutureGesuchsperioden(konfiguration.getGesuchsperiode().getId());

		if (gesuchsperioden != null && !gesuchsperioden.isEmpty()) {
			for (Gesuchsperiode gesuchsperiode : gesuchsperioden) {
				for (JaxEinstellung jaxKonfig : konfiguration.getKonfigurationen()) {
					saveEinstellung(gemeinde, gesuchsperiode, jaxKonfig);
				}
			}
		}
	}

	private void saveEinstellung(@Nonnull Gemeinde gemeinde, Gesuchsperiode gesuchsperiode, JaxEinstellung jaxKonfig) {
		Einstellung einstellung = einstellungService.findEinstellung(jaxKonfig.getKey(), gemeinde, gesuchsperiode);
		if (!gemeinde.equals(einstellung.getGemeinde()) || !gesuchsperiode.equals(einstellung.getGesuchsperiode())) {
			einstellung = new Einstellung();
			einstellung.setKey(jaxKonfig.getKey());
			einstellung.setGemeinde(gemeinde);
			einstellung.setGesuchsperiode(gesuchsperiode);
		}
		einstellung.setValue(jaxKonfig.getValue());
		einstellungService.saveEinstellung(einstellung);
	}

	@ApiOperation("Stores the logo image of the Gemeinde with the given id")
	@POST
	@Path("/logo/data/{gemeindeId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadLogo(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull MultipartFormDataInput input) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);

		Validate.notEmpty(fileList, "Need to upload something");

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);

		TransferFile file = fileList.get(0);
		gemeindeService.uploadLogo(gemeindeId, file.getContent(), file.getFilename(), file.getFiletype());

		return Response.ok().build();
	}

	@ApiOperation("Returns the logo image of the Gemeinde with the given id or an errorcode if none is available")
	@GET
	@Path("/logo/data/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadLogo(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Optional<GemeindeStammdaten> stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId);
		if (stammdaten.isPresent()) {
			try {
				String name = stammdaten.get().getLogoName();
				String type = stammdaten.get().getLogoType();
				return RestUtil.buildDownloadResponse(false, name == null ? "logo" : name,
					type == null ? "image/*" : type, stammdaten.get().getLogoContent());
			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND).entity("Logo kann nicht gelesen werden").build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}

	@ApiOperation(value = "Returns all unregistered Gemeinden from BFS", responseContainer = "Collection",
		response = JaxBfsGemeinde.class)
	@Nullable
	@GET
	@Path("/unregistered")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxBfsGemeinde> getUnregisteredBfsGemeinden() {
		Mandant bern = mandantService.getFirst(); //TODO (later) Change to real mandant!
		return gemeindeService.getUnregisteredBfsGemeinden(bern).stream()
			.map(gemeinde -> converter.gemeindeBfsToJax(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns true, if the currently logged in Benutzer has any Gemeinden in Status EINGELADEN",
		response = Boolean.class)
	@Nonnull
	@GET
	@Path("/hasEinladungen/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response hasGemeindenInStatusEingeladenForCurrentBenutzer() {
		final Benutzer benutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"hasGemeindenInStatusEingeladenForCurrentBenutzer",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		Collection<Gemeinde> gemeindeList = new HashSet<>();
		if (benutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			gemeindeList = benutzer.getCurrentBerechtigung().getGemeindeList();
		} else if (benutzer.getCurrentBerechtigung().getRole() == UserRole.SUPER_ADMIN) {
			gemeindeList = gemeindeService.getAllGemeinden();
		}
		long anzahl = gemeindeList.stream()
			.filter(inst -> inst.getStatus() == GemeindeStatus.EINGELADEN)
			.count();
		return Response.ok(anzahl > 0).build();
	}
}

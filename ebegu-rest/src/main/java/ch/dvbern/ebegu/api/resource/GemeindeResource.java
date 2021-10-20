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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBfsGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxEinstellung;
import ch.dvbern.ebegu.api.dtos.JaxExternalClientAssignment;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeKonfiguration;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeRegistrierung;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.MultipartFormToFileConverter;
import ch.dvbern.ebegu.api.resource.util.TransferFile;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.BfsGemeinde;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.ExternalClientService;
import ch.dvbern.ebegu.services.FerieninselStammdatenService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.Constants;
import com.lowagie.text.Image;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Resource fuer Gemeinde
 */
@Path("gemeinde")
@Stateless
@Api(description = "Resource für Gemeinden")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
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
	private ExternalClientService externalClientService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	@ApiOperation(value = "Erstellt eine neue Gemeinde in der Datenbank", response = JaxGemeinde.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
		//This line is weird I think its already done in the create methods
		benutzer.getCurrentBerechtigung().getGemeindeList().add(persistedGemeinde);

		benutzerService.einladen(Einladung.forGemeinde(benutzer, persistedGemeinde));

		// if Ferieninsel is active, we need to initialize the Ferien
		if (persistedGemeinde.isAngebotFI()) {
			gesuchsperiodeService.getAllGesuchsperioden()
				.forEach(gp -> initFerieninselnForGemeindeAndGesuchsperiode(persistedGemeinde, gp));
		}

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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
	@PermitAll // Oeffentliche Daten
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
	@PermitAll // Oeffentliche Daten
	public List<JaxGemeinde> getAktiveGemeinden(
			@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie
	) {
		AtomicReference<Mandant> mandant = new AtomicReference<>(mandantService.getDefaultMandant());
		mandantService.findMandantByName(mandantCookie.getValue()).ifPresent(mandant::set);
		return gemeindeService.getAktiveGemeinden().stream()
				//TODO MANDANTEN: do this in getAktiveGemeinden
			.filter(gemeinde -> gemeinde.getMandant() != null && gemeinde.getMandant().equals(mandant.get()))
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}


	@ApiOperation(value = "Returns all Gemeinden with Status AKTIV and gueltigBis after the current day",
		responseContainer = "Collection",
		response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/activegueltig")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGemeinde> getAktiveGueltigeGemeinden() {
		return gemeindeService.getAktiveGemeindenGueltigAm(LocalDate.now()).stream()
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns the Gemeinde with the given Id.", response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/id/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
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
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
			Objects.requireNonNull(konfiguration.getGesuchsperiode());
			if (eingeladen) {
				// KIBON-360: die Konfiguration in der aktuellen und in allen zukünftigen Gesuchsperioden speichern
				saveAllFutureJaxGemeindeKonfiguration(stammdaten.getGemeinde(), konfiguration);
			} else if (GesuchsperiodeStatus.GESCHLOSSEN != konfiguration.getGesuchsperiode().getStatus()) {
				saveJaxGemeindeKonfiguration(stammdaten.getGemeinde(), konfiguration);
			}

			saveFerieninseln(konfiguration.getFerieninselStammdaten(), stammdaten.getGemeinde());
		});

		// Statuswechsel
		if (convertedStammdaten.getGemeinde().getStatus() == GemeindeStatus.EINGELADEN) {
			convertedStammdaten.getGemeinde().setStatus(GemeindeStatus.AKTIV);
		}

		// ExternalCleints updaten:
		if (jaxStammdaten.getExternalClients() != null) {
			Collection<ExternalClient> availableClients = externalClientService.getAllForGemeinde();
			availableClients.removeIf(client -> !jaxStammdaten.getExternalClients().contains(client.getId()));
			convertedStammdaten.setExternalClients(new HashSet<>(availableClients));
		}

		GemeindeStammdaten persistedStammdaten = gemeindeService.saveGemeindeStammdaten(convertedStammdaten);

		return converter.gemeindeStammdatenToJAX(persistedStammdaten);

	}

	private void saveJaxGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull JaxGemeindeKonfiguration konfiguration
	) {
		requireNonNull(konfiguration);
		requireNonNull(konfiguration.getGesuchsperiode());
		requireNonNull(konfiguration.getGesuchsperiode().getId());

		Gesuchsperiode gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(konfiguration.getGesuchsperiode().getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("saveJaxGemeindeKonfiguration",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		for (JaxEinstellung jaxKonfig : konfiguration.getKonfigurationen()) {
			saveEinstellung(gemeinde, gesuchsperiode, jaxKonfig);
		}
	}

	private void saveAllFutureJaxGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull JaxGemeindeKonfiguration konfiguration
	) {
		requireNonNull(konfiguration);
		requireNonNull(konfiguration.getGesuchsperiode());
		requireNonNull(konfiguration.getGesuchsperiode().getId());

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

	private void initFerieninselnForGemeindeAndGesuchsperiode(Gemeinde gemeinde, Gesuchsperiode gp) {
		Optional<GemeindeStammdatenGesuchsperiode> gemeindeStammdatenGesuchsperiodeOpt =
			gemeindeService.findGemeindeStammdatenGesuchsperiode(
				gemeinde.getId(),
				gp.getId()
			);
		GemeindeStammdatenGesuchsperiode gsgp = null;
		if (!gemeindeStammdatenGesuchsperiodeOpt.isPresent()) {
			gsgp = gemeindeService.saveGemeindeStammdatenGesuchsperiode(
				gemeindeService.createGemeindeStammdatenGesuchsperiode(
					gemeinde.getId(),
					gp.getId()
				)
			);
		} else {
			gsgp = gemeindeStammdatenGesuchsperiodeOpt.get();
		}

		ferieninselStammdatenService.initFerieninselStammdaten(gsgp);
	}

	private void removeFerieninselnForGemeindeAndGesuchsperiode(Gemeinde gemeinde, Gesuchsperiode gp) {
		Optional<GemeindeStammdatenGesuchsperiode> gemeindeStammdatenGesuchsperiodeOpt =
			gemeindeService.findGemeindeStammdatenGesuchsperiode(
				gemeinde.getId(),
				gp.getId()
			);

		if (gemeindeStammdatenGesuchsperiodeOpt.isPresent()) {

			GemeindeStammdatenGesuchsperiode gsgp = gemeindeStammdatenGesuchsperiodeOpt.get();

			if (gsgp.getGemeindeStammdatenGesuchsperiodeFerieninseln() != null
				&& !gsgp.getGemeindeStammdatenGesuchsperiodeFerieninseln().isEmpty()) {
				gsgp.getGemeindeStammdatenGesuchsperiodeFerieninseln()
					.forEach(stammdaten -> ferieninselStammdatenService.removeFerieninselStammdaten(stammdaten.getId()));
				gsgp.getGemeindeStammdatenGesuchsperiodeFerieninseln().clear();
			}
		}
	}

	private void saveFerieninseln(List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> jaxFiStammdaten,
		Gemeinde gemeinde) {
		if (gemeinde.isAngebotFI()) {
			Objects.requireNonNull(jaxFiStammdaten);
			for (JaxGemeindeStammdatenGesuchsperiodeFerieninsel fiStammdaten : jaxFiStammdaten) {
				Objects.requireNonNull(fiStammdaten.getId());
				GemeindeStammdatenGesuchsperiodeFerieninsel fiStammdatenFromDB =
					ferieninselStammdatenService.findFerieninselStammdaten(fiStammdaten.getId()).orElseThrow(() ->
						new EbeguEntityNotFoundException("saveFerieninseln", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND)
					);
				ferieninselStammdatenService.saveFerieninselStammdaten(
					converter.ferieninselStammdatenToEntity(fiStammdaten,
						fiStammdatenFromDB)
				);
			}
		}
	}

	@ApiOperation("Stores the logo image of the Gemeinde with the given id")
	@POST
	@Path("/logo/data/{gemeindeId}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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

	@ApiOperation("Checks if it isa supported image")
	@POST
	@Path("/supported/image")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentlich
	public Response isSupportedImage(@Nonnull @NotNull MultipartFormDataInput input) {
		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);
		Validate.notEmpty(fileList, "Need to upload something");
		TransferFile file = fileList.get(0);
		try {
			Image.getInstance(file.getContent());
		} catch (IOException exception) {
			throw new EbeguRuntimeException("isSupportedImage", ErrorCodeEnum.ERROR_NOT_SUPPORTED_IMAGE, exception);
		}
		return Response.ok().build();
	}

	@ApiOperation("Returns the logo image of the Gemeinde with the given id or an errorcode if none is available")
	@GET
	@Path("/logo/data/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@PermitAll // Oeffentliche Daten
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBfsGemeinde> getUnregisteredBfsGemeinden() {
		Mandant bern = mandantService.getFirst(); //TODO (later) Change to real mandant!
		return gemeindeService.getUnregisteredBfsGemeinden(bern).stream()
			.map(gemeinde -> converter.gemeindeBfsToJax(gemeinde))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Returns all unregistered Gemeinden from BFS", responseContainer = "Collection",
		response = JaxBfsGemeinde.class)
	@Nullable
	@GET
	@Path("/allBfs")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxBfsGemeinde> getAllBfsGemeinden() {
		return gemeindeService.getAllBfsGemeinden().stream()
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
	@PermitAll
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

	@ApiOperation(value = "Returns GemeindeVerbund when already registred", responseContainer = "Collection",
		response = JaxBfsGemeinde.class)
	@Nullable
	@GET
	@Path("/gemeindeRegistrierung/{gemeindeId}/{gemeindenTSId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Fuer Registrierungsprozess
	public List<JaxGemeindeRegistrierung> getGemeindenRegistrierung(@Nullable @PathParam("gemeindeId") JaxId gemeindeBGJAXPId,
		@Nullable @PathParam("gemeindenTSId") String gemeindenTSIdList) {
		List<JaxGemeindeRegistrierung> gemeindeRegistrierungList = new ArrayList<>();

		// BG-Gemeinde: Es kann nur 1 geben. Falls sie gesetzt ist, darf sie für TS nicht ein zweites Mal hinzugefügt
		// werden
		String gemeindeBGId = null;

		if (gemeindeBGJAXPId != null) {
			gemeindeBGId = converter.toEntityId(gemeindeBGJAXPId);
			if (!gemeindeBGId.equals("null")) {
				String finalGemeindeBGId = gemeindeBGId;
				Gemeinde gemeindeBG =
					gemeindeService.findGemeinde(gemeindeBGId).orElseThrow(() -> new EbeguEntityNotFoundException(
						"findGemeinde",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, finalGemeindeBGId));

				JaxGemeindeRegistrierung gemeindeRegistrierungBG = new JaxGemeindeRegistrierung();
				gemeindeRegistrierungBG.setId(gemeindeBGId);
				gemeindeRegistrierungBG.setName(gemeindeBG.getName());
				gemeindeRegistrierungList.add(gemeindeRegistrierungBG);
			}
		}

		if (gemeindenTSIdList != null && !gemeindenTSIdList.equals("null")) {
			String[] gemeindenTSList = gemeindenTSIdList.split(",");
			for (String gemeindeTSId : gemeindenTSList) {
				Gemeinde gemeindeTSVerbund;
				String gemeindeName;
				//Wir pruefen ob der ID ist einen UUID, sonst es ist einen BFS Nummer und der Gemeinde existiert noch
				// nicht so es heisst das es einen Kind von einen Schulverbund ist
				if (gemeindeTSId.matches(Constants.REGEX_UUID)) {
					Gemeinde gemeindeTS =
						gemeindeService.findGemeinde(gemeindeTSId).orElseThrow(() -> new EbeguEntityNotFoundException(
							"findGemeinde",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeTSId));
					gemeindeTSVerbund =
						gemeindeService.findRegistredGemeindeVerbundIfExist(gemeindeTS.getBfsNummer()).orElse(null);
					gemeindeName = gemeindeTS.getName();
				} else {
					BfsGemeinde bfsGemeinde =
						gemeindeService.findBfsGemeinde(Long.parseLong(gemeindeTSId)).orElseThrow(() -> new EbeguEntityNotFoundException(
							"findBfsGemeinde",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeTSId));
					gemeindeTSVerbund =
						gemeindeService.findRegistredGemeindeVerbundIfExist(bfsGemeinde.getBfsNummer()).orElseThrow(() -> new EbeguEntityNotFoundException(
							"findRegistredGemeindeVerbundIfExist",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, bfsGemeinde.getBfsNummer()));
					gemeindeName = bfsGemeinde.getName();
				}

				// Innerhalb der TS-Gemeinden kann es keine Duplikate haben. Es kann aber sein, dass eine Gemeinde
				// sowohl für BG wie auch
				// für TS ausgewählt wurde. Fall diese Gemeinde für TS keinem Verbund angehört, muss sie nicht ein
				// zweites Mal hinzugefügt werden
				if (gemeindeBGId != null && gemeindeBGId.equals(gemeindeTSId) && gemeindeTSVerbund == null) {
					continue;
				}

				JaxGemeindeRegistrierung gemeindeRegistrierungTS = new JaxGemeindeRegistrierung();
				gemeindeRegistrierungTS.setId(gemeindeTSId);
				gemeindeRegistrierungTS.setName(gemeindeName);
				if (gemeindeTSVerbund != null && gemeindeTSVerbund.isAngebotTS()) {
					gemeindeRegistrierungTS.setVerbundId(gemeindeTSVerbund.getId());
					gemeindeRegistrierungTS.setVerbundName(gemeindeTSVerbund.getName());
				}
				gemeindeRegistrierungList.add(gemeindeRegistrierungTS);
			}
		}
		return gemeindeRegistrierungList;
	}

	@ApiOperation(value = "Returns all Gemeinden with Status AKTIV and all Gemeinde von Schulverbund",
		responseContainer = "Collection",
		response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/activeAndSchulverbund")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Fuer Registrierungsprozess
	public List<JaxGemeinde> getAktiveUndSchulverbundGemeinden() {
		List<JaxGemeinde> aktiveGemeinde = gemeindeService.getAktiveGemeindenGueltigAm(LocalDate.now()).stream()
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
		List<JaxGemeinde> aktiveUndSchulverbundGemeinden = new ArrayList<>(aktiveGemeinde);
		for (JaxGemeinde jaxGemeinde : aktiveGemeinde) {
			if (BfsGemeinde.isBfsNummerVerbund(jaxGemeinde.getBfsNummer()) && jaxGemeinde.isAngebotTS()) {
				List<BfsGemeinde> bfsGemeindeList = gemeindeService.findGemeindeVonVerbund(jaxGemeinde.getBfsNummer());
				bfsGemeindeList.forEach(bfsGemeinde -> {
					boolean isAlreadyVorhanden =
						aktiveUndSchulverbundGemeinden.stream().anyMatch(aktiveJaxGemeinde -> aktiveJaxGemeinde.getBfsNummer().equals(bfsGemeinde.getBfsNummer()));
					if (!isAlreadyVorhanden) {
						JaxGemeinde vonSchulVerbund = new JaxGemeinde();
						vonSchulVerbund.setKey(String.valueOf(bfsGemeinde.getBfsNummer()));
						vonSchulVerbund.setId(bfsGemeinde.getId());
						vonSchulVerbund.setBfsNummer(bfsGemeinde.getBfsNummer());
						vonSchulVerbund.setName(bfsGemeinde.getName());
						vonSchulVerbund.setAngebotBG(false);
						vonSchulVerbund.setAngebotTS(true);
						aktiveUndSchulverbundGemeinden.add(vonSchulVerbund);
					}
				});
			}
		}
		return aktiveUndSchulverbundGemeinden;
	}

	@ApiOperation(
		value = "Updated die Angebot Flags (BG/TS/FI) der Gemeinde",
		response = Response.class)
	@Nonnull
	@PUT
	@Path("/updateangebote")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response updateAngebot(
		@Nonnull @NotNull @Valid JaxGemeinde jaxGemeinde
	) {
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getId());
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		requireNonNull(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		requireNonNull(jaxGemeinde.getFerieninselanmeldungenStartdatum());

		Gemeinde gemeinde =
			gemeindeService.findGemeinde(jaxGemeinde.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
				"updateAngebotTS", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, jaxGemeinde.getId()));
		boolean datesChanged = false;
		if (!gemeinde.getBetreuungsgutscheineStartdatum().isEqual(jaxGemeinde.getBetreuungsgutscheineStartdatum())) {
			gemeinde.setBetreuungsgutscheineStartdatum(jaxGemeinde.getBetreuungsgutscheineStartdatum());
			datesChanged = true;
		}
		if (!gemeinde.getTagesschulanmeldungenStartdatum().equals(jaxGemeinde.getTagesschulanmeldungenStartdatum())) {
			gemeinde.setTagesschulanmeldungenStartdatum(jaxGemeinde.getTagesschulanmeldungenStartdatum());
			datesChanged = true;
		}
		if (!gemeinde.getFerieninselanmeldungenStartdatum().equals(jaxGemeinde.getFerieninselanmeldungenStartdatum())) {
			gemeinde.setFerieninselanmeldungenStartdatum(jaxGemeinde.getFerieninselanmeldungenStartdatum());
			datesChanged = true;
		}
		if (!gemeinde.getGueltigBis().equals(jaxGemeinde.getGueltigBis())) {
			gemeinde.setGueltigBis(jaxGemeinde.getGueltigBis());
			datesChanged = true;
		}
		if (datesChanged) {
			gemeinde = gemeindeService.saveGemeinde(gemeinde);
		}
		if (gemeinde.isAngebotBG() != jaxGemeinde.isAngebotBG()) {
			gemeindeService.updateAngebotBG(gemeinde, jaxGemeinde.isAngebotBG());
		}
		if (gemeinde.isAngebotTS() != jaxGemeinde.isAngebotTS()) {
			gemeindeService.updateAngebotTS(gemeinde, jaxGemeinde.isAngebotTS());
		}
		if (gemeinde.isAngebotFI() != jaxGemeinde.isAngebotFI()) {
			gemeindeService.updateAngebotFI(gemeinde, jaxGemeinde.isAngebotFI());

			handleFIAngebotChange(jaxGemeinde.isAngebotFI(), gemeinde);
		}

		return Response.ok().build();
	}

	private void handleFIAngebotChange(boolean isAngebotFI, final Gemeinde gemeinde) {
		if (isAngebotFI) {
			gesuchsperiodeService.getAllGesuchsperioden()
				.forEach(gp -> initFerieninselnForGemeindeAndGesuchsperiode(gemeinde, gp));
		} else {
			gesuchsperiodeService.getAllGesuchsperioden()
				.forEach(gp -> removeFerieninselnForGemeindeAndGesuchsperiode(gemeinde, gp));
		}
	}

	@ApiOperation("Returns a specific document of the Gemeinde with the given type or an errorcode if none is "
		+ "available")
	@GET
	@Path("/gemeindeGesuchsperiodeDoku/{gemeindeId}/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, GESUCHSTELLER, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response downloadGemeindeDokument(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp
	) {

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJAXPId);

		// wir schauen welche Sprache sind bei der Gemeinde konfiguriert, falls nur eine ist der Dokument in dieser
		// Sprache zurueckgegeben
		GemeindeStammdaten gemeindeStammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"GemeindeStammdaten",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		Sprache[] korrespondenzsprachen = gemeindeStammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length == 1) {
			sprache = korrespondenzsprachen[0];
		}

		final byte[] content = gemeindeService.downloadGemeindeGesuchsperiodeDokument(gemeindeId, gesuchsperiodeId,
			sprache, dokumentTyp);
		if (content != null && content.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(true, "vorlageMerkblattTS" + sprache + ".pdf",
					"application/octet-stream", content);
			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND).entity("Logo kann nicht gelesen werden").build();
			}
		}
		return Response.status(Status.NO_CONTENT).build();
	}

	@Nullable
	@DELETE
	@Path("/gemeindeGesuchsperiodeDoku/{gemeindeId}/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response removeGesuchsperiodeDokument(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response) {

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJAXPId);

		requireNonNull(gemeindeId);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);
		gemeindeService.removeGemeindeGesuchsperiodeDokument(gemeindeId, gesuchsperiodeId, sprache,
			dokumentTyp);
		return Response.ok().build();
	}

	@ApiOperation(value = "retuns true if the Dokument Typ exists for this gemeinde exists for the given language",
		response = boolean.class)
	@GET
	@Path("/existGemeindeGesuchsperiodeDoku/{gemeindeId}/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public boolean existDokument(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Context HttpServletResponse response
	) {
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJAXPId);
		requireNonNull(gemeindeId);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(sprache);
		requireNonNull(dokumentTyp);
		return gemeindeService.existGemeindeGesuchsperiodeDokument(gemeindeId, gesuchsperiodeId, sprache, dokumentTyp);
	}

	@ApiOperation(value = "Returns all still available external clients and all assigned external clients",
		response = JaxExternalClientAssignment.class)
	@Nonnull
	@GET
	@Path("/{gemeindeId}/externalclients")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS,
		SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS })
	public Response getExternalClients(@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId) {
		requireNonNull(gemeindeJAXPId.getId());
		String gemeindeID = converter.toEntityId(gemeindeJAXPId);
		GemeindeStammdaten gemeindeStammdaten =
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeID).orElse(null);

		Collection<ExternalClient> availableClients = externalClientService.getAllForGemeinde();
		JaxExternalClientAssignment jaxExternalClientAssignment = new JaxExternalClientAssignment();
		if (gemeindeStammdaten != null) {
			availableClients.removeAll(gemeindeStammdaten.getExternalClients());
			jaxExternalClientAssignment.getAssignedClients().addAll(converter.externalClientsToJAX(gemeindeStammdaten.getExternalClients()));
		}
		jaxExternalClientAssignment.getAvailableClients().addAll(converter.externalClientsToJAX(availableClients));

		return Response.ok(jaxExternalClientAssignment).build();
	}

	@ApiOperation(value = "Gibt alle Gemeinden zurueck, die Mahlzeitenverguenstigungen auszahlen und fuer die"
		+ "der eingeloggte Benutzer zustaendig ist",
		responseContainer = "Collection",
		response = JaxGemeinde.class)
	@Nullable
	@GET
	@Path("/mahlzeitenverguenstigung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxGemeinde> getGemeindenWithMahlzeitenverguenstigungForBenutzer() {
		return gemeindeService.getGemeindenWithMahlzeitenverguenstigungForBenutzer().stream()
			.map(gemeinde -> converter.gemeindeToJAX(gemeinde))
			.collect(Collectors.toList());
	}
}

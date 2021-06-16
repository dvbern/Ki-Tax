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
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.dtos.JaxGemeindeAntraegeFBTestdatenDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeAntraegeLATSTestdatenDTO;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.SchulungService;
import ch.dvbern.ebegu.services.TestfaelleService;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource zur Erstellung von (vordefinierten) Testfaellen.
 * Alle Testfaelle erstellen:
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/all
 */
@Path("testfaelle")
@Stateless
@Api(description = "Resource zur Erstellung von (vordefinierten) Testfaellen")
@RolesAllowed(SUPER_ADMIN)
public class TestfaelleResource {

	private static final String FALL = "Fall ";

	@Inject
	private TestfaelleService testfaelleService;

	@Inject
	private SchulungService schulungService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@ApiOperation(value = "Erstellt einen Testfall aus mehreren vordefinierten Testfaellen. Folgende Einstellungen " +
		"sind moeglich: Gesuchsperiode, Gemeinde, Status der Betreuungen, Gesuch verfuegen", response = String.class)
	@GET
	@Path("/testfall/{fallid}/{gesuchsperiodeId}/{gemeindeId}/{betreuungenBestaetigt}/{verfuegen}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTestFall(
		@PathParam("fallid") String fallid,
		@PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@PathParam("gemeindeId") String gemeindeId,
		@PathParam("betreuungenBestaetigt") boolean betreuungenBestaetigt,
		@PathParam("verfuegen") boolean verfuegen) {

		assertTestfaelleAccessAllowed();
		StringBuilder responseString = testfaelleService.createAndSaveTestfaelle(fallid,
			betreuungenBestaetigt,
			verfuegen,
			gesuchsperiodeId,
			gemeindeId);
		return Response.ok(responseString.toString()).build();
	}

	@ApiOperation(value = "Erstellt einen Testfall aus mehreren vordefinierten Testfaellen fuer einen Gesuchsteller "
		+
		"(Online Gesuch). Folgende Einstellungen sind moeglich: Gesuchsperiode, Gemeinde, Status der Betreuungen, "
		+ "Gesuch "
		+
		"verfuegen, gewuenschter Gesuchsteller", response = String.class)
	@GET
	@Path("/testfallgs/{fallid}/{gesuchsperiodeId}/{gemeindeId}/{betreuungenBestaetigt}/{verfuegen}/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTestFallGS(
		@PathParam("fallid") String fallid,
		@PathParam("gesuchsperiodeId") String gesuchsperiodeId,
		@PathParam("gemeindeId") String gemeindeId,
		@PathParam("betreuungenBestaetigt") boolean betreuungenBestaetigt,
		@PathParam("verfuegen") boolean verfuegen,
		@PathParam("username") String username) {

		assertTestfaelleAccessAllowed();
		StringBuilder responseString = testfaelleService.createAndSaveAsOnlineGesuch(fallid,
			betreuungenBestaetigt,
			verfuegen,
			username,
			gesuchsperiodeId,
			gemeindeId);
		return Response.ok(responseString.toString()).build();
	}

	@ApiOperation(value = "Loescht alle Antraege des uebergebenen Gesuchstellers.", response = String.class)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@DELETE
	@Path("/testfallgs/{username}")
	@Consumes(MediaType.WILDCARD)
	public Response removeFaelleOfGS(
		@PathParam("username") String username) {

		assertTestfaelleAccessAllowed();
		testfaelleService.removeGesucheOfGS(username);
		return Response.ok().build();
	}

	@ApiOperation(value = "Simuliert fuer den uebergebenen Testfall eine Heirat", response = String.class)
	@GET
	@Path("/mutationHeirat/{dossierId}/{gesuchsperiodeid}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response mutationHeirat(
		@PathParam("dossierId") String dossierId,
		@PathParam("gesuchsperiodeid") String gesuchsperiodeid,
		@Nullable @QueryParam("mutationsdatum") String stringMutationsdatum,
		@Nullable @QueryParam("aenderungper") String stringAenderungPer) {

		assertTestfaelleAccessAllowed();
		LocalDate mutationsdatum = DateUtil.parseStringToDateOrReturnNow(stringMutationsdatum);
		LocalDate aenderungPer = DateUtil.parseStringToDateOrReturnNow(stringAenderungPer);

		final Gesuch gesuch =
			testfaelleService.mutierenHeirat(dossierId, gesuchsperiodeid, mutationsdatum, aenderungPer, false);
		if (gesuch != null) {
			return Response.ok(FALL + gesuch.getFall().getFallNummer() + " mutiert zu heirat").build();
		}
		return Response.ok(FALL + dossierId + " konnte nicht mutiert").build();
	}

	@ApiOperation(value = "Simuliert fuer den uebergebenen Testfall eine Scheidung", response = String.class)
	@GET
	@Path("/mutationScheidung/{dossierId}/{gesuchsperiodeid}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response mutierenScheidung(
		@PathParam("dossierId") String dossierId,
		@PathParam("gesuchsperiodeid") String gesuchsperiodeid,
		@Nullable @QueryParam("mutationsdatum") String stringMutationsdatum,
		@Nullable @QueryParam("aenderungper") String stringAenderungPer) {

		assertTestfaelleAccessAllowed();
		LocalDate mutationsdatum = DateUtil.parseStringToDateOrReturnNow(stringMutationsdatum);
		LocalDate aenderungPer = DateUtil.parseStringToDateOrReturnNow(stringAenderungPer);

		final Gesuch gesuch =
			testfaelleService.mutierenScheidung(dossierId, gesuchsperiodeid, mutationsdatum, aenderungPer, false);
		if (gesuch != null) {
			return Response.ok(FALL + gesuch.getFall().getFallNummer() + " mutiert zu scheidung").build();
		}
		return Response.ok(FALL + dossierId + " konnte nicht mutiert").build();
	}

	@ApiOperation(value = "Setzt die Schulungsdaten zurueck", response = String.class)
	@GET
	@Path("/schulung/reset")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Response resetSchulungsdaten() {
		assertTestfaelleAccessAllowed();
		schulungService.resetSchulungsdaten();
		return Response.ok("Schulungsdaten zurückgesetzt").build();
	}

	@ApiOperation(value = "Loescht alle in der Schulung erstellten Daten.", response = String.class)
	@DELETE
	@Path("/schulung/delete")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Response deleteSchulungsdaten() {
		assertTestfaelleAccessAllowed();
		schulungService.deleteSchulungsdaten();
		return Response.ok("Schulungsdaten gelöscht").build();
	}

	@ApiOperation(value = "Erstellt die Schulungsdaten", response = String.class)
	@GET
	@Path("/schulung/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Response createSchulungsdaten() {
		assertTestfaelleAccessAllowed();
		schulungService.createSchulungsdaten();
		return Response.ok("Schulungsdaten erstellt").build();
	}

	@ApiOperation(value = "Setzt die Tutorialdaten zurueck. Gemeinde und Institution", response = String.class)
	@GET
	@Path("/schulung/tutorial/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public Response createTutorialdaten() {
		assertTestfaelleAccessAllowed();
		schulungService.createTutorialdaten();
		return Response.ok("Tutorialdaten erstellt").build();
	}

	@ApiOperation(value = "Gibt eine Liste der Schulungsbenutzer zurueck",
		responseContainer = "Array", response = String.class)
	@GET
	@Path("/schulung/public/user")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@PermitAll
	public Response getSchulungBenutzer() {
		assertTestfaelleAccessAllowed();
		String[] schulungBenutzer = schulungService.getSchulungBenutzer();
		return Response.ok(schulungBenutzer).build();
	}

	@ApiOperation(value = "Sendet ein Beispiel aller Mails an die uebergebene Adresse", response = String.class)
	@GET
	@Path("/mailtest/{mailadresse}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response testAllMails(
		@PathParam("mailadresse") String mailadresse) {

		assertTestfaelleAccessAllowed();

		testfaelleService.testAllMails(mailadresse);
		return Response.ok().build();
	}

	@ApiOperation(value = "Erstellt LATS testdaten", response = String.class)
	@POST
	@Path("/gemeinde-antraege/LASTENAUSGLEICH_TAGESSCHULEN")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTestdatenLATS(
		@Nonnull @NotNull @Valid JaxGemeindeAntraegeLATSTestdatenDTO jaxGemeindeAntraegeTestdatenDTO) {

		final String gemeindeId = jaxGemeindeAntraegeTestdatenDTO.getGemeinde() != null ? jaxGemeindeAntraegeTestdatenDTO.getGemeinde().getId() : null;
		final Collection<LastenausgleichTagesschuleAngabenGemeindeContainer> latsContainers =
			testfaelleService.createAndSaveLATSTestdaten(
				Objects.requireNonNull(jaxGemeindeAntraegeTestdatenDTO.getGesuchsperiode().getId()),
				gemeindeId,
				jaxGemeindeAntraegeTestdatenDTO.getStatus());
		return Response.ok(latsContainers.stream().map(container -> container.getId()).collect(Collectors.joining(","))).build();
	}

	@ApiOperation(value = "Erstellt FB testdaten", response = String.class)
	@POST
	@Path("/gemeinde-antraege/FERIENBETREUUNG")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTestdatenFerienbetreuung(
		@Nonnull @NotNull @Valid JaxGemeindeAntraegeFBTestdatenDTO jaxGemeindeAntraegeTestdatenDTO) {

		final FerienbetreuungAngabenContainer ferienbetreuungContainer =
			testfaelleService.createAndSaveFerienbetreuungTestdaten(
				Objects.requireNonNull(jaxGemeindeAntraegeTestdatenDTO.getGesuchsperiode().getId()),
				Objects.requireNonNull(jaxGemeindeAntraegeTestdatenDTO.getGemeinde().getId()),
				jaxGemeindeAntraegeTestdatenDTO.getStatus());
		return Response.ok(ferienbetreuungContainer.getId()).build();
	}

	private void assertTestfaelleAccessAllowed() {
		// Testfaelle duerfen nur erstellt werden, wenn das Flag gesetzt ist und das Dummy Login eingeschaltet ist
		if (!ebeguConfiguration.isDummyLoginEnabled()) {
			throw new EbeguRuntimeException(
				"assertTestfaelleAccessAllowed",
				ErrorCodeEnum.ERROR_TESTFAELLE_DISABLED,
				"Testfaelle duerfen nur verwendet werden,"
					+ " wenn das DummyLogin fuer diese Umgebung eingeschaltet ist");
		}
		if (!ebeguConfiguration.isTestfaelleEnabled()) {
			throw new EbeguRuntimeException(
				"assertTestfaelleAccessAllowed",
				ErrorCodeEnum.ERROR_TESTFAELLE_DISABLED,
				"Testfaelle duerfen nur verwendet "
					+ "werden, wenn diese ueber ein SystemProperty eingeschaltet sind");
		}
	}
}

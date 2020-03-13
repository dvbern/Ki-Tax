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

package ch.dvbern.ebegu.api.resource.schulamt;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldung;
import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldungTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxExternalError;
import ch.dvbern.ebegu.api.dtos.JaxExternalFinanzielleSituation;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsangebotTyp;
import ch.dvbern.ebegu.api.enums.JaxExternalErrorCode;
import ch.dvbern.ebegu.api.util.version.VersionInfoBean;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.ScolarisException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static org.slf4j.LoggerFactory.getLogger;

@Path("/schulamt")
@Api(description = "Resource für die Schnittstelle zu externen Schulamt-Applikationen")
@SuppressWarnings({ "EjbInterceptorInspection", "EjbClassBasicInspection", "PMD.AvoidDuplicateLiterals" })
@Stateless
@PermitAll
public class ScolarisBackendResource {

	private static final Logger LOG = getLogger(ScolarisBackendResource.class);

	@Inject
	private VersionInfoBean versionInfoBean;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private ScolarisConverter converter;

	@ApiOperation(value = "Gibt die Version von kiBon zurück. Kann als Testmethode verwendet werden, da ohne "
		+ "Authentifizierung aufrufbar",
		response = String.class)
	@GET
	@Path("/heartbeat")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public String getHeartBeat() {
		StringBuilder builder = new StringBuilder();
		if (versionInfoBean != null && versionInfoBean.getVersionInfo().isPresent()) {
			builder.append("Version: ");
			builder.append(versionInfoBean.getVersionInfo().get().getVersion());

		} else {
			builder.append("unknown Version");
		}
		return builder.toString();
	}

	@ApiOperation(value = "Gibt eine Anmeldung fuer ein Schulamt-Angebot zurueck (Tagesschule oder Ferieninsel)",
		response = JaxExternalAnmeldung.class)
	@ApiResponses({
		@ApiResponse(code = 400, message = "no data found"),
		@ApiResponse(code = 401, message = "unauthorized"),
		@ApiResponse(code = 500, message = "server error")
	})
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/anmeldung/{bgNummer}")
	@RolesAllowed(SUPER_ADMIN)
	public Response getAnmeldung(@Nonnull @PathParam("bgNummer") String bgNummer) {

		try {
			if (!BetreuungUtil.validateBGNummer(bgNummer)) {
				return createBgNummerFormatError();
			}

			final List<AbstractAnmeldung> betreuungen = betreuungService.findNewestAnmeldungByBGNummer(bgNummer);

			if (betreuungen == null || betreuungen.isEmpty()) {
				// Betreuung not found
				return createNoResultsResponse("No Betreuung with id " + bgNummer + " found");
			}
			if (betreuungen.size() > 1) {
				// More than one betreuung
				return createTooManyResultsResponse("More than one Betreuung with id " + bgNummer + " found");
			}

			final AbstractAnmeldung betreuung = betreuungen.get(0);

			// TODO (Team) pruefen, ob auf der Gemeinde Scolaris eingeschaltet ist, ansonsten createDrittanwendungNotAllowedResponse()

			JaxExternalBetreuungsangebotTyp jaxExternalBetreuungsangebotTyp = converter.betreuungsangebotTypToScolaris(betreuung.getBetreuungsangebotTyp());
			if (jaxExternalBetreuungsangebotTyp == JaxExternalBetreuungsangebotTyp.TAGESSCHULE) {
				// Betreuung ist Tagesschule
				AnmeldungTagesschule anmeldungTagesschule = (AnmeldungTagesschule) betreuung;
				if (anmeldungTagesschule.isKeineDetailinformationen()) {
					// Falls die Anmeldung ohne Detailangaben erfolgt ist, geben wir hier NO_RESULT zurueck
					return createNoResultsResponse("No Betreuung with id " + bgNummer + " found");
				}
				try {
					JaxExternalAnmeldungTagesschule jaxResult = converter.anmeldungTagesschuleToScolaris(anmeldungTagesschule);
					return Response.ok(jaxResult).build();
				} catch (ScolarisException e) {
					return createNoResultsResponse("No Scolaris Modules found for " + bgNummer);
				}
			}
			if (jaxExternalBetreuungsangebotTyp == JaxExternalBetreuungsangebotTyp.FERIENINSEL) {
				// Betreuung ist Ferieninsel
				AnmeldungFerieninsel anmeldungFerieninsel = (AnmeldungFerieninsel) betreuung;
				return Response.ok(converter.anmeldungFerieninselToScolaris(anmeldungFerieninsel)).build();
			}
			// Betreuung ist weder Tagesschule noch Ferieninsel
			return createNoResultsResponse("No Betreuung with id " + bgNummer + " found");

		} catch (Exception e) {
			LOG.error("getAnmeldung()", e);
			return createInternalServerErrorResponse("Please inform the adminstrator of this application");
		}
	}

	@ApiOperation(value =
		"Gibt das massgebende Einkommen fuer die uebergebene BgNummer zurueck. Falls das massgebende Einkommen noch "
			+ "nicht erfasst wurde, wird 400 zurueckgegeben.",
		response = JaxExternalFinanzielleSituation.class)
	@ApiResponses({
		@ApiResponse(code = 400, message = "no data found"),
		@ApiResponse(code = 401, message = "unauthorized"),
		@ApiResponse(code = 500, message = "server error")
	})
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/finanziellesituation")
	@RolesAllowed(SUPER_ADMIN)
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public Response getFinanzielleSituation(
		@Nonnull @QueryParam("stichtag") String stichtagParam,
		@Nonnull @QueryParam("bgNummer") String bgNummer) {

		try {
			// Check parameters
			if (stichtagParam.isEmpty()) {
				return createBadParameterResponse("stichtagParam is null or empty");
			}
			if (bgNummer.isEmpty()) {
				return createBadParameterResponse("bgNummer is null or empty");
			}

			// Parse Fallnummer
			if (!BetreuungUtil.validateBGNummer(bgNummer)) {
				return createBgNummerFormatError();
			}
			long fallNummer;
			try {
				fallNummer = BetreuungUtil.getFallnummerFromBGNummer(bgNummer);
			} catch (Exception e) {
				LOG.info("getFinanzielleSituation()", e);
				return createBadParameterResponse("Can not parse bgNummer");
			}

			// Parse Stichtag
			LocalDate stichtag;
			try {
				stichtag = DateUtil.parseStringToDateOrReturnNow(stichtagParam);
			} catch (Exception e) {
				LOG.info("getFinanzielleSituation()", e);
				return createBadParameterResponse("Can not parse date for stichtagParam");
			}

			// Parse Gesuchsperiode
			int yearFromBGNummer = BetreuungUtil.getYearFromBGNummer(bgNummer);
			Gesuchsperiode gesuchsperiodeFromBGNummer =
				gesuchsperiodeService.getGesuchsperiodeAm(LocalDate.of(yearFromBGNummer, Month.AUGUST, 1))
					.orElseThrow(() -> new EbeguEntityNotFoundException(
						"getFinanzielleSituation",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						bgNummer));

			stichtag = rearrangeStichtag(stichtag, gesuchsperiodeFromBGNummer);

			//Get "neustes" Gesuch on Stichtag an fallnummer
			Optional<Gesuch> neustesGesuchOpt =
				gesuchService.getNeustesGesuchFuerFallnumerForSchulamtInterface(gesuchsperiodeFromBGNummer,
					fallNummer);
			if (!neustesGesuchOpt.isPresent()) {
				return createNoResultsResponse("No gesuch found for fallnummer or finSit not yet set");
			}
			final Gesuch neustesGesuch = neustesGesuchOpt.get();

			// TODO (Team) pruefen, ob auf der Gemeinde Scolaris eingeschaltet ist, ansonsten createDrittanwendungNotAllowedResponse()
			// Calculate Verfuegungszeitabschnitte for Familiensituation
			final Verfuegung famGroessenVerfuegung =
				verfuegungService.getEvaluateFamiliensituationVerfuegung(neustesGesuch);
			JaxExternalFinanzielleSituation dto = converter.finanzielleSituationToScolaris(fallNummer, stichtag, neustesGesuch,
				famGroessenVerfuegung);
			if (dto == null) {
				// If no Finanzdaten found on Verfügungszeitabschnitt from Stichtag, return ErrorObject
				return createNoResultsResponse("No FinanzielleSituation for Stichtag");
			}
			return Response.ok(dto).build();

		} catch (Exception e) {
			LOG.error("getFinanzielleSituation()", e);
			return createInternalServerErrorResponse("Please inform the adminstrator of this application");
		}
	}

	private LocalDate rearrangeStichtag(@Nonnull LocalDate stichtag, @Nonnull Gesuchsperiode periode) {
		// Falls der Stichtag *vor* Beginn der Gesuchsperiode liegt, wird der Starttag der Gesuchsperiode genommen
		if (stichtag.isBefore(periode.getGueltigkeit().getGueltigAb())) {
			return periode.getGueltigkeit().getGueltigAb();
		}
		return stichtag;
	}

	private Response createBgNummerFormatError() {
		// Wrong BGNummer format
		return Response.status(Response.Status.BAD_REQUEST).entity(
			new JaxExternalError(
				JaxExternalErrorCode.BAD_PARAMETER,
				"Invalid BGNummer format")).build();
	}

	private Response createNoResultsResponse(String message) {
		return Response.status(Response.Status.BAD_REQUEST).entity(
			new JaxExternalError(
				JaxExternalErrorCode.NO_RESULTS,
				message)).build();
	}

	private Response createBadParameterResponse(String message) {
		return Response.status(Response.Status.BAD_REQUEST).entity(
			new JaxExternalError(
				JaxExternalErrorCode.BAD_PARAMETER,
				message)).build();
	}

	private Response createInternalServerErrorResponse(String message) {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
			new JaxExternalError(
				JaxExternalErrorCode.SERVER_ERROR,
				message)).build();
	}

	private Response createTooManyResultsResponse(String message) {
		return Response.status(Response.Status.BAD_REQUEST).entity(
			new JaxExternalError(
				JaxExternalErrorCode.TOO_MANY_RESULTS,
				message)).build();
	}

//	private Response createDrittanwendungNotAllowedResponse(String message) {
//		return Response.status(Response.Status.BAD_REQUEST).entity(
//			new JaxExternalError(
//				JaxExternalErrorCode.DRITTANWENDUNG_NOT_ALLOWED,
//				message)).build();
//	}
}

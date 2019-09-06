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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldungFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldungTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxExternalError;
import ch.dvbern.ebegu.api.dtos.JaxExternalFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxExternalFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.JaxExternalModul;
import ch.dvbern.ebegu.api.dtos.JaxExternalRechnungsAdresse;
import ch.dvbern.ebegu.api.enums.JaxExternalAntragstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalErrorCode;
import ch.dvbern.ebegu.api.enums.JaxExternalFerienName;
import ch.dvbern.ebegu.api.enums.JaxExternalModulName;
import ch.dvbern.ebegu.api.enums.JaxExternalTarifart;
import ch.dvbern.ebegu.api.util.version.VersionInfoBean;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
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
public class SchulamtBackendResource {

	private static final Logger LOG = getLogger(SchulamtBackendResource.class);

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

			if (betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESSCHULE) {
				// Betreuung ist Tagesschule
				AnmeldungTagesschule anmeldungTagesschule = (AnmeldungTagesschule) betreuung;
				if (anmeldungTagesschule.isKeineDetailinformationen()) {
					// Falls die Anmeldung ohne Detailangaben erfolgt ist, geben wir hier NO_RESULT zurueck
					return createNoResultsResponse("No Betreuung with id " + bgNummer + " found");
				}
				return Response.ok(getAnmeldungTagesschule(anmeldungTagesschule)).build();
			}
			if (betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.FERIENINSEL) {
				// Betreuung ist Ferieninsel
				AnmeldungFerieninsel anmeldungFerieninsel = (AnmeldungFerieninsel) betreuung;
				return Response.ok(getAnmeldungFerieninsel(anmeldungFerieninsel)).build();
			}
			// Betreuung ist weder Tagesschule noch Ferieninsel
			return createNoResultsResponse("No Betreuung with id " + bgNummer + " found");

		} catch (Exception e) {
			LOG.error("getAnmeldung()", e);
			return createInternalServerErrorResponse("Please inform the adminstrator of this application");
		}
	}

	private JaxExternalAnmeldungTagesschule getAnmeldungTagesschule(AnmeldungTagesschule betreuung) {
		Objects.requireNonNull(betreuung.getBelegungTagesschule());

		List<JaxExternalModul> anmeldungen = new ArrayList<>();
		betreuung.getBelegungTagesschule()
			.getModuleTagesschule()
			.forEach(modulTagesschule -> anmeldungen.add(new JaxExternalModul(modulTagesschule
				.getWochentag(), JaxExternalModulName.valueOf(modulTagesschule.getModulTagesschuleGroup().getModulTagesschuleName().name())))
			);
		return new JaxExternalAnmeldungTagesschule(
			betreuung.getBGNummer(),
			JaxExternalBetreuungsstatus.valueOf(betreuung.getBetreuungsstatus().name()),
			betreuung.getInstitutionStammdaten().getInstitution().getName(),
			anmeldungen,
			betreuung.getKind().getKindJA().getVorname(),
			betreuung.getKind().getKindJA().getNachname());
	}

	private JaxExternalAnmeldungFerieninsel getAnmeldungFerieninsel(AnmeldungFerieninsel betreuung) {
		Objects.requireNonNull(betreuung.getBelegungFerieninsel());

		List<LocalDate> datumList = new ArrayList<>();
		betreuung.getBelegungFerieninsel()
			.getTage()
			.forEach(belegungFerieninselTag -> datumList.add(belegungFerieninselTag.getTag()));

		JaxExternalFerieninsel ferieninsel =
			new JaxExternalFerieninsel(JaxExternalFerienName.valueOf(betreuung.getBelegungFerieninsel()
				.getFerienname()
				.name
					()), datumList);

		return new JaxExternalAnmeldungFerieninsel(
			betreuung.getBGNummer(),
			JaxExternalBetreuungsstatus.valueOf(betreuung.getBetreuungsstatus().name()),
			betreuung.getInstitutionStammdaten().getInstitution().getName(),
			ferieninsel,
			betreuung.getKind().getKindJA().getVorname(),
			betreuung.getKind().getKindJA().getNachname());
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

			return getExternalFinanzielleSituationResponse(fallNummer, stichtag, neustesGesuch);

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

	@SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:BooleanExpressionComplexity"})
	private Response getExternalFinanzielleSituationResponse(
		long fallNummer,
		LocalDate stichtag,
		Gesuch neustesGesuch) {

		final Familiensituation familiensituation = neustesGesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		if (familiensituation.getSozialhilfeBezueger() != null && familiensituation.getSozialhilfeBezueger()
			&& neustesGesuch.getFinSitStatus() == FinSitStatus.AKZEPTIERT) {
			// SozialhilfeBezüger Ja -> Basiszahler (keine finSit!)
			final JaxExternalFinanzielleSituation dto = convertToJaxExternalFinanzielleSituationWithoutFinDaten(
				fallNummer, stichtag, neustesGesuch, JaxExternalTarifart.BASISZAHLER);
			return Response.ok(dto).build();
		}

		if ((familiensituation.getSozialhilfeBezueger() != null
			&& !familiensituation.getSozialhilfeBezueger()
			&& familiensituation.getAntragNurFuerBehinderungszuschlag() != null
			&& familiensituation.getAntragNurFuerBehinderungszuschlag())
			|| neustesGesuch.getFinSitStatus() == FinSitStatus.ABGELEHNT) {
			// SozialhilfeBezüger Nein + Vergünstigung gewünscht Nein  -> Vollzahler (keine finSit!)
			final JaxExternalFinanzielleSituation dto = convertToJaxExternalFinanzielleSituationWithoutFinDaten(
				fallNummer, stichtag, neustesGesuch, JaxExternalTarifart.VOLLZAHLER);
			return Response.ok(dto).build();

		}
		// SozialhilfeBezüger Nein + Vergünstigung gewünscht ja  oder Kita-Betreuung vorhanden -> Detailrechnung (mit finSit!)

		// Calculate Verfuegungszeitabschnitte for Familiensituation
		final Verfuegung famGroessenVerfuegung =
			verfuegungService.getEvaluateFamiliensituationVerfuegung(neustesGesuch);

		// Find and return Finanzdaten on Verfügungszeitabschnitt from Stichtag
		List<VerfuegungZeitabschnitt> zeitabschnitten = famGroessenVerfuegung.getZeitabschnitte();
		// get finanzielleSituation only for stichtag
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitten) {
			if (zeitabschnitt.getGueltigkeit().contains(stichtag)) {
				final JaxExternalFinanzielleSituation dto = convertToJaxExternalFinanzielleSituation(
					fallNummer, stichtag, neustesGesuch, zeitabschnitt);
				return Response.ok(dto).build();
			}
		}
		// If no Finanzdaten found on Verfügungszeitabschnitt from Stichtag, return ErrorObject
		return createNoResultsResponse("No FinanzielleSituation for Stichtag");
	}

	private JaxExternalFinanzielleSituation convertToJaxExternalFinanzielleSituation(
		long fallNummer, LocalDate stichtag, Gesuch neustesGesuch,
		VerfuegungZeitabschnitt zeitabschnitt) {

		final GesuchstellerContainer gesuchsteller1 = neustesGesuch.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		final GesuchstellerAdresse rechnungsAdresse = gesuchsteller1.extractEffectiveRechnungsAdresse(stichtag);
		Objects.requireNonNull(rechnungsAdresse);
		return new JaxExternalFinanzielleSituation(
			fallNummer,
			stichtag,
			zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr(),
			zeitabschnitt.getAbzugFamGroesse(),
			JaxExternalAntragstatus.valueOf(neustesGesuch.getStatus().name()),
			JaxExternalTarifart.DETAILBERECHNUNG,
			new JaxExternalRechnungsAdresse(
				gesuchsteller1.extractVorname(),
				gesuchsteller1.extractNachname(),
				rechnungsAdresse.getStrasse(),
				rechnungsAdresse.getHausnummer(),
				rechnungsAdresse.getZusatzzeile(),
				rechnungsAdresse.getPlz(),
				rechnungsAdresse.getOrt(),
				rechnungsAdresse.getLand().name()));
	}

	private JaxExternalFinanzielleSituation convertToJaxExternalFinanzielleSituationWithoutFinDaten(
		long fallNummer, LocalDate stichtag, Gesuch neustesGesuch,
		JaxExternalTarifart tarifart) {

		final GesuchstellerContainer gesuchsteller1 = neustesGesuch.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		final GesuchstellerAdresse rechnungsAdresse = gesuchsteller1.extractEffectiveRechnungsAdresse(stichtag);
		Objects.requireNonNull(rechnungsAdresse);
		return new JaxExternalFinanzielleSituation(
			fallNummer,
			stichtag,
			JaxExternalAntragstatus.valueOf(neustesGesuch.getStatus().name()),
			tarifart,
			new JaxExternalRechnungsAdresse(
				gesuchsteller1.extractVorname(),
				gesuchsteller1.extractNachname(),
				rechnungsAdresse.getStrasse(),
				rechnungsAdresse.getHausnummer(),
				rechnungsAdresse.getZusatzzeile(),
				rechnungsAdresse.getPlz(),
				rechnungsAdresse.getOrt(),
				rechnungsAdresse.getLand().name()));
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
}

/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WorkJobType;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.WorkjobService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer asynchrone Reports
 */
@Path("reporting/async")
@Stateless
@Api(description = "Resource für Statistiken und Reports")
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class ReportResourceAsync {

	public static final String DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN =
		"Das von-Datum muss vor dem bis-Datum sein.";
	public static final String URL_PART_EXCEL = "excel/";

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private WorkjobService workjobService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private InstitutionService institutionService;

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuch-Stichtag'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchStichtag")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchStichtagReportExcel(
		@QueryParam("dateTimeStichtag") @Nonnull String dateTimeStichtag,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);
		Objects.requireNonNull(dateTimeStichtag);
		LocalDate datumVon = DateUtil.parseStringToDateOrReturnNow(dateTimeStichtag);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			LocaleThreadLocal.get().equals(Locale.FRENCH)
				? ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_FR
				: ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_DE,
			datumVon,
			null,
			periodeId,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuch-Zeitraum'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchZeitraum")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchZeitraumReportExcel(
		@QueryParam("dateTimeFrom") @Nonnull String dateTimeFromParam,
		@QueryParam("dateTimeTo") @Nonnull String dateTimeToParam,
		@QueryParam("gesuchDatumTyp") @Nonnull @Valid String gesuchDatumTypParam,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(dateTimeFromParam);
		Objects.requireNonNull(dateTimeToParam);
		Objects.requireNonNull(gesuchDatumTypParam);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(dateTimeFromParam);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(dateTimeToParam);
		DatumTyp gesuchDatumTyp = DatumTyp.valueOf(gesuchDatumTypParam);

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getGesuchZeitraumReportExcel",
				"Fehler beim erstellen Report Gesuch Zeitraum",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			LocaleThreadLocal.get().equals(Locale.FRENCH)
				? ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_FR
				: ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_DE,
			dateFrom,
			dateTo,
			gesuchDatumTyp,
			periodeId,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Kanton'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/kanton")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, REVISOR,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public Response getKantonReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("kantonSelbstbehalt") @Nullable BigDecimal kantonSelbstbehalt,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateAuswertungVon = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateAuswertungBis = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateAuswertungBis.isAfter(dateAuswertungVon)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getKantonReportExcel",
				"Fehler beim erstellen Report Kanton",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_KANTON,
			dateAuswertungVon,
			dateAuswertungBis,
			kantonSelbstbehalt,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'MitarbeiterInnen'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/mitarbeiterinnen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getMitarbeiterinnenReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateAuswertungVon = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateAuswertungBis = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateAuswertungBis.isAfter(dateAuswertungVon)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getMitarbeiterinnenReportExcel",
				"Fehler beim erstellen Report Mitarbeiterinnen",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_MITARBEITERINNEN,
			dateAuswertungVon,
			dateAuswertungBis,
			null,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Benutzer'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/benutzer")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, REVISOR, ADMIN_TS, ADMIN_TRAEGERSCHAFT, ADMIN_MANDANT,
		SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION })
	public Response getBenutzerReportExcel(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_BENUTZER,
			null,
			null,
			null,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Institutionen'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/institutionen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS })
	public Response getInstitutionenReportExcel(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_INSTITUTIONEN,
			null,
			null,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlungen pro Periode'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/zahlungperiode")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungPeriodReportExcel(
		@QueryParam("gesuchsperiodeID") @Nonnull @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		Objects.requireNonNull(gesuchPeriodIdParam);
		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam.getId();
		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE,
			null,
			null,
			periodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuchsteller-Kinder-Betreuung'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchstellerkinderbetreuung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchstellerKinderBetreuungReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getGesuchstellerKinderBetreuungReportExcel",
				"Fehler beim erstellen Report Gesuchsteller-Kinder-Betreuung",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG,
			dateFrom,
			dateTo,
			periodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Kinder'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/kinder")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, REVISOR,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION })
	public Response getKinderReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);
		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getKinderReportExcel",
				"Fehler beim erstellen Report Kinder"
				, DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}
		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_KINDER,
			dateFrom,
			dateTo,
			periodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuchsteller'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchsteller")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchstellerReportExcel(
		@QueryParam("stichtag") @Nonnull String stichtag,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(stichtag);
		LocalDate date = DateUtil.parseStringToDateOrReturnNow(stichtag);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER,
			date,
			null,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Massenversand'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/massenversand")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({SUPER_ADMIN, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS})
	public Response getMassenversandReportExcel(
		@QueryParam("auswertungVon") @Nullable String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nonnull @Valid JaxId gesuchPeriodIdParam,
		@QueryParam("inklBgGesuche") @Nullable String inklBgGesuche,
		@QueryParam("inklMischGesuche") @Nullable String inklMischGesuche,
		@QueryParam("inklTsGesuche") @Nullable String inklTsGesuche,
		@QueryParam("ohneErneuerungsgesuch") @Nullable String ohneErneuerungsgesuch,
		@QueryParam("text") @Nullable String text,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Validate.notNull(auswertungBis);

		// auswertungVon might be null
		LocalDate dateFrom = auswertungVon == null
			? Constants.START_OF_TIME
			: DateUtil.parseStringToDateNullSafe(auswertungVon);

		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);
		String periodeId = gesuchPeriodIdParam.getId();

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getMassenversandReportExcel",
				"Fehler beim erstellen Report Massenversand",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		final boolean inklBgGesucheBoolean = Boolean.parseBoolean(inklBgGesuche);
		final boolean inklMischGesucheBoolean = Boolean.parseBoolean(inklMischGesuche);
		final boolean inklTsGesucheBoolean = Boolean.parseBoolean(inklTsGesuche);
		if(!(inklBgGesucheBoolean || inklMischGesucheBoolean || inklTsGesucheBoolean))
		{
			throw new EbeguRuntimeException(KibonLogLevel.DEBUG, "getMassenversandReportExcel", ErrorCodeEnum.ERROR_MASSENVERSAND_VERANTWORTLICHKEIT_FEHLT);
		}
		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_MASSENVERSAND,
			dateFrom,
			dateTo,
			periodeId,
			inklBgGesucheBoolean,
			inklMischGesucheBoolean,
			inklTsGesucheBoolean,
			Boolean.valueOf(ohneErneuerungsgesuch),
			null,
			null,
			null,
			text,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Verrechnung kiBon'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/verrechnungkibon")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT })
	public Response getVerrechnungKibonReportExcel(
		@QueryParam("doSave") @Nonnull String doSaveParam,
		@QueryParam("betragProKind") @Nullable BigDecimal betragProKindParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		final boolean doSave = Boolean.parseBoolean(doSaveParam);
		final BigDecimal betragProKind = betragProKindParam != null ? betragProKindParam : BigDecimal.ZERO;

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_VERRECHNUNG_KIBON,
			doSave,
			betragProKind,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(
		value = "Erstellt ein Excel mit der Statistik 'Tagesschule kiBon'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/tagesschuleAnmeldungen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_TS, SACHBEARBEITER_TS, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT})
	public Response getTagesschuleAnmeldungenReportExcel(
		@QueryParam("stammdatenId") @Nonnull String stammdatenId,
		@QueryParam("gesuchsperiodeId") @Nonnull String gesuchsperiodeId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		InstitutionStammdaten stammdaten = institutionStammdatenService.findInstitutionStammdaten(stammdatenId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
			"getTagesschuleAnmeldungenReportExcel", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, stammdatenId));
		authorizer.checkReadAuthorizationInstitutionStammdaten(stammdaten);

		if (checkMaxTagesschulModuleExceeded(stammdaten, gesuchsperiodeId)) {
			throw new EbeguRuntimeException("getTagesschuleAnmeldungenReportExcel", "Für diese Tagesschule gibt es zu "
				+ "viele Module. Mehr als " + Constants.MAX_MODULGROUPS_TAGESSCHULE + " können im Excel nicht "
				+ "angezeigt werden");
		}

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN,
			stammdatenId,
			gesuchsperiodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(
		value = "Erstellt ein Excel mit der Statistik 'Tagesschule Rechnungsstellung'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/tagesschuleRechnungsstellung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public Response getTagesschuleRechnungsstellungReportExcel(
		@QueryParam("gesuchsperiodeId") @Nonnull String gesuchsperiodeId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_TAGESSCHULE_RECHNUNGSSTELLUNG,
			null,
			null,
			gesuchsperiodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Notrecht'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/notrecht")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getNotrechtReportExcel(
		@QueryParam("zahlungenAusloesen") @Nonnull String zahlungenAusloesenParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		final boolean zahlungenAusloesen = Boolean.parseBoolean(zahlungenAusloesenParam);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_NOTRECHT,
			zahlungenAusloesen,
			BigDecimal.ZERO, // Parameter wird nicht gebraucht
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Mahlzeitenverguenstigung'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/mahlzeitenverguenstigung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS})
	public Response getMahlzeitenverguenstigungReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gemeindeId") @Nonnull String gemeindeId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		Objects.requireNonNull(gemeindeId);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getMahlzeitenverguenstigungReportExcel",
				"Fehler beim erstellen Report Mahlzeitenverguenstigung",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() ->
			new EbeguEntityNotFoundException("getMahlzeitenverguenstigungReportExcel", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND)
		);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_MAHLZEITENVERGUENSTIGUNG,
			dateFrom,
			dateTo,
			null,
			false,
			false,
			false,
			false,
			gemeinde,
			null,
			null,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}


	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gemeinden'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gemeinden")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Response getGemeindenReportExcel(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_GEMEINDEN,
			null,
			null,
			null,
			false,
			false,
			false,
			false,
			null,
			null,
			null,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Ferienbetreuung'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/ferienbetreuung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT,SACHBEARBEITER_MANDANT })
	public Response getFerienbetreuungExcelReport(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_FERIENBETREUUNG,
			null,
			null,
			null,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'LastenausgleichTS'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/lastenausgleichTagesschulen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getLastenausgleichTagesschulenExcelReport(
		@QueryParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_TAGESSCHULEN,
			null,
			null,
			gesuchsperiodeId,
			LocaleThreadLocal.get(),
				Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}


	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Lastenausgleich Betreuungsgutscheine'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/lastenausgleichBGZeitabschnitte")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG })
	public Response getLastenausgleichBGZeitabschnitteExcelReport(
		@QueryParam("gemeindeId") String gemeindeId,
		@QueryParam("jahr") Integer jahr,
		@QueryParam("von") String von,
		@QueryParam("bis") String bis,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		if ((von == null || bis == null) && gemeindeId == null) {
			throw new EbeguRuntimeException(
				KibonLogLevel.ERROR,
				"getLastenausgleichBGZeitabschnitteExcelReport",
				"Gemeinde oder Von und Bis Datum müssen spezifieziert sein",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_STAT_PARAMS_MISSING);
		}

		String ip = downloadResource.getIP(request);

		Gemeinde gemeinde = null;
		LocalDate dateVon = null;
		LocalDate dateBis = null;

		if (gemeindeId != null) {
			gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() ->
				new EbeguEntityNotFoundException("getLastenausgleichBGZeitabschnitteExcelReport", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND)
			);
		}

		if (von != null && bis != null) {
			dateVon = DateUtil.parseStringToDateOrReturnNow(von);
			dateBis = DateUtil.parseStringToDateOrReturnNow(bis);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_BG_ZEITABSCHNITTE,
			dateVon,
			dateBis,
			null,
			false,
			false,
			false,
			false,
			gemeinde,
			null,
			jahr,
			null,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlungen'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/zahlungen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
	ADMIN_BG, SACHBEARBEITER_BG, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT})
	public Response getZahlungenExcelReport(
		@Nullable @QueryParam("gesuchsperiodeId") String gesuchsperiodeId,
		@Nullable @QueryParam("gemeindeId") String gemeindeId,
		@Nullable @QueryParam("institutionId") String institutionId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
	{

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		Gemeinde gemeinde = null;
		Institution institution = null;

		if (gemeindeId != null) {
			gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() ->
				new EbeguEntityNotFoundException("getZahlungenExcelReport", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND)
			);
		}
		if (institutionId != null) {
			institution = institutionService.findInstitution(institutionId, true).orElseThrow(() ->
				new EbeguEntityNotFoundException("getZahlungenExcelReport", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND)
			);
		}

		final ReportVorlage reportVorlage = LocaleThreadLocal.get().equals(Locale.FRENCH)
			? ReportVorlage.VORLAGE_REPORT_ZAHLUNGEN_FR
			: ReportVorlage.VORLAGE_REPORT_ZAHLUNGEN_DE;

		workJob = workjobService.createNewReporting(
			workJob,
			reportVorlage,
			null,
			null,
			gesuchsperiodeId,
			false,
			false,
			false,
			false,
			gemeinde,
			institution,
			null,
			null,
			LocaleThreadLocal.get(),
			Objects.requireNonNull(principalBean.getMandant())
		);

		return createWorkjobResponse(workJob);
	}

	/**
	 * Überprüft, ob für eine bestimmte Gesuchsperiode die Anzahl Module über dem maximalen Wert liegt.
	 * Dieser maximale Wert ist durch das Exceltemplate gegeben
	 */
	private boolean checkMaxTagesschulModuleExceeded(@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull String gesuchsperiodeId) {
		if (stammdaten.getInstitutionStammdatenTagesschule() != null) {
			for (EinstellungenTagesschule e : stammdaten.getInstitutionStammdatenTagesschule().getEinstellungenTagesschule()) {
				if (e.getGesuchsperiode().getId().equals(gesuchsperiodeId)) {
					return e.getModulTagesschuleGroups().size() > Constants.MAX_MODULGROUPS_TAGESSCHULE;
				}
			}
		}
		return false;
	}

	@Nonnull
	private Workjob createWorkjobForReport(@Context HttpServletRequest request, @Context UriInfo uriInfo, String ip) {
		Workjob workJob = new Workjob();
		workJob.setWorkJobType(WorkJobType.REPORT_GENERATION);
		workJob.setStartinguser(principalBean.getPrincipal().getName());
		workJob.setTriggeringIp(ip);
		workJob.setRequestURI(uriInfo.getRequestUri().toString());
		String param = StringUtils.substringAfterLast(request.getRequestURI(), URL_PART_EXCEL);
		workJob.setParams(param);
		return workJob;
	}

	@Nonnull
	private Response createWorkjobResponse(@Nonnull Workjob workjob) {
		String json = Json.createObjectBuilder().add("workjobId", workjob.getId()).build().toString();
		return Response.ok(json).build();
	}
}

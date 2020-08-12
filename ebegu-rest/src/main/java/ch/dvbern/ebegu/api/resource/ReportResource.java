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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
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

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Reports
 */
@Path("reporting")
@Stateless
@Api(description = "Resource für Statistiken und Reports")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ReportResource {

	@Inject
	private ReportService reportService;

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlungsauftrag'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/zahlungsauftrag")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungsauftragReportExcel(
		@QueryParam("zahlungsauftragID") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws ExcelMergeException, EbeguRuntimeException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String id = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService.generateExcelReportZahlungAuftrag(id, LocaleThreadLocal.get());

		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlung'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/zahlung")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungReportExcel(
		@QueryParam("zahlungID") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws ExcelMergeException, EbeguRuntimeException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String id = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService.generateExcelReportZahlung(id, LocaleThreadLocal.get());

		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}
}

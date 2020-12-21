/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleich;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichBerechnungService;
import ch.dvbern.ebegu.services.LastenausgleichService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Lastenausgleiche
 */
@Path("lastenausgleich")
@Stateless
@Api(description = "Resource zum Verwalten von Lastenausgleichen")
@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
public class LastenausgleichResource {

	@Inject
	private LastenausgleichService lastenausgleichService;

	@Inject
	private ReportLastenausgleichBerechnungService reportService;

	@Inject
	private ReportKinderMitZemisNummerService zemisNummerService;

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private JaxBConverter converter;

	@Inject
	private PrincipalBean principalBean;

	@ApiOperation(value = "Gibt alle Lastenausgleiche zurueck.",
		responseContainer = "List",
		response = JaxLastenausgleich.class)
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, ADMIN_BG })
	public List<JaxLastenausgleich> getAllLastenausgleiche() {
		if (principalBean.isCallerInAnyOfRole(SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE, SACHBEARBEITER_BG, ADMIN_BG)) {
			Set<Gemeinde> gemeindeList = principalBean.getBenutzer().getCurrentBerechtigung().getGemeindeList();

			return lastenausgleichService.getLastenausgleicheForGemeinden(gemeindeList).stream()
				.map(lastenausgleich -> converter.lastenausgleichToJAX(lastenausgleich))
				.collect(Collectors.toList());
		}
		return lastenausgleichService.getAllLastenausgleiche().stream()
			.map(lastenausgleich -> converter.lastenausgleichToJAX(lastenausgleich))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Erstellt einen neuen Lastenausgleich und speichert die Grundlagen",
		response = JaxLastenausgleich.class)
	@Nullable
	@GET
	@Path("/create")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxLastenausgleich createLastenausgleich(
		@QueryParam("jahr") String sJahr,
		@QueryParam("selbstbehaltPro100ProzentPlatz") String sSelbstbehaltPro100ProzentPlatz
	) throws EbeguRuntimeException {

		int jahr = Integer.parseInt(sJahr);
		BigDecimal selbstbehaltPro100ProzentPlatz = MathUtil.DEFAULT.from(sSelbstbehaltPro100ProzentPlatz);

		Lastenausgleich lastenausgleich =
			lastenausgleichService.createLastenausgleich(jahr, selbstbehaltPro100ProzentPlatz);
		return converter.lastenausgleichToJAX(lastenausgleich);
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlung'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, ADMIN_BG })
	public Response getLastenausgleichReportExcel(
		@QueryParam("lastenausgleichId") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws ExcelMergeException, EbeguRuntimeException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String lastenausgleichId = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo =
			reportService.generateExcelReportLastenausgleichKibon(lastenausgleichId, Locale.GERMAN);
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}

	@ApiOperation(value = "Erstellt ein CSV Textdokument für den Lastenausgleich", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/csv/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLastenausgleichReportCSV(
		@QueryParam("lastenausgleichId") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String lastenausgleichId = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService.generateCSVReportLastenausgleichKibon(lastenausgleichId);
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}


	@ApiOperation(value = "Erstellt ein Excel mit allen Kinder des angegebenen Jahres mit einer ZEMIS-Nummer",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/zemisexcel/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getZemisExcel(
		@QueryParam("jahr") @Nonnull @Valid Integer lastenausgleichJahr,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws ExcelMergeException, EbeguRuntimeException {

		Objects.requireNonNull(lastenausgleichJahr);
		String ip = downloadResource.getIP(request);

		UploadFileInfo uploadFileInfo = zemisNummerService.generateZemisReport(lastenausgleichJahr, Locale.GERMAN);
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}

	@ApiOperation("Loescht den Lastenausgleich mit der uebergebenen id aus der DB")
	@Nullable
	@DELETE
	@Path("/{lastenausgleichId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeLastenausgleich(
		@Nonnull @NotNull @PathParam("lastenausgleichId") JaxId lastenausgleichJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(lastenausgleichJAXPId.getId());
		final String lastenausgleichId = converter.toEntityId(lastenausgleichJAXPId);
		lastenausgleichService.removeLastenausgleich(lastenausgleichId);
		return Response.ok().build();
	}
}

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
import java.net.URI;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxMahnung;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.services.DownloadFileService;
import ch.dvbern.ebegu.services.EbeguVorlageService;
import ch.dvbern.ebegu.services.ExportService;
import ch.dvbern.ebegu.services.GeneratedDokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.RueckforderungDokumentService;
import ch.dvbern.ebegu.services.VorlageService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.*;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer den Download von Dokumenten
 *
 * Die Services muessen gross geschrieben werden, da es aus dem Client so kommt, weil wir dort ein Enum brauchen.
 */
@SuppressWarnings("InstanceMethodNamingConvention")
@Path("blobs/temp")
@Stateless
@Api(description = "Resource fuer den Download von Dokumenten")
@PermitAll
public class DownloadResource {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadResource.class.getSimpleName());
	public static final String GESUCH_ID_INVALID = "GesuchId invalid: ";

	@Inject
	private DownloadFileService downloadFileService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private DokumentService dokumentService;

	@Inject
	private VorlageService vorlageService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private ExportService exportService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private EbeguVorlageService ebeguVorlageService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private RueckforderungDokumentService rueckforderungDokumentService;


	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	@ApiOperation("L&auml;dt das Dokument herunter, auf welches das &uuml;bergebene accessToken verweist")
	@GET
	@Path("blobdata/{accessToken}")
	//mimetyp wird in buildDownloadResponse erraten
	public Response downloadByAccessToken(
		@PathParam("accessToken") String blobAccessTokenParam,
		@MatrixParam("attachment") @DefaultValue("false") boolean attachment,
		@Context HttpServletRequest request) {

		String ip = getIP(request);

		DownloadFile downloadFile = downloadFileService.getDownloadFileByAccessToken(blobAccessTokenParam);

		if (downloadFile == null) {
			return Response.status(Response.Status.FORBIDDEN).entity("Ung&uuml;ltige Anfrage f&uuml;r download").build();
		}

		if (!downloadFile.getIp().equals(ip)
			|| principalBean.getPrincipal() == null
			|| !principalBean.getPrincipal().getName().equals(downloadFile.getUserErstellt())) {
			// Wir loggen noch ein bisschen, bis wir sicher sind, dass das Problem geloest ist
			StringBuilder sb = new StringBuilder();
			sb.append("Keine Berechtigung fuer Download");
			if (!downloadFile.getIp().equals(ip)) {
				sb.append("; downloadFile.getIp(): ").append(downloadFile.getIp());
				sb.append("; ip").append(ip);
			}
			if (principalBean.getPrincipal() == null) {
				sb.append("; principalBean.getPrincipal() is null");
			} else if (!principalBean.getPrincipal().getName().equals(downloadFile.getUserErstellt())) {
				sb.append("; principalBean.getPrincipal().getName()").append(principalBean.getPrincipal().getName());
				sb.append("; downloadFile.getUserErstellt()").append(downloadFile.getUserErstellt());
			}
			LOG.error(sb.toString());
			return Response.status(Response.Status.FORBIDDEN).entity("Keine Berechtigung f&uuml;r download").build();
		}

		try {
			return RestUtil.buildDownloadResponse(downloadFile, attachment);
		} catch (IOException e) {
			LOG.error("Dokument kann nicht heruntergeladen werden", e);
			return Response.status(Response.Status.NOT_FOUND).entity("Dokument kann nicht gelesen werden").build();
		}
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download eines Dokumentes.")
	@Nonnull
	@GET
	@Path("/{dokumentId}/dokument")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenDokument(
		@Nonnull @Valid @PathParam("dokumentId") JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);

		requireNonNull(jaxId.getId());
		String id = converter.toEntityId(jaxId);

		final FileMetadata dokument = dokumentService.findDokument(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumentAccessTokenDokument", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));

		return getFileDownloadResponse(uriInfo, ip, dokument);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download einer Vorlage.")
	@Nonnull
	@GET
	@Path("/{dokumentId}/vorlage")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenVorlage(
		@Nonnull @Valid @PathParam("dokumentId") JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);

		requireNonNull(jaxId.getId());
		String id = converter.toEntityId(jaxId);

		final FileMetadata dokument = vorlageService.findVorlage(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumentAccessTokenVorlage", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));

		return getFileDownloadResponse(uriInfo, ip, dokument);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download einer Vorlage fuer die Notrecht Rueckforderung")
	@Nonnull
	@GET
	@Path("/NOTRECHTVORLAGEOEFFENTLICH/{language}/{angebotTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenNotrechtvorlageOeffentlicheInstitutionen(
		@Nonnull @Valid @PathParam("language") String language,
		@Nonnull @Valid @PathParam("angebotTyp") BetreuungsangebotTyp angebotTyp,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);
		FileMetadata vorlageNotrecht = ebeguVorlageService.getVorlageNotrechtOeffentlicheInstitutionen(language, angebotTyp);
		return getFileDownloadResponse(uriInfo, ip, vorlageNotrecht);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download einer Vorlage fuer die Notrecht Rueckforderung")
	@Nonnull
	@GET
	@Path("/NOTRECHTVORLAGEPRIVAT/{language}/{angebotTyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenNotrechtvorlagePrivateInstitutionen(
		@Nonnull @Valid @PathParam("language") String language,
		@Nonnull @Valid @PathParam("angebotTyp") BetreuungsangebotTyp angebotTyp,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);
		FileMetadata vorlageNotrecht = ebeguVorlageService.getVorlageNotrechtPrivateInstitutionen(language, angebotTyp);
		return getFileDownloadResponse(uriInfo, ip, vorlageNotrecht);
	}

	/**
	 * Methode fuer alle GeneratedDokumentTyp. Hier wird allgemein mit den Daten vom Gesuch gearbeitet.
	 * Alle anderen Vorlagen, die andere Daten brauchen, muessen ihre eigene Methode haben. So wie bei VERFUEGUNG
	 *
	 * @param jaxGesuchId gesuch ID
	 * @param request request
	 * @param uriInfo uri
	 * @return ein Response mit dem GeneratedDokument
	 */
	@ApiOperation("Erstellt ein Token f&uuml;r den Download der Finanziellen Situation des Gesuchs mit der " +
		"&uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{gesuchid}/FINANZIELLE_SITUATION/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFinSitDokumentAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("gesuchid") JaxId jaxGesuchId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException, MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuch = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		if (gesuch.isPresent()) {
			WriteProtectedDokument generatedDokument = generatedDokumentService
				.getFinSitDokumentAccessTokenGeneratedDokument(gesuch.get(), false);
			return getFileDownloadResponse(uriInfo, ip, generatedDokument);
		}
		throw new EbeguEntityNotFoundException("getFinSitDokumentAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + jaxGesuchId.getId());
	}

	/**
	 * Methode fuer alle GeneratedDokumentTyp. Hier wird es allgemein mit den Daten vom Gesuch gearbeitet.
	 * Alle anderen Vorlagen, die andere Daten brauchen, muessen ihre eigene Methode haben. So wie bei VERFUEGUNG
	 *
	 * @param jaxGesuchId gesuch ID
	 * @param request request
	 * @param uriInfo uri
	 * @return ein Response mit dem GeneratedDokument
	 */
	@ApiOperation("Erstellt ein Token f&uuml;r den Download des Begleitschreibens f&uuml;r das Gesuchs mit der " +
		"&uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{gesuchid}/BEGLEITSCHREIBEN/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBegleitschreibenDokumentAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("gesuchid") JaxId jaxGesuchId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException, MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuch = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		if (gesuch.isPresent()) {
			WriteProtectedDokument generatedDokument = generatedDokumentService.getBegleitschreibenDokument(gesuch.get(), false);
			return getFileDownloadResponse(uriInfo, ip, generatedDokument);
		}
		throw new EbeguEntityNotFoundException("getBegleitschreibenDokumentAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + jaxGesuchId.getId());
	}

	/**
	 * Methode fuer alle GeneratedDokumentTyp. Hier wird es allgemein mit den Daten vom Gesuch gearbeitet.
	 * Alle anderen Vorlagen, die andere Daten brauchen, muessen ihre eigene Methode haben. So wie bei VERFUEGUNG
	 *
	 * @param jaxGesuchId gesuch ID
	 * @param request request
	 * @param uriInfo uri
	 * @return ein Response mit dem GeneratedDokument
	 */
	@ApiOperation("Erstellt ein Token f&uuml;r den Download der kompletten Korrespondenz f&uuml;r das Gesuchs mit der " +
		"&uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{gesuchid}/KOMPLETTEKORRESPONDENZ/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKompletteKorrespondenzAccessTokenGeneratedDokument(

		@Nonnull @Valid @PathParam("gesuchid") JaxId jaxGesuchId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException, MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuch = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		if (gesuch.isPresent()) {
			WriteProtectedDokument generatedDokument = generatedDokumentService.getKompletteKorrespondenz(gesuch.get());
			return getFileDownloadResponse(uriInfo, ip, generatedDokument);
		}
		throw new EbeguEntityNotFoundException("getKompletteKorrespondenzAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + jaxGesuchId.getId());
	}

	/**
	 * Wir benutzen dafuer die Methode getDokumentAccessTokenGeneratedDokument nicht damit man unnoetige Parameter (zustelladresse)
	 * nicht fuer jeden DokumentTyp eingeben muss
	 */
	@ApiOperation("Erstellt ein Token f&uuml;r den Download der Freigabequittung f&uuml;r das Gesuchs mit der " +
		"&uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{gesuchid}/FREIGABEQUITTUNG/{forceCreation}/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFreigabequittungAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("gesuchid") JaxId jaxGesuchId,
		@Nonnull @Valid @PathParam("forceCreation") Boolean forceCreation,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException, MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuchOpt = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		final Gesuch gesuch = gesuchOpt.orElseThrow(() -> new EbeguEntityNotFoundException("getFreigabequittungAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + jaxGesuchId.getId()));

		// Only onlinegesuch have a freigabequittung
		if (gesuch.getEingangsart().isPapierGesuch()) {
			throw new EbeguRuntimeException("getFreigabequittungAccessTokenGeneratedDokument",
				ErrorCodeEnum.ERROR_FREIGABEQUITTUNG_PAPIER, gesuch.getId());
		}

		WriteProtectedDokument generatedDokument = generatedDokumentService
			.getFreigabequittungAccessTokenGeneratedDokument(gesuch, forceCreation);
		return getFileDownloadResponse(uriInfo, ip, generatedDokument);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download der Verf&uuml;gung f&uuml;r die Betreuung mit der " +
		"&uuml;bergebenen Id.")
	@Nonnull
	@POST
	@Path("/{gesuchid}/{betreuungId}/VERFUEGUNG/{forceCreation}/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVerfuegungDokumentAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("gesuchid") JaxId jaxGesuchId,
		@Nonnull @Valid @PathParam("betreuungId") JaxId jaxBetreuungId,
		@Nonnull @Valid @PathParam("forceCreation") Boolean forceCreation,
		@Nullable String manuelleBemerkungen,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException,
		IOException, MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		requireNonNull(jaxBetreuungId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		if (gesuchOptional.isPresent()) {
			Betreuung betreuung = gesuchOptional.get().extractBetreuungById(jaxBetreuungId.getId());
			requireNonNull(betreuung);
			requireNonNull(manuelleBemerkungen);

			WriteProtectedDokument persistedDokument = generatedDokumentService
				.getVerfuegungDokumentAccessTokenGeneratedDokument(gesuchOptional.get(), betreuung, manuelleBemerkungen, forceCreation);
			return getFileDownloadResponse(uriInfo, ip, persistedDokument);

		}
		throw new EbeguEntityNotFoundException("getVerfuegungDokumentAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId not found: " + jaxGesuchId.getId());
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download des Mahnungsbriefs f&uuml;r die " +
		"&uuml;bergebenen Mahnung.")
	@Nonnull
	@PUT
	@Path("/MAHNUNG/generated")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMahnungDokumentAccessTokenGeneratedDokument(
		@Nonnull @NotNull @Valid JaxMahnung jaxMahnung,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException,
		IOException, MimeTypeParseException, MergeDocException {

		requireNonNull(jaxMahnung);
		String ip = getIP(request);

		Mahnung mahnung = converter.mahnungToEntity(jaxMahnung, new Mahnung());
		authorizer.checkReadAuthorization(mahnung.getGesuch());

		WriteProtectedDokument persistedDokument = generatedDokumentService
			.getMahnungDokumentAccessTokenGeneratedDokument(mahnung, false);

		return getFileDownloadResponse(uriInfo, ip, persistedDokument);

	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download der Nichteintretens-Verf&uuml;gung f&uuml;r die " +
		"Betreuung mit der  &uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{betreuungId}/NICHTEINTRETEN/{forceCreation}/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNichteintretenDokumentAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("betreuungId") JaxId jaxBetreuungId,
		@Nonnull @Valid @PathParam("forceCreation") Boolean forceCreation,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException,
		IOException, MimeTypeParseException, MergeDocException {

		requireNonNull(jaxBetreuungId);
		String ip = getIP(request);

		Betreuung betreuung = betreuungService.findBetreuung(jaxBetreuungId.getId()).orElseThrow(()
			-> new EbeguEntityNotFoundException("getNichteintretenDokumentAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, jaxBetreuungId.getId()));
		WriteProtectedDokument persistedDokument = generatedDokumentService
			.getNichteintretenDokumentAccessTokenGeneratedDokument(betreuung, forceCreation);
		return getFileDownloadResponse(uriInfo, ip, persistedDokument);
	}

	/**
	 * Diese Methode generiert eine Textdatei mit einem JSON String darin welche heruntergeladen werden kann.
	 * Dazu wird das File fuer die entsprechende Betreuung generiert und auf dem Server fuer eine gewisse Zeit
	 * zum download bereitgestellt.
	 */
	@ApiOperation(value = "Generate Exportfile of a Verfuegung and return Token a token to download the generated file",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/{betreuungId}/EXPORT")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenVerfuegungExport(
		@Nonnull @Valid @PathParam("betreuungId") JaxId jaxBetreuungId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {
		requireNonNull(jaxBetreuungId);
		String ip = getIP(request);

		UploadFileInfo uploadFileInfo = exportService.exportVerfuegungOfBetreuungAsFile(converter.toEntityId(jaxBetreuungId));
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);
		return this.getFileDownloadResponse(uriInfo, ip, downloadFileInfo);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download des Zahlungsfiles (ISO20022) f&uuml;r die " +
		"Zahlung mit der &uuml;bergebenen Id.")
	@Nonnull
	@GET
	@Path("/{zahlungsauftragId}/PAIN001/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getPain001AccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("zahlungsauftragId") JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MimeTypeParseException {

		requireNonNull(jaxId.getId());
		String ip = getIP(request);

		final Optional<Zahlungsauftrag> zahlungsauftrag = zahlungService.findZahlungsauftrag(converter.toEntityId(jaxId));
		if (zahlungsauftrag.isPresent()) {

			WriteProtectedDokument persistedDokument = generatedDokumentService
				.getPain001DokumentAccessTokenGeneratedDokument(zahlungsauftrag.get(), false);
			return getFileDownloadResponse(uriInfo, ip, persistedDokument);

		}
		throw new EbeguEntityNotFoundException("getPain001AccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "ZahlungsauftragId invalid: " + jaxId.getId());
	}

	@Nonnull
	public Response getFileDownloadResponse(UriInfo uriInfo, String ip, FileMetadata fileMetadata) {
		final DownloadFile downloadFile = downloadFileService.create(fileMetadata, ip);

		URI uri = createDownloadURIForDownloadFile(uriInfo, downloadFile);

		JaxDownloadFile jaxDownloadFile = converter.downloadFileToJAX(downloadFile);

		return Response.created(uri).entity(jaxDownloadFile).build();
	}

	private URI createDownloadURIForDownloadFile(UriInfo uriInfo, DownloadFile downloadFile) {
		return uriInfo.getBaseUriBuilder()
				.path(DownloadResource.class)
				.path("/blobdata")
				.path('/' + downloadFile.getAccessToken())
				.build();
	}

	public String getIP(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		if (ipAddress.contains(",")) {
			String[] adresses = ipAddress.split(",");
			return adresses[0];
		} else {
			return ipAddress;
		}
	}

	@ApiOperation("Erstellt ein Token fuer den Download der Anmeldebestaetigung fuer die Betreuung mit der " +
		"gebenen Id.")
	@Nonnull
	@GET
	@Path("/{gesuchId}/{anmeldungId}/ANMELDEBESTAETIGUNG/{forceCreation}/{mitTarif}/generated")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		GESUCHSTELLER, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getAnmeldebestaetigungDokumentAccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("gesuchId") JaxId jaxGesuchId,
		@Nonnull @Valid @PathParam("anmeldungId") JaxId jaxAnmledungId,
		@Nonnull @Valid @PathParam("forceCreation") Boolean forceCreation,
		@Nonnull @Valid @PathParam("mitTarif") Boolean mitTarif,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) throws EbeguEntityNotFoundException, MergeDocException,
		MimeTypeParseException {

		requireNonNull(jaxGesuchId.getId());
		requireNonNull(jaxAnmledungId.getId());
		String ip = getIP(request);

		Gesuch gesuch = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId))
			.orElseThrow(() -> new EbeguEntityNotFoundException("getAnmeldebestaetigungDokumentAccessTokenGeneratedDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + jaxGesuchId.getId()));

		AbstractAnmeldung anmeldung =
			betreuungService.findAnmeldung(converter.toEntityId(jaxAnmledungId))
				.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getAnmeldebestaetigungDokumentAccessTokenGeneratedDokument",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				jaxAnmledungId.getId()));;

		WriteProtectedDokument persistedDokument = generatedDokumentService
			.getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(gesuch, anmeldung, mitTarif, forceCreation);
		return getFileDownloadResponse(uriInfo, ip, persistedDokument);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download eines Dokumentes.")
	@Nonnull
	@GET
	@Path("/{dokumentId}/rueckforderungDokument")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenRueckforderungDokument(
		@Nonnull @Valid @PathParam("dokumentId") JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);

		requireNonNull(jaxId.getId());
		String id = converter.toEntityId(jaxId);

		final FileMetadata dokument = rueckforderungDokumentService.findDokument(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumentAccessTokenDokument", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));

		return getFileDownloadResponse(uriInfo, ip, dokument);
	}
}

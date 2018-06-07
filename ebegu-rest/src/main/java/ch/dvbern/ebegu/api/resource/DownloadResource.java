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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxMahnung;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
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
import ch.dvbern.ebegu.services.VorlageService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer den Download von Dokumenten
 *
 * Die Services muessen gross geschrieben werden, da es aus dem Client so kommt, weil wir dort ein Enum brauchen.
 */
@SuppressWarnings("InstanceMethodNamingConvention")
@Path("blobs/temp")
@Stateless
@Api(description = "Resource fuer den Download von Dokumenten")
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
	private Persistence persistence;

	@Inject
	private EbeguVorlageService ebeguVorlageService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

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

		Validate.notNull(jaxId.getId());
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

		Validate.notNull(jaxId.getId());
		String id = converter.toEntityId(jaxId);

		final FileMetadata dokument = vorlageService.findVorlage(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumentAccessTokenVorlage", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));

		return getFileDownloadResponse(uriInfo, ip, dokument);
	}

	@ApiOperation("Erstellt ein Token f&uuml;r den Download des Benutzerhandbuchs. Es wird je nach Rolle des " +
		"eingeloggten Benutzers ein anderes Benutzerhandbuch zur&uuml;ckgegeben")
	@Nonnull
	@GET
	@Path("/BENUTZERHANDBUCH")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDokumentAccessTokenBenutzerhandbuch(
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException {

		String ip = getIP(request);
		FileMetadata benutzerhandbuch = ebeguVorlageService.getBenutzerhandbuch();
		return getFileDownloadResponse(uriInfo, ip, benutzerhandbuch);
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

		Validate.notNull(jaxGesuchId.getId());
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

		Validate.notNull(jaxGesuchId.getId());
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

		Validate.notNull(jaxGesuchId.getId());
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

		Validate.notNull(jaxGesuchId.getId());
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
		@Nonnull @NotNull @Valid String manuelleBemerkungen,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MergeDocException,
		IOException, MimeTypeParseException {

		Validate.notNull(jaxGesuchId.getId());
		Validate.notNull(jaxBetreuungId.getId());
		String ip = getIP(request);

		final Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(jaxGesuchId));
		if (gesuchOptional.isPresent()) {
			Betreuung betreuung = gesuchOptional.get().extractBetreuungById(jaxBetreuungId.getId());

			// Wir verwenden das Gesuch nur zur Berechnung und wollen nicht speichern, darum das Gesuch detachen
			loadRelationsAndDetach(gesuchOptional.get());

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

		Validate.notNull(jaxMahnung);
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

		Validate.notNull(jaxBetreuungId);
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
		Validate.notNull(jaxBetreuungId);
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
	public Response getPain001AccessTokenGeneratedDokument(
		@Nonnull @Valid @PathParam("zahlungsauftragId") JaxId jaxId,
		@Context HttpServletRequest request, @Context UriInfo uriInfo) throws EbeguEntityNotFoundException, MimeTypeParseException {

		Validate.notNull(jaxId.getId());
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
		return ipAddress;
	}

	/**
	 * Hack, welcher das Gesuch detached, damit es auf keinen Fall gespeichert wird. Vorher muessen die Lazy geloadeten
	 * BetreuungspensumContainers geladen werden, da danach keine Session mehr zur Verfuegung steht!
	 */
	private void loadRelationsAndDetach(Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			if (kindContainer.getBetreuungen() != null) {
				for (Betreuung betreuung : kindContainer.getBetreuungen()) {
					betreuung.getBetreuungspensumContainers().size();
					betreuung.getAbwesenheitContainers().size();
				}
			}
		}
		if (gesuch.getGesuchsteller1() != null) {
			gesuch.getGesuchsteller1().getErwerbspensenContainers().size();
			gesuch.getGesuchsteller1().getAdressen().size();
		}
		if (gesuch.getGesuchsteller2() != null) {
			gesuch.getGesuchsteller2().getErwerbspensenContainers().size();
			gesuch.getGesuchsteller2().getAdressen().size();
		}
		persistence.getEntityManager().detach(gesuch);
	}
}

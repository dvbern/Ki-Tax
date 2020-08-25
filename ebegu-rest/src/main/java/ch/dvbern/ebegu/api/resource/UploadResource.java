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
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDokument;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungDokument;
import ch.dvbern.ebegu.api.resource.util.MultipartFormToFileConverter;
import ch.dvbern.ebegu.api.resource.util.TransferFile;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.RueckforderungDokument;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.RueckforderungDokumentTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.RueckforderungDokumentService;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.tika.Tika;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource zum Upload von Dokumenten
 */
@SuppressWarnings("OverlyBroadCatchBlock")
@Path("upload")
@Stateless
@Api(description = "Resource zum Upload von Dokumenten")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class UploadResource {

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchService gesuchService;
	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private RueckforderungDokumentService rueckforderungDokumentService;

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	private static final String PART_FILE = "file";
	private static final String PART_DOKUMENT_GRUND = "dokumentGrund";

	private static final String FILENAME_HEADER = "x-filename";
	private static final String GESUCHID_HEADER = "x-gesuchID";

	private static final Logger LOG = LoggerFactory.getLogger(UploadResource.class);

	@ApiOperation(value = "Speichert ein oder mehrere Dokumente in der Datenbank", response = JaxDokumentGrund.class)
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@PermitAll
	public Response uploadFiles(@Context HttpServletRequest request, @Context UriInfo uriInfo, MultipartFormDataInput input)
		throws IOException, MimeTypeParseException {

		request.setAttribute(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "*/*; charset=UTF-8");

		String[] encodedFilenames = getFilenamesFromHeader(request);

		// check if filenames available
		if (encodedFilenames == null || encodedFilenames.length == 0) {
			final String problemString = "filename must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get GesuchId from header
		String gesuchId = request.getHeader(GESUCHID_HEADER);
		if (StringUtils.isEmpty(gesuchId)) {
			final String problemString = "a valid gesuchID must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get DokumentGrund Object from form-paramter
		List<InputPart> inputPartsDG = input.getFormDataMap().get(PART_DOKUMENT_GRUND);
		if (inputPartsDG == null || !inputPartsDG.stream().findAny().isPresent()) {
			final String problemString = "form-parameter 'inputPartsDG' not found";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Convert DokumentGrund from inputStream
		JaxDokumentGrund jaxDokumentGrund;
		try (InputStream dokGrund = input.getFormDataPart(PART_DOKUMENT_GRUND, InputStream.class, null)) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			jaxDokumentGrund = mapper.readValue(IOUtils.toString(dokGrund, StandardCharsets.UTF_8), JaxDokumentGrund.class);
		} catch (IOException e) {
			final String problemString = "Can't parse DokumentGrund from Jax to object";
			LOG.error(problemString, e);
			return Response.serverError().entity(problemString).build();
		}

		if (jaxDokumentGrund == null) {
			final String problemString = "\"Can't parse DokumentGrund from Jax to object";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();

		}

		// jaxDokumentGrund ist jetzt u.U. noch in einem falschen Zustand. Wir müssen es neu von der Datenbank lesen
		// Die neu hochgeladenen Files gehen nicht verloren, sie befinden sich im "input"
		DokumentGrund dokumentGrundToMerge = new DokumentGrund();
		if (jaxDokumentGrund.getId() != null) {
			Optional<DokumentGrund> existingDokumentGrundOptional = dokumentGrundService.findDokumentGrund(jaxDokumentGrund.getId());
			if (existingDokumentGrundOptional.isPresent()) {
				dokumentGrundToMerge = existingDokumentGrundOptional.get();
				jaxDokumentGrund = converter.dokumentGrundToJax(dokumentGrundToMerge);
			}
		}

		extractFilesFromInput(input, encodedFilenames, gesuchId, jaxDokumentGrund);

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchId);
		if (!gesuch.isPresent()) {
			final String problemString = "Can't find Gesuch on DB";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		DokumentGrund convertedDokumentGrund = converter.dokumentGrundToEntity(jaxDokumentGrund, dokumentGrundToMerge);
		convertedDokumentGrund.setGesuch(gesuch.get());

		// save modified Dokument to DB
		DokumentGrund persistedDokumentGrund = dokumentGrundService.saveDokumentGrund(convertedDokumentGrund);

		final JaxDokumentGrund jaxDokumentGrundToReturn = converter.dokumentGrundToJax(persistedDokumentGrund);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(UploadResource.class)
			.path('/' + persistedDokumentGrund.getId())
			.build();

		return Response.created(uri).entity(jaxDokumentGrundToReturn).build();
	}

	@ApiOperation(value = "Speichert ein oder mehrere RueckforderungsDokument in der Datenbank", response =
		JaxRueckforderungDokument.class)
	@Path("/uploadRueckforderungsDokument/{rueckforderungFormularId}/{rueckforderungDokumentTyp}")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public Response uploadRueckforderungsFiles(
		@Nonnull @NotNull @PathParam("rueckforderungFormularId") JaxId rueckforderungFormularJAXPId,
		@Nonnull @NotNull @PathParam("rueckforderungDokumentTyp") RueckforderungDokumentTyp rueckforderungDokumentTyp,
		@Context HttpServletRequest request, @Context UriInfo uriInfo,
		MultipartFormDataInput input)
		throws IOException, MimeTypeParseException {

		request.setAttribute(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "*/*; charset=UTF-8");

		String[] encodedFilenames = getFilenamesFromHeader(request);

		// check if filenames available
		if (encodedFilenames == null || encodedFilenames.length == 0) {
			final String problemString = "filename must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// Get RueckforderungId from request Parameter
		String rueckforderungId = converter.toEntityId(rueckforderungFormularJAXPId);

		Optional<RueckforderungFormular> rueckforderungFormular = rueckforderungFormularService.findRueckforderungFormular(rueckforderungId);
		if (!rueckforderungFormular.isPresent()) {
			final String problemString = "Can't find RueckforderungFormular on DB";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		// for every file create a new RueckforderungsDokument linked with the given RueckforderungsFormular
		List<JaxRueckforderungDokument> jaxRueckforderungDokuments =
			extractFilesFromInputAndCreateRueckforderungsDokumenten(encodedFilenames, input, rueckforderungFormular.get(), rueckforderungDokumentTyp);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(UploadResource.class)
			.path('/' + rueckforderungFormular.get().getId())
			.build();

		return Response.created(uri).entity(jaxRueckforderungDokuments).build();
	}

	@ApiOperation("Stores the Erlaeuterungen zu Verfuegung pdf of the Gesuchsperiode with the given id and Sprache")
	@POST
	@Path("/gesuchsperiodeDokument/{sprache}/{periodeId}/{dokumentTyp}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public Response saveGesuchsperiodeDokument(
		@Nonnull @NotNull @PathParam("sprache") Sprache sprache,
		@Nonnull @NotNull @PathParam("periodeId") String periodeId,
		@Nonnull @NotNull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Nonnull @NotNull MultipartFormDataInput input) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);
		Validate.notEmpty(fileList, "Need to upload something");

		gesuchsperiodeService.uploadGesuchsperiodeDokument(periodeId, sprache, dokumentTyp,
			fileList.get(0).getContent());

		return Response.ok().build();
	}

	@ApiOperation("Stores Dokument of Typ dokumentTyp  for a Gemeinde and a Gesuchsperiode")
	@POST
	@Path("/gemeindeGesuchsperiodeDoku/{gemeindeId}/{gesuchsperiodeId}/{sprache}/{dokumentTyp}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response uploadGemeindeGesuchsperiodeDokument(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Nonnull @PathParam("dokumentTyp") DokumentTyp dokumentTyp,
		@Nonnull @NotNull MultipartFormDataInput input) {

		List<TransferFile> fileList = MultipartFormToFileConverter.parse(input);

		Validate.notEmpty(fileList, "Need to upload something");

		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJAXPId);

		TransferFile file = fileList.get(0);

		gemeindeService.uploadGemeindeGesuchsperiodeDokument(gemeindeId, gesuchsperiodeId, sprache, dokumentTyp, file.getContent());

		return Response.ok().build();
	}

	@Nullable
	private String[] getFilenamesFromHeader(@Context HttpServletRequest request) {
		String filenamesJson = request.getHeader(FILENAME_HEADER);
		String[] filenames = null;
		if (!StringUtils.isEmpty(filenamesJson)) {
			filenames = filenamesJson.split(";");
		}
		return filenames;
	}

	private void extractFilesFromInput(
		MultipartFormDataInput input,
		String[] encodedFilenames,
		String gesuchId,
		JaxDokumentGrund jaxDokumentGrund) throws MimeTypeParseException, IOException {

		int filecounter = 0;
		String partrileName = PART_FILE + '[' + filecounter + ']';

		// do for every file:
		List<InputPart> inputParts = input.getFormDataMap().get(partrileName);
		while (inputParts != null && inputParts.stream().findAny().isPresent()) {
			UploadFileInfo fileInfo = RestUtil.parseUploadFile(inputParts.stream().findAny().get());

			// evil workaround, (Umlaute werden sonst nicht richtig übertragen!)
			if (encodedFilenames[filecounter] != null) {
				String decodedFilenamesJson =
					new String(Base64.getDecoder().decode(encodedFilenames[filecounter]), StandardCharsets.UTF_8);
				fileInfo.setFilename(decodedFilenamesJson);
			}

			try (InputStream fileInputStream = input.getFormDataPart(partrileName, InputStream.class, null)) {
				fileInfo.setBytes(IOUtils.toByteArray(fileInputStream));
			}

			// safe File to Filesystem, if we just analyze the input stream tika classifies all files as octet streams
			fileSaverService.save(fileInfo, gesuchId);
			checkFiletypeAllowed(fileInfo);

			// add the new file to DokumentGrund object
			addFileToDokumentGrund(jaxDokumentGrund, fileInfo);

			filecounter++;
			partrileName = PART_FILE + '[' + filecounter + ']';
			inputParts = input.getFormDataMap().get(partrileName);
		}
	}

	private List<JaxRueckforderungDokument> extractFilesFromInputAndCreateRueckforderungsDokumenten(
		@Nonnull String[] encodedFilenames,
		@Nonnull MultipartFormDataInput input,
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull RueckforderungDokumentTyp rueckforderungDokumentTyp
	) throws MimeTypeParseException, IOException {

		int filecounter = 0;
		String partrileName = PART_FILE + '[' + filecounter + ']';

		List<JaxRueckforderungDokument> rueckforderungJaxDokuments = new ArrayList<>();

		// do for every file:
		List<InputPart> inputParts = input.getFormDataMap().get(partrileName);
		while (inputParts != null && inputParts.stream().findAny().isPresent()) {
			UploadFileInfo fileInfo = RestUtil.parseUploadFile(inputParts.stream().findAny().get());

			// evil workaround, (Umlaute werden sonst nicht richtig übertragen!)
			if (encodedFilenames[filecounter] != null) {
				String decodedFilenamesJson =
					new String(Base64.getDecoder().decode(encodedFilenames[filecounter]), StandardCharsets.UTF_8);
				fileInfo.setFilename(decodedFilenamesJson);
			}

			try (InputStream fileInputStream = input.getFormDataPart(partrileName, InputStream.class, null)) {
				fileInfo.setBytes(IOUtils.toByteArray(fileInputStream));
			}

			// safe File to Filesystem, if we just analyze the input stream tika classifies all files as octet streams
			fileSaverService.save(fileInfo, rueckforderungFormular.getId());
			checkFiletypeAllowed(fileInfo);

			// create and add the new file to RueckforderungsDokument object and persist it
			RueckforderungDokument rueckforderungDokument = new RueckforderungDokument();
			rueckforderungDokument.setRueckforderungDokumentTyp(rueckforderungDokumentTyp);
			rueckforderungDokument.setRueckforderungFormular(rueckforderungFormular);
			rueckforderungDokument.setFilepfad(fileInfo.getPath());
			rueckforderungDokument.setFilename(fileInfo.getFilename());
			rueckforderungDokument.setFilesize(fileInfo.getSizeString());

			// when uploading a new document we check the flag so we know that something has been uploaded
			if (rueckforderungFormular.getStatus() == RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
				rueckforderungFormular.getStatus() == RueckforderungStatus.VERFUEGT_PROVISORISCH ) {
				rueckforderungFormular.setUncheckedDocuments(true);
			}

			RueckforderungDokument documentFromDB =
				rueckforderungDokumentService.saveDokumentGrund(rueckforderungDokument);

			rueckforderungJaxDokuments.add(converter.rueckforderungDokumentToJax(documentFromDB));

			filecounter++;
			partrileName = PART_FILE + '[' + filecounter + ']';
			inputParts = input.getFormDataMap().get(partrileName);
		}

		return rueckforderungJaxDokuments;
	}

	private void checkFiletypeAllowed(UploadFileInfo fileInfo) {
		//we dont purly trust the filetype set in the header, so we perform our own content-type guessing
		java.nio.file.Path filePath = Paths.get(fileInfo.getPath());
		try {
			Tika tika = new Tika();
			String contentType = tika.detect(filePath); //tika should be more accurate than Files.probeContentType
			final MimeType mimeType = fileInfo.getContentType();
			if (contentType == null || mimeType == null || !contentType.equals(mimeType.toString())) {
				LOG.warn("Content type from Header did not match content type returned from probing. "
					+ "\n\t header:   {} \n\t probing:  {}", mimeType, contentType);
			}
			if (!applicationPropertyService.readMimeTypeWhitelist().contains(contentType)) {
				fileSaverService.remove(fileInfo.getPath());
				String message = "Blocked upload of filetype that is not in whitelist: " + contentType;
				throw new EbeguRuntimeException(
					KibonLogLevel.INFO,
					"checkFiletypeAllowed",
					message,
					ErrorCodeEnum.ERROR_UPLOAD_INVALID_FILETYPE,
					contentType);
			}

		} catch (IOException e) {
			LOG.warn("Could not probe file for its content-type, check was omitted", e);
		}
	}

	private void addFileToDokumentGrund(JaxDokumentGrund jaxDokumentGrund, UploadFileInfo uploadFileInfo) {
		Objects.requireNonNull(jaxDokumentGrund.getDokumente());

		for (JaxDokument jaxDokument : jaxDokumentGrund.getDokumente()) {

			if (null == jaxDokument.getFilename() ||
				jaxDokument.getFilename().isEmpty()) {

				//set to existing
				jaxDokument.setFilename(uploadFileInfo.getFilename());
				jaxDokument.setFilepfad(uploadFileInfo.getPath());
				jaxDokument.setFilesize(uploadFileInfo.getSizeString());
				LOG.info(
					"Replace placeholder on {} by file {}",
					jaxDokumentGrund.getDokumentTyp(),
					uploadFileInfo.getFilename());
				return;
			}
		}

		//add new
		JaxDokument dokument = new JaxDokument();
		dokument.setFilename(uploadFileInfo.getFilename());
		dokument.setFilepfad(uploadFileInfo.getPath());
		dokument.setFilesize(uploadFileInfo.getSizeString());
		jaxDokumentGrund.getDokumente().add(dokument);
		LOG.info("Add on {} file {}", jaxDokumentGrund.getDokumentTyp(), uploadFileInfo.getFilename());
	}

}

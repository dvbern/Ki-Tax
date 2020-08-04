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
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxEbeguVorlage;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxVorlage;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.EbeguVorlage;
import ch.dvbern.ebegu.enums.EbeguVorlageKey;
import ch.dvbern.ebegu.services.EbeguVorlageService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Dokument-Vorlagen
 */
@Path("ebeguVorlage")
@Stateless
@Api(description = "Resource fuer Dokument-Vorlagen")
@PermitAll
public class EbeguVorlageResource {

	private static final String PART_FILE = "file";
	private static final String FILENAME_HEADER = "x-filename";
	private static final String VORLAGE_KEY_HEADER = "x-vorlagekey";

	private static final Logger LOG = LoggerFactory.getLogger(EbeguVorlageResource.class);

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@Inject
	private EbeguVorlageService ebeguVorlageService;

	@Inject
	private FileSaverService fileSaverService;

	@ApiOperation(value = "Gibt alle Vorlagen zurueck, welche nicht zu einer Gesuchsperiode gehoeren.",
		responseContainer = "List", response = JaxEbeguVorlage.class)
	@Nullable
	@GET
	@Path("/nogesuchsperiode/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxEbeguVorlage> getEbeguVorlagenWithoutGesuchsperiode() {

		List<EbeguVorlage> persistedEbeguVorlagen = new ArrayList<>(ebeguVorlageService
			.getALLEbeguVorlageByDate(LocalDate.now()));

		Collections.sort(persistedEbeguVorlagen);

		return persistedEbeguVorlagen.stream()
			.map(ebeguVorlage -> converter.ebeguVorlageToJax(ebeguVorlage))
			.collect(Collectors.toList());
	}

	@ApiOperation("Speichert eine Vorlage in der Datenbank")
	@POST
	@SuppressWarnings("PMD.NcssMethodCount")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response save(@Context HttpServletRequest request, @Context UriInfo uriInfo, MultipartFormDataInput input)
		throws IOException, MimeTypeParseException {

		request.setAttribute(InputPart.DEFAULT_CONTENT_TYPE_PROPERTY, "*/*; charset=UTF-8");

		String filename = request.getHeader(FILENAME_HEADER);

		// check if filename available
		if (filename == null || filename.isEmpty()) {
			final String problemString = "filename must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		EbeguVorlageKey ebeguVorlageKey;
		try {
			ebeguVorlageKey = EbeguVorlageKey.valueOf(request.getHeader(VORLAGE_KEY_HEADER));
		} catch (IllegalArgumentException e) {
			final String problemString = "ebeguVorlageKey must be given";
			LOG.error(problemString);
			return Response.serverError().entity(problemString).build();
		}

		List<InputPart> inputParts = input.getFormDataMap().get(PART_FILE);
		if (inputParts == null || !inputParts.stream().findAny().isPresent()) {
			return Response.serverError().entity("form-parameter 'file' not found").build();
		}

		UploadFileInfo fileInfo = RestUtil.parseUploadFile(inputParts.stream().findAny().get());

		// evil workaround, (Umlaute werden sonst nicht richtig übertragen!)
		String decodedFilenames = new String(Base64.getDecoder().decode(filename), Charset.forName("UTF-8"));
		fileInfo.setFilename(decodedFilenames);

		try (InputStream file = input.getFormDataPart(PART_FILE, InputStream.class, null)) {
			fileInfo.setBytes(IOUtils.toByteArray(file));
		}

		// safe File to Filesystem
		fileSaverService.save(fileInfo, "vorlagen");

		JaxEbeguVorlage jaxEbeguVorlage = new JaxEbeguVorlage();
		jaxEbeguVorlage.setName(ebeguVorlageKey);
		jaxEbeguVorlage.setGueltigAb(Constants.START_OF_TIME);
		jaxEbeguVorlage.setGueltigBis(Constants.END_OF_TIME);
		jaxEbeguVorlage.setVorlage(new JaxVorlage());
		jaxEbeguVorlage.getVorlage().setFilename(fileInfo.getFilename());
		jaxEbeguVorlage.getVorlage().setFilepfad(fileInfo.getPath());
		jaxEbeguVorlage.getVorlage().setFilesize(fileInfo.getSizeString());

		final Optional<EbeguVorlage> ebeguVorlageOptional = ebeguVorlageService.getEbeguVorlageByDatesAndKey(jaxEbeguVorlage.getGueltigAb(),
			jaxEbeguVorlage.getGueltigBis(), jaxEbeguVorlage.getName());
		EbeguVorlage ebeguVorlageToMerge = ebeguVorlageOptional.orElse(new EbeguVorlage());

		EbeguVorlage ebeguVorlageConverted = converter.ebeguVorlageToEntity(jaxEbeguVorlage, ebeguVorlageToMerge);

		// save modified EbeguVorlage to DB
		EbeguVorlage persistedEbeguVorlage = ebeguVorlageService.updateEbeguVorlage(ebeguVorlageConverted);

		final JaxEbeguVorlage jaxEbeguVorlageToReturn = converter.ebeguVorlageToJax(persistedEbeguVorlage);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(EbeguVorlageResource.class)
			.path('/' + persistedEbeguVorlage.getId())
			.build();

		return Response.created(uri).entity(jaxEbeguVorlageToReturn).build();
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@ApiOperation("Loescht die Vorlage mit der uebergebenen Id aus der Datenbank.")
	@Nullable
	@DELETE
	@Path("/{ebeguVorlageId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public Response removeEbeguVorlage(
		@Nonnull @NotNull @PathParam("ebeguVorlageId") JaxId ebeguVorlageId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(ebeguVorlageId.getId());
		ebeguVorlageService.removeVorlage(converter.toEntityId(ebeguVorlageId));
		return Response.ok().build();
	}
}

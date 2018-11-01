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

package ch.dvbern.ebegu.api.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.google.common.net.UrlEscapers;

import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxZahlungsauftrag;
import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.UploadFileInfo;

import static ch.dvbern.ebegu.api.EbeguApplicationV1.API_ROOT_PATH;

/**
 * Allgemeine Utils fuer Rest Funktionalitaeten
 */
public final class RestUtil {

	private static final Pattern MATCH_QUOTE = Pattern.compile("\"");
	private static final String BLOB_DOWNLOAD_PATH = "/blobs/temp/blobdata/";
	private static final String LOGO_DOWNLOAD_PATH = "/gemeinde/logo/data/";

	private RestUtil() {
		//nop
	}

	/**
	 * Parst den Content-Disposition Header
	 *
	 * @param part aus einem {@link MultipartFormDataInput}. Bei keinem Filename oder einem leeren Filename wird dieser auf null reduziert.
	 */
	@Nonnull
	public static UploadFileInfo parseUploadFile(@Nonnull InputPart part) throws MimeTypeParseException {
		Objects.requireNonNull(part);

		MultivaluedMap<String, String> headers = part.getHeaders();
		String[] contentDispositionHeader = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION).split(";");
		String filename = null;
		String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
		for (String name : contentDispositionHeader) {
			if (name.toLowerCase(Locale.US).trim().startsWith("filename")) {
				String[] tmp = name.split("=");
				filename = MATCH_QUOTE.matcher(tmp[1].trim()).replaceAll("");
			}
		}
		return new UploadFileInfo(StringUtils.defaultIfBlank(filename, null), new MimeType(contentType));
	}

	public static boolean isFileDownloadRequest(@Nonnull HttpServletRequest request) {
		String context = request.getContextPath() + API_ROOT_PATH;
		final String blobdataPath = context + BLOB_DOWNLOAD_PATH;
		final String logoPath = context + LOGO_DOWNLOAD_PATH;
		return request.getRequestURI().startsWith(blobdataPath) || request.getRequestURI().startsWith(logoPath);
	}

	public static Response buildDownloadResponse(boolean attachment, String filename, String filetype, byte[] content) throws IOException {
		String contentType = (filetype == null) ? "application/octet-stream" : filetype;
		return buildResponse(attachment, contentType, content, filename);
	}


	public static Response buildDownloadResponse(FileMetadata fileMetadata, boolean attachment) throws IOException {
		Path filePath = Paths.get(fileMetadata.getFilepfad());
		//if no guess can be made assume application/octet-stream
		String contentType = Files.probeContentType(filePath);
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		final byte[] content = Files.readAllBytes(filePath);
		String filename = fileMetadata.getFilename();
		return buildResponse(attachment, contentType, content, filename);
	}

	private static Response buildResponse(boolean attachment, String contentType, byte[] content, String filename) throws UnsupportedEncodingException {
		//Prepare Headerfield Content-Disposition:
		//we want percantage-encoding instead of url-encoding (spaces are %20 in percentage encoding but + in url-encoding)
		String isoEncodedFilename = URLEncoder.encode(filename, "ISO-8859-1").replace("+", "%20"); //percantage encoding mit utf-8 und %20 fuer space statt +
		// because of a bug in chrome, we replace all commas in filename
		String utfEncodedFilename = UrlEscapers.urlFragmentEscaper().escape(filename).replace(",", "");
		String simpleFilename = "filename=\"" + isoEncodedFilename + "\"; "; //iso8859-1 (default) filename for old browsers
		String filenameStarParam = "filename*=UTF-8''" + utfEncodedFilename;   //utf-8 url encoded filename https://tools.ietf.org/html/rfc5987
		String disposition = (attachment ? "attachment; " : "inline;") + simpleFilename + filenameStarParam;

		return Response.ok(content)
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
			.header(HttpHeaders.CONTENT_LENGTH, content.length)
			.type(MediaType.valueOf(contentType)).build();
	}

	/**
	 * Entfernt von der uebergebenen Collection von KindContainer die Kinder, die keine Betreuung mit einer der uebergebenen Institutionen hat.
	 *
	 * @param kindContainers Alle KindContainers
	 * @param userInstitutionen Institutionen mit denen, die Kinder eine Beziehung haben muessen.
	 */
	public static void purgeKinderAndBetreuungenOfInstitutionen(Collection<JaxKindContainer> kindContainers, Collection<Institution> userInstitutionen) {
		final Iterator<JaxKindContainer> kindsIterator = kindContainers.iterator();
		while (kindsIterator.hasNext()) {
			final JaxKindContainer kind = kindsIterator.next();
			purgeSingleKindAndBetreuungenOfInstitutionen(kind, userInstitutionen);
			if (kind.getBetreuungen().isEmpty()) {
				kindsIterator.remove();
			}
		}
	}

	public static void purgeSingleKindAndBetreuungenOfInstitutionen(JaxKindContainer kind, Collection<Institution> userInstitutionen) {
		kind.getBetreuungen()
			.removeIf(betreuung ->
				!RestUtil.isInstitutionInList(userInstitutionen, betreuung.getInstitutionStammdaten().getInstitution())
					|| !isVisibleForInstOrTraegerschaft(betreuung)
					|| isSchulamtAngebotNichtAusgeloest(betreuung));
	}

	private static boolean isVisibleForInstOrTraegerschaft(JaxBetreuung betreuung) {
		// Admin wird nicht extra abgefragt. Wenn SACHBEARBEITER okay, ist ADMIN auch berechtigt
		return Betreuungsstatus.allowedRoles(UserRole.SACHBEARBEITER_INSTITUTION).contains(betreuung.getBetreuungsstatus()) ||
			Betreuungsstatus.allowedRoles(UserRole.SACHBEARBEITER_TRAEGERSCHAFT).contains(betreuung.getBetreuungsstatus());
	}

	/**
	 * returns true if it is a Schulamangebot but the status is still ERFASST
	 */
	private static boolean isSchulamtAngebotNichtAusgeloest(JaxBetreuung betreuung) {
		return betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp().isSchulamt() &&
		Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST == betreuung.getBetreuungsstatus();
	}

	private static boolean isInstitutionInList(Collection<Institution> userInstitutionen, JaxInstitution institutionToLookFor) {
		for (final Institution institutionInList : userInstitutionen) {
			if (institutionInList.getId().equals(institutionToLookFor.getId())) {
				return true;
			}
		}
		return false;
	}

	public static Response sendErrorNotAuthorized() {
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	public static void purgeZahlungenOfInstitutionen(JaxZahlungsauftrag jaxZahlungsauftrag, Collection<Institution> allowedInst) {
		if (!allowedInst.isEmpty()) {
			jaxZahlungsauftrag.getZahlungen().removeIf(zahlung ->
				allowedInst.stream().noneMatch(institution ->
					institution.getId().equals(zahlung.getInstitutionsId())
				)
			);
		}
	}
}

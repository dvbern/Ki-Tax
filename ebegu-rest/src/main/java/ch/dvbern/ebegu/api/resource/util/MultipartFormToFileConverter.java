/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public final class MultipartFormToFileConverter {

	private static final String PART_FILE = "file";
	private static final Pattern MATCH_QUOTE = Pattern.compile("\"");

	private MultipartFormToFileConverter() {
		// util
	}

	@Nonnull
	public static List<TransferFile> parse(@Nonnull MultipartFormDataInput input) {
		List<InputPart> inputParts = input.getFormDataMap().get(PART_FILE);

		Validate.notNull(inputParts, "Must not be null");
		Validate.isTrue(!inputParts.isEmpty(),
				"form-parameter '" + PART_FILE + "' not found");

		return inputParts.stream()
				.map(MultipartFormToFileConverter::toBlobData)
				.collect(Collectors.toList());
	}

	@Nonnull
	private static TransferFile toBlobData(@Nonnull InputPart part) {
		try {
			String fileName = parseFileName(part).orElseThrow(() -> new IllegalArgumentException("filename must be given"));
			MediaType mediaType = part.getMediaType();
			InputStream inputStream = part.getBody(InputStream.class, null);
			byte[] bytes = IOUtils.toByteArray(inputStream);
			return new TransferFile(fileName, mediaType.toString(), bytes);
		} catch (IOException e) {
			throw new IllegalArgumentException("cannot parse file", e);
		}
	}

	/**
	 * Parst den Content-Disposition Header
	 *
	 * @param part aus einem {@link MultipartFormDataInput}. Bei keinem Filename oder einem leeren Filename wird
	 * dieser auf null reduziert.
	 */
	@Nonnull
	private static Optional<String> parseFileName(@Nonnull InputPart part) {
		MultivaluedMap<String, String> headers = part.getHeaders();

		String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
		String[] contentDispositionHeader = contentDisposition.split(";");

		return Arrays.stream(contentDispositionHeader)
				.filter(header -> header.toLowerCase(Locale.ENGLISH).trim().startsWith("filename"))
				.findAny()
				.map(header -> header.substring("filename=".length() + 1))
				.map(quotedFilename -> MATCH_QUOTE.matcher(quotedFilename.trim()).replaceAll(StringUtils.EMPTY))
				.map(encodedFilename -> {
					try {
						return URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8.name());
					} catch (UnsupportedEncodingException e) {
						throw new IllegalStateException("Could not decode filename to UTF-8", e);
					}
				})
				.map(StringUtils::trimToNull);
	}

}

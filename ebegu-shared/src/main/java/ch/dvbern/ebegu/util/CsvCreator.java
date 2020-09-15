/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Hilfsklasse zum Erstellen eines CSV-Files.
 * Anwendung:
 * - CsvCreator creator = new CsvCreator();
 * - creator.append(col1, col2, ...); // Repeat
 * - creator.create();
 */
public class CsvCreator {

	private static final Pattern PATTERN_BACKSLASH = Pattern.compile("\"", Pattern.LITERAL);
	private static final Pattern PATTERN_LINEBREAK = Pattern.compile("\\R");

	@Nonnull
	private final ByteArrayOutputStream outputStream;

	public CsvCreator() {
		outputStream = new ByteArrayOutputStream();
	}

	public void append(@Nonnull String... data) throws IOException {
		String line = convertToCSVLine(data);
		outputStream.write(line.getBytes(StandardCharsets.UTF_8));
		outputStream.write(Constants.CSV_NEW_LINE.getBytes(StandardCharsets.UTF_8));
	}

	@Nonnull
	public byte[] create() throws IOException {
		outputStream.close();
		return outputStream.toByteArray();
	}

	@Nonnull
	public String convertToCSVLine(@Nonnull String[] data) {
		return Stream.of(data)
			.map(this::escapeSpecialCharacters)
			.collect(Collectors.joining(Constants.CSV_DELIMITER));
	}

	@Nonnull
	private String escapeSpecialCharacters(@Nonnull String data) {
		String escapedData = PATTERN_LINEBREAK.matcher(data).replaceAll(" ");
		if (data.contains(Constants.CSV_DELIMITER) || data.contains("\"") || data.contains("'") || data.contains("|")) {
			data = PATTERN_BACKSLASH.matcher(data).replaceAll(Matcher.quoteReplacement("\"\""));
			escapedData = '"' + data + '"';
		}
		return escapedData;
	}
}

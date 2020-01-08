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
package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.util.Constants;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastenausgleichBerechnungCSVConverter {

	@Nonnull
	public String createLastenausgleichCSV(
		@Nonnull List<LastenausgleichBerechnungDataRow> data
	) {
		checkNotNull(data);

		StringBuilder csvString = new StringBuilder();

		String header = generateCSVHeader();
		csvString.append(appendNewLine(header));

		List<LastenausgleichBerechnungCSVDataRow> readyForCSV = mergeRevisionenIntoErhebungen(data);

		readyForCSV.stream()
			.map(this::dataRowToCSV)
			.map(this::appendNewLine)
			.forEach(csvString::append);

		return csvString.toString();
	}

	/**
	 * Beim Excel Export und in der Datenbank gibt es für Revisionen und aktuelle Werte separate Zeilen
	 * im Lastenausgleich. Beim CSV Export darf pro Gemeinde nur eine Zeile existieren. Diese Funktion
	 * verbindet Erhebungen mit den Revisionen.
	 */
	private List<LastenausgleichBerechnungCSVDataRow> mergeRevisionenIntoErhebungen(List<LastenausgleichBerechnungDataRow> data) {

		// Filtern nach Revisionen und anschliessend nach BFS Nummer gruppieren
		Map<String, List<LastenausgleichBerechnungDataRow>> revisionen = data
			.stream()
			.filter(LastenausgleichBerechnungDataRow::isKorrektur)
			.collect(Collectors.groupingBy(LastenausgleichBerechnungDataRow::getBfsNummer));

		List<LastenausgleichBerechnungCSVDataRow> result = new ArrayList<>();
		for (LastenausgleichBerechnungDataRow row : data) {
			// Werte von aktuellem Jahr (Nicht-Revisionen) in Resultate kopieren und Revisionen hinzufügen
			if (!row.isKorrektur()) {
				// Revisionen mit gleicher Gemeinde finden
				List<LastenausgleichBerechnungDataRow> revisionenCurrentGemeinde = revisionen.get(row.getBfsNummer());
				BigDecimal totalRevisionValue = BigDecimal.ZERO;
				// Revisionen dieser Gemeinde summieren, falls es welche gibt
				if (revisionenCurrentGemeinde != null) {
					for (LastenausgleichBerechnungDataRow revision : revisionenCurrentGemeinde) {
						totalRevisionValue = totalRevisionValue.add(revision.getEingabeLastenausgleich());
					}
				}
				LastenausgleichBerechnungCSVDataRow toAdd = new LastenausgleichBerechnungCSVDataRow(row);
				toAdd.setTotalRevision(totalRevisionValue);
				result.add(toAdd);
			}
		}
		return result;
	}

	private String generateCSVHeader() {
		return convertToCSVLine(new String[] {
			"bfs",
			"kibon_Belegung",
			"kibon_Gutscheine",
			"kibon_Anrechenbar",
			"kibon_Erhebung",
			"kibon_Revision",
			"kibon_Selbstbehalt"
		});
	}

	private String dataRowToCSV(LastenausgleichBerechnungCSVDataRow row) {
		return convertToCSVLine(new String[] {
			row.getBfsNummer(),
			row.getTotalBelegung().toString(),
			row.getTotalGutscheine().toString(),
			row.getTotalAnrechenbar().toString(),
			row.getEingabeLastenausgleich().toString(),
			row.getTotalRevision().toString(),
			row.getSelbstbehaltGemeinde().toString()
		});
	}

	private String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(Constants.CSV_DELIMITER) || data.contains("\"") || data.contains("'") || data.contains("|")) {
			data = data.replace("\"", "\"\"");
			escapedData = '"' + data + '"';
		}
		return escapedData;
	}

	private String convertToCSVLine(String[] data) {
		return Stream.of(data)
			.map(this::escapeSpecialCharacters)
			.collect(Collectors.joining(Constants.CSV_DELIMITER));
	}

	private String appendNewLine(String csvString) {
		return csvString + Constants.CSV_NEW_LINE;
	}
}

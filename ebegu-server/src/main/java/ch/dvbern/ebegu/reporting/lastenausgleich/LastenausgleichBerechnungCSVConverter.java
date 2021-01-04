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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.CsvCreator;
import ch.dvbern.ebegu.util.MathUtil;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastenausgleichBerechnungCSVConverter {

	private CsvCreator csvHelper = new CsvCreator();

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

		// Einträge nach BFS Nummer gruppieren
		Map<String, List<LastenausgleichBerechnungDataRow>> gemeindeGroups = data
			.stream()
			.collect(Collectors.groupingBy(LastenausgleichBerechnungDataRow::getBfsNummer));

		List<LastenausgleichBerechnungCSVDataRow> result = new ArrayList<>();
		for (Entry<String, List<LastenausgleichBerechnungDataRow>> gemeindeGroup : gemeindeGroups.entrySet()) {
			// pro Gemeinde sind mehrere Revisionen möglich, aber nur eine Erhebung
			LastenausgleichBerechnungDataRow currentErhebung = null;
			BigDecimal totalRevisionValue = BigDecimal.ZERO;
			for (LastenausgleichBerechnungDataRow entry : gemeindeGroup.getValue()) {
				if (entry.isKorrektur()) {
					totalRevisionValue = totalRevisionValue.add(entry.getEingabeLastenausgleich());
				} else {
					currentErhebung = entry;
				}
			}
			LastenausgleichBerechnungCSVDataRow toAdd;
			if (currentErhebung != null) {
				toAdd = new LastenausgleichBerechnungCSVDataRow(currentErhebung);
				toAdd.setTotalBelegung(MathUtil.DEFAULT.addNullSafe(toAdd.getTotalBelegungMitSelbstbehalt(), toAdd.getTotalBelegungOhneSelbstbehalt()));
				toAdd.setTotalGutscheine(MathUtil.DEFAULT.addNullSafe(toAdd.getTotalGutscheineMitSelbstbehalt(), toAdd.getTotalGutscheineOhneSelbstbehalt()));
			// falls keine Erhebung existiert, muss ein Eintrag speziell für die Revision gemacht werden
			} else {
				toAdd = new LastenausgleichBerechnungCSVDataRow();
				toAdd.setTotalBelegung(BigDecimal.ZERO);
				toAdd.setTotalGutscheine(BigDecimal.ZERO);
				toAdd.setTotalAnrechenbar(BigDecimal.ZERO);
				toAdd.setEingabeLastenausgleich(BigDecimal.ZERO);
				toAdd.setSelbstbehaltGemeinde(BigDecimal.ZERO);
				toAdd.setBfsNummer(gemeindeGroup.getValue().get(0).getBfsNummer());
			}
			toAdd.setTotalRevision(totalRevisionValue);
			result.add(toAdd);
		}
		// resultat wird nach BFS Nummer sortiert
		return result.stream()
			.sorted(Comparator.comparing(LastenausgleichBerechnungCSVDataRow::getBfsNummer))
			.collect(Collectors.toList());
	}

	private String generateCSVHeader() {
		return csvHelper.convertToCSVLine(new String[] {
			"BFS-Nr.",
			"kibon_Belegung",
			"kibon_Gutscheine",
			"kibon_Erhebung",
			"kibon_Selbstbehalt"
		});
	}

	private String dataRowToCSV(LastenausgleichBerechnungCSVDataRow row) {
		return csvHelper.convertToCSVLine(new String[] {
			row.getBfsNummer(),
			row.getTotalBelegung().toString(),
			row.getTotalGutscheine().toString(),
			MathUtil.DEFAULT.addNullSafe(row.getEingabeLastenausgleich(), row.getTotalGutscheineOhneSelbstbehalt()).add(row.getTotalRevision()).toString(),
			row.getSelbstbehaltGemeinde().toString()
		});
	}

	private String appendNewLine(String csvString) {
		return csvString + Constants.CSV_NEW_LINE;
	}
}

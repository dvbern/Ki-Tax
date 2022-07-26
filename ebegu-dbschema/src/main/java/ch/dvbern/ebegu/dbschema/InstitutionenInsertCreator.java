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

package ch.dvbern.ebegu.dbschema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.services.AdministrationService;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Liest die Liste der Institutionen (Excel) ein
 * Info:
 * Es gibt Institutionen mit mehreren Angeboten. Teilweise sollen sich diese nicht sehen können; In diesen Faellen machen
 * wir zwei Institutionen daraus. Im Excel muss dazu die Spalte Institutions-Id leer bleiben, bzw. dort wo für mehrere
 * Angebote die gleiche InstitutionsId drinn steht, werden die Angebote als InstitutiosStammdaten importiert.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "TooBroadScope", "PMD.AvoidDuplicateLiterals", "StringBufferReplaceableByString" })
public class InstitutionenInsertCreator {

	private static final Logger LOG = LoggerFactory.getLogger(InstitutionenInsertCreator.class);

	private final Map<String, String> traegerschaftenMap = new HashMap<>();
	private final Map<String, String> institutionenMap = new HashMap<>();

	private final List<String> insertTraegerschaften = new LinkedList<>();
	private final List<String> insertAdressen = new LinkedList<>();
	private final List<String> insertInstitutionen = new LinkedList<>();
	private final List<String> insertInstitutionsStammdaten = new LinkedList<>();

	private PrintWriter printWriter;
	private static final String INPUT_FILE = "/institutionen/institutionen-24.02.2017.xlsx";
	private static final int ANZAHL_ZEILEN = 87;
	private static final String OUTPUT_FILE = "insertInstitutionen.sql";

	public static void main(String[] args) {
		InstitutionenInsertCreator creator = new InstitutionenInsertCreator();
		try {
			creator.readExcel();
		} catch (IOException e) {
			LOG.error("Fehler beim Einlesen", e);
		}
	}

	private void readExcel() throws IOException {
		try (
			InputStream resourceAsStream = InstitutionenInsertCreator.class.getResourceAsStream(INPUT_FILE);
			XSSFWorkbook myWorkBook = new XSSFWorkbook(Objects.requireNonNull(resourceAsStream));
		) {
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIterator = mySheet.iterator();
			rowIterator.next(); // Titelzeile
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				readRow(row);
			}
			for (String s : insertTraegerschaften) {
				println(s);
			}
			for (String s : insertAdressen) {
				println(s);
			}
			for (String s : insertInstitutionen) {
				println(s);
			}
			for (String s : insertInstitutionsStammdaten) {
				println(s);
			}
			printWriter.flush();
		} finally {
			printWriter.close();
		}
	}

	@SuppressWarnings("OverlyComplexMethod")
	private void readRow(Row row) {
		if (row.getRowNum() > ANZAHL_ZEILEN) {
			return;
		}
		// Traegerschaften
		String traegerschaftKey = readString(row, AdministrationService.COL_TRAEGERSCHAFT_ID);
		String traegerschaftsId = null;
		if (StringUtils.isNotEmpty(traegerschaftKey)) {
			if (traegerschaftenMap.containsKey(traegerschaftKey)) {
				traegerschaftsId = traegerschaftenMap.get(traegerschaftKey);
			} else {
				traegerschaftsId = writeTraegerschaft(row);
				traegerschaftenMap.put(traegerschaftKey, traegerschaftsId);
			}

			if (traegerschaftsId == null) {
				LOG.error("TraegerschaftsId ist null, breche ab. {}", row.getRowNum());
				return;
			}
		}

		// Institutionen
		String institutionsKey = readString(row, AdministrationService.COL_INSTITUTION_ID);
		String institutionsId;
		if (StringUtils.isNotEmpty(institutionsKey) && institutionenMap.containsKey(institutionsKey)) {
			institutionsId = institutionenMap.get(institutionsKey);
		} else {
			institutionsId = writeInstitution(row, traegerschaftsId);
			institutionenMap.put(institutionsKey, institutionsId);
		}
		if (institutionsId == null) {
			LOG.error("institutionsId ist null, breche ab. {}", row.getRowNum());
			return;
		}
		// Adressen
		String adresseId = writeAdresse(row);
		if (adresseId == null) {
			LOG.error("adresseId ist null, breche ab. {}", row.getRowNum());
			return;
		}
		// Institutionsstammdaten
		String angebot = readString(row, AdministrationService.COL_ANGEBOT);
		if (angebot == null) {
			LOG.error("angebot is null: {}", row.getRowNum());
			return;
		}
		try {
			BetreuungsangebotTyp betreuungsangebotTyp = BetreuungsangebotTyp.valueOf(angebot);
			writeInstitutionStammdaten(row, institutionsId, adresseId, betreuungsangebotTyp);
		} catch (IllegalArgumentException iae) {
			if ("TAGESELTERN".equalsIgnoreCase(angebot)) {
				writeInstitutionStammdaten(row, institutionsId, adresseId, BetreuungsangebotTyp.TAGESFAMILIEN);
			} else {
				LOG.warn("Unbekannter Betreuungsangebot-Typ: {}", angebot);
			}
		}
	}

	private String readString(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);
		if (cell != null) {
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue();
		}
		return null;
	}

	private String writeAdresse(Row row) {
		String id = UUID.randomUUID().toString();
		String strasse = readString(row, AdministrationService.COL_STRASSE);
		String hausnummer = readString(row, AdministrationService.COL_HAUSNUMMER);
		String plz = readString(row, AdministrationService.COL_PLZ);
		String ort = readString(row, AdministrationService.COL_ORT);
		String zusatzzeile = readString(row, AdministrationService.COL_ZUSATZZEILE);

		if (strasse == null) {
			LOG.error("strasse is null: {}", row.getRowNum());
			return null;
		}
		if (plz == null) {
			LOG.error("plz is null: {}", row.getRowNum());
			return null;
		}
		if (ort == null) {
			LOG.error("ort is null: {}", row.getRowNum());
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO adresse ");
		sb.append("(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde, gueltig_ab, gueltig_bis, hausnummer, land, ort, plz, strasse, zusatzzeile, event_published) ");
		sb.append("VALUES (");
		sb.append('\'').append(id).append("', ");    // id
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_erstellt
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_mutiert
		sb.append("'flyway', ");                    // user_erstellt
		sb.append("'flyway', ");                    // user_mutiert
		sb.append("0, ");                            // version,
		sb.append("null, ");                        // gemeinde,
		sb.append("'1000-01-01', ");                // gueltig_ab,
		sb.append("'9999-12-31', ");                // gueltig_bis,
		sb.append(toStringOrNull(hausnummer)).append(", "); // hausnummer
		sb.append("'CH', ");                        // land,
		sb.append(toStringOrNull(ort)).append(", "); // ort
		sb.append(toStringOrNull(plz)).append(", "); // plz
		sb.append(toStringOrNull(strasse)).append(", "); // strasse
		sb.append(toStringOrNull(zusatzzeile)).append(", ");    // zusatzzeile
		sb.append("0");    // event_published
		sb.append(");");
		insertAdressen.add(sb.toString());

		return id;
	}

	private String writeTraegerschaft(Row row) {
		String id = UUID.randomUUID().toString();
		String traegerschaftsname = readString(row, AdministrationService.COL_TRAEGERSCHAFT_NAME);
		String traegerschaftEmail = readString(row, AdministrationService.COL_TRAEGERSCHAFT_MAIL);

		if (traegerschaftsname == null) {
			LOG.error("institutionsname is null: {}", row.getRowNum());
			return null;
		}
		if (traegerschaftEmail == null) {
			LOG.error("traegerschaftEmail is null: {}", row.getRowNum());
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO traegerschaft ");
		sb.append("(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mail) ");
		sb.append("VALUES (");
		sb.append('\'').append(id).append("', ");    // id
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_erstellt
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_mutiert
		sb.append("'flyway', ");                    // user_erstellt
		sb.append("'flyway', ");                    // user_mutiert
		sb.append("0, ");                            // version,
		sb.append(toStringOrNull(traegerschaftsname)).append(", "); // name
		sb.append("true, ");                                // active
		sb.append(toStringOrNull(traegerschaftEmail));  // mail
		sb.append(");");
		insertTraegerschaften.add(sb.toString());

		return id;
	}

	private String writeInstitution(Row row, String traegerschaftId) {
		String id = UUID.randomUUID().toString();
		String institutionsname = readString(row, AdministrationService.COL_INSTITUTION_NAME);
		String institutionsEmail = readString(row, AdministrationService.COL_INSTITUTION_MAIL);

		if (institutionsname == null) {
			LOG.error("institutionsname is null: {}", row.getRowNum());
			return null;
		}
		if (institutionsEmail == null) {
			LOG.error("institutionsEmail is null: {}", row.getRowNum());
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO institution ");
		sb.append("(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, mandant_id, traegerschaft_id, active, mail) ");
		sb.append("VALUES (");
		sb.append('\'').append(id).append("', ");    // id
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_erstellt
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_mutiert
		sb.append("'flyway', ");                    // user_erstellt
		sb.append("'flyway', ");                    // user_mutiert
		sb.append("0, ");                            // version,
		sb.append(toStringOrNull(institutionsname)).append(", "); // name
		sb.append('\'').append(AdministrationService.MANDANT_ID_BERN).append("', ");    // mandant_id,
		sb.append(toStringOrNull(traegerschaftId)).append(", "); // name
		sb.append("true, "); // active
		sb.append(toStringOrNull(institutionsEmail)); // mail
		sb.append(");");
		insertInstitutionen.add(sb.toString());

		return id;
	}

	private String writeInstitutionStammdaten(Row row, String institutionsId, String adresseId, BetreuungsangebotTyp typ) {
		if (institutionsId == null) {
			LOG.error("institutionsId is null: {}", row.getRowNum());
			return null;
		}
		if (adresseId == null) {
			LOG.error("adresseId is null: {}", row.getRowNum());
			return null;
		}

		// INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gueltig_ab, gueltig_bis, betreuungsangebot_typ, iban, institution_id, adresse_id) VALUES ('11111111-1111-1111-1111-111111111101', '2016-07-26 00:00:00', '2016-07-26 00:00:00', 'flyway', 'flyway', 0, '1000-01-01', '9999-12-31', 'KITA', null, 11.50, 240.00, '11111111-1111-1111-1111-111111111101', '11111111-1111-1111-1111-111111111101');
		String id = UUID.randomUUID().toString();
		String iban = readString(row, AdministrationService.COL_IBAN);

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO institution_stammdaten ");
		sb.append("(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gueltig_ab, gueltig_bis, betreuungsangebot_typ, iban, institution_id, adresse_id) ");
		sb.append("VALUES (");
		sb.append('\'').append(id).append("', ");    // id
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_erstellt
		sb.append("'2016-01-01 00:00:00', ");        // timestamp_mutiert
		sb.append("'flyway', ");                    // user_erstellt
		sb.append("'flyway', ");                    // user_mutiert
		sb.append("0, ");                    // version,
		sb.append("'1000-01-01', ");                // gueltig_ab,
		sb.append("'9999-12-31', ");                // gueltig_bis,
		sb.append('\'').append(typ.name()).append("', "); // betreuungsangebot_typ,
		sb.append(toStringOrNull(iban)).append(", "); // iban
		sb.append(toStringOrNull(institutionsId)).append(", "); // institution_id
		sb.append(toStringOrNull(adresseId)); // adresse_id
		sb.append(");");
		insertInstitutionsStammdaten.add(sb.toString());

		return id;
	}

	private String toStringOrNull(String aStringOrNull) {
		if (aStringOrNull == null) {
			return "null";
		}
		return '\'' + aStringOrNull + '\'';
	}

	// this class is not really a part of the software itself. For this reason it is not important that some code is duplicated
	@SuppressWarnings({"Duplicates", "PMD.CloseResource"})
	private PrintWriter getPrintWriter() {
		if (printWriter == null) {
			try {
				File output = new File(OUTPUT_FILE);
				FileOutputStream fos = new FileOutputStream(output.getAbsolutePath());
				printWriter = new PrintWriter(fos);
				LOG.info("File generiert: {}", output.getAbsolutePath());
			} catch (FileNotFoundException e) {
				LOG.error("Konnte Outputfile nicht erstellen", e);
			}
		}
		return printWriter;
	}

	private void println(String s) {
		getPrintWriter().println(s);
	}
}

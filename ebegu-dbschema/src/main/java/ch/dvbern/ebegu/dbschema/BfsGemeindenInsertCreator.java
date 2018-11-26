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

package ch.dvbern.ebegu.dbschema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.util.Constants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Liest die Liste der Gemeinden gemaess BFS ein.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "TooBroadScope", "PMD.AvoidDuplicateLiterals", "StringBufferReplaceableByString" })
public class BfsGemeindenInsertCreator {

	private static final Logger LOG = LoggerFactory.getLogger(BfsGemeindenInsertCreator.class);

	private PrintWriter printWriter;
	private static final String INPUT_FILE = "/gemeinden/Gemeindestand.xlsx";
	private static final String OUTPUT_FILE = "insertBfsGemeinden.sql";

	public static void main(String[] args) {
		BfsGemeindenInsertCreator creator = new BfsGemeindenInsertCreator();
		try {
			creator.readExcel();
		} catch (IOException e) {
			LOG.error("Fehler beim Einlesen", e);
		}
	}

	private void readExcel() throws IOException {
		InputStream resourceAsStream = BfsGemeindenInsertCreator.class.getResourceAsStream(INPUT_FILE);
		XSSFWorkbook myWorkBook = new XSSFWorkbook(resourceAsStream);
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		Iterator<Row> rowIterator = mySheet.iterator();
		rowIterator.next(); // Titelzeile
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			readRow(row);
		}
		printWriter.flush();
		printWriter.close();
	}

	@SuppressWarnings("OverlyComplexMethod")
	private void readRow(Row row) {
		String id = UUID.randomUUID().toString();
		String mandant = Constants.DEFAULT_MANDANT_ID;
		Long histNummer = readLong(row, 0);
		String kanton = readString(row, 1);
		Long bezirkNummer = readLong(row, 2);
		String bezirk = readString(row, 3);
		Long bfsNummer = readLong(row, 4);
		String gemeinde = readString(row, 5);
		LocalDate gueltigAb = readDate(row, 6);
		String gueltigAbAsString = gueltigAb != null ? Constants.SQL_DATE_FORMAT.format(gueltigAb) : "";

		if ("BE".equals(kanton)) {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO bfs_gemeinde ");
			sb.append("(id, mandant_id, hist_nummer, kanton, bezirk_nummer, bezirk, bfs_nummer, name, gueltig_ab) ");
			sb.append("VALUES (");
			sb.append('\'').append(id).append("', ");    		// id
			sb.append('\'').append(mandant).append("', ");      // mandant
			sb.append(histNummer).append(", ");       	 		// hist_nummer
			sb.append('\'').append(kanton).append("', ");       // kanton
			sb.append(bezirkNummer).append(", ");               // bezirk_nummer
			sb.append('\'').append(bezirk).append("', ");       // bezirk,
			sb.append(bfsNummer).append(", ");                  // bfs_nummer,
			sb.append('\'').append(gemeinde).append("', ");     // gemeinde,
			sb.append('\'').append(gueltigAbAsString).append('\''); // gueltig_ab,
			sb.append(");");
			println(sb.toString());
		}
	}

	@Nullable
	private String readString(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);
		if (cell != null) {
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue();
		}
		return null;
	}

	@Nullable
	private Long readLong(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);
		if (cell != null) {
			cell.setCellType(CellType.STRING);
			return Long.valueOf(cell.getStringCellValue());
		}
		return null;
	}

	@Nullable
	private LocalDate readDate(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);
		if (cell != null) {
			return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
		return null;
	}

	// this class is not really a part of the software itself. For this reason it is not important that some code is duplicated
	@SuppressWarnings("Duplicates")
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
		LOG.info(s);
		getPrintWriter().println(s);
	}
}

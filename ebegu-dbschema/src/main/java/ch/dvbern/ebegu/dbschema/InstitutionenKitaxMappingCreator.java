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

package ch.dvbern.ebegu.dbschema;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Liest die Liste der Institutionen (Excel) ein
 * Info:
 * Es gibt Institutionen mit mehreren Angeboten. Teilweise sollen sich diese nicht sehen können; In diesen Faellen machen
 * wir zwei Institutionen daraus. Im Excel muss dazu die Spalte Institutions-Id leer bleiben, bzw. dort wo für mehrere
 * Angebote die gleiche InstitutionsId drinn steht, werden die Angebote als InstitutiosStammdaten importiert.
 */
@SuppressWarnings({ "IOResourceOpenedButNotSafelyClosed", "TooBroadScope", "PMD.AvoidDuplicateLiterals" })
@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
public class InstitutionenKitaxMappingCreator {

	private static final Logger LOG = LoggerFactory.getLogger(InstitutionenKitaxMappingCreator.class);

	private PrintWriter printWriter;

	/**
	 * Das Inputfile soll im Format CSV sein, und folgende Spalten beinhalten:
	 * 240;11.5;aaregg; kita aaregg;
	 * (oeffnungstage,oeffnungsstunden,nameKitax,nameKibon)
	 */
	private static final String INPUT_FILE = "/institutionen/mapping.csv";
	private static final String OUTPUT_FILE = "resultMapping.sql";

	public static void main(String[] args) {
		InstitutionenKitaxMappingCreator creator = new InstitutionenKitaxMappingCreator();
		creator.readKitax();
	}

	private void readKitax() {
		BufferedReader reader = null;
		try (InputStream resourceAsStream = InstitutionenKitaxMappingCreator.class.getResourceAsStream(INPUT_FILE)) {
			String str = "";
			reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
			// Die erste Zeile ist der Header
			reader.readLine();
			while ((str = reader.readLine()) != null) {
				if (StringUtils.isEmpty(str)) {
					return;
				}
				String[] kitaArray = StringUtils.split(str, ";");
				KitaxInstitution kitax = new KitaxInstitution(kitaArray);
				println(kitax.toSqlInsert());
			}
		} catch (IOException e) {
			LOG.error("Ki-Tax Daten koennen nicht gelesen werden", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignore) {
					// nop
				}
			}
			printWriter.flush();
			printWriter.close();
		}
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
		getPrintWriter().println(s);
	}


	static class KitaxInstitution {

		private final String nameKitax;
		private final String nameKibon;
		private BigDecimal oeffnungstage = null;
		private BigDecimal oeffnungsstunden = null;

		public KitaxInstitution(String[] kitaArray) {
			if (kitaArray.length < 4) {
				LOG.info("Zuwenige Werte! {}", Arrays.toString(kitaArray));
			}
			int i = 0;
			String sOeffnungstage = kitaArray[i++].trim().replaceAll(" ", "");
			try {

				this.oeffnungstage = new BigDecimal(sOeffnungstage);
			} catch (Exception e) {
				LOG.info("{} konnte nicht in Number formatiert werden, {}", sOeffnungstage, Arrays.toString(kitaArray), e);
			}
			String sOeffnungsstunden = kitaArray[i++].trim().replaceAll(" ", "");
			try {
				this.oeffnungsstunden = new BigDecimal(sOeffnungsstunden);
			} catch (Exception e) {
				LOG.info("{} konnte nicht in Number formatiert werden, {}", sOeffnungsstunden, Arrays.toString(kitaArray), e);
			}
			this.nameKitax = normalize(kitaArray[i++]);
			this.nameKibon = normalize(kitaArray[i++]);
		}

		private String normalize(String s) {
			return s.toLowerCase(Locale.GERMAN).trim();
		}

		public String toSqlInsert() {
			String sql = "INSERT INTO kitax_uebergangsloesung_institution_oeffnungszeiten VALUES ("
				+ "UNHEX(REPLACE(UUID(), '-','')), '2020-06-01', '2020-06-01', 'flyway', 'flyway', 0, "
				+ "' " + nameKibon + "', '" + nameKitax + "', " + oeffnungsstunden + ", " + oeffnungstage + ");";
			return sql;
		}
	}
}

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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrateSchemaTest {

	private static final Logger LOG = LoggerFactory.getLogger(MigrateSchemaTest.class);

	@Test
	public void flywayMigrationNumbersCorrect() {
		Set<String> usedNumbers = new HashSet<>();
		URL scriptFolder = MigrateSchemaTest.class.getResource("/db/migration");
		if (scriptFolder != null) {
			File folder = new File(scriptFolder.getFile());
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						String revNbr = getRevisionNumberFromFilename(file.getName());
						if (usedNumbers.contains(revNbr)) {
							Assertions.fail("Fehler in Flyway-Skripts: Die Nummer " + revNbr +
								" ist mehrmals vergeben!");
						}
						usedNumbers.add(revNbr);
					}
				}
			}
		}
		LOG.info("All FlyWay files checked!");
	}

	private String getRevisionNumberFromFilename(String filename) {
		// Der Text zwischen V und dem _ ist die Nummer
		return StringUtils.substringBefore(StringUtils.substring(filename, 1), "_");
	}
}

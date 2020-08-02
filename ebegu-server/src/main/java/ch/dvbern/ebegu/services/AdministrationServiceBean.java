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

package ch.dvbern.ebegu.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer diverse Admin-Aufgaben.
 * Im Moment nur fuer internen Gebrauch, d.h. die Methoden werden nirgends im Code aufgerufen, koennen aber bei Bedarf
 * schnell irgendwo angehaengt werden.
 */
@Stateless
@Local(AdministrationService.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "ConstantConditions" })
public class AdministrationServiceBean extends AbstractBaseService implements AdministrationService {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	private static final Logger LOG = LoggerFactory.getLogger(AdministrationServiceBean.class);

	private PrintWriter printWriter;
	private static final String INPUT_FILE = "/institutionen/Institutionen_2017.03.01.xlsx";
	private static final String OUTPUT_FILE = "insertInstitutionen.sql";
	private static final int ANZAHL_ZEILEN = 87;

	private final List<String> traegerschaftenMap = new LinkedList<>();
	private final List<String> institutionenMap = new LinkedList<>();

	private final List<String> listTraegerschaften = new LinkedList<>();
	private final List<String> listAdressen = new LinkedList<>();
	private final List<String> listInstitutionen = new LinkedList<>();
	private final List<String> listInstitutionsStammdaten = new LinkedList<>();

	@Override
	public void createSQLSkriptInstutionsstammdaten() {
		try {
			InputStream resourceAsStream = AdministrationServiceBean.class.getResourceAsStream(INPUT_FILE);
			XSSFWorkbook myWorkBook = new XSSFWorkbook(resourceAsStream);
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIterator = mySheet.iterator();
			rowIterator.next(); // Titelzeile
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				readRow(row);
			}
			for (String s : listTraegerschaften) {
				println(s);
			}
			for (String s : listAdressen) {
				println(s);
			}
			for (String s : listInstitutionen) {
				println(s);
			}
			for (String s : listInstitutionsStammdaten) {
				println(s);
			}
			printWriter.flush();
			printWriter.close();
		} catch (IOException ioe) {
			LOG.error("Error beim Importieren", ioe);
		}
	}

	@SuppressWarnings("OverlyComplexMethod")
	private void readRow(Row row) {
		if (row.getRowNum() > ANZAHL_ZEILEN) {
			return;
		}
		// Traegerschaften
		String traegerschaftId = readString(row, AdministrationService.COL_TRAEGERSCHAFT_ID);
		if (StringUtils.isNotEmpty(traegerschaftId) && !traegerschaftenMap.contains(traegerschaftId)) {
			writeTraegerschaft(row, traegerschaftId);
			traegerschaftenMap.add(traegerschaftId);
		}
		// Institutionen
		String institutionsId = readString(row, AdministrationService.COL_INSTITUTION_ID);
		if (StringUtils.isEmpty(institutionsId) || !institutionenMap.contains(institutionsId)) {
			institutionsId = writeInstitution(row, institutionsId, traegerschaftId);
			institutionenMap.add(institutionsId);
		}
		// Stammdaten und Adressen
		String stammdatenId = readString(row, AdministrationService.COL_STAMMDATEN_ID);
		writeInstitutionStammdaten(row, stammdatenId, institutionsId);
	}

	private String writeTraegerschaft(Row row, String traegerschaftId) {
		String traegerschaftsname = readString(row, AdministrationService.COL_TRAEGERSCHAFT_NAME);
		String traegerschaftEmail = readString(row, AdministrationService.COL_TRAEGERSCHAFT_MAIL);
		if (StringUtils.isNotEmpty(traegerschaftsname) && StringUtils.isNotEmpty(traegerschaftEmail)) {
			// Es gibt eine Traegerschaft
			if (StringUtils.isNotEmpty(traegerschaftId)) {
				Optional<Traegerschaft> traegerschaftOptional = traegerschaftService.findTraegerschaft(traegerschaftId);
				if (traegerschaftOptional.isPresent()) {
					// Traegerschaft ist schon bekannt -> updaten
					listTraegerschaften.add(updateTraegerschaft(traegerschaftId, traegerschaftsname, traegerschaftEmail));

				} else {
					throw new IllegalStateException("Traegerschaft nicht gefunden!");
				}
			} else {
				// dies ist unmoeglich <- wenn ein traegerschaft ohne ID kommt wird einfach nichts gemacht
				// Traegerschaft ist neu
				traegerschaftId = UUID.randomUUID().toString();
				listTraegerschaften.add(insertTraegerschaft(traegerschaftId, traegerschaftsname, traegerschaftEmail));
			}
		} else {
			throw new IllegalStateException("Traegerschaftdaten fehlen");
		}
		return traegerschaftId;
	}

	private String insertTraegerschaft(String id, String traegerschaftsname, String traegerschaftEmail) {
		String sb = "INSERT INTO traegerschaft "
			+ "(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mail) "
			+ "VALUES ("
			+ '\'' + id + "', "    // id
			+ "'2016-01-01 00:00:00', "        // timestamp_erstellt
			+ "'2016-01-01 00:00:00', "        // timestamp_mutiert
			+ "'flyway', "                    // user_erstellt
			+ "'flyway', "                    // user_mutiert
			+ "0, "                            // version,
			+ toStringOrNull(traegerschaftsname) + ", " // name
			+ "true, "                                // active
			+ toStringOrNull(traegerschaftEmail)  // mail
			+ ");";
		return sb;
	}

	private String updateTraegerschaft(String id, String traegerschaftsname, String traegerschaftEmail) {
		String sb = "UPDATE traegerschaft set"
			+ " name = "
			+ toStringOrNull(traegerschaftsname) // name
			+ ", mail = "
			+ toStringOrNull(traegerschaftEmail)  // mail
			+ " where id = '"
			+ id    // id
			+ "';";
		return sb;
	}

	private String writeInstitution(Row row, String institutionId, String traegerschaftId) {
		String institutionsname = readString(row, AdministrationService.COL_INSTITUTION_NAME);
		String institutionsEmail = readString(row, AdministrationService.COL_INSTITUTION_MAIL);
		if (StringUtils.isEmpty(institutionsname) || StringUtils.isEmpty(institutionsEmail)) {
			throw new IllegalStateException("Institutionsangaben fehlen");
		}
		// Es gibt eine Institution
		if (StringUtils.isNotEmpty(institutionId)) {
			Optional<Institution> institutionOptional = institutionService.findInstitution(institutionId, true);
			if (institutionOptional.isPresent()) {
				// Institution ist schon bekannt -> updaten
				listInstitutionen.add(updateInstitution(institutionId, traegerschaftId, institutionsname, institutionsEmail));
			} else {
				throw new IllegalStateException("Institution nicht gefunden!");
			}
		} else {
			// Institution ist neu
			institutionId = UUID.randomUUID().toString();
			listInstitutionen.add(insertInstitution(institutionId, traegerschaftId, institutionsname, institutionsEmail));
		}
		return institutionId;
	}

	private String insertInstitution(String id, String traegerschaftId, String institutionsname, String institutionsEmail) {
		String sb = "INSERT INTO institution "
			+ "(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, mandant_id, traegerschaft_id, active, event_published, mail) "
			+ "VALUES ("
			+ '\'' + id + "', "    // id
			+ "'2016-01-01 00:00:00', "        // timestamp_erstellt
			+ "'2016-01-01 00:00:00', "        // timestamp_mutiert
			+ "'flyway', "                    // user_erstellt
			+ "'flyway', "                    // user_mutiert
			+ "0, "                            // version,
			+ toStringOrNull(institutionsname) + ", " // name
			+ '\'' + MANDANT_ID_BERN + "', "    // mandant_id,
			+ toStringOrNull(traegerschaftId) + ", " // name
			+ "true, " // active
			+ "false, " // event_published
			+ toStringOrNull(institutionsEmail) // mail
			+ ");";
		return sb;
	}

	private String updateInstitution(String id, String traegerschaftId, String institutionsname, String institutionsEmail) {
		String sb = "UPDATE institution set"
			+ " name = "
			+ toStringOrNull(institutionsname) // name
			+ ", mail = "
			+ toStringOrNull(institutionsEmail)  // mail
			+ ", traegerschaft_id = "
			+ toStringOrNull(traegerschaftId)  // traegerschaft_id
			+ " where id = '"
			+ id    // id
			+ "';";
		return sb;
	}

	private String writeInstitutionStammdaten(Row row, String stammdatenId, String institutionsId) {
		String angebot = readString(row, AdministrationService.COL_ANGEBOT);
		if (StringUtils.isEmpty(angebot)) {
			throw new IllegalStateException("Angebotstyp fehlen");
		}
		BetreuungsangebotTyp typ = BetreuungsangebotTyp.valueOf(angebot);
		String iban = readString(row, AdministrationService.COL_IBAN);

		String strasse = readString(row, AdministrationService.COL_STRASSE);
		String hausnummer = readString(row, AdministrationService.COL_HAUSNUMMER);
		String plz = readString(row, AdministrationService.COL_PLZ);
		String ort = readString(row, AdministrationService.COL_ORT);
		String zusatzzeile = readString(row, AdministrationService.COL_ZUSATZZEILE);
		if (StringUtils.isEmpty(strasse) || StringUtils.isEmpty(plz) || StringUtils.isEmpty(ort)) {
			throw new IllegalStateException("Adressangaben fehlen");
		}

		if (StringUtils.isEmpty(institutionsId)) {
			throw new IllegalStateException("institutionsId is null: " + row.getRowNum());
		}

		if (StringUtils.isNotEmpty(stammdatenId)) {
			Optional<InstitutionStammdaten> stammdatenOptional = institutionStammdatenService.findInstitutionStammdaten(stammdatenId);
			if (stammdatenOptional.isPresent()) {
				// Institution ist schon bekannt -> updaten
				String adresseId = stammdatenOptional.get().getAdresse().getId();
				listAdressen.add(updateAdresse(adresseId, hausnummer, ort, plz, strasse, zusatzzeile));
				listInstitutionsStammdaten.add(updateInstitutionsStammdaten(institutionsId, typ, iban));
			} else {
				throw new IllegalStateException("InstitutionStammdaten nicht gefunden!");
			}
		} else {
			// Institution ist neu
			String adresseId = UUID.randomUUID().toString();
			listAdressen.add(insertAdresse(adresseId, hausnummer, ort, plz, strasse, zusatzzeile));
			stammdatenId = UUID.randomUUID().toString();
			listInstitutionsStammdaten.add(insertInstitutionsStammdaten(stammdatenId, institutionsId, adresseId, typ, iban));
		}
		return stammdatenId;
	}

	private String insertInstitutionsStammdaten(String id, String institutionsId, String adresseId, BetreuungsangebotTyp typ, String iban) {
		String sb = "INSERT INTO institution_stammdaten "
			+ "(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gueltig_ab, gueltig_bis, betreuungsangebot_typ, iban, "
			+ "institution_id, adresse_id) "
			+ "VALUES ("
			+ '\'' + id + "', "    // id
			+ "'2016-01-01 00:00:00', "        // timestamp_erstellt
			+ "'2016-01-01 00:00:00', "        // timestamp_mutiert
			+ "'flyway', "                    // user_erstellt
			+ "'flyway', "                    // user_mutiert
			+ "0, "                    // version,
			+ "'1000-01-01', "                // gueltig_ab,
			+ "'9999-12-31', "                // gueltig_bis,
			+ '\'' + typ.name() + "', " // betreuungsangebot_typ,
			+ toStringOrNull(iban) + ", " // iban
			+ toStringOrNull(institutionsId) + ", " // institution_id
			+ toStringOrNull(adresseId) // adresse_id
			+ ");";
		return sb;
	}

	private String updateInstitutionsStammdaten(String id, BetreuungsangebotTyp typ, String iban) {
		String sb = "UPDATE institution_stammdaten set"
			+ " betreuungsangebot_typ = "
			+ '\'' + typ.name() + '\'' // typ
			+ ", iban = "
			+ toStringOrNull(iban)  // ort
			+ " where id = '"
			+ id    // id
			+ "';";
		return sb;
	}

	private String insertAdresse(String id, String hausnummer, String ort, String plz, String strasse, String zusatzzeile) {
		String sb = "INSERT INTO adresse "
			+ "(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde, gueltig_ab, gueltig_bis, hausnummer, land, ort, "
			+ "plz, strasse, zusatzzeile) "
			+ "VALUES ("
			+ '\'' + id + "', "    // id
			+ "'2016-01-01 00:00:00', "        // timestamp_erstellt
			+ "'2016-01-01 00:00:00', "        // timestamp_mutiert
			+ "'flyway', "                    // user_erstellt
			+ "'flyway', "                    // user_mutiert
			+ "0, "                            // version,
			+ "null, "                        // gemeinde,
			+ "'1000-01-01', "                // gueltig_ab,
			+ "'9999-12-31', "                // gueltig_bis,
			+ toStringOrNull(hausnummer) + ", " // hausnummer
			+ "'CH', "                        // land,
			+ toStringOrNull(ort) + ", " // ort
			+ toStringOrNull(plz) + ", " // plz
			+ toStringOrNull(strasse) + ", " // strasse
			+ toStringOrNull(zusatzzeile)    // zusatzzeile
			+ ");";
		return sb;
	}

	private String updateAdresse(String id, String hausnummer, String ort, String plz, String strasse, String zusatzzeile) {
		String sb = "UPDATE adresse set"
			+ " hausnummer = "
			+ toStringOrNull(hausnummer) // hausnummer
			+ ", ort = "
			+ toStringOrNull(ort)  // ort
			+ ", plz = "
			+ toStringOrNull(plz)  // plz
			+ ", strasse = "
			+ toStringOrNull(strasse)  // strasse
			+ ", zusatzzeile = "
			+ toStringOrNull(zusatzzeile)  // zusatzzeile
			+ " where id = '"
			+ id    // id
			+ "';";
		return sb;
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

	private String toStringOrNull(String aStringOrNull) {
		if (aStringOrNull == null) {
			return "null";
		}
		return '\'' + aStringOrNull + '\'';
	}

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

	@Override
	public void exportInstitutionsstammdaten() {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY.MM.dd");

		try {
			File fos = new File("Institutionen_" + formatter.format(LocalDate.now()) + ".csv");
			PrintWriter pw = new PrintWriter(fos);
			LOG.info("Writing File to: {}", fos.getAbsolutePath());

			pw.println("TrägerschaftId,Trägerschaft,InstitutionId,Name,Strasse,Hausnummer,Plz,Ort,Zusatzzeile,E-Mail,StammdatenId,Angebot,IBAN");

			Collection<InstitutionStammdaten> stammdatenList = criteriaQueryHelper.getAll(InstitutionStammdaten.class);
			for (InstitutionStammdaten stammdaten : stammdatenList) {
				Institution institution = stammdaten.getInstitution();
				Traegerschaft traegerschaft = institution.getTraegerschaft();
				Adresse adresse = stammdaten.getAdresse();

				if (stammdaten.isActive()) {

					StringBuilder sb = new StringBuilder();

					if (traegerschaft != null) {
						append(sb, traegerschaft.getId());
						append(sb, traegerschaft.getName());
					} else {
						append(sb, "");
						append(sb, "");
					}

					append(sb, institution.getId());
					append(sb, institution.getName());
					append(sb, adresse.getStrasse());
					append(sb, adresse.getHausnummer());
					append(sb, adresse.getPlz());
					append(sb, adresse.getOrt());
					append(sb, adresse.getZusatzzeile());
					append(sb, stammdaten.getMail());

					append(sb, stammdaten.getId());
					append(sb, stammdaten.getBetreuungsangebotTyp().name());
					String iban = stammdaten.extractIban() != null ? stammdaten.extractIban().getIban() : "";
					append(sb, iban);

					pw.println(sb);
				}
			}
			pw.flush();
			pw.close();

		} catch (IOException e) {
			LOG.debug(e.getMessage());
		}
	}

	private void append(@Nonnull StringBuilder sb, @Nullable String s) {
		if (StringUtils.isNotEmpty(s)) {
			sb.append(s);
		}
		sb.append(',');
	}
}

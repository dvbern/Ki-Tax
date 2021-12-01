/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

INSERT IGNORE INTO mandant
VALUES (UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')), '2021-11-30 00:00:00', '2021-11-30 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Luzern', false, false);

# APPLICATION PROPERTIES
INSERT IGNORE INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										 version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
    NULL, name, value, UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''))
FROM application_property
WHERE NOT EXISTS(SELECT name
                 FROM application_property a_p
                 WHERE mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')) AND
                         a_p.name = application_property.name);

UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = '#0072bd' WHERE name = 'PRIMARY_COLOR' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = '#00466f' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = '#C6C6C6' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = 'logo-kibon-luzern.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE application_property SET value = 'logo-kibon-white-luzern.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));

# BFS Gemeinde
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab)
VALUES ('9d2e02eb-5292-11ec-adf5-f4390979fa3e', UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-','')), 'LU', 1061, 'Luzern', '2013-01-01');

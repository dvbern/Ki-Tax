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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('7e0e0765-16b3-11ec-a125-f4390979fa3e', '-','')), '2021-09-16 12:00:00', '2021-09-16 12:00:00', 'flyway', 'flyway', 0, null, 'PRIMARY_COLOR', '#D50025');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('b6419cb7-16b3-11ec-a125-f4390979fa3e', '-','')), '2021-09-16 12:00:00', '2021-09-16 12:00:00', 'flyway', 'flyway', 0, null, 'PRIMARY_COLOR_DARK', '#BF0425');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('be7d2239-16b3-11ec-a125-f4390979fa3e', '-','')), '2021-09-16 12:00:00', '2021-09-16 12:00:00', 'flyway', 'flyway', 0, null, 'PRIMARY_COLOR_LIGHT', '#F0C3CB');

INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('6019fed2-16f4-11ec-a125-f4390979fa3e', '-','')), '2021-09-16 12:00:00', '2021-09-16 12:00:00', 'flyway', 'flyway', 0, null, 'LOGO_FILE_NAME', 'logo-kibon-bern.svg');

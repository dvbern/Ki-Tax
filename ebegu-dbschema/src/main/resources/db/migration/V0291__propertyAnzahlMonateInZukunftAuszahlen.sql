/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

# bern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('5e8f942d-c8d4-4632-8233-189b06872fc0', '-','')), UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')),'2022-07-21 12:00:00', '2022-07-21 12:00:00', 'flyway', 'flyway', 0, null, 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT', '0');

# solothurn
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('f182b557-9e63-4a42-9bdd-4d32dae67b72', '-','')), UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', '')),'2022-07-21 12:00:00', '2022-07-21 12:00:00', 'flyway', 'flyway', 0, null, 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT', '0');

# luzern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE('832920e0-657a-47f5-bb34-7f6f3ba91dd2', '-','')), UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')),'2022-07-21 12:00:00', '2022-07-21 12:00:00', 'flyway', 'flyway', 0, null, 'ANZAHL_MONATE_AUSZAHLEN_IN_ZUKUNFT', '1');

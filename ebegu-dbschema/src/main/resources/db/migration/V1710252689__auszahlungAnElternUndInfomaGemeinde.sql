/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
SET @mandant_id_bern = UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', ''));
SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));
SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));

# bern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @mandant_id_bern, now(), now(), 'flyway', 'flyway', 0, null, 'AUSZAHLUNGEN_AN_ELTERN', 'false');

# appenzell
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @mandant_id_ar, now(), now(), 'flyway', 'flyway', 0, null, 'AUSZAHLUNGEN_AN_ELTERN', 'false');

# solothurn
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @mandant_id_solothurn, now(), now(), 'flyway', 'flyway', 0, null, 'AUSZAHLUNGEN_AN_ELTERN', 'false');

# luzern
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @mandant_id_luzern, now(), now(), 'flyway', 'flyway', 0, null, 'AUSZAHLUNGEN_AN_ELTERN', 'true');

# schwyz
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), @mandant_id_schwyz, now(), now(), 'flyway', 'flyway', 0, null, 'AUSZAHLUNGEN_AN_ELTERN', 'false');

ALTER TABLE gemeinde_stammdaten ADD COLUMN infoma_zahlungen BIT NOT NULL DEFAULT FALSE;
ALTER TABLE gemeinde_stammdaten_aud ADD COLUMN infoma_zahlungen BIT;

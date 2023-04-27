/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

INSERT IGNORE INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
										 user_mutiert, version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), id, NOW(), NOW(), 'flyway', 'flyway', 0, NULL,
	'ANGEBOT_TS_ENABLED', angebotts
FROM mandant;

ALTER TABLE mandant DROP COLUMN angebotts;
ALTER TABLE mandant_aud DROP COLUMN angebotts;

INSERT IGNORE INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
										 user_mutiert, version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), id, NOW(), NOW(), 'flyway', 'flyway', 0, NULL,
	'ANGEBOT_FI_ENABLED', angebotfi
FROM mandant;

ALTER TABLE mandant DROP COLUMN angebotfi;
ALTER TABLE mandant_aud DROP COLUMN angebotfi;

INSERT IGNORE INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
										 user_mutiert, version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), id, NOW(), NOW(), 'flyway', 'flyway', 0, NULL,
	'ANGEBOT_TFO_ENABLED', 'true'
FROM mandant;

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

SET @mandant_schwyz_id = (SELECT id FROM mandant WHERE mandant_identifier = 'SCHWYZ');

INSERT IGNORE INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
										 user_mutiert, version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), id, NOW(), NOW(), 'flyway', 'flyway', 0, NULL, 'ABWEICHUNGEN_ENABLED', 'true'
FROM mandant;

UPDATE application_property SET value = 'false' WHERE name = 'ABWEICHUNGEN_ENABLED' AND mandant_id = @mandant_id_schwyz;

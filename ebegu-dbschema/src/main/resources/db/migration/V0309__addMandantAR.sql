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

SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));

INSERT IGNORE INTO mandant
VALUES (@mandant_id_ar, NOW(), NOW(), 'flyway', 'flyway', 0, NULL, 'Kanton Appenzell Ausserrhoden', false, false, 'APPENZELL_AUSSERRHODEN', false, 1, 1);

# APPLICATION PROPERTIES
INSERT IGNORE INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										 version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
	NULL, name, value, @mandant_id_ar
FROM application_property
WHERE mandant_id = @mandant_id_solothurn AND
	NOT EXISTS(SELECT name
			   FROM application_property a_p
			   WHERE mandant_id = @mandant_id_ar AND
					   a_p.name = application_property.name);

UPDATE application_property SET value = 'logo-kibon-ar.svg' WHERE name = 'LOGO_FILE_NAME' and mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'logo-kibon--white-ar.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' and mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'FRENCH_ENABLED' and mandant_id = @mandant_id_ar;

# BFS Gemeinden
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab)
VALUES
	(UUID(), @mandant_id_ar, 'AR', 3001, 'Herisau', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3002, 'Hundwil', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3003, 'Schönengrund', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3004, 'Schwellbrunn', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3005, 'Stein (AR)', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3006, 'Urnäsch', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3007, 'Waldstatt', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3021, 'Bühler', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3022, 'Gais', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3023, 'Speicher', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3024, 'Teufen (AR)', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3025, 'Trogen', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3031, 'Grub (AR)', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3032, 'Heiden', '1960-09-12'),
	(UUID(), @mandant_id_ar, 'AR', 3033, 'Lutzenberg', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3034, 'Rehetobel', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3035, 'Reute (AR)', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3036, 'Wald (AR)', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3037, 'Walzenhausen', '1960-01-01'),
	(UUID(), @mandant_id_ar, 'AR', 3038, 'Wolfhalden', '1960-01-01');
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
VALUES (@mandant_id_ar, NOW(), NOW(), 'flyway', 'flyway', 0, NULL, 'Appenzell Ausserrhoden', false, false, 'APPENZELL_AUSSERRHODEN', false, 1, 1);

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
UPDATE application_property SET value = 'logo-kibon-white-ar.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' and mandant_id = @mandant_id_ar;
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

INSERT IGNORE INTO gesuchsperiode
VALUES (UNHEX(REPLACE('9bb4a798-3998-11ed-a63d-b05cda43de9c', '-', '')), NOW(), NOW(), 'system', 'system', 0, 0, '2023-08-01', '2024-07-31', NULL,
		'ENTWURF', NULL, NULL, NULL,
		NULL, NULL, NULL, @mandant_id_ar, NULL,
		NULL);

# Einstellungen für Gesuchsperiode kopieren
INSERT IGNORE INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, einstellung_key, value, NULL,
	(SELECT gesuchsperiode.id
	 FROM gesuchsperiode
			  INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	 WHERE mandant_identifier = 'APPENZELL_AUSSERRHODEN'), NULL
FROM einstellung
		 INNER JOIN gesuchsperiode g ON einstellung.gesuchsperiode_id = g.id
		 INNER JOIN mandant m2 ON g.mandant_id = m2.id
WHERE m2.mandant_identifier = 'SOLOTHURN' AND gueltig_ab = '2022-08-01' AND gemeinde_id IS NULL AND einstellung.mandant_id is NULL;

# Gemeinde Einstellungen für Gesuchsperiode kopieren
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	einstellung_key, value, NULL, UNHEX(REPLACE('9bb4a798-3998-11ed-a63d-b05cda43de9c', '-', '')), @mandant_id_ar
FROM einstellung
WHERE mandant_id = @mandant_id_solothurn AND gesuchsperiode_id = UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', '')) AND NOT EXISTS(
		SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', ''))
				and e1.mandant_id = @mandant_id_ar AND e1.einstellung_key = einstellung.einstellung_key
	) AND gemeinde_id IS NULL;

INSERT IGNORE INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
	UNHEX(REPLACE('ed455784-3a51-11ed-a0ae-00090ffe0001', '-', '')), # id
	'2018-01-01 00:00:00', # timestamp_erstellt
	'2018-01-01 00:00:00', # timestamp_mutiert
	'flyway', # user_erstellt
	'flyway', # user_mutiert
	0, # version
	'FALL_NUMMER', # sequence_type
	100, # current_value
	@mandant_id_ar);
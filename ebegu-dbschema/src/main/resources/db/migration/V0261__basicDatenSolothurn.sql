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
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));

UPDATE application_property
SET value = 'logo-kibon-solothurn.svg'
WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property
SET value = 'logo-kibon-white-solothurn.svg'
WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_solothurn;

INSERT IGNORE INTO gesuchsperiode
VALUES (UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', '')), NOW(), NOW(), 'system', 'system', 0, 0, '2022-08-01', '2023-07-31', NULL,
		'ENTWURF', NULL, NULL, NULL,
		NULL, NULL, NULL, @mandant_id_solothurn, NULL,
		NULL);

INSERT IGNORE INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, einstellung_key, value, NULL,
	(SELECT gesuchsperiode.id
	 FROM gesuchsperiode
		  INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	 WHERE mandant_identifier = 'SOLOTHURN'), NULL
FROM einstellung
	 INNER JOIN gesuchsperiode g ON einstellung.gesuchsperiode_id = g.id
	 INNER JOIN mandant m2 ON g.mandant_id = m2.id
WHERE m2.mandant_identifier = 'BERN' AND gueltig_ab = '2021-08-01' AND gemeinde_id IS NULL AND
	gesuchsperiode_id IS NOT NULL AND einstellung.mandant_id is NULL;

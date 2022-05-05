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

INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, status,
							datum_aktiviert, mandant_id)
VALUES (UNHEX(REPLACE('1670d04a-30a9-11ec-a86f-b89a2ae4a038', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2022-08-01', '2023-07-31', 'ENTWURF', NULL, UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', '')));

INSERT IGNORE INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, einstellung_key, value, NULL,
	(SELECT gesuchsperiode.id
	 FROM gesuchsperiode
		  INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
	 WHERE mandant_identifier = 'LUZERN'), NULL
FROM einstellung
	 INNER JOIN gesuchsperiode g ON einstellung.gesuchsperiode_id = g.id
	 INNER JOIN mandant m2 ON g.mandant_id = m2.id
WHERE m2.mandant_identifier = 'BERN' AND gueltig_ab = '2019-08-01' AND gemeinde_id IS NULL AND
	gesuchsperiode_id IS NOT NULL AND einstellung.mandant_id is NULL;

UPDATE einstellung
INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = 'FREIWILLIGER_KINDERGARTEN', einstellung.user_mutiert='flyway',
    einstellung.timestamp_mutiert=now(), einstellung.version=einstellung.version+1
WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND m.mandant_identifier = 'LUZERN';


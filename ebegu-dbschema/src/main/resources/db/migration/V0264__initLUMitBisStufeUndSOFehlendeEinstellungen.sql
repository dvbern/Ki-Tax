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

SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
SET @gesuchsperiode_id_luzern = UNHEX(REPLACE('1670d04a-30a9-11ec-a86f-b89a2ae4a038', '-', ''));
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
SET @mandant_id_bern = UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', ''));
SET @gesuchsperiode_bern_id = UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', ''));

INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, status,
							datum_aktiviert, mandant_id)
VALUES (@gesuchsperiode_id_luzern, '2018-01-01 00:00:00', '2018-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2022-08-01', '2023-07-31', 'ENTWURF', NULL, @mandant_id_luzern);

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

INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	einstellung_key, value, NULL, @gesuchsperiode_id_luzern, @mandant_id_luzern
FROM einstellung
WHERE mandant_id = @mandant_id_bern AND gesuchsperiode_id = @gesuchsperiode_bern_id AND NOT EXISTS(
		SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id = @gesuchsperiode_id_luzern
				and e1.mandant_id = @mandant_id_luzern AND e1.einstellung_key = einstellung.einstellung_key
	) AND gemeinde_id IS NULL;

INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	einstellung_key, value, NULL, UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', '')), @mandant_id_solothurn
FROM einstellung
WHERE mandant_id = @mandant_id_bern AND gesuchsperiode_id = @gesuchsperiode_bern_id AND NOT EXISTS(
		SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', ''))
				and e1.mandant_id = @mandant_id_solothurn AND e1.einstellung_key = einstellung.einstellung_key
	) AND gemeinde_id IS NULL;

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

SET @gesuchsperiode_id = UNHEX(REPLACE('1b0ed338-b3d2-11ee-829a-0242ac160002', '-', ''));

UPDATE einstellung set value = 'UNABHAENGING' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_id and gemeinde_id is null;
UPDATE einstellung set value = 'PRIMARSTUFE' where gesuchsperiode_id = @gesuchsperiode_id and einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gemeinde_id is null;

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'ANGEBOT_SCHULSTUFE' AS einstellungkey,
			'KITA' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = 'KITA,TAGESFAMILIEN,MITTAGSTISCH'
WHERE einstellung_key = 'ANGEBOT_SCHULSTUFE' AND mandant_identifier = 'SCHWYZ';

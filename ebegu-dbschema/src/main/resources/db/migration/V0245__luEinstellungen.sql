/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

# Insert for all mandants
INSERT IGNORE INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), id, NOW(), NOW(), 'flyway', 'flyway', 0, NULL, 'FRENCH_ENABLED', 'true' FROM mandant;

# set false for LU
UPDATE application_property INNER JOIN mandant ON application_property.mandant_id = mandant.id
SET value = 'false'
WHERE application_property.name = 'FRENCH_ENABLED' AND mandant_identifier = 'LUZERN';

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'DIPLOMATENSTATUS_DEAKTIVIERT' AS einstellungkey,
			'false' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung INNER JOIN mandant m ON einstellung.mandant_id = m.id
SET value = 'true'
WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND mandant_identifier = 'LUZERN';

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'ZEMIS_DISABLED' AS einstellungkey,
			'false' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung INNER JOIN mandant m ON einstellung.mandant_id = m.id
SET value = 'true'
WHERE einstellung_key = 'ZEMIS_DISABLED' AND mandant_identifier = 'LUZERN';

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'SPRACHE_AMTSPRACHE_DISABLED' AS einstellungkey,
			'false' AS value,
			id AS gesuchsperiode_id
		FROM gesuchsperiode
	);

UPDATE einstellung INNER JOIN mandant m ON einstellung.mandant_id = m.id
SET value = 'true'
WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND mandant_identifier = 'LUZERN';
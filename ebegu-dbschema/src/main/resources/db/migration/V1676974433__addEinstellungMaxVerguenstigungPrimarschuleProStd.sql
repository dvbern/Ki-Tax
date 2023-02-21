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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id, gemeinde_id, mandant_id, erklaerung)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_mutiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AS einstellungkey,
			value,
			gesuchsperiode_id,
			gemeinde_id,
			mandant_id,
			erklaerung
		FROM einstellung
		WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD'
	);

UPDATE einstellung
	INNER JOIN gemeinde ON einstellung.gemeinde_id = gemeinde.id
SET einstellung.value = 7
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND gemeinde.name = 'Grenchen';

UPDATE einstellung
SET einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD';

UPDATE einstellung
SET einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_TG';
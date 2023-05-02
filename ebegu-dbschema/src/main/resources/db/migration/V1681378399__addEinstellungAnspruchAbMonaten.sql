
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
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id, erklaerung)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			'ANSPRUCH_AB_X_MONATEN' AS einstellungkey,
			0 AS value,
			id AS gesuchsperiode_id,
			'Ab welchem Alter in Monaten kann ein Kind Anspruch haben (davor ist der Anspruch 0)' AS erklaerung
		FROM gesuchsperiode
	);

UPDATE einstellung
INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
SET value = 3
WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND mandant_identifier = 'APPENZELL_AUSSERRHODEN'
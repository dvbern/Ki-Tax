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

# einstellung in allen GPs hinzufügen, per default aktiviert
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id, erklaerung)
	(SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
		 NOW() AS timestamp_erstellt,
		 NOW() AS timestamp_muiert,
		 'flyway' AS user_erstellt,
		 'flyway' AS user_mutiert,
		 '0' AS version,
		 'ERWEITERTE_BEDUERFNISSE_AKTIV' AS einstellungkey,
		 'true' AS value,
		 id AS gesuchsperiode_id,
		 'Aktiviert die anzeige des Besondere Bedürfnisse Frage.' AS erklaerung
	 FROM gesuchsperiode);


SET @mandant_id_schwyz = (SELECT id
						  FROM mandant
						  WHERE mandant_identifier = 'SCHWYZ');

# einstellung für schwyz deaktivieren
UPDATE einstellung INNER JOIN gesuchsperiode ON gesuchsperiode.id = einstellung.gesuchsperiode_id
SET value='false'
WHERE gesuchsperiode.mandant_id = @mandant_id_schwyz AND einstellung_key = 'ERWEITERTE_BEDUERFNISSE_AKTIV';
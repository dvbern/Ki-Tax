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
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id, erklaerung)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			   NOW() AS timestamp_erstellt,
			   NOW() AS timestamp_muiert,
			   'ebegu' AS user_erstellt,
			   'ebegu' AS user_mutiert,
			   '0' AS version,
			   'ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT' AS einstellungkey,
			   'false' AS value,
			   id AS gesuchsperiode_id,
			   'Aktiviert das Feld "Tage pro Monat" bei Tagesfamilien-Betreuungen' AS erklaerung
		FROM gesuchsperiode
	);

UPDATE einstellung
	INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
SET value = 'true'
WHERE einstellung_key = 'ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT' AND mandant_identifier = 'SCHWYZ'

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
                         einstellung_key, value, gesuchsperiode_id, erklaerung)
    (
        SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
               NOW() AS timestamp_erstellt,
               NOW() AS timestamp_muiert,
               'ebegu' AS user_erstellt,
               'ebegu' AS user_mutiert,
               '0' AS version,
               'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' AS einstellungkey,
               'false' AS value,
			   id AS gesuchsperiode_id,
		      'Auf der FinSit werden zur Berechnung des massgebenden Einkommen drei zusätzliche Felder für das Ersatzeinkommen und eine Frage angezeigt.' AS erklaerung
        FROM gesuchsperiode
    );

UPDATE einstellung set value = 'true'
where einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and
      gesuchsperiode_id = (select gesuchsperiode.id from gesuchsperiode
                   join mandant on gesuchsperiode.mandant_id = mandant.id
    where gesuchsperiode.gueltig_ab = '2024-08-01' and mandant_identifier = 'BERN');
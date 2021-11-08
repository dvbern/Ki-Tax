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

ALTER TABLE fachstelle ADD COLUMN IF NOT EXISTS gueltig_ab DATE NOT NULL DEFAULT '1000-01-01';
ALTER TABLE fachstelle_aud ADD COLUMN IF NOT EXISTS gueltig_ab DATE;

ALTER TABLE fachstelle ADD COLUMN IF NOT EXISTS gueltig_bis DATE NOT NULL DEFAULT '9999-12-31';
ALTER TABLE fachstelle_aud ADD COLUMN IF NOT EXISTS gueltig_bis DATE;

INSERT INTO fachstelle (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
						name, fachstelle_anspruch, fachstelle_erweiterte_betreuung, gueltig_ab, gueltig_bis)
VALUES (UNHEX(REPLACE('46d37d8e-4083-11ec-a836-b89a2ae4a038', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, 'BESONDERE_BEDUERFNISSE_KRANKHEIT', FALSE, TRUE, '2021-08-01', '9999-12-31');

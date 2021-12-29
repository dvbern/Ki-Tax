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

ALTER TABLE mandant
ADD COLUMN IF NOT EXISTS mandant_identifier VARCHAR(255) NOT NULL DEFAULT 'BERN';

ALTER TABLE mandant_aud
ADD COLUMN IF NOT EXISTS mandant_identifier VARCHAR(255);

UPDATE mandant SET mandant_identifier = 'LUZERN' WHERE UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
UPDATE mandant SET mandant_identifier = 'SOLOTHURN' WHERE id = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
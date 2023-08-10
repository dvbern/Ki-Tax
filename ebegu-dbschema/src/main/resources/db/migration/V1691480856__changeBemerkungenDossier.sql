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

ALTER TABLE dossier ADD COLUMN IF NOT EXISTS bemerkungen varchar(4000);
ALTER TABLE dossier_aud ADD COLUMN IF NOT EXISTS bemerkungen varchar(4000);

UPDATE dossier
JOIN fall ON dossier.fall_id = fall.id
SET bemerkungen = bemerkungen_dossier
WHERE dossier.fall_id = fall.id;

ALTER TABLE fall
DROP COLUMN bemerkungen_dossier;

ALTER TABLE fall_aud
DROP COLUMN bemerkungen_dossier;
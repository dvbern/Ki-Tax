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

ALTER TABLE kind ADD COLUMN IF NOT EXISTS hoehere_beitraege_wegen_beeintraechtigung_beantragen BIT NOT NULL DEFAULT FALSE;
ALTER TABLE kind_aud ADD COLUMN IF NOT EXISTS hoehere_beitraege_wegen_beeintraechtigung_beantragen BIT;

ALTER TABLE kind ADD COLUMN IF NOT EXISTS hoehere_beitraege_unterlagen_digital BIT DEFAULT NULL;
ALTER TABLE kind_aud ADD COLUMN IF NOT EXISTS hoehere_beitraege_unterlagen_digital BIT;

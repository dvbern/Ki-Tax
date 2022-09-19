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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

ALTER TABLE mandant ADD next_infoma_belegnummer_institutionen BIGINT NOT NULL DEFAULT 1;
ALTER TABLE mandant_aud ADD next_infoma_belegnummer_institutionen BIGINT NULL;

ALTER TABLE mandant CHANGE COLUMN next_infoma_belegnummer next_infoma_belegnummer_antragsteller BIGINT NOT NULL DEFAULT 1;
ALTER TABLE mandant_aud CHANGE COLUMN next_infoma_belegnummer next_infoma_belegnummer_antragsteller  BIGINT NULL;

UPDATE mandant SET next_infoma_belegnummer_institutionen = 800001 WHERE mandant_identifier = 'LUZERN';



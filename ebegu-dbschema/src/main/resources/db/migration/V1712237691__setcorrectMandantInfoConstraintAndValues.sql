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


UPDATE mandant SET next_infoma_belegnummer_antragsteller = 1 WHERE next_infoma_belegnummer_antragsteller = 0;
UPDATE mandant SET next_infoma_belegnummer_institutionen = 1 WHERE next_infoma_belegnummer_institutionen = 0;

ALTER TABLE mandant
	ADD CONSTRAINT check_next_infoma_belegnummer_antragsteller_min_value CHECK ( next_infoma_belegnummer_antragsteller >= 1 );
ALTER TABLE mandant
	ADD CONSTRAINT check_next_infoma_belegnummer_institutionen_min_value CHECK ( next_infoma_belegnummer_institutionen >= 1 );

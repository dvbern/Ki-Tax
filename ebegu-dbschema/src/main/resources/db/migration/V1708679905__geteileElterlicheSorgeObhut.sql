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

alter table kind add column geteilte_elterliche_sorge_obhut BIT;
alter table kind_aud add column geteilte_elterliche_sorge_obhut BIT;

UPDATE kind set geteilte_elterliche_sorge_obhut = false where obhut_alternierend_ausueben = true
      and familien_ergaenzende_betreuung= false;

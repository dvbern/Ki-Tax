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

alter table lastenausgleich_tagesschule_angaben_institution
    add column if not exists anzahl_eingeschriebene_kinder_basisstufe decimal(19,2) default 0;

alter table lastenausgleich_tagesschule_angaben_institution_aud
    add column if not exists anzahl_eingeschriebene_kinder_basisstufe decimal(19,2);

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde ADD COLUMN IF NOT EXISTS
    einnahmen_elterngebuehren_volksschulangebot decimal(19,2);

ALTER TABLE lastenausgleich_tagesschule_angaben_gemeinde_aud ADD COLUMN IF NOT EXISTS
    einnahmen_elterngebuehren_volksschulangebot decimal(19,2);
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

alter table familiensituation add column  gesuchsteller_kardinalitaet VARCHAR(20);
alter table familiensituation_aud add column  gesuchsteller_kardinalitaet VARCHAR(20);

alter table familiensituation add column fkjv_fam_sit BIT NOT NULL default false;
alter table familiensituation add column min_dauer_konkubinat INTEGER  NOT NULL default 5;

alter table familiensituation_aud add column fkjv_fam_sit BIT;
alter table familiensituation_aud add column min_dauer_konkubinat INTEGER;

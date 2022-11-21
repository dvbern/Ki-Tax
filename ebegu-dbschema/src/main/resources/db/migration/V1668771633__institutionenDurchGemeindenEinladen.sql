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

ALTER TABLE institution_stammdaten_betreuungsgutscheine drop column gemeinde_id;
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud drop column gemeinde_id;


create table gemeinde_stammdaten_institution (
    gemeinde_stammdaten_id binary(16) not null,
    institution_id binary(16) not null
);

create table gemeinde_stammdaten_institution_aud (
    rev integer not null,
    gemeinde_stammdaten_id binary(16) not null,
    institution_id binary(16) not null,
    revtype tinyint,
    primary key (rev, gemeinde_stammdaten_id, institution_id)
);

alter table gemeinde_stammdaten_institution_aud
add constraint FK_gemeinde_stammdaten_institution_aud_rev
    foreign key (rev)
        references revinfo (rev);

alter table gemeinde_stammdaten_institution
add constraint FK_gemeinde_stammdaten_institutionen_institution_id
    foreign key (institution_id)
        references institution (id);

alter table gemeinde_stammdaten_institution
add constraint FK_gemeinde_stammdaten_institutionen_gemeinde_stammdaten_id
    foreign key (gemeinde_stammdaten_id)
        references gemeinde_stammdaten (id);

alter table gemeinde_stammdaten ADD COLUMN alle_bg_institutionen_zugelassen BIT NOT NULL DEFAULT 1;
alter table gemeinde_stammdaten_aud ADD COLUMN alle_bg_institutionen_zugelassen BIT;

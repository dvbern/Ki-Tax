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

create table lastenausgleich_detail_zeitabschnitt (
    id binary(16) not null,
    timestamp_erstellt datetime not null,
    timestamp_mutiert datetime not null,
    user_erstellt varchar(255) not null,
    user_mutiert varchar(255) not null,
    version bigint not null,
    lastenausgleich_detail_id binary(16) not null,
    zeitabschnitt_id binary(16),
    primary key (id)
);

create table lastenausgleich_detail_zeitabschnitt_aud (
    id binary(16) not null,
    rev integer not null,
    revtype tinyint,
    timestamp_erstellt datetime,
    timestamp_mutiert datetime,
    user_erstellt varchar(255),
    user_mutiert varchar(255),
    lastenausgleich_detail_id binary(16),
    zeitabschnitt_id binary(16),
    primary key (id, rev)
);

alter table lastenausgleich_detail_zeitabschnitt_aud
add constraint FK_lastenausgleich_detail_zeitabschnitt_rev
    foreign key (rev)
        references revinfo (rev);

alter table lastenausgleich_detail_zeitabschnitt
add constraint FK_lastenausgleich_dz_lastenausgleich_d_id
    foreign key (lastenausgleich_detail_id)
        references lastenausgleich_detail (id);

alter table lastenausgleich_detail_zeitabschnitt
add constraint FK_lastenausgleich_dz_zeitabschnitt_id
    foreign key (zeitabschnitt_id)
        references verfuegung_zeitabschnitt (id);

/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

create table gemeinde_kennzahlen (
    id binary(16) not null,
    timestamp_erstellt datetime not null,
    timestamp_mutiert datetime not null,
    user_erstellt varchar(255) not null,
    user_mutiert varchar(255) not null,
    version bigint not null,
    status varchar(255),
    gemeinde_id binary(16) not null,
    gesuchsperiode_id binary(16) not null,
    nachfrage_erfuellt bit,
    nachfrage_anzahl bigint,
    nachfrage_dauer DECIMAL (19,2),
    kostenlenkung_andere bit,
    welche_kostenlenkungsmassnahmen VARCHAR(255),
    primary key (id)
);

create table gemeinde_kennzahlen_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
    status varchar(255),
    gemeinde_id binary(16),
    gesuchsperiode_id binary(16),
    nachfrage_erfuellt bit,
    nachfrage_anzahl bigint,
    nachfrage_dauer DECIMAL (19,2),
    kostenlenkung_andere bit,
    welche_kostenlenkungsmassnahmen VARCHAR(255),
	primary key (id, rev)
);

alter table gemeinde_kennzahlen_aud
	add constraint FK_gemeinde_kennzahlen_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table gemeinde_kennzahlen
	add constraint FK_gemeinde_kennzahlen_gemeinde_id
		foreign key (gemeinde_id)
			references gemeinde (id);

alter table gemeinde_kennzahlen
	add constraint FK_gemeinde_kennzahlen_gesuchsperiode_id
		foreign key (gesuchsperiode_id)
			references gesuchsperiode (id);

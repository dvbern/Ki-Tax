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

create table interne_pendenz_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	erledigt bit,
	termin date,
	text varchar(255),
	gesuch_id binary(16),
	primary key (id, rev)
);

create table interne_pendenz (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	erledigt bit not null,
	termin date not null,
	text varchar(255) not null,
	gesuch_id binary(16) not null,
	primary key (id)
);

alter table interne_pendenz_aud
	add constraint FK_interne_pendenz_aud_rev
		foreign key (rev)
			references revinfo (rev);

alter table interne_pendenz
	add constraint FK_interne_pendenz_gesuch_id
		foreign key (gesuch_id)
			references gesuch (id);

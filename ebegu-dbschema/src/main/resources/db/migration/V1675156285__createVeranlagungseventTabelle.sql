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


CREATE TABLE veranlagung_event_log (
    id BINARY(16) NOT NULL,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	zpv_nummer bigint NOT NULL,
	geburtsdatum date NOT NULL,
	gesuch_id BINARY(16) NULL,
	gesuchsperiode_beginn_jahr INTEGER NOT NULL,
	result VARCHAR(255),
	primary key (id)
);

CREATE TABLE veranlagung_event_log_aud (
    id BINARY(16) NOT NULL,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	zpv_nummer bigint NOT NULL,
	geburtsdatum date NOT NULL,
	gesuch_id BINARY(16) NULL,
	gesuchsperiode_beginn_jahr INTEGER NOT NULL,
	result VARCHAR(255),
	primary key (id, rev)
);

alter table veranlagung_event_log_aud
	add constraint FK_veranlagungsevent_log_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table veranlagung_event_log
	add constraint FK_veranlagungsevent_log_antrag_id
		foreign key (gesuch_id)
			references gesuch (id)
				ON DELETE CASCADE ;




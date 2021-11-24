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

CREATE TABLE modul_tagesschule_external_client (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	external_client_id	   BINARY(16)	NOT NULL,
	modul_tagesschule_group_id BINARY(16) NOT NULL,
	identifier		   VARCHAR(255) NOT NULL,
	PRIMARY KEY(id),
	CONSTRAINT FK_modul_tagesschule_external_client_external_client_id FOREIGN KEY(external_client_id) REFERENCES external_client(id),
	CONSTRAINT FK_modul_tagesschule_external_client_modul_tagesschule_group_id FOREIGN KEY(modul_tagesschule_group_id) REFERENCES modul_tagesschule_group(id)
);

CREATE TABLE modul_tagesschule_external_client_aud (
	id                 BINARY(16)   NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	external_client_id	   BINARY(16),
	modul_tagesschule_group_id BINARY(16),
	identifier		   VARCHAR(255),
	PRIMARY KEY(id, rev),
	CONSTRAINT FK_modul_tagesschule_external_client_rev FOREIGN KEY(rev) REFERENCES revinfo(rev)
);



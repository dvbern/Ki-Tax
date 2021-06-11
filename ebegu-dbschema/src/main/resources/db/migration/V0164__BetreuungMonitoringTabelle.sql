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

CREATE TABLE betreuung_monitoring (
	id                 	BINARY(16)   NOT NULL,
	timestamp_erstellt 	datetime not null,
	timestamp_mutiert 	datetime not null,
	user_erstellt 		varchar(255) not null,
	user_mutiert 		varchar(255) not null,
	version 			bigint not null,
	ref_nummer			VARCHAR(255) NOT NULL,
	benutzer      		VARCHAR(255) NOT NULL,
	info_text      		VARCHAR(255) NOT NULL,
	timestamp       	DATETIME NOT NULL,
	PRIMARY KEY (id)
);
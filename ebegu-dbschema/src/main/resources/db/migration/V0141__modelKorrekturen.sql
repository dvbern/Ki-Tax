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

ALTER TABLE sozialdienst_fall ADD vorname VARCHAR(255) not null;

DROP TABLE sozialdienst_fall_aud;

CREATE TABLE sozialdienst_fall_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	vorgaenger_id varchar(36),
	name VARCHAR(255),
	status VARCHAR(255),
	geburtsdatum  DATE,
	vollmacht longblob,
	adresse_id BINARY(16),
	sozialdienst_id BINARY(16),
	vorname VARCHAR(255),
	primary key (id, rev)
);

ALTER TABLE sozialdienst_fall_aud
	ADD CONSTRAINT FK_sozialdienst_fall_aud_revinfo
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);


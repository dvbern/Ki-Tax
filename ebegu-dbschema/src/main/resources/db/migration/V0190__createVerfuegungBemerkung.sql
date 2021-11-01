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

create table verfuegung_zeitabschnitt_bemerkung(
	id                              BINARY(16) NOT NULL,
	timestamp_erstellt 				DATETIME,
	timestamp_mutiert  				DATETIME,
	user_erstellt      				VARCHAR(255),
	user_mutiert       				VARCHAR(255),
	version            				BIGINT NOT NULL,
	vorgaenger_id                   VARCHAR(36),
	bemerkung  						VARCHAR(4000) NOT NULL,
	gueltig_ab                      DATE NOT NULL,
	gueltig_bis                     DATE NOT NULL,
	verfuegung_zeitabschnitt_id 	BINARY(16)  NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE verfuegung_zeitabschnitt_bemerkung_aud (
	id                               BINARY(16) NOT NULL,
	rev                              INTEGER     NOT NULL,
	revtype                          TINYINT,
	timestamp_erstellt               DATETIME,
	timestamp_mutiert                DATETIME,
	user_erstellt                    VARCHAR(255),
	user_mutiert                     VARCHAR(255),
	vorgaenger_id                    VARCHAR(36),
	bemerkung  						 VARCHAR(4000),
	gueltig_ab                       DATE,
	gueltig_bis                      DATE,
	verfuegung_zeitabschnitt_id 	 BINARY(16)  NOT NULL,
	PRIMARY KEY (id, rev)
);

ALTER TABLE verfuegung_zeitabschnitt_bemerkung
ADD CONSTRAINT FK_verfuegung_zeitabschnitt_bemerkung_zeitabschnitt_id
	FOREIGN KEY (verfuegung_zeitabschnitt_id)
		REFERENCES verfuegung_zeitabschnitt (id);

ALTER TABLE verfuegung_zeitabschnitt_bemerkung_aud
ADD CONSTRAINT FK_verfuegung_zeitabschnitt_bemerkung_aud_revinfo
	FOREIGN KEY (rev)
		REFERENCES revinfo(rev);
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

CREATE TABLE IF NOT EXISTS gemeinde_stammdaten_korrespondenz (
	id                            BINARY(16)   NOT NULL,
	timestamp_erstellt            DATETIME     NOT NULL,
	timestamp_mutiert             DATETIME     NOT NULL,
	user_erstellt                 VARCHAR(255) NOT NULL,
	user_mutiert                  VARCHAR(255) NOT NULL,
	version                       BIGINT       NOT NULL,
	logo_content                  LONGBLOB,
	logo_name                     VARCHAR(255),
	logo_spacing_left             INTEGER		NOT NULL,
	logo_spacing_top           	  INTEGER		NOT NULL,
	logo_type                     VARCHAR(255),
	logo_width                    INTEGER,
	receiver_address_spacing_left INTEGER		NOT NULL,
	receiver_address_spacing_top  INTEGER		NOT NULL,
	sender_address_spacing_left   INTEGER		NOT NULL,
	sender_address_spacing_top    INTEGER		NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gemeinde_stammdaten_korrespondenz_aud (
	id                            BINARY(16) NOT NULL,
	rev                           INTEGER    NOT NULL,
	revtype                       TINYINT,
	timestamp_erstellt            DATETIME,
	timestamp_mutiert             DATETIME,
	user_erstellt                 VARCHAR(255),
	user_mutiert                  VARCHAR(255),
	logo_content                  LONGBLOB,
	logo_name                     VARCHAR(255),
	logo_spacing_left             INTEGER,
	logo_spacing_top              INTEGER,
	logo_type                     VARCHAR(255),
	logo_width                    INTEGER,
	receiver_address_spacing_left INTEGER,
	receiver_address_spacing_top  INTEGER,
	sender_address_spacing_left   INTEGER,
	sender_address_spacing_top    INTEGER,
	PRIMARY KEY (id, rev)
);

ALTER TABLE gemeinde_stammdaten
ADD IF NOT EXISTS gemeinde_stammdaten_korrespondenz_id BINARY(16);

ALTER TABLE gemeinde_stammdaten_aud
ADD IF NOT EXISTS gemeinde_stammdaten_korrespondenz_id BINARY(16);

ALTER TABLE gemeinde_stammdaten
ADD CONSTRAINT UK_gemeinde_stammdaten_korrespondenz_id UNIQUE (gemeinde_stammdaten_korrespondenz_id);

ALTER TABLE gemeinde_stammdaten
ADD CONSTRAINT FK_gemeindestammdaten_stammdatenkorrespondenz_id
	FOREIGN KEY (gemeinde_stammdaten_korrespondenz_id)
		REFERENCES gemeinde_stammdaten_korrespondenz(id);

ALTER TABLE gemeinde_stammdaten_korrespondenz_aud
ADD CONSTRAINT FK_gemeinde_stammdaten_korrespondenz_aud_revinfo
	FOREIGN KEY (rev)
		REFERENCES revinfo(rev);
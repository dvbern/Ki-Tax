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

CREATE TABLE gesuchsteller_ausweis_dokument (
	id                                   BINARY(16)   NOT NULL,
	timestamp_erstellt                   DATETIME     NOT NULL,
	timestamp_mutiert                    DATETIME     NOT NULL,
	user_erstellt                        VARCHAR(255) NOT NULL,
	user_mutiert                         VARCHAR(255) NOT NULL,
	version                              BIGINT       NOT NULL,
	vorgaenger_id                        VARCHAR(36),
	filename                             VARCHAR(255) NOT NULL,
	filepfad                             text         NOT NULL,
	filesize                             VARCHAR(255) NOT NULL,
	timestamp_upload                     DATETIME     NOT NULL,
	gesuch_id			 BINARY(16)   NOT NULL,
	PRIMARY key (id)
);

CREATE TABLE gesuchsteller_ausweis_dokument_aud (
	id                                   BINARY(16) NOT NULL,
	rev                                  INTEGER    NOT NULL,
	revtype                              TINYINT,
	timestamp_erstellt                   DATETIME,
	timestamp_mutiert                    DATETIME,
	user_erstellt                        VARCHAR(255),
	user_mutiert                         VARCHAR(255),
	vorgaenger_id                        VARCHAR(36),
	filename                             VARCHAR(255),
	filepfad                             text,
	filesize                             VARCHAR(255),
	timestamp_upload                     DATETIME,
	gesuch_id 			 BINARY(16),
	PRIMARY key (id, rev)
);

ALTER TABLE gesuchsteller_ausweis_dokument_aud
	ADD CONSTRAINT FK_gesuchsteller_ausweis_dokument_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE gesuchsteller_ausweis_dokument
	ADD CONSTRAINT FK_gesuchstellerAusweisDokument_gesuch_id
	    FOREIGN key (gesuch_id) REFERENCES gesuch(id);

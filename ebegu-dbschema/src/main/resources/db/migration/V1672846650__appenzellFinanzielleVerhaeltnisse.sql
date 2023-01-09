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

CREATE TABLE finanzielle_verhaeltnisse (
	id                                			BINARY(16)   NOT NULL,
	timestamp_erstellt                			DATETIME     NOT NULL,
	timestamp_mutiert                 			DATETIME     NOT NULL,
	user_erstellt                     			VARCHAR(255) NOT NULL,
	user_mutiert                      			VARCHAR(255) NOT NULL,
	version                           			BIGINT       NOT NULL,
	vorgaenger_id                     			VARCHAR(36),
	saeule3a      								DECIMAL(19, 2),
	saeule3a_nicht_bvg      					DECIMAL(19, 2),
	berufliche_vorsorge      					DECIMAL(19, 2),
	liegenschaftsaufwand      					DECIMAL(19, 2),
	einkuenfte_bgsa      						DECIMAL(19, 2),
	vorjahresverluste      						DECIMAL(19, 2),
	politische_partei_spende      				DECIMAL(19, 2),
	leistung_an_juristische_personen			DECIMAL(19,2),
	PRIMARY KEY (id)
);

CREATE TABLE finanzielle_verhaeltnisse_aud (
	id                                			BINARY(16)   NOT NULL,
	rev											INTEGER		 NOT NULL,
	revtype										TINYINT,
	timestamp_erstellt                			DATETIME,
	timestamp_mutiert                 			DATETIME,
	user_erstellt                     			VARCHAR(255),
	user_mutiert                      			VARCHAR(255),
	version                           			BIGINT,
	vorgaenger_id                     			VARCHAR(36),
	saeule3a     								DECIMAL(19, 2),
	saeule3a_nicht_bvg      					DECIMAL(19, 2),
	berufliche_vorsorge      					DECIMAL(19, 2),
	liegenschaftsaufwand      					DECIMAL(19, 2),
	einkuenfte_bgsa      						DECIMAL(19, 2),
	vorjahresverluste      						DECIMAL(19, 2),
	politische_partei_spende      				DECIMAL(19, 2),
	leistung_an_juristische_personen			DECIMAL(19,2),
	primary key (id, rev)
);

ALTER TABLE finanzielle_situation
	ADD COLUMN finanzielle_verhaeltnisse_id BINARY(16);

ALTER TABLE finanzielle_situation_aud
	ADD COLUMN finanzielle_verhaeltnisse_id BINARY(16);

ALTER TABLE finanzielle_situation
	ADD CONSTRAINT FK_finanziellesituation_finanzielle_verhaeltnisse_id
		FOREIGN KEY (finanzielle_verhaeltnisse_id)
			REFERENCES finanzielle_verhaeltnisse(id);

alter table finanzielle_verhaeltnisse_aud
	add constraint FK_finanzielle_verhaeltnisse_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

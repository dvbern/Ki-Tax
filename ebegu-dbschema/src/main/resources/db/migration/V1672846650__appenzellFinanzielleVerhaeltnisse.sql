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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

CREATE TABLE fin_sit_zusatzangaben_appenzell (
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

CREATE TABLE fin_sit_zusatzangaben_appenzell_aud (
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
	ADD COLUMN fin_sit_zusatzangaben_appenzell_id BINARY(16);

ALTER TABLE finanzielle_situation_aud
	ADD COLUMN fin_sit_zusatzangaben_appenzell_id BINARY(16);

ALTER TABLE finanzielle_situation
	ADD CONSTRAINT FK_finanziellesituation_fin_sit_appenzell_id
		FOREIGN KEY (fin_sit_zusatzangaben_appenzell_id)
			REFERENCES fin_sit_zusatzangaben_appenzell(id);

alter table fin_sit_zusatzangaben_appenzell_aud
	add constraint FK_fin_sit_appenzell_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

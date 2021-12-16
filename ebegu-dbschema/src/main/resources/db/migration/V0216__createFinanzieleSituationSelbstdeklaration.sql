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

CREATE TABLE finanzielle_situation_selbstdeklaration (
	id                                			BINARY(16)   NOT NULL,
	timestamp_erstellt                			DATETIME     NOT NULL,
	timestamp_mutiert                 			DATETIME     NOT NULL,
	user_erstellt                     			VARCHAR(255) NOT NULL,
	user_mutiert                      			VARCHAR(255) NOT NULL,
	version                           			BIGINT       NOT NULL,
	vorgaenger_id                     			VARCHAR(36),
	einkuft_erwerb                    			DECIMAL(19, 2),
	einkuft_versicherung              			DECIMAL(19, 2),
	einkuft_ausgleichskassen                	DECIMAL(19, 2),
	einkuft_wertschriften                    	DECIMAL(19, 2),
	einkuft_unterhaltsbeitrag_steuerpflichtige 	DECIMAL(19, 2),
	einkuft_unterhaltsbeitrag_kinder        	DECIMAL(19, 2),
	einkuft_ueberige                         	DECIMAL(19, 2),
	einkunft_liegenschaften                     DECIMAL(19, 2),
	abzug_berufsauslagen 						DECIMAL(19, 2),
	abzug_schuldzinsen 							DECIMAL(19, 2),
	abzug_unterhaltsbeitrag_ehepartner 			DECIMAL(19, 2),
	abzug_unterhaltsbeitrag_kinder 				DECIMAL(19, 2),
	abzug_rentenleistungen 						DECIMAL(19, 2),
	abzug_saeule3A 							    DECIMAL(19, 2),
	abzug_versicherungspraemien 				DECIMAL(19, 2),
	abzug_krankheits_unfall_kosten      		DECIMAL(19, 2),
	abzug_freiweilige_zuwendung_partien      	DECIMAL(19, 2),
	abzug_kinder_vorschule      				DECIMAL(19, 2),
	abzug_kinder_schule      					DECIMAL(19, 2),
	abzug_kinder_auswaertiger_aufenthalt      	DECIMAL(19, 2),
	abzug_eigenbetreuung      					DECIMAL(19, 2),
	abzug_fremdbetreuung      					DECIMAL(19, 2),
	abzug_erwerbsunfaehige_personen      		DECIMAL(19, 2),
	abzug_steuerfreier_betrag_erwachsene      	DECIMAL(19, 2),
	abzug_steuerfreier_betrag_kinder      		DECIMAL(19, 2),
	vermoegen      								DECIMAL(19, 2),
	PRIMARY KEY (id)
);

CREATE TABLE finanzielle_situation_selbstdeklaration_aud (
	id                                			BINARY(16)   NOT NULL,
	rev											INTEGER		 NOT NULL,
	revtype										TINYINT,
	timestamp_erstellt                			DATETIME,
	timestamp_mutiert                 			DATETIME,
	user_erstellt                     			VARCHAR(255),
	user_mutiert                      			VARCHAR(255),
	version                           			BIGINT,
	vorgaenger_id                     			VARCHAR(36),
	einkuft_erwerb                    			DECIMAL(19, 2),
	einkuft_versicherung              			DECIMAL(19, 2),
	einkuft_ausgleichskassen                	DECIMAL(19, 2),
	einkuft_wertschriften                    	DECIMAL(19, 2),
	einkuft_unterhaltsbeitrag_steuerpflichtige 	DECIMAL(19, 2),
	einkuft_unterhaltsbeitrag_kinder        	DECIMAL(19, 2),
	einkuft_ueberige                         	DECIMAL(19, 2),
	einkunft_liegenschaften                     DECIMAL(19, 2),
	abzug_berufsauslagen 						DECIMAL(19, 2),
	abzug_schuldzinsen 							DECIMAL(19, 2),
	abzug_unterhaltsbeitrag_ehepartner 			DECIMAL(19, 2),
	abzug_unterhaltsbeitrag_kinder 				DECIMAL(19, 2),
	abzug_rentenleistungen 						DECIMAL(19, 2),
	abzug_saeule3A 							    DECIMAL(19, 2),
	abzug_versicherungspraemien 				DECIMAL(19, 2),
	abzug_krankheits_unfall_kosten      		DECIMAL(19, 2),
	abzug_freiweilige_zuwendung_partien      	DECIMAL(19, 2),
	abzug_kinder_vorschule      				DECIMAL(19, 2),
	abzug_kinder_schule      					DECIMAL(19, 2),
	abzug_kinder_auswaertiger_aufenthalt      	DECIMAL(19, 2),
	abzug_eigenbetreuung      					DECIMAL(19, 2),
	abzug_fremdbetreuung      					DECIMAL(19, 2),
	abzug_erwerbsunfaehige_personen      		DECIMAL(19, 2),
	abzug_steuerfreier_betrag_erwachsene      	DECIMAL(19, 2),
	abzug_steuerfreier_betrag_kinder      		DECIMAL(19, 2),
	vermoegen      								DECIMAL(19, 2),
	PRIMARY KEY (id)
);

ALTER TABLE finanzielle_situation
    ADD COLUMN selbstdeklaration_id BINARY(16);

ALTER TABLE finanzielle_situation_aud
ADD COLUMN selbstdeklaration_id BINARY(16);

ALTER TABLE finanzielle_situation
ADD CONSTRAINT FK_finanziellesituation_selbstdeklaration_id
	FOREIGN KEY (selbstdeklaration_id)
		REFERENCES finanzielle_situation_selbstdeklaration(id);

alter table finanzielle_situation_selbstdeklaration_aud
add constraint FK_finanzielle_situation_selbstdeklaration_aud_revinfo
	foreign key (rev)
		references revinfo (rev);

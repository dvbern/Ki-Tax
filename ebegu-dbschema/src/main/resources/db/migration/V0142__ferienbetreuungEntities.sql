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

 # Ferienbetreuung Angaben

CREATE TABLE ferienbetreuung_angaben (
	id                                                           BINARY(16)   NOT NULL,
	timestamp_erstellt                                           DATETIME     NOT NULL,
	timestamp_mutiert                                            DATETIME     NOT NULL,
	user_erstellt                                                VARCHAR(255) NOT NULL,
	user_mutiert                                                 VARCHAR(255) NOT NULL,
	version                                                      BIGINT       NOT NULL,
	angebot                                                      VARCHAR(255),
	angebot_kontaktperson_nachname                               VARCHAR(255),
	angebot_kontaktperson_vorname                                VARCHAR(255),
	angebot_vereine_und_private_integriert                       BIT,
	anzahl_betreute_kinder                                       DECIMAL(19, 2),
	anzahl_betreute_kinder_1_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_2_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_3_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_sonderschueler                        DECIMAL(19, 2),
	anzahl_betreuungstage_kinder_bern                            DECIMAL(19, 2),
	anzahl_ferienwochen_fruehlingsferien                         DECIMAL(19, 2),
	anzahl_ferienwochen_herbstferien                             DECIMAL(19, 2),
	anzahl_ferienwochen_sommerferien                             DECIMAL(19, 2),
	anzahl_ferienwochen_winterferien                             DECIMAL(19, 2),
	anzahl_stunden_pro_betreuungstag                             DECIMAL(19, 2),
	anzahl_tage                                                  DECIMAL(19, 2),
	aufwand_betreuungspersonal                                   DECIMAL(19, 2),
	bemerkungen_kooperation                                      text,
	bemerkungen_kosten                                           text,
	bemerkungen_oeffnungszeiten                                  text,
	bemerkungen_personal                                         text,
	bemerkungen_tarifsystem                                      text,
	betreuungstage_kinder_dieser_gemeinde                        DECIMAL(19, 2),
	betreuungstage_kinder_dieser_gemeinde_sonderschueler         DECIMAL(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden                DECIMAL(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden_sonderschueler DECIMAL(19, 2),
	einkommensabhaengiger_tarif_kinder_der_gemeinde              BIT,
	elterngebuehren                                              DECIMAL(19, 2),
	ferienbetreuung_tarif_wird_aus_tagesschule_tarif_abgeleitet  BIT,
	fixer_tarif_kinder_der_gemeinde                              BIT,
	gemeinde_beauftragt_externe_anbieter                         BIT,
	gemeinde_fuehrt_angebot_selber                               BIT,
	gemeindebeitrag                                              DECIMAL(19, 2),
	kantonsbeitrag                                               DECIMAL(19, 2),
	kinder_aus_anderen_gemeinden_zahlen_anderen_tarif            BIT,
	leitung_durch_person_mit_ausbildung                          BIT,
	personalkosten                                               DECIMAL(19, 2),
	personalkosten_leitung_admin                                 DECIMAL(19, 2),
	sachkosten                                                   DECIMAL(19, 2),
	stammdaten_kontaktperson_email                               VARCHAR(255),
	stammdaten_kontaktperson_funktion                            VARCHAR(255),
	stammdaten_kontaktperson_nachname                            VARCHAR(255),
	stammdaten_kontaktperson_telefon                             VARCHAR(255),
	stammdaten_kontaktperson_vorname                             VARCHAR(255),
	tagesschule_tarif_gilt_fuer_ferienbetreuung                  BIT,
	traegerschaft                                                VARCHAR(255),
	verpflegungskosten                                           DECIMAL(19, 2),
	weitere_einnahmen                                            DECIMAL(19, 2),
	weitere_kosten                                               DECIMAL(19, 2),
	zusaetzlicher_aufwand_leitung_admin                          DECIMAL(19, 2),
	angebot_adresse_id                                           BINARY(16),
	auszahlungsdaten_id                                          BINARY(16),
	stammdaten_adresse_id                                        BINARY(16)   NOT NULL,
	PRIMARY key (id)
);

CREATE TABLE ferienbetreuung_angaben_aud (
	id                                                           BINARY(16) NOT NULL,
	rev                                                          INTEGER    NOT NULL,
	revtype                                                      TINYINT,
	timestamp_erstellt                                           DATETIME,
	timestamp_mutiert                                            DATETIME,
	user_erstellt                                                VARCHAR(255),
	user_mutiert                                                 VARCHAR(255),
	angebot                                                      VARCHAR(255),
	angebot_kontaktperson_nachname                               VARCHAR(255),
	angebot_kontaktperson_vorname                                VARCHAR(255),
	angebot_vereine_und_private_integriert                       BIT,
	anzahl_betreute_kinder                                       DECIMAL(19, 2),
	anzahl_betreute_kinder_1_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_2_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_3_zyklus                              DECIMAL(19, 2),
	anzahl_betreute_kinder_sonderschueler                        DECIMAL(19, 2),
	anzahl_betreuungstage_kinder_bern                            DECIMAL(19, 2),
	anzahl_ferienwochen_fruehlingsferien                         DECIMAL(19, 2),
	anzahl_ferienwochen_herbstferien                             DECIMAL(19, 2),
	anzahl_ferienwochen_sommerferien                             DECIMAL(19, 2),
	anzahl_ferienwochen_winterferien                             DECIMAL(19, 2),
	anzahl_stunden_pro_betreuungstag                             DECIMAL(19, 2),
	anzahl_tage                                                  DECIMAL(19, 2),
	aufwand_betreuungspersonal                                   DECIMAL(19, 2),
	bemerkungen_kooperation                                      text,
	bemerkungen_kosten                                           text,
	bemerkungen_oeffnungszeiten                                  text,
	bemerkungen_personal                                         text,
	bemerkungen_tarifsystem                                      text,
	betreuungstage_kinder_dieser_gemeinde                        DECIMAL(19, 2),
	betreuungstage_kinder_dieser_gemeinde_sonderschueler         DECIMAL(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden                DECIMAL(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden_sonderschueler DECIMAL(19, 2),
	einkommensabhaengiger_tarif_kinder_der_gemeinde              BIT,
	elterngebuehren                                              DECIMAL(19, 2),
	ferienbetreuung_tarif_wird_aus_tagesschule_tarif_abgeleitet  BIT,
	fixer_tarif_kinder_der_gemeinde                              BIT,
	gemeinde_beauftragt_externe_anbieter                         BIT,
	gemeinde_fuehrt_angebot_selber                               BIT,
	gemeindebeitrag                                              DECIMAL(19, 2),
	kantonsbeitrag                                               DECIMAL(19, 2),
	kinder_aus_anderen_gemeinden_zahlen_anderen_tarif            BIT,
	leitung_durch_person_mit_ausbildung                          BIT,
	personalkosten                                               DECIMAL(19, 2),
	personalkosten_leitung_admin                                 DECIMAL(19, 2),
	sachkosten                                                   DECIMAL(19, 2),
	stammdaten_kontaktperson_email                               VARCHAR(255),
	stammdaten_kontaktperson_funktion                            VARCHAR(255),
	stammdaten_kontaktperson_nachname                            VARCHAR(255),
	stammdaten_kontaktperson_telefon                             VARCHAR(255),
	stammdaten_kontaktperson_vorname                             VARCHAR(255),
	tagesschule_tarif_gilt_fuer_ferienbetreuung                  BIT,
	traegerschaft                                                VARCHAR(255),
	verpflegungskosten                                           DECIMAL(19, 2),
	weitere_einnahmen                                            DECIMAL(19, 2),
	weitere_kosten                                               DECIMAL(19, 2),
	zusaetzlicher_aufwand_leitung_admin                          DECIMAL(19, 2),
	angebot_adresse_id                                           BINARY(16),
	auszahlungsdaten_id                                          BINARY(16),
	stammdaten_adresse_id                                        BINARY(16),
	PRIMARY key (id, rev)
);

ALTER TABLE ferienbetreuung_angaben_aud
	ADD CONSTRAINT FK_ferienbetreuung_angaben_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE ferienbetreuung_angaben
	ADD CONSTRAINT FK_ferienbetreuung_angebot_adresse_id
		FOREIGN key (angebot_adresse_id) REFERENCES adresse(id);

ALTER TABLE ferienbetreuung_angaben
	ADD CONSTRAINT FK_ferienbetreuung_angaben_auszahlungsdaten_id
		FOREIGN key (auszahlungsdaten_id) REFERENCES auszahlungsdaten(id);

ALTER TABLE ferienbetreuung_angaben
	ADD CONSTRAINT FK_ferienbetreuung_stammdaten_adresse_id
		FOREIGN key (stammdaten_adresse_id) REFERENCES adresse(id);

# Ferienbetreuung Angaben Container

CREATE TABLE ferienbetreuung_angaben_container (
	id                     BINARY(16)   NOT NULL,
	timestamp_erstellt     DATETIME     NOT NULL,
	timestamp_mutiert      DATETIME     NOT NULL,
	user_erstellt          VARCHAR(255) NOT NULL,
	user_mutiert           VARCHAR(255) NOT NULL,
	version                BIGINT       NOT NULL,
	interner_kommentar     VARCHAR(255),
	status                 VARCHAR(255) NOT NULL,
	angaben_deklaration_id BINARY(16)   NOT NULL,
	angaben_korrektur_id   BINARY(16),
	gemeinde_id            BINARY(16)   NOT NULL,
	gesuchsperiode_id      BINARY(16)   NOT NULL,
	PRIMARY key (id)
);

CREATE TABLE ferienbetreuung_angaben_container_aud (
	id                     BINARY(16) NOT NULL,
	rev                    INTEGER    NOT NULL,
	revtype                TINYINT,
	timestamp_erstellt     DATETIME,
	timestamp_mutiert      DATETIME,
	user_erstellt          VARCHAR(255),
	user_mutiert           VARCHAR(255),
	interner_kommentar     VARCHAR(255),
	status                 VARCHAR(255),
	angaben_deklaration_id BINARY(16),
	angaben_korrektur_id   BINARY(16),
	gemeinde_id            BINARY(16),
	gesuchsperiode_id      BINARY(16),
	PRIMARY key (id, rev)
);

ALTER TABLE ferienbetreuung_angaben_container_aud
	ADD CONSTRAINT FK_ferienbetreuung_angaben_container_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE ferienbetreuung_angaben_container
	ADD CONSTRAINT FK_ferienbetreuung_container_deklaration_id
		FOREIGN key (angaben_deklaration_id) REFERENCES ferienbetreuung_angaben(id);

ALTER TABLE ferienbetreuung_angaben_container
	ADD CONSTRAINT FK_ferienbetreuung_container_korrektur_id
		FOREIGN key (angaben_korrektur_id) REFERENCES ferienbetreuung_angaben(id);

ALTER TABLE ferienbetreuung_angaben_container
	ADD CONSTRAINT FK_ferienbetreuung_container_gemeinde_id
		FOREIGN key (gemeinde_id) REFERENCES gemeinde(id);

ALTER TABLE ferienbetreuung_angaben_container
	ADD CONSTRAINT FK_ferienbetreuung_container_gesuchsperiode_id
		FOREIGN key (gesuchsperiode_id) REFERENCES gesuchsperiode(id);


# Ferienbetreuung am Angebot beteiligte Gemeinden

CREATE TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden (
	ferienbetreuung_angaben_id BINARY(16) NOT NULL,
	gemeinde_id                BINARY(16) NOT NULL,
	PRIMARY key (ferienbetreuung_angaben_id, gemeinde_id)
);

CREATE TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden_aud (
	rev                        INTEGER    NOT NULL,
	ferienbetreuung_angaben_id BINARY(16) NOT NULL,
	gemeinde_id                BINARY(16) NOT NULL,
	revtype                    TINYINT,
	PRIMARY key (rev, ferienbetreuung_angaben_id, gemeinde_id)
);

ALTER TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden_aud
	ADD CONSTRAINT FK_ferienbetreuung_am_angebot_beteiligte_gemeinden_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden
	ADD CONSTRAINT ferienbetreuung_am_angebot_beteiligte_gemeinden_gemeinde_id
		FOREIGN key (gemeinde_id) REFERENCES gemeinde(id);

ALTER TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden
	ADD CONSTRAINT ferienbetreuung_am_angebot_beteiligte_gemeinden_angaben_id
		FOREIGN key (ferienbetreuung_angaben_id) REFERENCES ferienbetreuung_angaben(id);

CREATE INDEX IX_ferienbetreuung_am_angebot_beteiligte_gemeinden_angaben_id
	ON ferienbetreuung_am_angebot_beteiligte_gemeinden(ferienbetreuung_angaben_id);

CREATE INDEX IX_ferienbetreuung_am_angebot_beteiligte_gemeinden_gemeinde_id
	ON ferienbetreuung_am_angebot_beteiligte_gemeinden(gemeinde_id);

# Ferienbetreuung finanziell beteiligte Gemeinden

CREATE TABLE ferienbetreuung_finanziell_beteiligte_gemeinden (
	ferienbetreuung_angaben_id BINARY(16) NOT NULL,
	gemeinde_id                BINARY(16) NOT NULL,
	PRIMARY key (ferienbetreuung_angaben_id, gemeinde_id)
);

CREATE TABLE ferienbetreuung_finanziell_beteiligte_gemeinden_aud (
	rev                        INTEGER    NOT NULL,
	ferienbetreuung_angaben_id BINARY(16) NOT NULL,
	gemeinde_id                BINARY(16) NOT NULL,
	revtype                    TINYINT,
	PRIMARY key (rev, ferienbetreuung_angaben_id, gemeinde_id)
);

ALTER TABLE ferienbetreuung_finanziell_beteiligte_gemeinden_aud
	ADD CONSTRAINT FK_ferienbetreuung_finanziell_beteiligte_gemeinden_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE ferienbetreuung_finanziell_beteiligte_gemeinden
	ADD CONSTRAINT FK_ferienbetreuung_finanziell_beteiligte_gemeinden_gemeinde_id
		FOREIGN key (gemeinde_id) REFERENCES gemeinde(id);

ALTER TABLE ferienbetreuung_finanziell_beteiligte_gemeinden
	ADD CONSTRAINT FK_ferienbetreuung_finanziell_beteiligte_gemeinden_angaben_id
		FOREIGN key (ferienbetreuung_angaben_id) REFERENCES ferienbetreuung_angaben(id);

CREATE INDEX IX_ferienbetreuung_finanziell_beteiligte_gemeinden_angaben_id
	ON ferienbetreuung_finanziell_beteiligte_gemeinden(ferienbetreuung_angaben_id);

CREATE INDEX IX_ferienbetreuung_finanziell_beteiligte_gemeinden_gemeinde_id
	ON ferienbetreuung_finanziell_beteiligte_gemeinden(gemeinde_id);

# Dokumente

CREATE TABLE ferienbetreuung_dokument (
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
	ferienbetreuung_angaben_container_id BINARY(16)   NOT NULL,
	PRIMARY key (id)
);

CREATE TABLE ferienbetreuung_dokument_aud (
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
	ferienbetreuung_angaben_container_id BINARY(16),
	PRIMARY key (id, rev)
);

ALTER TABLE ferienbetreuung_dokument_aud
	ADD CONSTRAINT FK_ferienbetreuung_dokument_aud
		FOREIGN key (rev) REFERENCES revinfo(rev);

ALTER TABLE ferienbetreuung_dokument
	ADD CONSTRAINT FK_ferienbetreuungDokument_ferienbetreuungAngabenContainer_id
	    FOREIGN key (ferienbetreuung_angaben_container_id) REFERENCES ferienbetreuung_angaben_container(id);

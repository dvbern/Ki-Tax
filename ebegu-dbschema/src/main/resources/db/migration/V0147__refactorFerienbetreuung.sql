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

#  NEW TABLES

create table ferienbetreuung_angaben_stammdaten (
	id                                binary(16)   not null,
	timestamp_erstellt                datetime     not null,
	timestamp_mutiert                 datetime     not null,
	user_erstellt                     varchar(255) not null,
	user_mutiert                      varchar(255) not null,
	version                           bigint       not null,
	seit_wann_ferienbetreuungen       varchar(255),
	stammdaten_kontaktperson_email    varchar(255),
	stammdaten_kontaktperson_funktion varchar(255),
	stammdaten_kontaktperson_nachname varchar(255),
	stammdaten_kontaktperson_telefon  varchar(255),
	stammdaten_kontaktperson_vorname  varchar(255),
	traegerschaft                     varchar(255),
	vermerk_auszahlung                varchar(255),
	auszahlungsdaten_id               binary(16),
	stammdaten_adresse_id             binary(16),
	primary key (id)
);

create table ferienbetreuung_angaben_stammdaten_aud (
	id                                binary(16) not null,
	rev                               integer    not null,
	revtype                           tinyint,
	timestamp_erstellt                datetime,
	timestamp_mutiert                 datetime,
	user_erstellt                     varchar(255),
	user_mutiert                      varchar(255),
	seit_wann_ferienbetreuungen       varchar(255),
	stammdaten_kontaktperson_email    varchar(255),
	stammdaten_kontaktperson_funktion varchar(255),
	stammdaten_kontaktperson_nachname varchar(255),
	stammdaten_kontaktperson_telefon  varchar(255),
	stammdaten_kontaktperson_vorname  varchar(255),
	traegerschaft                     varchar(255),
	vermerk_auszahlung                varchar(255),
	auszahlungsdaten_id               binary(16),
	stammdaten_adresse_id             binary(16),
	primary key (id, rev)
);

create table ferienbetreuung_angaben_angebot (
	id                                                          binary(16)   not null,
	timestamp_erstellt                                          datetime     not null,
	timestamp_mutiert                                           datetime     not null,
	user_erstellt                                               varchar(255) not null,
	user_mutiert                                                varchar(255) not null,
	version                                                     bigint       not null,
	angebot                                                     varchar(255),
	angebot_kontaktperson_nachname                              varchar(255),
	angebot_kontaktperson_vorname                               varchar(255),
	angebot_vereine_und_private_integriert                      bit,
	anzahl_ferienwochen_fruehlingsferien                        decimal(19, 2),
	anzahl_ferienwochen_herbstferien                            decimal(19, 2),
	anzahl_ferienwochen_sommerferien                            decimal(19, 2),
	anzahl_ferienwochen_winterferien                            decimal(19, 2),
	anzahl_kinder_angemessen                                    bit,
	anzahl_stunden_pro_betreuungstag                            decimal(19, 2),
	anzahl_tage                                                 decimal(19, 2),
	bemerkungen_anzahl_ferienwochen								text,
	bemerkungen_kooperation                                     text,
	bemerkungen_oeffnungszeiten                                 text,
	bemerkungen_personal                                        text,
	bemerkungen_tarifsystem                                     text,
	betreuung_durch_personen_mit_erfahrung                      bit,
	betreuung_erfolgt_tagsueber                                 bit,
	betreuungsschluessel                                        decimal(19, 2),
	einkommensabhaengiger_tarif_kinder_der_gemeinde             bit,
	ferienbetreuung_tarif_wird_aus_tagesschule_tarif_abgeleitet bit,
	fixer_tarif_kinder_der_gemeinde                             bit,
	gemeinde_beauftragt_externe_anbieter                        bit,
	gemeinde_fuehrt_angebot_selber                              bit,
	kinder_aus_anderen_gemeinden_zahlen_anderen_tarif           bit,
	leitung_durch_person_mit_ausbildung                         bit,
	tagesschule_tarif_gilt_fuer_ferienbetreuung                 bit,
	angebot_adresse_id                                          binary(16),
	primary key (id)
);

create table ferienbetreuung_angaben_angebot_aud (
	id                                                          binary(16) not null,
	rev                                                         integer    not null,
	revtype                                                     tinyint,
	timestamp_erstellt                                          datetime,
	timestamp_mutiert                                           datetime,
	user_erstellt                                               varchar(255),
	user_mutiert                                                varchar(255),
	angebot                                                     varchar(255),
	angebot_kontaktperson_nachname                              varchar(255),
	angebot_kontaktperson_vorname                               varchar(255),
	angebot_vereine_und_private_integriert                      bit,
	anzahl_ferienwochen_fruehlingsferien                        decimal(19, 2),
	anzahl_ferienwochen_herbstferien                            decimal(19, 2),
	anzahl_ferienwochen_sommerferien                            decimal(19, 2),
	anzahl_ferienwochen_winterferien                            decimal(19, 2),
	anzahl_kinder_angemessen                                    bit,
	anzahl_stunden_pro_betreuungstag                            decimal(19, 2),
	anzahl_tage                                                 decimal(19, 2),
	bemerkungen_anzahl_ferienwochen								text,
	bemerkungen_kooperation                                     text,
	bemerkungen_oeffnungszeiten                                 text,
	bemerkungen_personal                                        text,
	bemerkungen_tarifsystem                                     text,
	betreuung_durch_personen_mit_erfahrung                      bit,
	betreuung_erfolgt_tagsueber                                 bit,
	betreuungsschluessel                                        decimal(19, 2),
	einkommensabhaengiger_tarif_kinder_der_gemeinde             bit,
	ferienbetreuung_tarif_wird_aus_tagesschule_tarif_abgeleitet bit,
	fixer_tarif_kinder_der_gemeinde                             bit,
	gemeinde_beauftragt_externe_anbieter                        bit,
	gemeinde_fuehrt_angebot_selber                              bit,
	kinder_aus_anderen_gemeinden_zahlen_anderen_tarif           bit,
	leitung_durch_person_mit_ausbildung                         bit,
	tagesschule_tarif_gilt_fuer_ferienbetreuung                 bit,
	angebot_adresse_id                                          binary(16),
	primary key (id, rev)
);

create table ferienbetreuung_angaben_nutzung_aud (
	id                                                           binary(16) not null,
	rev                                                          integer    not null,
	revtype                                                      tinyint,
	timestamp_erstellt                                           datetime,
	timestamp_mutiert                                            datetime,
	user_erstellt                                                varchar(255),
	user_mutiert                                                 varchar(255),
	anzahl_betreute_kinder                                       decimal(19, 2),
	anzahl_betreute_kinder_1_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_2_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_3_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_sonderschueler                        decimal(19, 2),
	anzahl_betreuungstage_kinder_bern                            decimal(19, 2),
	betreuungstage_kinder_dieser_gemeinde                        decimal(19, 2),
	betreuungstage_kinder_dieser_gemeinde_sonderschueler         decimal(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden                decimal(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden_sonderschueler decimal(19, 2),
	primary key (id, rev)
);

create table ferienbetreuung_angaben_nutzung (
	id                                                           binary(16)   not null,
	timestamp_erstellt                                           datetime     not null,
	timestamp_mutiert                                            datetime     not null,
	user_erstellt                                                varchar(255) not null,
	user_mutiert                                                 varchar(255) not null,
	version                                                      bigint       not null,
	anzahl_betreute_kinder                                       decimal(19, 2),
	anzahl_betreute_kinder_1_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_2_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_3_zyklus                              decimal(19, 2),
	anzahl_betreute_kinder_sonderschueler                        decimal(19, 2),
	anzahl_betreuungstage_kinder_bern                            decimal(19, 2),
	betreuungstage_kinder_dieser_gemeinde                        decimal(19, 2),
	betreuungstage_kinder_dieser_gemeinde_sonderschueler         decimal(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden                decimal(19, 2),
	davon_betreuungstage_kinder_anderer_gemeinden_sonderschueler decimal(19, 2),
	primary key (id)
);

create table ferienbetreuung_angaben_kosten_einnahmen_aud (
	id                           binary(16) not null,
	rev                          integer    not null,
	revtype                      tinyint,
	timestamp_erstellt           datetime,
	timestamp_mutiert            datetime,
	user_erstellt                varchar(255),
	user_mutiert                 varchar(255),
	bemerkungen_kosten           text,
	elterngebuehren              decimal(19, 2),
	personalkosten               decimal(19, 2),
	personalkosten_leitung_admin decimal(19, 2),
	sachkosten                   decimal(19, 2),
	verpflegungskosten           decimal(19, 2),
	weitere_einnahmen            decimal(19, 2),
	weitere_kosten               decimal(19, 2),
	primary key (id, rev)
);

create table ferienbetreuung_angaben_kosten_einnahmen (
	id                           binary(16)   not null,
	timestamp_erstellt           datetime     not null,
	timestamp_mutiert            datetime     not null,
	user_erstellt                varchar(255) not null,
	user_mutiert                 varchar(255) not null,
	version                      bigint       not null,
	bemerkungen_kosten           text,
	elterngebuehren              decimal(19, 2),
	personalkosten               decimal(19, 2),
	personalkosten_leitung_admin decimal(19, 2),
	sachkosten                   decimal(19, 2),
	verpflegungskosten           decimal(19, 2),
	weitere_einnahmen            decimal(19, 2),
	weitere_kosten               decimal(19, 2),
	primary key (id)
);

# DROP OLD TABLES (FOREIGN KEYS ARE DELETED WITH TABLES)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE ferienbetreuung_angaben;
DROP TABLE ferienbetreuung_angaben_aud;
TRUNCATE TABLE ferienbetreuung_angaben_container;
TRUNCATE TABLE ferienbetreuung_angaben_container_aud;
TRUNCATE TABLE ebegu.ferienbetreuung_dokument;
TRUNCATE TABLE ebegu.ferienbetreuung_dokument_aud;
DROP TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden;
DROP TABLE ferienbetreuung_am_angebot_beteiligte_gemeinden_aud;
DROP TABLE ferienbetreuung_finanziell_beteiligte_gemeinden;
DROP TABLE ferienbetreuung_finanziell_beteiligte_gemeinden_aud;
SET FOREIGN_KEY_CHECKS = 1;

# RECREATE FERIENBETREUUNG_ANGABEN WITH NEW STRUCTURE

create table ferienbetreuung_angaben (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	gemeindebeitrag decimal(19,2),
	kantonsbeitrag decimal(19,2),
	ferienbetreuung_angaben_angebot_id binary(16) not null,
	ferienbetreuung_angaben_kosten_einnahmen_id binary(16) not null,
	ferienbetreuung_angaben_nutzung_id binary(16) not null,
	ferienbetreuung_angaben_stammdaten_id binary(16) not null,
	primary key (id)
);

create table ferienbetreuung_angaben_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	gemeindebeitrag decimal(19,2),
	kantonsbeitrag decimal(19,2),
	ferienbetreuung_angaben_angebot_id binary(16),
	ferienbetreuung_angaben_kosten_einnahmen_id binary(16),
	ferienbetreuung_angaben_nutzung_id binary(16),
	ferienbetreuung_angaben_stammdaten_id binary(16),
	primary key (id, rev)
);

# RECREATE OTHER TABLES WITH NEW STRUCTURE

create table ferienbetreuung_am_angebot_beteiligte_gemeinden (
	ferienbetreuung_stammdaten_id binary(16) not null,
	am_angebot_beteiligte_gemeinden varchar(255) not null,
	primary key (ferienbetreuung_stammdaten_id, am_angebot_beteiligte_gemeinden)
);

create table ferienbetreuung_am_angebot_beteiligte_gemeinden_aud (
	rev integer not null,
	ferienbetreuung_stammdaten_id binary(16) not null,
	am_angebot_beteiligte_gemeinden varchar(255) not null,
	revtype tinyint,
	primary key (rev, ferienbetreuung_stammdaten_id, am_angebot_beteiligte_gemeinden)
);

create table ferienbetreuung_finanziell_beteiligte_gemeinden (
	ferienbetreuung_angebot_id binary(16) not null,
	finanziell_beteiligte_gemeinden varchar(255) not null,
	primary key (ferienbetreuung_angebot_id, finanziell_beteiligte_gemeinden)
);

create table ferienbetreuung_finanziell_beteiligte_gemeinden_aud (
	rev integer not null,
	ferienbetreuung_angebot_id binary(16) not null,
	finanziell_beteiligte_gemeinden varchar(255) not null,
	revtype tinyint,
	primary key (rev, ferienbetreuung_angebot_id, finanziell_beteiligte_gemeinden)
);

# ADD NEW FOREIGN KEYS

# angaben
alter table ferienbetreuung_angaben
	add constraint FK_ferienbetreuung_stammdaten_ferienbetreuung
		foreign key (ferienbetreuung_angaben_stammdaten_id)
			references ferienbetreuung_angaben_stammdaten(id);

alter table ferienbetreuung_angaben
	add constraint FK_ferienbetreuung_angebot_ferienbetreuung
		foreign key (ferienbetreuung_angaben_angebot_id)
			references ferienbetreuung_angaben_angebot(id);

alter table ferienbetreuung_angaben
	add constraint FK_ferienbetreuung_nutzung_ferienbetreuung
		foreign key (ferienbetreuung_angaben_nutzung_id)
			references ferienbetreuung_angaben_nutzung(id);

alter table ferienbetreuung_angaben
	add constraint FK_ferienbetreuung_kosten_einnahmen_ferienbetreuung
		foreign key (ferienbetreuung_angaben_kosten_einnahmen_id)
			references ferienbetreuung_angaben_kosten_einnahmen(id);

# others
alter table ferienbetreuung_angaben_stammdaten
	add constraint FK_ferienbetreuung_angaben_auszahlungsdaten_id
		foreign key (auszahlungsdaten_id)
			references auszahlungsdaten(id);

alter table ferienbetreuung_angaben_stammdaten
	add constraint FK_ferienbetreuung_stammdaten_adresse_id
		foreign key (stammdaten_adresse_id)
			references adresse(id);

alter table ferienbetreuung_angaben_stammdaten_aud
	add constraint FK_ferienbetreuung_stammdaten_aud
		foreign key (rev)
			references revinfo(rev);

alter table ferienbetreuung_angaben_angebot
	add constraint FK_ferienbetreuung_angebot_adresse_id
		foreign key (angebot_adresse_id)
			references adresse(id);

alter table ferienbetreuung_angaben_angebot_aud
	add constraint FK_ferienbetreuung_angebot_aud
		foreign key (rev)
			references revinfo(rev);

alter table ferienbetreuung_angaben_nutzung_aud
	add constraint FK_ferienbetreuung_nutzung_aud
		foreign key (rev)
			references revinfo(rev);

alter table ferienbetreuung_angaben_kosten_einnahmen_aud
	add constraint FK_ferienbetreuung_kosten_einnahmen_aud
		foreign key (rev)
			references revinfo(rev);

# foreign keys and indexes for ferienbetreuung_am_angebot_beteiligte_gemeinden

alter table ferienbetreuung_am_angebot_beteiligte_gemeinden
	add constraint FK_am_angebot_beteiligte_gemeinden
		foreign key (ferienbetreuung_stammdaten_id)
			references ferienbetreuung_angaben_stammdaten (id);

alter table ferienbetreuung_am_angebot_beteiligte_gemeinden_aud
	add constraint FK_am_angebot_beteiligte_gemeinden_aud
		foreign key (rev)
			references revinfo (rev);

# foreign keys and indexes for ferienbetreuung_finanziell_beteiligte_gemeinden

alter table ferienbetreuung_finanziell_beteiligte_gemeinden
	add constraint FK_finanziell_beteiligte_gemeinden
		foreign key (ferienbetreuung_angebot_id)
			references ferienbetreuung_angaben_angebot (id);

alter table ferienbetreuung_finanziell_beteiligte_gemeinden_aud
	add constraint FK_finanziell_beteiligte_gemeinden_aud
		foreign key (rev)
			references revinfo (rev);

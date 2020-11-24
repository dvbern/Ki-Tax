/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

create table lastenausgleich_tagesschule_angaben_gemeinde_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	angebot_fuer_ferienbetreuung_vorhanden bit,
	angebot_verfuegbar_fuer_alle_schulstufen bit,
	ausbildungen_mitarbeitende_belegt bit,
	bedarf_bei_eltern_abgeklaert bit,
	begruendung_wenn_angebot_nicht_verfuegbar_fuer_alle_schulstufen varchar(255),
	bemerkungen varchar(255),
	bemerkungen_weitere_kosten_und_ertraege varchar(255),
	betreuungsstunden_dokumentiert_und_ueberprueft bit,
	davon_stunden_zu_normlohn_mehr_als50prozent_ausgebildete decimal(19,2),
	davon_stunden_zu_normlohn_weniger_als50prozent_ausgebildete decimal(19,2),
	einkommen_eltern_belegt bit,
	einnahmen_elterngebuehren decimal(19,2),
	einnahmen_subventionen_dritter decimal(19,2),
	einnnahmen_verpflegung decimal(19,2),
	elterngebuehren_gemaess_verordnung_berechnet bit,
	geleistete_betreuungsstunden_besondere_beduerfnisse decimal(19,2),
	geleistete_betreuungsstunden_ohne_besondere_beduerfnisse decimal(19,2),
	gesamt_kosten_tagesschule decimal(19,2),
	interner_kommentar varchar(255),
	maximal_tarif bit,
	mindestens50prozent_betreuungszeit_durch_ausgebildetes_personal bit,
	primary key (id, rev)
);

create table lastenausgleich_tagesschule_angaben_gemeinde_container_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	status varchar(255),
	angaben_deklaration_id binary(16),
	angaben_korrektur_id binary(16),
	gemeinde_id binary(16),
	gesuchsperiode_id binary(16),
	alle_angaben_in_kibon_erfasst bit,
	primary key (id, rev)
);

create table lastenausgleich_tagesschule_angaben_gemeinde_status_history_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	status varchar(255),
	timestamp_bis datetime,
	timestamp_von datetime,
	angaben_gemeinde_container_id binary(16),
	benutzer_id binary(16),
	primary key (id, rev)
);

create table lastenausgleich_tagesschule_angaben_institution_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	anzahl_eingeschriebene_kinder decimal(19,2),
	anzahl_eingeschriebene_kinder_basisstufe decimal(19,2),
	anzahl_eingeschriebene_kinder_kindergarten decimal(19,2),
	anzahl_eingeschriebene_kinder_mit_besonderen_beduerfnissen decimal(19,2),
	anzahl_eingeschriebene_kinder_primarstufe decimal(19,2),
	bemerkungen varchar(255),
	betreuungsverhaeltnis_eingehalten bit,
	durchschnitt_kinder_pro_tag_fruehbetreuung decimal(19,2),
	durchschnitt_kinder_pro_tag_mittag decimal(19,2),
	durchschnitt_kinder_pro_tag_nachmittag1 decimal(19,2),
	durchschnitt_kinder_pro_tag_nachmittag2 decimal(19,2),
	ernaehrungs_grundsaetze_eingehalten bit,
	is_lehrbetrieb bit,
	raeumliche_voraussetzungen_eingehalten bit,
	schule_auf_basis_organisatorisches_konzept bit,
	schule_auf_basis_paedagogisches_konzept bit,
	primary key (id, rev)
);

create table lastenausgleich_tagesschule_angaben_institution_container_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	status varchar(255),
	angaben_deklaration_id binary(16),
	angaben_gemeinde_id binary(16),
	angaben_korrektur_id binary(16),
	institution_id binary(16),
	primary key (id, rev)
);

create table lastenausgleich_tagesschule_angaben_gemeinde (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	angebot_fuer_ferienbetreuung_vorhanden bit not null,
	angebot_verfuegbar_fuer_alle_schulstufen bit not null,
	ausbildungen_mitarbeitende_belegt bit not null,
	bedarf_bei_eltern_abgeklaert bit not null,
	begruendung_wenn_angebot_nicht_verfuegbar_fuer_alle_schulstufen varchar(255),
	bemerkungen varchar(255),
	bemerkungen_weitere_kosten_und_ertraege varchar(255),
	betreuungsstunden_dokumentiert_und_ueberprueft bit not null,
	davon_stunden_zu_normlohn_mehr_als50prozent_ausgebildete decimal(19,2) not null,
	davon_stunden_zu_normlohn_weniger_als50prozent_ausgebildete decimal(19,2) not null,
	einkommen_eltern_belegt bit not null,
	einnahmen_elterngebuehren decimal(19,2) not null,
	einnahmen_subventionen_dritter decimal(19,2) not null,
	einnnahmen_verpflegung decimal(19,2) not null,
	elterngebuehren_gemaess_verordnung_berechnet bit not null,
	geleistete_betreuungsstunden_besondere_beduerfnisse decimal(19,2) not null,
	geleistete_betreuungsstunden_ohne_besondere_beduerfnisse decimal(19,2) not null,
	gesamt_kosten_tagesschule decimal(19,2) not null,
	interner_kommentar varchar(255),
	maximal_tarif bit not null,
	mindestens50prozent_betreuungszeit_durch_ausgebildetes_personal bit not null,
	primary key (id)
);

create table lastenausgleich_tagesschule_angaben_gemeinde_container (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	status varchar(255) not null,
	angaben_deklaration_id binary(16),
	angaben_korrektur_id binary(16),
	gemeinde_id binary(16) not null,
	gesuchsperiode_id binary(16) not null,
	alle_angaben_in_kibon_erfasst bit,
	primary key (id)
);

create table lastenausgleich_tagesschule_angaben_gemeinde_status_history (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	status varchar(255) not null,
	timestamp_bis datetime,
	timestamp_von datetime not null,
	angaben_gemeinde_container_id binary(16) not null,
	benutzer_id binary(16) not null,
	primary key (id)
);

create table lastenausgleich_tagesschule_angaben_institution (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	anzahl_eingeschriebene_kinder decimal(19,2) not null,
	anzahl_eingeschriebene_kinder_basisstufe decimal(19,2) not null,
	anzahl_eingeschriebene_kinder_kindergarten decimal(19,2) not null,
	anzahl_eingeschriebene_kinder_mit_besonderen_beduerfnissen decimal(19,2) not null,
	anzahl_eingeschriebene_kinder_primarstufe decimal(19,2) not null,
	bemerkungen varchar(255),
	betreuungsverhaeltnis_eingehalten bit not null,
	durchschnitt_kinder_pro_tag_fruehbetreuung decimal(19,2) not null,
	durchschnitt_kinder_pro_tag_mittag decimal(19,2) not null,
	durchschnitt_kinder_pro_tag_nachmittag1 decimal(19,2) not null,
	durchschnitt_kinder_pro_tag_nachmittag2 decimal(19,2) not null,
	ernaehrungs_grundsaetze_eingehalten bit not null,
	is_lehrbetrieb bit not null,
	raeumliche_voraussetzungen_eingehalten bit not null,
	schule_auf_basis_organisatorisches_konzept bit not null,
	schule_auf_basis_paedagogisches_konzept bit not null,
	primary key (id)
);

create table lastenausgleich_tagesschule_angaben_institution_container (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	status varchar(255) not null,
	angaben_deklaration_id binary(16),
	angaben_gemeinde_id binary(16) not null,
	angaben_korrektur_id binary(16),
	institution_id binary(16) not null,
	primary key (id)
);

alter table lastenausgleich_tagesschule_angaben_gemeinde_aud
	add constraint FK_lats_angaben_gemeinde_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table lastenausgleich_tagesschule_angaben_gemeinde_container_aud
	add constraint FK_lats_angaben_gemeinde_container_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table lastenausgleich_tagesschule_angaben_gemeinde_status_history_aud
	add constraint FK_lats_angaben_gemeinde_status_history_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table lastenausgleich_tagesschule_angaben_institution_aud
	add constraint FK_lats_angaben_institution_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table lastenausgleich_tagesschule_angaben_institution_container_aud
	add constraint FK_lats_angaben_institution_container_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

alter table lastenausgleich_tagesschule_angaben_gemeinde_container
	add constraint FK_lats_fall_container_falldeklaration_id
		foreign key (angaben_deklaration_id)
			references lastenausgleich_tagesschule_angaben_gemeinde (id);

alter table lastenausgleich_tagesschule_angaben_gemeinde_container
	add constraint FK_lats_fall_container_fallkorrektur_id
		foreign key (angaben_korrektur_id)
			references lastenausgleich_tagesschule_angaben_gemeinde (id);

alter table lastenausgleich_tagesschule_angaben_gemeinde_container
	add constraint FK_lats_fall_container_gemeinde_id
		foreign key (gemeinde_id)
			references gemeinde (id);

alter table lastenausgleich_tagesschule_angaben_gemeinde_container
	add constraint FK_lats_fall_container_gesuchsperiode_id
		foreign key (gesuchsperiode_id)
			references gesuchsperiode (id);

alter table lastenausgleich_tagesschule_angaben_gemeinde_status_history
	add constraint FK_lats_statushistory_fall_id
		foreign key (angaben_gemeinde_container_id)
			references lastenausgleich_tagesschule_angaben_gemeinde_container (id);

alter table lastenausgleich_tagesschule_angaben_gemeinde_status_history
	add constraint FK_lats_statushistory_benutzer_id
		foreign key (benutzer_id)
			references benutzer (id);

alter table lastenausgleich_tagesschule_angaben_institution_container
	add constraint FK_lats_institution_container_institutiondeklaration_id
		foreign key (angaben_deklaration_id)
			references lastenausgleich_tagesschule_angaben_institution (id);

alter table lastenausgleich_tagesschule_angaben_institution_container
	add constraint FK_lats_institutionContainer_gemeindeContainerid
		foreign key (angaben_gemeinde_id)
			references lastenausgleich_tagesschule_angaben_gemeinde_container (id);

alter table lastenausgleich_tagesschule_angaben_institution_container
	add constraint FK_lats_institution_container_institutionkorrektur_id
		foreign key (angaben_korrektur_id)
			references lastenausgleich_tagesschule_angaben_institution (id);

alter table lastenausgleich_tagesschule_angaben_institution_container
	add constraint FK_lats_institution_container_institution_id
		foreign key (institution_id)
			references institution (id);
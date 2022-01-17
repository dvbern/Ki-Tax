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
CREATE TABLE steuerdaten_anfrage_log (
    id binary(16) not null,
	version bigint not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	timestamp_sent datetime not null,
	status varchar(255) not null,
    request_id binary(16) not null,
    response_id binary(16),
	fault_received VARCHAR(255),
	PRIMARY KEY (id)
);

create table steuerdaten_response (
	id binary(16) not null,
	version bigint not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	zpv_nr_antragsteller bigint,
	geburtsdatum_antragsteller date,
	kibon_antrag_id VARCHAR(255),
	beginn_gesuchsperiode bigint,
	zpv_nr_dossiertraeger BIGINT,
	geburtsdatum_dossiertraeger date,
	zpv_nr_partner bigint,
	geburtsdatum_partner date,
	fall_id bigint,
	antwortdatum date,
	synchrone_antwort bool,
	veranlagungsstand varchar(255),
	unterjaehriger_fall bool,
	erwerbseinkommen_unselbstaendigkeit_dossiertraeger decimal(19,2),
	erwerbseinkommen_unselbstaendigkeit_partner decimal(19,2),
	steuerpflichtiges_ersatzeinkommen_dossiertraeger decimal(19,2),
	steuerpflichtiges_ersatzeinkommen_partner decimal(19,2),
	erhaltene_unterhaltsbeitraege_dossiertraeger decimal(19,2),
	erhaltene_unterhaltsbeitraege_partner decimal(19,2),
	ausgewiesener_geschaeftsertrag_dossiertraeger decimal(19,2),
	ausgewiesener_geschaeftsertrag_partner decimal(19,2),
	ausgewiesener_geschaeftsertrag_vorperiode_dossiertraeger decimal(19,2),
	ausgewiesener_geschaeftsertrag_vorperiode_partner decimal(19,2),
	ausgewiesener_geschaeftsertrag_vorperiode2dossiertraeger decimal(19,2),
	ausgewiesener_geschaeftsertrag_vorperiode2partner decimal(19,2),
	weitere_steuerbare_einkuenfte_dossiertraeger decimal(19,2),
	weitere_steuerbare_einkuenfte_partner decimal(19,2),
	bruttoertraege_aus_vermoegen_ohne_liegenschaften_und_ohne_egme decimal(19,2),
	bruttoertraege_aus_liegenschaften decimal(19,2),
	nettoertraege_aus_egme_dossiertraeger decimal(19,2),
	nettoertraege_aus_egme_partner decimal(19,2),
	geleistete_unterhaltsbeitraege decimal(19,2),
	schuldzinsen decimal(19,2),
	gewinnungskosten_bewegliches_vermoegen decimal(19,2),
	liegenschafts_abzuege decimal(19,2),
	nettovermoegen decimal(19,2),
	PRIMARY KEY (id)
);

create table steuerdaten_request (
	id binary(16) not null,
	version bigint not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	zpv_nummer bigint not null,
	geburtsdatum_antragsteller date,
	antrag_id varchar(255) not null,
	gesuchsperiode_beginn_jahr bigint not null,
	PRIMARY KEY (id)
);

alter table steuerdaten_anfrage_log
add constraint FK_steuerdaten_anfrage_log_request
	foreign key (request_id)
		references steuerdaten_request(id);

alter table steuerdaten_anfrage_log
add constraint FK_steuerdaten_anfrage_log_response
	foreign key (response_id)
		references steuerdaten_response(id);
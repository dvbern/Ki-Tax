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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
-- funktion speichert die gesuchsperiode id für eine gesuchsperiode gültig ab (input) in der übergebenen variable gp_id.
-- falls keine periode mit dem übergebenen gültig_ab datum existeirt wird eine neue uuid in die variable gespeichert
DELIMITER //
create or replace procedure select_gesuchsperiode(IN gueltig_ab_input date,IN mandant_id_input binary(16),OUT gp_id binary(16))
begin
    IF EXISTS(select id from gesuchsperiode where mandant_id = mandant_id_input and gueltig_ab = gueltig_ab_input)
    THEN set gp_id = (select id from gesuchsperiode where mandant_id = mandant_id_input and gueltig_ab = gueltig_ab_input);
    ELSE set gp_id = UNHEX(REPLACE(UUID(), '-', ''));
    END IF;
end;

//

DELIMITER ;


# Variables definition
SET @bern_mandant_id = UNHEX('E3736EB86EEF40EF9E5296AB48D8F220');
call select_gesuchsperiode('2019-08-01',@bern_mandant_id, @gesuchsperiode_19_20);
call select_gesuchsperiode('2022-08-01',@bern_mandant_id, @gesuchsperiode_22_23);
call select_gesuchsperiode('2023-08-01',@bern_mandant_id, @gesuchsperiode_23_24);
call select_gesuchsperiode('2024-08-01',@bern_mandant_id, @gesuchsperiode_24_25);

SET @gemeinde_london = UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', ''));
SET @gemeinde_paris = UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', ''));

# Application properties
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id =  @bern_mandant_id;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id =  @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'FRENCH_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'GERES_ENABLED_FOR_MANDANT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '2022-04-04' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'STADT_BERN_ASIV_CONFIGURED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'ALLE_MUTATIONSMELDUNGEN_VERFUEGEN, KIBON_2754, GESUCH_BEENDEN_FAMSIT, ZAHLUNGEN_STATISTIK, BEMERKUNGEN_FALLUEBERGREIFEND, MEHRERE_FACHSTELLENBESTAETIGUNGEN, FACHSTELLEN_UEBERGANGSLOESUNG' WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'logo-kibon-white-bern.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'logo-kibon-bern.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '01.01.2021' WHERE name = 'STADT_BERN_ASIV_START_DATUM' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '15' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#D50025' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf' WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '2020-07-31' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '2020-07-17' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '0.2' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '1' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '100000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '50000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @bern_mandant_id;

# Gesuchsperiode
UPDATE gesuchsperiode SET status = 'INAKTIV' WHERE ID = @gesuchsperiode_19_20;
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, verfuegung_erlaeuterungen_de, verfuegung_erlaeuterungen_fr, vorlage_merkblatt_ts_de, vorlage_merkblatt_ts_fr, vorlage_verfuegung_lats_de, vorlage_verfuegung_lats_fr, mandant_id, vorlage_verfuegung_ferienbetreuung_de, vorlage_verfuegung_ferienbetreuung_fr) VALUES (@gesuchsperiode_22_23, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2022-08-01', '2023-07-31', '2023-12-07', 'AKTIV', null, null, null, null, null, null, @bern_mandant_id, null, null);
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, verfuegung_erlaeuterungen_de, verfuegung_erlaeuterungen_fr, vorlage_merkblatt_ts_de, vorlage_merkblatt_ts_fr, vorlage_verfuegung_lats_de, vorlage_verfuegung_lats_fr, mandant_id, vorlage_verfuegung_ferienbetreuung_de, vorlage_verfuegung_ferienbetreuung_fr) VALUES (@gesuchsperiode_23_24, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2023-08-01', '2024-07-31', '2023-12-08', 'AKTIV', null, null, null, null, null, null, @bern_mandant_id, null, null);
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, verfuegung_erlaeuterungen_de, verfuegung_erlaeuterungen_fr, vorlage_merkblatt_ts_de, vorlage_merkblatt_ts_fr, vorlage_verfuegung_lats_de, vorlage_verfuegung_lats_fr, mandant_id, vorlage_verfuegung_ferienbetreuung_de, vorlage_verfuegung_ferienbetreuung_fr) VALUES (@gesuchsperiode_24_25, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2024-08-01', '2025-07-31', '2024-01-01', 'AKTIV', null, null, null, null, null, null, @bern_mandant_id, null, null);

# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system', '', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ec-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), now(), now(),'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), null, null);

# Antragstellende Benutzer fuer e2e erstellen
# geem
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('3DBDEFEB5E474A998E92DDEF02F45480'), now(), now(), 'anonymous', 'anonymous', 0, null, 'emma.gerber.be@mailbucket.dvbern.ch', null, 'Gerber', 'AKTIV', 'geem', 'Emma', @bern_mandant_id, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('7A3062C060AA4B26AFDD370C80167A7D'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('3DBDEFEB5E474A998E92DDEF02F45480'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('B7F462DE8A2F4C47A0D00A67B171ABE8'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'geem', null, null, null);
# bemi
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('BF619D36435A4D4AAB3DBDCDE72A099C'), now(), now(), 'anonymous', 'anonymous', 0, null, 'michael.berger.be@mailbucket.dvbern.ch', null, 'Berger', 'AKTIV', 'bemi', 'Michael', @bern_mandant_id, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('ABB3B9C45925455A8F7C14CA454FFD48'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('BF619D36435A4D4AAB3DBDCDE72A099C'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('980918D8DEDF42C98DF87FD457D636B3'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'bemi', null, null, null);
# muhe
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('46DD546DAFA111EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, 'heinrich.mueller.be@mailbucket.dvbern.ch', null, 'Mueller', 'AKTIV', 'muhe', 'Heinrich', @bern_mandant_id, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('14B98044AFCF11EEAC8F0242AC1A0002'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('46DD546DAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('938DFF96AFA111EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'muhe', null, null, null);
# ziha
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('CEB688EEAFA111EEA5AF00155D1D453D'), now(), now(),'anonymous', 'anonymous', 0, null, 'hans.zimmermann.be@mailbucket.dvbern.ch', null, 'Zimmermann', 'AKTIV', 'ziha', 'Hans', @bern_mandant_id, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('D55C0C44AFA111EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('CEB688EEAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('D55C0C44AFA111EEA5AF00155D1D453B'), now(), now(),'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'ziha', null, null, null);
# chje
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('36949E5EAFA211EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, 'jean.chambre.be@mailbucket.dvbern.ch', null, 'Chambre', 'AKTIV', 'chje', 'Jean', @bern_mandant_id, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('3BE07774AFA211EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('36949E5EAFA211EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('3EE78AF5AFA211EEA5AF00155D1D453D'), now(), now(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'chje', null, null, null);

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis, event_published)
SELECT @gemeinde_london, now(), now(), 'flyway', 'flyway', 0,
	   'London', max(gemeinde_nummer)+1, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99999,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31', false from gemeinde;
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum,
                      angebotbg, angebotts, angebotfi, gueltig_bis, event_published)
SELECT  @gemeinde_paris,now(), now(), 'flyway', 'flyway', 0,
		   'Paris', max(gemeinde_nummer)+1, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99998, '2016-01-01',
        '2020-08-01', '2020-08-01', true, false, false, '9999-12-31', false from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
																							 now(), now(), 'flyway', 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Paris', '21',
																							 'CH', 'Jugendamt', 'Paris', '3008', 'Effingerstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
                                                                                             now(), now(), 'flyway', 'flyway', 0, null, '2018-01-01', '9999-01-01', 'London', '1',
																							 'CH', 'Gemeinde', 'London', '3072', 'Schiessplatzweg', null);
INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bc', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bd', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);


INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
								 standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
								 VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),  @gemeinde_paris, UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'paris@mailbucket.dvbern.ch', '+41 31 321 61 11', 'https://www.bern.ch', null, 'DE_FR', 'BIC', 'CH93 0076 2011 6238 5295 7', 'Paris Kontoinhaber', true, true, true, true, true, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bc', '-', '')));
INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
								 standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
								 VALUES (UNHEX(REPLACE('4a7dc6e5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), @gemeinde_london, UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'london@mailbucket.dvbern.ch', '+41 31 930 14 14', 'https://www.ostermundigen.ch', null, 'DE', 'BIC', 'CH93 0076 2011 6238 5295 7', 'London Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bd', '-', '')));
# Einstellungen 22/23
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_22_23, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_19_20;

# Einstellungen
UPDATE einstellung set value = 'ABHAENGING' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ABWESENHEIT_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ANSPRUCH_AB_X_MONATEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ANSPRUCH_MONATSWEISE' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' and gemeinde_id is null;
UPDATE einstellung set value = '12' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'DAUER_BABYTARIF' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'EINGEWOEHNUNG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' and gemeinde_id is null;
UPDATE einstellung set value = 'BERN' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLEN_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '60' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' and gemeinde_id is null;
UPDATE einstellung set value = '60' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' and gemeinde_id is null;
UPDATE einstellung set value = 'BERN_FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FINANZIELLE_SITUATION_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '80000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' and gemeinde_id is null;
UPDATE einstellung set value = 'KLASSE9' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_TEXTE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' and gemeinde_id is null;
UPDATE einstellung set value = 'KINDERGARTEN2' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '51000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '6.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '70000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '3.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '10' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KITA_STUNDEN_PRO_TAG' and gemeinde_id is null;
UPDATE einstellung set value = '10.59' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_LOHNNORMKOSTEN' and gemeinde_id is null;
UPDATE einstellung set value = '5.30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gemeinde_id is null;
UPDATE einstellung set value = '2022-09-15' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_STICHTAG' and gemeinde_id is null;
UPDATE einstellung set value = '160000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '12.40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '6.20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '75' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '12.75' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '150' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '100' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '2' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MINIMALDAUER_KONKUBINAT' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '43000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '0.79' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_TARIF' and gemeinde_id is null;
UPDATE einstellung set value = '0.70' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '7' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '11' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '240' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSTAGE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '240' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSTAGE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' and gemeinde_id is null;
UPDATE einstellung set value = '30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' and gemeinde_id is null;
UPDATE einstellung set value = '3800' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' and gemeinde_id is null;
UPDATE einstellung set value = '6000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' and gemeinde_id is null;
UPDATE einstellung set value = '7000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' and gemeinde_id is null;
UPDATE einstellung set value = '7700' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_KITA_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' and gemeinde_id is null;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PENSUM_ANZEIGE_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZEMIS_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '4.25' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' and gemeinde_id is null;

# Gemeinde Einstellungen müssen inserted werden, da sie in der Periode 19/20 noch nicht gestzt sind und desshalb auch nicht kopiert werden
# Gemeinde Paris
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB', '04.05.2022', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_KONTINGENTIERUNG_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN', '51000', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT', '6.00', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN', '70000', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT', '3.00', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT', '0', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT', '2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT', '5', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT', '5', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB', '04.05.2022', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG', '15.08.2022', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT', '15', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA', '50', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO', '4.94', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA', '11', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO', '1', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;

# Gemeinde London (alle Einstellungen von Paris kopieren und dann für London updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, @gemeinde_london, @gesuchsperiode_22_23, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED';
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT';
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT';
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB';
UPDATE einstellung set value = '01.08.2022' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO';
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA';
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED';

# Einstellungen Periode 23/24 (Kopieren aus 22/23 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_23_24, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23;
UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV_2' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '10.72' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_LOHNNORMKOSTEN' and gemeinde_id is null;
UPDATE einstellung set value = '5.36' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gemeinde_id is null;
UPDATE einstellung set value = '2023-09-15' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_STICHTAG' and gemeinde_id is null;
UPDATE einstellung set value = '12.55' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '6.27' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '0.8' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MIN_TARIF' and gemeinde_id is null;
UPDATE einstellung set value = '0.7' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' and gemeinde_id is null;

UPDATE einstellung set value = '08.05.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '08.05.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '14.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_paris;

UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '01.08.2023' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_london;

# Einstellungen Periode 24/25 (Kopieren aus 23/24 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_24_25, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_23_24;
UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'SPRACHFOERDERUNG_BESTAETIGEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GESUCH_BEENDEN_BEI_TAUSCH_GS2' and gemeinde_id is null;

UPDATE einstellung set value = '08.05.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '08.05.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '14.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_paris;

UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '01.08.2024' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_london;

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active) VALUES (UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Bern', true);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Brünnen', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Tageseltern Bern', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Weissenstein', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Bern', 'Bern', '3005', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Bern', '3007', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Bern', '3027', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Bern', null);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, auslastung_institutionen, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), false, false, false, false, 30, null, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, false, false, false, false);
INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, auslastung_institutionen, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), false, false, false, false, 35, null, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, false, false, false, false);
INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, auslastung_institutionen, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), false, false, false, false, 40, null, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, false, false, false, false);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('6b7beb6e-6cf3-49d6-84c0-5818d9215ecd', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN', UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')),  null, null, UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), 'tagesfamilien@mailbucket.dvbern.ch', null, null);
INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('945e3eef-8f43-43d2-a684-4aa61089684b', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), null, null, UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), 'weissenstein@mailbucket.dvbern.ch', null, null);
INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('9a0eb656-b6b7-4613-8f55-4e0e4720455e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), null,  null, UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), 'bruennen@mailbucket.dvbern.ch', null, null);

INSERT IGNORE INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('c93fbba5-91e2-4fac-88a3-a2dc8386d62d', '-', '')), now(), now(), 'flyway', 'flyway', 0, ' Brünnen', 'Brünnen', 11.50, 240.00);
INSERT IGNORE INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('9a9cc8a2-32b9-4ad0-8f41-ed503f886100', '-', '')), now(), now(), 'flyway', 'flyway', 0, ' Weissenstein', 'Weissenstein', 11.50, 240.00);

# Tagesschule
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Tagesschule', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')),now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '21', 'CH', 'Tagesschule', 'Bern', '3008', 'Effingerstrasse', null);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('199ac4a1-448f-4d4c-b3a6-5aee21f89613', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESSCHULE', UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), null, null, null, 'tagesschule@mailbucket.dvbern.ch', null, null);

update gemeinde set angebotts = true, angebotfi = true, angebotbgtfo = true where bfs_nummer in (99999, 99998);


-- Tagesschule Gemeinde Paris
INSERT IGNORE INTO institution (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,name,status,mandant_id,traegerschaft_id,stammdaten_check_required,event_published)VALUES (UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b610', '-', '')),now(), now(),'flyway','flyway',0,null,'Tagesschule Paris','AKTIV', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')),null,false,true);
INSERT IGNORE INTO adresse (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,gemeinde,hausnummer,land,organisation,ort,plz,strasse,zusatzzeile) VALUES (UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1937', '-', '')),now(), now(),'flyway','flyway',0,null,'1000-01-01','9999-12-31',null,'2','CH','Tageschule Paris','Paris','3000','Pariser Strasse',null);
INSERT IGNORE INTO institution_stammdaten_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,gemeinde_id) VALUES (UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871', '-', '')),now(), now(),'flyway','flyway',0,UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')));
INSERT IGNORE INTO institution_stammdaten (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,betreuungsangebot_typ,mail,telefon,webseite,adresse_id,institution_id,institution_stammdaten_ferieninsel_id,institution_stammdaten_tagesschule_id,send_mail_wenn_offene_pendenzen,institution_stammdaten_betreuungsgutscheine_id) VALUES (UNHEX(REPLACE('0f1c6b9e-37de-4c10-8ddc-9514fb840f5e', '-', '')),now(), now(),'flyway','flyway',0,null,'2020-08-01','9999-12-31','TAGESSCHULE','test@mailbucket.dvbern.ch',null,null,UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1937', '-', '')),UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b610', '-', '')),null,UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871', '-', '')),true,null);
INSERT IGNORE INTO einstellungen_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,
                                       modul_tagesschule_typ,gesuchsperiode_id,institution_stammdaten_tagesschule_id,
                                       erlaeuterung)
SELECT *
FROM (SELECT UNHEX(REPLACE('3628c6de-1166-11ec-82a8-0242ac130003', '-', ''))    as id,
		  now()              as timestamp_erstellt,
		  now()              as timestamp_mutiert,
		  'flyway'                           as user_erstellt,
		  'flyway'                           as user_mutiert,
		  0                                  as version,
		  'DYNAMISCH' as  modul_tagesschule_typ,
		  gp.id   							 as gesuchsperiode_id,
		  UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871','-', '')) as institution_stammdaten_tagesschule_id,
		  null as erlaeuterung
	  from gesuchsperiode as gp where gp.mandant_id = UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-',''))) as tmp;

INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, modul_tagesschule_typ, gesuchsperiode_id, institution_stammdaten_tagesschule_id, erlaeuterung, tagi) VALUES (UNHEX('1D534A58A06147FEB93A6A52089DCAC9'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'DYNAMISCH', @gesuchsperiode_22_23, UNHEX('0F7639463A594AA696944754E58E8871'), null, false);
INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, modul_tagesschule_typ, gesuchsperiode_id, institution_stammdaten_tagesschule_id, erlaeuterung, tagi) VALUES (UNHEX('28178A34B066426B835DE94B482F7898'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'DYNAMISCH', @gesuchsperiode_23_24, UNHEX('0F7639463A594AA696944754E58E8871'), null, false);
INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, modul_tagesschule_typ, gesuchsperiode_id, institution_stammdaten_tagesschule_id, erlaeuterung, tagi) VALUES (UNHEX('37178A34B022426B835DE94B482F7841'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'DYNAMISCH', @gesuchsperiode_24_25, UNHEX('0F7639463A594AA696944754E58E8871'), null, false);

INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('0D627D78D27E4F6D8DDCBE45142BFDD4'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Nachmittag', 'Après-midi');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('88A2A155CA014787ACB69F11C16B4F0A'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Morgen', 'Matin');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('9FB84DCC8CF3405A92F3BE231BD6F5AD'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Nachmittag', 'Après-midi');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('CDAFD28831614EF293622CE92E3DDAEF'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Morgen', 'Matin');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('6CB84DCC8CF3405A92F3BE231BD6F5AD'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Nachmittag', 'Après-midi');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch) VALUES (UNHEX('BCAFD28831614EF293622CE92E3DDAEF'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Morgen', 'Matin');

INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'coY1r4cqrK1Aq1HIcgvHwGe9pJnGzffZoZO0', 'WOECHENTLICH', 'DYNAMISCH', 0, 2.00, true, '17:00:00', '13:00:00', UNHEX('28178A34B066426B835DE94B482F7898'), UNHEX('9FB84DCC8CF3405A92F3BE231BD6F5AD'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('88EDBED7B32E458A953C246EF7534D2A'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'lNd5s2wJLEGNAyVgEJOyhGPfjhJoEaDCp4Pp', 'WOECHENTLICH', 'DYNAMISCH', 0, 2.00, true, '17:00:00', '13:00:00', UNHEX('1D534A58A06147FEB93A6A52089DCAC9'), UNHEX('0D627D78D27E4F6D8DDCBE45142BFDD4'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('B19E121548304BE2BB76E13FF36E3DD7'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'F8vXs39fkEuXdvi3kjLv5DZn5m4fcVpm27tw', 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00', '08:00:00', UNHEX('28178A34B066426B835DE94B482F7898'), UNHEX('CDAFD28831614EF293622CE92E3DDAEF'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('BDB3153E0C274EDD88388EAC1A50855F'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'Hxttr08V9uANnNSmNnqVXZwcXjg8qWdoSnoK', 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00', '08:00:00', UNHEX('1D534A58A06147FEB93A6A52089DCAC9'), UNHEX('88A2A155CA014787ACB69F11C16B4F0A'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('C19E121548304CE2BB76E13FF36E3CC4'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'C8vXs39fkEuXdvi3kjLv5DZn5m4fcVpm36ww', 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00', '08:00:00', UNHEX('37178A34B022426B835DE94B482F7841'), UNHEX('BCAFD28831614EF293622CE92E3DDAEF'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id) VALUES (UNHEX('CDB3153E0C274EDC88388EAC1A50854C'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'Ereer08V9uANnNSmNnqVXZwcXjg8qWdoKnos', 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00', '08:00:00', UNHEX('37178A34B022426B835DE94B482F7841'), UNHEX('6CB84DCC8CF3405A92F3BE231BD6F5AD'), null);

INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('0EA3EDEED54F4C3A88D1DFADF3617F5D'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('117B588BA62C458283D6FAB4728E1EC5'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('BDB3153E0C274EDD88388EAC1A50855F'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('19510B703914458C88149A5368C6FA0E'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('BDB3153E0C274EDD88388EAC1A50855F'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('2B6C46F53E7949FD8E81B8D6EA177A23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('38126E85CA9F4853865C60947A53D229'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('B19E121548304BE2BB76E13FF36E3DD7'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('457636509C7547A783D0618E44065AAB'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('88EDBED7B32E458A953C246EF7534D2A'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('587247B821C643808186A74636FFD47A'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('75C07D1B3325477ABB33778B8EAB0D7A'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('B19E121548304BE2BB76E13FF36E3DD7'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('7FCD91E461E64C018AB3A58B39A1ED22'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('B19E121548304BE2BB76E13FF36E3DD7'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('96615E4146834B1AADEE771EBBDE345C'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('9A8933264002438E8ED72FEE0E77ED79'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('88EDBED7B32E458A953C246EF7534D2A'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('9B9DCAEB7E3345D193B0E9921033DB5C'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('BDB3153E0C274EDD88388EAC1A50855F'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('9D4FE03C3B1644859A38D0D594F9B6DD'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('88EDBED7B32E458A953C246EF7534D2A'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('AAD177CFCD7A4EB1821C223ABADE96F2'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('B19E121548304BE2BB76E13FF36E3DD7'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('B67779C653C7403B9C2586FF3546FF63'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('B19E121548304BE2BB76E13FF36E3DD7'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('BCE19C6EBF1843148707258A3A4C192D'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('BDB3153E0C274EDD88388EAC1A50855F'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('C1B54945167B4D849E2640E46E141803'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('88EDBED7B32E458A953C246EF7534D2A'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('DE7FE9D920D54D95BFD3A67B24DC69FD'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('3FAB843B518D4DA3826ECA99269DA2A2'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('E06F47EFEACD4E8D8E07C84928420BD6'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('88EDBED7B32E458A953C246EF7534D2A'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('E7F9560D8A43446A8BC53CEDF1263F18'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('BDB3153E0C274EDD88388EAC1A50855F'));
# Module Tagesschule 24/25 Morgen / Nachmittag
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('ED71EB98E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('C19E121548304CE2BB76E13FF36E3CC4'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('F646CB45E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('C19E121548304CE2BB76E13FF36E3CC4'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('F866B614E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('C19E121548304CE2BB76E13FF36E3CC4'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('FA9C34A1E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('C19E121548304CE2BB76E13FF36E3CC4'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('FCEEA0B5E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('C19E121548304CE2BB76E13FF36E3CC4'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('FED29368E5C111EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', UNHEX('CDB3153E0C274EDC88388EAC1A50854C'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('00DB1FCDE5C211EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', UNHEX('CDB3153E0C274EDC88388EAC1A50854C'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('0319EB9DE5C211EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', UNHEX('CDB3153E0C274EDC88388EAC1A50854C'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('052C3542E5C211EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', UNHEX('CDB3153E0C274EDC88388EAC1A50854C'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX('07A39A99E5C211EE89BE005056BD1F23'), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', UNHEX('CDB3153E0C274EDC88388EAC1A50854C'));

INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde_id, gesuchsperiode_id, merkblatt_anmeldung_tagesschule_de, merkblatt_anmeldung_tagesschule_fr) VALUES
# PARIS
(UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null),
# LONDON
(UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, @gemeinde_london, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null);

# PARIS
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('54086b1a-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('4ea68aa1-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9c19b314-6900-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('36665051-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')));
# LONDON
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('a3e774d0-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9ea7ae08-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('90cb89be-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9989a3f8-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')));

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,name,status,mandant_id) VALUES (UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b620', '-', '')),now(), now(),'flyway','flyway',0,null,'BernerSozialdienst','AKTIV', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));
INSERT IGNORE INTO adresse (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,gemeinde,hausnummer,land,organisation,ort,plz,strasse,zusatzzeile) VALUES (UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1999', '-', '')),now(), now(),'flyway','flyway',0,null,'1000-01-01','9999-12-31',null,'2','CH','Bern Sozialdienst','Paris','3000','Sozialdienst Strasse',null);
INSERT IGNORE INTO sozialdienst_stammdaten (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,mail,telefon,webseite,adresse_id,sozialdienst_id) VALUES (UNHEX(REPLACE('0f1c6b9e-37de-4c10-8ddc-9514fb840f5f', '-', '')),now(), now(),'flyway','flyway',0,null,'test@mailbucket.dvbern.ch','078 898 98 98','http://test.dvbern.ch',UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1999', '-', '')),UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b620', '-', '')));

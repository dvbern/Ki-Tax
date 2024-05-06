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

START TRANSACTION;

# Variables definition
SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
call select_gesuchsperiode('2022-08-01', @mandant_id_luzern, @gesuchsperiode_22_23_lu_id);
call select_gesuchsperiode('2023-08-01', @mandant_id_luzern, @gesuchsperiode_23_24_lu_id);
SET @luzern_test_gemeinde_id = UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', ''));
SET @system_user = UNHEX(REPLACE('55555555-5555-5555-5555-555555555555', '-', ''));

# Adresse
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('3073FF81BD9640168B92F7483B79BC0E'), '2023-12-08 10:58:36', '2023-12-08 10:59:59', 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 1, null, '2023-12-08', '9999-12-31', null, '2', 'CH', 'Kita Luzern', 'Luzern', '6000', 'Kita Strasse', null, null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('5DBE5D0E8C4D43AB9FC1C44F4B259784'), '2023-12-08 10:56:11', '2023-12-08 10:56:11', 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, null, '2023-12-08', '9999-12-31', null, null, 'CH', 'Stadt Luzern Kinder Jugend Familie', 'Luzern 7', '6000', 'Kasernenplatz 3, Postfach', null, null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('C153D59824D14650970109F3CD5E1AD4'), '2023-12-08 10:56:11', '2023-12-08 10:56:11', 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, null, '1000-01-01', '9999-12-31', null, null, 'CH', 'Stadt Luzern Frühkindliche Bildung und Betreuung', 'Luzern 7', '6000', 'Kasernenplatz 3', null, null);

# application property
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id =  @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'FRENCH_ENABLED' and mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id =  @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'GERES_ENABLED_FOR_MANDANT' and mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '2022-03-29' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' and mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '' WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'STADT_BERN_ASIV_CONFIGURED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'logo-kibon-white-luzern.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET  value = '60' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'logo-kibon-luzern.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '01.01.2021' WHERE name = 'STADT_BERN_ASIV_START_DATUM' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '14' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '10' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '#0072bd' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '#00466f' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf' WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '#C6C6C6' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '2020-07-31' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '2020-07-17' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '0.2' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '1' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '100000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = '50000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @mandant_id_luzern;
UPDATE application_property SET value = 'true' WHERE name = 'ABWEICHUNGEN_ENABLED' AND mandant_id = @mandant_id_luzern;

# Gesuchsperiode
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_22_23_lu_id;
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, verfuegung_erlaeuterungen_de, verfuegung_erlaeuterungen_fr, vorlage_merkblatt_ts_de, vorlage_merkblatt_ts_fr, vorlage_verfuegung_lats_de, vorlage_verfuegung_lats_fr, mandant_id, vorlage_verfuegung_ferienbetreuung_de, vorlage_verfuegung_ferienbetreuung_fr) VALUES (@gesuchsperiode_23_24_lu_id, now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, null, '2023-08-01', '2024-07-31', '2023-12-08', 'AKTIV', null, null, null, null, null, null, @mandant_id_luzern, null, null);

# Antragstellende Benutzer fuer e2e erstellen
# geem
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('5DBDEFEB5E474A998E92DDEF02F45480'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, 'emma.gerber.stadtluzern@mailbucket.dvbern.ch', null, 'Gerber', 'AKTIV', 'geem', 'Emma', @mandant_id_luzern, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('9A3062C060AA4B26AFDD370C80167A7D'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('5DBDEFEB5E474A998E92DDEF02F45480'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('D7F462DE8A2F4C47A0D00A67B171ABE8'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'geem', null, null, null);
# bemi
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('DF619D36435A4D4AAB3DBDCDE72A099C'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'michael.berger.stadtluzern@mailbucket.dvbern.ch', null, 'Berger', 'AKTIV', 'bemi', 'Michael', @mandant_id_luzern, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('CBB3B9C45925455A8F7C14CA454FFD48'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('DF619D36435A4D4AAB3DBDCDE72A099C'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('B80918D8DEDF42C98DF87FD457D636B3'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'bemi', null, null, null);
# muhe
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('66DD546DAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'heinrich.mueller.stadtluzern@mailbucket.dvbern.ch', null, 'Mueller', 'AKTIV', 'muhe', 'Heinrich', @mandant_id_luzern, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('34B98044AFCF11EEAC8F0242AC1A0002'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('66DD546DAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('B38DFF96AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'muhe', null, null, null);
# ziha
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('EEB688EEAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'hans.zimmermann.stadtluzern@mailbucket.dvbern.ch', null, 'Zimmermann', 'AKTIV', 'ziha', 'Hans', @mandant_id_luzern, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('F55C0C44AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('EEB688EEAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('F55C0C44AFA111EEA5AF00155D1D453B'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'ziha', null, null, null);
# chje
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('56949E5EAFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'jean.chambre.stadtluzern@mailbucket.dvbern.ch', null, 'Chambre', 'AKTIV', 'chje', 'Jean', @mandant_id_luzern, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('5BE07774AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('56949E5EAFA211EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('5EE78AF5AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'chje', null, null, null);

# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (@system_user, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system_lu', '', @mandant_id_luzern, null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('ebdbc4d5-cf80-11ee-8608-0242ac160002', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', @system_user, null, null);

# Gemeinden Testgemeinde Luzern erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, angebotbgtfo, gueltig_bis, infoma_zahlungen)
SELECT @luzern_test_gemeinde_id, '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
	   'Testgemeinde Luzern', max(gemeinde_nummer)+1,  @mandant_id_luzern, 'AKTIV', 99997,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, true, '9999-12-31', true from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('2476287e-3264-11ec-a17e-b89a2ae4a038', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Luzern', '1',
																							 'CH', 'Gemeinde', 'Luzern', '3072', 'Schiessplatzweg', null);
INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0be', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE('fd91477c-3263-11ec-a17e-b89a2ae4a038', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0,
		@system_user, @system_user,
        UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')), UNHEX(REPLACE('2476287e-3264-11ec-a17e-b89a2ae4a038', '-', '')),
        'luzern@mailbucket.dvbern.ch', '+41 31 930 14 14', 'https://www.luzern.ch', null, 'DE', 'BIC', 'CH93 0077 2011 6238 5295 7',
        'Luzern Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0be', '-', '')));

# Gesuchsperiode 22/23 Einstellungen
UPDATE einstellung set value = 'ABHAENGING' WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ABWESENHEIT_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ANSPRUCH_MONATSWEISE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'ASIV' WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '18' WHERE einstellung_key = 'DAUER_BABYTARIF' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'LUZERN' WHERE einstellung_key = 'EINGEWOEHNUNG_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'LUZERN' WHERE einstellung_key = 'FACHSTELLEN_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'LUZERN' WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'null' WHERE einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'OBLIGATORISCHER_KINDERGARTEN' WHERE einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_TEXTE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'FREIWILLIGER_KINDERGARTEN' WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'KINDERABZUG_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'KITA_STUNDEN_PRO_TAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '125000' WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '12.24' WHERE einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '6.11' WHERE einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '12.4' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '130' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '8.50' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '16.3' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '160' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '12.4' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '130' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '2' WHERE einstellung_key = 'MINIMALDAUER_KONKUBINAT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '48000' WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.78' WHERE einstellung_key = 'MIN_TARIF' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0.70' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '11' WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '246' WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '246' WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '25' WHERE einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_KITA_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'NUR_PROZENT' WHERE einstellung_key = 'PENSUM_ANZEIGE_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'OBLIGATORISCHER_KINDERGARTEN' WHERE einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ZEMIS_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '4.25' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;
UPDATE einstellung set value = '50' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_lu_id and gemeinde_id is null;

# Gemeinde Einstellungen müssen inserted werden, da sie beim Erstellen der Periode 22/23 (flyway-script) noch nicht inserted wurden
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM', 'ABHAENGING', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'ERWERBSPENSUM_ZUSCHLAG', '10', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE', 'KINDERGARTEN1', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB', '01.08.2019', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_KONTINGENTIERUNG_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN', '51000', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT', '6.00', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN', '70000', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT', '3.00', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT', '0.00', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT', '20', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT', '20', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB', '01.08.2019', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG', '01.08.2019', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO', '0', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA', 'VORSCHULALTER', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO', 'VORSCHULALTER', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED', 'false', @luzern_test_gemeinde_id, @gesuchsperiode_22_23_lu_id, @mandant_id_luzern, null;

# Einstellungen Periode 23/24 (Kopieren aus 22/23 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Stadt Luzern', 'ebegu:Stadt Luzern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_23_24_lu_id, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23_lu_id;

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mandant_id)
	VALUES (UNHEX(REPLACE('31bf2433-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Luzern', true,  @mandant_id_luzern);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (UNHEX(REPLACE('f5ceae4a-30a5-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen LU',
	         @mandant_id_luzern, UNHEX(REPLACE('31bf2433-30a3-11ec-a86f-b89a2ae4a038', '-', '')), 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (UNHEX(REPLACE('6a06f59b-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Luzern',
	         @mandant_id_luzern, null, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (UNHEX(REPLACE('7c436811-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein LU',
	         @mandant_id_luzern, UNHEX(REPLACE('31bf2433-30a3-11ec-a86f-b89a2ae4a038', '-', '')), 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('abd8ec9b-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Luzern', 'Luzern', '6000', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('bda4670c-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Luzern', '6000', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('c41ab591-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Luzern', '6000', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, infoma_kreditorennummer, infoma_bankcode)
	VALUES (UNHEX(REPLACE('ad4c1134-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen LU', null, 'BRU000', 'BRU0001');
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, infoma_kreditorennummer, infoma_bankcode)
	VALUES (UNHEX(REPLACE('b4625718-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein LU', null, 'WEISS000', 'WEISS0001');
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, infoma_kreditorennummer, infoma_bankcode)
	VALUES (UNHEX(REPLACE('be37b235-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Luzern', null, 'TAG000', 'TAG0001');

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('e85adc3b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('be37b235-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 30, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('eebf7efd-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('b4625718-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 35, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('f482ce4b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('ad4c1134-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('16075d77-30a5-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN',
		UNHEX(REPLACE('abd8ec9b-30a3-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('6a06f59b-30a3-11ec-a86f-b89a2ae4a038', '-', '')), NULL, NULL,
		UNHEX(REPLACE('e85adc3b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), 'tagesfamilien-lu@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('97882a4e-3261-11ec-a17e-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('bda4670c-30a3-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('7c436811-30a3-11ec-a86f-b89a2ae4a038', '-', '')), NULL, NULL,
		UNHEX(REPLACE('eebf7efd-30a4-11ec-a86f-b89a2ae4a038', '-', '')), 'weissenstein-lu@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('6d6afdb2-3261-11ec-a17e-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('c41ab591-30a3-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('f5ceae4a-30a5-11ec-a86f-b89a2ae4a038', '-', '')), NULL, NULL,
		UNHEX(REPLACE('f482ce4b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), 'bruennen-lu@mailbucket.dvbern.ch', NULL, NULL);

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								 vorgaenger_id, name, status, mandant_id)
VALUES (UNHEX(REPLACE('7049ec48-30ab-11ec-a86f-b89a2ae4a038', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 0, NULL, 'LuzernerSozialdienst', 'AKTIV',
		 @mandant_id_luzern);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('a0b91196-30ab-11ec-a86f-b89a2ae4a038', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Luzern Sozialdienst', 'Luzern', '6000',
		'Sozialdienst Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											version, vorgaenger_id, mail, telefon, webseite, adresse_id,
											sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2021-02-15 09:48:18', '2021-02-15 09:48:18',
		'flyway', 'flyway', 0, NULL, 'test-lu@mailbucket.dvbern.ch', '078 898 98 98', 'http://test.dvbern.ch',
		UNHEX(REPLACE('a0b91196-30ab-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('7049ec48-30ab-11ec-a86f-b89a2ae4a038', '-', '')));

UPDATE mandant SET mandant.activated=true where id = @mandant_id_luzern;

COMMIT;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));
call select_gesuchsperiode('2022-08-01', @mandant_id_solothurn, @gesuchsperiode_22_23_id);
call select_gesuchsperiode('2023-08-01', @mandant_id_solothurn, @gesuchsperiode_23_24_id);
SET @testgemeinde_solothurn_id = UNHEX(REPLACE('47c4b3a8-5379-11ec-98e8-f4390979fa3e', '-', ''));
SET @testgemeinde_grenchen_id = UNHEX(REPLACE('47c4b3a8-5371-11ec-98e8-f4390979fa3e', '-', ''));
SET @traegerschaft_solothurn_id = UNHEX(REPLACE('5c537fd1-537b-11ec-98e8-f4390979fa3e', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('78051383-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('7ce411e7-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @tfo_id = UNHEX(REPLACE('8284b8e2-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @system_user = UNHEX(REPLACE('44444444-4444-4444-4444-444444444444', '-', ''));

# APPLICATION PROPERTIES
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'FRENCH_ENABLED' and mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'GERES_ENABLED_FOR_MANDANT' and mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '2022-03-29' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' and mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'VERAENDERUNG_BEI_MUTATION' WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'true' WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'true' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'true' WHERE name = 'STADT_BERN_ASIV_CONFIGURED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'logo-kibon-white-solothurn.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'logo-kibon-solothurn.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '01.01.2021' WHERE name = 'STADT_BERN_ASIV_START_DATUM' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '15' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '#D50025' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf' WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '2020-07-31' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '2020-07-17' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '0.2' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '1' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR'AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '100000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = '50000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @mandant_id_solothurn;

# Gesuchsperiode
# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_22_23_id;
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, verfuegung_erlaeuterungen_de, verfuegung_erlaeuterungen_fr, vorlage_merkblatt_ts_de, vorlage_merkblatt_ts_fr, vorlage_verfuegung_lats_de, vorlage_verfuegung_lats_fr, mandant_id, vorlage_verfuegung_ferienbetreuung_de, vorlage_verfuegung_ferienbetreuung_fr) VALUES (@gesuchsperiode_23_24_id, now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 3, null, '2023-08-01', '2024-07-31', '2023-12-08', 'AKTIV', null, null, null, null, null, null, @mandant_id_solothurn, null, null);

# Antragstellende Benutzer fuer e2e erstellen
# geem
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('6DBDEFEB5E474A998E92DDEF02F45480'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, 'emma.gerber.so@mailbucket.dvbern.ch', null, 'Gerber', 'AKTIV', 'geem', 'Emma', @mandant_id_solothurn, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('AA3062C060AA4B26AFDD370C80167A7D'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('6DBDEFEB5E474A998E92DDEF02F45480'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('E7F462DE8A2F4C47A0D00A67B171ABE8'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'geem', null, null, null);
# bemi
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('EF619D36435A4D4AAB3DBDCDE72A099C'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'michael.berger.so@mailbucket.dvbern.ch', null, 'Berger', 'AKTIV', 'bemi', 'Michael', @mandant_id_solothurn, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('DBB3B9C45925455A8F7C14CA454FFD48'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('EF619D36435A4D4AAB3DBDCDE72A099C'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('C80918D8DEDF42C98DF87FD457D636B3'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'bemi', null, null, null);
# muhe
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('76DD546DAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'heinrich.mueller.so@mailbucket.dvbern.ch', null, 'Mueller', 'AKTIV', 'muhe', 'Heinrich', @mandant_id_solothurn, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('44B98044AFCF11EEAC8F0242AC1A0002'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('76DD546DAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('C38DFF96AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'muhe', null, null, null);
# ziha
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('FEB688EEAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'hans.zimmermann.so@mailbucket.dvbern.ch', null, 'Zimmermann', 'AKTIV', 'ziha', 'Hans', @mandant_id_solothurn, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('055C0C44AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('FEB688EEAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('055C0C44AFA111EEA5AF00155D1D453B'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'ziha', null, null, null);
# chje
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('66949E5EAFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'jean.chambre.so@mailbucket.dvbern.ch', null, 'Chambre', 'AKTIV', 'chje', 'Jean', @mandant_id_solothurn, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('6BE07774AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('66949E5EAFA211EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('6EE78AF5AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'chje', null, null, null);
# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (@system_user, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system_so', '', @mandant_id_solothurn, null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ed-4af0-11e9-9b2c-afd41a03c0bb', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', @system_user, null, null);

# Test Gemeinden Solothurn und Grenchen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis)
SELECT @testgemeinde_solothurn_id, '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
	   'Testgemeinde Solothurn', max(gemeinde_nummer)+1, @mandant_id_solothurn, 'AKTIV', 99996,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31' from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fa3e', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Solothurn', '1',
																							 'CH', 'Gemeinde', 'Solothurn', '4500', 'Berfüssergasse', null);
INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bf', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE('b5171d87-537a-11ec-98e8-f4390979fa3e', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0,
        @system_user, @system_user,
        @testgemeinde_solothurn_id, UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fa3e', '-', '')),
        'solothurn@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.solothurn.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
        'Solothurn Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bf', '-', '')));

INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
	angebotts, angebotfi, gueltig_bis)
SELECT @testgemeinde_grenchen_id, '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
	'Testgemeinde Grenchen', max(gemeinde_nummer)+1, @mandant_id_solothurn, 'AKTIV', 99994,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31' from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
							hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fb3e', '-', '')),
																									'2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																									'flyway', 0, null, '2018-01-01', '9999-01-01', 'Grenchen', '1',
																									'CH', 'Gemeinde', 'Grenchen', '2540', 'Testergasse', null);
INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c6bg', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE('b5171d87-537a-11ec-98e8-f4390979fa6e', '-', '')), '2021-10-23 00:00:00', '2021-10-23 00:00:00', 'flyway', 'flyway', 0,
		@system_user, @system_user,
		@testgemeinde_grenchen_id, UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fb3e', '-', '')),
		'grenchen@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.grenchen.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
		'Grenchen Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c6bg', '-', '')));

# Gesuchsperiode 22/23 Einstellungen:
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'KINDERABZUG_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '18' WHERE einstellung_key = 'DAUER_BABYTARIF' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_TEXTE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ZEMIS_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '12.24' WHERE einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '6.11' WHERE einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.78' WHERE einstellung_key = 'MIN_TARIF' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_KITA_MIN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KINDERGARTEN2' WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '140' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '95' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '70' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '14' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '9.5' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '160000' WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '40000' WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '6' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '3' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '25' WHERE einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' WHERE einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '10.55' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '5.28' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '2021-09-15' WHERE einstellung_key = 'LATS_STICHTAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'EINGEWOEHNUNG_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'null' WHERE einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ANSPRUCH_MONATSWEISE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '2' WHERE einstellung_key = 'MINIMALDAUER_KONKUBINAT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'SOLOTHURN' WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'UNABHAENGING' WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KLASSE9' WHERE einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'BERN' WHERE einstellung_key = 'FACHSTELLEN_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' WHERE einstellung_key = 'PENSUM_ANZEIGE_TYP' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ABWESENHEIT_AKTIV' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'KITA_STUNDEN_PRO_TAG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '6.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '51000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '3.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '70000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' AND gesuchsperiode_id = @gesuchsperiode_22_23_id AND gemeinde_id is null;

# Gemeinde Einstellungen müssen inserted werden, da sie beim Erstellen der Periode 22/23 (flyway-script) noch nicht inserted wurden
# Gemeinde Solothurn
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ERWERBSPENSUM_ZUSCHLAG', '20', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE', 'KINDERGARTEN2', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB', '01.08.2022', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_KONTINGENTIERUNG_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN', '51000', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT', '6.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN', '70000', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT', '3.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT', '0', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT', '0.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT', '40', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT', '20', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB', '01.08.2022', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG', '01.08.2022', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT', '0', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA', '0.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO', '0.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA', '0.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO', '0.00', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO', 'VORSCHULALTER', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED', 'false', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_MASSGEBENDES_EINKOMMEN', '160000', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD', '7', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG', '70', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD', '7', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD', '14', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG', '140', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD', '9.5', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG', '95', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_MASSGEBENDES_EINKOMMEN', '40000', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_VERGUENSTIGUNG_PRO_STD', '3', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_VERGUENSTIGUNG_PRO_TG', '30', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ZUSCHLAG_BEHINDERUNG_PRO_STD', '6', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ZUSCHLAG_BEHINDERUNG_PRO_TG', '60', @testgemeinde_solothurn_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;

# Gemeinde Grenchen
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM', 'MINIMUM', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ERWERBSPENSUM_ZUSCHLAG', '20', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE', 'KINDERGARTEN2', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB', '01.08.2022', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER', 'true', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_KONTINGENTIERUNG_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN', '51000', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT', '6.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN', '70000', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT', '3.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT', '0', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT', '0.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT', '40', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT', '20', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT', 'true', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA', '8', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO', '0.8', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE', '160000', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB', '01.08.2022', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG', '01.08.2022', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT', '0', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA', '0.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO', '0.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA', '0.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO', '0.00', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO', 'VORSCHULALTER', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED', 'false', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_MASSGEBENDES_EINKOMMEN', '200000', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD', '9.5', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG', '95', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD', '7', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD', '14', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG', '140', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD', '9.5', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG', '95', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_MASSGEBENDES_EINKOMMEN', '40000', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_VERGUENSTIGUNG_PRO_STD', '3', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'MIN_VERGUENSTIGUNG_PRO_TG', '30', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ZUSCHLAG_BEHINDERUNG_PRO_STD', '6', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung) SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, 'ZUSCHLAG_BEHINDERUNG_PRO_TG', '60', @testgemeinde_grenchen_id, @gesuchsperiode_22_23_id, @mandant_id_solothurn, null;

# Gesuchsperiode 23/24 Einstellungen
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Solothurn', 'ebegu:Kanton Solothurn', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_23_24_id, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23_id;

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mandant_id)
	VALUES (@traegerschaft_solothurn_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Solothurn', true, @mandant_id_solothurn);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@bruennen_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen SO',
	        @mandant_id_solothurn, @traegerschaft_solothurn_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@tfo_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Solothurn',
	        @mandant_id_solothurn, null, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@weissenstein_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein SO',
	        @mandant_id_solothurn, @traegerschaft_solothurn_id, 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('bb7d074c-537e-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Solothurn', 'Solothurn', '4500', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('c2ea1156-537e-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein Solothurn', 'Solothurn', '4500', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('ca3e50e6-537e-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen Solothurn', 'Solothurn', '4500', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('fca0bc52-537e-11ec-98e8-f4390979fa3e', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen SO', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('04176a28-537f-11ec-98e8-f4390979fa3e', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein SO', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('0b1dc282-537f-11ec-98e8-f4390979fa3e', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Solothurn', null);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('2d3f850d-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('0b1dc282-537f-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('365cbb27-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('04176a28-537f-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('3f194c4f-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('fca0bc52-537e-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('50518a55-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN',
		UNHEX(REPLACE('bb7d074c-537e-11ec-98e8-f4390979fa3e', '-', '')),
		@tfo_id, NULL, NULL,
		UNHEX(REPLACE('2d3f850d-537f-11ec-98e8-f4390979fa3e', '-', '')), 'tagesfamilien-so@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('58b84479-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('c2ea1156-537e-11ec-98e8-f4390979fa3e', '-', '')),
		@weissenstein_id, NULL, NULL,
		UNHEX(REPLACE('365cbb27-537f-11ec-98e8-f4390979fa3e', '-', '')), 'weissenstein-so@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('6aa08c20-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('ca3e50e6-537e-11ec-98e8-f4390979fa3e', '-', '')),
		@bruennen_id, NULL, NULL,
		UNHEX(REPLACE('3f194c4f-537f-11ec-98e8-f4390979fa3e', '-', '')), 'bruennen-so@mailbucket.dvbern.ch', NULL, NULL);

update gemeinde set angebotts = false, angebotfi = false, angebotbgtfo = false where bfs_nummer = 99996;

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								 vorgaenger_id, name, status, mandant_id)
VALUES (UNHEX(REPLACE('1b1b4208-5394-11ec-98e8-f4390979fa3e', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 0, NULL, 'Solothurner Sozialdienst', 'AKTIV',
		@mandant_id_solothurn);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('a0b91196-30ab-11ec-a86f-b89a2ae4a038', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Solothurn Sozialdienst', 'Solothurn', '4500',
		'Sozialdienst Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											version, vorgaenger_id, mail, telefon, webseite, adresse_id,
											sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2021-02-15 09:48:18', '2021-02-15 09:48:18',
		'flyway', 'flyway', 0, NULL, 'sozialdienst-so@mailbucket.dvbern.ch', '078 898 98 98', 'http://sodialdienst-so.dvbern.ch',
		UNHEX(REPLACE('a0b91196-30ab-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('1b1b4208-5394-11ec-98e8-f4390979fa3e', '-', '')));

-- Einstellungen
UPDATE einstellung SET VALUE='true' WHERE einstellung_key='GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' and einstellung.mandant_id = @mandant_id_solothurn;

UPDATE mandant SET mandant.activated=true where id = @mandant_id_solothurn;

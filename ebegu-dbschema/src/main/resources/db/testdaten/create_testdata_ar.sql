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
SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));
call select_gesuchsperiode('2022-08-01', @mandant_id_ar, @gesuchsperiode_22_23_id);
call select_gesuchsperiode('2023-08-01', @mandant_id_ar, @gesuchsperiode_23_24_id);

SET @testgemeinde_ar_id = UNHEX(REPLACE('b3e44f85-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @testgemeinde_ar_bfs_nr = 99995;
SET @traegerschaft_id = UNHEX(REPLACE('c256ebf1-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('caa83a6b-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('d0bb7d2a-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @tfo_id = UNHEX(REPLACE('d6c10415-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @ts_id = UNHEX(REPLACE('5c136a35-39a9-11ed-a63d-b05cda43de9c', '-', ''));
SET @system_user = UNHEX(REPLACE('66666666-6666-6666-6666-666666666666', '-', ''));

# APPLICATION PROPERTIES
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'true' WHERE name = 'GERES_ENABLED_FOR_MANDANT' and mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'FRENCH_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '2022-03-29' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '' WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'true' WHERE name = 'STADT_BERN_ASIV_CONFIGURED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'logo-kibon-white-ar.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'logo-kibon-ar.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '01.01.2021' WHERE name = 'STADT_BERN_ASIV_START_DATUM' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '15' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '#D50025' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf' WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '2020-07-31' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '2020-07-17' WHERE name = 'NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '0.5' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '0.5' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '100' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '100' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @mandant_id_ar;

UPDATE mandant SET activated = TRUE WHERE id = @mandant_id_ar;

UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_22_23_id;
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_23_24_id;

# Antragstellende Benutzer fuer e2e erstellen
# geem
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('4DBDEFEB5E474A998E92DDEF02F45480'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, 'emma.gerber.ar@mailbucket.dvbern.ch', null, 'Gerber', 'AKTIV', 'geem', 'Emma', @mandant_id_ar, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('8A3062C060AA4B26AFDD370C80167A7D'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('4DBDEFEB5E474A998E92DDEF02F45480'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('C7F462DE8A2F4C47A0D00A67B171ABE8'), '2024-01-09 15:08:10', '2024-01-09 15:08:10', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'geem', null, null, null);
# bemi
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('CF619D36435A4D4AAB3DBDCDE72A099C'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'michael.berger.ar@mailbucket.dvbern.ch', null, 'Berger', 'AKTIV', 'bemi', 'Michael', @mandant_id_ar, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('BBB3B9C45925455A8F7C14CA454FFD48'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('CF619D36435A4D4AAB3DBDCDE72A099C'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('A80918D8DEDF42C98DF87FD457D636B3'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'bemi', null, null, null);
# muhe
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('56DD546DAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'heinrich.mueller.ar@mailbucket.dvbern.ch', null, 'Mueller', 'AKTIV', 'muhe', 'Heinrich', @mandant_id_ar, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('24B98044AFCF11EEAC8F0242AC1A0002'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('56DD546DAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('A38DFF96AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'muhe', null, null, null);
# ziha
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('DEB688EEAFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'hans.zimmermann.ar@mailbucket.dvbern.ch', null, 'Zimmermann', 'AKTIV', 'ziha', 'Hans', @mandant_id_ar, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('E55C0C44AFA111EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('DEB688EEAFA111EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('E55C0C44AFA111EEA5AF00155D1D453B'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'ziha', null, null, null);
# chje
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX('46949E5EAFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, 'jean.chambre.ar@mailbucket.dvbern.ch', null, 'Chambre', 'AKTIV', 'chje', 'Jean', @mandant_id_ar, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('4BE07774AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX('46949E5EAFA211EEA5AF00155D1D453D'), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX('4EE78AF5AFA211EEA5AF00155D1D453D'), '2024-01-09 15:09:03', '2024-01-09 15:09:03', 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'chje', null, null, null);

# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (@system_user, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system_ar', '', @mandant_id_ar, null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ed-4af0-11e9-9b2c-afd41a03c0aa', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', @system_user, null, null);

# Einstellungen 22/23
UPDATE einstellung set value = 'ABHAENGING' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ABWESENHEIT_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '3' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ANSPRUCH_AB_X_MONATEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ANSPRUCH_MONATSWEISE' and gemeinde_id is null;
UPDATE einstellung set value = 'ASIV' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' and gemeinde_id is null;
UPDATE einstellung set value = '18' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'DAUER_BABYTARIF' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'EINGEWOEHNUNG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FACHSTELLEN_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '60' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '30' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' and gemeinde_id is null;
UPDATE einstellung set value = '60' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' and gemeinde_id is null;
UPDATE einstellung set value = 'APPENZELL' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FINANZIELLE_SITUATION_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'null' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' and gemeinde_id is null;
UPDATE einstellung set value = '100' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = '100' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FKJV_TEXTE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' and gemeinde_id is null;
UPDATE einstellung set value = 'KLASSE1' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '51000' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '6.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '70000' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '3.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '01.08.2022' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' and gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '10' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'KITA_STUNDEN_PRO_TAG' and gemeinde_id is null;
UPDATE einstellung set value = '10.55' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'LATS_LOHNNORMKOSTEN' and gemeinde_id is null;
UPDATE einstellung set value = '5.28' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gemeinde_id is null;
UPDATE einstellung set value = '100001' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '12.24' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '6.11' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '11.50' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '70' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '11.50' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '13.50' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '140' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '11.50' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '95' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '2' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MINIMALDAUER_KONKUBINAT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '40000' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '0.78' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_TARIF' and gemeinde_id is null;
UPDATE einstellung set value = '3' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '30' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '10' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '240' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'OEFFNUNGSTAGE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '240' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'OEFFNUNGSTAGE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '20' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' and gemeinde_id is null;
UPDATE einstellung set value = '40' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PENSUM_KITA_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' and gemeinde_id is null;
UPDATE einstellung set value = 'NUR_STUNDEN' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'PENSUM_ANZEIGE_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'KLASSE9' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '0' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ZEMIS_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '6' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '60' where (gesuchsperiode_id = @gesuchsperiode_22_23_id or gesuchsperiode_id = @gesuchsperiode_23_24_id)  and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' and gemeinde_id is null;

# Gemeinde Testgemeinde Appenzell Ausserrhoden erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published)
SELECT @testgemeinde_ar_id, now(), now(), 'flyway', 'flyway', 0,
	   'Testgemeinde Appenzell Ausserrhoden', max(gemeinde_nummer)+1, @mandant_id_ar, 'AKTIV', @testgemeinde_ar_bfs_nr,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31', false, FALSE, false from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('967b2041-39a8-11ed-a63d-b05cda43de9c', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Herisau', '1',
																							 'CH', 'Gemeinde', 'Herisau', '9100', 'Güterstrasse', null);
INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('ae69aa8a-39a8-11ed-a63d-b05cda43de9c', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE('e7cf727f-39a8-11ed-a63d-b05cda43de9c', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0,
		@system_user, @system_user,
		@testgemeinde_ar_id, UNHEX(REPLACE('967b2041-39a8-11ed-a63d-b05cda43de9c', '-', '')),
		'herisau@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.herisau.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
		'Herisau Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('ae69aa8a-39a8-11ed-a63d-b05cda43de9c', '-', '')));

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mandant_id)
	VALUES (@traegerschaft_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Appenzell Ausserrhoden', true, @mandant_id_ar);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@bruennen_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen AR',
	        @mandant_id_ar, @traegerschaft_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@tfo_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Appenzell Ausserrhoden',
	        @mandant_id_ar, null, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@weissenstein_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein AR',
	        @mandant_id_ar, @traegerschaft_id, 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('0a292a5b-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Appenzell Ausserrhoden', 'Herisau', '9100', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('0ee89acb-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein Appenzell Ausserrhoden', 'Herisau', '9100', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('142bf33d-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen Appenzell Ausserrhoden', 'Herisau', '9100', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('22996c95-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen AR', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('276dd6ec-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein AR', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('2bc7aafc-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Appenzell Ausserrhoden', null);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('331186b0-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, UNHEX(REPLACE('2bc7aafc-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('3909773b-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, UNHEX(REPLACE('276dd6ec-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('3eb65ff2-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, UNHEX(REPLACE('22996c95-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('458bee8d-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN',
		UNHEX(REPLACE('0a292a5b-39a9-11ed-a63d-b05cda43de9c', '-', '')),
		@tfo_id, NULL, NULL,
		UNHEX(REPLACE('331186b0-39a9-11ed-a63d-b05cda43de9c', '-', '')), 'tagesfamilien-ar@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('51451c92-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('0ee89acb-39a9-11ed-a63d-b05cda43de9c', '-', '')),
		@weissenstein_id, NULL, NULL,
		UNHEX(REPLACE('3909773b-39a9-11ed-a63d-b05cda43de9c', '-', '')), 'weissenstein-ar@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('56aa13db-39a9-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('142bf33d-39a9-11ed-a63d-b05cda43de9c', '-', '')),
		@bruennen_id, NULL, NULL,
		UNHEX(REPLACE('3eb65ff2-39a9-11ed-a63d-b05cda43de9c', '-', '')), 'bruennen-ar@mailbucket.dvbern.ch', NULL, NULL);

update gemeinde set angebotts = false, angebotfi = false, angebotbgtfo = false where bfs_nummer = @testgemeinde_ar_bfs_nr;

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								 vorgaenger_id, name, status, mandant_id)
VALUES (UNHEX(REPLACE('1653a0c7-39ab-11ed-a63d-b05cda43de9c', '-', '')), now(), now(),
		'flyway', 'flyway', 0, NULL, 'Appenzell Ausserrhodener Sozialdienst', 'AKTIV',
		@mandant_id_ar);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('1bfb8920-39ab-11ed-a63d-b05cda43de9c', '-', '')),  now(), now(),
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Appenzell Ausserrhoden Sozialdienst', 'Herisau', '9100',
		'Sozialdienst Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											version, vorgaenger_id, mail, telefon, webseite, adresse_id,
											sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')),  now(), now(),
		'flyway', 'flyway', 0, NULL, 'sozialdienst-ar@mailbucket.dvbern.ch', '078 898 98 98', 'http://sodialdienst-ar.dvbern.ch',
		UNHEX(REPLACE('1bfb8920-39ab-11ed-a63d-b05cda43de9c', '-', '')),
		UNHEX(REPLACE('1653a0c7-39ab-11ed-a63d-b05cda43de9c', '-', '')));

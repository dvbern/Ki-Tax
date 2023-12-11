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
SET @mandant_id_solothurn = UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-', ''));

SET @gesuchperiode_20_id = UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', ''));
SET @testgemeinde_solothurn_id = UNHEX(REPLACE('47c4b3a8-5379-11ec-98e8-f4390979fa3e', '-', ''));
SET @testgemeinde_grenchen_id = UNHEX(REPLACE('47c4b3a8-5371-11ec-98e8-f4390979fa3e', '-', ''));
SET @traegerschaft_solothurn_id = UNHEX(REPLACE('5c537fd1-537b-11ec-98e8-f4390979fa3e', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('78051383-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('7ce411e7-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @tfo_id = UNHEX(REPLACE('8284b8e2-537e-11ec-98e8-f4390979fa3e', '-', ''));

# Adresse
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('7EBFC8DC537A11EC98E8F4390979FA3E'), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Solothurn', '1', 'CH', 'Gemeinde', 'Solothurn', '4500', 'Berfüssergasse', null, null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('7EBFC8DC537A11EC98E8F4390979FB3E'), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Grenchen', '1', 'CH', 'Gemeinde', 'Grenchen', '2540', 'Testergasse', null, null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('B240C25F539311EC98E8F4390979FA3E'), '2020-02-28 09:48:18', '2020-02-28 10:11:35', 'flyway', 'flyway', 1, null, '1000-01-01', '9999-12-31', null, '2', 'CH', 'Tageschule Solothurn', 'Solothurn', '4500', 'Solothurner Strasse', null, null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('BB7D074C537E11EC98E8F4390979FA3E'), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Solothurn', 'Solothurn', '4500', 'Gasstrasse', null, null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('CA3E50E6537E11EC98E8F4390979FA3E'), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen Solothurn', 'Solothurn', '4500', 'Colombstrasse', null, null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile, bfs_nummer) VALUES (UNHEX('C2EA1156537E11EC98E8F4390979FA3E'), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein Solothurn', 'Solothurn', '4500', 'Weberstrasse', null, null);


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
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_solothurn;
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

# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchperiode_20_id;

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
        UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
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
		UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
		@testgemeinde_grenchen_id, UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fb3e', '-', '')),
		'grenchen@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.grenchen.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
		'Grenchen Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c6bg', '-', '')));



# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active)
	VALUES (@traegerschaft_solothurn_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Solothurn', true);

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
															   alterskategorie_kindergarten, alterskategorie_schule,
															   subventionierte_plaetze, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('2d3f850d-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('0b1dc282-537f-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
		FALSE, FALSE, 30, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   subventionierte_plaetze, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('365cbb27-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('04176a28-537f-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
		FALSE, FALSE, 35, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   subventionierte_plaetze, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('3f194c4f-537f-11ec-98e8-f4390979fa3e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('fca0bc52-537e-11ec-98e8-f4390979fa3e', '-', '')), FALSE, FALSE, FALSE,
		FALSE, FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

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

update gemeinde set angebotts = false, angebotfi = false where bfs_nummer = 99996;

-- Tagesschule Gemeinde Paris
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								vorgaenger_id, name, status, mandant_id, traegerschaft_id, stammdaten_check_required,
								event_published)
VALUES (UNHEX(REPLACE('bbf7f306-5392-11ec-98e8-f4390979fa3e', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 0, NULL, 'Tagesschule Solothurn', 'AKTIV',
		@mandant_id_solothurn, NULL, FALSE, TRUE);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('b240c25f-5393-11ec-98e8-f4390979fa3e', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Tageschule Solothurn', 'Solothurn', '4500',
		'Solothurner Strasse', NULL);

INSERT IGNORE INTO institution_stammdaten_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
													   user_mutiert, version, gemeinde_id)
VALUES (UNHEX(REPLACE('d75e306e-5393-11ec-98e8-f4390979fa3e', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
		'flyway', 'flyway', 0, @testgemeinde_solothurn_id);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, mail,
										   telefon, webseite, adresse_id, institution_id,
										   institution_stammdaten_ferieninsel_id, institution_stammdaten_tagesschule_id,
										   send_mail_wenn_offene_pendenzen,
										   institution_stammdaten_betreuungsgutscheine_id)
VALUES (UNHEX(REPLACE('feddca3a-617f-11ec-9b42-b89a2ae4a038', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
		'flyway', 'flyway', 0, NULL, '2020-08-01', '9999-12-31', 'TAGESSCHULE', 'tagesschule-so@mailbucket.dvbern.ch', NULL, NULL,
		UNHEX(REPLACE('b240c25f-5393-11ec-98e8-f4390979fa3e', '-', '')),
		UNHEX(REPLACE('bbf7f306-5392-11ec-98e8-f4390979fa3e', '-', '')), NULL,
		UNHEX(REPLACE('d75e306e-5393-11ec-98e8-f4390979fa3e', '-', '')), TRUE, NULL);

INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											  version,
											  modul_tagesschule_typ, gesuchsperiode_id,
											  institution_stammdaten_tagesschule_id,
											  erlaeuterung)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
		  '2020-01-01 00:00:00'              as timestamp_erstellt,
		  '2020-01-01 00:00:00'              as timestamp_mutiert,
		  'flyway'                           as user_erstellt,
		  'flyway'                           as user_mutiert,
		  0                                  as version,
		  'DYNAMISCH' as  modul_tagesschule_typ,
		  gp.id   							 as gesuchsperiode_id,
		  UNHEX(REPLACE('d75e306e-5393-11ec-98e8-f4390979fa3e','-', '')) as institution_stammdaten_tagesschule_id,
		  null as erlaeuterung
	  from gesuchsperiode as gp where gp.mandant_id = @mandant_id_solothurn) as tmp;

INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															 user_mutiert, version, gemeinde_id, gesuchsperiode_id,
															 merkblatt_anmeldung_tagesschule_de,
															 merkblatt_anmeldung_tagesschule_fr)
VALUES
# LONDON
	(UNHEX(REPLACE('ff9aff6f-5393-11ec-98e8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, @testgemeinde_solothurn_id,
	 @gesuchperiode_20_id, NULL, NULL);

# LONDON
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert,
																		 user_erstellt, user_mutiert, version,
																		 vorgaenger_id, anmeldeschluss, ferienname,
																		 gemeinde_stammdaten_gesuchsperiode_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-09-01', 'HERBSTFERIEN',
		UNHEX(REPLACE('ff9aff6f-5393-11ec-98e8-f4390979fa3e', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-06-01', 'SOMMERFERIEN',
	 UNHEX(REPLACE('ff9aff6f-5393-11ec-98e8-f4390979fa3e', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-01-01', 'SPORTFERIEN',
	 UNHEX(REPLACE('ff9aff6f-5393-11ec-98e8-f4390979fa3e', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-04-01', 'FRUEHLINGSFERIEN',
	 UNHEX(REPLACE('ff9aff6f-5393-11ec-98e8-f4390979fa3e', '-', '')));

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

/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));
call select_gesuchsperiode('2024-08-01', @mandant_id_schwyz, @gesuchsperiode_24_25_id);

SET @testgemeinde_schwyz_id = UNHEX(REPLACE('de7c81c0-b3d5-11ee-829a-0242ac160002', '-', ''));
SET @traegerschaft_schwyz_id = UNHEX(REPLACE('ef7ef939-b3e7-11ee-829a-0242ac160002', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('1188c355-b3d6-11ee-829a-0242ac160002', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('1722f92b-b3d6-11ee-829a-0242ac160002', '-', ''));
SET @tfo_id = UNHEX(REPLACE('1c218a88-b3d6-11ee-829a-0242ac160002', '-', ''));
SET @ts_id = UNHEX(REPLACE('e67aa195-b912-11ee-8d78-0242ac160002', '-', ''));
SET @mittagstisch_id = UNHEX(REPLACE('7212f92b-b3c6-21ea-729b-1242ac160003', '-', ''));
SET @system_user = UNHEX(REPLACE('33333333-3333-3333-3333-333333333333', '-', ''));

# APPLICATION PROPERTIES
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'false' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'false' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @mandant_id_schwyz;
UPDATE application_property SET value = 'true' WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @mandant_id_schwyz;

# Gesuchsperiode
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_24_25_id;

# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (@system_user, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system_sz', '', @mandant_id_schwyz, null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ed-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', @system_user, null, null);

# Antragstellende Benutzer fuer e2e erstellen
# geem
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX(REPLACE('2805915e-b3e5-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, 'emma.gerber.sz@mailbucket.dvbern.ch', null, 'Gerber', 'AKTIV', 'geem', 'Emma', @mandant_id_schwyz, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('4aedf52b-b3e5-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX(REPLACE('2805915e-b3e5-11ee-829a-0242ac160002', '-', '')), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('77f5cf9e-b3e5-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'geem', null, null, null);
# bemi
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX(REPLACE('8dc23a93-b3e5-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, 'michael.berger.sz@mailbucket.dvbern.ch', null, 'Berger', 'AKTIV', 'bemi', 'Michael', @mandant_id_schwyz, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('9ddc7f7e-b3e5-11ee-829a-0242ac160002','-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX(REPLACE('8dc23a93-b3e5-11ee-829a-0242ac160002', '-', '')), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('ae235ef5-b3e5-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'bemi', null, null, null);
# muhe
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX(REPLACE('19b2da43-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, 'heinrich.mueller.sz@mailbucket.dvbern.ch', null, 'Mueller', 'AKTIV', 'muhe', 'Heinrich', @mandant_id_schwyz, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('54cc871a-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX(REPLACE('19b2da43-b3e6-11ee-829a-0242ac160002', '-', '')), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('5800b6a0-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'muhe', null, null, null);
# ziha
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX(REPLACE('5bc6afd8-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, 'hans.zimmermann.sz@mailbucket.dvbern.ch', null, 'Zimmermann', 'AKTIV', 'ziha', 'Hans', @mandant_id_schwyz, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('5f971663-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX(REPLACE('5bc6afd8-b3e6-11ee-829a-0242ac160002', '-', '')), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('61f4007c-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'ziha', null, null, null);
# chje
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid, nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer) VALUES (UNHEX(REPLACE('649c3105-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, 'jean.chambre.sz@mailbucket.dvbern.ch', null, 'Chambre', 'AKTIV', 'chje', 'Jean', @mandant_id_schwyz, null, null);
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('69385b24-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', 'GESUCHSTELLER', UNHEX(REPLACE('649c3105-b3e6-11ee-829a-0242ac160002', '-', '')), null, null, null);
INSERT IGNORE INTO berechtigung_history (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id) VALUES (UNHEX(REPLACE('6c6855d9-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2024-01-09', '9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', 'chje', null, null, null);

# Test Gemeinden Schwyz erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published, angebotbgtfo)
SELECT @testgemeinde_schwyz_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0,
	   'Testgemeinde Schwyz', max(gemeinde_nummer)+1, @mandant_id_schwyz, 'AKTIV', 99992,
	   '2016-01-01', '2020-08-01', '2020-08-01', TRUE, TRUE, FALSE, '9999-12-31', FALSE, FALSE, FALSE, TRUE from gemeinde;

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('c055f560-b3e6-11ee-829a-0242ac160002', '-', '')),
																							 NOW(), NOW(), 'flyway:Kanton Schwyz',
																							 'flyway:Kanton Schwyz', 0, null, '2018-01-01', '9999-01-01', 'Schwyz', '1',
																							 'CH', 'Gemeinde', 'Schwyz', '640', 'Berfüssergasse', null);

INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(UNHEX(REPLACE('eedd4b82-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE('f5c2c6b3-b3e6-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0,
        @system_user, @system_user,
        @testgemeinde_schwyz_id, UNHEX(REPLACE('c055f560-b3e6-11ee-829a-0242ac160002', '-', '')),
        'Schwyz@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.schwyz.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
        'Schwyz Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('eedd4b82-b3e6-11ee-829a-0242ac160002', '-', '')));



# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, email, mandant_id)
	VALUES (@traegerschaft_schwyz_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, 'Kitas & Tagis Kanton Schwyz', true, 'kitastagis-sz@mailbucket.dvbern.ch', @mandant_id_schwyz);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@bruennen_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, 'Brünnen SZ',
			@mandant_id_schwyz, @traegerschaft_schwyz_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@tfo_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, 'Tageseltern Schwyz',
			@mandant_id_schwyz, @traegerschaft_schwyz_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@weissenstein_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, 'Weissenstein SZ',
			@mandant_id_schwyz, @traegerschaft_schwyz_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@mittagstisch_id, NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, 'Mittagstisch SZ',
			@mandant_id_schwyz, @traegerschaft_schwyz_id, 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('34b03b7e-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Schwyz', 'Schwyz', '4500', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('3b3277b4-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein Schwyz', 'Schwyz', '4500', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('40933ba4-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen Schwyz', 'Schwyz', '4500', 'Colombstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('ed882d63-dc72-11ee-8dae-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Mittagstisch Schwyz', 'Schwyz', '4500', 'MIttagstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('4ef020a5-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen SZ', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('539c6b3e-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein SZ', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('5913320b-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Schwyz', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('fcc33b19-dc72-11ee-8dae-0242ac160002', '-', '')), NOW(), NOW(), 'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Mittagstisch Schwyz', null);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('65dd4898-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, UNHEX(REPLACE('5913320b-b3e8-11ee-829a-0242ac160002', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 30, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('7f7041ab-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, UNHEX(REPLACE('539c6b3e-b3e8-11ee-829a-0242ac160002', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 35, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('95440105-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, UNHEX(REPLACE('4ef020a5-b3e8-11ee-829a-0242ac160002', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('159918e0-dc73-11ee-8dae-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, UNHEX(REPLACE('fcc33b19-dc72-11ee-8dae-0242ac160002', '-', '')), FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('9fdc2b4d-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN',
		UNHEX(REPLACE('34b03b7e-b3e8-11ee-829a-0242ac160002', '-', '')),
		@tfo_id, NULL, NULL,
		UNHEX(REPLACE('65dd4898-b3e8-11ee-829a-0242ac160002', '-', '')), 'tagesfamilien-sz@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('28026216-dc73-11ee-8dae-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, '2019-08-01', '9999-12-31', 'MITTAGSTISCH',
		UNHEX(REPLACE('ed882d63-dc72-11ee-8dae-0242ac160002', '-', '')),
		@mittagstisch_id, NULL, NULL,
		UNHEX(REPLACE('159918e0-dc73-11ee-8dae-0242ac160002', '-', '')), 'mittagstisch-sz@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('cfeeb01a-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('3b3277b4-b3e8-11ee-829a-0242ac160002', '-', '')),
		@weissenstein_id, NULL, NULL,
		UNHEX(REPLACE('7f7041ab-b3e8-11ee-829a-0242ac160002', '-', '')), 'weissenstein-sz@mailbucket.dvbern.ch', NULL,
		NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('d968ba59-b3e8-11ee-829a-0242ac160002', '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('40933ba4-b3e8-11ee-829a-0242ac160002', '-', '')),
		@bruennen_id, NULL, NULL,
		UNHEX(REPLACE('95440105-b3e8-11ee-829a-0242ac160002', '-', '')), 'bruennen-sz@mailbucket.dvbern.ch', NULL, NULL);

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								 vorgaenger_id, name, status, mandant_id)
VALUES (UNHEX(REPLACE('070e2aa4-b3e9-11ee-829a-0242ac160002', '-', '')), NOW(), '2021-02-15 10:11:35',
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, 'Schwyzer Sozialdienst', 'AKTIV',
		@mandant_id_schwyz);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('0c84210c-b3e9-11ee-829a-0242ac160002', '-', '')), NOW(), '2021-02-15 10:11:35',
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Schwyzer Sozialdienst', 'Schwyz', '4500',
		'Schwyzer Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											version, vorgaenger_id, mail, telefon, webseite, adresse_id,
											sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(),
		'flyway:Kanton Schwyz', 'flyway:Kanton Schwyz', 0, NULL, 'sozialdienst-sz@mailbucket.dvbern.ch', '078 898 98 98', 'http://sodialdienst-sz.dvbern.ch',
		UNHEX(REPLACE('0c84210c-b3e9-11ee-829a-0242ac160002', '-', '')),
		UNHEX(REPLACE('070e2aa4-b3e9-11ee-829a-0242ac160002', '-', '')));

UPDATE mandant SET mandant.activated=true where id = @mandant_id_schwyz;

# Tagesschule Schwyz
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
								name, status, mandant_id, traegerschaft_id, stammdaten_check_required, event_published)
VALUES (@ts_id, '2020-02-28 09:48:18', '2020-02-28 10:11:35', 'flyway',
		'flyway', 0, null, 'Tagesschule Schwyz', 'AKTIV', @mandant_id_schwyz, null,
		false, true);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
							gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('162812f0-b911-11ee-8d78-0242ac160002', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35', 'flyway',
		'flyway', 1, null, '1000-01-01', '9999-12-31', null, '2', 'CH', 'Tageschule Schwyz', 'Schwyz', '6430', 'Schwyzer Strasse',
		null);

INSERT IGNORE INTO institution_stammdaten_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
													   version, gemeinde_id)
VALUES (UNHEX(REPLACE('34a03f8b-b911-11ee-8d78-0242ac160002', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18', 'flyway',
		'flyway', 0, @testgemeinde_schwyz_id);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										   vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, mail, telefon, webseite,
										   adresse_id, institution_id, institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_tagesschule_id, send_mail_wenn_offene_pendenzen,
										   institution_stammdaten_betreuungsgutscheine_id)
VALUES (UNHEX(REPLACE('47ce5ee5-b911-11ee-8d78-0242ac160002', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18', 'flyway',
		'flyway', 0, null, '2020-08-01', '9999-12-31', 'TAGESSCHULE', 'test@mailbucket.dvbern.ch', null, null,
		UNHEX(REPLACE('162812f0-b911-11ee-8d78-0242ac160002', '-', '')),
		@ts_id, null,
		UNHEX(REPLACE('34a03f8b-b911-11ee-8d78-0242ac160002', '-', '')), true, null);

INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
											  modul_tagesschule_typ, gesuchsperiode_id, institution_stammdaten_tagesschule_id,
											  erlaeuterung, tagi)
VALUES (UNHEX('c17a6c06b91111ee8d780242ac160002'), '2023-12-07 15:55:26', '2023-12-07 15:55:26', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'DYNAMISCH', @gesuchsperiode_24_25_id, UNHEX('34a03f8bb91111ee8d780242ac160002'),
		null, false);

INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
								   text_deutsch, text_franzoesisch)
VALUES (UNHEX('5c4a4720b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, null, 'Nachmittag', 'Après-midi');
INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
								   text_deutsch, text_franzoesisch)
VALUES (UNHEX('6c2bc64ab91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, null, 'Morgen', 'Matin');

INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
											identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten,
											wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id,
											bezeichnung_id, fremd_id)
VALUES (UNHEX('4edfb4c3b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'lNd5s2hVLEGNAyVgEJOyhGPfjhJoEaDCp4Pp', 'WOECHENTLICH', 'DYNAMISCH', 0, 2.00, true, '17:00:00',
		'13:00:00', UNHEX('c17a6c06b91111ee8d780242ac160002'), UNHEX('5c4a4720b91211ee8d780242ac160002'), null);
INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
											identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten,
											wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id,
											bezeichnung_id, fremd_id)
VALUES (UNHEX('614e247cb91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'F8vXs39fkEuXdvi3kjLv5DXh5m4fcVpm27tw', 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00',
		'08:00:00', UNHEX('c17a6c06b91111ee8d780242ac160002'), UNHEX('6c2bc64ab91211ee8d780242ac160002'), null);

INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('9464012cb91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'THURSDAY', UNHEX('614e247cb91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('97133873b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'WEDNESDAY', UNHEX('4edfb4c3b91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('9fc6a35ab91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'MONDAY', UNHEX('614e247cb91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('a2a4e499b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'FRIDAY', UNHEX('614e247cb91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('a73af6d7b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'THURSDAY', UNHEX('4edfb4c3b91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('ad5e04b0b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'MONDAY', UNHEX('4edfb4c3b91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('af8448b2b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'TUESDAY', UNHEX('614e247cb91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('b18a65bbb91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'WEDNESDAY', UNHEX('614e247cb91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('b6e0e9d0b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'TUESDAY', UNHEX('4edfb4c3b91211ee8d780242ac160002'));
INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag,
									  modul_tagesschule_group_id)
VALUES (UNHEX('baff3e93b91211ee8d780242ac160002'), '2023-12-08 09:45:52', '2023-12-08 09:45:52', 'ebegu:Kanton Schwyz',
		'ebegu:Kanton Schwyz', 0, 'FRIDAY', UNHEX('4edfb4c3b91211ee8d780242ac160002'));

# Set Einstellungen Periode 24/25
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'FACHSTELLEN_TYP' AND gesuchsperiode_id = @gesuchsperiode_24_25_id AND gemeinde_id is null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND gesuchsperiode_id = @gesuchsperiode_24_25_id AND gemeinde_id is null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ZEMIS_DISABLED' AND gesuchsperiode_id = @gesuchsperiode_24_25_id AND gemeinde_id is null;

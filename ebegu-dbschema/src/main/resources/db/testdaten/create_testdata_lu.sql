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

SET @mandant_id_luzern = UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-', ''));

# GESUCHSPERIODE 20/21

UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id =  @mandant_id_luzern;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id =  @mandant_id_luzern;

# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE mandant_id =  @mandant_id_luzern;

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis)
SELECT UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
	   'Testgemeinde Luzern', max(gemeinde_nummer)+1,  @mandant_id_luzern, 'AKTIV', 99997,
	'2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31' from gemeinde;

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
        UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
        UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')), UNHEX(REPLACE('2476287e-3264-11ec-a17e-b89a2ae4a038', '-', '')),
        'luzern@mailbucket.dvbern.ch', '+41 31 930 14 14', 'https://www.luzern.ch', null, 'DE', 'BIC', 'CH93 0077 2011 6238 5295 7',
        'Luzern Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0be', '-', '')));

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
	VALUES (UNHEX(REPLACE('abd8ec9b-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Bern', 'Bern', '3005', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) 
	VALUES (UNHEX(REPLACE('bda4670c-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Bern', '3007', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (UNHEX(REPLACE('c41ab591-30a3-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Bern', '3027', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('ad4c1134-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen LU', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('b4625718-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein LU', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (UNHEX(REPLACE('be37b235-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Luzern', null);

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
VALUES (UNHEX(REPLACE('e85adc3b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('be37b235-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('eebf7efd-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('b4625718-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('f482ce4b-30a4-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('ad4c1134-30a4-11ec-a86f-b89a2ae4a038', '-', '')), FALSE, FALSE, FALSE,
		FALSE, FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

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

# Tagesschule
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
VALUES (UNHEX(REPLACE('3db43c9b-30a6-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, 'Tagesschule LU',  @mandant_id_luzern,
		NULL, 'AKTIV', FALSE);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('4cb593c7-30a6-11ec-a86f-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '1000-01-01', '9999-12-31', NULL, '21', 'CH', 'Tagesschule LU', 'Luzern', '3008',
		'Effingerstrasse', NULL);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('b8e3acdc-3261-11ec-a17e-b89a2ae4a038', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '1000-01-01', '9999-12-31', 'TAGESSCHULE',
		UNHEX(REPLACE('4cb593c7-30a6-11ec-a86f-b89a2ae4a038', '-', '')),
		UNHEX(REPLACE('3db43c9b-30a6-11ec-a86f-b89a2ae4a038', '-', '')), NULL, NULL, NULL,
		'tagesschule-lu@mailbucket.dvbern.ch', NULL, NULL);

update gemeinde set angebotts = false, angebotfi = false where bfs_nummer = 99997;

-- Tagesschule Gemeinde Paris
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								vorgaenger_id, name, status, mandant_id, traegerschaft_id, stammdaten_check_required,
								event_published)
VALUES (UNHEX(REPLACE('9d668709-30aa-11ec-a86f-b89a2ae4a038', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 0, NULL, 'Tagesschule Luzern', 'AKTIV',
		 @mandant_id_luzern, NULL, FALSE, TRUE);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('d4388a51-30aa-11ec-a86f-b89a2ae4a038', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Tageschule Luzern', 'Luzern', '6000',
		'Luzerner Strasse', NULL);

INSERT IGNORE INTO institution_stammdaten_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
													   user_mutiert, version, gemeinde_id)
VALUES (UNHEX(REPLACE('e24f092b-30aa-11ec-a86f-b89a2ae4a038', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
		'flyway', 'flyway', 0, UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')));

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
		  UNHEX(REPLACE('e24f092b-30aa-11ec-a86f-b89a2ae4a038','-', '')) as institution_stammdaten_tagesschule_id,
		  null as erlaeuterung
	  from gesuchsperiode as gp where gp.mandant_id =  @mandant_id_luzern) as tmp;

# Tagis fuer Paris aktivieren
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' as einstellung_key,
			 'true' 								as value,
			 UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp
where (select count(*) from einstellung where gemeinde_id =  UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', ''))
		and einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED') = 0;


# Minimale Erwerbspensen fuer Paris ueberschreiben: 15%/30% anstatt 20%/40% fuer Vorschulkinder/EingeschulteKinder
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' as einstellung_key,
			 '15' 								as value,
			 UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp
where (select count(*) from einstellung where gemeinde_id =  UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', ''))
		and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT') = 0;

INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' as einstellung_key,
			 '30' 								as value,
			 UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp
where (select count(*) from einstellung where gemeinde_id =  UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', ''))
		and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT') = 0;

INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															 user_mutiert, version, gemeinde_id, gesuchsperiode_id,
															 merkblatt_anmeldung_tagesschule_de,
															 merkblatt_anmeldung_tagesschule_fr)
VALUES
# LONDON
	(UNHEX(REPLACE('2f75ae22-30ab-11ec-a86f-b89a2ae4a038', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, UNHEX(REPLACE('6fd6183c-30a2-11ec-a86f-b89a2ae4a038', '-', '')),
	 UNHEX(REPLACE('1670d04a-30a9-11ec-a86f-b89a2ae4a038', '-', '')), NULL, NULL);

# LONDON
INSERT IGNORE INTO ebegu.gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert,
																		 user_erstellt, user_mutiert, version,
																		 vorgaenger_id, anmeldeschluss, ferienname,
																		 gemeinde_stammdaten_gesuchsperiode_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-09-01', 'HERBSTFERIEN',
		UNHEX(REPLACE('2f75ae22-30ab-11ec-a86f-b89a2ae4a038', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-06-01', 'SOMMERFERIEN',
	 UNHEX(REPLACE('2f75ae22-30ab-11ec-a86f-b89a2ae4a038', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-01-01', 'SPORTFERIEN',
	 UNHEX(REPLACE('2f75ae22-30ab-11ec-a86f-b89a2ae4a038', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-04-01', 'FRUEHLINGSFERIEN',
	 UNHEX(REPLACE('2f75ae22-30ab-11ec-a86f-b89a2ae4a038', '-', '')));

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

UPDATE ebegu.mandant SET ebegu.mandant.activated=true where id = @mandant_id_luzern;
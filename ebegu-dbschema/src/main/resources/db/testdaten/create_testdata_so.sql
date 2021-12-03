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
SET @mandant_id_bern = UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', ''));
SET @gesuchperiode_20_id = UNHEX(REPLACE('6dc45fb0-5378-11ec-98e8-f4390979fa3e', '-', ''));
SET @gesuchsperiode_bern_id = UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', ''));
SET @testgemeinde_solothurn_id = UNHEX(REPLACE('47c4b3a8-5379-11ec-98e8-f4390979fa3e', '-', ''));
SET @traegerschaft_solothurn_id = UNHEX(REPLACE('5c537fd1-537b-11ec-98e8-f4390979fa3e', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('78051383-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('7ce411e7-537e-11ec-98e8-f4390979fa3e', '-', ''));
SET @tfo_id = UNHEX(REPLACE('8284b8e2-537e-11ec-98e8-f4390979fa3e', '-', ''));

# APPLICATION PROPERTIES
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_solothurn;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id_solothurn;

# GESUCHSPERIODE 20/21
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, status,
							datum_aktiviert, mandant_id)
VALUES (@gesuchperiode_20_id, '2018-01-01 00:00:00', '2018-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2020-08-01', '2021-07-31', 'ENTWURF', NULL, @mandant_id_solothurn);

# Default system einstellungen for lu GS
INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	einstellung_key, value, NULL, @gesuchperiode_20_id, NULL
FROM einstellung
WHERE mandant_id IS NULL AND gesuchsperiode_id = @gesuchsperiode_bern_id AND NOT EXISTS(
		SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  @gesuchperiode_20_id
				and e1.mandant_id IS NULL AND e1.einstellung_key = einstellung.einstellung_key AND e1.gemeinde_id IS NULL
	);

INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	einstellung_key, value, NULL, @gesuchperiode_20_id, @mandant_id_solothurn
FROM einstellung
WHERE mandant_id = @mandant_id_bern AND gesuchsperiode_id = @gesuchsperiode_bern_id AND NOT EXISTS(
		SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  @gesuchperiode_20_id
				and e1.mandant_id = @mandant_id_solothurn AND e1.einstellung_key = einstellung.einstellung_key
	) AND gemeinde_id IS NULL;

# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchperiode_20_id;

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
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

INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										logo_content, bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen)
VALUES (UNHEX(REPLACE('b5171d87-537a-11ec-98e8-f4390979fa3e', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0,
        UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
        @testgemeinde_solothurn_id, UNHEX(REPLACE('7ebfc8dc-537a-11ec-98e8-f4390979fa3e', '-', '')),
        'solothurn@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.solothurn.ch', null, 'DE', null, 'BIC', 'CH2089144969768441935',
        'Solothurn Kontoinhaber', true, true, true, true, false);

INSERT IGNORE INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
	UNHEX(REPLACE('09399d46-537b-11ec-98e8-f4390979fa3e', '-', '')), # id
	'2018-01-01 00:00:00', # timestamp_erstellt
	'2018-01-01 00:00:00', # timestamp_mutiert
	'flyway', # user_erstellt
	'flyway', # user_mutiert
	0, # version
	'FALL_NUMMER', # sequence_type
	100, # current_value
	@mandant_id_solothurn);

INSERT IGNORE INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
	UNHEX(REPLACE('0e285bf4-537b-11ec-98e8-f4390979fa3e', '-', '')), # id
	'2018-01-01 00:00:00', # timestamp_erstellt
	'2018-01-01 00:00:00', # timestamp_mutiert
	'flyway', # user_erstellt
	'flyway', # user_mutiert
	0, # version
	'GEMEINDE_NUMMER', # sequence_type
	1, # current_value
	@mandant_id_solothurn);

UPDATE sequence
SET current_value = (SELECT max(gemeinde_nummer)
					 FROM gemeinde
					 WHERE gemeinde.mandant_id = @mandant_id_solothurn)
WHERE sequence_type = 'GEMEINDE_NUMMER' AND
		mandant_id = @mandant_id_solothurn;

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

-- Zusatzgutschein-Konfigurationen überschreiben am Beispiel Paris

# -- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))  as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' as einstellung_key,
# 			 'false' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' as einstellung_key,
# 			 '11.00' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' as einstellung_key,
# 			 '0.11' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' as einstellung_key,
# 			 'KINDERGARTEN2' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 								einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 		  '2020-01-01 00:00:00'              as timestamp_erstellt,
# 		  '2020-01-01 00:00:00'              as timestamp_mutiert,
# 		  'flyway'                           as user_erstellt,
# 		  'flyway'                           as user_mutiert,
# 		  0                                  as version,
# 		  'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' as einstellung_key,
# 		  'KINDERGARTEN2'								as value,
# 		  @testgemeinde_solothurn_id as gemeinde_id,
# 		  gp.id                              as gesuchsperiode_id,
# 		  @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' as einstellung_key,
# 			 'false'								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA
# INSERT IGNORE  INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' as einstellung_key,
# 			 '50.00' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' as einstellung_key,
# 			 '4.54' 							as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' as einstellung_key,
# 			 'false' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED') = 0;
#
# -- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' as einstellung_key,
# 			 '20' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' as einstellung_key,
# 			 'false' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
# 			 '6' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' as einstellung_key,
# 			 '51000' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT
# INSERT IGNORE  INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
# 			 '3' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' as einstellung_key,
# 			 '70000' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN') = 0;
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
# 			 '0' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT') = 0;
#
#
# -- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' as einstellung_key,
# 			 'true' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED') = 0;

-- GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 		  '2020-01-01 00:00:00'              as timestamp_erstellt,
# 		  '2020-01-01 00:00:00'              as timestamp_mutiert,
# 		  'flyway'                           as user_erstellt,
# 		  'flyway'                           as user_mutiert,
# 		  0                                  as version,
# 		  'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' as einstellung_key,
# 		  'false' 								as value,
# 		  @testgemeinde_solothurn_id as gemeinde_id,
# 		  gp.id                              as gesuchsperiode_id,
# 		  @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED') = 0;


-- Tagesschule Gemeinde Paris // hier
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
VALUES (UNHEX(REPLACE('0f1c6b9e-37de-4c10-8ddc-9514fb840f5e', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
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
	  from gesuchsperiode as gp) as tmp;

# Tagis fuer Paris aktivieren
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' as einstellung_key,
# 			 'false' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED') = 0;
#
#
# # Minimale Erwerbspensen fuer Paris ueberschreiben: 15%/30% anstatt 20%/40% fuer Vorschulkinder/EingeschulteKinder
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' as einstellung_key,
# 			 'null' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT') = 0;
#
# INSERT IGNORE INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
# 						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
# SELECT *
# FROM (SELECT UNHEX(REPLACE(UUID(), '-', ''))    as id,
# 			 '2020-01-01 00:00:00'              as timestamp_erstellt,
# 			 '2020-01-01 00:00:00'              as timestamp_mutiert,
# 			 'flyway'                           as user_erstellt,
# 			 'flyway'                           as user_mutiert,
# 			 0                                  as version,
# 			 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' as einstellung_key,
# 			 'null' 								as value,
# 			 @testgemeinde_solothurn_id as gemeinde_id,
# 			 gp.id                              as gesuchsperiode_id,
# 			 @mandant_id_solothurn as mandant_id
# 	  from gesuchsperiode as gp) as tmp
# where (select count(*) from einstellung where gemeinde_id =  @testgemeinde_solothurn_id
# 		and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT') = 0;

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
INSERT IGNORE INTO ebegu.gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert,
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

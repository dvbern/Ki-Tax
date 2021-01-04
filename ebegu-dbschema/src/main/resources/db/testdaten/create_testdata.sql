UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED';
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR';
# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV';

# Benutzer System erstellen
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system', '', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV');
INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ec-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), null, null);

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis)
VALUES (
		   UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
		   'London', 2, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99999,
        '2016-01-01', '2020-08-01', '2020-08-01', true, false, false, '9999-12-31');
INSERT INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum,
                      angebotbg, angebotts, angebotfi, gueltig_bis)
VALUES (
		   UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
		   'Paris', 1, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99998, '2016-01-01',
        '2020-08-01', '2020-08-01', true, false, false, '9999-12-31');

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Paris', '21',
																							 'CH', 'Jugendamt', 'Paris', '3008', 'Effingerstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'London', '1',
																							 'CH', 'Gemeinde', 'London', '3072', 'Schiessplatzweg', null);

INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 logo_content, bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
								 standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen) VALUES(UNHEX(REPLACE
	('4a7d313f-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2018-10-23 00:00:00',
																													 '2018-10-23 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'paris@mailbucket.dvbern.ch', '+41 31 321 61 11', 'https://www.bern.ch', null, 'DE_FR', null, 'BIC', 'CH93 0076 2011 6238 5295 7', 'Paris Kontoinhaber', true, true, true, true, true);
INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 logo_content, bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
								 standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen) VALUES (UNHEX(REPLACE
	('4a7dc6e5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2018-10-23 00:00:00',
																													  '2018-10-23 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', '')), UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'london@mailbucket.dvbern.ch', '+41 31 930 14 14', 'https://www.ostermundigen.ch', null, 'DE', null, 'BIC', 'CH93 0076 2011 6238 5295 7', 'London Kontoinhaber', true, true, true, true, false);

UPDATE sequence SET current_value = 2 WHERE sequence_type = 'GEMEINDE_NUMMER';

# Test-Institutionen erstellen
INSERT INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active) VALUES (UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Bern', true);

# Kita und Tagesfamilien
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Bern', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Bern', 'Bern', '3005', 'Gasstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Bern', '3007', 'Weberstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Bern', '3027', 'Colombstrasse', null);

INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen', null);
INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein', null);
INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), '2020-01-01 00:00:00', '2020-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Bern', null);

INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis) VALUES (UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), false, false, false, false, false, 30, null, '08:00', '18:00');
INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis) VALUES (UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), false, false, false, false, false, 35, null, '08:00', '18:00');
INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis) VALUES (UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), false, false, false, false, false, 40, null, '08:00', '18:00');

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('6b7beb6e-6cf3-49d6-84c0-5818d9215ecd', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN', UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')),  null, null, UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), 'tagesfamilien@mailbucket.dvbern.ch', null, null);
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('945e3eef-8f43-43d2-a684-4aa61089684b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), null, null, UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), 'weissenstein@mailbucket.dvbern.ch', null, null);
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('9a0eb656-b6b7-4613-8f55-4e0e4720455e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), null,  null, UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), 'bruennen@mailbucket.dvbern.ch', null, null);

INSERT INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('c93fbba5-91e2-4fac-88a3-a2dc8386d62d', '-', '')), '2020-06-01 00:00:00', '2020-06-01 00:00:00', 'flyway', 'flyway', 0, ' Brünnen', 'Brünnen', 11.50, 240.00);
INSERT INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('9a9cc8a2-32b9-4ad0-8f41-ed503f886100', '-', '')), '2020-06-01 00:00:00', '2020-06-01 00:00:00', 'flyway', 'flyway', 0, ' Weissenstein', 'Weissenstein', 11.50, 240.00);

# Tagesschule
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tagesschule', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV', false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '21', 'CH', 'Tagesschule', 'Bern', '3008', 'Effingerstrasse', null);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('199ac4a1-448f-4d4c-b3a6-5aee21f89613', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESSCHULE', UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), null, null, null, 'tagesschule@mailbucket.dvbern.ch', null, null);

update mandant set angebotts = true;
update mandant set angebotfi = true;

update gemeinde set angebotts = true;
update gemeinde set angebotfi = true;

-- Zusatzgutschein-Konfigurationen überschreiben am Beispiel Paris

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' as einstellung_key,
			 'true' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' as einstellung_key,
			 '11.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' as einstellung_key,
			 '0.11' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' as einstellung_key,
			 'KINDERGARTEN2' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' as einstellung_key,
			 'KINDERGARTEN2' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' as einstellung_key,
			 'true'								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' as einstellung_key,
			 '50.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' as einstellung_key,
			 '4.54' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' as einstellung_key,
			 'true' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' as einstellung_key,
			 '20' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' as einstellung_key,
			 'true' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
			 '6' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' as einstellung_key,
			 '51000' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
			 '3' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' as einstellung_key,
			 '70000' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' as einstellung_key,
			 '0' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' as einstellung_key,
			 'true' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
		  '2020-01-01 00:00:00'              as timestamp_erstellt,
		  '2020-01-01 00:00:00'              as timestamp_mutiert,
		  'flyway'                           as user_erstellt,
		  'flyway'                           as user_mutiert,
		  0                                  as version,
		  'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' as einstellung_key,
		  'true' 								as value,
		  UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
		  gp.id                              as gesuchsperiode_id,
		  UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;


-- Tagesschule Gemeinde Paris
INSERT INTO institution (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,name,status,mandant_id,traegerschaft_id,stammdaten_check_required,event_published) VALUES (UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b610', '-', '')),'2020-02-28 09:48:18','2020-02-28 10:11:35','flyway','flyway',0,null,'Tagesschule Paris','AKTIV', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')),null,false,true);
INSERT INTO adresse (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,gemeinde,hausnummer,land,organisation,ort,plz,strasse,zusatzzeile) VALUES (UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1937', '-', '')),'2020-02-28 09:48:18','2020-02-28 10:11:35','flyway','flyway',1,null,'1000-01-01','9999-12-31',null,'2','CH','Tageschule Paris','Paris','3000','Pariser Strasse',null);
INSERT INTO institution_stammdaten_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,gemeinde_id) VALUES (UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871', '-', '')),'2020-02-28 09:48:18','2020-02-28 09:48:18','flyway','flyway',0,UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')));
INSERT INTO institution_stammdaten (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,betreuungsangebot_typ,mail,telefon,webseite,adresse_id,institution_id,institution_stammdaten_ferieninsel_id,institution_stammdaten_tagesschule_id,send_mail_wenn_offene_pendenzen,institution_stammdaten_betreuungsgutscheine_id) VALUES (UNHEX(REPLACE('0f1c6b9e-37de-4c10-8ddc-9514fb840f5e', '-', '')),'2020-02-28 09:48:18','2020-02-28 09:48:18','flyway','flyway',0,null,'2020-08-01','9999-12-31','TAGESSCHULE','test@mailbucket.dvbern.ch',null,null,UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1937', '-', '')),UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b610', '-', '')),null,UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871', '-', '')),true,null);
INSERT INTO einstellungen_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,
                                       modul_tagesschule_typ,gesuchsperiode_id,institution_stammdaten_tagesschule_id,
                                       erlaeuterung)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
		  '2020-01-01 00:00:00'              as timestamp_erstellt,
		  '2020-01-01 00:00:00'              as timestamp_mutiert,
		  'flyway'                           as user_erstellt,
		  'flyway'                           as user_mutiert,
		  0                                  as version,
		  'DYNAMISCH' as  modul_tagesschule_typ,
		  gp.id   							 as gesuchsperiode_id,
		  UNHEX(REPLACE('0f763946-3a59-4aa6-9694-4754e58e8871','-', '')) as institution_stammdaten_tagesschule_id,
		  null as erlaeuterung
	  from gesuchsperiode as gp) as tmp;

# Tagis fuer Paris aktivvieren
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' as einstellung_key,
			 'true' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;


# Minimale Erwerbspensen fuer Paris ueberschreiben: 15%/30% anstatt 20%/40% fuer Vorschulkinder/EingeschulteKinder
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' as einstellung_key,
			 '15' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' as einstellung_key,
			 '30' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

INSERT INTO ebegu.gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde_id, gesuchsperiode_id, merkblatt_anmeldung_tagesschule_de, merkblatt_anmeldung_tagesschule_fr) VALUES
# PARIS
(UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null),
# LONDON
(UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', '')), UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null);

# PARIS
INSERT INTO ebegu.gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('54086b1a-6901-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('4ea68aa1-6901-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9c19b314-6900-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('36665051-6901-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')));
# LONDON
INSERT INTO ebegu.gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('a3e774d0-6903-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9ea7ae08-6903-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('90cb89be-6903-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9989a3f8-6903-11ea-bbf8-f4390979fa3e', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00', 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')));

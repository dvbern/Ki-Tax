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
SET @mandant_id_ar = UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-', ''));

SET @testgemeinde_solothurn_id = UNHEX(REPLACE('47c4b3a8-5379-11ec-98e8-f4390979fa3e', '-', ''));

SET @gesuchperiode_23_id = UNHEX(REPLACE('9bb4a798-3998-11ed-a63d-b05cda43de9c', '-', ''));
SET @gesuchperiode_22_23_id = UNHEX('30636536393134632D393530652D3131');
SET @testgemeinde_ar_id = UNHEX(REPLACE('b3e44f85-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @testgemeinde_ar_bfs_nr = 99995;
SET @traegerschaft_solothurn_id = UNHEX(REPLACE('c256ebf1-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @bruennen_id = UNHEX(REPLACE('caa83a6b-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @weissenstein_id = UNHEX(REPLACE('d0bb7d2a-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @tfo_id = UNHEX(REPLACE('d6c10415-3999-11ed-a63d-b05cda43de9c', '-', ''));
SET @ts_id = UNHEX(REPLACE('5c136a35-39a9-11ed-a63d-b05cda43de9c', '-', ''));

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
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_ar;
UPDATE application_property SET value = '15' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG', value = '15', mandant_id = @mandant_id_ar;
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

# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchperiode_23_id OR id = @gesuchperiode_22_23_id;


# Gemeinde Testgemeinde Appenzell Ausserrhoden erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT IGNORE INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published)
SELECT @testgemeinde_ar_id, '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
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
		UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')),
		@testgemeinde_ar_id, UNHEX(REPLACE('967b2041-39a8-11ed-a63d-b05cda43de9c', '-', '')),
		'herisau@mailbucket.dvbern.ch', '+41 31 930 15 15', 'https://www.herisau.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
		'Herisau Kontoinhaber', true, true, true, true, false, UNHEX(REPLACE('ae69aa8a-39a8-11ed-a63d-b05cda43de9c', '-', '')));

# Gesuchsperiode 22-23 Einstellung:
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'ASIV' WHERE einstellung_key = 'KINDERABZUG_TYP' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'ASIV' WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '18' WHERE einstellung_key = 'DAUER_BABYTARIF' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_TEXTE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZEMIS_DISABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '12.24' WHERE einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '6.11' WHERE einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0.78' WHERE einstellung_key = 'MIN_TARIF' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_KITA_MIN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'KLASSE1' WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '140' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '95' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '70' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '14' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '9.5' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '100001' WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '40000' WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '6' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '3' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '3800' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '6000' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '7000' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '7700' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '10.39' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '5.2' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '2019-09-15' WHERE einstellung_key = 'LATS_STICHTAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'EINGEWOEHNUNG_TYP' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'null' WHERE einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ANSPRUCH_MONATSWEISE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '5' WHERE einstellung_key = 'MINIMALDAUER_KONKUBINAT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'APPENZELL' WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'ABHAENGING' WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'KLASSE9' WHERE einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'FACHSTELLEN_TYP' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' WHERE einstellung_key = 'PENSUM_ANZEIGE_TYP' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ABWESENHEIT_AKTIV' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = 'KLASSE1' WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '6.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '51000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '3.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '70000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' AND gesuchsperiode_id = @gesuchperiode_22_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '3' WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'KITA_STUNDEN_PRO_TAG' AND gesuchsperiode_id = @gesuchperiode_22_23_id AND mandant_id =  null;

# Gesuchsperiode 23-24 Einstellungen :
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'ASIV' WHERE einstellung_key = 'KINDERABZUG_TYP' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'ASIV' WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '18' WHERE einstellung_key = 'DAUER_BABYTARIF' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_TEXTE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZEMIS_DISABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '12.24' WHERE einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '6.11' WHERE einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0.78' WHERE einstellung_key = 'MIN_TARIF' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'PARAM_PENSUM_KITA_MIN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'KLASSE1' WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '140' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '95' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '70' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '14' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '9.5' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '100001' WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '40000' WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '240' WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '6' WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '3' WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '40' WHERE einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '3800' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '6000' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '7000' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '7700' WHERE einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '01.08.2019' WHERE einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '10.39' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '5.2' WHERE einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '2019-09-15' WHERE einstellung_key = 'LATS_STICHTAG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'EINGEWOEHNUNG_TYP' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '100' WHERE einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'null' WHERE einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ANSPRUCH_MONATSWEISE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '30' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '60' WHERE einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '5' WHERE einstellung_key = 'MINIMALDAUER_KONKUBINAT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'APPENZELL' WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'ABHAENGING' WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'KLASSE9' WHERE einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'KEINE' WHERE einstellung_key = 'FACHSTELLEN_TYP' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' WHERE einstellung_key = 'PENSUM_ANZEIGE_TYP' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'ABWESENHEIT_AKTIV' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'true' WHERE einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'VORSCHULALTER' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '6.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '51000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '3.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '70000' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '20' WHERE einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = '0.00' WHERE einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id = @mandant_id_ar;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '0' WHERE einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = 'false' WHERE einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '7' WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '3' WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;
UPDATE einstellung set value = '10' WHERE einstellung_key = 'KITA_STUNDEN_PRO_TAG' AND gesuchsperiode_id = @gesuchperiode_23_id and mandant_id =  null;

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, mandant_id)
	VALUES (@traegerschaft_solothurn_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Appenzell Ausserrhoden', true, @mandant_id_ar);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@bruennen_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen AR',
	        @mandant_id_ar, @traegerschaft_solothurn_id, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@tfo_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Appenzell Ausserrhoden',
	        @mandant_id_ar, null, 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@weissenstein_id, '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein AR',
	        @mandant_id_ar, @traegerschaft_solothurn_id, 'AKTIV', false);

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
															   alterskategorie_kindergarten, alterskategorie_schule,
															   subventionierte_plaetze, anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr, auslastung_institutionen,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (UNHEX(REPLACE('331186b0-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('2bc7aafc-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('3909773b-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('276dd6ec-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
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
VALUES (UNHEX(REPLACE('3eb65ff2-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, UNHEX(REPLACE('22996c95-39a9-11ed-a63d-b05cda43de9c', '-', '')), FALSE, FALSE, FALSE,
		FALSE, FALSE, 40, NULL, '08:00', '18:00', 0, 0.00, 0.00, 0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (UNHEX(REPLACE('458bee8d-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
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
VALUES (UNHEX(REPLACE('51451c92-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
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
VALUES (UNHEX(REPLACE('56aa13db-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
		UNHEX(REPLACE('142bf33d-39a9-11ed-a63d-b05cda43de9c', '-', '')),
		@bruennen_id, NULL, NULL,
		UNHEX(REPLACE('3eb65ff2-39a9-11ed-a63d-b05cda43de9c', '-', '')), 'bruennen-ar@mailbucket.dvbern.ch', NULL, NULL);

update gemeinde set angebotts = false, angebotfi = false where bfs_nummer = @testgemeinde_ar_bfs_nr;

-- Tagesschule Gemeinde Paris
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								vorgaenger_id, name, status, mandant_id, traegerschaft_id, stammdaten_check_required,
								event_published)
VALUES (@ts_id, '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 0, NULL, 'Tagesschule Appenzell Ausserrhoden', 'AKTIV',
		@mandant_id_ar, NULL, FALSE, TRUE);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('648f4d9f-39a9-11ed-a63d-b05cda43de9c', '-', '')), '2020-02-28 09:48:18', '2020-02-28 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Tageschule Appenzell Ausserrhoden', 'Herisau', '9100',
		'Appenzell Ausserrhodener Strasse', NULL);

INSERT IGNORE INTO institution_stammdaten_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
													   user_mutiert, version, gemeinde_id)
VALUES (UNHEX(REPLACE('d75e306e-5393-11ec-98e8-f4390979fa3e', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
		'flyway', 'flyway', 0, @testgemeinde_ar_id);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, mail,
										   telefon, webseite, adresse_id, institution_id,
										   institution_stammdaten_ferieninsel_id, institution_stammdaten_tagesschule_id,
										   send_mail_wenn_offene_pendenzen,
										   institution_stammdaten_betreuungsgutscheine_id)
VALUES (UNHEX(REPLACE('f89d4bf4-39aa-11ed-a63d-b05cda43de9c', '-', '')), '2020-02-28 09:48:18', '2020-02-28 09:48:18',
		'flyway', 'flyway', 0, NULL, '2020-08-01', '9999-12-31', 'TAGESSCHULE', 'tagesschule-ar@mailbucket.dvbern.ch', NULL, NULL,
		UNHEX(REPLACE('648f4d9f-39a9-11ed-a63d-b05cda43de9c', '-', '')), @ts_id, NULL,
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
	  from gesuchsperiode as gp where gp.mandant_id = @mandant_id_ar) as tmp;

INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															 user_mutiert, version, gemeinde_id, gesuchsperiode_id,
															 merkblatt_anmeldung_tagesschule_de,
															 merkblatt_anmeldung_tagesschule_fr)
VALUES
# LONDON
	(UNHEX(REPLACE('073fcdda-39ab-11ed-a63d-b05cda43de9c', '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, @testgemeinde_ar_id,
	 @gesuchperiode_23_id, NULL, NULL);

# LONDON
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert,
																		 user_erstellt, user_mutiert, version,
																		 vorgaenger_id, anmeldeschluss, ferienname,
																		 gemeinde_stammdaten_gesuchsperiode_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
		'flyway', 'flyway', 0, NULL, '2019-09-01', 'HERBSTFERIEN',
		UNHEX(REPLACE('073fcdda-39ab-11ed-a63d-b05cda43de9c', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-06-01', 'SOMMERFERIEN',
	 UNHEX(REPLACE('073fcdda-39ab-11ed-a63d-b05cda43de9c', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-01-01', 'SPORTFERIEN',
	 UNHEX(REPLACE('073fcdda-39ab-11ed-a63d-b05cda43de9c', '-', ''))),
	(UNHEX(REPLACE(UUID(), '-', '')), '2020-03-18 00:00:00', '2020-03-18 00:00:00',
	 'flyway', 'flyway', 0, NULL, '2019-04-01', 'FRUEHLINGSFERIEN',
	 UNHEX(REPLACE('073fcdda-39ab-11ed-a63d-b05cda43de9c', '-', '')));

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								 vorgaenger_id, name, status, mandant_id)
VALUES (UNHEX(REPLACE('1653a0c7-39ab-11ed-a63d-b05cda43de9c', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 0, NULL, 'Appenzell Ausserrhodener Sozialdienst', 'AKTIV',
		@mandant_id_ar);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
							strasse, zusatzzeile)
VALUES (UNHEX(REPLACE('1bfb8920-39ab-11ed-a63d-b05cda43de9c', '-', '')), '2021-02-15 09:48:18', '2021-02-15 10:11:35',
		'flyway', 'flyway', 1, NULL, '1000-01-01', '9999-12-31', NULL, '2', 'CH', 'Appenzell Ausserrhoden Sozialdienst', 'Herisau', '9100',
		'Sozialdienst Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
											version, vorgaenger_id, mail, telefon, webseite, adresse_id,
											sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), '2021-02-15 09:48:18', '2021-02-15 09:48:18',
		'flyway', 'flyway', 0, NULL, 'sozialdienst-ar@mailbucket.dvbern.ch', '078 898 98 98', 'http://sodialdienst-ar.dvbern.ch',
		UNHEX(REPLACE('1bfb8920-39ab-11ed-a63d-b05cda43de9c', '-', '')),
		UNHEX(REPLACE('1653a0c7-39ab-11ed-a63d-b05cda43de9c', '-', '')));

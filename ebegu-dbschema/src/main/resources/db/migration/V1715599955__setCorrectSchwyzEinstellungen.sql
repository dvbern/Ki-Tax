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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));

# Abhängigkeit des Anspruchs vom Beschäftigungspensum (Abhaengig, Unabhaengig, Minimum)
UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);

# Angabe der Sozialversicherungsnummer je Antragsteller/in
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'SOZIALVERSICHERUNGSNUMMER_PERIODE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Anspruch ab (in Monaten)
UPDATE einstellung
SET value = '3'
WHERE einstellung_key = 'ANSPRUCH_AB_X_MONATEN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Anspruchsberechnung monatsweise
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'ANSPRUCH_MONATSWEISE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Antrag beenden, wenn der Gesuchsteller 2 innerhalb der Periode ändert
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'GESUCH_BEENDEN_BEI_TAUSCH_GS2' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Anwesenheitstage bei Tagesfamilienbetreuungen aktivieren
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Betreuungsgutscheine bis und mit Schulstufe
UPDATE einstellung
SET value = 'PRIMARSTUFE'
WHERE einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Betroffene Angebote fuer die SchulstufeCalcRule
UPDATE einstellung
SET value = 'KITA,TAGESFAMILIEN,MITTAGSTISCH'
WHERE einstellung_key = 'ANGEBOT_SCHULSTUFE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Bis zu welchem Alter (in Monaten) soll der Babytarif gewährt werden
UPDATE einstellung
SET value = '18'
WHERE einstellung_key = 'DAUER_BABYTARIF' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Erweitere Bedürfnisse Frage aktivieren
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'ERWEITERTE_BEDUERFNISSE_AKTIV' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Fachstellen Typ
UPDATE einstellung
SET value = 'KEINE'
WHERE einstellung_key = 'FACHSTELLEN_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# FKJV: Neue Familiensituation
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# FKJV: Pauschale auch rückwirkend ausbezahlen, sofern Anspruch vorhanden
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# FKJV: Pauschale nur möglich, wenn Anspruch auf Gutschein
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# FKJV: Textänderungen
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'FKJV_TEXTE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# GESUCHFREIGABE_ONLINE
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'GESUCHFREIGABE_ONLINE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Höhere Beiträge Beeinträchtigung aktiviert
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Geschwisternbonustyp (LUZERN, SCHWYZ, NONE)
UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'GESCHWISTERNBONUS_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# ID Nachweis für Antragstellende verlangt
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# KESB-Platzierung deaktivieren
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Kinderabzugtyp (FKJV, FKJV_2, ASIV oder KEINE)
UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'KINDERABZUG_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Kita / schulische Tagesstruktur Stunden pro Tag
UPDATE einstellung
SET value = '10'
WHERE einstellung_key = 'KITA_STUNDEN_PRO_TAG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Kontingentierung aktiviert
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Luzern: Ausserordentlicher Betreuungsaufwand konfigurierbar
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Luzern: Frage Diplomatenstatus deaktiviert
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Luzern: Frage Sprache Amtsprache deaktiviert
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Luzern: ZEMIS Nr. deaktiviert
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'ZEMIS_DISABLED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Maximal Massgebendes Einkommen
UPDATE einstellung
SET value = '153215'
WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Maximale Vergünstigung für Vorschulkinder (pro Stunde)
UPDATE einstellung
SET value = '9'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Maximale Vergünstigung für Vorschulkinder (pro Tag)
UPDATE einstellung
SET value = '130'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Maximale Vergünstigung für Vorschulkinder im Babytarif (pro Stunde)
UPDATE einstellung
SET value = '12'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Maximale Vergünstigung für Vorschulkinder im Babytarif (pro Tag)
UPDATE einstellung
SET value = '185'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Min Pensum Kitas / schulische Tagesstrukturen
UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'PARAM_PENSUM_KITA_MIN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Min Pensum Tageseltern
UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Min Pensum Tagesschule
UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Minimal Massgebendes Einkommen
UPDATE einstellung
SET value = '47193'
WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Minimale Einkommensverschlechterung (%)
UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);

# Minimaler Elternbeitrag pro Stunde (Tagesfamilien)
UPDATE einstellung
SET value = '3'
WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Minimaler Elternbeitrag pro Tag (Kitas)
UPDATE einstellung
SET value = '30'
WHERE einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Minimales Erwerbspensum, wenn das Kind eingeschult ist
UPDATE einstellung
SET value = '20'
WHERE einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
UPDATE einstellung
SET value = '20'
WHERE einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Mutationsbegründungsfeld aktiv
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Öffnungsstunden Tagesfamilien
UPDATE einstellung
SET value = '10'
WHERE einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Öffnungstage KITA
UPDATE einstellung
SET value = '246'
WHERE einstellung_key = 'OEFFNUNGSTAGE_KITA' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Öffnungstage Tagesfamilien
UPDATE einstellung
SET value = '246'
WHERE einstellung_key = 'OEFFNUNGSTAGE_TFO' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Pensum Anzeige Typ
UPDATE einstellung
SET value = 'ZEITEINHEIT_UND_PROZENT'
WHERE einstellung_key = 'PENSUM_ANZEIGE_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Schnittstelle zu den Steuersystemen aktiv
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Schulergänzende Betreuungen aktiviert
UPDATE einstellung
SET value = 'true'
WHERE einstellung_key = 'SCHULERGAENZENDE_BETREUUNGEN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Solothurn, Luzern: Gemeindespezifische Konfigurationen für BG
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Solothurn: Einlesen Freigabequittung aktiviert
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Typ der Eingewöhung (FKJV, LUZERN, KEINE)
UPDATE einstellung
SET value = 'KEINE'
WHERE einstellung_key = 'EINGEWOEHNUNG_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Typ der finanziellen Situation
UPDATE einstellung
SET value = 'SCHWYZ'
WHERE einstellung_key = 'FINANZIELLE_SITUATION_TYP' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Unbezahlter Urlaub aktiv
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Verfügung eingeschrieben Versenden aktiv
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Verfügung Export aktiviert
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Welche ausserordentlicher Anspruch Rule soll gelten
UPDATE einstellung
SET value = 'KEINE'
WHERE einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Wizardstep Abwesenheit sichtbar
UPDATE einstellung
SET value = 'false'
WHERE einstellung_key = 'ABWESENHEIT_AKTIV' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Zuschlag, um den der Anspruch aufgrund des Erwerbspensums automatisch erhöht wird
UPDATE einstellung
SET value = '0'
WHERE einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' AND
	  gesuchsperiode_id IN (SELECT id FROM gesuchsperiode WHERE mandant_id = @mandant_id_schwyz);


# Abweichungen aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'ABWEICHUNGEN_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Aktivierte Demo Features
UPDATE application_property
SET value = ''
WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @mandant_id_schwyz;


# Angebot Ferieninseln aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Angebot Mittagstisch aktiviert
UPDATE application_property
SET value = 'true'
WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Angebot Tagesfamilienorganisationen aktiviert
UPDATE application_property
SET value = 'true'
WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Angebot Tagesschulen aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Anzahl Tage bis Löschung nach Warnung Freigabe
UPDATE application_property
SET value = '180'
WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_schwyz;


# Anzahl Tage bis Warnung Freigabe
UPDATE application_property
SET value = '30'
WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @mandant_id_schwyz;


# Auszahlung an Eltern
UPDATE application_property
SET value = 'true'
WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @mandant_id_schwyz;



# Checkbox Auszahlen in Zukunft einblenden
UPDATE application_property
SET value = 'true'
WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @mandant_id_schwyz;


# ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN
UPDATE application_property
SET value = 'false'
WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @mandant_id_schwyz;


# Evaluator-Debug aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Ferienbetreuung aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @mandant_id_schwyz;


# Französische Übersetzungen aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'FRENCH_ENABLED' AND mandant_id = @mandant_id_schwyz;


# Gemeinde Kennzahlen aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @mandant_id_schwyz;


# Infoma Zahlungen
UPDATE application_property
SET value = 'false'
WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @mandant_id_schwyz;


# Institutionen durch Gemeinden einladen
UPDATE application_property
SET value = 'false'
WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @mandant_id_schwyz;


# Notverordnung Phase 2 aktiv
UPDATE application_property
SET value = 'false'
WHERE name = 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV' AND mandant_id = @mandant_id_schwyz;


# Lastenausgleich BG aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @mandant_id_schwyz;


# Lastenausgleich Tagesschulen aktiviert
UPDATE application_property
SET value = 'false'
WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @mandant_id_schwyz;


# Logo File Name
UPDATE application_property
SET value = 'logo-kibon-schwyz.svg'
WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_schwyz;


# Logo White File Name
UPDATE application_property
SET value = 'logo-kibon-white-schwyz.svg'
WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_schwyz;


# Farbe Primary
UPDATE application_property
SET value = '#ee1d23'
WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_schwyz;


# Farbe Primary Dark
UPDATE application_property
SET value = '#BF0425'
WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_schwyz;


# Farbe Primary Light
UPDATE application_property
SET value = '#F0C3CB'
WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_schwyz;


# Schnittstelle zu Steuersystemen aktiv ab
UPDATE application_property
SET value = 'false'
WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' AND mandant_id = @mandant_id_schwyz;


# Dateitypen-Whitelist
UPDATE application_property
SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf,application/vnd.ms-outlook,application/zip,application/x-zip-compressed'
WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @mandant_id_schwyz;


# Zusatzinformationen Institution
UPDATE application_property
SET value = 'false'
WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @mandant_id_schwyz;

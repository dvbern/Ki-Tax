/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums;

/**
 * Keys fuer die Application Properties die wir in der DB speichern
 */
public enum ApplicationPropertyKey {

	/**
	 * Wenn true gibt der Evaluator seine Debugmeldungen in das log aus.
	 */
	EVALUATOR_DEBUG_ENABLED,

	/**
	 * Damit wir Test/Produktion leichter unterscheiden koennen kann man die Hintergrundfarbe einstellen
	 */
	BACKGROUND_COLOR,

	/**
	 * Anzahl Tage nach Erstellungsdatum bis der GS gewarnt wird, wenn er nicht freigibt
	 */
	ANZAHL_TAGE_BIS_WARNUNG_FREIGABE,

	/**
	 * Anzahl Tage nach Freigabe bis der GS gewarnt wird, wenn er Quittung nicht schickt
	 */
	ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG,

	/**
	 * Anzahl Tage nach Warnung bis Gesuch geloescht wird, wenn er nicht freigibt
	 */
	ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE,

	/**
	 * Anzahl Tage nach Warnung bis Gesuch geloescht wird, wenn er Quittung nicht schickt
	 */
	ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG,

	/**
	 * Key fuer Komma separierte Whiteliste von zugelassenen Filetypen fuer den upload
	 *
	 */
	UPLOAD_FILETYPES_WHITELIST,

	/**
	 * Flag, ob das Dummy Login eingeschaltet ist. Aus Sicherheitsgruenden muss sowohl dieses wie auch das entsprechende
	 * System-Property eingeschaltet sein, damit das Dummy Login funktioniert.
	 */
	DUMMY_LOGIN_ENABLED,

	/**
	 * Gibt das Sentry DNS Token zurueck
	 */
	SENTRY_ENV,

	/**
	 * Ab diesem Datum gelten fuer die Stadt Bern die ASIV Regeln
	 */
	STADT_BERN_ASIV_START_DATUM,

	/**
	 * Wenn TRUE koennen die Zeitraeume ab ASIV_START_DATUM verfuegt werden
	 */
	STADT_BERN_ASIV_CONFIGURED,

	/**
	 * Wenn TRUE ist die zweite Phase aktiv
	 */
	KANTON_NOTVERORDNUNG_PHASE_2_AKTIV,

	/**
	 * Default Datum bis wann die öffentlichen Gesuche der Notverordnung einreicht werden können
	 */
	NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH,
	/**
	 * Default Datum bis wann die privaten Gesuche der Notverordnung einreicht werden können
	 */
	NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT,
	/**
	 * Wenn TRUE sind Ferienbetreuungen aktiviert
	 */
	FERIENBETREUUNG_AKTIV,
	/**
	 * Wenn TRUE ist Lastenausgleich Tagesschulen aktiviert
	 */
	LASTENAUSGLEICH_TAGESSCHULEN_AKTIV,
	/**
	 * Wenn TRUE ist Gemeinde Kennzahlen aktiviert
	 */
	GEMEINDE_KENNZAHLEN_AKTIV,

	/**
	 * Setzt fest, was für ein Anteil der LATS Anträge der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, ab welcher Anzahl Betreuungsstunden der LATS Antrag der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, was für ein Anteil der LATS Anträge der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR,

	/**
	 * Setzt fest, ab welcher Anzahl Betreuungsstunden der LATS Antrag der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR,

	/**
	 * True, wenn Lastenausgleich BG aktiv
	 */
	LASTENAUSGLEICH_AKTIV,

	/**
	 * Legt die Primary Color fest
	 */
	PRIMARY_COLOR,

	/**
	 * Legt die dunkle Primary Color fest
	 */
	PRIMARY_COLOR_DARK,

	/**
	 * Legt die helle Primary Color fest
	 */
	PRIMARY_COLOR_LIGHT,

	/**
	 * Filename des Logos, welches in /assets/images/ ordner abgelegt ist
	 */
	LOGO_FILE_NAME,

	/**
	 * Filename des weissen Logos, welches in /assets/images/ ordner abgelegt ist
	 */
	LOGO_WHITE_FILE_NAME,

	/**
	 * Bestimmt, ob Multimandant für diese kiBon Instanz aktiviert sein soll
	 */
	MULTIMANDANT_AKTIV,

	/**
	 * Falls das Luzerner Zahlungssystem verwendet wird (Auszahlungen an Eltern, Infoma) ist dieses Flag true
	 */
	INFOMA_ZAHLUNGEN,

	/**
	 * Sind die französischen Übersetzungen verfügbar
	 */
	FRENCH_ENABLED,

	/**
	 * Ist Geres verfügbar
	 */
	GERES_ENABLED_FOR_MANDANT,

	/*
	 * Wenn dieses Datum überschritten wird, ist Steuerschnittstelle aktiv. Ansonste wird eine Warnung gezeigt.
	 */
	SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB,

	/*
	 * Bestimmte Informationen bei den Institutionen sind nicht zwingend nötig für alle Mandanten
	 */
	ZUSATZINFORMATIONEN_INSTITUTION,
	/**
	 * Wenn TRUE koennen die Schnittstelle events z.B. AnmeldungTagesschuleEvent, BetreuungAnfrageAddedEvent
	 * werden veröffentlicht
	 */
	SCHNITTSTELLE_EVENTS_AKTIVIERT,

	/**
	 * Falls true wird eine Checkbox bei den Zahlungen angezeigt, mit der die Auszahlungen in der Zukunft
	 * ausbezahlt werden können
	 */
	CHECKBOX_AUSZAHLEN_IN_ZUKUNFT,

	/**
	 * Einige Features sollen in der Produktion noch ausgeblendet werden. Auf den Testungebungen können diese Features
	 * mit dieser Einstellung aktiviert werden. Eine Liste aller möglichen Features sind in TSDemoFeature.ts zu finden.
	 */
	ACTIVATED_DEMO_FEATURES,
	/**
	 * Falls aktiv, können Gemeinden durch die Institutionen eingeladen werden. Diese werden direkt mit der Gemeinde
	 * verknüpft
	 */
	INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN,

	/**
	 * Falls aktiv, können Gemeinden die Institutionen Wahl begrenzen. Nur die Gewaehlte Institutionen sind dann
	 * im Antrag Prozess waehlbar
	 */
	ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN,

	/**
	 * Ist für den Mandanten das Angebot TS aktiviert
	 */
	ANGEBOT_TS_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot TFO aktiviert
	 */
	ANGEBOT_TFO_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot FI aktiviert
	 */
	ANGEBOT_FI_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot Mittagstisch aktiviert
	 */
	ANGEBOT_MITTAGSTISCH_ENABLED,

	/*
	 * Wenn dieses Datum überschritten wird, ist die SprachfoerderungBestaegit Flag Wert beruecksichtig.
	 */
	SCHNITTSTELLE_SPRACHFOERDERUNG_AKTIV_AB;

}

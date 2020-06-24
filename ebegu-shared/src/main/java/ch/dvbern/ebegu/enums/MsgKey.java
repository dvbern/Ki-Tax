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
 * Dieses Enum dient der Verwaltung von Server Seitigen Uebersetzbaren Messages. Die hier definierten keys sollten im
 * server-messages.properties file uebersetzt werden.
 * Achtung:
 * - Die Reihenfolge im Enum definiert die Reihenfolge, in der die Bemerkungen auf der Verfuegung angezeigt werden
 * - Pro MsgKey kann nur 1 Bemerkung vorhanden sein. Wenn mehrere eingefuegt werden, wird die letzte behalten. Man muss die
 * 		Regeln also in der korrekten Reihenfolge ausfuehren, mit aufsteigender Prioritaet.
 */
public enum MsgKey {

	ERWERBSPENSUM_GS1_MSG,
	ERWERBSPENSUM_GS2_MSG,
	ERWERBSPENSUM_ANSPRUCH,
	ERWERBSPENSUM_KEIN_ANSPRUCH,
	ERWERBSPENSUM_FREIWILLIGENARBEIT,
	BETREUUNGSANGEBOT_MSG,
	EINKOMMEN_MSG,
	EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
	EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
	EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
	EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG,
	UNBEZAHLTER_URLAUB_MSG,

	ABWESENHEIT_MSG,
	BETREUUNG_VOR_BEGU_START,

	WOHNSITZ_MSG,
	FACHSTELLE_MSG,
	FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG,
	AUSSERORDENTLICHER_ANSPRUCH_MSG,
	EINREICHUNGSFRIST_MSG,
	RESTANSPRUCH_MSG,
	REDUCKTION_RUECKWIRKEND_MSG,
	ANSPRUCHSAENDERUNG_MSG,

	STORNIERT_MSG,
	FAMILIENSITUATION_HEIRAT_MSG,
	FAMILIENSITUATION_TRENNUNG_MSG,
	FAMILIENSITUATION_KONKUBINAT_MSG,

	SCHULSTUFE_VORSCHULE_MSG,
	SCHULSTUFE_KINDERGARTEN_1_MSG,
	SCHULSTUFE_KINDERGARTEN_2_MSG,

	KESB_PLATZIERUNG_MSG,
	ERWEITERTE_BEDUERFNISSE_MSG,

	ZUSATZGUTSCHEIN_JA_KITA,
	ZUSATZGUTSCHEIN_JA_TFO,
	ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE,
	ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE,
	ZUSATZGUTSCHEIN_NEIN_SCHULSTUFE,

	FEBR_INFO,
	FEBR_INFO_ASIV_NOT_CONFIGUERD,
	FEBR_BETREUUNG_NICHT_IN_BERN,
	NO_MATCHING_FROM_KITAX,

	MAHLZEITENVERGUENSTIGUNG_BG,
	MAHLZEITENVERGUENSTIGUNG_TS
}

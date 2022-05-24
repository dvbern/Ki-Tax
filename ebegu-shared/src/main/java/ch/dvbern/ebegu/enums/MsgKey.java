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

	VERFUEGUNG_MIT_ANSPRUCH,
	VERFUEGUNG_MIT_ANSPRUCH_FKJV,

	ERWERBSPENSUM_GS1_MSG,
	ERWERBSPENSUM_GS2_MSG,
	ERWERBSPENSUM_ANSPRUCH,
	ERWERBSPENSUM_ANSPRUCH_FKJV,
	ERWERBSPENSUM_KEIN_ANSPRUCH,
	ERWERBSPENSUM_KEIN_ANSPRUCH_FKJV,
	ERWERBSPENSUM_FREIWILLIGENARBEIT,
	ERWERBSPENSUM_EINGEWOEHNUNG,
	BETREUUNGSANGEBOT_MSG,
	EINKOMMEN_MAX_MSG,
	EINKOMMEN_MAX_MSG_FKJV,
	EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG,
	EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG,
	EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG_FKJV,
	EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG,
	EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG_FKJV,
	EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
	EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG_FKJV,
	EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
	EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG_FKJV,
	EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
	EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG_FKJV,
	EINKOMMEN_TOO_HIGH_FOR_EKV,
	EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG,
	EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG_FKJV,
	UNBEZAHLTER_URLAUB_MSG,
	UNBEZAHLTER_URLAUB_MSG_FKJV,

	ABWESENHEIT_MSG,
	ABWESENHEIT_MSG_FKJV,
	BETREUUNG_VOR_BEGU_START,

	WOHNSITZ_MSG,
	WOHNSITZ_MSG_FKJV,
	FACHSTELLE_MSG,
	FACHSTELLE_MSG_FKJV,
	FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG,
	FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG_FKJV,
	AUSSERORDENTLICHER_ANSPRUCH_MSG,
	AUSSERORDENTLICHER_ANSPRUCH_MSG_FKJV,
	KEIN_AUSSERORDENTLICHER_ANSPRUCH_MSG,
	EINREICHUNGSFRIST_MSG,
	EINREICHUNGSFRIST_MSG_FKJV,
	RESTANSPRUCH_MSG,
	REDUCKTION_RUECKWIRKEND_MSG,
	REDUCKTION_RUECKWIRKEND_MSG_FKJV,
	ANSPRUCHSAENDERUNG_MSG,
	ANSPRUCHSAENDERUNG_MSG_FKJV,

	STORNIERT_MSG,
	FAMILIENSITUATION_HEIRAT_MSG,
	FAMILIENSITUATION_TRENNUNG_MSG,
	FAMILIENSITUATION_KONKUBINAT_MSG,

	SCHULSTUFE_VORSCHULE_MSG,
	SCHULSTUFE_KINDERGARTEN_1_MSG,
	SCHULSTUFE_KINDERGARTEN_2_MSG,
	SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG,
	SCHULSTUFE_KINDERGARTEN_2_MSG_FKJV,

	KESB_PLATZIERUNG_MSG,
	ERWEITERTE_BEDUERFNISSE_MSG,
	ERWEITERTE_BEDUERFNISSE_MSG_FKJV,
	KEINE_ERWEITERTE_BEDUERFNISSE_MSG, //Zu diesem Key existiert keine Übersetztung, er wird nur zum Überschreibe verwenet (@see VerfuegungsBemerkungDTOList#removeNotRequiredBemerkungen())

	ZUSATZGUTSCHEIN_JA_KITA,
	ZUSATZGUTSCHEIN_JA_TFO,
	ZUSATZGUTSCHEIN_NEIN_NICHT_IN_GEMEINDE,
	ZUSATZGUTSCHEIN_NEIN_SOZIALHILFE,
	ZUSATZGUTSCHEIN_NEIN_SCHULSTUFE,

	BABYGUTSCHEIN_JA_KITA,
	BABYGUTSCHEIN_JA_TFO,
	BABYGUTSCHEIN_NEIN_SOZIALHILFE,

	FEBR_INFO,
	FEBR_INFO_ASIV_NOT_CONFIGUERD,
	FEBR_BETREUUNG_NICHT_IN_BERN,
	NO_MATCHING_FROM_KITAX,

	MAHLZEITENVERGUENSTIGUNG_BG,
	MAHLZEITENVERGUENSTIGUNG_TS,
	MAHLZEITENVERGUENSTIGUNG_BG_NEIN,
	MAHLZEITENVERGUENSTIGUNG_TS_NEIN,

	KITAPLUS_ZUSCHLAG,
	GESCHWSTERNBONUS_KIND_2,
	GESCHWSTERNBONUS_KIND_3

}

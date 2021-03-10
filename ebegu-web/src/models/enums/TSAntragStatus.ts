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

import {TSEingangsart} from './TSEingangsart';
import {TSRole} from './TSRole';

export enum TSAntragStatus {
    IN_BEARBEITUNG_GS = 'IN_BEARBEITUNG_GS',
    IN_BEARBEITUNG_SOZIALDIENST = 'IN_BEARBEITUNG_SOZIALDIENST',
    FREIGABEQUITTUNG = 'FREIGABEQUITTUNG',
    NUR_SCHULAMT = 'NUR_SCHULAMT',
    FREIGEGEBEN = 'FREIGEGEBEN',
    ERSTE_MAHNUNG = 'ERSTE_MAHNUNG',
    ERSTE_MAHNUNG_ABGELAUFEN = 'ERSTE_MAHNUNG_ABGELAUFEN',
    ZWEITE_MAHNUNG = 'ZWEITE_MAHNUNG',
    ZWEITE_MAHNUNG_ABGELAUFEN = 'ZWEITE_MAHNUNG_ABGELAUFEN',
    IN_BEARBEITUNG_JA = 'IN_BEARBEITUNG_JA',
    GEPRUEFT = 'GEPRUEFT',
    KEIN_KONTINGENT = 'KEIN_KONTINGENT',
    PLATZBESTAETIGUNG_ABGEWIESEN = 'PLATZBESTAETIGUNG_ABGEWIESEN',
    PLATZBESTAETIGUNG_WARTEN = 'PLATZBESTAETIGUNG_WARTEN',
    VERFUEGEN = 'VERFUEGEN',
    VERFUEGT = 'VERFUEGT',
    KEIN_ANGEBOT = 'KEIN_ANGEBOT',
    BESCHWERDE_HAENGIG = 'BESCHWERDE_HAENGIG',
    PRUEFUNG_STV = 'PRUEFUNG_STV',
    IN_BEARBEITUNG_STV = 'IN_BEARBEITUNG_STV',
    GEPRUEFT_STV = 'GEPRUEFT_STV'
}

export const IN_BEARBEITUNG_BASE_NAME = 'IN_BEARBEITUNG';

export function getTSAntragStatusValues(): Array<TSAntragStatus> {
    return [
        TSAntragStatus.IN_BEARBEITUNG_GS,
        TSAntragStatus.FREIGABEQUITTUNG,
        TSAntragStatus.NUR_SCHULAMT,
        TSAntragStatus.FREIGEGEBEN,
        TSAntragStatus.ERSTE_MAHNUNG,
        TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN,
        TSAntragStatus.ZWEITE_MAHNUNG,
        TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN,
        TSAntragStatus.IN_BEARBEITUNG_JA,
        TSAntragStatus.GEPRUEFT,
        TSAntragStatus.KEIN_KONTINGENT,
        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN,
        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN,
        TSAntragStatus.VERFUEGEN,
        TSAntragStatus.VERFUEGT,
        TSAntragStatus.KEIN_ANGEBOT,
        TSAntragStatus.BESCHWERDE_HAENGIG,
        TSAntragStatus.PRUEFUNG_STV,
        TSAntragStatus.IN_BEARBEITUNG_STV,
        TSAntragStatus.GEPRUEFT_STV,
    ];
}

/**
 * Alle Status die eine gewisse Rolle sehen darf
 */
export function getTSAntragStatusValuesByRole(userrole: TSRole): Array<TSAntragStatus> {
    switch (userrole) {
        case TSRole.STEUERAMT:
            return [
                TSAntragStatus.PRUEFUNG_STV,
                TSAntragStatus.IN_BEARBEITUNG_STV,
            ];
        case TSRole.SACHBEARBEITER_TS:
        case TSRole.ADMIN_TS:
        case TSRole.SACHBEARBEITER_BG:
        case TSRole.ADMIN_BG:
        case TSRole.SACHBEARBEITER_GEMEINDE:
        case TSRole.ADMIN_GEMEINDE:
        case TSRole.REVISOR:
        case TSRole.JURIST:
        case TSRole.ADMIN_MANDANT:
        case TSRole.SACHBEARBEITER_MANDANT:
            return getTSAntragStatusValues().filter(element => (element !== TSAntragStatus.IN_BEARBEITUNG_GS
                && element !== TSAntragStatus.FREIGABEQUITTUNG));
        case TSRole.ADMIN_INSTITUTION:
        case TSRole.SACHBEARBEITER_INSTITUTION:
        case TSRole.ADMIN_TRAEGERSCHAFT:
        case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
            return getTSAntragStatusValues().filter(element => (element !== TSAntragStatus.PRUEFUNG_STV
                && element !== TSAntragStatus.IN_BEARBEITUNG_STV && element !== TSAntragStatus.GEPRUEFT_STV));
        default:
            return getTSAntragStatusValues();
    }
}

/**
 * Gibt alle Werte zurueck ausser VERFUEGT und KEIN_ANGEBOT.
 * Diese Werte sind die, die bei der Pendenzenliste notwendig sind
 */
export function getTSAntragStatusPendenzValues(userrole: TSRole): TSAntragStatus[] {
    const allVisibleValuesByRole = getTSAntragStatusValuesByRole(userrole);
    switch (userrole) {
        case TSRole.SACHBEARBEITER_BG:
        case TSRole.ADMIN_BG:
        case TSRole.SACHBEARBEITER_GEMEINDE:
        case TSRole.ADMIN_GEMEINDE:
        case TSRole.REVISOR:
        case TSRole.JURIST:
        case TSRole.ADMIN_MANDANT:
        case TSRole.SACHBEARBEITER_MANDANT:
            return allVisibleValuesByRole.filter(element => (
                element !== TSAntragStatus.VERFUEGT
                && element !== TSAntragStatus.KEIN_ANGEBOT
                && element !== TSAntragStatus.NUR_SCHULAMT
                && element !== TSAntragStatus.KEIN_KONTINGENT
                && element !== TSAntragStatus.IN_BEARBEITUNG_STV
                && element !== TSAntragStatus.PRUEFUNG_STV));
        case TSRole.SACHBEARBEITER_TS:
        case TSRole.ADMIN_TS:
            return allVisibleValuesByRole.filter(element => (
                element !== TSAntragStatus.VERFUEGT
                && element !== TSAntragStatus.KEIN_ANGEBOT
                && element !== TSAntragStatus.NUR_SCHULAMT
                && element !== TSAntragStatus.VERFUEGEN
                && element !== TSAntragStatus.KEIN_KONTINGENT
                && element !== TSAntragStatus.IN_BEARBEITUNG_STV
                && element !== TSAntragStatus.PRUEFUNG_STV));
        default:
            return allVisibleValuesByRole.filter(element => (element !== TSAntragStatus.VERFUEGT
                && element !== TSAntragStatus.KEIN_ANGEBOT && element !== TSAntragStatus.NUR_SCHULAMT));
    }
}

export function isAtLeastFreigegeben(status: TSAntragStatus): boolean {
    const validStates: Array<TSAntragStatus> = [
        TSAntragStatus.NUR_SCHULAMT,
        TSAntragStatus.FREIGEGEBEN,
        TSAntragStatus.ERSTE_MAHNUNG,
        TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN,
        TSAntragStatus.ZWEITE_MAHNUNG,
        TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN,
        TSAntragStatus.IN_BEARBEITUNG_JA,
        TSAntragStatus.KEIN_KONTINGENT,
        TSAntragStatus.GEPRUEFT,
        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN,
        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN,
        TSAntragStatus.VERFUEGEN,
        TSAntragStatus.VERFUEGT,
        TSAntragStatus.KEIN_ANGEBOT,
        TSAntragStatus.BESCHWERDE_HAENGIG,
        TSAntragStatus.PRUEFUNG_STV,
        TSAntragStatus.IN_BEARBEITUNG_STV,
        TSAntragStatus.GEPRUEFT_STV,
    ];
    return validStates.indexOf(status) !== -1;
}

export function isAtLeastFreigegebenOrFreigabequittung(status: TSAntragStatus): boolean {
    return isAtLeastFreigegeben(status) || status === TSAntragStatus.FREIGABEQUITTUNG;
}

export function isAnyStatusOfVerfuegt(status: TSAntragStatus): boolean {
    return status === TSAntragStatus.NUR_SCHULAMT
        || status === TSAntragStatus.VERFUEGT
        || status === TSAntragStatus.BESCHWERDE_HAENGIG
        || status === TSAntragStatus.PRUEFUNG_STV
        || status === TSAntragStatus.IN_BEARBEITUNG_STV
        || status === TSAntragStatus.GEPRUEFT_STV
        || status === TSAntragStatus.KEIN_ANGEBOT;
}

// KeinKontingent darf zwar nicht zu den "verfuegt" Status hinzugefuegt werden, wird
// aber fuer gewissen Validierungen gleich behandelt
export function isAnyStatusOfVerfuegtOrKeinKontingent(status: TSAntragStatus): boolean {
    return isAnyStatusOfVerfuegt(status) || status === TSAntragStatus.KEIN_KONTINGENT;
}

export function isAnyStatusOfVerfuegtButSchulamt(status: TSAntragStatus): boolean {
    return status === TSAntragStatus.VERFUEGT
        || status === TSAntragStatus.BESCHWERDE_HAENGIG
        || status === TSAntragStatus.PRUEFUNG_STV
        || status === TSAntragStatus.IN_BEARBEITUNG_STV
        || status === TSAntragStatus.GEPRUEFT_STV
        || status === TSAntragStatus.KEIN_ANGEBOT;
}

export function isVerfuegtOrSTV(status: TSAntragStatus): boolean {
    return status === TSAntragStatus.VERFUEGT
        || status === TSAntragStatus.PRUEFUNG_STV
        || status === TSAntragStatus.IN_BEARBEITUNG_STV
        || status === TSAntragStatus.GEPRUEFT_STV
        || status === TSAntragStatus.KEIN_ANGEBOT;
}

/**
 * Returns true when the status of the Gesuch is VERFUEGEN or VERFUEGT or NUR_SCHULAMT
 */
export function isStatusVerfuegenVerfuegt(status: TSAntragStatus): boolean {
    return isAnyStatusOfVerfuegt(status) || status === TSAntragStatus.VERFUEGEN;
}

export function isAnyStatusOfMahnung(status: TSAntragStatus): boolean {
    return status === TSAntragStatus.ERSTE_MAHNUNG || status === TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN
        || status === TSAntragStatus.ZWEITE_MAHNUNG || status === TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN;
}

export function getStartAntragStatusFromEingangsart(
    eingangsart: TSEingangsart,
    sozialdienstFall: boolean,
): TSAntragStatus {
    return TSEingangsart.ONLINE === eingangsart ?
        TSAntragStatus.IN_BEARBEITUNG_GS :
        sozialdienstFall ? TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST : TSAntragStatus.IN_BEARBEITUNG_JA;
}

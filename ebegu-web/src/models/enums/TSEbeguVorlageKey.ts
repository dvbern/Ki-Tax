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

export enum TSEbeguVorlageKey {
    VORLAGE_MAHNUNG_1 = 'VORLAGE_MAHNUNG_1',
    VORLAGE_MAHNUNG_2 = 'VORLAGE_MAHNUNG_2',
    VORLAGE_VERFUEGUNG_KITA = 'VORLAGE_VERFUEGUNG_KITA',
    VORLAGE_NICHT_EINTRETENSVERFUEGUNG = 'VORLAGE_NICHT_EINTRETENSVERFUEGUNG',
    VORLAGE_INFOSCHREIBEN_MAXIMALTARIF = 'VORLAGE_INFOSCHREIBEN_MAXIMALTARIF',
    VORLAGE_VERFUEGUNG_TAGESFAMILIEN = 'VORLAGE_VERFUEGUNG_TAGESFAMILIEN',
    VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER = 'VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER',
    VORLAGE_FREIGABEQUITTUNG = 'VORLAGE_FREIGABEQUITTUNG',
    VORLAGE_FINANZIELLE_SITUATION = 'VORLAGE_FINANZIELLE_SITUATION',
    VORLAGE_BEGLEITSCHREIBEN = 'VORLAGE_BEGLEITSCHREIBEN',
}

export function getTSEbeguVorlageKeyValues(): Array<TSEbeguVorlageKey> {
    return [
        TSEbeguVorlageKey.VORLAGE_MAHNUNG_1,
        TSEbeguVorlageKey.VORLAGE_MAHNUNG_2,
        TSEbeguVorlageKey.VORLAGE_NICHT_EINTRETENSVERFUEGUNG,
        TSEbeguVorlageKey.VORLAGE_INFOSCHREIBEN_MAXIMALTARIF,
        TSEbeguVorlageKey.VORLAGE_VERFUEGUNG_TAGESFAMILIEN,
        TSEbeguVorlageKey.VORLAGE_FREIGABEQUITTUNG,
        TSEbeguVorlageKey.VORLAGE_FINANZIELLE_SITUATION,
        TSEbeguVorlageKey.VORLAGE_BEGLEITSCHREIBEN,
        TSEbeguVorlageKey.VORLAGE_VERFUEGUNG_KITA,
        TSEbeguVorlageKey.VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER
    ];
}

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

export enum TSVerfuegungZeitabschnittZahlungsstatus {
    NEU = 'NEU',
    VERRECHNET = 'VERRECHNET',
    VERRECHNET_KEINE_BETREUUNG = 'VERRECHNET_KEINE_BETREUUNG',
    VERRECHNET_KORRIGIERT = 'VERRECHNET_KORRIGIERT',
    IGNORIERT = 'IGNORIERT',
    IGNORIEREND = 'IGNORIEREND',
    VERRECHNEND = 'VERRECHNEND',
    IGNORIERT_KORRIGIERT = 'IGNORIERT_KORRIGIERT'
}

export function getZahlungsstatusAlreadyHandeledInZahlungsauftrag(): Array<TSVerfuegungZeitabschnittZahlungsstatus> {
    return [
        TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET,
        TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KEINE_BETREUUNG,
        TSVerfuegungZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT,
        TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT,
        TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT
    ];
}

export function getZahlungsstatusIgnorieren(): Array<TSVerfuegungZeitabschnittZahlungsstatus> {
    return [
        TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT,
        TSVerfuegungZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT,
        TSVerfuegungZeitabschnittZahlungsstatus.IGNORIEREND
    ];
}

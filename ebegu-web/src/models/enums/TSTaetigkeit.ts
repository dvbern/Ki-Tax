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

export enum TSTaetigkeit {
    ANGESTELLT = 'ANGESTELLT',
    SELBSTAENDIG = 'SELBSTAENDIG',
    AUSBILDUNG = 'AUSBILDUNG',
    RAV = 'RAV',
    GESUNDHEITLICHE_EINSCHRAENKUNGEN = 'GESUNDHEITLICHE_EINSCHRAENKUNGEN',
    INTEGRATION_BESCHAEFTIGUNSPROGRAMM = 'INTEGRATION_BESCHAEFTIGUNSPROGRAMM',
    FREIWILLIGENARBEIT = 'FREIWILLIGENARBEIT'
}

export function getTSTaetigkeit(): Array<TSTaetigkeit> {
    return [
        TSTaetigkeit.ANGESTELLT,
        TSTaetigkeit.SELBSTAENDIG,
        TSTaetigkeit.AUSBILDUNG,
        TSTaetigkeit.RAV,
        TSTaetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN,
        TSTaetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM
    ];
}

export function getTSTaetigkeitWithFreiwilligenarbeit(): Array<TSTaetigkeit> {
    const taetigkeiten = getTSTaetigkeit();
    taetigkeiten.push(TSTaetigkeit.FREIWILLIGENARBEIT);
    return taetigkeiten;
}

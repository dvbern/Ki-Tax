/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

export enum TSEinschulungTyp {
    VORSCHULALTER = 'VORSCHULALTER',
    KINDERGARTEN1 = 'KINDERGARTEN1',
    FREIWILLIGER_KINDERGARTEN = 'FREIWILLIGER_KINDERGARTEN',
    KINDERGARTEN2 = 'KINDERGARTEN2',
    OBLIGATORISCHER_KINDERGARTEN = 'OBLIGATORISCHER_KINDERGARTEN',
    PRIMARSTUFE = 'PRIMARSTUFE',
    PRIMAR_SEKUNDAR_STUFE = 'PRIMAR_SEKUNDAR_STUFE',
    SEKUNDAR_UND_HOEHER_STUFE = 'SEKUNDAR_UND_HOEHER_STUFE',
    KLASSE1 = 'KLASSE1',
    KLASSE2 = 'KLASSE2',
    KLASSE3 = 'KLASSE3',
    KLASSE4 = 'KLASSE4',
    KLASSE5 = 'KLASSE5',
    KLASSE6 = 'KLASSE6',
    KLASSE7 = 'KLASSE7',
    KLASSE8 = 'KLASSE8',
    KLASSE9 = 'KLASSE9'
}

export function getTSEinschulungTypValues(): Array<TSEinschulungTyp> {
    return [
        TSEinschulungTyp.VORSCHULALTER,
        TSEinschulungTyp.KINDERGARTEN1,
        TSEinschulungTyp.KINDERGARTEN2,
        TSEinschulungTyp.KLASSE1,
        TSEinschulungTyp.KLASSE2,
        TSEinschulungTyp.KLASSE3,
        TSEinschulungTyp.KLASSE4,
        TSEinschulungTyp.KLASSE5,
        TSEinschulungTyp.KLASSE6,
        TSEinschulungTyp.KLASSE7,
        TSEinschulungTyp.KLASSE8,
        TSEinschulungTyp.KLASSE9
    ];
}

export function getTSEinschulungTypValuesLuzern(): Array<TSEinschulungTyp> {
    return [
        TSEinschulungTyp.VORSCHULALTER,
        TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN,
        TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN,
        TSEinschulungTyp.PRIMAR_SEKUNDAR_STUFE
    ];
}

export function getTSEinschulungTypValuesAppenzellAusserrhoden(): Array<TSEinschulungTyp> {
    return [TSEinschulungTyp.VORSCHULALTER, TSEinschulungTyp.KINDERGARTEN1];
}

export function getTSEinschulungTypGemeindeValues(): Array<TSEinschulungTyp> {
    return [
        TSEinschulungTyp.VORSCHULALTER,
        TSEinschulungTyp.KINDERGARTEN1,
        TSEinschulungTyp.KINDERGARTEN2
    ];
}

export function getTSEinschulungTypValuesSchwyz(): Array<TSEinschulungTyp> {
    return [
        TSEinschulungTyp.VORSCHULALTER,
        TSEinschulungTyp.PRIMARSTUFE,
        TSEinschulungTyp.SEKUNDAR_UND_HOEHER_STUFE
    ];
}

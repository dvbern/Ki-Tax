/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

export enum TSRueckforderungStatus {
    NEU = 'NEU',
    EINGELADEN = 'EINGELADEN',
    IN_BEARBEITUNG_INSTITUTION_STUFE_1 = 'IN_BEARBEITUNG_INSTITUTION_STUFE_1',
    IN_PRUEFUNG_KANTON_STUFE_1 = 'IN_PRUEFUNG_KANTON_STUFE_1',
    GEPRUEFT_STUFE_1 = 'GEPRUEFT_STUFE_1',
    IN_BEARBEITUNG_INSTITUTION_STUFE_2 = 'IN_BEARBEITUNG_INSTITUTION_STUFE_2',
    VERFUEGT_PROVISORISCH = 'VERFUEGT_PROVISORISCH',
    BEREIT_ZUM_VERFUEGEN = 'BEREIT_ZUM_VERFUEGEN',
    IN_PRUEFUNG_KANTON_STUFE_2 = 'IN_PRUEFUNG_KANTON_STUFE_2',
    VERFUEGT = 'VERFUEGT',
    ABGESCHLOSSEN_OHNE_GESUCH = 'ABGESCHLOSSEN_OHNE_GESUCH',
}

export function isNeuOrEingeladenStatus(status: TSRueckforderungStatus): boolean {
    return status === TSRueckforderungStatus.NEU
        || status === TSRueckforderungStatus.EINGELADEN;
}

export function isStatusRelevantForFrist(status: TSRueckforderungStatus): boolean {
    return status === TSRueckforderungStatus.NEU
        || status === TSRueckforderungStatus.EINGELADEN
        || status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1
        || status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1
        || status === TSRueckforderungStatus.GEPRUEFT_STUFE_1
        || status === TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2;
}

export function isBereitZumVerfuegenOderVerfuegt(status: TSRueckforderungStatus): boolean {
    return status === TSRueckforderungStatus.VERFUEGT || status === TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN;
}

export function isAnyOfVerfuegtOrPruefungKantonStufe2(status: TSRueckforderungStatus): boolean {
    return status === TSRueckforderungStatus.VERFUEGT || status === TSRueckforderungStatus.BEREIT_ZUM_VERFUEGEN ||
        status === TSRueckforderungStatus.VERFUEGT_PROVISORISCH || status === TSRueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
}


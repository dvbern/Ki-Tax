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

export enum TSModulTagesschuleName {
    SCOLARIS_01 = 'SCOLARIS_01',
    SCOLARIS_02 = 'SCOLARIS_02',
    SCOLARIS_03 = 'SCOLARIS_03',
    SCOLARIS_04 = 'SCOLARIS_04',
    SCOLARIS_05 = 'SCOLARIS_05',
    SCOLARIS_06 = 'SCOLARIS_06',
    SCOLARIS_07 = 'SCOLARIS_07',
    SCOLARIS_08 = 'SCOLARIS_08',
    SCOLARIS_09 = 'SCOLARIS_09',
    SCOLARIS_10 = 'SCOLARIS_10',
    DYNAMISCH = 'DYNAMISCH'
}

export function getTSModulTagesschuleNameValues(): Array<TSModulTagesschuleName> {
    return [
        TSModulTagesschuleName.SCOLARIS_01,
        TSModulTagesschuleName.SCOLARIS_02,
        TSModulTagesschuleName.SCOLARIS_03,
        TSModulTagesschuleName.SCOLARIS_04,
        TSModulTagesschuleName.SCOLARIS_05,
        TSModulTagesschuleName.SCOLARIS_06,
        TSModulTagesschuleName.SCOLARIS_07,
        TSModulTagesschuleName.SCOLARIS_08,
        TSModulTagesschuleName.SCOLARIS_09,
        TSModulTagesschuleName.SCOLARIS_10
    ];
}

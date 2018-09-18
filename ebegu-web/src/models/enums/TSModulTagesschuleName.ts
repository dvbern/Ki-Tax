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
    VORMITTAG = 'VORMITTAG',
    MITTAG = 'MITTAG',
    MITTAG_HALB = 'MITTAG_HALB',
    NACHMITTAGS_1 = 'NACHMITTAGS_1',
    NACHMITTAGS_1_HALB = 'NACHMITTAGS_1_HALB',
    NACHMITTAGS_2 = 'NACHMITTAGS_2',
    NACHMITTAGS_2_HALB = 'NACHMITTAGS_2_HALB'
}

export function getTSModulTagesschuleNameValues(): Array<TSModulTagesschuleName> {
    return [
        TSModulTagesschuleName.VORMITTAG,
        TSModulTagesschuleName.MITTAG,
        TSModulTagesschuleName.MITTAG_HALB,
        TSModulTagesschuleName.NACHMITTAGS_1,
        TSModulTagesschuleName.NACHMITTAGS_1_HALB,
        TSModulTagesschuleName.NACHMITTAGS_2,
        TSModulTagesschuleName.NACHMITTAGS_2_HALB
    ];
}

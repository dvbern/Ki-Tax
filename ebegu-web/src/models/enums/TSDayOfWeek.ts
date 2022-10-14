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

export enum TSDayOfWeek {
    MONDAY = 'MONDAY',
    TUESDAY = 'TUESDAY',
    WEDNESDAY = 'WEDNESDAY',
    THURSDAY = 'THURSDAY',
    FRIDAY = 'FRIDAY',
    SATURDAY = 'SATURDAY',
    SUNDAY = 'SUNDAY'
}

export function getWeekdaysValues(): Array<TSDayOfWeek> {
    return [
        TSDayOfWeek.MONDAY,
        TSDayOfWeek.TUESDAY,
        TSDayOfWeek.WEDNESDAY,
        TSDayOfWeek.THURSDAY,
        TSDayOfWeek.FRIDAY
    ];
}

export const MAP_SORTED_BY_DAY_OF_WEEK = new Map<TSDayOfWeek, number>([
    [TSDayOfWeek.MONDAY, 1],
    [TSDayOfWeek.TUESDAY, 2],
    [TSDayOfWeek.WEDNESDAY, 3],
    [TSDayOfWeek.THURSDAY, 4],
    [TSDayOfWeek.FRIDAY, 5],
    [TSDayOfWeek.SATURDAY, 6],
    [TSDayOfWeek.SUNDAY, 7]
]);

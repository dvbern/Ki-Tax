/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

export interface LogLevelEntry {
    level: number;
}

export enum LogLevel {
    ERROR = 'ERROR',
    WARN = 'WARN',
    INFO = 'INFO',
    DEBUG = 'DEBUG',
    NONE = 'NONE'
}

export const LEVELS: {[k in LogLevel]: LogLevelEntry} = {
    ERROR: {level: 4},
    WARN: {level: 3},
    INFO: {level: 2},
    DEBUG: {level: 1},
    NONE: {level: 0}
};

/* eslint-enable @typescript-eslint/consistent-type-assertions */

/**
 * key: Name vom Logger (typischerweise der Klassenname)
 */
export interface LogModules {
    [key: string]: LogLevel;
}

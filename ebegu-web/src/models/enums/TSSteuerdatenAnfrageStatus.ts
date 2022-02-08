/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export enum TSSteuerdatenAnfrageStatus {
    OFFEN = 'OFFEN',
    PROVISORISCH = 'PROVISORISCH',
    RECHTSKRAEFTIG = 'RECHTSKRAEFTIG',
    FAILED = 'FAILED',
    FAILED_KEIN_PARTNER_GEMEINSAM = 'FAILED_KEIN_PARTNER_GEMEINSAM',
    FAILED_PARTNER_NICHT_GEMEINSAM = 'FAILED_PARTNER_NICHT_GEMEINSAM',
    FAILED_GEBURTSDATUM = 'FAILED_GEBURTSDATUM',
    FAILED_UNTERJAEHRIGER_FALL = 'FAILED_UNTERJAEHRIGER_FALL',
}

export function isSteuerdatenAnfrageStatusErfolgreich(status: TSSteuerdatenAnfrageStatus): boolean {
    return status === TSSteuerdatenAnfrageStatus.PROVISORISCH
    || status === TSSteuerdatenAnfrageStatus.RECHTSKRAEFTIG;
}

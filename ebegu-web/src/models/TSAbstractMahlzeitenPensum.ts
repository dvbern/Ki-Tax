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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSAbstractDecimalPensumEntity} from './TSAbstractDecimalPensumEntity';

export class TSAbstractMahlzeitenPensum extends TSAbstractDecimalPensumEntity {
    private _monatlicheHauptmahlzeiten: number;
    private _monatlicheNebenmahlzeiten: number;
    private _tarifProHauptmahlzeit: number;
    private _tarifProNebenmahlzeit: number;

    public constructor() {
        super();
    }

    public get monatlicheHauptmahlzeiten(): number {
        return this._monatlicheHauptmahlzeiten;
    }

    public set monatlicheHauptmahlzeiten(value: number) {
        this._monatlicheHauptmahlzeiten = value;
    }

    public get monatlicheNebenmahlzeiten(): number {
        return this._monatlicheNebenmahlzeiten;
    }

    public set monatlicheNebenmahlzeiten(value: number) {
        this._monatlicheNebenmahlzeiten = value;
    }

    public get tarifProHauptmahlzeit(): number {
        return this._tarifProHauptmahlzeit;
    }

    public set tarifProHauptmahlzeit(value: number) {
        this._tarifProHauptmahlzeit = value;
    }

    public get tarifProNebenmahlzeit(): number {
        return this._tarifProNebenmahlzeit;
    }

    public set tarifProNebenmahlzeit(value: number) {
        this._tarifProNebenmahlzeit = value;
    }
}

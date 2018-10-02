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

import {TSTaetigkeit} from './enums/TSTaetigkeit';
import {TSZuschlagsgrund} from './enums/TSZuschlagsgrund';
import {TSAbstractPensumEntity} from './TSAbstractPensumEntity';
import {TSDateRange} from './types/TSDateRange';

/**
 * Definiert ein Erwerbspensum
 */
export default class TSErwerbspensum extends TSAbstractPensumEntity {

    private _taetigkeit: TSTaetigkeit;

    private _zuschlagZuErwerbspensum: boolean;

    private _zuschlagsgrund: TSZuschlagsgrund;

    private _zuschlagsprozent: number;

    private _bezeichnung: string;

    public constructor(pensum?: number,
                       gueltigkeit?: TSDateRange,
                       taetigkeit?: TSTaetigkeit,
                       zuschlagZuErwerbspensum?: boolean,
                       zuschlagsgrund?: TSZuschlagsgrund,
                       zuschlagsprozent?: number) {
        super(pensum, gueltigkeit);
        this._taetigkeit = taetigkeit;
        this._zuschlagZuErwerbspensum = zuschlagZuErwerbspensum;
        this._zuschlagsgrund = zuschlagsgrund;
        this._zuschlagsprozent = zuschlagsprozent;
    }

    public get taetigkeit(): TSTaetigkeit {
        return this._taetigkeit;
    }

    public set taetigkeit(value: TSTaetigkeit) {
        this._taetigkeit = value;
    }

    public get zuschlagZuErwerbspensum(): boolean {
        return this._zuschlagZuErwerbspensum;
    }

    public set zuschlagZuErwerbspensum(value: boolean) {
        this._zuschlagZuErwerbspensum = value;
    }

    public get zuschlagsgrund(): TSZuschlagsgrund {
        return this._zuschlagsgrund;
    }

    public set zuschlagsgrund(value: TSZuschlagsgrund) {
        this._zuschlagsgrund = value;
    }

    public get zuschlagsprozent(): number {
        return this._zuschlagsprozent;
    }

    public set zuschlagsprozent(value: number) {
        this._zuschlagsprozent = value;
    }

    public get bezeichnung(): string {
        return this._bezeichnung;
    }

    public set bezeichnung(value: string) {
        this._bezeichnung = value;
    }
}

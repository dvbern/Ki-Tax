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

import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import TSEWKPerson from './TSEWKPerson';

/**
 * DTO für Resultate aus dem EWK
 */
export default class TSEWKResultat extends TSAbstractMutableEntity {

    private _maxResultate: number;
    private _anzahlResultate: number;
    private _personen: Array<TSEWKPerson>;

    public constructor(maxResultate?: number, anzahlResultate?: number, personen?: Array<TSEWKPerson>) {
        super();
        this._maxResultate = maxResultate;
        this._anzahlResultate = anzahlResultate;
        this._personen = personen;
    }

    public get maxResultate(): number {
        return this._maxResultate;
    }

    public set maxResultate(value: number) {
        this._maxResultate = value;
    }

    public get anzahlResultate(): number {
        return this._anzahlResultate;
    }

    public set anzahlResultate(value: number) {
        this._anzahlResultate = value;
    }

    public get personen(): Array<TSEWKPerson> {
        return this._personen;
    }

    public set personen(value: Array<TSEWKPerson>) {
        this._personen = value;
    }

    public isTooManyResults(): boolean {
        return this.anzahlResultate > this.maxResultate;
    }
}

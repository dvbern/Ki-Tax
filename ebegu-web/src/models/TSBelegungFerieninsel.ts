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

import {TSFerienname} from './enums/TSFerienname';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBelegungFerieninselTag} from './TSBelegungFerieninselTag';

export class TSBelegungFerieninsel extends TSAbstractMutableEntity {
    private _ferienname: TSFerienname;
    private _tage: Array<TSBelegungFerieninselTag> = [];
    private _tageMorgenmodul: Array<TSBelegungFerieninselTag> = [];
    private _notfallAngaben: string;

    public constructor(
        ferienname?: TSFerienname,
        tage?: Array<TSBelegungFerieninselTag>,
        notfallAngaben?: string
    ) {
        super();
        this._ferienname = ferienname;
        this._tage = tage;
        this._notfallAngaben = notfallAngaben;
    }

    public get ferienname(): TSFerienname {
        return this._ferienname;
    }

    public set ferienname(value: TSFerienname) {
        this._ferienname = value;
    }

    public get tage(): Array<TSBelegungFerieninselTag> {
        return this._tage;
    }

    public set tage(value: Array<TSBelegungFerieninselTag>) {
        this._tage = value;
    }

    public get tageMorgenmodul(): Array<TSBelegungFerieninselTag> {
        return this._tageMorgenmodul;
    }

    public set tageMorgenmodul(value: Array<TSBelegungFerieninselTag>) {
        this._tageMorgenmodul = value;
    }

    public get notfallAngaben(): string {
        return this._notfallAngaben;
    }

    public set notfallAngaben(value: string) {
        this._notfallAngaben = value;
    }
}

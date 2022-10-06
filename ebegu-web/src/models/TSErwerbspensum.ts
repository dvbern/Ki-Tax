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
import {TSAbstractIntegerPensumEntity} from './TSAbstractIntegerPensumEntity';
import {TSUnbezahlterUrlaub} from './TSUnbezahlterUrlaub';

/**
 * Definiert ein Erwerbspensum
 */
export class TSErwerbspensum extends TSAbstractIntegerPensumEntity {

    private _taetigkeit: TSTaetigkeit;

    private _bezeichnung: string;

    private _unbezahlterUrlaub: TSUnbezahlterUrlaub;

    private _unregelmaessigeArbeitszeiten: boolean;

    private _erwerbspensumInstitution: string

    public constructor() {
        super();
    }

    public get taetigkeit(): TSTaetigkeit {
        return this._taetigkeit;
    }

    public set taetigkeit(value: TSTaetigkeit) {
        this._taetigkeit = value;
    }

    public get bezeichnung(): string {
        return this._bezeichnung;
    }

    public set bezeichnung(value: string) {
        this._bezeichnung = value;
    }

    public get unbezahlterUrlaub(): TSUnbezahlterUrlaub {
        return this._unbezahlterUrlaub;
    }

    public set unbezahlterUrlaub(value: TSUnbezahlterUrlaub) {
        this._unbezahlterUrlaub = value;
    }

    public get unregelmaessigeArbeitszeiten(): boolean {
        return this._unregelmaessigeArbeitszeiten;
    }

    public set unregelmaessigeArbeitszeiten(value: boolean) {
        this._unregelmaessigeArbeitszeiten = value;
    }

    public get erwerbspensumInstitution(): string {
        return this._erwerbspensumInstitution;
    }

    public set erwerbspensumInstitution(value: string) {
        this._erwerbspensumInstitution = value;
    }
}

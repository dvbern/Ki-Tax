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

import TSAbstractEntity from './TSAbstractEntity';
import TSUser from './TSUser';
import EbeguUtil from '../utils/EbeguUtil';

export default class TSFall extends TSAbstractEntity {

    private _fallNummer: number;
    private _nextNumberKind: number;
    private _nextNumberDossier: number;
    private _verantwortlicher: TSUser;
    private _verantwortlicherSCH: TSUser;
    private _besitzer: TSUser;

    constructor(fallNummer?: number, verantwortlicher?: TSUser, verantwortlicherSCH?: TSUser, nextNumberKind?: number, nextNumberDossier?: number,
                besitzer?: TSUser) {
        super();
        this._fallNummer = fallNummer;
        this._verantwortlicher = verantwortlicher;
        this._verantwortlicherSCH = verantwortlicherSCH;
        this._nextNumberKind = nextNumberKind;
        this._nextNumberDossier = nextNumberDossier;
        this._besitzer = besitzer;
    }

    get fallNummer(): number {
        return this._fallNummer;
    }

    set fallNummer(value: number) {
        this._fallNummer = value;
    }

    get nextNumberDossier(): number {
        return this._nextNumberDossier;
    }

    set nextNumberDossier(value: number) {
        this._nextNumberDossier = value;
    }

    get verantwortlicher(): TSUser {
        return this._verantwortlicher;
    }

    set verantwortlicher(value: TSUser) {
        this._verantwortlicher = value;
    }

    public get verantwortlicherSCH(): TSUser {
        return this._verantwortlicherSCH;
    }

    public set verantwortlicherSCH(value: TSUser) {
        this._verantwortlicherSCH = value;
    }

    get nextNumberKind(): number {
        return this._nextNumberKind;
    }

    set nextNumberKind(value: number) {
        this._nextNumberKind = value;
    }

    get besitzer(): TSUser {
        return this._besitzer;
    }

    set besitzer(value: TSUser) {
        this._besitzer = value;
    }

    public getHauptverantwortlicher(): TSUser {
        if (this.verantwortlicher) {
            return this.verantwortlicher;
        }
        return this.verantwortlicherSCH;
    }

    public isHauptverantwortlicherSchulamt(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.verantwortlicherSCH) && EbeguUtil.isNullOrUndefined(this.verantwortlicher);
    }
}

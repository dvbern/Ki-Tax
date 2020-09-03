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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {TSAbstractEntity} from './TSAbstractEntity';
import {TSAdresse} from './TSAdresse';

export class TSBetreuungsstandort extends TSAbstractEntity {

    private _adresse: TSAdresse;
    private _mail: string;
    private _telefon: string;
    private _webseite: string;

    public constructor() {
        super();
        this.adresse = new TSAdresse();
    }

    public get adresse(): TSAdresse {
        return this._adresse;
    }

    public set adresse(value: TSAdresse) {
        this._adresse = value;
    }

    public get mail(): string {
        return this._mail;
    }

    public set mail(value: string) {
        this._mail = value;
    }

    public get telefon(): string {
        return this._telefon;
    }

    public set telefon(value: string) {
        this._telefon = value;
    }

    public get webseite(): string {
        return this._webseite;
    }

    public set webseite(value: string) {
        this._webseite = value;
    }
}

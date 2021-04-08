/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import {TSFerienbetreuungAbstractAngaben} from './TSFerienbetreuungAbstractAngaben';

export class TSFerienbetreuungAngabenKostenEinnahmen extends TSFerienbetreuungAbstractAngaben {

    private _personalkosten: number;
    private _personalkostenLeitungAdmin: number;
    private _sachkosten: number;
    private _verpflegungskosten: number;
    private _weitereKosten: number;
    private _bemerkungenKosten: string;
    private _elterngebuehren: number;
    private _weitereEinnahmen: number;

    public get personalkosten(): number {
        return this._personalkosten;
    }

    public set personalkosten(value: number) {
        this._personalkosten = value;
    }

    public get personalkostenLeitungAdmin(): number {
        return this._personalkostenLeitungAdmin;
    }

    public set personalkostenLeitungAdmin(value: number) {
        this._personalkostenLeitungAdmin = value;
    }

    public get sachkosten(): number {
        return this._sachkosten;
    }

    public set sachkosten(value: number) {
        this._sachkosten = value;
    }

    public get verpflegungskosten(): number {
        return this._verpflegungskosten;
    }

    public set verpflegungskosten(value: number) {
        this._verpflegungskosten = value;
    }

    public get weitereKosten(): number {
        return this._weitereKosten;
    }

    public set weitereKosten(value: number) {
        this._weitereKosten = value;
    }

    public get bemerkungenKosten(): string {
        return this._bemerkungenKosten;
    }

    public set bemerkungenKosten(value: string) {
        this._bemerkungenKosten = value;
    }

    public get elterngebuehren(): number {
        return this._elterngebuehren;
    }

    public set elterngebuehren(value: number) {
        this._elterngebuehren = value;
    }

    public get weitereEinnahmen(): number {
        return this._weitereEinnahmen;
    }

    public set weitereEinnahmen(value: number) {
        this._weitereEinnahmen = value;
    }
}

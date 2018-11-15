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

export class TSFachstelle extends TSAbstractMutableEntity {

    private _name: string;
    private _beschreibung: string;
    private _fachstelleAnspruch: boolean;
    private _fachstelleErweiterteBetreuung: boolean;

    public constructor(
        name?: string,
        beschreibung?: string,
        fachstelleAnspruch?: boolean,
        fachstelleErweiterteBetreuung?: boolean
    ) {
        super();
        this._name = name;
        this._beschreibung = beschreibung;
        this._fachstelleAnspruch = !!fachstelleAnspruch;
        this._fachstelleErweiterteBetreuung = !!fachstelleErweiterteBetreuung;
    }

    public get name(): string {
        return this._name;
    }

    public set name(value: string) {
        this._name = value;
    }

    public get beschreibung(): string {
        return this._beschreibung;
    }

    public set beschreibung(value: string) {
        this._beschreibung = value;
    }

    public get fachstelleAnspruch(): boolean {
        return this._fachstelleAnspruch;
    }

    public set fachstelleAnspruch(value: boolean) {
        this._fachstelleAnspruch = value;
    }

    public get fachstelleErweiterteBetreuung(): boolean {
        return this._fachstelleErweiterteBetreuung;
    }

    public set fachstelleErweiterteBetreuung(value: boolean) {
        this._fachstelleErweiterteBetreuung = value;
    }
}

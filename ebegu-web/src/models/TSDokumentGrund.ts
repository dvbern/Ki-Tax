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

import TSDokument from './TSDokument';
import {TSDokumentGrundTyp} from './enums/TSDokumentGrundTyp';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSDokumentTyp} from './enums/TSDokumentTyp';
import {TSDokumentGrundPersonType} from './enums/TSDokumentGrundPersonType';

export default class TSDokumentGrund extends TSAbstractMutableEntity {

    private _dokumentGrundTyp: TSDokumentGrundTyp;

    private _fullName: string;

    private _tag: string;

    private _personType: TSDokumentGrundPersonType;

    private _personNumber: number;

    private _dokumente: Array<TSDokument>;

    private _dokumentTyp: TSDokumentTyp;

    private _needed: boolean;

    public constructor(dokumentGrundTyp?: TSDokumentGrundTyp) {
        super();
        this._dokumentGrundTyp = dokumentGrundTyp;
    }

    public get dokumentGrundTyp(): TSDokumentGrundTyp {
        return this._dokumentGrundTyp;
    }

    public set dokumentGrundTyp(value: TSDokumentGrundTyp) {
        this._dokumentGrundTyp = value;
    }

    public get fullName(): string {
        return this._fullName;
    }

    public set fullName(value: string) {
        this._fullName = value;
    }

    public get tag(): string {
        return this._tag;
    }

    public set tag(value: string) {
        this._tag = value;
    }

    public get dokumente(): Array<TSDokument> {
        return this._dokumente;
    }

    public set dokumente(value: Array<TSDokument>) {
        this._dokumente = value;
    }

    public get dokumentTyp(): TSDokumentTyp {
        return this._dokumentTyp;
    }

    public set dokumentTyp(value: TSDokumentTyp) {
        this._dokumentTyp = value;
    }

    public get needed(): boolean {
        return this._needed;
    }

    public set needed(value: boolean) {
        this._needed = value;
    }

    public get personType(): TSDokumentGrundPersonType {
        return this._personType;
    }

    public set personType(value: TSDokumentGrundPersonType) {
        this._personType = value;
    }

    public get personNumber(): number {
        return this._personNumber;
    }

    public set personNumber(value: number) {
        this._personNumber = value;
    }
}

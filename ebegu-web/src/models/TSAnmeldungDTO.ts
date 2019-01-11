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

import {TSEinschulungTyp} from './enums/TSEinschulungTyp';
import TSBetreuung from './TSBetreuung';

export default class TSAnmeldungDTO {

    private _kindContainerId: string;
    private _betreuung: TSBetreuung;
    private _additionalKindQuestions: boolean;
    private _sprichtAmtssprache: boolean;
    private _einschulungTyp: TSEinschulungTyp;

    public get kindContainerId(): string {
        return this._kindContainerId;
    }

    public set kindContainerId(value: string) {
        this._kindContainerId = value;
    }

    public get betreuung(): TSBetreuung {
        return this._betreuung;
    }

    public set betreuung(value: TSBetreuung) {
        this._betreuung = value;
    }

    public get additionalKindQuestions(): boolean {
        return this._additionalKindQuestions;
    }

    public set additionalKindQuestions(value: boolean) {
        this._additionalKindQuestions = value;
    }

    public get sprichtAmtssprache(): boolean {
        return this._sprichtAmtssprache;
    }

    public set sprichtAmtssprache(value: boolean) {
        this._sprichtAmtssprache = value;
    }

    public get einschulungTyp(): TSEinschulungTyp {
        return this._einschulungTyp;
    }

    public set einschulungTyp(value: TSEinschulungTyp) {
        this._einschulungTyp = value;
    }
}

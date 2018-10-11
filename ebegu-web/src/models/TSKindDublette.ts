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

export default class TSKindDublette {

    private _gesuchId: string;
    private _fallNummer: number;
    private _kindNummerOriginal: number;
    private _kindNummerDublette: number;

    public constructor(
        gesuchId?: string,
        fallNummer?: number,
        kindNummerOriginal?: number,
        kindNummerDublette?: number,
    ) {
        this._gesuchId = gesuchId;
        this._fallNummer = fallNummer;
        this._kindNummerOriginal = kindNummerOriginal;
        this._kindNummerDublette = kindNummerDublette;
    }

    public get gesuchId(): string {
        return this._gesuchId;
    }

    public set gesuchId(value: string) {
        this._gesuchId = value;
    }

    public get fallNummer(): number {
        return this._fallNummer;
    }

    public set fallNummer(value: number) {
        this._fallNummer = value;
    }

    public get kindNummerOriginal(): number {
        return this._kindNummerOriginal;
    }

    public set kindNummerOriginal(value: number) {
        this._kindNummerOriginal = value;
    }

    public get kindNummerDublette(): number {
        return this._kindNummerDublette;
    }

    public set kindNummerDublette(value: number) {
        this._kindNummerDublette = value;
    }
}

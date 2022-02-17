/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

export class TSFinanzielleSituationAufteilungDTO {
    private _bruttoertraegeVermoegen: TSAufteilungDTO;
    private _abzugSchuldzinsen: TSAufteilungDTO;
    private _gewinnungskosten: TSAufteilungDTO;
    private _geleisteteAlimente: TSAufteilungDTO;
    private _nettovermoegen: TSAufteilungDTO;

    public get bruttoertraegeVermoegen(): TSAufteilungDTO {
        return this._bruttoertraegeVermoegen;
    }

    public set bruttoertraegeVermoegen(value: TSAufteilungDTO) {
        this._bruttoertraegeVermoegen = value;
    }

    public get abzugSchuldzinsen(): TSAufteilungDTO {
        return this._abzugSchuldzinsen;
    }

    public set abzugSchuldzinsen(value: TSAufteilungDTO) {
        this._abzugSchuldzinsen = value;
    }

    public get gewinnungskosten(): TSAufteilungDTO {
        return this._gewinnungskosten;
    }

    public set gewinnungskosten(value: TSAufteilungDTO) {
        this._gewinnungskosten = value;
    }

    public get geleisteteAlimente(): TSAufteilungDTO {
        return this._geleisteteAlimente;
    }

    public set geleisteteAlimente(value: TSAufteilungDTO) {
        this._geleisteteAlimente = value;
    }

    public get nettovermoegen(): TSAufteilungDTO {
        return this._nettovermoegen;
    }

    public set nettovermoegen(value: TSAufteilungDTO) {
        this._nettovermoegen = value;
    }
}

export class TSAufteilungDTO {
    private _gs1: number;
    /**
     * Value from GS Container. Used for DV Bisher
     */
    private _gs1Urspruenglich: number;
    private _gs2: number;
    /**
     * Value from GS Container. Used for DV Bisher
     */
    private _gs2Urspruenglich: number;
    /**
     * stores initial sum of gs1 + gs2. Used for validation
     */
    private _initialSum: number;

    public get gs1(): number {
        return this._gs1;
    }

    public set gs1(value: number) {
        this._gs1 = value;
    }

    public get gs2(): number {
        return this._gs2;
    }

    public set gs2(value: number) {
        this._gs2 = value;
    }

    public get gs1Urspruenglich(): number {
        return this._gs1Urspruenglich;
    }

    public set gs1Urspruenglich(value: number) {
        this._gs1Urspruenglich = value;
    }

    public get gs2Urspruenglich(): number {
        return this._gs2Urspruenglich;
    }

    public set gs2Urspruenglich(value: number) {
        this._gs2Urspruenglich = value;
    }

    public get initialSum(): number {
        return this._initialSum;
    }

    public calculateInitiaSum(): void {
        this._initialSum = this.gs1 + this.gs2;
    }
}

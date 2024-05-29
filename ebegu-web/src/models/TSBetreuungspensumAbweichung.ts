/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSBetreuungspensumAbweichungStatus} from './enums/betreuung/TSBetreuungspensumAbweichungStatus';
import {TSAbstractMahlzeitenPensum} from './TSAbstractMahlzeitenPensum';

export class TSBetreuungspensumAbweichung extends TSAbstractMahlzeitenPensum {

    private _status: TSBetreuungspensumAbweichungStatus;
    private _vertraglichesPensum: number;
    private _vertraglicheKosten: number;
    private _vertraglicheHauptmahlzeiten: number;
    private _vertraglicheNebenmahlzeiten: number;
    private _vertraglicherTarifHaupt: number;
    private _vertraglicherTarifNeben: number;
    private _multiplier: number;

    public get status(): TSBetreuungspensumAbweichungStatus {
        return this._status;
    }

    public set status(value: TSBetreuungspensumAbweichungStatus) {
        this._status = value;
    }

    public get vertraglichesPensum(): number {
        return this._vertraglichesPensum;
    }

    public set vertraglichesPensum(value: number) {
        this._vertraglichesPensum = value;
    }

    public get vertraglicheKosten(): number {
        return this._vertraglicheKosten;
    }

    public set vertraglicheKosten(value: number) {
        this._vertraglicheKosten = value;
    }

    public get vertraglicheHauptmahlzeiten(): number {
        return this._vertraglicheHauptmahlzeiten;
    }

    public set vertraglicheHauptmahlzeiten(value: number) {
        this._vertraglicheHauptmahlzeiten = value;
    }

    public get vertraglicheNebenmahlzeiten(): number {
        return this._vertraglicheNebenmahlzeiten;
    }

    public set vertraglicheNebenmahlzeiten(value: number) {
        this._vertraglicheNebenmahlzeiten = value;
    }

    public get vertraglicherTarifHaupt(): number {
        return this._vertraglicherTarifHaupt;
    }

    public set vertraglicherTarifHaupt(value: number) {
        this._vertraglicherTarifHaupt = value;
    }

    public get vertraglicherTarifNeben(): number {
        return this._vertraglicherTarifNeben;
    }

    public set vertraglicherTarifNeben(value: number) {
        this._vertraglicherTarifNeben = value;
    }

    public get multiplier(): number {
        return this._multiplier;
    }

    public set multiplier(value: number) {
        this._multiplier = value;
    }
}

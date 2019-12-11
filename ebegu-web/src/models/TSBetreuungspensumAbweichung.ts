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

import {TSBetreuungspensumAbweichungStatus} from './enums/TSBetreuungspensumAbweichungStatus';
import {TSAbstractDecimalPensumEntity} from './TSAbstractDecimalPensumEntity';

export class TSBetreuungspensumAbweichung extends TSAbstractDecimalPensumEntity {

    private _status: TSBetreuungspensumAbweichungStatus;
    private _vertraglichesPensum: number;
    private _vertraglicheKosten: number;

    public constructor() {
        super();
    }

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
}

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {EbeguUtil} from '../utils/EbeguUtil';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

import {TSSocialhilfeZeitraum} from './TSSocialhilfeZeitraum';

export class TSSocialhilfeZeitraumContainer extends TSAbstractMutableEntity {
    private _socialhilfeZeitraumGS: TSSocialhilfeZeitraum;
    private _socialhilfeZeitraumJA: TSSocialhilfeZeitraum;

    public constructor(socialhilfeZeitraumGS?: TSSocialhilfeZeitraum, socialhilfeZeitraumJA?: TSSocialhilfeZeitraum) {
        super();
        this._socialhilfeZeitraumGS = socialhilfeZeitraumGS;
        this._socialhilfeZeitraumJA = socialhilfeZeitraumJA;
    }

    public get socialhilfeZeitraumGS(): TSSocialhilfeZeitraum {
        return this._socialhilfeZeitraumGS;
    }

    public set socialhilfeZeitraumGS(value: TSSocialhilfeZeitraum) {
        this._socialhilfeZeitraumGS = value;
    }

    public get socialhilfeZeitraumJA(): TSSocialhilfeZeitraum {
        return this._socialhilfeZeitraumJA;
    }

    public set socialhilfeZeitraumJA(value: TSSocialhilfeZeitraum) {
        this._socialhilfeZeitraumJA = value;
    }

    public isGSContainerEmpty(): boolean {
        return EbeguUtil.isNullOrUndefined(this.socialhilfeZeitraumGS);
    }
}

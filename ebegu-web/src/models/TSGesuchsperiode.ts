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

import {Moment} from 'moment';
import {TSGesuchsperiodeStatus} from './enums/TSGesuchsperiodeStatus';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';

export class TSGesuchsperiode extends TSAbstractDateRangedEntity {

    private _status: TSGesuchsperiodeStatus;

    public constructor(
        status?: TSGesuchsperiodeStatus,
        gueltigkeit?: TSDateRange,
    ) {
        super(gueltigkeit);
        this._status = status;
    }

    public get status(): TSGesuchsperiodeStatus {
        return this._status;
    }

    public set status(value: TSGesuchsperiodeStatus) {
        this._status = value;
    }

    public get gesuchsperiodeString(): string {
        if (this.gueltigkeit && this.gueltigkeit.gueltigAb && this.gueltigkeit.gueltigBis) {
            const currentMillenia = 2000;

            return `${this.gueltigkeit.gueltigAb.year()}/${this.gueltigkeit.gueltigBis.year() - currentMillenia}`;
        }

        return undefined;
    }

    public isEntwurf(): boolean {
        return this.status === TSGesuchsperiodeStatus.ENTWURF;
    }

    public isAktiv(): boolean {
        return this.status === TSGesuchsperiodeStatus.AKTIV;
    }

    public getBasisJahr(): number {
        return this.gueltigkeit.gueltigAb.year() - 1;
    }

    public getBasisJahrPlus1(): number {
        return this.getBasisJahr() + 1;
    }

    public getBasisJahrPlus2(): number {
        return this.getBasisJahr() + 2;
    }

    public isBefore(moment: Moment): boolean {
        return this.gueltigkeit.gueltigBis.isBefore(moment);
    }
}

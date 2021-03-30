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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {TSFerienbetreuungFormularStatus} from '../enums/TSFerienbetreuungFormularStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';

export class TSFerienbetreuungAbstractAngaben extends TSAbstractEntity {
    protected _status: TSFerienbetreuungFormularStatus;

    public get status(): TSFerienbetreuungFormularStatus {
        return this._status;
    }

    public set status(value: TSFerienbetreuungFormularStatus) {
        this._status = value;
    }

    public isAtLeastAbgeschlossenGemeinde(): boolean {
        return [
            TSFerienbetreuungFormularStatus.ABEGSCHLOSSEN,
            TSFerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON,
            TSFerienbetreuungFormularStatus.GEPRUEFT,
        ].includes(this.status);
    }

    public isAtLeastInPruefungKanton(): boolean {
        return [
            TSFerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON,
            TSFerienbetreuungFormularStatus.GEPRUEFT,
        ].includes(this.status);
    }

    public isGeprueft(): boolean {
        return this.status === TSFerienbetreuungFormularStatus.GEPRUEFT;
    }

    public isInPruefungKanton(): boolean {
        return this.status === TSFerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON;
    }
}

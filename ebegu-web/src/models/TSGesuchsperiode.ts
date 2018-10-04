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

import * as moment from 'moment';
import {TSGesuchsperiodeStatus} from './enums/TSGesuchsperiodeStatus';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSDateRange} from './types/TSDateRange';

export default class TSGesuchsperiode extends TSAbstractDateRangedEntity {

    private _status: TSGesuchsperiodeStatus;
    private _datumFreischaltungTagesschule: moment.Moment;
    private _datumErsterSchultag: moment.Moment;

    public constructor(
        status?: TSGesuchsperiodeStatus,
        gueltigkeit?: TSDateRange,
        datumFreischaltungTagesschule?: moment.Moment,
        datumErsterSchultag?: moment.Moment,
    ) {
        super(gueltigkeit);
        this._status = status;
        this._datumFreischaltungTagesschule = datumFreischaltungTagesschule;
        this._datumErsterSchultag = datumErsterSchultag;
    }

    public get status(): TSGesuchsperiodeStatus {
        return this._status;
    }

    public set status(value: TSGesuchsperiodeStatus) {
        this._status = value;
    }

    public get datumFreischaltungTagesschule(): moment.Moment {
        return this._datumFreischaltungTagesschule;
    }

    public set datumFreischaltungTagesschule(value: moment.Moment) {
        this._datumFreischaltungTagesschule = value;
    }

    public get datumErsterSchultag(): moment.Moment {
        return this._datumErsterSchultag;
    }

    public set datumErsterSchultag(value: moment.Moment) {
        this._datumErsterSchultag = value;
    }

    public get gesuchsperiodeString(): string {
        if (this.gueltigkeit && this.gueltigkeit.gueltigAb && this.gueltigkeit.gueltigBis) {
            const currentMillenia = 2000;

            return `${this.gueltigkeit.gueltigAb.year()}/${this.gueltigkeit.gueltigBis.year() - currentMillenia}`;
        }

        return undefined;
    }

    /**
     * Ein datumFreischaltungTagesschule, das nicht vor dem Gesuchsperiodeanfang liegt, wird als "nicht konfiguriert"
     * betrachtet. Dies ist so, weil ein datumFreischaltungTagesschule immer vor dem Gesuchsperiodeanfang liegen muss,
     * damit die Kinder sich rechtzeitig anmelden koennen.
     */
    public isTagesschulenAnmeldungKonfiguriert(): boolean {
        return this.hasTagesschulenAnmeldung()
            && (this.datumFreischaltungTagesschule.isBefore(this.gueltigkeit.gueltigAb)
                || this.datumFreischaltungTagesschule.isSame(moment([])));
    }

    public isTageschulenAnmeldungAktiv(): boolean {
        return this.isTagesschulenAnmeldungKonfiguriert() && this.datumFreischaltungTagesschule.isBefore(moment());
    }

    public hasTagesschulenAnmeldung(): boolean {
        return this._datumFreischaltungTagesschule !== null && this.datumFreischaltungTagesschule !== undefined;
    }

    public isEntwurf(): boolean {
        return this.status === TSGesuchsperiodeStatus.ENTWURF;
    }
}

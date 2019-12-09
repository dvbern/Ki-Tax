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
import {TSZahlungsauftragsstatus} from './enums/TSZahlungsauftragstatus';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSGemeinde} from './TSGemeinde';
import {TSZahlung} from './TSZahlung';
import {TSDateRange} from './types/TSDateRange';

export class TSZahlungsauftrag extends TSAbstractDateRangedEntity {

    private _datumGeneriert: moment.Moment;
    private _datumFaellig: moment.Moment;
    private _status: TSZahlungsauftragsstatus;
    private _beschrieb: string;
    private _betragTotalAuftrag: number;
    private _hasNegativeZahlungen: boolean = false;
    private _gemeinde: TSGemeinde;
    private _zahlungen: Array<TSZahlung>;

    public constructor(
        gueltigkeit?: TSDateRange,
        datumGeneriert?: moment.Moment,
        datumFaellig?: moment.Moment,
        status?: TSZahlungsauftragsstatus,
        beschrieb?: string,
        betragTotalAuftrag?: number,
        hasNegativeZahlungen?: boolean | false,
        gemeinde?: TSGemeinde,
        zahlungen?: Array<TSZahlung>,
    ) {
        super(gueltigkeit);
        this._datumGeneriert = datumGeneriert;
        this._datumFaellig = datumFaellig;
        this._status = status;
        this._beschrieb = beschrieb;
        this._betragTotalAuftrag = betragTotalAuftrag;
        this._hasNegativeZahlungen = hasNegativeZahlungen;
        this._gemeinde = gemeinde;
        this._zahlungen = zahlungen;
    }

    public get datumGeneriert(): moment.Moment {
        return this._datumGeneriert;
    }

    public set datumGeneriert(value: moment.Moment) {
        this._datumGeneriert = value;
    }

    public get datumFaellig(): moment.Moment {
        return this._datumFaellig;
    }

    public set datumFaellig(value: moment.Moment) {
        this._datumFaellig = value;
    }

    public get beschrieb(): string {
        return this._beschrieb;
    }

    public set beschrieb(value: string) {
        this._beschrieb = value;
    }

    public get betragTotalAuftrag(): number {
        return this._betragTotalAuftrag;
    }

    public set betragTotalAuftrag(value: number) {
        this._betragTotalAuftrag = value;
    }

    public get hasNegativeZahlungen(): boolean {
        return this._hasNegativeZahlungen;
    }

    public set hasNegativeZahlungen(value: boolean) {
        this._hasNegativeZahlungen = value;
    }

    public get zahlungen(): Array<TSZahlung> {
        return this._zahlungen;
    }

    public set zahlungen(value: Array<TSZahlung>) {
        this._zahlungen = value;
    }

    public get status(): TSZahlungsauftragsstatus {
        return this._status;
    }

    public set status(value: TSZahlungsauftragsstatus) {
        this._status = value;
    }

    public get gemeinde(): TSGemeinde {
        return this._gemeinde;
    }

    public set gemeinde(value: TSGemeinde) {
        this._gemeinde = value;
    }
}

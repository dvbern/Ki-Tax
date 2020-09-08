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
import {TSZahlungslaufTyp} from './enums/TSZahlungslaufTyp';
import {TSAbstractDateRangedEntity} from './TSAbstractDateRangedEntity';
import {TSGemeinde} from './TSGemeinde';
import {TSZahlung} from './TSZahlung';
import {TSDateRange} from './types/TSDateRange';

export class TSZahlungsauftrag extends TSAbstractDateRangedEntity {

    public zahlungslaufTyp: TSZahlungslaufTyp;
    public datumGeneriert: moment.Moment;
    public datumFaellig: moment.Moment;
    public status: TSZahlungsauftragsstatus;
    public beschrieb: string;
    public betragTotalAuftrag: number;
    public hasNegativeZahlungen: boolean = false;
    public gemeinde: TSGemeinde;
    public zahlungen: Array<TSZahlung>;

    public constructor(
        zahlungslaufTyp?: TSZahlungslaufTyp,
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
        this.zahlungslaufTyp = zahlungslaufTyp;
        this.datumGeneriert = datumGeneriert;
        this.datumFaellig = datumFaellig;
        this.status = status;
        this.beschrieb = beschrieb;
        this.betragTotalAuftrag = betragTotalAuftrag;
        this.hasNegativeZahlungen = hasNegativeZahlungen;
        this.gemeinde = gemeinde;
        this.zahlungen = zahlungen;
    }
}

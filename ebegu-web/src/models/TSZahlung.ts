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

import {TSBetreuungsangebotTyp} from './enums/betreuung/TSBetreuungsangebotTyp';
import {TSZahlungsstatus} from './enums/TSZahlungsstatus';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';

export class TSZahlung extends TSAbstractMutableEntity {

    public empfaengerName: string;
    public betreuungsangebotTyp: TSBetreuungsangebotTyp;
    public status: TSZahlungsstatus;
    public betragTotalZahlung: number;

    public constructor(
        empfaengerName?: string,
        betreuungsangebotTyp?: TSBetreuungsangebotTyp,
        status?: TSZahlungsstatus,
        betragTotalZahlung?: number
    ) {
        super();
        this.empfaengerName = empfaengerName;
        this.betreuungsangebotTyp = betreuungsangebotTyp;
        this.status = status;
        this.betragTotalZahlung = betragTotalZahlung;
    }
}

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
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {CONSTANTS} from '../app/core/constants/CONSTANTS';

export class TSMandant extends TSAbstractMutableEntity {

    public name: string;
    public angebotTS: boolean;
    public angebotFI: boolean;
    public mandantIdentifier: string;

    public constructor(name?: string) {
        super();
        this.name = name;
    }

    // TODO: dies muss beim Mandanten gespeichert werden, sobald Mandantenfähigkeit umgesetzt wird
    public static get earliestDateOfTSAnmeldung(): moment.Moment {
        return moment(CONSTANTS.EARLIEST_DATE_OF_TS_ANMELDUNG);
    }
}

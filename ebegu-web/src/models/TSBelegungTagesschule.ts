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
import {TSAbholungTagesschule} from './enums/TSAbholungTagesschule';
import {TSAbstractMutableEntity} from './TSAbstractMutableEntity';
import {TSBelegungTagesschuleModul} from './TSBelegungTagesschuleModul';

export class TSBelegungTagesschule extends TSAbstractMutableEntity {

    public belegungTagesschuleModule: Array<TSBelegungTagesschuleModul> = [];
    public eintrittsdatum: moment.Moment;
    public planKlasse: string;
    public abholungTagesschule: TSAbholungTagesschule;
    public bemerkung: string;
    public abweichungZweitesSemester: boolean;
    public keineKesbPlatzierung: boolean;

}

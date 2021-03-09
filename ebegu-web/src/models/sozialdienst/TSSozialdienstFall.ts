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

import * as moment from 'moment';
import {TSSozialdienstFallStatus} from '../enums/TSSozialdienstFallStatus';
import {TSAbstractEntity} from '../TSAbstractEntity';
import {TSAdresse} from '../TSAdresse';
import {TSSozialdienst} from './TSSozialdienst';

export class TSSozialdienstFall extends TSAbstractEntity {
    public name: string;
    public vorname: string;
    public status: TSSozialdienstFallStatus;
    public adresse: TSAdresse;
    public geburtsdatum: moment.Moment;
    public sozialdienst: TSSozialdienst;
}

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import * as moment from 'moment';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Component({
    selector: 'dv-rueckforderung-mitteilungen',
    templateUrl: './rueckforderung-mitteilungen.component.html',
    styleUrls: ['./rueckforderung-mitteilungen.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungMitteilungenComponent implements OnChanges {
    @Input()
    public rueckforderungMitteilungen: TSRueckforderungMitteilung[];

    public ngOnChanges(changes: SimpleChanges): void {
        if (
            !changes.rueckforderungMitteilungen ||
            !changes.rueckforderungMitteilungen.currentValue
        ) {
            return;
        }
        this.rueckforderungMitteilungen.sort((a, b) => {
            if (a.sendeDatum.isBefore(b.sendeDatum)) {
                return -1;
            }
            return 1;
        });
    }

    public formatDateTime(date: moment.Moment): string {
        return date.format(CONSTANTS.DATE_TIME_FORMAT);
    }
}

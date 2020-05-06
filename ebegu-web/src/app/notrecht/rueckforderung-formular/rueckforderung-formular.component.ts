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

import {Component, ChangeDetectionStrategy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Transition} from '@uirouter/core';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';

@Component({
    selector: 'dv-rueckforderung-formular',
    templateUrl: './rueckforderung-formular.component.html',
    styleUrls: ['./rueckforderung-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class RueckforderungFormularComponent implements OnInit {

    @ViewChild(NgForm) private readonly form: NgForm;

    public rueckforderungFormular: TSRueckforderungFormular;

    public constructor(
        private readonly $transition$: Transition,
        private readonly notrechtRS: NotrechtRS
    )
        {
    }

    public ngOnInit(): void {
        const rueckforederungFormId: string = this.$transition$.params().benutzerId;

        if (!rueckforederungFormId) {
            return;
        }


        this.rueckforderungFormular = new TSRueckforderungFormular();
    }

    public saveRueckforderungFormular(): void{
        // TODO
    }
}

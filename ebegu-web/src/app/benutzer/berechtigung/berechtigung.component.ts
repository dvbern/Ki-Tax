/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSRole} from '../../../models/enums/TSRole';
import TSBerechtigung from '../../../models/TSBerechtigung';
import TSInstitution from '../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import EbeguUtil from '../../../utils/EbeguUtil';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

let nextId = 0;

@Component({
    selector: 'dv-berechtigung',
    templateUrl: './berechtigung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class BerechtigungComponent {

    @Input() public berechtigung: TSBerechtigung;
    @Input() public disabled: boolean = false;
    @Input() public readonly exludedRoles: TSRole[] = [];

    public readonly inputId = `berechtigung-${nextId++}`;
    public readonly rolleId: string;
    public readonly institutionId: string;
    public readonly traegerschaftId: string;

    public readonly institutionen$: Observable<TSInstitution[]>;
    public readonly traegerschaften$: Observable<TSTraegerschaft[]>;

    public readonly compareById = EbeguUtil.compareById;

    constructor(
        public readonly form: NgForm,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftenRS: TraegerschaftRS,
    ) {
        this.rolleId = 'rolle-' + this.inputId;
        this.institutionId = 'institution-' + this.inputId;
        this.traegerschaftId = 'treagerschaft-' + this.inputId;

        this.institutionen$ = from(this.institutionRS.getAllInstitutionen())
            .pipe(map(arr => arr.sort(EbeguUtil.compareByName)));

        this.traegerschaften$ = from(this.traegerschaftenRS.getAllTraegerschaften())
            .pipe(map(arr => arr.sort(EbeguUtil.compareByName)));
    }
}

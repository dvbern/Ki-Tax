/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-institution-ferieninsel',
    templateUrl: './edit-institution-ferieninsel.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ],
})

export class EditInstitutionFerieninselComponent {

    @Input() public stammdaten: TSInstitutionStammdaten;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
    ) {
    }

    // TODO (hefr) das muss dann irgendwie vom äusseren aufgerufen werden!
    private persistStammdaten(): void {
    }
}

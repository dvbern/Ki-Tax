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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';

@Component({
    selector: 'dv-wizard-side-nav',
    templateUrl: './wizard-side-nav.component.html',
    styleUrls: ['./wizard-side-nav.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardSideNavComponent implements OnInit {

    @Input() public readonly id: string;
    @Input() public readonly wizardTyp: string;
    @Input() public readonly status: string;
    @Input() public readonly gemeinde: TSGemeinde;

    public wizardSteps$: Observable<TSWizardStepX[]>;

    public constructor(
        private readonly wizardStepXRS: WizardStepXRS
    ) {}

    public ngOnInit(): void {
        this.wizardSteps$ = this.wizardStepXRS.getAllSteps();
    }

    public isInBearbeitung(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.IN_BEARBEITUNG;
    }

    public isOK(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.OK;
    }

    public isNOK(stepX: TSWizardStepX): boolean {
        return stepX.status === TSWizardStepStatus.NOK;
    }
}

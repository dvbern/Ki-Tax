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

import {ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSWizardStepX} from '../../../models/TSWizardStepX';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {WizardStepXRS} from '../../core/service/wizardStepXRS.rest';
import {LastenausgleichTSService} from '../services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-lastenausgleich-ts',
    templateUrl: './lastenausgleich-ts.component.html',
    styleUrls: ['./lastenausgleich-ts.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTSComponent implements OnInit, OnDestroy {

    @Input() public  lastenausgleichId: string;

    private lATSAngabenGemeindeContainer: TSLastenausgleichTagesschuleAngabenGemeindeContainer;
    private subscription: Subscription;

    public wizardSteps$: Observable<TSWizardStepX[]>;
    public wizardTyp = 'LASTENAUSGLEICH_TS';

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly wizardStepXRS: WizardStepXRS
    ) {
    }

    public ngOnInit(): void {
        this.lastenausgleichTSService.updateLATSAngabenGemeindeContainer(this.lastenausgleichId);
        this.subscription = this.lastenausgleichTSService.getLATSAngabenGemeindeContainer()
            .subscribe(container => {
                this.lATSAngabenGemeindeContainer = container;
                // update wizard steps every time LATSAngabenGemeindeContainer is reloaded
                this.wizardStepXRS.updateSteps(this.wizardTyp, this.lastenausgleichId);
            });
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public showToolbar(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showKommentare(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }
}

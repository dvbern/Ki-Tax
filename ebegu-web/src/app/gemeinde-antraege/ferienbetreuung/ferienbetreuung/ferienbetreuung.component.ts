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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../../core/logging/LogFactory';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {FerienbetreuungService} from '../services/ferienbetreuung.service';

const LOG = LogFactory.createLog('FerienbetreuungComponent');

@Component({
    selector: 'dv-ferienbetreuung',
    templateUrl: './ferienbetreuung.component.html',
    styleUrls: ['./ferienbetreuung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FerienbetreuungComponent implements OnInit {

    @Input()
    public ferienbetreuungId: string;

    public wizardTyp = TSWizardStepXTyp.FERIENBETREUUNG;
    public ferienbetreuungContainer: TSFerienbetreuungAngabenContainer;

    private subscription: Subscription;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly ferienbetreuungService: FerienbetreuungService,
        private readonly wizardStepXRS: WizardStepXRS
    ) {
    }

    public ngOnInit(): void {
        this.ferienbetreuungService.updateFerienbetreuungContainerStore(this.ferienbetreuungId);
        this.subscription = this.ferienbetreuungService.getFerienbetreuungContainer()
            .subscribe(container => {
                this.ferienbetreuungContainer = container;
                // update wizard steps every time LATSAngabenGemeindeContainer is reloaded
                this.wizardStepXRS.updateSteps(this.wizardTyp, this.ferienbetreuungId);
            }, err => LOG.error(err));
    }

    public showKommentare(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
        this.ferienbetreuungService.emptyStore();
    }
}

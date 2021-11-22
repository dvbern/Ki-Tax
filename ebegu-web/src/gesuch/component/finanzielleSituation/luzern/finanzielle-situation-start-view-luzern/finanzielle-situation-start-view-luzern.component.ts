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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {IPromise} from 'angular';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitLuzernView} from '../AbstractFinSitLuzernView';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-finanzielle-situation-start-view-luzern',
    templateUrl: '../finanzielle-situation-luzern.component.html',
    styleUrls: ['../finanzielle-situation-luzern.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartViewLuzernComponent extends AbstractFinSitLuzernView implements OnInit {

    @ViewChild(NgForm) private readonly form: NgForm;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager);
    }

    public ngOnInit(): void {
    }

    public isGemeinsam(): boolean {
        // if we don't need two antragsteller for gesuch, this is the component for both antragsteller together
        return FinanzielleSituationLuzernService.finSitNeedsTwoAntragsteller(this.gesuchModelManager);
    }

    public getAntragstellerNummer(): number {
        // this is always antragsteller 1. if we have two antragsteller, we have angaben-gesuchsteller-2 component
        return 1;
    }

    public getTrue(): any {
        return true;
    }

    public getSubStepIndex(): number {
        return 1;
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.LUZERN_START;
    }

    public isGesuchValid(): boolean {
        if (!this.form.valid) {
            EbeguUtil.selectFirstInvalid();
        }

        return this.form.valid;
    }

    public prepareSave(): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            return undefined;
        }
        return this.save();
    }
}

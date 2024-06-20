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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {IPromise} from 'angular';
import {ApplicationPropertyRS} from '../../../../../app/core/rest-services/applicationPropertyRS.rest';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitLuzernView} from '../AbstractFinSitLuzernView';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-angaben-gesuchsteller2',
    templateUrl: '../finanzielle-situation-luzern.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AngabenGesuchsteller2Component extends AbstractFinSitLuzernView {
    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected authServiceRS: AuthServiceRS,
        protected readonly translate: TranslateService,
        protected readonly applicationPropertyRS: ApplicationPropertyRS
    ) {
        super(
            gesuchModelManager,
            wizardStepManager,
            2,
            finSitLuService,
            authServiceRS,
            translate,
            applicationPropertyRS
        );
    }

    public isGemeinsam(): boolean {
        return false;
    }

    public getAntragstellerNummer(): number {
        return 2;
    }

    public getTrue(): any {
        return true;
    }

    public getSubStepIndex(): number {
        return 2;
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.LUZERN_GS2;
    }

    public prepareSave(
        onResult: (arg: any) => any
    ): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    protected save(
        onResult: (arg: any) => any
    ): angular.IPromise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager
            .saveFinanzielleSituation()
            .then(
                (
                    finanzielleSituationContainer: TSFinanzielleSituationContainer
                ) => {
                    this.updateWizardStepStatus();
                    onResult(finanzielleSituationContainer);
                    return finanzielleSituationContainer;
                }
            )
            .catch(error => {
                throw error;
            });
    }

    public isNotSozialhilfeBezueger(): boolean {
        return true;
    }
}

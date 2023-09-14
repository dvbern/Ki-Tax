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

import {ChangeDetectionStrategy, Component, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {IPromise} from 'angular';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFamiliensituation} from '../../../../../models/TSFamiliensituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSGesuch} from '../../../../../models/TSGesuch';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitLuzernView} from '../AbstractFinSitLuzernView';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';
import {ResultatComponent} from '../resultat/resultat.component';

@Component({
    selector: 'dv-finanzielle-situation-start-view-luzern',
    templateUrl: '../finanzielle-situation-luzern.component.html',
    styleUrls: ['../finanzielle-situation-luzern.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinanzielleSituationStartViewLuzernComponent extends AbstractFinSitLuzernView {

    @ViewChild(ResultatComponent) private readonly resultatComponent: ResultatComponent;

    public constructor(
        protected gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected finSitLuService: FinanzielleSituationLuzernService,
        protected authServiceRS: AuthServiceRS,
        protected readonly translate: TranslateService
    ) {
        super(gesuchModelManager, wizardStepManager, 1, finSitLuService, authServiceRS, translate);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.FINANZIELLE_SITUATION_LUZERN,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public isGemeinsam(): boolean {
        // if we don't need two separate antragsteller for gesuch, this is the component for both antragsteller together
        // or only for the single antragsteller
        return !FinanzielleSituationLuzernService.finSitNeedsTwoSeparateAntragsteller(this.gesuchModelManager)
            && EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
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

    public prepareSave(onResult: (arg: any) => any): IPromise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    protected save(onResult: (arg: any) => any): IPromise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then((gesuch: TSGesuch) => {
                this.model.copyFinSitDataFromGesuch(gesuch);
                const isSozialhilfeBezueger = gesuch.extractFamiliensituation().sozialhilfeBezueger;
                if (this.isGemeinsam() || this.gesuchModelManager.isLastGesuchsteller() || isSozialhilfeBezueger) {
                    this.updateWizardStepStatus();
                }
                if (isSozialhilfeBezueger) {
                    onResult(isSozialhilfeBezueger);
                    return undefined;
                }
                onResult(gesuch.gesuchsteller1.finanzielleSituationContainer);
                return gesuch.gesuchsteller1.finanzielleSituationContainer;
            }).catch(error => {
                throw(error);
            });
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.gesuchModelManager.getFamiliensituation();
    }

    public isNotSozialhilfeBezueger(): boolean {
        return EbeguUtil.isNotNullAndFalse(this.model.familienSituation.sozialhilfeBezueger);
    }
}

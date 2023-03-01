/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component} from '@angular/core';
import {IPromise} from 'angular';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

const LOG = LogFactory.createLog('FinanzielleSituationAppenzellViewComponent');

@Component({
    selector: 'dv-finanzielle-situation-appenzell-view',
    templateUrl: './finanzielle-situation-appenzell-view.component.html',
    styleUrls: ['./finanzielle-situation-appenzell-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinanzielleSituationAppenzellViewComponent extends AbstractGesuchViewX<TSFinanzModel> {

    public constructor(
        protected ref: ChangeDetectorRef,
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL);
        // wie ich es verstehe sind wir immer auf dem 1 Antragsteller, entweder gemeinsam oder nicht
        // falls gemeinsam soll die FinSit fuer beide in einmal erfasst werden
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            1);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setGesuchstellerNumber(1);
        if(EbeguUtil.isNullOrUndefined(this.getModel().finanzielleSituationJA.finSitZusatzangabenAppenzell)){
            this.getModel().finanzielleSituationJA.finSitZusatzangabenAppenzell = new TSFinSitZusatzangabenAppenzell();
        }
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public getAntragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
    }

    public getAntragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }

    public getAntragstellerNumber(): number {
        // im moment gibt es nur antragsteller 1
        return 1;
    }

    public isGemeinsam(): boolean {
        // TODO definieren ob er gemeinsam ist oder nicht
        return false;
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public getSubStepIndex(): number {
        return 0;
    }

    public prepareSave(onResult: (arg: any) => void): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    private save(onResult: (arg: any) => any): Promise<TSFinanzielleSituationContainer> {
        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituation()
            .then(async (finanzielleSituationContainer: TSFinanzielleSituationContainer) => {
                // TODO to adapt when step are clear
                await this.updateWizardStepStatus();

                onResult(finanzielleSituationContainer);
                return finanzielleSituationContainer;
            }).catch(error => {
                throw(error);
            }) as Promise<TSFinanzielleSituationContainer>;
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    private updateWizardStepStatus(): IPromise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL,
                TSWizardStepStatus.OK);
    }
}

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
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {Observable} from 'rxjs';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSFinSitZusatzangabenAppenzell} from '../../../../../models/TSFinSitZusatzangabenAppenzell';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationAppenzellService} from '../finanzielle-situation-appenzell.service';

const LOG = LogFactory.createLog('FinanzielleSituationAppenzellViewComponent');

@Component({
    selector: 'dv-finanzielle-situation-appenzell-view',
    templateUrl: './finanzielle-situation-appenzell-view.component.html',
    styleUrls: ['./finanzielle-situation-appenzell-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FinanzielleSituationAppenzellViewComponent extends AbstractGesuchViewX<TSFinanzModel> {

    private readonly gesuchstellerNumber: number;

    public constructor(
        protected ref: ChangeDetectorRef,
        protected readonly gesuchModelManager: GesuchModelManager,
        protected readonly wizardStepManager: WizardStepManager,
        private readonly $transition$: Transition,
        private readonly finanzielleSituationService: FinanzielleSituationAppenzellService,
        private readonly translate: TranslateService
    ) {
        super(gesuchModelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL);
        this.gesuchstellerNumber = parseInt(this.$transition$.params().gesuchstellerNumber, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            FinanzielleSituationAppenzellService.finSitNeedsTwoSeparateAntragsteller(gesuchModelManager),
            this.gesuchstellerNumber);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        // in Appenzell stellen wir die Frage nach dem Sozialhilfebezüger nicht. Deshalb setzen wir den immer auf false.
        this.model.sozialhilfeBezueger = false;
        this.gesuchModelManager.setGesuchstellerNumber(this.gesuchstellerNumber);
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
        return this.gesuchstellerNumber;
    }

    // die Frage wird nur auf dem ersten Step gezeigt und nur, falls das Gesuch überhaupt einen
    // zweiten Antragsteller hat.
    public showQuestionGemeinsameSteuererklaerung(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2)
        && this.getSubStepIndex() === 1;
    }

    public isGemeinsam(): boolean {
        return this.model.gemeinsameSteuererklaerung;
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public getSubStepIndex(): number {
        if (this.gesuchstellerNumber === 1) {
            return 1;
        } else if (this.gesuchstellerNumber === 2) {
            return 2;
        }
        LOG.error('SubStepIndex not defined');
        return undefined;
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        if (this.gesuchstellerNumber === 1) {
            return TSFinanzielleSituationSubStepName.APPENZELL_START;
        } else if (this.gesuchstellerNumber === 2) {
            return TSFinanzielleSituationSubStepName.APPENZELL_GS2;
        }
        LOG.error('SubStepName not defined');
        return undefined;
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

    public getMassgebendesEinkommen$(): Observable<TSFinanzielleSituationResultateDTO> {
        return this.finanzielleSituationService.massgebendesEinkommenStore;
    }

    public getFinSitTitle(): string {
        const title = this.translate.instant('APPENZELL_TITEL_FIN_SIT') as string;
        if (this.getAntragstellerNumber() === 1) {
            if (this.isGemeinsam()) {
                return `${title} ${this.getAntragsteller1Name()} + ${this.getAntragsteller2Name()}`;
            } else {
                return title + this.getAntragsteller1Name();
            }
        } else if (this.getAntragstellerNumber() === 2) {
            return title + this.getAntragsteller2Name();
        }
        LOG.error('wrong antragstellerNumber');
        return '';
    }
}

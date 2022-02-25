/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IPromise} from 'angular';
import {DvDialog} from '../../../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationTyp} from '../../../../../models/enums/TSFinanzielleSituationTyp';
import {isSteuerdatenAnfrageStatusErfolgreich} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationAufteilungDialogController} from '../../../../dialog/FinanzielleSituationAufteilungDialogController';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewController} from '../../../abstractGesuchView';
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

const aufteilungDialogTemplate = require('../../../../dialog/finanzielleSituationAufteilungDialogTemplate.html');

export class FinanzielleSituationResultateViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./finanzielleSituationResultateView.html');
    public controller = FinanzielleSituationResultateViewController;
    public controllerAs = 'vm';
}

/**
 * Controller fuer die Finanzielle Situation
 */
export class FinanzielleSituationResultateViewController extends AbstractGesuchViewController<TSFinanzModel> {

    public static $inject: string[] = [
        'GesuchModelManager',
        'BerechnungsManager',
        'ErrorService',
        'WizardStepManager',
        '$scope',
        '$timeout',
        'DvDialog'
    ];

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly dvDialog: DvDialog,
    ) {
        super(gesuchModelManager,
            berechnungsManager,
            wizardStepManager,
            $scope,
            TSWizardStepName.FINANZIELLE_SITUATION,
            $timeout);

        this.initModelAndCalculate();
    }

    private initModelAndCalculate(): void {
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());

        this.calculate();
    }

    public showGS2(): boolean {
        return this.model.isGesuchsteller2Required();
    }

    public save(): IPromise<void> {
        if (!this.isGesuchValid()) {
            return undefined;
        }

        this.model.copyFinSitDataToGesuch(this.gesuchModelManager.getGesuch());
        if (!this.form.$dirty) {
            // If there are no changes in form we don't need anything to update on Server and we could return the
            // promise immediately
            // Update wizardStepStatus also if the form is empty and not dirty
            return this.updateWizardStepStatus();
        }

        this.errorService.clearAll();

        if (!this.gesuchModelManager.getGesuch().gesuchsteller1) {
            return undefined;
        }

        this.gesuchModelManager.setGesuchstellerNumber(1);
        if (this.gesuchModelManager.getGesuch().gesuchsteller2) {
            return this.gesuchModelManager.saveFinanzielleSituation().then(() => {
                this.gesuchModelManager.setGesuchstellerNumber(2);
                return this.saveFinanzielleSituation();
            });
        }
        return this.saveFinanzielleSituation();
    }

    private saveFinanzielleSituation(): IPromise<void> {
        return this.gesuchModelManager.saveFinanzielleSituation().then(() => {
            return this.updateWizardStepStatus();
        });
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    private updateWizardStepStatus(): IPromise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION,
                TSWizardStepStatus.OK);
    }

    public calculate(): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model);
    }

    // init weg

    public getFinanzielleSituationGS1(): TSFinanzielleSituationContainer {
        return this.model.finanzielleSituationContainerGS1;

    }

    public getFinanzielleSituationGS2(): TSFinanzielleSituationContainer {
        return this.model.finanzielleSituationContainerGS2;
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.berechnungsManager.finanzielleSituationResultate;
    }

    public isFKJV(): boolean {
        return this.getGesuch().finSitTyp === TSFinanzielleSituationTyp.BERN_FKJV;
    }

    public hasGS1SteuerDatenErfolgreichAbgefragt(): boolean {
        return this.getFinanzielleSituationGS1().finanzielleSituationJA.steuerdatenZugriff &&
            !this.getFinanzielleSituationGS1().finanzielleSituationJA.steuerdatenAbfrageStatus.startsWith('FAILED');
    }
    public hasGS2SteuerDatenErfolgreichAbgefragt(): boolean {
        return this.getFinanzielleSituationGS2().finanzielleSituationJA.steuerdatenZugriff &&
            !this.getFinanzielleSituationGS2().finanzielleSituationJA.steuerdatenAbfrageStatus.startsWith('FAILED');
    }

    public startAufteilung(): void {
        this.dvDialog.showDialogFullscreen(aufteilungDialogTemplate, FinanzielleSituationAufteilungDialogController)
            .then(() => {
                this.initModelAndCalculate();
            });
    }

    public isSteueranfrageErfolgreichForGS1(): boolean {
        const finSit = this.gesuchModelManager.getGesuch().gesuchsteller1.finanzielleSituationContainer.finanzielleSituationJA;
        return EbeguUtil.isNotNullAndTrue(finSit.steuerdatenZugriff)
            && isSteuerdatenAnfrageStatusErfolgreich(finSit.steuerdatenAbfrageStatus);
    }

    /*
    Falls gemeinsameSteuererklärung = true ist, wird die Abfrage immer nur für GS1 gemacht. Deshalb reicht es hier
    wenn wir prüfen, ob die Steuerabfrage für gs1 erfolgrech war
     */
    public showAufteilung(): boolean {
        return this.isSteueranfrageErfolgreichForGS1()
            && this.gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.gemeinsameSteuererklaerung;
    }
}

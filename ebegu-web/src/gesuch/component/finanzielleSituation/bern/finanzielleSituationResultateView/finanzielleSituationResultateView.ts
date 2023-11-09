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

import {IComponentOptions, IPromise} from 'angular';
import {DvDialog} from '../../../../../app/core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {DemoFeatureRS} from '../../../../../app/core/service/demoFeatureRS.rest';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {isSteuerdatenAnfrageStatusErfolgreich} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {
    FinanzielleSituationAufteilungDialogController
} from '../../../../dialog/FinanzielleSituationAufteilungDialogController';
import {BerechnungsManager} from '../../../../service/berechnungsManager';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {GesuchRS} from '../../../../service/gesuchRS.rest';
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
        'DvDialog',
        'GesuchRS',
        'DemoFeatureRS'
    ];

    public constructor(
        gesuchModelManager: GesuchModelManager,
        berechnungsManager: BerechnungsManager,
        private readonly errorService: ErrorService,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        $timeout: ITimeoutService,
        private readonly dvDialog: DvDialog,
        private readonly gesuchRS: GesuchRS,
        private readonly demoFeatureRS: DemoFeatureRS
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
        this.initFinSitVorMutation();
    }

    private async initFinSitVorMutation(): Promise<void> {
        // beim Erstgesuch macht dies keinen Sinn
        if (EbeguUtil.isNullOrUndefined(this.getGesuch().vorgaengerId)) {
            return;
        }
        const gesuchVorMutation = await this.gesuchRS.findVorgaengerGesuchNotIgnoriert(this.getGesuch().vorgaengerId);
        this.model.initFinSitVorMutation(gesuchVorMutation);
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
        return this.gesuchModelManager.saveFinanzielleSituation().then(() => this.updateWizardStepStatus());
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

    public hasGS1SteuerDatenErfolgreichAbgefragt(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getFinanzielleSituationGS1().finanzielleSituationJA.steuerdatenZugriff) &&
            (isSteuerdatenAnfrageStatusErfolgreich(
                this.getFinanzielleSituationGS1().finanzielleSituationJA.steuerdatenAbfrageStatus));
    }

    public hasGS2SteuerDatenErfolgreichAbgefragt(): boolean {
        return EbeguUtil.isNotNullAndTrue(this.getFinanzielleSituationGS2().finanzielleSituationJA.steuerdatenZugriff) &&
            (isSteuerdatenAnfrageStatusErfolgreich(
                this.getFinanzielleSituationGS2().finanzielleSituationJA.steuerdatenAbfrageStatus));
    }

    public startAufteilung(): void {
        this.dvDialog.showDialogFullscreen(aufteilungDialogTemplate, FinanzielleSituationAufteilungDialogController)
            .then(() => {
                this.initModelAndCalculate();
            });
    }

    /*
    Falls gemeinsameSteuererklärung = true ist, wird die Abfrage immer nur für GS1 gemacht. Deshalb reicht es hier
    wenn wir prüfen, ob die Steuerabfrage für gs1 erfolgrech war
     */
    public showAufteilung(): boolean {
        return this.hasGS1SteuerDatenErfolgreichAbgefragt()
            && this.gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.gemeinsameSteuererklaerung;
    }

    public getBruttovermoegenTooltipLabel(): string {
        if (this.isFKJV()) {
            return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP_FKJV';
        }
        return 'FINANZIELLE_SITUATION_VERMOEGEN_HELP';
    }

    public getFinanzielleSituationVorMutationGS1(): TSFinanzielleSituation | object {
        if (this.model.finanzielleSituationVorMutationGS1) {
            return this.model.finanzielleSituationVorMutationGS1;
        }
        // leeres objekt zurückgeben, damit wir den Nullcheck nicht immer machen müssen
        return {};
    }

    public getFinanzielleSituationVorMutationGS2(): TSFinanzielleSituation | object {
        if (this.model.finanzielleSituationVorMutationGS2) {
            return this.model.finanzielleSituationVorMutationGS2;
        }
        // leeres objekt zurückgeben, damit wir den Nullcheck nicht immer machen müssen
        return {};
    }
}

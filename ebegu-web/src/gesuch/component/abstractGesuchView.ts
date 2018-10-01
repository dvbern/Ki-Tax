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

import GesuchModelManager from '../service/gesuchModelManager';
import BerechnungsManager from '../service/berechnungsManager';
import {TSRole} from '../../models/enums/TSRole';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import WizardStepManager from '../service/wizardStepManager';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import TSExceptionReport from '../../models/TSExceptionReport';
import {TSMessageEvent} from '../../models/enums/TSErrorEvent';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import EbeguUtil from '../../utils/EbeguUtil';
import IPromise = angular.IPromise;
import IFormController = angular.IFormController;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export default class AbstractGesuchViewController<T> {

    public $scope: IScope;
    public gesuchModelManager: GesuchModelManager;
    public berechnungsManager: BerechnungsManager;
    public wizardStepManager: WizardStepManager;
    public TSRole = TSRole;
    public TSRoleUtil = TSRoleUtil;
    private _model: T;
    public form: IFormController;
    public $timeout: ITimeoutService;

    public constructor($gesuchModelManager: GesuchModelManager, $berechnungsManager: BerechnungsManager,
                       wizardStepManager: WizardStepManager, $scope: IScope, stepName: TSWizardStepName,
                       $timeout: ITimeoutService) {
        this.gesuchModelManager = $gesuchModelManager;
        this.berechnungsManager = $berechnungsManager;
        this.wizardStepManager = wizardStepManager;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.wizardStepManager.setCurrentStep(stepName);
    }

    public $onInit() {
        /**
         * Grund fuer diesen Code ist:
         * Wenn der Server einen Validation-Fehler zurueckgibt, wird der DirtyPlugin nicht informiert und setzt das Form
         * auf !dirty. Dann kann der Benutzer nochmal auf Speichern klicken und die Daten werden gespeichert.
         * Damit dies nicht passiert, hoeren wir in allen Views auf diesen Event und setzen das Form auf dirty
         */
        this.$scope.$on(TSMessageEvent[TSMessageEvent.ERROR_UPDATE], (event: any, errors: Array<TSExceptionReport>) => {
            this.form.$dirty = true;
            this.form.$pristine = false;
        });
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    /**
     * Diese Methode prueft ob das Form valid ist. Sollte es nicht valid sein wird das erste fehlende Element gesucht
     * und fokusiert, damit der Benutzer nicht scrollen muss, um den Fehler zu finden.
     * Am Ende wird this.form.$valid zurueckgegeben
     */
    public isGesuchValid(): boolean {
        if (!this.form.$valid) {
           EbeguUtil.selectFirstInvalid();
        }
        return this.form.$valid;
    }

    public getGesuchId(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().id;
        } else {
            return '';
        }
    }

    public setGesuchStatus(status: TSAntragStatus): IPromise<TSAntragStatus> {
        if (this.gesuchModelManager) {
            return this.gesuchModelManager.saveGesuchStatus(status);
        }
        return undefined;
    }

    public isGesuchInStatus(status: TSAntragStatus): boolean {
        return this.gesuchModelManager.getGesuch() && status === this.gesuchModelManager.getGesuch().status;
    }

    public isBetreuungInStatus(status: TSBetreuungsstatus): boolean {
        if (this.gesuchModelManager.getBetreuungToWorkWith()) {
            return status === this.gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus;
        }
        return false;
    }

    public isMutation(): boolean {
        if (this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().isMutation();
        }
        return false;
    }

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public get model(): T {
        return this._model;
    }

    public set model(value: T) {
        this._model = value;
    }

    public extractFullNameGS1(): string {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller1) {
            return this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName();
        }
        return '';
    }

    public extractFullNameGS2(): string {
        if (this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller2) {
            return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
        }
        return '';
    }

    public $postLink() {
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, 200);
    }
}

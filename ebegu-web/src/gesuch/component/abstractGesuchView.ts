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

import {IController} from 'angular';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import {TSMessageEvent} from '../../models/enums/TSErrorEvent';
import {TSRole} from '../../models/enums/TSRole';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import TSExceptionReport from '../../models/TSExceptionReport';
import EbeguUtil from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import BerechnungsManager from '../service/berechnungsManager';
import GesuchModelManager from '../service/gesuchModelManager';
import WizardStepManager from '../service/wizardStepManager';
import IFormController = angular.IFormController;
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export default class AbstractGesuchViewController<T> implements IController {

    public $scope: IScope;
    public gesuchModelManager: GesuchModelManager;
    public berechnungsManager: BerechnungsManager;
    public wizardStepManager: WizardStepManager;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;
    private _model: T;
    public form: IFormController;
    public $timeout: ITimeoutService;

    public constructor(
        $gesuchModelManager: GesuchModelManager,
        $berechnungsManager: BerechnungsManager,
        wizardStepManager: WizardStepManager,
        $scope: IScope,
        stepName: TSWizardStepName,
        $timeout: ITimeoutService,
    ) {
        this.gesuchModelManager = $gesuchModelManager;
        this.berechnungsManager = $berechnungsManager;
        this.wizardStepManager = wizardStepManager;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.wizardStepManager.setCurrentStep(stepName);
    }

    public $onInit(): void {
        /**
         * Grund fuer diesen Code ist:
         * Wenn der Server einen Validation-Fehler zurueckgibt, wird der DirtyPlugin nicht informiert und setzt das Form
         * auf !dirty. Dann kann der Benutzer nochmal auf Speichern klicken und die Daten werden gespeichert.
         * Damit dies nicht passiert, hoeren wir in allen Views auf diesen Event und setzen das Form auf dirty
         */
        this.$scope.$on(TSMessageEvent[TSMessageEvent.ERROR_UPDATE], (_event: any, _errors: TSExceptionReport[]) => {
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
        return this.gesuchModelManager && this.gesuchModelManager.getGesuch() ?
            this.gesuchModelManager.getGesuch().id :
            '';
    }

    public setGesuchStatus(status: TSAntragStatus): IPromise<TSAntragStatus> {
        return this.gesuchModelManager ? this.gesuchModelManager.saveGesuchStatus(status) : undefined;
    }

    public isGesuchInStatus(status: TSAntragStatus): boolean {
        return this.gesuchModelManager.getGesuch() && status === this.gesuchModelManager.getGesuch().status;
    }

    public isBetreuungInStatus(status: TSBetreuungsstatus): boolean {
        return this.gesuchModelManager.getBetreuungToWorkWith() ?
            status === this.gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus :
            false;
    }

    public isMutation(): boolean {
        return this.gesuchModelManager.getGesuch() ? this.gesuchModelManager.getGesuch().isMutation() : false;
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
        return this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller1 ?
            this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName() :
            '';
    }

    public extractFullNameGS2(): string {
        return this.gesuchModelManager.getGesuch() && this.gesuchModelManager.getGesuch().gesuchsteller2 ?
            this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName() :
            '';
    }

    public $postLink(): void {
        const delay = 200;
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, delay);
    }
}

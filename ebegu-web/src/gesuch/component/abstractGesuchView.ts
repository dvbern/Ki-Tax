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
import {isVerfuegtOrSTV, TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import {TSMessageEvent} from '../../models/enums/TSErrorEvent';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import {TSRole} from '../../models/enums/TSRole';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSBetreuung} from '../../models/TSBetreuung';
import {TSExceptionReport} from '../../models/TSExceptionReport';
import {TSGesuch} from '../../models/TSGesuch';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {BerechnungsManager} from '../service/berechnungsManager';
import {GesuchModelManager} from '../service/gesuchModelManager';
import {WizardStepManager} from '../service/wizardStepManager';
import IFormController = angular.IFormController;
import IPromise = angular.IPromise;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;

export class AbstractGesuchViewController<T> implements IController {

    public readonly DEFAULT_DELAY: number = 200;

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
            if (this.form) {
                this.form.$dirty = true;
                this.form.$pristine = false;
            }
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
        return this.gesuchModelManager && this.getGesuch() ?
            this.getGesuch().id :
            '';
    }

    public setGesuchStatus(status: TSAntragStatus): IPromise<TSAntragStatus> {
        return this.gesuchModelManager ? this.gesuchModelManager.saveGesuchStatus(status) : undefined;
    }

    public isGesuchInStatus(status: TSAntragStatus): boolean {
        return this.getGesuch() && status === this.getGesuch().status;
    }

    public isBetreuungInStatus(status: TSBetreuungsstatus): boolean {
        return this.gesuchModelManager.getBetreuungToWorkWith() ?
            status === this.gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus :
            false;
    }

    public isMutation(): boolean {
        return this.getGesuch() ? this.getGesuch().isMutation() : false;
    }

    protected getGesuch(): TSGesuch {
        return this.gesuchModelManager.getGesuch();
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
        return this.getGesuch() && this.getGesuch().gesuchsteller1
            ? this.getGesuch().gesuchsteller1.extractFullName()
            : '';
    }

    public extractFullNameGS2(): string {
        return this.getGesuch() && this.getGesuch().gesuchsteller2
            ? this.getGesuch().gesuchsteller2.extractFullName()
            : '';
    }

    public $postLink(): void {
        this.doPostLinkActions(this.DEFAULT_DELAY);
    }

    public doPostLinkActions(delay: number): void {
        // always when a new site is loaded we set the semaphore back to false so a new transition can happen
        this.wizardStepManager.isTransitionInProgress = false;

        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, delay);
    }

    public isNullOrUndefined(value: any): boolean {
        return EbeguUtil.isNullOrUndefined(value);
    }

    public isNotNullOrUndefined(value: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(value);
    }

    public isMutationsmeldungAllowed(betreuung: TSBetreuung, isNewestGesuch: boolean): boolean {
        if (!betreuung || !this.gesuchModelManager.getGesuch()) {
            return false;
        }
        return (
                (
                    this.isMutation()
                    && (
                        betreuung.vorgaengerId
                        || betreuung.betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                    )
                )
                || (
                    !this.isMutation()
                    && (isVerfuegtOrSTV(this.gesuchModelManager.getGesuch().status)
                    || this.gesuchModelManager.getGesuch().gesperrtWegenBeschwerde)
                    && betreuung.betreuungsstatus === TSBetreuungsstatus.VERFUEGT
                )
            )
            && betreuung.betreuungsstatus !== TSBetreuungsstatus.WARTEN
            && this.gesuchModelManager.getGesuch().gesuchsperiode.status === TSGesuchsperiodeStatus.AKTIV
            && isNewestGesuch;
    }

    public getBasisjahr(): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr();
        }
        return undefined;
    }

    public getBasisjahrMinus1(): number | undefined {
        return this.getBasisjahrMinus(1);
    }

    public getBasisjahrMinus2(): number | undefined {
        return this.getBasisjahrMinus(2);
    }

    private getBasisjahrMinus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahr()) {
            return this.gesuchModelManager.getBasisjahr() - nbr;
        }
        return undefined;
    }

    public getBasisjahrPlus1(): number | undefined {
        return this.getBasisjahrPlus(1);
    }

    public getBasisjahrPlus2(): number | undefined {
        return this.getBasisjahrPlus(2);
    }

    private getBasisjahrPlus(nbr: number): number | undefined {
        if (this.gesuchModelManager && this.gesuchModelManager.getBasisjahrPlus(nbr)) {
            return this.gesuchModelManager.getBasisjahrPlus(nbr);
        }
        return undefined;
    }
}

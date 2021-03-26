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

import {IPromise, IQService} from 'angular';
import {LogFactory} from '../../app/core/logging/LogFactory';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {isAnyStatusOfVerfuegtOrKeinKontingent} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import {TSRole} from '../../models/enums/TSRole';
import {getTSWizardStepNameValues, TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import {TSGesuch} from '../../models/TSGesuch';
import {TSWizardStep} from '../../models/TSWizardStep';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {WizardStepRS} from './WizardStepRS.rest';

const LOG = LogFactory.createLog('WizardStepManager');

export class WizardStepManager {

    public static $inject = ['AuthServiceRS', 'WizardStepRS', '$q', 'AuthLifeCycleService'];

    private allowedSteps: Array<TSWizardStepName> = [];
    private readonly hiddenSteps: Array<TSWizardStepName> = []; // alle Steps die obwohl allowed, ausgeblendet werden
                                                                // muessen
    private wizardSteps: Array<TSWizardStep> = [];
    private currentStepName: TSWizardStepName; // keeps track of the name of the current step

    private wizardStepsSnapshot: Array<TSWizardStep> = [];

    // this semaphore will prevent a navigation to be executed again until the process is not finished
    public isTransitionInProgress: boolean = false;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly wizardStepRS: WizardStepRS,
        private readonly $q: IQService,
        private readonly authLifeCycleService: AuthLifeCycleService,
    ) {

        this.setAllowedStepsForRole(authServiceRS.getPrincipalRole());
        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .subscribe(
                () => this.setAllowedStepsForRole(this.authServiceRS.getPrincipalRole()),
                err => LOG.error(err),
            );
    }

    public getCurrentStep(): TSWizardStep {
        return this.getStepByName(this.currentStepName);
    }

    public setCurrentStep(stepName: TSWizardStepName): void {
        this.currentStepName = stepName;
    }

    public getCurrentStepName(): TSWizardStepName {
        return this.currentStepName;
    }

    public createWizardStep(
        gesuchId: string,
        stepName: TSWizardStepName,
        status: TSWizardStepStatus,
        bemerkungen: string,
        verfuegbar: boolean,
    ): TSWizardStep {

        const tsWizardStep = new TSWizardStep();
        tsWizardStep.gesuchId = gesuchId;
        tsWizardStep.wizardStepName = stepName;
        tsWizardStep.wizardStepStatus = status;
        tsWizardStep.bemerkungen = bemerkungen;
        tsWizardStep.verfuegbar = verfuegbar;
        return tsWizardStep;
    }

    /**
     * Initializes WizardSteps with one single Step GESUCH_ERSTELLEN which status is IN_BEARBEITUNG.
     * This method must be called only when the Gesuch doesn't exist yet.
     */
    public initWizardSteps(newFall: boolean): void {
        if (this.isStepVisible(TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN)
            && (!this.isStepStatusOk(TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN) || newFall)) {
            this.wizardSteps = [
                this.createWizardStep(undefined,
                    TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
                    TSWizardStepStatus.IN_BEARBEITUNG,
                    undefined,
                    true),
            ];
            this.currentStepName = TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN;
        } else {
            this.wizardSteps = [
                this.createWizardStep(undefined,
                    TSWizardStepName.GESUCH_ERSTELLEN,
                    TSWizardStepStatus.IN_BEARBEITUNG,
                    undefined,
                    true),
            ];
            this.currentStepName = TSWizardStepName.GESUCH_ERSTELLEN;
        }
        this.wizardSteps.push(
            this.createWizardStep(undefined,
                TSWizardStepName.FAMILIENSITUATION,
                TSWizardStepStatus.UNBESUCHT,
                'initFinSit dummy',
                false),
        );
    }

    public getAllowedSteps(): Array<TSWizardStepName> {
        return this.allowedSteps;
    }

    public getWizardSteps(): Array<TSWizardStep> {
        return this.wizardSteps;
    }

    public getVisibleSteps(): Array<TSWizardStepName> {
        return this.allowedSteps.filter(element =>
            !this.isStepHidden(element),
        );
    }

    public setAllowedStepsForRole(role: TSRole): void {
        if (TSRoleUtil.getTraegerschaftInstitutionOnlyRoles().indexOf(role) > -1) {
            this.setAllowedStepsForInstitutionTraegerschaft();

        } else if (TSRoleUtil.getSteueramtOnlyRoles().indexOf(role) > -1) {
            this.setAllowedStepsForSteueramt();

        } else if (TSRoleUtil.getAmtRole().concat(TSRole.GESUCHSTELLER).indexOf(role) > -1) {
            this.setAllowedStepsForAmtAndGesuchsteller();
            // TODO abklaeren ob die Gemeinde koennen auch sehe dieser Schritt
        } else {
            // Nur sozialdienst und superadmin koennen alle Step sehen
            this.setAllAllowedSteps();
        }
    }

    private setAllowedStepsForInstitutionTraegerschaft(): void {
        this.allowedSteps = [];
        this.allowedSteps.push(TSWizardStepName.FAMILIENSITUATION);
        this.allowedSteps.push(TSWizardStepName.GESUCHSTELLER);
        this.allowedSteps.push(TSWizardStepName.UMZUG);
        this.allowedSteps.push(TSWizardStepName.BETREUUNG);
        this.allowedSteps.push(TSWizardStepName.ABWESENHEIT);
        this.allowedSteps.push(TSWizardStepName.VERFUEGEN);
    }

    private setAllowedStepsForSteueramt(): void {
        this.allowedSteps = [];
        this.allowedSteps.push(TSWizardStepName.FAMILIENSITUATION);
        this.allowedSteps.push(TSWizardStepName.GESUCHSTELLER);
    }

    private setAllowedStepsForAmtAndGesuchsteller(): void {
        this.allowedSteps = [];
        this.allowedSteps.push(TSWizardStepName.GESUCH_ERSTELLEN);
        this.allowedSteps.push(TSWizardStepName.FAMILIENSITUATION);
        this.allowedSteps.push(TSWizardStepName.GESUCHSTELLER);
        this.allowedSteps.push(TSWizardStepName.UMZUG);
        this.allowedSteps.push(TSWizardStepName.KINDER);
        this.allowedSteps.push(TSWizardStepName.BETREUUNG);
        this.allowedSteps.push(TSWizardStepName.ABWESENHEIT);
        this.allowedSteps.push(TSWizardStepName.ERWERBSPENSUM);
        this.allowedSteps.push(TSWizardStepName.FINANZIELLE_SITUATION);
        this.allowedSteps.push(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
        this.allowedSteps.push(TSWizardStepName.DOKUMENTE);
        this.allowedSteps.push(TSWizardStepName.FREIGABE);
        this.allowedSteps.push(TSWizardStepName.VERFUEGEN);
    }

    private setAllAllowedSteps(): void {
        this.allowedSteps = getTSWizardStepNameValues();
    }

    /**
     * Sollten keine WizardSteps gefunden werden, wird die Methode initWizardSteps aufgerufen, um die
     * minimale Steps herzustellen. Die erlaubten Steps fuer den aktuellen Benutzer werden auch gesetzt
     */
    public findStepsFromGesuch(gesuchId: string): IPromise<void> {
        return this.wizardStepRS.findWizardStepsFromGesuch(gesuchId).then(response => {
            if (Array.isArray(response) && response.length > 0) {
                this.wizardSteps = response;
            } else {
                this.initWizardSteps(false);
            }
            this.backupCurrentSteps();
            this.setAllowedStepsForRole(this.authServiceRS.getPrincipalRole());
        });
    }

    public getStepByName(stepName: TSWizardStepName): TSWizardStep {

        return this.wizardSteps.filter((step: TSWizardStep) => {
            return step.wizardStepName === stepName;
        })[0];
    }

    /**
     * Der Step wird aktualisiert und die Liste von Steps wird nochmal aus dem Server geholt. Sollte der Status gleich
     * sein, wird nichts gemacht und undefined wird zurueckgegeben. Der Status wird auch auf verfuegbar gesetzt
     */
    public updateWizardStepStatus(stepName: TSWizardStepName, newStepStatus: TSWizardStepStatus): IPromise<void> {
        const step = this.getStepByName(stepName);
        if (step) {
            step.verfuegbar = true;
            if (this.needNewStatusSave(step.wizardStepStatus, newStepStatus)) {
                // nur wenn der Status sich geaendert hat updaten und steps laden
                step.wizardStepStatus = newStepStatus;
                return this.wizardStepRS.updateWizardStep(step).then((response: TSWizardStep) => {
                    return this.findStepsFromGesuch(response.gesuchId);
                });
            }
        }
        return this.$q.when();
    }

    public updateCurrentWizardStepStatusMutiert(): IPromise<void> {
        return this.wizardStepRS.setWizardStepMutiert(this.getCurrentStep().id).then((response: TSWizardStep) => {
            return this.findStepsFromGesuch(response.gesuchId);
        });
    }

    private needNewStatusSave(oldStepStatus: TSWizardStepStatus, newStepStatus: TSWizardStepStatus): boolean {
        if (oldStepStatus === newStepStatus) {
            return false;
        }

        if ((newStepStatus === TSWizardStepStatus.IN_BEARBEITUNG || newStepStatus === TSWizardStepStatus.WARTEN)
            && oldStepStatus !== TSWizardStepStatus.UNBESUCHT) {
            return false;
        }

        return !(newStepStatus === TSWizardStepStatus.OK && oldStepStatus === TSWizardStepStatus.MUTIERT);
    }

    /**
     * Like updateCurrentWizardStepStatus but it will only execute the action when the currentStep has the given
     * stepName. Use this method to avoid changing the status of a different Step than the one you have to change.
     */
    public updateCurrentWizardStepStatusSafe(
        stepName: TSWizardStepName,
        stepStatus: TSWizardStepStatus,
    ): IPromise<void> {
        if (this.getCurrentStepName() === stepName) {
            return this.updateCurrentWizardStepStatus(stepStatus);
        }
        return undefined;
    }

    /**
     * Der aktuelle Step wird aktualisiert und die Liste von Steps wird nochmal aus dem Server geholt. Sollte der
     * Status gleich sein, nichts wird gemacht und undefined wird zurueckgegeben.
     */
    public updateCurrentWizardStepStatus(stepStatus: TSWizardStepStatus): IPromise<void> {
        return this.updateWizardStepStatus(this.currentStepName, stepStatus);
    }

    /**
     * Just updates the current step as is
     */
    public updateCurrentWizardStep(): IPromise<void> {
        return this.wizardStepRS.updateWizardStep(this.getCurrentStep()).then((response: TSWizardStep) => {
            return this.findStepsFromGesuch(response.gesuchId);
        });
    }

    /**
     * Diese Methode ist eine Ausnahme. Im ersten Step haben wir das Problem, dass das Gesuch noch nicht existiert.
     * Deswegen koennen wir die Kommentare nicht direkt speichern. Die Loesung ist: nach dem das Gesuch erstellt wird
     * und somit auch die WizardSteps, holen wir diese aus der Datenbank, aktualisieren den Step GESUCH_ERSTELLEN mit
     * den Kommentaren und speichern dieses nochmal.
     */
    public updateFirstWizardStep(gesuchId: string): IPromise<void> {
        const firstStepBemerkungen = angular.copy(this.getCurrentStep().bemerkungen);
        return this.findStepsFromGesuch(gesuchId).then(() => {
            this.getCurrentStep().bemerkungen = firstStepBemerkungen;
            return this.updateCurrentWizardStep();
        });
    }

    /**
     * Gibt true zurueck wenn der Status vom naechsten Step != UNBESUCHT ist. D.h. wenn es verfuegbar ist
     */
    public isNextStepBesucht(gesuch: TSGesuch): boolean {
        const step = this.getStepByName(this.getNextStep(gesuch));
        if (!step) {
            return false;
        }

        return step.wizardStepStatus !== TSWizardStepStatus.UNBESUCHT;
    }

    /**
     * Gibt true zurueck wenn der naechste Step enabled (verfuegbar) ist
     */
    public isNextStepEnabled(gesuch: TSGesuch): boolean {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())
            && this.currentStepName === TSWizardStepName.GESUCHSTELLER) {
            // Dies ist ein Hack. Das Problem ist, dass der Step GESUCHSTELLER der letzte fuer das Steueramt ist, und
            // da er substeps hat, ist es sehr schwierig zu wissen, wann man darf und wann nicht. Wir sollten die ganze
            // Funktionalitaet von Steps verbessern
            return true;
        }
        return this.isStepAvailableViaBtn(this.getNextStep(gesuch), gesuch);
    }

    public getNextStep(gesuch: TSGesuch): TSWizardStepName {
        const allVisibleStepNames = this.getVisibleSteps();
        const currentPosition = allVisibleStepNames.indexOf(this.getCurrentStepName()) + 1;
        for (let i = currentPosition; i < allVisibleStepNames.length; i++) {
            if (this.isStepAvailableViaBtn(allVisibleStepNames[i], gesuch)) {
                return allVisibleStepNames[i];
            }
        }
        return undefined;
    }

    /**
     * iterate through the existing steps and get the previous one based on the current position
     */
    public getPreviousStep(gesuch: TSGesuch): TSWizardStepName {
        const allVisibleStepNames = this.getVisibleSteps();
        const currentPosition = allVisibleStepNames.indexOf(this.getCurrentStepName()) - 1;
        for (let i = currentPosition; i >= 0; i--) {
            if (this.isStepAvailableViaBtn(allVisibleStepNames[i], gesuch)) {
                return allVisibleStepNames[i];
            }
        }
        return undefined;
    }

    /**
     * gibt true zurueck wenn step mit next/prev button erreichbar sein soll
     */
    private isStepAvailableViaBtn(stepName: TSWizardStepName, gesuch: TSGesuch): boolean {
        if (gesuch) {
            const step = this.getStepByName(stepName);

            if (step !== undefined) {
                return this.isStepClickableForCurrentRole(step, gesuch)
                    || (
                        (gesuch.typ === TSAntragTyp.ERSTGESUCH || gesuch.typ === TSAntragTyp.ERNEUERUNGSGESUCH)
                        && step.wizardStepStatus === TSWizardStepStatus.UNBESUCHT
                        && !(
                            this.authServiceRS.isOneOfRoles(TSRoleUtil.getAllButAdministratorJugendamtRole())
                            && stepName === TSWizardStepName.VERFUEGEN
                        )
                    )
                    || (
                        gesuch.typ === TSAntragTyp.MUTATION
                        && step.wizardStepName === TSWizardStepName.FAMILIENSITUATION
                    );
            }
        }
        return false;  // wenn der step undefined ist geben wir mal verfuegbar zurueck
    }

    /**
     * gibt true zurueck wenn eins step fuer die aktuelle rolle disabled ist.
     * Wenn es keine sonderregel gibt wird der default der aus dem server empfangen wurde
     * zurueckgegeben
     */
    public isStepClickableForCurrentRole(step: TSWizardStep, gesuch: TSGesuch): boolean {
        if (!gesuch) {
            return false;
        }

        if (step.wizardStepName === TSWizardStepName.VERFUEGEN) {
            // verfuegen fuer admin, jugendamt und gesuchsteller immer sichtbar
            if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole()) &&
                !this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerSozialdienstRolle()) &&
                !isAnyStatusOfVerfuegtOrKeinKontingent(gesuch.status)) {
                return false;
            }
            return this.areAllStepsOK(gesuch);
        }
        return step.verfuegbar;  // wenn keine Sonderbedingung gehen wir davon aus dass der step nicht disabled ist
    }

    /**
     * Gibt true zurueck, nur wenn alle Steps den Status OK haben.
     *  - Dokumente duerfen allerdings IN_BEARBEITUNG sein
     *  - Bei BETREUUNGEN darf es WARTEN sein
     *  - Der Status von VERFUEGEN wird gar nicht beruecksichtigt
     */
    public areAllStepsOK(gesuch: TSGesuch): boolean {
        if (EbeguUtil.isNullOrUndefined(gesuch)) {
            return false;
        }
        // tslint:disable-next-line:prefer-for-of
        for (let i = 0; i < this.wizardSteps.length; i++) {
            if (this.wizardSteps[i].wizardStepName === TSWizardStepName.BETREUUNG) {
                if (!this.isStatusOk(this.wizardSteps[i].wizardStepStatus)
                    && this.wizardSteps[i].wizardStepStatus !== TSWizardStepStatus.PLATZBESTAETIGUNG
                    && (
                        this.wizardSteps[i].wizardStepStatus !== TSWizardStepStatus.NOK
                        && !gesuch.isThereAnyBetreuung()
                    )) {
                    return false;
                }

            } else if (this.wizardSteps[i].wizardStepName === TSWizardStepName.DOKUMENTE) {
                if (this.wizardSteps[i].wizardStepStatus === TSWizardStepStatus.NOK) {
                    return false;
                }

            } else if (this.wizardSteps[i].wizardStepName !== TSWizardStepName.VERFUEGEN
                && this.wizardSteps[i].wizardStepName !== TSWizardStepName.ABWESENHEIT
                && this.wizardSteps[i].wizardStepName !== TSWizardStepName.UMZUG
                && this.wizardSteps[i].wizardStepName !== TSWizardStepName.FREIGABE
                && !this.isStatusOk(this.wizardSteps[i].wizardStepStatus)) {
                return false;
            }
        }
        return true;
    }

    private isStatusOk(wizardStepStatus: TSWizardStepStatus): boolean {
        return wizardStepStatus === TSWizardStepStatus.OK || wizardStepStatus === TSWizardStepStatus.MUTIERT;
    }

    /**
     * Prueft fuer den gegebenen Step ob sein Status OK oder MUTIERT ist
     */
    public isStepStatusOk(wizardStepName: TSWizardStepName): boolean {
        return this.hasStepGivenStatus(wizardStepName, TSWizardStepStatus.OK)
            || this.hasStepGivenStatus(wizardStepName, TSWizardStepStatus.MUTIERT);
    }

    /**
     * Gibt true zurueck wenn der Step existiert und sein Status OK ist
     */
    public hasStepGivenStatus(stepName: TSWizardStepName, status: TSWizardStepStatus): boolean {
        if (this.getStepByName(stepName)) {
            return this.getStepByName(stepName).wizardStepStatus === status;
        }
        return false;
    }

    public backupCurrentSteps(): void {
        this.wizardStepsSnapshot = angular.copy(this.wizardSteps);
    }

    public restorePreviousSteps(): void {
        this.wizardSteps = this.wizardStepsSnapshot;
    }

    /**
     * Guckt zuerst dass der Step in der Liste von allowedSteps ist. wenn ja wird es geguckt
     * ob der Step in derl Liste hiddenSteps ist.
     * allowed und nicht hidden Steps -> true
     * alle anderen -> false
     */
    public isStepVisible(stepName: TSWizardStepName): boolean {
        return (this.allowedSteps.indexOf(stepName) >= 0 && !this.isStepHidden(stepName));
    }

    public hideStep(stepName: TSWizardStepName): void {
        if (!this.isStepHidden(stepName)) {
            this.hiddenSteps.push(stepName);
        }
    }

    /**
     * Obwohl das Wort unhide nicht existiert, finde ich den Begriff ausfuehrlicher fuer diesen Fall als show
     */
    public unhideStep(stepName: TSWizardStepName): void {
        if (this.isStepHidden(stepName)) {
            this.hiddenSteps.splice(this.hiddenSteps.indexOf(stepName), 1);
        }
    }

    private isStepHidden(stepName: TSWizardStepName): boolean {
        return this.hiddenSteps.indexOf(stepName) >= 0;
    }

    /**
     * Mit den Daten vom Gesuch, werden die entsprechenden Steps der Liste hiddenSteps hinzugefuegt.
     * Oder ggf. aus der Liste entfernt (nur public fuer test)
     */
    public setHiddenSteps(gesuch: TSGesuch): void {
        if (!gesuch) {
            return;
        }

        if (gesuch.isOnlineGesuch()) {
            this.unhideStep(TSWizardStepName.FREIGABE);
        } else {
            this.hideStep(TSWizardStepName.FREIGABE);
        }
        if (gesuch.isMutation()) {
            this.unhideStep(TSWizardStepName.ABWESENHEIT);
        } else {
            this.hideStep(TSWizardStepName.ABWESENHEIT);
        }
        if (!gesuch.isMutation() && !gesuch.isThereAnyUmzug()) {
            this.hideStep(TSWizardStepName.UMZUG);
        } else {
            this.unhideStep(TSWizardStepName.UMZUG);
        }
        if (EbeguUtil.isNullOrUndefined(gesuch.dossier.fall.sozialdienstFall)) {
            this.hideStep(TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN);
        } else {
            this.unhideStep(TSWizardStepName.SOZIALDIENSTFALL_ERSTELLEN);
        }
    }
}

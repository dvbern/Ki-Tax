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

import {StateService, TransitionPromise} from '@uirouter/core';
import {IController, IDirective, IDirectiveFactory, IQService, ITimeoutService} from 'angular';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import WizardStepManager from '../../../../gesuch/service/wizardStepManager';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import ErrorService from '../../errors/service/ErrorService';
import ITranslateService = angular.translate.ITranslateService;

/**
 * Diese Direktive wird benutzt, um die Navigation Buttons darzustellen. Folgende Parameter koennen benutzt werden,
 * um die Funktionalitaet zu definieren:
 *
 * -- dvPrevious: function      Wenn true wird der Button "previous" angezeigt (nicht gleichzeitig mit dvCancel
 * benutzen)
 * -- dvNext: function          Wenn true wird der Button "next" angezeigt
 * -- dvNextDisabled: function  Wenn man eine extra Pruefung braucht, um den Button Next zu disablen
 * -- dvSubStep: number         Manche Steps haben sog. SubSteps (z.B. finanzielle Situation). Dieses Parameter wird
 * benutzt, um zwischen SubSteps unterscheiden zu koennen
 * -- dvSave: function          Die callback Methode, die man aufrufen muss, wenn der Button geklickt wird. Verwenden
 * nur um die Daten zu speichern
 * -- dvCancel: function        Die callback Methode, um alles zurueckzusetzen (nicht gleichzeitig mit dvPrevious
 * benutzen)
 */
export class DVNavigation implements IDirective {
    public restrict = 'E';
    public scope = {};
    public controller = NavigatorController;
    public controllerAs = 'vm';
    public bindToController = {
        dvPrevious: '&?',
        dvNext: '&?',
        dvCancel: '&?',
        dvNextDisabled: '&?',
        dvSubStep: '<',
        dvSave: '&?',
        dvSavingPossible: '<?',
        dvTranslateNext: '@',
        dvTranslatePrevious: '@',
    };
    public template = require('./dv-navigation.html');

    public static factory(): IDirectiveFactory {
        const directive = () => new DVNavigation();
        directive.$inject = [];
        return directive;
    }
}

export class NavigatorController implements IController {

    public static $inject: string[] = [
        'WizardStepManager',
        '$state',
        'GesuchModelManager',
        '$translate',
        'ErrorService',
        '$q',
        '$timeout',
    ];

    public dvPrevious: () => any;
    public dvNext: () => any;
    public dvSave: () => any;
    public dvCancel: () => any;
    public dvNextDisabled: () => any;
    public dvSavingPossible: boolean;
    public dvSubStep: number;
    public dvTranslateNext: string;
    public dvTranslatePrevious: string;
    // this semaphore will prevent a navigation button to be called again until the prozess is not finished
    public isRequestInProgress: boolean = false;

    public performSave: boolean;

    public constructor(
        private readonly wizardStepManager: WizardStepManager,
        private readonly state: StateService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
        private readonly errorService: ErrorService,
        private readonly $q: IQService,
        private readonly $timeout: ITimeoutService,
    ) {
    }

    // wird von angular aufgerufen
    public $onInit(): void {
        // initial nach aktuell eingeloggtem filtern
        this.dvSavingPossible = this.dvSavingPossible || false;

    }

    public doesCancelExist(): boolean {
        return this.dvCancel !== undefined && this.dvCancel !== null;
    }

    public doesdvTranslateNextExist(): boolean {
        return this.dvTranslateNext !== undefined && this.dvTranslateNext !== null;
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und weiter. Sonst nur weiter
     */
    public getPreviousButtonName(): string {
        if (this.dvTranslatePrevious) {
            return this.$translate.instant(this.dvTranslatePrevious);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.$translate.instant('ZURUECK_ONLY_UPPER');
        }
        if (this.dvSave) {
            return this.$translate.instant('ZURUECK_UPPER');
        }
        return this.$translate.instant('ZURUECK_ONLY_UPPER');
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und zurueck. Sonst nur zurueck
     */
    public getNextButtonName(): string {
        if (this.dvTranslateNext) {
            return this.$translate.instant(this.dvTranslateNext);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.$translate.instant('WEITER_ONLY_UPPER');
        }
        if (this.dvSave) {
            return this.$translate.instant('WEITER_UPPER');
        }
        return this.$translate.instant('WEITER_ONLY_UPPER');
    }

    /**
     * Diese Methode prueft zuerst ob eine Function in dvSave uebergeben wurde. In diesem Fall wird diese Methode
     * aufgerufen und erst als callback zum naechsten Step gefuehrt. Wenn dvSave keine gueltige Function enthaelt und
     * deshalb keine Promise zurueckgibt, wird dann direkt zum naechsten Step geleitet.
     */
    public nextStep(): void {
        if (this.isRequestInProgress) {
            console.log('doubleclick suppressed'); // for testing
            return;
        }

        this.isRequestInProgress = true;
        if (this.isSavingEnabled() && this.dvSave) {
            const returnValue = this.dvSave();  // callback ausfuehren, could return promise
            if (returnValue === undefined) {
                this.isRequestInProgress = false;
            } else {
                this.$q.when(returnValue).then(() => {
                    this.$timeout(() => {
                        this.navigateToNextStep(); // wait till digest is finished (EBEGU-1595)
                    });

                }).finally(() => {
                    this.isRequestInProgress = false;
                });
            }
        } else {
            // do nothing if we are already saving
            this.isRequestInProgress = false;
            this.navigateToNextStep();
        }
    }

    private isSavingEnabled(): boolean {
        return this.dvSavingPossible ? true : !this.gesuchModelManager.isGesuchReadonly();
    }

    /**
     * Diese Methode prueft zuerst ob eine Function in dvSave uebergeben wurde. In diesem Fall wird diese Methode
     * aufgerufen und erst als callback zum vorherigen Step gefuehrt. Wenn dvSave keine gueltige Function enthaelt und
     * deshalb keine Promise zurueckgibt, wird dann direkt zum vorherigen Step geleitet.
     */
    public previousStep(): void {
        if (this.isRequestInProgress) {
            // do nothing if we are already saving
            return;
        }

        if (this.isSavingEnabled() && this.dvSave) {
            const returnValue = this.dvSave();  // callback ausfuehren, could return promise
            if (returnValue === undefined) {
                this.isRequestInProgress = false;
            } else {
                this.$q.when(returnValue).then(() => {
                    this.$timeout(() => {
                        this.navigateToPreviousStep(); // wait till digest is finished (EBEGU-1595)
                    });
                }).finally(() => {
                    this.isRequestInProgress = false;
                });
            }
        } else {
            this.isRequestInProgress = false;
            this.navigateToPreviousStep();
        }
    }

    /**
     * Diese Methode ist aehnlich wie previousStep() aber wird verwendet, um die Aenderungen NICHT zu speichern
     */
    public cancel(): void {
        if (this.dvCancel) {
            this.dvCancel();
        }
        this.navigateToPreviousStep();
    }

    /**
     * Berechnet fuer den aktuellen Benutzer und Step, welcher der naechste Step ist und wechselt zu diesem.
     * Bay default wird es zum nae
     */
    // tslint:disable-next-line:cognitive-complexity
    private navigateToNextStep(): TransitionPromise | undefined {

        this.errorService.clearAll();

        // Improvement?: All diese Sonderregel koennten in getNextStep() vom wizardStepManager sein, damit die gleiche
        // Funktionalität für isButtonDisable wie für die Navigation existiert.
        if (TSWizardStepName.GESUCHSTELLER === this.wizardStepManager.getCurrentStepName()
            && this.gesuchModelManager.getGesuchstellerNumber() === 1
            && this.gesuchModelManager.isGesuchsteller2Required()) {
            return this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '2');
        }
        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            return this.navigateToStep(TSWizardStepName.KINDER);
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            // Diese Logik ist ziemlich kompliziert. Deswegen bleibt sie noch in betreuungView.ts -> Hier wird dann
            // nichts gemacht
            return undefined;
        }
        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 2) {
                return this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
            }

            return undefined;
        }
        if (TSWizardStepName.FINANZIELLE_SITUATION === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                if (!this.gesuchModelManager.isFinanzielleSituationRequired()) {
                    return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
                }
                if (this.gesuchModelManager.getGesuchstellerNumber() === 1
                    && this.gesuchModelManager.isGesuchsteller2Required()) {
                    return this.navigateToStepFinanzielleSituation('2');
                }
                return this.navigateToFinanziellSituationResultate();
            }
            if (this.dvSubStep === 2) {
                if (!this.gesuchModelManager.isFinanzielleSituationEnabled()
                    || !this.gesuchModelManager.isFinanzielleSituationRequired()) {
                    return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
                }
                return this.navigateToStepFinanzielleSituation('1');
            }
            if (this.dvSubStep === 3) {
                return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
            }

            return undefined;
        }
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                const info = this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo();
                if (info && info.einkommensverschlechterung) { // was muss hier sein?
                    if (this.gesuchModelManager.isGesuchsteller2Required()) {
                        return info.ekvFuerBasisJahrPlus1 ?
                            this.navigateToStepEinkommensverschlechterungSteuern() :
                            this.navigateToStepEinkommensverschlechterung('1', '2');
                    }
                    if (info.ekvFuerBasisJahrPlus1) {
                        return this.navigateToStepEinkommensverschlechterung('1', undefined);
                    }
                    return this.navigateToStepEinkommensverschlechterung('1', '2');
                }
                return this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK).then(() => {
                    return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
                }) as any;
            }
            if (this.dvSubStep === 2) {
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) { // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung('1', '1');
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung('1', '2');
                }
                return undefined;
            }
            if (this.dvSubStep === 3) {
                return this.navigateNextEVSubStep3();
            }
            if (this.dvSubStep === 4) {
                return this.navigateNextEVSubStep4();
            }

            return undefined;
        }

        // by default navigieren wir zum naechsten erlaubten Step
        return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
    }

    /**
     * Berechnet fuer den aktuellen Benutzer und Step, welcher der previous Step ist und wechselt zu diesem.
     * wenn es kein Sonderfall ist wird der letzte else case ausgefuehrt
     */
    // tslint:disable-next-line:cognitive-complexity
    private navigateToPreviousStep(): TransitionPromise | undefined {
        this.errorService.clearAll();

        if (TSWizardStepName.GESUCH_ERSTELLEN === this.wizardStepManager.getCurrentStepName()) {
            return this.navigateToStep(TSWizardStepName.GESUCH_ERSTELLEN);
        }

        if (TSWizardStepName.GESUCHSTELLER === this.wizardStepManager.getCurrentStepName()
            && this.gesuchModelManager.getGesuchstellerNumber() === 2) {
            return this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '1');
        }

        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            return this.navigateToStep(TSWizardStepName.KINDER);
        }

        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            return this.navigateToStep(TSWizardStepName.BETREUUNG);
        }

        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            return this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
        }

        if (TSWizardStepName.FINANZIELLE_SITUATION === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                if ((this.gesuchModelManager.getGesuchstellerNumber() === 2)) {
                    return this.navigateToStepFinanzielleSituation('1');
                }

                if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                    return this.navigateToStep(TSWizardStepName.FINANZIELLE_SITUATION);
                }
                return this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 2) {
                return this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 3) {
                return this.navigateToStepFinanzielleSituation(this.gesuchModelManager.getGesuchstellerNumber() === 2 ?
                    '2' :
                    '1');
            }

            return undefined; // TODO is this allowed?
        }

        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                return this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 2) {
                return this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            }
            if (this.dvSubStep === 3) {
                return this.navigatePreviousEVSubStep3();
            }
            if (this.dvSubStep === 4) {
                return this.navigatePreviousEVSubStep4();
            }

            return undefined; // TODO is this allowed?
        }

        if (TSWizardStepName.VERFUEGEN === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            return this.navigateToStep(TSWizardStepName.VERFUEGEN);
        }

        return this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
    }

    /**
     * Diese Methode navigierte zum ersten substep jedes Steps. Fuer die navigation innerhalb eines Steps muss
     * man eine extra Methode machen
     */
    private navigateToStep(stepName: TSWizardStepName, gsNumber?: string): TransitionPromise {
        const gesuchId = this.getGesuchId();
        const gesuchIdParam = {gesuchId};
        const gesuchstellerParams = {gesuchstellerNumber: gsNumber ? gsNumber : '1', gesuchId};

        switch (stepName) {
            case TSWizardStepName.GESUCH_ERSTELLEN:
                return this.state.go('gesuch.fallcreation', this.getFallCreationParams());
            case TSWizardStepName.FAMILIENSITUATION:
                return this.state.go('gesuch.familiensituation', gesuchIdParam);
            case TSWizardStepName.GESUCHSTELLER:
                return this.state.go('gesuch.stammdaten', gesuchstellerParams);
            case TSWizardStepName.UMZUG:
                return this.state.go('gesuch.umzug', gesuchIdParam);
            case TSWizardStepName.KINDER:
                return this.state.go('gesuch.kinder', gesuchIdParam);
            case TSWizardStepName.BETREUUNG:
                return this.state.go('gesuch.betreuungen', gesuchIdParam);
            case TSWizardStepName.ABWESENHEIT:
                return this.state.go('gesuch.abwesenheit', gesuchIdParam);
            case TSWizardStepName.ERWERBSPENSUM:
                return this.state.go('gesuch.erwerbsPensen', gesuchIdParam);
            case TSWizardStepName.FINANZIELLE_SITUATION:
                return this.state.go('gesuch.finanzielleSituationStart', gesuchIdParam);
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG:
                return this.state.go('gesuch.einkommensverschlechterungInfo', gesuchIdParam);
            case TSWizardStepName.DOKUMENTE:
                return this.state.go('gesuch.dokumente', gesuchIdParam);
            case TSWizardStepName.FREIGABE:
                return this.state.go('gesuch.freigabe', gesuchIdParam);
            case TSWizardStepName.VERFUEGEN:
                return this.state.go('gesuch.verfuegen', gesuchIdParam);
            default:
                throw new Error(`not implemented for step ${stepName}`);
        }
    }

    private getFallCreationParams(): {
        eingangsart: TSEingangsart; gesuchId: string; gesuchsperiodeId: string; dossierId: string; gemeindeId: string
    } {
        const gesuch = this.gesuchModelManager.getGesuch();

        return {
            eingangsart: gesuch.eingangsart,
            gesuchId: gesuch.id,
            gesuchsperiodeId: gesuch.gesuchsperiode.id,
            dossierId: this.gesuchModelManager.getDossier().id,
            gemeindeId: this.gesuchModelManager.getDossier().gemeinde.id,
        };
    }

    private navigateToStepEinkommensverschlechterung(gsNumber: string, basisjahrPlus: string): TransitionPromise {
        return this.state.go('gesuch.einkommensverschlechterung', {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId(),
        });
    }

    private navigateToFinanziellSituationResultate(): TransitionPromise {
        return this.state.go('gesuch.finanzielleSituationResultate', {
            gesuchId: this.getGesuchId(),
        });
    }

    // tslint:disable-next-line:no-identical-functions
    private navigateToStepEinkommensverschlechterungSteuern(): TransitionPromise {
        return this.state.go('gesuch.einkommensverschlechterungSteuern', {
            gesuchId: this.getGesuchId(),
        });
    }

    private navigateToStepEinkommensverschlechterungResultate(basisjahrPlus: string): TransitionPromise {
        return this.state.go('gesuch.einkommensverschlechterungResultate', {
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId(),
        });
    }

    private navigateToStepFinanzielleSituation(gsNumber: string): TransitionPromise {
        return this.state.go('gesuch.finanzielleSituation', {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            gesuchId: this.getGesuchId(),
        });
    }

    private getGesuchId(): string {
        if (this.gesuchModelManager && this.gesuchModelManager.getGesuch()) {
            return this.gesuchModelManager.getGesuch().id;
        }
        return '';
    }

    /**
     * Checks whether the button should be disable for the current conditions. By default (auch fuer Mutaionen) enabled
     */
    public isNextButtonDisabled(): boolean {
        // if step is disabled in manager we can stop here
        if (!this.wizardStepManager.isNextStepEnabled(this.gesuchModelManager.getGesuch())) {
            return true;
        }

        if (!this.gesuchModelManager.isGesuch()) {
            return false;
        }

        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return !this.gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()
                && !this.wizardStepManager.isNextStepBesucht(this.gesuchModelManager.getGesuch());
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return !this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
                && !this.wizardStepManager.isNextStepBesucht(this.gesuchModelManager.getGesuch());
        }
        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.dvNextDisabled()
                && !this.wizardStepManager.isNextStepBesucht(this.gesuchModelManager.getGesuch());
        }

        return false;
    }

    public getTooltip(): string {
        if (!this.isNextButtonDisabled()) {
            return undefined;
        }

        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.$translate.instant('KINDER_TOOLTIP_REQUIRED');
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.$translate.instant('BETREUUNG_TOOLTIP_REQUIRED');
        }
        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.$translate.instant('ERWERBSPENSUM_TOOLTIP_REQUIRED');
        }

        return undefined;
    }

    // tslint:disable-next-line:cognitive-complexity
    private navigateNextEVSubStep3(): TransitionPromise {
        if ((this.gesuchModelManager.getBasisJahrPlusNumber() === 1)) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
                    return this.navigateToStepEinkommensverschlechterung('2', '1');
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung('1', '2');
                }
                return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
            }
            // ist Zustand 2/1
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
                return this.navigateToStepEinkommensverschlechterung('1', '2'); // gehe ekv 1/2
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) {
                return this.navigateToStepEinkommensverschlechterung('2', '2'); // gehe ekv 2/2
            }
            return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
        }
        if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
            // ist Zustand 1/2
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '2');
            }
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
            }
            return this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
            return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
        }
        return this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
    }

    // tslint:disable-next-line:cognitive-complexity
    private navigatePreviousEVSubStep3(): TransitionPromise {
        if ((this.gesuchModelManager.getBasisJahrPlusNumber() === 1)) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                if (this.gesuchModelManager.isGesuchsteller2Required() &&
                    this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1) {
                    return this.navigateToStepEinkommensverschlechterungSteuern();
                }
                return this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            }
            // ist Zustand 2/1
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            }
            if (this.gesuchModelManager.isGesuchsteller2Required() &&
                this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1) {
                return this.navigateToStepEinkommensverschlechterungSteuern();
            }
            return this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
        }
        if (this.gesuchModelManager.getGesuchstellerNumber() === 1) { // ist Zustand 1/2
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '1');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            }
            if (this.gesuchModelManager.isGesuchsteller2Required() &&
                this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1) {
                return this.navigateToStepEinkommensverschlechterungSteuern();
            }
            return this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
            return this.navigateToStepEinkommensverschlechterung('1', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/2
            return this.navigateToStepEinkommensverschlechterung('2', '1');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
            return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
        }
        if (this.gesuchModelManager.isGesuchsteller2Required() &&
            this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus1) {
            return this.navigateToStepEinkommensverschlechterungSteuern();
        }
        return this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
    }

    private navigatePreviousEVSubStep4(): TransitionPromise {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 2) {
            // baisjahrPlus2
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '2');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                return this.navigateToStepEinkommensverschlechterung('1', '2');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
                return this.navigateToStepEinkommensverschlechterung('2', '1');
            }
            return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
        }

        // baisjahrPlus1
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
            return this.navigateToStepEinkommensverschlechterung('2', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
            return this.navigateToStepEinkommensverschlechterung('1', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
            return this.navigateToStepEinkommensverschlechterung('2', '1');
        }
        return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
    }

    private navigateNextEVSubStep4(): TransitionPromise {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 1
            && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2) {
            return this.navigateToStepEinkommensverschlechterungResultate('2');
        }

        return this.wizardStepManager.updateCurrentWizardStepStatus(TSWizardStepStatus.OK).then(() => {
            return this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
        }) as any;
    }

}

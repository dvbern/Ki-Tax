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

import {StateService, TransitionPromise} from '@uirouter/core';
import {
    IComponentController,
    IController,
    IQService,
    ITimeoutService
} from 'angular';
import {FinanzielleSituationRS} from '../../../../gesuch/service/finanzielleSituationRS.rest';
import {FinanzielleSituationSubStepManager} from '../../../../gesuch/service/finanzielleSituationSubStepManager';
import {FinanzielleSituationSubStepManagerAppenzell} from '../../../../gesuch/service/finanzielleSituationSubStepManagerAppenzell';
import {FinanzielleSituationSubStepManagerBernAsiv} from '../../../../gesuch/service/finanzielleSituationSubStepManagerBernAsiv';
import {FinanzielleSituationSubStepManagerLuzern} from '../../../../gesuch/service/finanzielleSituationSubStepManagerLuzern';
import {FinanzielleSituationSubStepManagerSchwyz} from '../../../../gesuch/service/finanzielleSituationSubStepManagerSchwyz';
import {FinanzielleSituationSubStepManagerSolothurn} from '../../../../gesuch/service/finanzielleSituationSubStepManagerSolothurn';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {WizardStepManager} from '../../../../gesuch/service/wizardStepManager';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSFinanzielleSituationSubStepName} from '../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationTyp} from '../../../../models/enums/TSFinanzielleSituationTyp';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorService} from '../../errors/service/ErrorService';
import {LogFactory} from '../../logging/LogFactory';
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
export class DVNavigation implements IComponentController {
    public restrict = 'E';
    public scope = {};
    public controller = NavigatorController;
    public controllerAs = 'vm';
    public bindings = {
        dvPrevious: '&?',
        dvNext: '&?',
        dvCancel: '&?',
        dvNextDisabled: '&?',
        dvSubStep: '<',
        dvSubStepName: '@',
        dvSave: '&?',
        dvSavingPossible: '<?',
        dvTranslateNext: '@',
        dvTranslatePrevious: '@',
        containerClass: '<'
    };
    public template = require('./dv-navigation.html');
}

const LOG = LogFactory.createLog('DVNavigation');

export class NavigatorController implements IController {
    public static $inject: string[] = [
        'WizardStepManager',
        'FinanzielleSituationRS',
        '$state',
        'GesuchModelManager',
        '$translate',
        'ErrorService',
        '$q',
        '$timeout'
    ];

    public dvPrevious: () => any;
    public dvNext: () => any;
    public dvSave: () => any;
    public dvCancel: () => any;
    public dvNextDisabled: () => any;
    public dvSavingPossible: boolean;
    public dvSubStep: number;
    public dvSubStepName: TSFinanzielleSituationSubStepName;
    public dvTranslateNext: string;
    public dvTranslatePrevious: string;
    public containerClass: string;

    private finSitWizardSubStepManager: FinanzielleSituationSubStepManager;

    public constructor(
        private readonly wizardStepManager: WizardStepManager,
        private readonly finanzielleSituationRS: FinanzielleSituationRS,
        private readonly state: StateService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $translate: ITranslateService,
        private readonly errorService: ErrorService,
        private readonly $q: IQService,
        private readonly $timeout: ITimeoutService
    ) {}

    // wird von angular aufgerufen
    public $onInit(): void {
        // initial nach aktuell eingeloggtem filtern
        this.dvSavingPossible = this.dvSavingPossible || false;
        if (!this.containerClass) {
            this.containerClass = 'dv-navigation-flex';
        }
        this.initSubStepManager();
    }

    private initSubStepManager(): void {
        if (
            EbeguUtil.isNullOrUndefined(
                this.gesuchModelManager.getGesuchsperiode()
            )
        ) {
            return;
        }
        this.finanzielleSituationRS
            .getFinanzielleSituationTyp(
                this.gesuchModelManager.getGesuchsperiode(),
                this.gesuchModelManager.getGemeinde()
            )
            .subscribe(
                typ => {
                    switch (typ) {
                        case TSFinanzielleSituationTyp.BERN:
                        case TSFinanzielleSituationTyp.BERN_FKJV:
                            this.finSitWizardSubStepManager =
                                new FinanzielleSituationSubStepManagerBernAsiv(
                                    this.gesuchModelManager
                                );
                            break;
                        case TSFinanzielleSituationTyp.LUZERN:
                            this.finSitWizardSubStepManager =
                                new FinanzielleSituationSubStepManagerLuzern(
                                    this.gesuchModelManager
                                );
                            break;
                        case TSFinanzielleSituationTyp.SOLOTHURN:
                            this.finSitWizardSubStepManager =
                                new FinanzielleSituationSubStepManagerSolothurn(
                                    this.gesuchModelManager
                                );
                            break;
                        case TSFinanzielleSituationTyp.APPENZELL:
                        case TSFinanzielleSituationTyp.APPENZELL_FOLGEMONAT:
                            this.finSitWizardSubStepManager =
                                new FinanzielleSituationSubStepManagerAppenzell(
                                    this.gesuchModelManager
                                );
                            break;
                        case TSFinanzielleSituationTyp.SCHWYZ:
                            this.finSitWizardSubStepManager =
                                new FinanzielleSituationSubStepManagerSchwyz(
                                    this.gesuchModelManager
                                );
                            break;
                        default:
                            throw new Error(
                                `unexpected TSFinanzielleSituationTyp ${typ}`
                            );
                    }
                },
                err => LOG.error(err)
            );
    }

    public doesCancelExist(): boolean {
        return this.dvCancel !== undefined && this.dvCancel !== null;
    }

    public doesdvTranslateNextExist(): boolean {
        return (
            this.dvTranslateNext !== undefined && this.dvTranslateNext !== null
        );
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und weiter. Sonst nur weiter
     */
    public getPreviousButtonName(): string {
        if (this.dvTranslatePrevious) {
            return this.$translate.instant(this.dvTranslatePrevious);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.$translate.instant('ZURUECK_ONLY');
        }
        if (this.dvSave) {
            return this.$translate.instant('ZURUECK');
        }
        return this.$translate.instant('ZURUECK_ONLY');
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und zurueck. Sonst nur zurueck
     */
    public getNextButtonName(): string {
        if (this.dvTranslateNext) {
            return this.$translate.instant(this.dvTranslateNext);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.$translate.instant('WEITER_ONLY');
        }
        if (this.dvSave) {
            return this.$translate.instant('WEITER');
        }
        return this.$translate.instant('WEITER_ONLY');
    }

    /**
     * Diese Methode prueft zuerst ob eine Function in dvSave uebergeben wurde. In diesem Fall wird diese Methode
     * aufgerufen und erst als callback zum naechsten Step gefuehrt. Wenn dvSave keine gueltige Function enthaelt und
     * deshalb keine Promise zurueckgibt, wird dann direkt zum naechsten Step geleitet.
     */
    public nextStep(): void {
        if (this.wizardStepManager.isTransitionInProgress) {
            console.log('doubleclick suppressed'); // for testing
            return;
        }

        this.wizardStepManager.isTransitionInProgress = true;

        if (this.isSavingEnabled() && this.dvSave) {
            const returnValue = this.dvSave(); // callback ausfuehren, could return promise
            if (returnValue) {
                this.$q
                    .when(returnValue)
                    .then(() =>
                        this.$timeout(() => {
                            this.navigateToNextStep(); // wait till digest is finished (EBEGU-1595)
                        })
                    )
                    .catch(() => {
                        // the promise was rejected, the navigation aborted:
                        this.wizardStepManager.isTransitionInProgress = false;
                    });
            } else {
                // we need to release the semaphore because we stay in the page and we need to allow the user to move on
                this.wizardStepManager.isTransitionInProgress = false;
            }
        } else {
            // do nothing if we are already saving
            this.navigateToNextStep();
        }
    }

    private isSavingEnabled(): boolean {
        return this.dvSavingPossible
            ? true
            : !this.gesuchModelManager.isGesuchReadonly();
    }

    /**
     * Diese Methode prueft zuerst ob eine Function in dvSave uebergeben wurde. In diesem Fall wird diese Methode
     * aufgerufen und erst als callback zum vorherigen Step gefuehrt. Wenn dvSave keine gueltige Function enthaelt und
     * deshalb keine Promise zurueckgibt, wird dann direkt zum vorherigen Step geleitet.
     */
    public previousStep(): void {
        if (this.wizardStepManager.isTransitionInProgress) {
            // do nothing if we are already saving
            return;
        }

        this.wizardStepManager.isTransitionInProgress = true;

        if (this.isSavingEnabled() && this.dvSave) {
            const returnValue = this.dvSave(); // callback ausfuehren, could return promise
            if (returnValue) {
                this.$q
                    .when(returnValue)
                    .then(() =>
                        this.$timeout(() => {
                            this.navigateToPreviousStep(); // wait till digest is finished (EBEGU-1595)
                        })
                    )
                    .catch(() => {
                        // the promise was rejected, the navigation aborted:
                        this.wizardStepManager.isTransitionInProgress = false;
                    });
            } else {
                // we need to release the semaphore because we stay in the page and we need to allow the user to move on
                this.wizardStepManager.isTransitionInProgress = false;
            }
        } else {
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
    private navigateToNextStep(): TransitionPromise | undefined {
        this.errorService.clearAll();

        // Improvement?: All diese Sonderregel koennten in getNextStep() vom wizardStepManager sein, damit die gleiche
        // Funktionalität für isButtonDisable wie für die Navigation existiert.
        if (
            TSWizardStepName.GESUCHSTELLER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.gesuchModelManager.getGesuchstellerNumber() === 1 &&
            this.gesuchModelManager.isGesuchsteller2Required()
        ) {
            return this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '2');
        }
        if (
            TSWizardStepName.KINDER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            return this.navigateToStep(TSWizardStepName.KINDER);
        }
        if (
            TSWizardStepName.BETREUUNG ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            // Diese Logik ist ziemlich kompliziert. Deswegen bleibt sie noch in betreuungView.ts -> Hier wird dann
            // nichts gemacht
            return undefined;
        }
        if (
            TSWizardStepName.ERWERBSPENSUM ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            if (this.dvSubStep === 1) {
                return this.navigateToStep(
                    this.wizardStepManager.getNextStep(
                        this.gesuchModelManager.getGesuch()
                    )
                );
            }
            if (this.dvSubStep === 2) {
                return this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
            }

            return undefined;
        }
        if (
            TSWizardStepName.FINANZIELLE_SITUATION ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_LUZERN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL ===
                this.wizardStepManager.getCurrentStepName()
        ) {
            const nextSubStep =
                this.finSitWizardSubStepManager.getNextSubStepFinanzielleSituation(
                    this.dvSubStepName
                );
            const nextMainStep = this.wizardStepManager.getNextStep(
                this.gesuchModelManager.getGesuch()
            );
            return this.navigateToSubStepFinanzielleSituation(
                nextSubStep,
                nextMainStep
            );
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL ===
                this.wizardStepManager.getCurrentStepName()
        ) {
            if (this.dvSubStep === 1) {
                const info = this.gesuchModelManager
                    .getGesuch()
                    .extractEinkommensverschlechterungInfo();
                if (info && info.einkommensverschlechterung) {
                    // was muss hier sein?
                    if (info.ekvFuerBasisJahrPlus1) {
                        return this.navigateToStepEinkommensverschlechterung(
                            '1',
                            undefined
                        );
                    }
                    return this.navigateToStepEinkommensverschlechterung(
                        '1',
                        '2'
                    );
                }
                return this.wizardStepManager
                    .updateCurrentWizardStepStatus(TSWizardStepStatus.OK)
                    .then(() =>
                        this.navigateToStep(
                            this.wizardStepManager.getNextStep(
                                this.gesuchModelManager.getGesuch()
                            )
                        )
                    ) as any;
            }
            if (this.dvSubStep === 2) {
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                    // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung(
                        '1',
                        '1'
                    );
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
                    // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung(
                        '1',
                        '2'
                    );
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
        return this.navigateToStep(
            this.wizardStepManager.getNextStep(
                this.gesuchModelManager.getGesuch()
            )
        );
    }

    /**
     * Berechnet fuer den aktuellen Benutzer und Step, welcher der previous Step ist und wechselt zu diesem.
     * wenn es kein Sonderfall ist wird der letzte else case ausgefuehrt
     */
    private navigateToPreviousStep(): TransitionPromise | undefined {
        this.errorService.clearAll();

        if (
            TSWizardStepName.GESUCH_ERSTELLEN ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            return this.navigateToStep(TSWizardStepName.GESUCH_ERSTELLEN);
        }

        if (
            TSWizardStepName.GESUCHSTELLER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.gesuchModelManager.getGesuchstellerNumber() === 2
        ) {
            return this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '1');
        }

        if (
            TSWizardStepName.KINDER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            return this.navigateToStep(TSWizardStepName.KINDER);
        }

        if (
            TSWizardStepName.BETREUUNG ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            return this.navigateToStep(TSWizardStepName.BETREUUNG);
        }

        if (
            TSWizardStepName.ERWERBSPENSUM ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            return this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
        }

        if (
            TSWizardStepName.FINANZIELLE_SITUATION ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_LUZERN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN ===
                this.wizardStepManager.getCurrentStepName()
        ) {
            const previousSubStep =
                this.finSitWizardSubStepManager.getPreviousSubStepFinanzielleSituation(
                    this.dvSubStepName
                );
            const previousMainStep = this.wizardStepManager.getPreviousStep(
                this.gesuchModelManager.getGesuch()
            );

            return this.navigateToSubStepFinanzielleSituation(
                previousSubStep,
                previousMainStep
            );
        }

        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ ===
                this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL ===
                this.wizardStepManager.getCurrentStepName()
        ) {
            if (this.dvSubStep === 1) {
                return this.navigateToStep(
                    this.wizardStepManager.getPreviousStep(
                        this.gesuchModelManager.getGesuch()
                    )
                );
            }
            if (this.dvSubStep === 2) {
                return this.navigateToStep(
                    this.wizardStepManager.getCurrentStepName()
                );
            }
            if (this.dvSubStep === 3) {
                return this.navigatePreviousEVSubStep3();
            }
            if (this.dvSubStep === 4) {
                return this.navigatePreviousEVSubStep4();
            }

            return undefined; // TODO is this allowed?
        }

        if (
            TSWizardStepName.VERFUEGEN ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 2
        ) {
            return this.navigateToStep(TSWizardStepName.VERFUEGEN);
        }

        return this.navigateToStep(
            this.wizardStepManager.getPreviousStep(
                this.gesuchModelManager.getGesuch()
            )
        );
    }

    private navigateToSubStepFinanzielleSituation(
        navigateToSubStep: TSFinanzielleSituationSubStepName,
        navigateToStepIfNoSubstep: TSWizardStepName
    ): TransitionPromise {
        switch (navigateToSubStep) {
            case TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP:
                return this.navigateToStep(navigateToStepIfNoSubstep);
            case TSFinanzielleSituationSubStepName.BERN_START:
                return this.navigateToStep(
                    TSWizardStepName.FINANZIELLE_SITUATION
                );
            case TSFinanzielleSituationSubStepName.BERN_GS1:
                return this.navigateToStepFinanzielleSituation('1');
            case TSFinanzielleSituationSubStepName.BERN_GS2:
                return this.navigateToStepFinanzielleSituation('2');
            case TSFinanzielleSituationSubStepName.BERN_RESULTATE:
                return this.navigateToFinanziellSituationResultate();
            case TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE:
            case TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE_DETAIL:
                return this.navigateToSozialhilfeZeitraeume();
            case TSFinanzielleSituationSubStepName.LUZERN_START:
                return this.navigateToLuzernStart();
            case TSFinanzielleSituationSubStepName.LUZERN_GS2:
                return this.navigateToLuzernGS2();
            default:
                throw new Error(
                    `not implemented for Substep ${navigateToSubStep}`
                );
        }
    }

    /**
     * Diese Methode navigierte zum ersten substep jedes Steps. Fuer die navigation innerhalb eines Steps muss
     * man eine extra Methode machen
     */
    private navigateToStep(
        stepName: TSWizardStepName,
        gsNumber?: string
    ): TransitionPromise {
        const gesuchId = this.getGesuchId();
        const gesuchIdParam = {gesuchId};
        const gesuchstellerParams = {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            gesuchId
        };

        switch (stepName) {
            case TSWizardStepName.GESUCH_ERSTELLEN:
                return this.state.go(
                    'gesuch.fallcreation',
                    this.getFallCreationParams()
                );
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
                return this.state.go(
                    'gesuch.finanzielleSituationStart',
                    gesuchIdParam
                );
            case TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ:
                return this.state.go(
                    'gesuch.finanzielleSituationStartSchwyz',
                    gesuchIdParam
                );
            case TSWizardStepName.FINANZIELLE_SITUATION_LUZERN:
                return this.state.go(
                    'gesuch.finanzielleSituationStartLuzern',
                    gesuchIdParam
                );
            case TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN:
                return this.state.go(
                    'gesuch.finanzielleSituationStartSolothurn',
                    gesuchIdParam
                );
            case TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL:
                return this.state.go(
                    'gesuch.finanzielleSituationAppenzell',
                    gesuchIdParam
                );
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL:
                return this.state.go(
                    'gesuch.einkommensverschlechterungInfo',
                    gesuchIdParam
                );
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
        eingangsart: TSEingangsart;
        gesuchId: string;
        gesuchsperiodeId: string;
        dossierId: string;
        gemeindeId: string;
    } {
        const gesuch = this.gesuchModelManager.getGesuch();

        return {
            eingangsart: gesuch.eingangsart,
            gesuchId: gesuch.id,
            gesuchsperiodeId: gesuch.gesuchsperiode.id,
            dossierId: this.gesuchModelManager.getDossier().id,
            gemeindeId: this.gesuchModelManager.getDossier().gemeinde.id
        };
    }

    private navigateToStepEinkommensverschlechterung(
        gsNumber: string,
        basisjahrPlus: string
    ): TransitionPromise {
        let stateName = 'gesuch.einkommensverschlechterung';
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungLuzern';
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungSolothurn';
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungSchwyz';
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungAppenzell';
        }
        return this.state.go(stateName, {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToStepEinkommensverschlechterungLuzern(
        gsNumber: string,
        basisjahrPlus: string
    ): TransitionPromise {
        return this.state.go('gesuch.einkommensverschlechterungLuzern', {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToSozialhilfeZeitraeume(): TransitionPromise {
        return this.navigateToPath('gesuch.SozialhilfeZeitraeume');
    }

    private navigateToFinanziellSituationResultate(): TransitionPromise {
        return this.navigateToPath('gesuch.finanzielleSituationResultate');
    }

    private navigateToPath(path: string): TransitionPromise {
        return this.state.go(path, {
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToStepEinkommensverschlechterungResultate(
        basisjahrPlus: string
    ): TransitionPromise {
        let stateName = 'gesuch.einkommensverschlechterungResultate';
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungLuzernResultate';
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungSolothurnResultate';
        }
        if (
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL ===
            this.wizardStepManager.getCurrentStepName()
        ) {
            stateName = 'gesuch.einkommensverschlechterungAppenzellResultate';
        }
        return this.state.go(stateName, {
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToStepFinanzielleSituation(
        gsNumber: string
    ): TransitionPromise {
        return this.state.go('gesuch.finanzielleSituation', {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToLuzernStart(): any {
        return this.state.go('gesuch.finanzielleSituationStartLuzern', {
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToLuzernGS2(): any {
        return this.state.go('gesuch.finanzielleSituationGS2Luzern', {
            gesuchId: this.getGesuchId()
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
        // Wenn das Gesuch disabled ist (z.B. in Rolle Mandant), darf man nur soweit navigieren, wie die Steps
        // besucht sind
        const nextStepBesucht = this.wizardStepManager.isNextStepBesucht(
            this.gesuchModelManager.getGesuch()
        );
        const nextStepEnabled = this.wizardStepManager.isNextStepEnabled(
            this.gesuchModelManager.getGesuch()
        );
        if (
            this.gesuchModelManager.isGesuchReadonly() &&
            TSWizardStepName.GESUCHSTELLER !==
                this.wizardStepManager.getCurrentStepName()
        ) {
            return !nextStepBesucht;
        }

        // if step is disabled in manager we can stop here
        if (!nextStepEnabled) {
            return true;
        }

        if (!this.gesuchModelManager.isGesuch()) {
            return false;
        }

        // Wenn dvNtextDisabled gesetzt ist und true zurückgibt, soll dies immer alle anderen Regeln übersteuern
        if (this.dvNextDisabled && this.dvNextDisabled() === true) {
            return true;
        }

        if (
            TSWizardStepName.GESUCHSTELLER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return (
                !this.gesuchModelManager.isGesuchsteller2Required() &&
                !nextStepBesucht
            );
        }
        if (
            TSWizardStepName.KINDER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return (
                (!this.gesuchModelManager.isThereAnyKindWithBetreuungsbedarf() ||
                    this.gesuchModelManager.isThereAnyNotGeprueftesKind()) &&
                !nextStepBesucht
            );
        }
        if (
            TSWizardStepName.BETREUUNG ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return (
                !this.gesuchModelManager.getGesuch().isThereAnyBetreuung() &&
                !nextStepBesucht
            );
        }
        return false;
    }

    public getTooltip(): string {
        if (!this.isNextButtonDisabled()) {
            return undefined;
        }

        if (
            TSWizardStepName.KINDER ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return this.$translate.instant('KINDER_TOOLTIP_REQUIRED');
        }
        if (
            TSWizardStepName.BETREUUNG ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return this.$translate.instant('BETREUUNG_TOOLTIP_REQUIRED');
        }
        if (
            TSWizardStepName.ERWERBSPENSUM ===
                this.wizardStepManager.getCurrentStepName() &&
            this.dvSubStep === 1
        ) {
            return this.$translate.instant('ERWERBSPENSUM_TOOLTIP_REQUIRED');
        }

        return undefined;
    }

    private navigateNextEVSubStep3(): TransitionPromise {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 1) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) {
                    // gehe ekv 2/1
                    return this.navigateToStepEinkommensverschlechterung(
                        '2',
                        '1'
                    );
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
                    // gehe ekv 1/2
                    return this.navigateToStepEinkommensverschlechterung(
                        '1',
                        '2'
                    );
                }
                return this.navigateToStepEinkommensverschlechterungResultate(
                    '1'
                ); // gehe Resultate Bj 1
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
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) {
                // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '2');
            }
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                return this.navigateToStepEinkommensverschlechterungResultate(
                    '1'
                ); // gehe Resultate Bj 1
            }
            return this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
            return this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
        }
        return this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
    }

    private navigatePreviousEVSubStep3(): TransitionPromise {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 1) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                return this.navigateToStep(
                    this.wizardStepManager.getCurrentStepName()
                );
            }
            // ist Zustand 2/1
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            }
            return this.navigateToStep(
                this.wizardStepManager.getCurrentStepName()
            );
        }
        if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
            // ist Zustand 1/2
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) {
                // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '1');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            }
            return this.navigateToStep(
                this.wizardStepManager.getCurrentStepName()
            );
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
            // gehe ekv 1/2
            return this.navigateToStepEinkommensverschlechterung('1', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) {
            // gehe ekv 2/2
            return this.navigateToStepEinkommensverschlechterung('2', '1');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
            return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
        }
        return this.navigateToStep(this.wizardStepManager.getCurrentStepName());
    }

    private navigatePreviousEVSubStep4(): TransitionPromise {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 2) {
            // baisjahrPlus2
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                return this.navigateToStepEinkommensverschlechterungResultate(
                    '1'
                ); // gehe Resultate Bj 1
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) {
                // gehe ekv 2/2
                return this.navigateToStepEinkommensverschlechterung('2', '2');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
                // gehe ekv 1/2
                return this.navigateToStepEinkommensverschlechterung('1', '2');
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) {
                // gehe ekv 2/1
                return this.navigateToStepEinkommensverschlechterung('2', '1');
            }
            return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
        }

        // baisjahrPlus1
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) {
            // gehe ekv 2/2
            return this.navigateToStepEinkommensverschlechterung('2', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
            // gehe ekv 1/2
            return this.navigateToStepEinkommensverschlechterung('1', '2');
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) {
            // gehe ekv 2/1
            return this.navigateToStepEinkommensverschlechterung('2', '1');
        }
        return this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
    }

    private navigateNextEVSubStep4(): TransitionPromise {
        if (
            this.gesuchModelManager.getBasisJahrPlusNumber() === 1 &&
            this.gesuchModelManager
                .getGesuch()
                .extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2
        ) {
            return this.navigateToStepEinkommensverschlechterungResultate('2');
        }

        return this.wizardStepManager
            .updateCurrentWizardStepStatus(TSWizardStepStatus.OK)
            .then(() =>
                this.navigateToStep(
                    this.wizardStepManager.getNextStep(
                        this.gesuchModelManager.getGesuch()
                    )
                )
            ) as any;
    }

    public setSubstepManager(
        manager: FinanzielleSituationSubStepManager
    ): void {
        this.finSitWizardSubStepManager = manager;
    }
}

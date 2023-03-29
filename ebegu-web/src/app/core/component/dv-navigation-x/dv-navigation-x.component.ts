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

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionPromise} from '@uirouter/core';
import {FinanzielleSituationRS} from '../../../../gesuch/service/finanzielleSituationRS.rest';
import {FinanzielleSituationSubStepManager} from '../../../../gesuch/service/finanzielleSituationSubStepManager';
import {
    FinanzielleSituationSubStepManagerAppenzell
} from '../../../../gesuch/service/finanzielleSituationSubStepManagerAppenzell';
import {
    FinanzielleSituationSubStepManagerBernAsiv
} from '../../../../gesuch/service/finanzielleSituationSubStepManagerBernAsiv';
import {
    FinanzielleSituationSubStepManagerLuzern
} from '../../../../gesuch/service/finanzielleSituationSubStepManagerLuzern';
import {
    FinanzielleSituationSubStepManagerSolothurn
} from '../../../../gesuch/service/finanzielleSituationSubStepManagerSolothurn';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {WizardStepManager} from '../../../../gesuch/service/wizardStepManager';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSFinanzielleSituationSubStepName} from '../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationTyp} from '../../../../models/enums/TSFinanzielleSituationTyp';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../models/enums/TSWizardStepStatus';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {ErrorService} from '../../errors/service/ErrorService';
import {Log, LogFactory} from '../../logging/LogFactory';

@Component({
    selector: 'dv-navigation-x',
    templateUrl: './dv-navigation-x.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DvNavigationXComponent implements OnInit {

    private readonly log: Log = LogFactory.createLog('DvNavigationXComponent');

    @Input() public dvPrevious: boolean;
    @Input() public dvNext: boolean;
    @Output() public readonly dvSave = new EventEmitter<{onResult: (arg: any) => any }>();
    @Output() public readonly dvCancel: EventEmitter<any> = new EventEmitter<any>();
    @Input() public readonly dvNextDisabled: boolean;
    @Input() public dvSubStep: number;
    @Input() public dvSubStepName: TSFinanzielleSituationSubStepName;
    @Input() public dvSavingPossible: boolean;
    @Input() public dvTranslateNext: string;
    @Input() public dvTranslatePrevious: string;
    @Input() public containerClass: string;

    private finSitWizardSubStepManager: FinanzielleSituationSubStepManager;

    public constructor(
        private readonly wizardStepManager: WizardStepManager,
        private readonly finanzielleSituationRS: FinanzielleSituationRS,
        private readonly $state: StateService,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService
    ) {
    }

    // wird von angular aufgerufen
    public ngOnInit(): void {
        // initial nach aktuell eingeloggtem filtern
        this.dvSavingPossible = this.dvSavingPossible || false;
        if (!this.containerClass) {
            this.containerClass = 'dv-navigation-flex';
        }
        this.initSubStepManager();
    }

    private initSubStepManager(): void {
        if (EbeguUtil.isNullOrUndefined(this.gesuchModelManager.getGesuchsperiode())) {
            return;
        }
        this.finanzielleSituationRS.getFinanzielleSituationTyp(this.gesuchModelManager.getGesuchsperiode(),
            this.gesuchModelManager.getGemeinde())
            .subscribe(typ => {
                switch (typ) {
                    case TSFinanzielleSituationTyp.BERN:
                    case TSFinanzielleSituationTyp.BERN_FKJV:
                        this.finSitWizardSubStepManager =
                            new FinanzielleSituationSubStepManagerBernAsiv(this.gesuchModelManager);
                        break;
                    case TSFinanzielleSituationTyp.LUZERN:
                        this.finSitWizardSubStepManager =
                            new FinanzielleSituationSubStepManagerLuzern(this.gesuchModelManager);
                        break;
                    case TSFinanzielleSituationTyp.SOLOTHURN:
                        this.finSitWizardSubStepManager =
                            new FinanzielleSituationSubStepManagerSolothurn(this.gesuchModelManager);
                        break;
                    case TSFinanzielleSituationTyp.APPENZELL:
                        this.finSitWizardSubStepManager =
                            new FinanzielleSituationSubStepManagerAppenzell(this.gesuchModelManager);
                        break;
                    default:
                        throw new Error(`unexpected TSFinanzielleSituationTyp ${typ}`);
                }
            }, err => this.log.error(err));
    }

    public doesCancelExist(): boolean {
        return this.dvCancel.observers.length > 0;
    }

    public doesSaveExist(): boolean {
        return this.dvSave.observers.length > 0;
    }

    public doesdvTranslateNextExist(): boolean {
        return this.dvTranslateNext !== undefined && this.dvTranslateNext !== null;
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und weiter. Sonst nur weiter
     */
    public getPreviousButtonName(): string {
        if (this.dvTranslatePrevious) {
            return this.translate.instant(this.dvTranslatePrevious);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.translate.instant('ZURUECK_ONLY');
        }
        if (this.doesSaveExist()) {
            return this.translate.instant('ZURUECK');
        }
        return this.translate.instant('ZURUECK_ONLY');
    }

    /**
     * Wenn die function save uebergeben wurde, dann heisst der Button Speichern und zurueck. Sonst nur zurueck
     */
    public getNextButtonName(): string {
        if (this.dvTranslateNext) {
            return this.translate.instant(this.dvTranslateNext);
        }
        if (this.gesuchModelManager.isGesuchReadonly()) {
            return this.translate.instant('WEITER_ONLY');
        }
        if (this.doesSaveExist()) {
            return this.translate.instant('WEITER');
        }
        return this.translate.instant('WEITER_ONLY');
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

        // eslint-disable-next-line
        if (this.isSavingEnabled() && this.doesSaveExist()) {
            this.dvSave.emit({
                onResult: (result: any) => {
                    if (result) {
                        this.navigateToNextStep();
                    } else {
                        // we need to release the semaphore because we stay in the page and we need to allow the user
                        // to move on
                        this.wizardStepManager.isTransitionInProgress = false;
                    }
                }
            });
        } else {
            // do nothing if we are already saving
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
        if (this.wizardStepManager.isTransitionInProgress) {
            // do nothing if we are already saving
            return;
        }

        this.wizardStepManager.isTransitionInProgress = true;
        // eslint-disable-next-line
        if (this.isSavingEnabled() && this.doesSaveExist()) {
            this.dvSave.emit({
                onResult: (result: any) => {
                    if (result) {
                        this.navigateToPreviousStep();
                    } else {
                        // we need to release the semaphore because we stay in the page and we need to allow the user
                        // to move on
                        this.wizardStepManager.isTransitionInProgress = false;
                    }
                }
            });
        } else {
            this.navigateToPreviousStep();
        }
    }

    /**
     * Diese Methode ist aehnlich wie previousStep() aber wird verwendet, um die Aenderungen NICHT zu speichern
     */
    public cancel(): void {
        if (this.doesCancelExist()) {
            this.dvCancel.emit();
        }
        this.navigateToPreviousStep();
    }

    /**
     * Berechnet fuer den aktuellen Benutzer und Step, welcher der naechste Step ist und wechselt zu diesem.
     * Bay default wird es zum nae
     */
    // eslint-disable-next-line
    private navigateToNextStep(): void {

        this.errorService.clearAll();

        // Improvement?: All diese Sonderregel koennten in getNextStep() vom wizardStepManager sein, damit die gleiche
        // Funktionalität für isButtonDisable wie für die Navigation existiert.
        if (TSWizardStepName.GESUCHSTELLER === this.wizardStepManager.getCurrentStepName()
            && this.gesuchModelManager.getGesuchstellerNumber() === 1
            && this.gesuchModelManager.isGesuchsteller2Required()) {
            this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '2');
            return;
        }
        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            this.navigateToStep(TSWizardStepName.KINDER);
            return;
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            // Diese Logik ist ziemlich kompliziert. Deswegen bleibt sie noch in betreuungView.ts -> Hier wird dann
            // nichts gemacht
            return undefined;
        }
        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 2) {
                this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
            }

            return undefined;
        }
        if (TSWizardStepName.FINANZIELLE_SITUATION === this.wizardStepManager.getCurrentStepName()
            || TSWizardStepName.FINANZIELLE_SITUATION_LUZERN === this.wizardStepManager.getCurrentStepName()
            || TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            const nextSubStep = this.finSitWizardSubStepManager.getNextSubStepFinanzielleSituation(this.dvSubStepName);
            const nextMainStep = this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch());
            this.navigateToSubStepFinanzielleSituation(
                nextSubStep,
                nextMainStep);
            return;
        }
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG === this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN === this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                const info = this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo();
                if (info && info.einkommensverschlechterung) { // was muss hier sein?
                    if (info.ekvFuerBasisJahrPlus1) {
                        this.navigateToStepEinkommensverschlechterung('1', undefined);
                    }
                    this.navigateToStepEinkommensverschlechterung('1', '2');
                }
                this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
                return;
            }
            if (this.dvSubStep === 2) {
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) { // gehe ekv 1/2
                    this.navigateToStepEinkommensverschlechterung('1', '1');
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                    this.navigateToStepEinkommensverschlechterung('1', '2');
                }
                return undefined;
            }
            if (this.dvSubStep === 3) {
                this.navigateNextEVSubStep3();
                return;
            }
            if (this.dvSubStep === 4) {
                this.navigateNextEVSubStep4();
                return;
            }

            return undefined;
        }

        // by default navigieren wir zum naechsten erlaubten Step
        this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
    }

    /**
     * Berechnet fuer den aktuellen Benutzer und Step, welcher der previous Step ist und wechselt zu diesem.
     * wenn es kein Sonderfall ist wird der letzte else case ausgefuehrt
     */
    // eslint-disable-next-line
    private navigateToPreviousStep(): void {
        this.errorService.clearAll();

        if (TSWizardStepName.GESUCH_ERSTELLEN === this.wizardStepManager.getCurrentStepName()) {
            this.navigateToStep(TSWizardStepName.GESUCH_ERSTELLEN);
            return;
        }

        if (TSWizardStepName.GESUCHSTELLER === this.wizardStepManager.getCurrentStepName()
            && this.gesuchModelManager.getGesuchstellerNumber() === 2) {
            this.navigateToStep(TSWizardStepName.GESUCHSTELLER, '1');
            return;
        }

        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            this.navigateToStep(TSWizardStepName.KINDER);
            return;
        }

        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            this.navigateToStep(TSWizardStepName.BETREUUNG);
            return;
        }

        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            this.navigateToStep(TSWizardStepName.ERWERBSPENSUM);
            return;
        }

        if (TSWizardStepName.FINANZIELLE_SITUATION === this.wizardStepManager.getCurrentStepName()
            || TSWizardStepName.FINANZIELLE_SITUATION_LUZERN === this.wizardStepManager.getCurrentStepName()
            || TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            const previousSubStep = this.finSitWizardSubStepManager.getPreviousSubStepFinanzielleSituation(this.dvSubStepName);
            const previousMainStep = this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch());

            this.navigateToSubStepFinanzielleSituation(previousSubStep, previousMainStep);
            return;
        }

        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG === this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN === this.wizardStepManager.getCurrentStepName() ||
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            if (this.dvSubStep === 1) {
                this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
            }
            if (this.dvSubStep === 2) {
                this.navigateToStep(this.wizardStepManager.getCurrentStepName());
            }
            if (this.dvSubStep === 3) {
                this.navigatePreviousEVSubStep3();
            }
            if (this.dvSubStep === 4) {
                this.navigatePreviousEVSubStep4();
            }

            return;
        }

        if (TSWizardStepName.VERFUEGEN === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 2) {
            this.navigateToStep(TSWizardStepName.VERFUEGEN);
            return;
        }

        this.navigateToStep(this.wizardStepManager.getPreviousStep(this.gesuchModelManager.getGesuch()));
    }

    private navigateToSubStepFinanzielleSituation(
        navigateToSubStep: TSFinanzielleSituationSubStepName,
        navigateToStepIfNoSubstep: TSWizardStepName
    ): void {
        switch (navigateToSubStep) {
            case TSFinanzielleSituationSubStepName.KEIN_WEITERER_SUBSTEP:
                this.navigateToStep(navigateToStepIfNoSubstep);
                return;
            case TSFinanzielleSituationSubStepName.BERN_START:
                this.navigateToStep(TSWizardStepName.FINANZIELLE_SITUATION);
                return;
            case TSFinanzielleSituationSubStepName.BERN_GS1:
                this.navigateToStepFinanzielleSituation('1');
                return;
            case TSFinanzielleSituationSubStepName.BERN_GS2:
                this.navigateToStepFinanzielleSituation('2');
                return;
            case TSFinanzielleSituationSubStepName.BERN_RESULTATE:
                this.navigateToFinanziellSituationResultate();
                return;
            case TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE:
            case TSFinanzielleSituationSubStepName.BERN_SOZIALHILFE_DETAIL:
                this.navigateToSozialhilfeZeitraeume();
                return;
            case TSFinanzielleSituationSubStepName.LUZERN_START:
                this.navigateToLuzernStart();
                return;
            case TSFinanzielleSituationSubStepName.LUZERN_GS2:
                this.navigateToLuzernGS2();
                return;
            case TSFinanzielleSituationSubStepName.SOLOTHURN_START:
                this.navigateToSolothurnStart();
                return;
            case TSFinanzielleSituationSubStepName.SOLOTHURN_GS1:
                this.navigateToSolothurnGS1();
                return;
            case TSFinanzielleSituationSubStepName.SOLOTHURN_GS2:
                this.navigateToSolothurnGS2();
                return;
            default:
                throw new Error(`not implemented for Substep ${navigateToSubStep}`);
        }
    }

    /**
     * Diese Methode navigierte zum ersten substep jedes Steps. Fuer die navigation innerhalb eines Steps muss
     * man eine extra Methode machen
     */
    private navigateToStep(stepName: TSWizardStepName, gsNumber?: string): void {
        const gesuchId = this.getGesuchId();
        const gesuchIdParam = {gesuchId};
        const gesuchstellerParams = {gesuchstellerNumber: gsNumber ? gsNumber : '1', gesuchId};

        switch (stepName) {
            case TSWizardStepName.GESUCH_ERSTELLEN:
                this.$state.go('gesuch.fallcreation', this.getFallCreationParams());
                return;
            case TSWizardStepName.FAMILIENSITUATION:
                this.$state.go('gesuch.familiensituation', gesuchIdParam);
                return;
            case TSWizardStepName.GESUCHSTELLER:
                this.$state.go('gesuch.stammdaten', gesuchstellerParams);
                return;
            case TSWizardStepName.UMZUG:
                this.$state.go('gesuch.umzug', gesuchIdParam);
                return;
            case TSWizardStepName.KINDER:
                this.$state.go('gesuch.kinder', gesuchIdParam);
                return;
            case TSWizardStepName.BETREUUNG:
                this.$state.go('gesuch.betreuungen', gesuchIdParam);
                return;
            case TSWizardStepName.ABWESENHEIT:
                this.$state.go('gesuch.abwesenheit', gesuchIdParam);
                return;
            case TSWizardStepName.ERWERBSPENSUM:
                this.$state.go('gesuch.erwerbsPensen', gesuchIdParam);
                return;
            case TSWizardStepName.FINANZIELLE_SITUATION:
                this.$state.go('gesuch.finanzielleSituationStart', gesuchIdParam);
                return;
            case TSWizardStepName.FINANZIELLE_SITUATION_LUZERN:
                this.$state.go('gesuch.finanzielleSituationStartLuzern', gesuchIdParam);
                return;
            case TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN:
                this.$state.go('gesuch.finanzielleSituationStartSolothurn', gesuchIdParam);
                return;
            case TSWizardStepName.FINANZIELLE_SITUATION_APPENZELL:
                this.$state.go('gesuch.finanzielleSituationAppenzell', gesuchIdParam);
                return;
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN:
            case TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN:
                this.$state.go('gesuch.einkommensverschlechterungInfo', gesuchIdParam);
                return;
            case TSWizardStepName.DOKUMENTE:
                this.$state.go('gesuch.dokumente', gesuchIdParam);
                return;
            case TSWizardStepName.FREIGABE:
                this.$state.go('gesuch.freigabe', gesuchIdParam);
                return;
            case TSWizardStepName.VERFUEGEN:
                this.$state.go('gesuch.verfuegen', gesuchIdParam);
                return;
            default:
                throw new Error(`not implemented for step ${stepName}`);
        }
    }

    private getFallCreationParams(): {
        eingangsart: TSEingangsart; gesuchId: string; gesuchsperiodeId: string; dossierId: string; gemeindeId: string;
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

    private navigateToStepEinkommensverschlechterung(gsNumber: string, basisjahrPlus: string): TransitionPromise {
        let stateName = 'gesuch.einkommensverschlechterung';
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN === this.wizardStepManager.getCurrentStepName()) {
            stateName = 'gesuch.einkommensverschlechterungLuzern';
        }
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            stateName = 'gesuch.einkommensverschlechterungSolothurn';
        }
        return this.$state.go(stateName, {
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
        return this.$state.go(path, {
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToStepEinkommensverschlechterungResultate(basisjahrPlus: string): TransitionPromise {
        let stateName = 'gesuch.einkommensverschlechterungResultate';
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN === this.wizardStepManager.getCurrentStepName()) {
            stateName = 'gesuch.einkommensverschlechterungLuzernResultate';
        }
        if (TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN === this.wizardStepManager.getCurrentStepName()) {
            stateName = 'gesuch.einkommensverschlechterungSolothurnResultate';
        }
        return this.$state.go(stateName, {
            basisjahrPlus: basisjahrPlus ? basisjahrPlus : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToStepFinanzielleSituation(gsNumber: string): TransitionPromise {
        return this.$state.go('gesuch.finanzielleSituation', {
            gesuchstellerNumber: gsNumber ? gsNumber : '1',
            gesuchId: this.getGesuchId()
        });
    }

    private navigateToLuzernStart(): any {
        return this.$state.go('gesuch.finanzielleSituationStartLuzern', {
            gesuchId: this.getGesuchId()
        });
    }

    // eslint-disable-next-line
    private navigateToLuzernGS2(): any {
        return this.$state.go('gesuch.finanzielleSituationGS2Luzern', {
            gesuchId: this.getGesuchId()
        });
    }

    // eslint-disable-next-line
    private navigateToSolothurnStart(): any {
        return this.$state.go('gesuch.finanzielleSituationStartSolothurn', {
            gesuchId: this.getGesuchId()
        });
    }

    // eslint-disable-next-line
    private navigateToSolothurnGS1(): any {
        return this.$state.go('gesuch.finanzielleSituationGS1Solothurn', {
            gesuchId: this.getGesuchId(),
            gsNummer: 1
        });
    }

    // eslint-disable-next-line
    private navigateToSolothurnGS2(): any {
        return this.$state.go('gesuch.finanzielleSituationGS2Solothurn', {
            gesuchId: this.getGesuchId(),
            gsNummer: 2
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
    // eslint-disable-next-line
    public isNextButtonDisabled(): boolean {
        // Wenn das Gesuch disabled ist (z.B. in Rolle Mandant), darf man nur soweit navigieren, wie die Steps
        // besucht sind
        const nextStepBesucht = this.wizardStepManager.isNextStepBesucht(this.gesuchModelManager.getGesuch());
        const nextStepEnabled = this.wizardStepManager.isNextStepEnabled(this.gesuchModelManager.getGesuch());
        if (this.gesuchModelManager.isGesuchReadonly()
            && TSWizardStepName.GESUCHSTELLER !== this.wizardStepManager.getCurrentStepName()) {
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
        if (this.dvNextDisabled) {
            return true;
        }

        if (TSWizardStepName.GESUCHSTELLER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return !this.gesuchModelManager.isGesuchsteller2Required()
                && !nextStepBesucht;
        }
        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return (!this.gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()
                || this.gesuchModelManager.isThereAnyNotGeprueftesKind())
                && !nextStepBesucht;
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return !this.gesuchModelManager.getGesuch().isThereAnyBetreuung()
                && !nextStepBesucht;
        }
        return false;
    }

    public getTooltip(): string {
        if (!this.isNextButtonDisabled()) {
            return undefined;
        }

        if (TSWizardStepName.KINDER === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.translate.instant('KINDER_TOOLTIP_REQUIRED');
        }
        if (TSWizardStepName.BETREUUNG === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.translate.instant('BETREUUNG_TOOLTIP_REQUIRED');
        }
        if (TSWizardStepName.ERWERBSPENSUM === this.wizardStepManager.getCurrentStepName() && this.dvSubStep === 1) {
            return this.translate.instant('ERWERBSPENSUM_TOOLTIP_REQUIRED');
        }

        return undefined;
    }

    // eslint-disable-next-line
    private navigateNextEVSubStep3(): void {
        if ((this.gesuchModelManager.getBasisJahrPlusNumber() === 1)) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
                    this.navigateToStepEinkommensverschlechterung('2', '1');
                    return;
                }
                if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                    this.navigateToStepEinkommensverschlechterung('1', '2');
                    return;
                }
                this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
                return;
            }
            // ist Zustand 2/1
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) {
                this.navigateToStepEinkommensverschlechterung('1', '2'); // gehe ekv 1/2
                return;
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) {
                this.navigateToStepEinkommensverschlechterung('2', '2'); // gehe ekv 2/2
                return;
            }
            this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
            return;
        }
        if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
            // ist Zustand 1/2
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
                this.navigateToStepEinkommensverschlechterung('2', '2');
                return;
            }
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
                return;
            }
            this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
            return;
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
            this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
            return;
        }
        this.navigateToStepEinkommensverschlechterungResultate('2'); // gehe Resultate Bj 2
        return;
    }

    // eslint-disable-next-line
    private navigatePreviousEVSubStep3(): void {
        if ((this.gesuchModelManager.getBasisJahrPlusNumber() === 1)) {
            if (this.gesuchModelManager.getGesuchstellerNumber() === 1) {
                // ist Zustand 1/1
                this.navigateToStep(this.wizardStepManager.getCurrentStepName());
                return;
            }
            // ist Zustand 2/1
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
                return;
            }
            this.navigateToStep(this.wizardStepManager.getCurrentStepName());
            return;
        }
        if (this.gesuchModelManager.getGesuchstellerNumber() === 1) { // ist Zustand 1/2
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/2
                this.navigateToStepEinkommensverschlechterung('2', '1');
                return;
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
                this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
                return;
            }
            this.navigateToStep(this.wizardStepManager.getCurrentStepName());
            return;
        }
        // ist Zustand 2/2
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
            this.navigateToStepEinkommensverschlechterung('1', '2');
            return;
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/2
            this.navigateToStepEinkommensverschlechterung('2', '1');
            return;
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 1)) {
            this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            return;
        }
        this.navigateToStep(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
        return;
    }

    private navigatePreviousEVSubStep4(): void {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 2) {
            // baisjahrPlus2
            if (this.gesuchModelManager.getEkvFuerBasisJahrPlus(1)) {
                this.navigateToStepEinkommensverschlechterungResultate('1'); // gehe Resultate Bj 1
                return;
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
                this.navigateToStepEinkommensverschlechterung('2', '2');
                return;
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
                this.navigateToStepEinkommensverschlechterung('1', '2');
                return;
            }
            if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
                this.navigateToStepEinkommensverschlechterung('2', '1');
                return;
            }
            this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
            return;
        }

        // baisjahrPlus1
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 2)) { // gehe ekv 2/2
            this.navigateToStepEinkommensverschlechterung('2', '2');
            return;
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(1, 2)) { // gehe ekv 1/2
            this.navigateToStepEinkommensverschlechterung('1', '2');
            return;
        }
        if (this.gesuchModelManager.isRequiredEKV_GS_BJ(2, 1)) { // gehe ekv 2/1
            this.navigateToStepEinkommensverschlechterung('2', '1');
            return;
        }
        this.navigateToStepEinkommensverschlechterung('1', '1'); // gehe ekv 1/1
        return;
    }

    private navigateNextEVSubStep4(): void {
        if (this.gesuchModelManager.getBasisJahrPlusNumber() === 1
            && this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo().ekvFuerBasisJahrPlus2) {
            this.navigateToStepEinkommensverschlechterungResultate('2');
            return;
        }

        this.navigateToStep(this.wizardStepManager.getNextStep(this.gesuchModelManager.getGesuch()));
    }

    public setSubstepManager(manager: FinanzielleSituationSubStepManager): void {
        this.finSitWizardSubStepManager = manager;
    }

}

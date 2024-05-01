import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatRadioChange} from '@angular/material/radio';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {TSFinanzielleSituationResultateDTO} from '../../../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';
import {FinanzielleSituationSchwyzService} from '../finanzielle-situation-schwyz.service';

const LOG = LogFactory.createLog('FinanzielleSituationStartSchwyzComponent');

@Component({
    selector: 'dv-finanzielle-situation-start-schwyz',
    templateUrl: './finanzielle-situation-start-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartSchwyzComponent extends AbstractGesuchViewX<TSFinanzModel> implements OnInit {

    public hasMultipleGS = false;
    public gs2Ausgefuellt = false;

    private finSitGS1JAToRestore: TSFinanzielleSituation;
    private finSitGS2JAToRestore: TSFinanzielleSituation;

    public resultate?: TSFinanzielleSituationResultateDTO;

    public constructor(
        protected ref: ChangeDetectorRef,
        protected readonly gesuchModelManager: GesuchModelManager,
        private readonly finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService,
        private readonly wizardstepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardstepManager, TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ);
    }

    public ngOnInit(): void {
        this.wizardstepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.hasMultipleGS = this.gesuchModelManager.getFamiliensituation()
            .hasSecondGesuchsteller(this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis);
        this.gs2Ausgefuellt = EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
        this.initFinanzModel();
    }

    private initFinanzModel(): void {
        this.model =
            new TSFinanzModel(this.gesuchModelManager.getBasisjahr(), this.gesuchModelManager.isGesuchsteller2Required(), 1);
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.setupCalculation();
        this.calculate();
        // this field is not present on schwyz but will be checked in a lot of distributed places. Therefore we set it
        this.model.familienSituation.sozialhilfeBezueger = false;
    }

    public prepareSave(onResult: (arg: any) => void): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    private save(onResult: (arg: any) => void): Promise<TSFinanzielleSituationContainer> {
        const hasGemeinsamChanged = this.getGesuch().extractFamiliensituation().gemeinsameSteuererklaerung
            !== this.model.familienSituation.gemeinsameSteuererklaerung;
        if (this.gesuchModelManager.isGesuchsteller2Required()
            && hasGemeinsamChanged
            && EbeguUtil.isNotNullAndFalse(this.model.familienSituation.gemeinsameSteuererklaerung)) {
            this.resetAllFinSitSchwyzData();
        }
        this.model.copyFinSitDataToGesuch(this.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituationStart()
            .then(() => this.gesuchModelManager.updateGesuch())
            .then(async () => {
                if (!this.hasMultipleGS || this.model.familienSituation.gemeinsameSteuererklaerung) {
                    await this.updateWizardStepStatus();
                }
                onResult(this.getModel());
                return this.getModel();
            }) as Promise<TSFinanzielleSituationContainer>;
    }

    public recalculateMassgendesEinkommen(): void {
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.model);
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    protected updateWizardStepStatus(): Promise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
                TSWizardStepStatus.OK) as Promise<void>;
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.SCHWYZ_START;
    }

    public getSubStepIndex(): number {
        return 0;
    }

    public getYearForDeklaration(): number {
        return this.gesuchModelManager.getBasisjahr();
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public calculate(): void {
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.model);
    }

    public setupCalculation(): void {
        this.finanzielleSituationSchwyzService.massgebendesEinkommenStore.subscribe(resultate => {
                this.resultate = resultate;
                this.ref.markForCheck();
            }, error => LOG.error(error),
        );
    }

    public gemeinsamChanged($event: MatRadioChange): void {
        if ($event.value === true) {
            this.finSitGS1JAToRestore = this.model.finanzielleSituationContainerGS1.finanzielleSituationJA;
            this.finSitGS2JAToRestore = this.model.finanzielleSituationContainerGS2.finanzielleSituationJA;
            this.model.finanzielleSituationContainerGS2.finanzielleSituationJA = new TSFinanzielleSituation();
            if (this.model.finanzielleSituationContainerGS1.finanzielleSituationJA.quellenbesteuert) {
                this.model.finanzielleSituationContainerGS1.finanzielleSituationJA = new TSFinanzielleSituation();
            }
        } else {
            this.model.finanzielleSituationContainerGS2.finanzielleSituationJA =
                this.finSitGS2JAToRestore ? this.finSitGS2JAToRestore : new TSFinanzielleSituation();
            this.model.finanzielleSituationContainerGS1.finanzielleSituationJA =
                this.finSitGS1JAToRestore ? this.finSitGS1JAToRestore : new TSFinanzielleSituation();
        }
        this.calculate();
    }

    private resetAllFinSitSchwyzData(): void {
        this.getModel().finanzielleSituationJA.quellenbesteuert = null;
        this.getModel().finanzielleSituationJA.bruttoLohn = null;
        this.getModel().finanzielleSituationJA.steuerbaresEinkommen = null;
        this.getModel().finanzielleSituationJA.abzuegeLiegenschaft = null;
        this.getModel().finanzielleSituationJA.einkaeufeVorsorge = null;
        this.getModel().finanzielleSituationJA.steuerbaresVermoegen = null;
    }
}

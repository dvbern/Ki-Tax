import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
    selector: 'dv-finanzielle-situation-start-schwyz',
    templateUrl: './finanzielle-situation-start-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartSchwyzComponent extends AbstractGesuchViewX<TSFinanzModel> implements OnInit {

    public hasMultipleGS = false;
    public massgebendesEinkommen = 0;
    public gs2Ausgefuellt = false;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
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

    private resetAllFinSitSchwyzData(): void {
        this.getModel().finanzielleSituationJA.quellenbesteuert = null;
        this.getModel().finanzielleSituationJA.bruttoLohn = null;
        this.getModel().finanzielleSituationJA.steuerbaresEinkommen = null;
        this.getModel().finanzielleSituationJA.abzuegeLiegenschaft = null;
        this.getModel().finanzielleSituationJA.einkaeufeVorsorge = null;
        this.getModel().finanzielleSituationJA.steuerbaresVermoegen = null;
    }
}
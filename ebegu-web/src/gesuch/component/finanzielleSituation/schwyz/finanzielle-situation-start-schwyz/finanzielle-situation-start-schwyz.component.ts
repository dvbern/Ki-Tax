import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {TSGesuch} from '../../../../../models/TSGesuch';
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
        this.model.copyFinSitDataToGesuch(this.getGesuch());
        return this.gesuchModelManager.saveFinanzielleSituationStart().then(async (gesuch: TSGesuch) => {
            this.model.copyFinSitDataToGesuch(gesuch);
            await this.updateWizardStepStatus();
            onResult(this.getModel());
            return this.getModel();
        }) as Promise<TSFinanzielleSituationContainer>;
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    protected updateWizardStepStatus(): Promise<void> {
        const status = this.isFinSitOk() ? TSWizardStepStatus.OK : TSWizardStepStatus.IN_BEARBEITUNG;
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
                status) as Promise<void>;
    }

    private isFinSitOk(): boolean {
        return  !this.hasMultipleGS || this.model.familienSituation.gemeinsameSteuererklaerung;
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

    public resetAllFinSitSchwyzData(): void {
        this.getModel().finanzielleSituationJA.bruttoLohn = null;
        this.getModel().finanzielleSituationJA.steuerbaresEinkommen = null;
        this.getModel().finanzielleSituationJA.abzuegeLiegenschaft = null;
        this.getModel().finanzielleSituationJA.einkaeufeVorsorge = null;
        this.getModel().finanzielleSituationJA.steuerbaresVermoegen = null;
    }
}

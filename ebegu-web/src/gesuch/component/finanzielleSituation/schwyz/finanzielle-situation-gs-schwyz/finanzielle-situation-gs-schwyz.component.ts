import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
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
    selector: 'dv-finanzielle-situation-gs-schwyz',
    templateUrl: './finanzielle-situation-gs-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationGsSchwyzComponent extends AbstractGesuchViewX<TSFinanzModel> implements OnInit {
    public massgebendesEinkommen = 0;

    public constructor(
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardStepManager: WizardStepManager,
        private readonly $stateParams: UIRouterGlobals,
    ) {
        super(gesuchmodelManager, wizardStepManager, TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ);
    }

    public ngOnInit(): void {
        this.initFinanzModel();
    }

    public getModel(): TSFinanzielleSituationContainer {
        return this.model.getFiSiConToWorkWith();
    }

    private initFinanzModel(): void {
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parseInt(this.$stateParams.params.gesuchstellerNumber, 10));
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        // this field is not present on schwyz but will be checked in a lot of distributed places. Therefore we set it
        this.model.familienSituation.sozialhilfeBezueger = false;
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public getAntragstellerNameForCurrentStep(): string {
        const gs = this.model.getGesuchstellerNumber() === 1 ?
            this.gesuchmodelManager.getGesuch().gesuchsteller1 :
            this.gesuchmodelManager.getGesuch().gesuchsteller2;
        return gs.gesuchstellerJA.getFullName();
    }

    public getYearForDeklaration(): number {
        return this.gesuchModelManager.getBasisjahr();
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return this.model.getGesuchstellerNumber() === 1 ?
            TSFinanzielleSituationSubStepName.SCHWYZ_GS1 :
            TSFinanzielleSituationSubStepName.SCHWYZ_GS2;
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
            if (this.model.getGesuchstellerNumber() === 2) {
                await this.updateWizardStepStatus();
            }
            onResult(this.getModel());
            return this.getModel();
        }) as Promise<TSFinanzielleSituationContainer>;
    }

    protected updateWizardStepStatus(): Promise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
                TSWizardStepStatus.OK) as Promise<void>;
    }

}

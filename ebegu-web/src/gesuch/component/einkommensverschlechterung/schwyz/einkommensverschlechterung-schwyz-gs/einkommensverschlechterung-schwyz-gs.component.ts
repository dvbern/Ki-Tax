import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Transition} from '@uirouter/core';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
    selector: 'dv-einkommensverschlechterung-schwyz-gs',
    templateUrl: './einkommensverschlechterung-schwyz-gs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungSchwyzGsComponent extends AbstractGesuchViewX<TSFinanzModel> {

    public hasMultipleFinSit: boolean;
    public isFinSitVollstaendigAusgefuellt: boolean;

    public constructor(
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardstepManager: WizardStepManager,
        private readonly $transition$: Transition,
    ) {
        super(gesuchmodelManager, wizardstepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ);
        this.initModel();
        this.hasMultipleFinSit = this.gesuchmodelManager.isGesuchsteller2Required()
            && EbeguUtil.isNotNullAndFalse(this.gesuchmodelManager.getFamiliensituation().gemeinsameSteuererklaerung);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN,
            TSWizardStepStatus.IN_BEARBEITUNG);
        this.isFinSitVollstaendigAusgefuellt =
            this.wizardstepManager.getStepByName(TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ).wizardStepStatus
            === TSWizardStepStatus.OK;
    }

    public prepareSave(onResult: (arg: any) => any): void {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }

        if (!this.form.dirty) {
            // If there are no changes in form we don't need anything to update
            onResult(this.model.getEkvContToWorkWith());
            return;
        }
        this.model.copyEkvSitDataToGesuch(this.getGesuch());
        this.gesuchmodelManager.saveEinkommensverschlechterungContainer().then(ekv => {
            onResult(ekv);
            if (!this.gesuchmodelManager.isGesuchsteller2Required() || this.gesuchmodelManager.gesuchstellerNumber === 2) {
                return this.updateWizardStepStatus();
            }
            return Promise.resolve();
        });
    }

    /**
     * updates the Status of the Step depending on whether the Gesuch is a Mutation or not
     */
    private updateWizardStepStatus(): Promise<void> {
        return this.gesuchModelManager.getGesuch().isMutation() ?
            this.wizardStepManager.updateCurrentWizardStepStatusMutiert() as Promise<void> :
            this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ,
                TSWizardStepStatus.OK) as Promise<void>;
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    private initModel() {
        const parsedGesuchstelllerNum = parseInt(this.$transition$.params().gesuchstellerNumber, 10);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.gesuchModelManager.setGesuchstellerNumber(parsedGesuchstelllerNum);
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            parsedGesuchstelllerNum, parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.initEinkommensverschlechterungContainer(parsedBasisJahrPlusNum, parsedGesuchstelllerNum);
    }

    public getGSName(): string {
        return this.gesuchmodelManager.gesuchstellerNumber === 1 ? this.extractFullNameGS1() : this.extractFullNameGS2();
    }

}

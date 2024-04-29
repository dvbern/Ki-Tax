import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
    selector: 'dv-einkommensverschlechterung-schwyz-resultate',
    templateUrl: './einkommensverschlechterung-schwyz-resultate.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EinkommensverschlechterungSchwyzResultateComponent extends AbstractGesuchViewX<TSFinanzModel> {
    public massgebendesEinkommen = 0;
    public massgebendesEinkommenGS1 = 0;
    public massgebendesEinkommenGS2 = 0;

    public constructor(
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardstepManager: WizardStepManager,
    ) {
        super(gesuchmodelManager, wizardstepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ);
    }

    public save(onResult: (arg: any) => any): void {
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(
            this.wizardStepManager.getCurrentStepName(),
            TSWizardStepStatus.OK).then(() => {
            onResult(true);
        });
    }

    public hasMultipleFinSits(): boolean {
        return this.gesuchmodelManager.isGesuchsteller2Required()
            && EbeguUtil.isNotNullAndFalse(this.getGesuch().extractFamiliensituation().gemeinsameSteuererklaerung);
    }
}

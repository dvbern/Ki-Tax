import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
  selector: 'dv-einkommensverschlechterung-schwyz-gs',
  templateUrl: './einkommensverschlechterung-schwyz-gs.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EinkommensverschlechterungSchwyzGsComponent extends AbstractGesuchViewX<TSFinanzModel>{

    public constructor(
        protected readonly gesuchmodelManager: GesuchModelManager,
        protected readonly wizardstepManager: WizardStepManager,
    ) {
        super(gesuchmodelManager, wizardstepManager, TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ);
    }

    public prepareSave(onResult: (arg: any) => any): void {
        onResult(true);
    }
}

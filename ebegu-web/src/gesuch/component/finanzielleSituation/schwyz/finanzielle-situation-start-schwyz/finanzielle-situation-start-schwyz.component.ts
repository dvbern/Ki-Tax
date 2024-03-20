import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../../../abstractGesuchViewX';

@Component({
    selector: 'dv-finanzielle-situation-start-schwyz',
    templateUrl: './finanzielle-situation-start-schwyz.component.html',
    styleUrls: ['./finanzielle-situation-start-schwyz.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartSchwyzComponent extends AbstractGesuchViewX<TSFinanzModel> implements OnInit {

    public constructor(
        private readonly gesuchModelManaager: GesuchModelManager,
        private readonly wizardstepManager: WizardStepManager,
    ) {
        super(gesuchModelManaager, wizardstepManager, TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ);
    }

    public ngOnInit(): void {
        this.wizardstepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.FINANZIELLE_SITUATION_SCHWYZ,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public prepareSave(onResult: (arg: any) => void): Promise<TSFinanzielleSituationContainer> {
        return this.save(onResult);
    }

    private save(onResult: (arg: any) => void): Promise<TSFinanzielleSituationContainer> {
        onResult(this.gesuchModelManaager.getGesuch().gesuchsteller1.finanzielleSituationContainer);
        return Promise.resolve(this.gesuchModelManaager.getGesuch().gesuchsteller1.finanzielleSituationContainer);
    }

    public getSubStepName(): TSFinanzielleSituationSubStepName {
        return TSFinanzielleSituationSubStepName.SCHWYZ_START;
    }

    public getSubStepIndex(): number {
        return 0;
    }
}

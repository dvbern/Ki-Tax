import {ChangeDetectionStrategy, Component} from '@angular/core';
import {IPromise} from 'angular';
import {TSFinanzielleSituationSubStepName} from '../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSWizardStepName} from '../../../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../../../models/enums/TSWizardStepStatus';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../AbstractFinSitsolothurnView';

@Component({
    selector: 'dv-finanzielle-situation-start-solothurn',
    templateUrl: '../finanzielle-situation-solothurn.component.html',
    styleUrls: ['./finanzielle-situation-start-solothurn.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FinanzielleSituationStartSolothurnComponent extends AbstractFinSitsolothurnView {
    public sozialhilfeBezueger: boolean;
    public finanzielleSituationRequired: boolean;
    public verguenstigungGewuenscht: boolean;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
    ) {
        super(gesuchModelManager, wizardStepManager, 1);
        this.wizardStepManager.updateCurrentWizardStepStatusSafe(TSWizardStepName.FINANZIELLE_SITUATION_SOLOTHURN,
            TSWizardStepStatus.IN_BEARBEITUNG);
    }

    public ngOnInit(): void {
    }

    public getAntragstellerNummer(): number {
        return 1;
    }

    public getSubStepIndex(): number {
        return 1;
    }

    public getSubStepName(): string {
        return TSFinanzielleSituationSubStepName.SOLOTHURN_START;
    }

    public isGemeinsam(): boolean {
        return false;
    }

    public notify(): void {
    }

    public prepareSave(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        console.log(this.sozialhilfeBezueger, this.finanzielleSituationRequired, this.verguenstigungGewuenscht);
        return undefined;
    }

}

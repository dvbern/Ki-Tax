import {Component, ChangeDetectionStrategy} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../../AbstractFinSitsolothurnView';

@Component({
    selector: 'dv-angaben-gs2',
    templateUrl: '../angaben-gs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AngabenGs2Component extends AbstractFinSitsolothurnView {

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        public wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, 1);
    }

    public ngOnInit(): void {
    }

    public getAntragstellerNummer(): number {
        return 0;
    }

    public getSubStepIndex(): number {
        return 0;
    }

    public getSubStepName(): string {
        return TSFinanzielleSituationSubStepName.SOLOTHURN_GS2;
    }

    public notify(): void {
    }

    public prepareSave(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        onResult(true);
        return undefined;
    }

    public isGemeinsam(): boolean {
        return true;
    }
}

import {Component, ChangeDetectionStrategy, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
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

    @ViewChild(NgForm) private readonly form: NgForm;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        public wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, 2);
    }

    public ngOnInit(): void {
    }

    public getAntragstellerNummer(): number {
        return 2;
    }

    public getSubStepIndex(): number {
        return 2;
    }

    public getSubStepName(): string {
        return TSFinanzielleSituationSubStepName.SOLOTHURN_GS2;
    }

    public notify(): void {
    }

    public prepareSave(onResult: Function): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid(this.form)) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    public isGemeinsam(): boolean {
        return true;
    }
}

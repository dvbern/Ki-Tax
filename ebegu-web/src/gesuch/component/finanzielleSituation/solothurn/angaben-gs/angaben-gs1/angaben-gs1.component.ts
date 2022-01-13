import {Component, ChangeDetectionStrategy, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TSFinanzielleSituationSubStepName} from '../../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../../AbstractFinSitsolothurnView';

@Component({
    selector: 'dv-angaben-gs1',
    templateUrl: '../angaben-gs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AngabenGs1Component extends AbstractFinSitsolothurnView {

    @ViewChild(NgForm) private readonly form: NgForm;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        public wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, 1);
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
        return TSFinanzielleSituationSubStepName.SOLOTHURN_GS1;
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

    public steuerveranlagungErhaltenChange(steuerveranlagungErhalten: boolean): void {
        if (EbeguUtil.isNotNullAndTrue(steuerveranlagungErhalten)) {
            this.resetBruttoLohn();
        }
        // tslint:disable-next-line:early-exit
        if (EbeguUtil.isNotNullAndFalse(steuerveranlagungErhalten)) {
            this.resetVeranlagungSolothurn();
        }
    }
}

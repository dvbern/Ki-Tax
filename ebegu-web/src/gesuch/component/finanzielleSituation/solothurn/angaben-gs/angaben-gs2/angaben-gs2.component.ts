import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TSFinanzielleSituationSubStepName} from '../../../../../../models/enums/TSFinanzielleSituationSubStepName';
import {TSFinanzielleSituationContainer} from '../../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../../../service/wizardStepManager';
import {AbstractFinSitsolothurnView} from '../../AbstractFinSitsolothurnView';
import {FinanzielleSituationSolothurnService} from '../../finanzielle-situation-solothurn.service';

@Component({
    selector: 'dv-angaben-gs2',
    templateUrl: '../angaben-gs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AngabenGs2Component extends AbstractFinSitsolothurnView {
    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected readonly finSitSoService: FinanzielleSituationSolothurnService,
        public wizardStepManager: WizardStepManager
    ) {
        super(gesuchModelManager, wizardStepManager, finSitSoService, 2);
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

    public prepareSave(onResult: (arg: any) => any): Promise<TSFinanzielleSituationContainer> {
        if (!this.isGesuchValid()) {
            onResult(undefined);
            return undefined;
        }
        return this.save(onResult);
    }

    public isGemeinsam(): boolean {
        return false;
    }

    public steuerveranlagungErhaltenChange(
        steuerveranlagungErhalten: boolean
    ): void {
        if (EbeguUtil.isNotNullAndTrue(steuerveranlagungErhalten)) {
            this.resetBruttoLohn();
        }

        if (EbeguUtil.isNotNullAndFalse(steuerveranlagungErhalten)) {
            this.resetVeranlagungSolothurn();
        }
    }
}

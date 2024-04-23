import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationSchwyzService} from '../finanzielle-situation-schwyz.service';

@Component({
    selector: 'dv-steuerveranlagt-schwyz',
    templateUrl: './steuerveranlagt-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagtSchwyzComponent {

    @Input()
    public readonly!: boolean;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        public finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService
    ) {
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public isEKV(abstractFinSit: TSAbstractFinanzielleSituation): abstractFinSit is TSEinkommensverschlechterung {
        return abstractFinSit instanceof TSEinkommensverschlechterung;
    }

    public onValueChangeFunction = (): void => {
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.finanzModel);
    };

    public getFinSitJA(): TSAbstractFinanzielleSituation {
        return this.finanzModel.getFiSiConToWorkWith()?.finanzielleSituationJA;
    }

    public getFinSitGS(): TSAbstractFinanzielleSituation {
        return this.finanzModel.getFiSiConToWorkWith()?.finanzielleSituationGS;
    }

}

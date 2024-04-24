import {ChangeDetectionStrategy, Component, Input, ViewChild} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationSchwyzService} from '../finanzielle-situation-schwyz.service';

@Component({
    selector: 'dv-finanzielle-situation-single-gs',
    templateUrl: './finanzielle-situation-single-gs.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class FinanzielleSituationSingleGsComponent {

    @ViewChild(NgForm) public form: NgForm;

    @Input()
    public readonly!: boolean;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        public finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService
    ) {
    }

    public onQuellenbesteuertChange(): void {
        if (EbeguUtil.isNullOrUndefined(this.getFinSitJA().quellenbesteuert)) {
            return;
        }
        if (this.getFinSitJA().quellenbesteuert) {
            this.getFinSitJA().bruttoLohn = null;
        } else {
            this.getFinSitJA().steuerbaresEinkommen = null;
            this.getFinSitJA().einkaeufeVorsorge = null;
            this.getFinSitJA().abzuegeLiegenschaft = null;
            this.getFinSitJA().steuerbaresVermoegen = null;
        }
        this.recalculateMassgebendesEinkommen();
    }

    public recalculateMassgebendesEinkommen(): void {
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.finanzModel);
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public getFinSitJA(): TSFinanzielleSituation {
        return this.finanzModel.getFiSiConToWorkWith()?.finanzielleSituationJA;
    }
}

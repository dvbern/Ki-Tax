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
    public finSitJA!: TSFinanzielleSituation;

    @Input()
    public readonly!: boolean;

    @Input()
    public finSitGS?: TSFinanzielleSituation;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        public finanzielleSituationSchwyzService: FinanzielleSituationSchwyzService
    ) {
    }

    public onQuellenbesteuertChange(): void {
        if (EbeguUtil.isNullOrUndefined(this.finSitJA.quellenbesteuert)) {
            return;
        }
        if (this.finSitJA.quellenbesteuert) {
            this.finSitJA.bruttoLohn = null;
        } else {
            this.finSitJA.steuerbaresEinkommen = null;
            this.finSitJA.einkaeufeVorsorge = null;
            this.finSitJA.abzuegeLiegenschaft = null;
            this.finSitJA.steuerbaresVermoegen = null;
        }
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.finanzModel);
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

}

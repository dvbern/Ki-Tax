import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
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

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public onValueChangeFunction = (): void => {
        this.finanzielleSituationSchwyzService.calculateMassgebendesEinkommen(this.finanzModel);
    };
}

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-bruttolohn-schwyz',
    templateUrl: './bruttolohn-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class BruttolohnSchwyzComponent {

    @Input()
    public finSitJA!: TSAbstractFinanzielleSituation;

    @Input()
    public readonly!: boolean;

    @Input()
    public finSitGS?: TSAbstractFinanzielleSituation;

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }
}

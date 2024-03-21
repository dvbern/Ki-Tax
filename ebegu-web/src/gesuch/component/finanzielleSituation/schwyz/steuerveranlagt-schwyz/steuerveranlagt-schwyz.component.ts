import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TSFinanzielleSituation} from '../../../../../models/TSFinanzielleSituation';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-steuerveranlagt-schwyz',
    templateUrl: './steuerveranlagt-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SteuerveranlagtSchwyzComponent {

    @Input()
    public finSitJA!: TSFinanzielleSituation;

    @Input()
    public readonly!: boolean;

    @Input()
    public finSitGS?: TSFinanzielleSituation;

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }
}

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-steuerveranlagt-schwyz',
    templateUrl: './steuerveranlagt-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagtSchwyzComponent {

    @Input()
    public finSitJA!: TSAbstractFinanzielleSituation;

    @Input()
    public readonly!: boolean;

    @Input()
    public finSitGS?: TSAbstractFinanzielleSituation;

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public isEKV(abstractFinSit: TSAbstractFinanzielleSituation): abstractFinSit is TSEinkommensverschlechterung {
        return abstractFinSit instanceof TSEinkommensverschlechterung;
    }
}

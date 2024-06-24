import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';
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
    public readonly!: boolean;

    @Input()
    public finSitJA!: TSAbstractFinanzielleSituation;

    @Input()
    public finSitGS?: TSAbstractFinanzielleSituation;

    @Output()
    public readonly valueChanged = new EventEmitter<void>();

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public emitEvent = () => this.valueChanged.emit();
}

import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSEinkommensverschlechterung} from '../../../../../models/TSEinkommensverschlechterung';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-steuerveranlagt-schwyz',
    templateUrl: './steuerveranlagt-schwyz.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class SteuerveranlagtSchwyzComponent implements OnChanges {

    @Input()
    public readonly!: boolean;

    @Input()
    public finSitJA!: TSAbstractFinanzielleSituation;

    @Input()
    public finSitGS?: TSAbstractFinanzielleSituation;

    @Output()
    public valueChanged = new EventEmitter<void>();

    public isEKV = false;

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }

    public onValueChangeFunction = (): void => {
       this.valueChanged.emit();
    };

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.finSitJA) {
            this.isEKV = this.finSitJA instanceof TSEinkommensverschlechterung;
        }
    }

}

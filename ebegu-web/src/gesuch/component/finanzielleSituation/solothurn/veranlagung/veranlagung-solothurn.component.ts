import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-veranlagung-solothurn',
    templateUrl: './veranlagung-solothurn.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class VeranlagungSolothurnComponent {
    @Input() public model: TSFinanzielleSituationContainer;
    @Input() public readOnly: boolean;
    @Input() public dvValueChange: () => void;

    @Output()
    public readonly massgebendesEinkommenChange: EventEmitter<number> =
        new EventEmitter<number>();

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }
}

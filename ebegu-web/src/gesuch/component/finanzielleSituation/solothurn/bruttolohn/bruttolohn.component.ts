import {Component, ChangeDetectionStrategy, Input} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-bruttolohn',
    templateUrl: './bruttolohn.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class BruttolohnComponent {
    @Input() public model: TSFinanzielleSituationContainer;
    @Input() public dvValueChange: () => void;

    public constructor(public gesuchModelManager: GesuchModelManager) {}

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }
}

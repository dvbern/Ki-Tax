import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TSEWKAdresse} from '../../models/TSEWKAdresse';

@Component({
    selector: 'dv-ewk-adresse',
    templateUrl: './ewk-adresse.component.html',
    styleUrls: ['./ewk-adresse.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class EwkAdresseComponent {
    @Input()
    public adresse: TSEWKAdresse;
}

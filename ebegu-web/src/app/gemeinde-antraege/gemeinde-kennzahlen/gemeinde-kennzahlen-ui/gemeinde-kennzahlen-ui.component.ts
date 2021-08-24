import {Component, OnInit, ChangeDetectionStrategy, Input} from '@angular/core';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';

@Component({
    selector: 'dv-gemeinde-kennzahlen-ui',
    templateUrl: './gemeinde-kennzahlen-ui.component.html',
    styleUrls: ['./gemeinde-kennzahlen-ui.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKennzahlenUiComponent implements OnInit {

    @Input()
    public gemeindeKennzahlenId: string;

    public wizardTyp = TSWizardStepXTyp.GEMEINDE_KENNZAHLEN;
    public gemeindeKennzahlen: TSGemeindeKennzahlen;

    constructor() {
    }

    ngOnInit(): void {
    }

}

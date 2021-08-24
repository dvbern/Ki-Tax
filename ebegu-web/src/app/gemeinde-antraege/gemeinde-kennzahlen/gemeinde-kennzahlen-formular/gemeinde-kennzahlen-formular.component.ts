import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';

@Component({
    selector: 'dv-gemeinde-kennzahlen-formular',
    templateUrl: './gemeinde-kennzahlen-formular.component.html',
    styleUrls: ['./gemeinde-kennzahlen-formular.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKennzahlenFormularComponent implements OnInit {

    constructor() {
    }

    ngOnInit(): void {
    }

}

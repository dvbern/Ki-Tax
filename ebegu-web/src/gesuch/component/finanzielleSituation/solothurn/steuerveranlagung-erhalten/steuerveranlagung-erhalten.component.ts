import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-steuerveranlagung-erhalten',
    templateUrl: './steuerveranlagung-erhalten.component.html',
    styleUrls: ['./steuerveranlagung-erhalten.component.less'],
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagungErhaltenComponent implements OnInit {

    @Input() public model: TSFinanzielleSituationContainer;

    @Output() public readonly steuerveranlagungErhaltenChange: EventEmitter<boolean> = new EventEmitter<boolean>();

    public constructor(
        public gesuchModelManager: GesuchModelManager,
    ) {
    }

    public ngOnInit(): void {
    }

    public setSteuerveranlagungErhalten(value: any): void {
        this.model.finanzielleSituationJA.steuerveranlagungErhalten = value;
        this.steuerveranlagungErhaltenChange.emit(value);
    }
}

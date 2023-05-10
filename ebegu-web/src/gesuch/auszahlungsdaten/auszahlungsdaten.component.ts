import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {TSZahlungsinformationen} from '../../models/TSZahlungsinformationen';
import {GesuchModelManager} from '../service/gesuchModelManager';

@Component({
    selector: 'dv-auszahlungsdaten',
    templateUrl: './auszahlungsdaten.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuszahlungsdatenComponent implements OnInit {

    @Input()
    public auszahlungsdaten: TSZahlungsinformationen;

    @Input()
    public auszahlungsdatenGS: TSZahlungsinformationen;

    @Output()
    public readonly auszahlungsdatenChange = new EventEmitter<TSZahlungsinformationen>();

    public constructor(
        private readonly gesuchsmodelManager: GesuchModelManager
    ) {
    }

    public ngOnInit(): void {
    }

    public isReadOnly(): boolean {
        return this.gesuchsmodelManager.isGesuchReadonly();
    }

    public isKorrekturModusOrFreigegeben(): boolean {
        return this.gesuchsmodelManager.isKorrekturModusJugendamt()
    }

}

import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';

@Component({
    selector: 'dv-steuerveranlagung-erhalten',
    templateUrl: './steuerveranlagung-erhalten.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class SteuerveranlagungErhaltenComponent {

    @Input() public model: TSFinanzielleSituationContainer;

    @Output() public readonly steuerveranlagungErhaltenChange: EventEmitter<boolean> = new EventEmitter<boolean>();

    public constructor(
        public gesuchModelManager: GesuchModelManager
    ) {
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public isKorrekturModusJugendamt(): boolean {
        return this.gesuchModelManager.isKorrekturModusJugendamt();
    }

    public setSteuerveranlagungErhalten(value: any): void {
        this.model.finanzielleSituationJA.steuerveranlagungErhalten = value;
        this.steuerveranlagungErhaltenChange.emit(value);
    }

    public showVeranlagungErhalten(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.model.finanzielleSituationJA.momentanSelbststaendig);
    }

    public canEdit(): boolean {
        return !this.gesuchModelManager.isGesuchReadonly();
    }
}

import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {ListResourceRS} from '../../app/core/service/listResourceRS.rest';
import {TSAdresse} from '../../models/TSAdresse';
import {TSZahlungsinformationen} from '../../models/TSZahlungsinformationen';
import {TSLand} from '../../models/types/TSLand';
import {EbeguUtil} from '../../utils/EbeguUtil';
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

    @Input()
    public showAddress = false;

    @Output()
    public readonly auszahlungsdatenChange = new EventEmitter<TSZahlungsinformationen>();

    public laenderList: TSLand[];

    public constructor(
        private readonly gesuchsmodelManager: GesuchModelManager,
        private readonly listResourceRS: ListResourceRS,
    ) {
    }

    public ngOnInit(): void {
        this.listResourceRS.getLaenderList().then((laenderList: TSLand[]) => {
            this.laenderList = laenderList;
        });
    }

    public isReadOnly(): boolean {
        return this.gesuchsmodelManager.isGesuchReadonly();
    }

    public isKorrekturModusOrFreigegeben(): boolean {
        return this.gesuchsmodelManager.isKorrekturModusJugendamt();
    }

    public abweichendeZahlungsadresseChanged(): void {
        if (!this.auszahlungsdaten.abweichendeZahlungsadresse) {
            this.auszahlungsdaten.zahlungsadresse = null;
            return;
        }
        if (EbeguUtil.isNullOrUndefined(this.auszahlungsdaten.zahlungsadresse)) {
            this.auszahlungsdaten.zahlungsadresse = new TSAdresse();
        }
    }

}

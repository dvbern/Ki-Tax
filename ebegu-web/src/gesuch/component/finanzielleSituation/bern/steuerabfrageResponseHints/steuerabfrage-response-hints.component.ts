import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {
    isSteuerdatenAnfrageStatusErfolgreich,
    TSSteuerdatenAnfrageStatus
} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';

@Component({
    selector: 'dv-steuerabfrage-response-hints',
    templateUrl: './steuerabfrage-response-hints.component.html',
    styleUrls: ['./steuerabfrage-response-hints.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SteuerabfrageResponseHintsComponent implements OnInit {

    @Input()
    private readonly status: TSSteuerdatenAnfrageStatus;

    public constructor() {
    }

    public ngOnInit(): void {
    }

    public showZugriffErfolgreich(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.status) &&
            isSteuerdatenAnfrageStatusErfolgreich(this.status);
    }

    public showZugriffFailed(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED;
    }

    public showZugriffUnterjaehrigeFall(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL;
    }

    public showWarningKeinPartnerGemeinsam(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM;
    }

    public showWarningGeburtsdatum(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM;
    }

}

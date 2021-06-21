import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from '@uirouter/core';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSZahlung} from '../../../models/TSZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../../core/service/zahlungRS.rest';

@Component({
    selector: 'dv-zahlungview-x',
    templateUrl: './zahlungview-x.component.html',
    styleUrls: ['./zahlungview-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ZahlungviewXComponent implements OnInit {

    private zahlungen: TSZahlung[] = [];
    private isMahlzeitenzahlungen: boolean = false;

    public itemsByPage: number = 20;

    public constructor(
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly reportRS: ReportRS,
        private readonly zahlungRS: ZahlungRS,
    ) {
    }

    public ngOnInit(): void {
    }

    public gotToUebersicht(): void {
        this.$state.go('zahlungsauftrag.view', {
            isMahlzeitenzahlungen: this.isMahlzeitenzahlungen,
        });
    }

    public downloadDetails(zahlung: TSZahlung): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungReportExcel(zahlung.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            .catch(() => {
                win.close();
            });
    }

    public bestaetigen(zahlung: TSZahlung): void {
        this.zahlungRS.zahlungBestaetigen(zahlung.id).then((response: TSZahlung) => {
            const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungen);
            if (index > -1) {
                this.zahlungen[index] = response;
            }
            EbeguUtil.handleSmarttablesUpdateBug(this.zahlungen);
        });
    }

    // noinspection JSMethodCanBeStatic
    public isBestaetigt(zahlungstatus: TSZahlungsstatus): boolean {
        return zahlungstatus === TSZahlungsstatus.BESTAETIGT;
    }

}

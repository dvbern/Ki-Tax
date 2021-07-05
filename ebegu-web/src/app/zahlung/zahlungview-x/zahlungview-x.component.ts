import {CurrencyPipe} from '@angular/common';
import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSZahlung} from '../../../models/TSZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../services/zahlungRS.rest';

const LOG = LogFactory.createLog('ZahlungviewXComponent');

@Component({
    selector: 'dv-zahlungview-x',
    templateUrl: './zahlungview-x.component.html',
    styleUrls: ['./zahlungview-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ZahlungviewXComponent implements OnInit {

    @ViewChild(MatSort) public sort: MatSort;

    private zahlungen: TSZahlung[] = [];
    private isMahlzeitenzahlungen: boolean = false;
    public datasource: MatTableDataSource<TSZahlung> = new MatTableDataSource<TSZahlung>([]);

    public itemsByPage: number = 20;
    public tableColumns: any[];

    public constructor(
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly reportRS: ReportRS,
        private readonly zahlungRS: ZahlungRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly routerGlobals: UIRouterGlobals,
        private readonly translate: TranslateService,
        private readonly currency: CurrencyPipe,
        private readonly cd: ChangeDetectorRef,
        private readonly errorService: ErrorService,
    ) {
    }

    public ngOnInit(): void {
        if (this.routerGlobals.params.isMahlzeitenzahlungen) {
            this.isMahlzeitenzahlungen = true;
        }

        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        const zahlungsauftragId = this.routerGlobals.params.zahlungsauftragId;
                        if (this.routerGlobals.params.zahlungsauftragId) {
                            return this.zahlungRS.getZahlungsauftragForRole$(
                                principal.getCurrentRole(), zahlungsauftragId);
                        }
                    }

                    return of(null);
                }),
                map(zahlungsauftrag => zahlungsauftrag ? zahlungsauftrag.zahlungen : []),
            )
            .subscribe(
                zahlungen => {
                    this.zahlungen = zahlungen;
                    this.datasource.data = zahlungen;
                    this.datasource.sort = this.sort;
                    this.cd.markForCheck();
                },
                err => LOG.error(err),
            );
        this.setupTableColumns();
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
        this.zahlungRS.zahlungBestaetigen(zahlung.id).subscribe((response: TSZahlung) => {
            const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungen);
            if (index < 0) {
                return;
            }
            this.zahlungen[index] = response;
            this.datasource.data = this.zahlungen;
            this.cd.markForCheck();
        }, error => this.errorService.addMesageAsError(error?.translatedMessage || this.translate.instant('ERROR_UNEXPECTED')));
    }

    // noinspection JSMethodCanBeStatic
    public isBestaetigt(zahlungstatus: TSZahlungsstatus): boolean {
        return zahlungstatus === TSZahlungsstatus.BESTAETIGT;
    }

    private setupTableColumns(): void {
        this.tableColumns = [
            {
                displayedName: this.translate.instant('ZAHLUNG_INSTITUTION'),
                attributeName: 'empfaengerName',
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_BETREUUNGSANGEBOTTYP'),
                attributeName: 'betreuungsangebotTyp',
                displayFunction: (angebotTyp: TSBetreuungsangebotTyp) => this.translate.instant(angebotTyp),
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_TOTAL'),
                attributeName: 'betragTotalZahlung',
                displayFunction: (betrag: number) => this.currency.transform(betrag, '', ''),
            },
        ];
    }

    public getDisplayValue(element: any, column: any): string {
        if (column.displayFunction !== undefined) {
            return column.displayFunction(element[column.attributeName], element);
        }
        return element[column.attributeName];
    }

    public getColumnsAttributeName(): string[] {
        const mapped = this.tableColumns.map(column => column.attributeName);
        mapped.splice(1, 0, 'zahlungPainExcel');
        mapped.push('status');
        return mapped;
    }
}

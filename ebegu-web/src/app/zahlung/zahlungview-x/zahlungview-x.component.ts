import {CurrencyPipe} from '@angular/common';
import {Component, OnInit, ChangeDetectionStrategy, ViewChild, ChangeDetectorRef} from '@angular/core';
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSZahlung} from '../../../models/TSZahlung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {LogFactory} from '../../core/logging/LogFactory';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../../core/service/zahlungRS.rest';

const LOG = LogFactory.createLog('ZahlungViewController');

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
    public datasource: MatTableDataSource<TSZahlung[]>;
    public canSeeBestaetigen = false;

    public itemsByPage: number = 20;
    public tableColumns: any[];
    private principal: TSBenutzer;

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
                        this.canSeeBestaetigen = principal.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
                        this.principal = principal;
                    }

                    return of(null);
                }),
                map(zahlungsauftrag => zahlungsauftrag ? zahlungsauftrag.zahlungen : []),
            )
            .subscribe(
                zahlungen => {
                    this.zahlungen = zahlungen;
                    this.datasource = new MatTableDataSource<TSZahlung[]>(zahlungen);
                    this.cd.markForCheck();
                },
                err => LOG.error(err),
            );
        this.setupTableColumns()
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
            {
                displayedName: this.translate.instant('ZAHLUNG_STATUS'),
                attributeName: 'status',
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
        if (this.principal?.hasOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            mapped.push('bestaetigen');
        }
        return mapped;
    }
}

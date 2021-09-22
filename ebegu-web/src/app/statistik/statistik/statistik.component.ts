/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSRole} from '../../../models/enums/TSRole';
import {TSStatistikParameterType} from '../../../models/enums/TSStatistikParameterType';
import {TSBatchJobInformation} from '../../../models/TSBatchJobInformation';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSStatistikParameter} from '../../../models/TSStatistikParameter';
import {TSWorkJob} from '../../../models/TSWorkJob';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {BatchJobRS} from '../../core/service/batchRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../core/service/reportAsyncRS.rest';

const LOG = LogFactory.createLog('StatistikComponent');

@Component({
    selector: 'dv-statistik',
    templateUrl: './statistik.component.html',
    styleUrls: ['./statistik.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatistikComponent implements OnInit {

    public readonly TSStatistikParameterType = TSStatistikParameterType;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;

    private polling: NodeJS.Timeout;
    public statistikParameter: TSStatistikParameter;
    public gesuchsperioden: Array<TSGesuchsperiode>;
    private readonly DATE_PARAM_FORMAT: string = 'YYYY-MM-DD';
    // Statistiken sind nur moeglich ab Beginn der fruehesten Periode bis Ende der letzten Periode
    public maxDate: moment.Moment;
    public minDate: moment.Moment;
    public userjobs: Array<TSWorkJob>;
    public allJobs: Array<TSBatchJobInformation>;
    public years: string[];
    public institutionStammdatenList: TSInstitutionStammdaten[];
    private showMahlzeitenStatistik: boolean = false;
    public gemeindenMahlzeitenverguenstigungen: TSGemeinde[];
    private flagShowErrorNoGesuchSelected: boolean = false;
    private showKantonStatistik: boolean = false;

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly reportAsyncRS: ReportAsyncRS,
        private readonly downloadRS: DownloadRS,
        private readonly batchJobRS: BatchJobRS,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    private static sortInstitutions(stammdaten: TSInstitutionStammdaten[]): TSInstitutionStammdaten[] {
        return stammdaten.sort((a, b) => {
            return a.institution.name.localeCompare(b.institution.name);
        });
    }

    public ngOnInit(): void {
    }

    public $onDestroy(): void {
        if (this.polling) {
            clearInterval(this.polling);
            LOG.debug('canceled job polling');
        }
    }

    public $onInit(): void {
        this.statistikParameter = new TSStatistikParameter();
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: any) => {
            this.gesuchsperioden = response;
            if (this.gesuchsperioden.length > 0) {
                this.maxDate = this.gesuchsperioden[0].gueltigkeit.gueltigBis;
                this.minDate = DateUtil.localDateToMoment('2017-01-01');
            }
            this.calculateYears();
        });

        this.institutionStammdatenRS.getAllTagesschulenForCurrentBenutzer()
            .then((institutionStammdatenList: TSInstitutionStammdaten[]) => {
                this.institutionStammdatenList = StatistikComponent.sortInstitutions(institutionStammdatenList);
            });
        this.updateShowMahlzeitenStatistik();
        this.updateShowKantonStatistik();
        this.refreshUserJobs();
        this.initBatchJobPolling();
    }

    private initBatchJobPolling(): void {
        // check all 8 seconds for the state
        const delay = 12000;
        this.polling = setInterval(() => this.refreshUserJobs(), delay);
    }

    private refreshUserJobs(): void {
        this.batchJobRS.getBatchJobsOfUser().subscribe((response: TSWorkJob[]) => {
            this.userjobs = response;
        });
    }

    // tslint:disable-next-line:cognitive-complexity
    public generateStatistik(form: NgForm, type?: TSStatistikParameterType): void {
        if (!form.valid) {
            return;
        }
        LOG.debug('Validated Form: ' + form.name);
        const stichtag = this.statistikParameter.stichtag ?
            this.statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT) :
            undefined;
        switch (type) {
            case TSStatistikParameterType.GESUCH_STICHTAG:
                this.reportAsyncRS.getGesuchStichtagReportExcel(stichtag,
                    this.statistikParameter.gesuchsperiode ?
                        this.statistikParameter.gesuchsperiode.toString() :
                        null)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.GESUCH_ZEITRAUM:
                this.reportAsyncRS.getGesuchZeitraumReportExcel(this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.gesuchsperiode ?
                        this.statistikParameter.gesuchsperiode.toString() :
                        null)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.KINDER:
                this.reportAsyncRS.getKinderReportExcel(
                    this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.gesuchsperiode ?
                        this.statistikParameter.gesuchsperiode.toString() :
                        null)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.GESUCHSTELLER:
                this.reportAsyncRS.getGesuchstellerReportExcel(stichtag)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.KANTON:
                this.reportAsyncRS.getKantonReportExcel(this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.kantonSelbstbehalt)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.MITARBEITERINNEN:
                this.reportAsyncRS.getMitarbeiterinnenReportExcel(this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT))
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.BENUTZER:
                this.reportAsyncRS.getBenutzerReportExcel()
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.GESUCHSTELLER_KINDER_BETREUUNG:
                this.reportAsyncRS.getGesuchstellerKinderBetreuungReportExcel(
                    this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.gesuchsperiode ?
                        this.statistikParameter.gesuchsperiode.toString() :
                        null)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    }, () => {
                        LOG.error('An error occurred downloading the document, closing download window.');
                    });
                return;
            case TSStatistikParameterType.ZAHLUNGEN_PERIODE:
                if (this.statistikParameter.gesuchsperiode) {
                    this.reportAsyncRS.getZahlungPeriodeReportExcel(
                        this.statistikParameter.gesuchsperiode)
                        .subscribe((batchExecutionId: string) => {
                            LOG.debug('executionID: ' + batchExecutionId);
                            const startmsg = this.translate.instant('STARTED_GENERATION');
                            this.errorService.addMesageAsInfo(startmsg);
                        });
                } else {
                    LOG.warn('gesuchsperiode muss gewählt sein');
                }
                return;
            case TSStatistikParameterType.MASSENVERSAND:
                if (!this.isMassenversandValid()) {
                    return;
                }
                if (this.statistikParameter.text) {
                    this.openRemoveDialog$().subscribe(() => {
                        this.createMassenversand();
                    }, err => LOG.error(err));
                } else {
                    this.createMassenversand();
                }
                return;
            case TSStatistikParameterType.INSTITUTIONEN:
                this.reportAsyncRS.getInstitutionenReportExcel()
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.VERRECHNUNG_KIBON:
                this.reportAsyncRS.getVerrechnungKibonReportExcel(
                    this.statistikParameter.doSave, this.statistikParameter.betragProKind)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.LASTENAUSGLEICH_KIBON:
                this.reportAsyncRS.getLastenausgleichKibonReportExcel(this.statistikParameter.jahr)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.TAGESSCHULE_ANMELDUNGEN:
                this.reportAsyncRS.getTagesschuleAnmeldungenReportExcel(
                    this.statistikParameter.tagesschuleAnmeldungen.id,
                    this.statistikParameter.gesuchsperiode)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.TAGESSCHULE_RECHNUNGSSTELLUNG:
                this.reportAsyncRS.getTagesschuleRechnungsstellungReportExcel()
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.NOTRECHT:
                this.reportAsyncRS.getNotrechtReportExcel(
                    this.statistikParameter.doSave)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.MAHLZEITENVERGUENSTIGUNG:
                this.reportAsyncRS.getMahlzeitenverguenstigungReportExcel(
                    this.statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this.statistikParameter.gemeindeMahlzeitenverguenstigungen)
                    .subscribe((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            default:
                throw new Error(`unknown TSStatistikParameterType: ${type}`);
        }
    }

    private openRemoveDialog$(): Observable<boolean> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_TITLE'),
            deleteText: this.translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_INFO'),
        };
        return this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed();
    }

    private createMassenversand(): void {
        LOG.info('Erstelle Massenversand');
        this.reportAsyncRS.getMassenversandReportExcel(
            this.statistikParameter.von ? this.statistikParameter.von.format(this.DATE_PARAM_FORMAT) : null,
            this.statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
            this.statistikParameter.gesuchsperiode.toString(),
            this.statistikParameter.bgGesuche,
            this.statistikParameter.mischGesuche,
            this.statistikParameter.tsGesuche,
            this.statistikParameter.ohneFolgegesuche,
            this.statistikParameter.text)
            .subscribe((batchExecutionId: string) => {
                this.informReportGenerationStarted(batchExecutionId);
            }, () => {
                LOG.error('An error occurred downloading the document, closing download window.');
            });
    }

    private informReportGenerationStarted(batchExecutionId: string): void {
        LOG.debug('executionID: ' + batchExecutionId);
        const startmsg = this.translate.instant('STARTED_GENERATION');
        this.errorService.addMesageAsInfo(startmsg);
        this.refreshUserJobs();
    }

    public rowClicked(row: TSWorkJob): void {
        if (EbeguUtil.isNullOrUndefined(row) || EbeguUtil.isNullOrUndefined(row.execution)) {
            return;
        }

        if (EbeguUtil.isNullOrUndefined(row.execution.batchStatus) || row.execution.batchStatus !== 'COMPLETED') {
            LOG.info('batch-job is not yet finnished');
            return;
        }

        const win = this.downloadRS.prepareDownloadWindow();
        LOG.debug('accessToken: ' + row.resultData);
        this.downloadRS.startDownload(row.resultData, 'report.xlsx', false, win);
    }

    /**
     * helper methode die es dem Admin erlaubt alle jobs zu sehen
     */
    public showAllJobs(): void {
        this.batchJobRS.getAllJobs().subscribe((result: TSWorkJob[]) => {
            let res: TSBatchJobInformation[] = [];
            res = res.concat(result.map(value => {
                return value.execution || undefined;
            }));
            this.allJobs = res;
        });
    }

    /**
     * Takes all years of all Gesuchsperioden and saves them as a string into an array
     */
    private calculateYears(): void {
        this.years = [];
        this.gesuchsperioden
            .forEach(periode => {
                if (this.years.indexOf(periode.getBasisJahrPlus1().toString()) < 0) {
                    this.years.push(periode.getBasisJahrPlus1().toString());
                }
                if (this.years.indexOf(periode.getBasisJahrPlus2().toString()) < 0) {
                    this.years.push(periode.getBasisJahrPlus2().toString());
                }
            });

        this.years.sort();
    }

    public getGesuchsperiodenForTagesschule(stammdaten: TSInstitutionStammdaten): TSGesuchsperiode[] {
        return stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.map(d => {
            return d.gesuchsperiode;
        }).sort((a, b) => {
            return b.gesuchsperiodeString.localeCompare(a.gesuchsperiodeString);
        });
    }

    public showMahlzeitenverguenstigungStatistik(): boolean {
        return this.gemeindenMahlzeitenverguenstigungen && this.gemeindenMahlzeitenverguenstigungen.length > 0;
    }

    private updateShowMahlzeitenStatistik(): void {
        this.showMahlzeitenStatistik = false;
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole())) {
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS.getGemeindenWithMahlzeitenverguenstigungForBenutzer().then(value => {
            // falls es nur eine Gemeinde gibt, wird dropdown nicht angezeigt
            if (value.length === 1) {
                this.statistikParameter.gemeindeMahlzeitenverguenstigungen = value[0];
            }
            this.gemeindenMahlzeitenverguenstigungen = value;
        });
    }

    private isMassenversandValid(): boolean {
        // simulate a click in the checkboxes of Verantwortlichkeit
        this.gesuchTypeClicked();
        return !this.flagShowErrorNoGesuchSelected;

    }

    public gesuchTypeClicked(): void {
        this.flagShowErrorNoGesuchSelected =
            !this.statistikParameter.bgGesuche
            && !this.statistikParameter.mischGesuche
            && !this.statistikParameter.tsGesuche;
    }

    public updateShowKantonStatistik(): void {
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            this.showKantonStatistik = true;
            return;
        }
        this.institutionStammdatenRS.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer().then(response => {
            response.forEach(angebottyp => {
                if (angebottyp !== TSBetreuungsangebotTyp.TAGESSCHULE) {
                    this.showKantonStatistik = true;
                }
            });
        });
    }

}

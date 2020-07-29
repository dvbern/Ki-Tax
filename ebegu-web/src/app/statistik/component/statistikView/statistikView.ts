/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {IComponentOptions, IController, IPromise} from 'angular';
import * as moment from 'moment';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSStatistikParameterType} from '../../../../models/enums/TSStatistikParameterType';
import {TSBatchJobInformation} from '../../../../models/TSBatchJobInformation';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {TSInstitutionStammdaten} from '../../../../models/TSInstitutionStammdaten';
import {TSStatistikParameter} from '../../../../models/TSStatistikParameter';
import {TSWorkJob} from '../../../../models/TSWorkJob';
import {DateUtil} from '../../../../utils/DateUtil';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {DvDialog} from '../../../core/directive/dv-dialog/dv-dialog';
import {ErrorService} from '../../../core/errors/service/ErrorService';
import {BatchJobRS} from '../../../core/service/batchRS.rest';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import {GesuchsperiodeRS} from '../../../core/service/gesuchsperiodeRS.rest';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';
import {ReportAsyncRS} from '../../../core/service/reportAsyncRS.rest';
import IFormController = angular.IFormController;
import ILogService = angular.ILogService;
import ITranslateService = angular.translate.ITranslateService;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');

export class StatistikViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./statistikView.html');
    public controller = StatistikViewController;
    public controllerAs = 'vm';
}

export class StatistikViewController implements IController {

    public get statistikParameter(): TSStatistikParameter {
        return this._statistikParameter;
    }

    public get gesuchsperioden(): Array<TSGesuchsperiode> {
        return this._gesuchsperioden;
    }

    public static $inject: string[] = [
        'GesuchsperiodeRS',
        'InstitutionStammdatenRS',
        '$log',
        'ReportAsyncRS',
        'DownloadRS',
        'BatchJobRS',
        'ErrorService',
        '$translate',
        '$interval',
        'DvDialog',
    ];

    public readonly TSStatistikParameterType = TSStatistikParameterType;

    private polling: IPromise<any>;
    private _statistikParameter: TSStatistikParameter;
    private _gesuchsperioden: Array<TSGesuchsperiode>;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;
    private readonly DATE_PARAM_FORMAT: string = 'YYYY-MM-DD';
    // Statistiken sind nur moeglich ab Beginn der fruehesten Periode bis Ende der letzten Periode
    public maxDate: moment.Moment;
    public minDate: moment.Moment;
    public userjobs: Array<TSWorkJob>;
    public allJobs: Array<TSBatchJobInformation>;
    public years: string[];
    public institutionStammdatenList: TSInstitutionStammdaten[];

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly $log: ILogService,
        private readonly reportAsyncRS: ReportAsyncRS,
        private readonly downloadRS: DownloadRS,
        private readonly batchJobRS: BatchJobRS,
        private readonly errorService: ErrorService,
        private readonly $translate: ITranslateService,
        private readonly $interval: angular.IIntervalService,
        private readonly dvDialog: DvDialog,
    ) {
    }

    private static sortInstitutions(stammdaten: TSInstitutionStammdaten[]): TSInstitutionStammdaten[] {
        return stammdaten.sort((a, b) => {
            return a.institution.name.localeCompare(b.institution.name);
        });
    }

    public $onInit(): void {
        this._statistikParameter = new TSStatistikParameter();
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: any) => {
            this._gesuchsperioden = response;
            if (this._gesuchsperioden.length > 0) {
                this.maxDate = this._gesuchsperioden[0].gueltigkeit.gueltigBis;
                this.minDate = DateUtil.localDateToMoment('2017-01-01');
            }
            this.calculateYears();
        });

        this.institutionStammdatenRS.getAllTagesschulenForCurrentBenutzer()
            .then((institutionStammdatenList: TSInstitutionStammdaten[]) => {
                this.institutionStammdatenList = StatistikViewController.sortInstitutions(institutionStammdatenList);
            });

        this.refreshUserJobs();
        this.initBatchJobPolling();
    }

    public $onDestroy(): void {
        if (this.polling) {
            this.$interval.cancel(this.polling);
            this.$log.debug('canceled job polling');
        }
    }

    private initBatchJobPolling(): void {
        // check all 8 seconds for the state
        const delay = 12000;
        this.polling = this.$interval(() => this.refreshUserJobs(), delay);

    }

    private refreshUserJobs(): void {
        this.batchJobRS.getBatchJobsOfUser().then((response: TSWorkJob[]) => {
            this.userjobs = response;
        });
    }

    // tslint:disable-next-line:cognitive-complexity
    public generateStatistik(form: IFormController, type?: TSStatistikParameterType): void {
        if (!form.$valid) {
            return;
        }
        this.$log.debug('Validated Form: ' + form.$name);
        const stichtag = this._statistikParameter.stichtag ?
            this._statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT) :
            undefined;
        switch (type) {
            case TSStatistikParameterType.GESUCH_STICHTAG:
                this.reportAsyncRS.getGesuchStichtagReportExcel(stichtag,
                    this._statistikParameter.gesuchsperiode ?
                        this._statistikParameter.gesuchsperiode.toString() :
                        null)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.GESUCH_ZEITRAUM:
                this.reportAsyncRS.getGesuchZeitraumReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.gesuchsperiode ?
                        this._statistikParameter.gesuchsperiode.toString() :
                        null)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.KINDER:
                this.reportAsyncRS.getKinderReportExcel(
                    this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.gesuchsperiode ?
                        this._statistikParameter.gesuchsperiode.toString() :
                        null)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.GESUCHSTELLER:
                this.reportAsyncRS.getGesuchstellerReportExcel(stichtag)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.KANTON:
                this.reportAsyncRS.getKantonReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT))
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.MITARBEITERINNEN:
                this.reportAsyncRS.getMitarbeiterinnenReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT))
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.BENUTZER:
                this.reportAsyncRS.getBenutzerReportExcel()
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.GESUCHSTELLER_KINDER_BETREUUNG:
                this.reportAsyncRS.getGesuchstellerKinderBetreuungReportExcel(
                    this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                    this._statistikParameter.gesuchsperiode ?
                        this._statistikParameter.gesuchsperiode.toString() :
                        null)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    })
                    .catch(() => {
                        this.$log.error('An error occurred downloading the document, closing download window.');
                    });
                return;
            case TSStatistikParameterType.ZAHLUNGEN_PERIODE:
                if (this._statistikParameter.gesuchsperiode) {
                    this.reportAsyncRS.getZahlungPeriodeReportExcel(
                        this._statistikParameter.gesuchsperiode)
                        .then((batchExecutionId: string) => {
                            this.$log.debug('executionID: ' + batchExecutionId);
                            const startmsg = this.$translate.instant('STARTED_GENERATION');
                            this.errorService.addMesageAsInfo(startmsg);
                        });
                } else {
                    this.$log.warn('gesuchsperiode muss gewählt sein');
                }
                return;
            case TSStatistikParameterType.MASSENVERSAND:
                if (this.statistikParameter.text) {
                    this.dvDialog.showRemoveDialog(removeDialogTemplate, undefined, RemoveDialogController, {
                        title: this.$translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_TITLE'),
                        deleteText: this.$translate.instant('MASSENVERSAND_ERSTELLEN_CONFIRM_INFO'),
                        parentController: undefined,
                        elementID: undefined
                    }).then(() => { // User confirmed removal
                        this.createMassenversand();
                    });
                } else {
                    this.createMassenversand();
                }
                return;
            case TSStatistikParameterType.INSTITUTIONEN:
                this.reportAsyncRS.getInstitutionenReportExcel()
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                return;
            case TSStatistikParameterType.VERRECHNUNG_KIBON:
                this.reportAsyncRS.getVerrechnungKibonReportExcel(
                    this._statistikParameter.doSave, this._statistikParameter.betragProKind)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.LASTENAUSGLEICH_KIBON:
                this.reportAsyncRS.getLastenausgleichKibonReportExcel(this._statistikParameter.jahr)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.TAGESSCHULE_ANMELDUNGEN:
                this.reportAsyncRS.getTagesschuleAnmeldungenReportExcel(
                    this._statistikParameter.tagesschuleAnmeldungen.id,
                    this._statistikParameter.gesuchsperiode)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.TAGESSCHULE_RECHNUNGSSTELLUNG:
                this.reportAsyncRS.getTagesschuleRechnungsstellungReportExcel()
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            case TSStatistikParameterType.NOTRECHT:
                this.reportAsyncRS.getNotrechtReportExcel(
                    this._statistikParameter.doSave)
                    .then((batchExecutionId: string) => {
                        this.informReportGenerationStarted(batchExecutionId);
                    });
                break;
            default:
                throw new Error(`unknown TSStatistikParameterType: ${type}`);
        }
    }

    private createMassenversand(): void {
        this.$log.info('Erstelle Massenversand');
        this.reportAsyncRS.getMassenversandReportExcel(
            this._statistikParameter.von ? this._statistikParameter.von.format(this.DATE_PARAM_FORMAT) : null,
            this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
            this._statistikParameter.gesuchsperiode.toString(),
            this._statistikParameter.bgGesuche,
            this._statistikParameter.mischGesuche,
            this._statistikParameter.tsGesuche,
            this._statistikParameter.ohneFolgegesuche,
            this._statistikParameter.text)
            .then((batchExecutionId: string) => {
                this.informReportGenerationStarted(batchExecutionId);
            })
            .catch(() => {
                this.$log.error('An error occurred downloading the document, closing download window.');
            });
    }

    private informReportGenerationStarted(batchExecutionId: string): void {
        this.$log.debug('executionID: ' + batchExecutionId);
        const startmsg = this.$translate.instant('STARTED_GENERATION');
        this.errorService.addMesageAsInfo(startmsg);
        this.refreshUserJobs();
    }

    public rowClicked(row: TSWorkJob): void {
        if (EbeguUtil.isNullOrUndefined(row) || EbeguUtil.isNullOrUndefined(row.execution)) {
            return;
        }

        if (EbeguUtil.isNullOrUndefined(row.execution.batchStatus) || row.execution.batchStatus !== 'COMPLETED') {
            this.$log.info('batch-job is not yet finnished');
            return;
        }

        const win = this.downloadRS.prepareDownloadWindow();
        this.$log.debug('accessToken: ' + row.resultData);
        this.downloadRS.startDownload(row.resultData, 'report.xlsx', false, win);
    }

    /**
     * helper methode die es dem Admin erlaubt alle jobs zu sehen
     */
    public showAllJobs(): void {
        this.batchJobRS.getAllJobs().then((result: TSWorkJob[]) => {
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
}

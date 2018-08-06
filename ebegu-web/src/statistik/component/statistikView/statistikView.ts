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

import {IComponentOptions, IIntervalService} from 'angular';
import {StateService} from '@uirouter/core';
import TSStatistikParameter from '../../../models/TSStatistikParameter';
import {TSStatistikParameterType} from '../../../models/enums/TSStatistikParameterType';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import GesuchsperiodeRS from '../../../core/service/gesuchsperiodeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DownloadRS} from '../../../core/service/downloadRS.rest';
import * as moment from 'moment';
import DateUtil from '../../../utils/DateUtil';
import {ReportAsyncRS} from '../../../core/service/reportAsyncRS.rest';
import ErrorService from '../../../core/errors/service/ErrorService';
import BatchJobRS from '../../../core/service/batchRS.rest';
import TSWorkJob from '../../../models/TSWorkJob';
import TSBatchJobInformation from '../../../models/TSBatchJobInformation';
import IFormController = angular.IFormController;
import ILogService = angular.ILogService;
import Moment = moment.Moment;
import ITranslateService = angular.translate.ITranslateService;

const template = require('./statistikView.html');
require('./statistikView.less');

export class StatistikViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = StatistikViewController;
    controllerAs = 'vm';
}

export class StatistikViewController {

    get statistikParameter(): TSStatistikParameter {
        return this._statistikParameter;
    }

    get gesuchsperioden(): Array<TSGesuchsperiode> {
        return this._gesuchsperioden;
    }

    static $inject: string[] = ['$state', 'GesuchsperiodeRS', '$log', 'ReportAsyncRS', 'DownloadRS', 'BatchJobRS',
        'ErrorService', '$translate', '$interval'];
    private polling: angular.IPromise<any>;
    private _statistikParameter: TSStatistikParameter;
    private _gesuchsperioden: Array<TSGesuchsperiode>;
    TSRole: any;
    TSRoleUtil: any;
    private readonly DATE_PARAM_FORMAT: string = 'YYYY-MM-DD';
    // Statistiken sind nur moeglich ab Beginn der fruehesten Periode bis Ende der letzten Periode
    private maxDate: Moment;
    private minDate: Moment;
    private userjobs: Array<TSWorkJob>;
    private allJobs: Array<TSBatchJobInformation>;

    constructor(private readonly $state: StateService, private readonly gesuchsperiodeRS: GesuchsperiodeRS, private readonly $log: ILogService,
        private readonly reportAsyncRS: ReportAsyncRS, private readonly downloadRS: DownloadRS, private readonly bachJobRS: BatchJobRS, private readonly errorService: ErrorService,
        private readonly $translate: ITranslateService, private readonly $interval: IIntervalService) {
    }

    $onInit() {
        this._statistikParameter = new TSStatistikParameter();
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: any) => {
            this._gesuchsperioden = response;
            if (this._gesuchsperioden.length > 0) {
                this.maxDate = this._gesuchsperioden[0].gueltigkeit.gueltigBis;
                this.minDate = DateUtil.localDateToMoment('2017-01-01');
            }
        });
        this.TSRole = TSRole;
        this.TSRoleUtil = TSRoleUtil;

        this.refreshUserJobs();
        this.initBatchJobPolling();
    }

    $onDestroy() {
        if (this.polling) {
            this.$interval.cancel(this.polling);
            this.$log.debug('canceld job polling');
        }
    }

    private initBatchJobPolling() {
        //check all 8 seconds for the state
        this.polling = this.$interval(() => this.refreshUserJobs(), 12000);

    }

    private refreshUserJobs() {
        this.bachJobRS.getBatchJobsOfUser().then((response: TSWorkJob[]) => {
            this.userjobs = response;
        });
    }

    public generateStatistik(form: IFormController, type?: TSStatistikParameterType): void {
        if (form.$valid) {
            const tmpType = (<any>TSStatistikParameterType)[type];
            tmpType ? this.$log.debug('Statistik Type: ' + tmpType) : this.$log.debug('default, Type not recognized');
            this.$log.debug('Validated Form: ' + form.$name);

            switch (tmpType) {
                case TSStatistikParameterType.GESUCH_STICHTAG: {
                    this.reportAsyncRS.getGesuchStichtagReportExcel(this._statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.gesuchsperiode ? this._statistikParameter.gesuchsperiode.toString() : null)
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.GESUCH_ZEITRAUM: {
                    this.reportAsyncRS.getGesuchZeitraumReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.gesuchsperiode ? this._statistikParameter.gesuchsperiode.toString() : null)
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.KINDER: {
                    this.reportAsyncRS.getKinderReportExcel(
                        this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.gesuchsperiode ? this._statistikParameter.gesuchsperiode.toString() : null)
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.GESUCHSTELLER: {
                    this.reportAsyncRS.getGesuchstellerReportExcel(this._statistikParameter.stichtag.format(this.DATE_PARAM_FORMAT))
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.KANTON: {
                    this.reportAsyncRS.getKantonReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT))
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.MITARBEITERINNEN: {
                    this.reportAsyncRS.getMitarbeiterinnenReportExcel(this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT))
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.BENUTZER: {
                    this.reportAsyncRS.getBenutzerReportExcel()
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.GESUCHSTELLER_KINDER_BETREUUNG: {
                    this.reportAsyncRS.getGesuchstellerKinderBetreuungReportExcel(
                        this._statistikParameter.von.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.bis.format(this.DATE_PARAM_FORMAT),
                        this._statistikParameter.gesuchsperiode ? this._statistikParameter.gesuchsperiode.toString() : null)
                        .then((batchExecutionId: string) => {
                            this.informReportGenerationStarted(batchExecutionId);
                        })
                        .catch(() => {
                            this.$log.error('An error occurred downloading the document, closing download window.');
                        });
                    break;
                }
                case TSStatistikParameterType.ZAHLUNGEN_PERIODE:
                    if (this._statistikParameter.gesuchsperiode) {
                        this.reportAsyncRS.getZahlungPeriodeReportExcel(
                            this._statistikParameter.gesuchsperiode)
                            .then((batchExecutionId: string) => {
                                this.$log.debug('executionID: ' + batchExecutionId);
                                const startmsg = this.$translate.instant('STARTED_GENERATION');
                                this.errorService.addMesageAsInfo(startmsg);
                            })
                            .catch(() => {
                                this.$log.error('An error occurred downloading the document, closing download window.');
                            });
                    } else {
                        this.$log.warn('gesuchsperiode muss gewählt sein');
                    }
                    break;
                default:
                    this.$log.debug('default, Type not recognized');
                    break;
            }
        }
    }

    private informReportGenerationStarted(batchExecutionId: string) {
        this.$log.debug('executionID: ' + batchExecutionId);
        const startmsg = this.$translate.instant('STARTED_GENERATION');
        this.errorService.addMesageAsInfo(startmsg);
        this.refreshUserJobs();
    }

    public rowClicked(row: TSWorkJob) {
        if (EbeguUtil.isNotNullOrUndefined(row) && EbeguUtil.isNotNullOrUndefined(row.execution)) {
            if (EbeguUtil.isNotNullOrUndefined(row.execution.batchStatus) && row.execution.batchStatus === 'COMPLETED') {
                const win: Window = this.downloadRS.prepareDownloadWindow();
                this.$log.debug('accessToken: ' + row.resultData);
                this.downloadRS.startDownload(row.resultData, 'report.xlsx', false, win);
            } else {
                this.$log.info('batch-job is not yet finnished');
            }
        }
    }

    /**
     * helper methode die es dem Admin erlaubt alle jobs zu sehen
     */
    public showAllJobs() {
        this.bachJobRS.getAllJobs().then((result: TSWorkJob[]) => {
            let res: TSBatchJobInformation[] = [];
            res = res.concat(result.map((value) => {
                return value.execution || undefined;
            }));
            this.allJobs = res;
        });
    }
}

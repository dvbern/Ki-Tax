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

import {IHttpParamSerializer, IHttpService, ILogService, IPromise} from 'angular';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class ReportRS {

    public static $inject = ['$httpParamSerializer', 'REST_API', '$log', 'EbeguRestUtil', '$http'];
    public serviceURL: string;
    public reportingTimeout: number = 240000;

    public constructor(
        public httpParamSerializer: IHttpParamSerializer,
        REST_API: string,
        public log: ILogService,
        public ebeguRestUtil: EbeguRestUtil,
        public http: IHttpService
    ) {
        this.serviceURL = `${REST_API}reporting`;
    }

    public getZahlungsauftragReportExcel(zahlungsauftragID: string): IPromise<TSDownloadFile> {

        const reportParams = this.httpParamSerializer({
            zahlungsauftragID
        });

        return this.http.get(`${this.serviceURL}/excel/zahlungsauftrag?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data));
    }

    public getZahlungReportExcel(zahlungID: string): IPromise<TSDownloadFile> {

        const reportParams = this.httpParamSerializer({
            zahlungID
        });

        return this.http.get(`${this.serviceURL}/excel/zahlung?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data));
    }
}

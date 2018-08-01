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
import EbeguRestUtil from '../../../utils/EbeguRestUtil';

/**
 * spricht die reportAsync resourec an welce die reports async generiert
 */
export class ReportAsyncRS {

    static $inject = ['$httpParamSerializer', 'REST_API', '$log', '$window', 'EbeguRestUtil', '$http'];
    serviceURL: string;
    reportingTimeout: number = 240000;

    constructor(public httpParamSerializer: IHttpParamSerializer, REST_API: string, public log: ILogService, private readonly $window: ng.IWindowService, public ebeguRestUtil: EbeguRestUtil,
                public http: IHttpService) {
        this.serviceURL = REST_API + 'reporting/async';
    }

    public getGesuchStichtagReportExcel(dateTimeStichtag: string, gesuchPeriodeID: string): IPromise<string> {

        const reportParams: string = this.httpParamSerializer({
            dateTimeStichtag: dateTimeStichtag,
            gesuchPeriodeID: gesuchPeriodeID
        });

        return this.http.get(this.serviceURL + '/excel/gesuchStichtag?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                this.log.debug('Queued Job returned ', response.data);
                return response.data;
            });

    }

    public getGesuchZeitraumReportExcel(dateTimeFrom: string, dateTimeTo: string, gesuchPeriodeID: string): IPromise<string> {

        const reportParams: string = this.httpParamSerializer({
            dateTimeFrom: dateTimeFrom,
            dateTimeTo: dateTimeTo,
            gesuchPeriodeID: gesuchPeriodeID
        });

        return this.http.get(this.serviceURL + '/excel/gesuchZeitraum?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getKantonReportExcel(auswertungVon: string, auswertungBis: string): IPromise<string> {

        const reportParams: string = this.httpParamSerializer({
            auswertungVon: auswertungVon,
            auswertungBis: auswertungBis
        });

        return this.http.get(this.serviceURL + '/excel/kanton?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getMitarbeiterinnenReportExcel(auswertungVon: string, auswertungBis: string): IPromise<string> {
        const reportParams: string = this.httpParamSerializer({
            auswertungVon: auswertungVon,
            auswertungBis: auswertungBis
        });
        return this.http.get(this.serviceURL + '/excel/mitarbeiterinnen?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungsauftragReportExcel(zahlungsauftragID: string): IPromise<string> {

        const reportParams: string = this.httpParamSerializer({
            zahlungsauftragID: zahlungsauftragID
        });

        return this.http.get(this.serviceURL + '/excel/zahlungsauftrag?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungReportExcel(zahlungID: string): IPromise<string> {

        const reportParams: string = this.httpParamSerializer({
            zahlungID: zahlungID
        });

        return this.http.get(this.serviceURL + '/excel/zahlung?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungPeriodeReportExcel(gesuchsperiode: string): IPromise<string> {
        const reportParams: string = this.httpParamSerializer({
            gesuchsperiodeID: gesuchsperiode
        });

        return this.http.get(this.serviceURL + '/excel/zahlungperiode?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });

    }

    public getGesuchstellerKinderBetreuungReportExcel(auswertungVon: string, auswertungBis: string, gesuchPeriodeID: string): IPromise<string> {
        const reportParams: string = this.httpParamSerializer({
            auswertungVon: auswertungVon,
            auswertungBis: auswertungBis,
            gesuchPeriodeID: gesuchPeriodeID
        });
        return this.http.get(this.serviceURL + '/excel/gesuchstellerkinderbetreuung?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getBenutzerReportExcel(): IPromise<string> {
        return this.http.get(this.serviceURL + '/excel/benutzer')
            .then((response: any) => {
                return response.data;
            });
    }

    public getKinderReportExcel(auswertungVon: string, auswertungBis: string, gesuchPeriodeID: string): IPromise<string> {
        const reportParams: string = this.httpParamSerializer({
            auswertungVon: auswertungVon,
            auswertungBis: auswertungBis,
            gesuchPeriodeID: gesuchPeriodeID
        });
        return this.http.get(this.serviceURL + '/excel/kinder?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getGesuchstellerReportExcel(stichtag: string): IPromise<string> {
        const reportParams: string = this.httpParamSerializer({
            stichtag: stichtag
        });
        return this.http.get(this.serviceURL + '/excel/gesuchsteller?' + reportParams, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getServiceName(): string {
        return 'ReportAsyncRS';
    }
}

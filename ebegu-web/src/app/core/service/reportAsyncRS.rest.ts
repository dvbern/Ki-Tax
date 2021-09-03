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
import {TSGemeinde} from '../../../models/TSGemeinde';

/**
 * spricht die reportAsync resourec an welce die reports async generiert
 */
export class ReportAsyncRS {

    public static $inject = ['$httpParamSerializer', 'REST_API', '$log', '$http'];
    public serviceURL: string;
    public reportingTimeout: number = 240000;

    public constructor(
        public httpParamSerializer: IHttpParamSerializer,
        REST_API: string,
        public log: ILogService,
        public http: IHttpService,
    ) {
        this.serviceURL = `${REST_API}reporting/async`;
    }

    public getGesuchStichtagReportExcel(dateTimeStichtag: string, gesuchPeriodeID: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            dateTimeStichtag,
            gesuchPeriodeID,
        });
        return this.http.get(`${this.serviceURL}/excel/gesuchStichtag?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                this.log.debug('Queued Job returned ', response.data);
                return response.data;
            });

    }

    public getGesuchZeitraumReportExcel(
        dateTimeFrom: string,
        dateTimeTo: string,
        gesuchPeriodeID: string,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            dateTimeFrom,
            dateTimeTo,
            gesuchPeriodeID,
        });
        return this.http.get(`${this.serviceURL}/excel/gesuchZeitraum?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getKantonReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        kantonSelbstbehalt: number,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
            kantonSelbstbehalt,
        });
        return this.http.get(`${this.serviceURL}/excel/kanton?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getMitarbeiterinnenReportExcel(auswertungVon: string, auswertungBis: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
        });
        return this.http.get(`${this.serviceURL}/excel/mitarbeiterinnen?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungsauftragReportExcel(zahlungsauftragID: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            zahlungsauftragID,
        });
        return this.http.get(`${this.serviceURL}/excel/zahlungsauftrag?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungReportExcel(zahlungID: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            zahlungID,
        });
        return this.http.get(`${this.serviceURL}/excel/zahlung?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getZahlungPeriodeReportExcel(gesuchsperiode: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            gesuchsperiodeID: gesuchsperiode,
        });
        return this.http.get(`${this.serviceURL}/excel/zahlungperiode?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getGesuchstellerKinderBetreuungReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gesuchPeriodeID: string,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
        });
        return this.http.get(`${this.serviceURL}/excel/gesuchstellerkinderbetreuung?${reportParams}`,
            {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getBenutzerReportExcel(): IPromise<string> {
        return this.http.get(`${this.serviceURL}/excel/benutzer`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getKinderReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gesuchPeriodeID: string,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
        });
        return this.http.get(`${this.serviceURL}/excel/kinder?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getGesuchstellerReportExcel(stichtag: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            stichtag,
        });
        return this.http.get(`${this.serviceURL}/excel/gesuchsteller?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getMassenversandReportExcel(auswertungVon: string, auswertungBis: string, gesuchPeriodeID: string,
                                       inklBgGesuche: boolean, inklMischGesuche: boolean, inklTsGesuche: boolean,
                                       ohneErneuerungsgesuch: boolean, text: string,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
            inklBgGesuche,
            inklMischGesuche,
            inklTsGesuche,
            ohneErneuerungsgesuch,
            text,
        });
        return this.http.get(`${this.serviceURL}/excel/massenversand?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getInstitutionenReportExcel(): IPromise<string> {
        return this.http.get(`${this.serviceURL}/excel/institutionen`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getVerrechnungKibonReportExcel(doSave: boolean, betragProKind: number): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            doSave,
            betragProKind,
        });
        return this.http.get(`${this.serviceURL}/excel/verrechnungkibon?${reportParams}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getLastenausgleichKibonReportExcel(year: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            year,
        });
        return this.http
            .get(`${this.serviceURL}/excel/lastenausgleich?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getTagesschuleAnmeldungenReportExcel(stammdatenId: string, gesuchsperiodeId: string): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            stammdatenId,
            gesuchsperiodeId,
        });
        return this.http
            .get(`${this.serviceURL}/excel/tagesschuleAnmeldungen?${reportParams}`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getTagesschuleRechnungsstellungReportExcel(): IPromise<string> {
        return this.http
            .get(`${this.serviceURL}/excel/tagesschuleRechnungsstellung`, {timeout: this.reportingTimeout})
            .then((response: any) => {
                return response.data;
            });
    }

    public getNotrechtReportExcel(zahlungenAusloesen: boolean): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            zahlungenAusloesen,
        });
        return this.http.get(`${this.serviceURL}/excel/notrecht?${reportParams}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getMahlzeitenverguenstigungReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gemeinde: TSGemeinde,
    ): IPromise<string> {
        const reportParams = this.httpParamSerializer({
            auswertungVon,
            auswertungBis,
            gemeindeId: gemeinde.id,
        });
        return this.http.get(`${this.serviceURL}/excel/mahlzeitenverguenstigung?${reportParams}`)
            .then((response: any) => {
                return response.data;
            });
    }

    public getServiceName(): string {
        return 'ReportAsyncRS';
    }
}

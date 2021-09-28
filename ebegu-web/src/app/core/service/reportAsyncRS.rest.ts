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

import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {CONSTANTS} from '../constants/CONSTANTS';

/**
 * spricht die reportAsync ressource an welche die reports async generiert
 */
@Injectable({
    providedIn: 'root',
})
export class ReportAsyncRS {

    private readonly serviceURL = `${CONSTANTS.REST_API}reporting/async`;

    public constructor(
        public http: HttpClient,
    ) {
    }

    private static createParamsFromObject(paramsObj: object): HttpParams {
        let params = new HttpParams();
        for (const [key, value] of Object.entries(paramsObj)) {
            if (!value) {
                continue;
            }
            params = params.append(key, value as string);
        }
        return params;
    }

    public getGesuchStichtagReportExcel(dateTimeStichtag: string, gesuchPeriodeID: string):
        Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            dateTimeStichtag,
            gesuchPeriodeID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/gesuchStichtag`,
            {params: reportParams}
        );

    }

    public getGesuchZeitraumReportExcel(
        dateTimeFrom: string,
        dateTimeTo: string,
        gesuchPeriodeID: string,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            dateTimeFrom,
            dateTimeTo,
            gesuchPeriodeID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/gesuchZeitraum`,
            {params: reportParams});
    }

    public getKantonReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        kantonSelbstbehalt: number,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
            kantonSelbstbehalt,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/kanton`, {params: reportParams});
    }

    public getMitarbeiterinnenReportExcel(auswertungVon: string, auswertungBis: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/mitarbeiterinnen`, {params: reportParams});
    }

    public getZahlungsauftragReportExcel(zahlungsauftragID: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            zahlungsauftragID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/zahlungsauftrag`, {params: reportParams});
    }

    public getZahlungReportExcel(zahlungID: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            zahlungID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/zahlung`, {params: reportParams});
    }

    public getZahlungPeriodeReportExcel(gesuchsperiode: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            gesuchsperiodeID: gesuchsperiode,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/zahlungperiode`, {params: reportParams});
    }

    public getGesuchstellerKinderBetreuungReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gesuchPeriodeID: string,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/gesuchstellerkinderbetreuung`, {params: reportParams});
    }

    public getBenutzerReportExcel(): Observable<{workjobId: string}> {
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/benutzer`);
    }

    public getKinderReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gesuchPeriodeID: string,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/kinder`, {params: reportParams});
    }

    public getGesuchstellerReportExcel(stichtag: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            stichtag,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/gesuchsteller`, {params: reportParams});
    }

    public getMassenversandReportExcel(auswertungVon: string, auswertungBis: string, gesuchPeriodeID: string,
                                       inklBgGesuche: boolean, inklMischGesuche: boolean, inklTsGesuche: boolean,
                                       ohneErneuerungsgesuch: boolean, text: string,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
            gesuchPeriodeID,
            inklBgGesuche,
            inklMischGesuche,
            inklTsGesuche,
            ohneErneuerungsgesuch,
            text,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/massenversand`, {params: reportParams});
    }

    public getInstitutionenReportExcel(): Observable<{workjobId: string}> {
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/institutionen`);
    }

    public getVerrechnungKibonReportExcel(doSave: boolean, betragProKind: number): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            doSave,
            betragProKind,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/verrechnungkibon`, {params: reportParams});
    }

    public getLastenausgleichKibonReportExcel(year: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            year,
        });
        return this.http
            .get<{workjobId: string}>(`${this.serviceURL}/excel/lastenausgleich`, {params: reportParams});
    }

    public getTagesschuleAnmeldungenReportExcel(stammdatenId: string, gesuchsperiodeId: string): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            stammdatenId,
            gesuchsperiodeId,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/tagesschuleAnmeldungen`, {params: reportParams});
    }

    public getTagesschuleRechnungsstellungReportExcel(): Observable<{workjobId: string}> {
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/tagesschuleRechnungsstellung`);
    }

    public getNotrechtReportExcel(zahlungenAusloesen: boolean): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            zahlungenAusloesen,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/notrecht`, {params: reportParams});
    }

    public getMahlzeitenverguenstigungReportExcel(
        auswertungVon: string,
        auswertungBis: string,
        gemeinde: TSGemeinde,
    ): Observable<{workjobId: string}> {
        const reportParams = ReportAsyncRS.createParamsFromObject({
            auswertungVon,
            auswertungBis,
            gemeindeId: gemeinde.id,
        });
        return this.http.get<{workjobId: string}>(`${this.serviceURL}/excel/mahlzeitenverguenstigung`, {params: reportParams});
    }
}

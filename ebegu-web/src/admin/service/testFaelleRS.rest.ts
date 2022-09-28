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

import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {CONSTANTS} from '../../app/core/constants/CONSTANTS';
import {TSGemeindeAntragTyp} from '../../models/enums/TSGemeindeAntragTyp';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {DateUtil} from '../../utils/DateUtil';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root',
})
export class TestFaelleRS {

    public serviceURL: string;
    public readonly ebeguRestUtil: EbeguRestUtil = new EbeguRestUtil();

    public constructor(
        public http: HttpClient
    ) {
        this.serviceURL = `${CONSTANTS.REST_API}testfaelle`;
    }

    public createTestFallGS(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
        username: string,
    ): Observable<string> {
        // TODO that is a strange API path. Configuration does not belong in a hierarchy. Use POST and move the
        // parameter to the method body
        // tslint:disable-next-line:max-line-length
        const url = `${this.serviceURL}/testfallgs/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}/${encodeURIComponent(
            username)}`;
        return this.http.get(url, {responseType: 'text'});
    }

    public removeFaelleOfGS(username: string): Observable<string> {
        return this.http.delete<string>(`${this.serviceURL}/testfallgs/${encodeURIComponent(username)}`);
    }

    public createTestFall(
        testFall: string,
        gesuchsperiodeId: string,
        gemeindeId: string,
        bestaetigt: boolean,
        verfuegen: boolean,
    ): Observable<string> {
        // tslint:disable-next-line:max-line-length
        const url = `${this.serviceURL}/testfall/${encodeURIComponent(testFall)}/${gesuchsperiodeId}/${gemeindeId}/${bestaetigt}/${verfuegen}`;

        return this.http.get(url, {responseType: 'text'});
    }

    public mutiereFallHeirat(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment,
    ): Observable<string> {
        return this.http.get<string>(`${this.serviceURL}/mutationHeirat/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`, {
            params: {
                mutationsdatum: DateUtil.momentToLocalDate(mutationsdatum),
                aenderungper: DateUtil.momentToLocalDate(aenderungper),
            },
        });
    }

    public testAllMails(mailadresse: string): Observable<void> {
        return this.http.get<void>(`${this.serviceURL}/mailtest/${mailadresse}`);
    }

    public mutiereFallScheidung(
        dossierid: string,
        gesuchsperiodeid: string,
        mutationsdatum: moment.Moment,
        aenderungper: moment.Moment,
    ): Observable<string> {
        const url = `${this.serviceURL}/mutationScheidung/${dossierid}/${encodeURIComponent(gesuchsperiodeid)}`;
        return this.http.get(url,
            {
                params: {
                    mutationsdatum: DateUtil.momentToLocalDate(mutationsdatum),
                    aenderungper: DateUtil.momentToLocalDate(aenderungper),
                },
                responseType: 'text'
            });
    }

    public resetSchulungsdaten(): Observable<string> {
        return this.http.get(`${this.serviceURL}/schulung/reset`, {responseType: 'text'});
    }

    public createSchulungsdaten(): Observable<string> {
        return this.http.get(`${this.serviceURL}/schulung/create`, {responseType: 'text'});
    }

    public deleteSchulungsdaten(): Observable<string> {
        return this.http.delete(`${this.serviceURL}/schulung/delete`, {responseType: 'text'});
    }

    public createTutorialdaten(): Observable<string> {
        return this.http.get(`${this.serviceURL}/schulung/tutorial/create`, {responseType: 'text'});
    }

    public getSchulungBenutzer(): Observable<string[]> {
        return this.http.get(`${this.serviceURL}/schulung/public/user`)
            .pipe(map((response: any) => {
                return response;
            }));
    }

    public processScript(scriptNr: string): Observable<any> {
        return this.http.get(`${this.serviceURL}/processscript/${scriptNr}`);
    }

    public createGemeindeAntragTestDaten(
        antragTyp: TSGemeindeAntragTyp,
        gesuchsperiode: TSGesuchsperiode,
        gemeinde: TSGemeinde,
        status: string,
    ): Observable<string> {
        return this.http.post(`${this.serviceURL}/gemeinde-antraege/${antragTyp}`,
            {
                gesuchsperiode: this.ebeguRestUtil.gesuchsperiodeToRestObject({}, gesuchsperiode),
                gemeinde: this.ebeguRestUtil.gemeindeToRestObject({}, gemeinde),
                status,
            },
            {responseType: 'text'});
    }
}

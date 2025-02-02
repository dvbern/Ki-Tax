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
import {map} from 'rxjs/operators';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../models/TSLastenausgleich';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root'
})
export class LastenausgleichRS {
    private readonly API_BASE_URL = `${CONSTANTS.REST_API}lastenausgleich`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(public http: HttpClient) {}

    public getAllLastenausgleiche(): Observable<TSLastenausgleich[]> {
        return this.http
            .get(`${this.API_BASE_URL}/all`)
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseLastenausgleichList(response)
                )
            );
    }

    public createLastenausgleich(
        jahr: number,
        selbstbehaltPro100ProzentPlatz: number
    ): Observable<TSLastenausgleich> {
        let params = new HttpParams();
        params = params.append('jahr', jahr.toFixed(0));

        if (EbeguUtil.isNotNullOrUndefined(selbstbehaltPro100ProzentPlatz)) {
            params = params.append(
                'selbstbehaltPro100ProzentPlatz',
                selbstbehaltPro100ProzentPlatz.toFixed(0)
            );
        }

        return this.http
            .get(`${this.API_BASE_URL}/create`, {
                params
            })
            .pipe(
                map((httpresponse: any) =>
                    this.ebeguRestUtil.parseLastenausgleich(
                        new TSLastenausgleich(),
                        httpresponse
                    )
                )
            );
    }

    public getLastenausgleichReportExcel(
        lastenausgleichId: string
    ): Observable<TSDownloadFile> {
        let params = new HttpParams();
        params = params.append('lastenausgleichId', lastenausgleichId);

        return this.http
            .get(`${this.API_BASE_URL}/excel`, {
                params
            })
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseDownloadFile(
                        new TSDownloadFile(),
                        response
                    )
                )
            );
    }

    public getLastenausgleichReportCSV(
        lastenausgleichId: string
    ): Observable<TSDownloadFile> {
        let params = new HttpParams();
        params = params.append('lastenausgleichId', lastenausgleichId);

        return this.http
            .get(`${this.API_BASE_URL}/csv`, {
                params
            })
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseDownloadFile(
                        new TSDownloadFile(),
                        response
                    )
                )
            );
    }

    public getZemisExcel(jahr: number): Observable<TSDownloadFile> {
        let params = new HttpParams();
        params = params.append('jahr', jahr.toFixed(0));

        return this.http
            .get(`${this.API_BASE_URL}/zemisexcel`, {
                params
            })
            .pipe(
                map((response: any) =>
                    this.ebeguRestUtil.parseDownloadFile(
                        new TSDownloadFile(),
                        response
                    )
                )
            );
    }

    public removeLastenausgleich(lastenausgleichId: string): Observable<any> {
        return this.http.delete(
            `${this.API_BASE_URL}/${encodeURIComponent(lastenausgleichId)}`
        );
    }
}

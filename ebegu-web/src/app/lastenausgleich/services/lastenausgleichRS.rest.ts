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
import {tap} from 'rxjs/operators';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../models/TSLastenausgleich';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Injectable({
    providedIn: 'root'
})
export class LastenausgleichRS {

    private readonly API_BASE_URL = `${CONSTANTS.REST_API}lastenausgleich`;
    private readonly ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public http: HttpClient,
    ) {}

    public getAllLastenausgleiche(): Observable<TSLastenausgleich[]> {
        return this.http.get(`${this.API_BASE_URL}/all`)
            .pipe(tap((response: any) => {
                return this.ebeguRestUtil.parseLastenausgleichList(response.data);
            }));
    }

    public createLastenausgleich(
        jahr: number,
        selbstbehaltPro100ProzentPlatz: number,
    ): Observable<TSLastenausgleich> {

        let params = new HttpParams();
        params = params.append('jahr', jahr.toFixed(0));
        params = params.append('selbstbehaltPro100ProzentPlatz', selbstbehaltPro100ProzentPlatz.toFixed(0));

        return this.http.get(`${this.API_BASE_URL}/create`,
            {
                params,
            }).pipe(tap((httpresponse: any) => {
            return this.ebeguRestUtil.parseLastenausgleich(new TSLastenausgleich(), httpresponse.data);
        }));
    }

    public getLastenausgleichReportExcel(lastenausgleichId: string): Observable<TSDownloadFile> {

        let params = new HttpParams();
        params = params.append('lastenausgleichId', lastenausgleichId);

        return this.http.get(`${this.API_BASE_URL}/excel`,
            {
                params
            }).pipe(tap((response: any) => {
            return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
        }));
    }

    public getLastenausgleichReportCSV(lastenausgleichId: string): Observable<TSDownloadFile> {

        let params = new HttpParams();
        params = params.append('lastenausgleichId', lastenausgleichId);

        return this.http.get(`${this.API_BASE_URL}/csv`,
            {
                params
            }).pipe(tap((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
        }));
    }

    public getZemisExcel(jahr: number): Observable<TSDownloadFile> {

        let params = new HttpParams();
        params = params.append('jahr', jahr.toFixed(0));

        return this.http.get(`${this.API_BASE_URL}/zemisexcel`,
            {
                params
            }).pipe(tap((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
        }));
    }

    public removeLastenausgleich(lastenausgleichId: string): Observable<any> {
        return this.http.delete(`${this.API_BASE_URL}/${encodeURIComponent(lastenausgleichId)}`)
            .pipe(tap(value => {
                return value.data;
            }));
    }
}

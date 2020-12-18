/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {IHttpService, IPromise} from 'angular';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSLastenausgleich} from '../../../models/TSLastenausgleich';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class LastenausgleichRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        public ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}lastenausgleich`;
    }

    public getServiceName(): string {
        return 'LastenausgleichRS';
    }

    public getAllLastenausgleiche(): IPromise<TSLastenausgleich[]> {
        return this.http.get(`${this.serviceURL}/all`).then((response: any) => {
            return this.ebeguRestUtil.parseLastenausgleichList(response.data);
        });
    }

    public createLastenausgleich(
        jahr: number,
        selbstbehaltPro100ProzentPlatz: number,
    ): IPromise<TSLastenausgleich> {
        return this.http.get(`${this.serviceURL}/create`,
            {
                params: {
                    jahr,
                    selbstbehaltPro100ProzentPlatz,
                },
            }).then((httpresponse: any) => {
            return this.ebeguRestUtil.parseLastenausgleich(new TSLastenausgleich(), httpresponse.data);
        });
    }

    public getLastenausgleichReportExcel(lastenausgleichId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/excel`,
            {
                params: {
                    lastenausgleichId,
                }})
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public getLastenausgleichReportCSV(lastenausgleichId: string): IPromise<TSDownloadFile> {
        return this.http.get(`${this.serviceURL}/csv`,
            {
                params: {
                    lastenausgleichId,
                }})
            .then((response: any) => {
                return this.ebeguRestUtil.parseDownloadFile(new TSDownloadFile(), response.data);
            });
    }

    public removeLastenausgleich(lastenausgleichId: string): IPromise<any> {
        return this.http.delete(`${this.serviceURL}/${encodeURIComponent(lastenausgleichId)}`)
            .then(value => {
                return value.data;
            });
    }
}

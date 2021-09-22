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
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSWorkJob} from '../../../models/TSWorkJob';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {CONSTANTS} from '../constants/CONSTANTS';

/**
 * liest information ueber batch jobs aus
 */
@Injectable({
    providedIn: 'root',
})
export class BatchJobRS {

    public serviceURL = `${CONSTANTS.REST_API}admin/batch`;
    private ebeguRestUtil = new EbeguRestUtil();

    public constructor(
        public http: HttpClient,
    ) {
    }

    public getAllJobs(): Observable<TSWorkJob[]> {
        return this.getInfo(`${this.serviceURL}/jobs`);
    }

    public getBatchJobsOfUser(): Observable<TSWorkJob[]> {
        return this.getInfo(`${this.serviceURL}/userjobs/notokenrefresh`);
    }

    private getInfo(url: string): Observable<Array<TSWorkJob> | never> {
        return this.http.get(url)
            .pipe(map((response: any) => {
                return this.ebeguRestUtil.parseWorkJobList(response.data);
            }));
    }
}

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

import {IHttpService, IPromise} from 'angular';
import {TSWorkJob} from '../../../models/TSWorkJob';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

/**
 * liest information ueber batch jobs aus
 */
export class BatchJobRS {
    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];

    public serviceURL: string;

    public constructor(
        public http: IHttpService,
        REST_API: string,
        private readonly ebeguRestUtil: EbeguRestUtil,
    ) {
        this.serviceURL = `${REST_API}admin/batch`;
    }

    public getAllJobs(): IPromise<TSWorkJob[]> {
        return this.getInfo(`${this.serviceURL}/jobs`);
    }

    public getBatchJobsOfUser(): IPromise<TSWorkJob[]> {
        return this.getInfo(`${this.serviceURL}/userjobs/notokenrefresh`);
    }

    private getInfo(url: string): IPromise<Array<TSWorkJob> | never> {
        return this.http.get(url).then((response: any) => {
            return this.ebeguRestUtil.parseWorkJobList(response.data);
        });
    }
}

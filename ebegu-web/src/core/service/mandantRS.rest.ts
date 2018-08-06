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

import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {IHttpService, ILogService, IPromise} from 'angular';
import {TSMandant} from '../../models/TSMandant';

export class MandantRS {

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log'];
    serviceURL: string;
    http: IHttpService;
    ebeguRestUtil: EbeguRestUtil;
    log: ILogService;
    /* @ngInject */
    constructor($http: IHttpService, REST_API: string, ebeguRestUtil: EbeguRestUtil, $log: ILogService) {
        this.serviceURL = REST_API + 'mandanten';
        this.http = $http;
        this.ebeguRestUtil = ebeguRestUtil;
        this.log = $log;
    }

    public findMandant(mandantID: string): IPromise<TSMandant> {
        return this.http.get(this.serviceURL + '/id/' + encodeURIComponent(mandantID))
            .then((response: any) => {
                this.log.debug('PARSING mandant REST object ', response.data);
                return this.ebeguRestUtil.parseMandant(new TSMandant(), response.data);
            });
    }

    /**
     * laedt und cached den ersten und einzigenMandanten aus der DB
     * @returns {IPromise<TSMandant>}
     */
    public getFirst(): IPromise<TSMandant> {
        return this.http.get(this.serviceURL + '/first', { cache: true })
            .then((response: any) => {
                this.log.debug('PARSING mandant REST object ', response.data);
                return this.ebeguRestUtil.parseMandant(new TSMandant(), response.data);
            });
    }

    public getServiceName(): string {
        return 'MandantRS';
    }

}

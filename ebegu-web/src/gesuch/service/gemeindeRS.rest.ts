/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import {IHttpService, ILogService, IPromise} from 'angular';
import {IEntityRS} from '../../core/service/iEntityRS.rest';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import TSGemeinde from '../../models/TSGemeinde';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import GlobalCacheService from './globalCacheService';

export default class GemeindeRS implements IEntityRS {
    serviceURL: string;
    http: IHttpService;
    ebeguRestUtil: EbeguRestUtil;

    allGemeindenCache: Array<TSGemeinde>;

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'GlobalCacheService'];
    /* @ngInject */
    constructor($http: IHttpService, REST_API: string, ebeguRestUtil: EbeguRestUtil, private $log: ILogService, private globalCacheService: GlobalCacheService) {
        this.serviceURL = REST_API + 'gemeinde';
        this.http = $http;
        this.ebeguRestUtil = ebeguRestUtil;
    }

    public getAllGemeinden(): IPromise<Array<TSGemeinde>> {
        let cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN);
        return this.http.get(this.serviceURL + '/all', {cache: cache})
            .then((response: any) => {
                this.$log.debug('PARSING gemeinde REST object ', response.data);
                return this.ebeguRestUtil.parseGemeindeList(response.data);
            });
    }
}

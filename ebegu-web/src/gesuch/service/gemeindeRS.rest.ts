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

import {IHttpService, ILogService, IPromise, IQService} from 'angular';
import {IEntityRS} from '../../app/core/service/iEntityRS.rest';
import {TSCacheTyp} from '../../models/enums/TSCacheTyp';
import TSGemeinde from '../../models/TSGemeinde';
import TSUser from '../../models/TSUser';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import GlobalCacheService from './globalCacheService';

export default class GemeindeRS implements IEntityRS {

    static $inject = ['$http', 'REST_API', 'EbeguRestUtil', '$log', 'GlobalCacheService', '$q'];
    serviceURL: string;

    constructor(public http: IHttpService, REST_API: string, public ebeguRestUtil: EbeguRestUtil, private readonly $log: ILogService,
                private readonly globalCacheService: GlobalCacheService, private readonly $q: IQService) {
        this.serviceURL = REST_API + 'gemeinde';
    }

    public getAllGemeinden(): IPromise<TSGemeinde[]> {
        const cache = this.globalCacheService.getCache(TSCacheTyp.EBEGU_GEMEINDEN);
        return this.http.get(this.serviceURL + '/all', {cache: cache})
            .then((response: any) => {
                this.$log.debug('PARSING gemeinde REST object ', response.data);
                return this.ebeguRestUtil.parseGemeindeList(response.data);
            });
    }

    public getGemeindenForPrincipal(user: TSUser): IPromise<Array<TSGemeinde>> {
        if (!user) {
            return this.$q.when([]); // empty list for unknown user
        }
        if (TSRoleUtil.isGemeindeabhaengig(user.getCurrentRole())) {
            return this.$q.when(angular.copy(user.extractCurrentGemeinden()));
        } else {
            return this.getAllGemeinden().then(response => {
                return response;
            });
        }
    }

    public findGemeinde(gemeindeId: string): IPromise<TSGemeinde> {
        return this.http.get(this.serviceURL + '/id/' + encodeURIComponent(gemeindeId))
            .then((response: any) => {
                this.$log.debug('PARSING gemeinde REST object ', response.data);
                return this.ebeguRestUtil.parseGemeinde(new TSGemeinde(), response.data);
            });
    }
}

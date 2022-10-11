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

import {IHttpResponse, IHttpService, IPromise} from 'angular';
import {TSQuickSearchResult} from '../../../models/dto/TSQuickSearchResult';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';

export class SearchIndexRS {

    public static $inject = ['$http', 'REST_API', 'EbeguRestUtil'];
    public serviceURL: string;

    public constructor(public http: IHttpService, REST_API: string, public ebeguRestUtil: EbeguRestUtil) {
        this.serviceURL = `${REST_API}search/`;
    }

    /**
     * performs a global search that will only return a certain ammount of results
     *
     * @param query searchstring
     */
    public quickSearch(query: string): IPromise<TSQuickSearchResult> {
        return this.search(`${this.serviceURL}quicksearch/${query}`);
    }

    /**
     * performs a global search that will return the full number of matched results
     *
     * @param query searchstring
     */
    public globalSearch(query: string): IPromise<TSQuickSearchResult> {
        return this.search(`${this.serviceURL}globalsearch/${query}`);
    }

    private search(url: string): IPromise<TSQuickSearchResult> {
        return this.http.get(url).then((response: IHttpResponse<TSQuickSearchResult>) => this.ebeguRestUtil.parseQuickSearchResult(response.data));
    }
}

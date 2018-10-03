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

import {IComponentOptions} from 'angular';
import TSQuickSearchResult from '../../../models/dto/TSQuickSearchResult';
import TSAbstractAntragDTO from '../../../models/TSAbstractAntragDTO';
import EbeguUtil from '../../../utils/EbeguUtil';
import {SearchIndexRS} from '../../core/service/searchIndexRS.rest';
import {ISearchResultateStateParams} from '../search.route';
import ILogService = angular.ILogService;

export class SearchListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./searchListView.html');
    public controller = SearchListViewController;
    public controllerAs = 'vm';
}

export class SearchListViewController {

    public static $inject: string[] = ['$log', '$stateParams', 'SearchIndexRS'];

    private antragList: Array<TSAbstractAntragDTO>;
    public totalResultCount: string = '-';
    // we want to ignore the first filter request because the default sort triggers always a second one
    private readonly ignoreRequest: boolean = true;
    public searchString: string;

    public constructor(private readonly $log: ILogService,
                       $stateParams: ISearchResultateStateParams,
                       private readonly searchIndexRS: SearchIndexRS,
    ) {
        this.searchString = $stateParams.searchString;
        this.initViewModel();

    }

    private initViewModel(): void {
        this.searchIndexRS.globalSearch(this.searchString).then((quickSearchResult: TSQuickSearchResult) => {
            this.antragList = [];
            for (const res of quickSearchResult.resultEntities) {
                this.antragList.push(res.antragDTO);
            }
            EbeguUtil.handleSmarttablesUpdateBug(this.antragList);
        }).catch(() => {
            this.$log.warn('error during globalSearch');
        });
    }

    public getSearchList(): Array<TSAbstractAntragDTO> {
        return this.antragList;
    }

}

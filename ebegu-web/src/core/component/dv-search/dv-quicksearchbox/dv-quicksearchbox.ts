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

import {IComponentOptions, IFilterService, ILogService, IPromise, IQService} from 'angular';
import TSSearchResultEntry from '../../../../models/dto/TSSearchResultEntry';
import TSQuickSearchResult from '../../../../models/dto/TSQuickSearchResult';
import {IStateService} from 'angular-ui-router';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {isAnyStatusOfVerfuegt} from '../../../../models/enums/TSAntragStatus';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {SearchIndexRS} from '../../../service/searchIndexRS.rest';
import TSAntragDTO from '../../../../models/TSAntragDTO';
import ITranslateService = angular.translate.ITranslateService;
import IInjectorService = angular.auto.IInjectorService;
let template = require('./dv-quicksearchbox.html');
require('./dv-quicksearchbox.less');


export class DvQuicksearchboxComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = DvQuicksearchboxController;
    controllerAs = 'vm';

}

export class DvQuicksearchboxController {

    noCache: boolean = true;
    delay: number = 250;

    selectedItem: TSSearchResultEntry;
    searchQuery: string;
    searchString: string;
    TSRoleUtil: TSRoleUtil;
    gesuchModelManager: GesuchModelManager;

    static $inject: any[] = ['EbeguUtil', '$timeout', '$log', '$q', 'SearchIndexRS', 'CONSTANTS', '$filter', '$translate',
        '$state', 'AuthServiceRS', '$injector'];
    /* @ngInject */
    constructor(private ebeguUtil: EbeguUtil, private $timeout: IFilterService, private $log: ILogService,
                private $q: IQService, private searchIndexRS: SearchIndexRS, private CONSTANTS: any,
                private $filter: IFilterService, private $translate: ITranslateService,
                private $state: IStateService, private authServiceRS: AuthServiceRS, private $injector: IInjectorService) {
        this.TSRoleUtil = TSRoleUtil;
    }

    //wird von angular aufgerufen
    $onInit() {
        this.selectedItem = undefined;
    }


    public querySearch(query: string): IPromise<Array<TSSearchResultEntry>> {
        this.searchString = query;
        let deferred = this.$q.defer<Array<TSSearchResultEntry>>();
        this.searchIndexRS.quickSearch(query).then((quickSearchResult: TSQuickSearchResult) => {
            this.limitResultsize(quickSearchResult);
            deferred.resolve(quickSearchResult.resultEntities);
        }).catch((ee) => {
            deferred.resolve([]);
            this.$log.warn('error during quicksearch', ee);
        });

        return deferred.promise;

    }

    private limitResultsize(quickSearchResult: TSQuickSearchResult) {

        let limitedResults = this.$filter('limitTo')(quickSearchResult.resultEntities, 8);
        // if (limitedResults.length < quickSearchResult.length) { //total immer anzeigen
        this.addFakeTotalResultEntry(quickSearchResult, limitedResults);
    }

    private addFakeTotalResultEntry(quickSearchResult: TSQuickSearchResult, limitedResults: TSSearchResultEntry[]) {
        if (angular.isArray(limitedResults) && limitedResults.length > 0) {
            let totalResEntry: TSSearchResultEntry = new TSSearchResultEntry();
            let alleFaelleEntry = new TSAntragDTO();
            alleFaelleEntry.familienName = this.$translate.instant('QUICKSEARCH_ALL_RESULTS', {totalNum: quickSearchResult.totalResultSize});
            totalResEntry.entity = 'ALL';
            totalResEntry.antragDTO = alleFaelleEntry;
            limitedResults.push(totalResEntry);
        }
        quickSearchResult.resultEntities = limitedResults;
    }

    private selectItemChanged() {
        this.navigateToFall();
        this.selectedItem = undefined;

    }

    private navigateToFall() {
        if (this.selectedItem) {
            if (this.selectedItem.antragDTO instanceof TSAntragDTO && this.selectedItem.gesuchID) {
                if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles()) && this.selectedItem.antragDTO) {
                    // Reload Gesuch in gesuchModelManager on Init in fallCreationView because  maybe it has been changed since last time
                    if (!this.gesuchModelManager) {
                        this.gesuchModelManager = this.$injector.get<GesuchModelManager>('GesuchModelManager');
                    }
                    this.gesuchModelManager.clearGesuch();
                    if (isAnyStatusOfVerfuegt(this.selectedItem.antragDTO.status)) {

                        this.openGesuch(this.selectedItem.gesuchID, 'gesuch.verfuegen');
                    } else {
                        this.openGesuch(this.selectedItem.gesuchID, 'gesuch.betreuungen');
                    }
                } else {
                    this.openGesuch(this.selectedItem.gesuchID, 'gesuch.fallcreation');
                }
            } else if (this.selectedItem.entity === 'FALL') {
                //open mitteilung
                this.$state.go('mitteilungen', {dossierId: this.selectedItem.dossierId});
            } else {

                this.$state.go('search', {searchString: this.searchString});
            }
        }
    }

    /**
     * Oeffnet das Gesuch und geht zur gegebenen Seite (route)
     * @param antragId
     * @param urlToGoTo
     * @param inNewTab true if fall should be opend in new tab
     */
    private openGesuch(antragId: string, urlToGoTo: string, inNewTab?: boolean): void {
        if (antragId) {
            if (inNewTab) {
                let url = this.$state.href(urlToGoTo, {createNew: false, gesuchId: antragId});
                window.open(url, '_blank');
            } else {
                this.$state.go(urlToGoTo, {createNew: false, gesuchId: antragId});
            }
        }
    }
}

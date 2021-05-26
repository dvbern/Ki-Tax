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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IFilterService, ILogService, IPromise, IQService} from 'angular';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {TSQuickSearchResult} from '../../../../../models/dto/TSQuickSearchResult';
import {TSSearchResultEntry} from '../../../../../models/dto/TSSearchResultEntry';
import {isAnyStatusOfVerfuegt, TSAntragStatus} from '../../../../../models/enums/TSAntragStatus';
import {TSAntragDTO} from '../../../../../models/TSAntragDTO';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {SearchIndexRS} from '../../../service/searchIndexRS.rest';
import IInjectorService = angular.auto.IInjectorService;
import ITranslateService = angular.translate.ITranslateService;

export class DvQuicksearchboxComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./dv-quicksearchbox.html');
    public controller = DvQuicksearchboxController;
    public controllerAs = 'vm';
}

export class DvQuicksearchboxController {

    public static $inject: ReadonlyArray<string> = [
        '$log',
        '$q',
        'SearchIndexRS',
        '$filter',
        '$translate',
        '$state',
        'AuthServiceRS',
        '$injector',
    ];

    public noCache: boolean = true;
    public delay: number = 250;

    public selectedItem: TSSearchResultEntry;
    public searchQuery: string;
    public searchString: string;
    public readonly TSRoleUtil = TSRoleUtil;
    public gesuchModelManager: GesuchModelManager;

    public constructor(
        private readonly $log: ILogService,
        private readonly $q: IQService,
        private readonly searchIndexRS: SearchIndexRS,
        private readonly $filter: IFilterService,
        private readonly $translate: ITranslateService,
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $injector: IInjectorService,
    ) {
    }

    // wird von angular aufgerufen
    public $onInit(): void {
        this.selectedItem = undefined;
    }

    public querySearch(query: string): IPromise<Array<TSSearchResultEntry>> {
        this.searchString = query;
        const deferred = this.$q.defer<Array<TSSearchResultEntry>>();
        this.searchIndexRS.quickSearch(query).then((quickSearchResult: TSQuickSearchResult) => {
            this.limitResultsize(quickSearchResult);
            deferred.resolve(quickSearchResult.resultEntities);
        }).catch(ee => {
            deferred.resolve([]);
            this.$log.warn('error during quicksearch', ee);
        });

        return deferred.promise;

    }

    private limitResultsize(quickSearchResult: TSQuickSearchResult): void {

        const limitedResults = this.$filter('limitTo')(quickSearchResult.resultEntities, 8);
        // if (limitedResults.length < quickSearchResult.length) { //total immer anzeigen
        this.addFakeTotalResultEntry(quickSearchResult, limitedResults);
    }

    private addFakeTotalResultEntry(
        quickSearchResult: TSQuickSearchResult,
        limitedResults: TSSearchResultEntry[],
    ): void {
        if (angular.isArray(limitedResults) && limitedResults.length > 0) {
            const totalResEntry = new TSSearchResultEntry();
            const alleFaelleEntry = new TSAntragDTO();
            alleFaelleEntry.familienName =
                this.$translate.instant('QUICKSEARCH_ALL_RESULTS',
                    {totalNum: quickSearchResult.resultEntities.length});
            totalResEntry.entity = 'ALL';
            totalResEntry.antragDTO = alleFaelleEntry;
            limitedResults.push(totalResEntry);
        }
        quickSearchResult.resultEntities = limitedResults;
    }

    public selectItemChanged(): void {
        this.navigateToFall();
        this.selectedItem = undefined;

    }

    // tslint:disable-next-line:cognitive-complexity
    private navigateToFall(): void {
        if (!this.selectedItem) {
            return;
        }

        if (this.selectedItem.antragDTO instanceof TSAntragDTO && this.selectedItem.gesuchID) {
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionRoles()) && this.selectedItem.antragDTO) {
                // Reload Gesuch in gesuchModelManager on Init in fallCreationView because  maybe it has been
                // changed since last time
                if (!this.gesuchModelManager) {
                    this.gesuchModelManager = this.$injector.get<GesuchModelManager>('GesuchModelManager');
                }
                this.gesuchModelManager.clearGesuch();
                if (isAnyStatusOfVerfuegt(this.selectedItem.antragDTO.status)) {

                    this.openGesuch(this.selectedItem.antragDTO, 'gesuch.verfuegen');
                } else {
                    this.openGesuch(this.selectedItem.antragDTO, 'gesuch.betreuungen');
                }
            } else if (this.selectedItem.antragDTO.status === TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST) {
                this.openGesuch(this.selectedItem.antragDTO, 'gesuch.sozialdienstfallcreation');
            } else {
                this.openGesuch(this.selectedItem.antragDTO, 'gesuch.fallcreation');
            }
        } else if (this.selectedItem.entity === 'DOSSIER') {
            // open mitteilung
            this.$state.go('mitteilungen.view', {
                dossierId: this.selectedItem.dossierId,
                fallId: this.selectedItem.fallID,
            });
        } else {
            this.$state.go('search.list-view', {searchString: this.searchString});
        }
    }

    /**
     * Oeffnet das Gesuch und geht zur gegebenen Seite (route)
     */
    private openGesuch(antrag: TSAntragDTO, urlToGoTo: string, inNewTab: boolean = false): void {
        if (!antrag) {
            return;
        }
        const navObj: any = {
            gesuchId: antrag.antragId,
            dossierId: antrag.dossierId,
            fallId: antrag.fallId,
            gemeindeId: antrag.gemeindeId,
        };
        if (inNewTab) {
            const url = this.$state.href(urlToGoTo, navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go(urlToGoTo, navObj);
        }
    }
}

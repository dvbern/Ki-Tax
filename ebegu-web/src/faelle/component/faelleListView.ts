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

import {IComponentOptions, IFilterService} from 'angular';
import {IStateService} from 'angular-ui-router';
import GesuchModelManager from '../../gesuch/service/gesuchModelManager';
import TSAntragDTO from '../../models/TSAntragDTO';
import TSAntragSearchresultDTO from '../../models/TSAntragSearchresultDTO';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {isAnyStatusOfVerfuegt} from '../../models/enums/TSAntragStatus';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import IPromise = angular.IPromise;
import ILogService = angular.ILogService;
import SearchRS from '../../gesuch/service/searchRS.rest';
let template = require('./faelleListView.html');
require('./faelleListView.less');

export class FaelleListViewComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = FaelleListViewController;
    controllerAs = 'vm';
}

export class FaelleListViewController {

    private antragList: Array<TSAntragDTO>;
    totalResultCount: string = '0';


    static $inject: string[] = ['$filter', 'GesuchModelManager', '$state', '$log', 'AuthServiceRS', 'SearchRS'];

    constructor(private $filter: IFilterService, private gesuchModelManager: GesuchModelManager,
                private $state: IStateService, private $log: ILogService,
                private authServiceRS: AuthServiceRS, private searchRS: SearchRS) {
        this.initViewModel();
    }

    private initViewModel() {
        // this.updateAntragList();

    }


    public passFilterToServer = (tableFilterState: any): IPromise<TSAntragSearchresultDTO> => {
        this.$log.debug('Triggering ServerFiltering with Filter Object', tableFilterState);
        return this.searchRS.searchAntraege(tableFilterState).then((response: TSAntragSearchresultDTO) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize.toString() : '0';
            this.antragList = response.antragDTOs;
            return response;
        });

    }

    public getAntragList(): Array<TSAntragDTO> {
        return this.antragList;
    }

    /**
     * Fuer Benutzer mit der Rolle SACHBEARBEITER_INSTITUTION oder SACHBEARBEITER_TRAEGERSCHAFT oeffnet es das Gesuch mit beschraenkten Daten
     * Fuer anderen Benutzer wird das Gesuch mit allen Daten geoeffnet
     * @param antrag
     * @param event optinally this function can check if ctrl was clicked when opeing
     */
    public editFall(antrag: TSAntragDTO, event: any): void {
        if (antrag) {
            let isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
                // Reload Gesuch in gesuchModelManager on Init in fallCreationView because it has been changed since last time
                this.gesuchModelManager.clearGesuch();
                if (isAnyStatusOfVerfuegt(antrag.status)) {
                    this.openGesuch(antrag.antragId, 'gesuch.verfuegen', isCtrlKeyPressed);
                } else {
                    this.openGesuch(antrag.antragId, 'gesuch.betreuungen', isCtrlKeyPressed);
                }
            } else {
                this.openGesuch(antrag.antragId, 'gesuch.fallcreation', isCtrlKeyPressed);
            }
        }
    }

    /**
     * Oeffnet das Gesuch und geht zur gegebenen Seite (route)
     * @param antragId
     * @param urlToGoTo
     * @param isCtrlKeyPressed true if user pressed ctrl when clicking
     */
    private openGesuch(antragId: string, urlToGoTo: string, isCtrlKeyPressed: boolean): void {
        if (antragId) {
            if (isCtrlKeyPressed) {
                let url = this.$state.href(urlToGoTo, {createNew: false, gesuchId: antragId});
                window.open(url, '_blank');
            } else {
                this.$state.go(urlToGoTo, {createNew: false, gesuchId: antragId});
            }
        }
    }
}

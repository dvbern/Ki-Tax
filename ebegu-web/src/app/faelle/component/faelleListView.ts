/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IComponentOptions} from 'angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../gesuch/service/gesuchModelManager';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {isAnyStatusOfVerfuegt, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class FaelleListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./faelleListView.html');
    public controller = FaelleListViewController;
    public controllerAs = 'vm';
}

export class FaelleListViewController {

    public static $inject: string[] = ['GesuchModelManager', '$state', '$log', 'AuthServiceRS', 'SearchRS'];

    private antragList: Array<TSAntragDTO>;
    public totalResultCount: string = '0';

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $state: StateService,
        private readonly $log: ILogService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly searchRS: SearchRS,
    ) {
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
     * Fuer Benutzer mit der Rolle SACHBEARBEITER_INSTITUTION oder SACHBEARBEITER_TRAEGERSCHAFT oeffnet es das Gesuch
     * mit beschraenkten Daten Fuer anderen Benutzer wird das Gesuch mit allen Daten geoeffnet
     * @param antrag der antrag
     * @param event optinally this function can check if ctrl was clicked when opeing
     */
    public editFall(antrag: TSAntragDTO, event: any): void {
        if (!antrag) {
            return;
        }

        const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())) {
            // Reload Gesuch in gesuchModelManager on Init in fallCreationView because it has been changed since
            // last time
            this.gesuchModelManager.clearGesuch();
            if (isAnyStatusOfVerfuegt(antrag.status)) {
                this.openGesuch(antrag, 'gesuch.verfuegen',
                    {gesuchId: antrag.antragId},
                    isCtrlKeyPressed);
            } else {
                this.openGesuch(antrag, 'gesuch.betreuungen',
                    {gesuchId: antrag.antragId},
                    isCtrlKeyPressed);
            }
        } else if (antrag.status === TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST) {
                const navObj: any = {
                    gesuchId: antrag.antragId,
                    dossierId: antrag.dossierId,
                    fallId: antrag.fallId,
                    gemeindeId: antrag.gemeindeId,
                };
                this.openGesuch(antrag, 'gesuch.sozialdienstfallcreation', navObj, isCtrlKeyPressed);
            } else {
            this.openGesuch(antrag, 'gesuch.fallcreation',
                {gesuchId: antrag.antragId, dossierId: antrag.dossierId},
                isCtrlKeyPressed);
        }
    }

    /**
     * Oeffnet das Gesuch und geht zur gegebenen Seite (route)
     */
    private openGesuch(antrag: TSAntragDTO, urlToGoTo: string, params: any, isCtrlKeyPressed: boolean): void {
        if (!antrag) {
            return;
        }
        if (isCtrlKeyPressed) {
            const url = this.$state.href(urlToGoTo, params);
            window.open(url, '_blank');
        } else {
            this.$state.go(urlToGoTo, params);
        }
    }
}

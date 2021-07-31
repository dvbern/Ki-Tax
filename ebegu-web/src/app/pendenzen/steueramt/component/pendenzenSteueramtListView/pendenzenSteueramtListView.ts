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

import IComponentOptions = angular.IComponentOptions;
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;
import {StateService} from '@uirouter/core';
import {IController} from 'angular';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {SearchRS} from '../../../../../gesuch/service/searchRS.rest';
import {TSAntragDTO} from '../../../../../models/TSAntragDTO';
import {TSPaginationResultDTO} from '../../../../../models/TSPaginationResultDTO';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';

export class PendenzenSteueramtListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./pendenzenSteueramtListView.html');
    public controller = PendenzenSteueramtListViewController;
    public controllerAs = 'vm';
}

export class PendenzenSteueramtListViewController implements IController {

    public static $inject: string[] = ['GesuchModelManager', '$state', '$log', 'SearchRS'];

    public totalResultCount: string = '0';
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $state: StateService,
        private readonly $log: ILogService,
        private readonly searchRS: SearchRS,
    ) {
    }

    public editpendenzSteueramt(pendenz: TSAntragDTO, event: any): void {
        if (pendenz) {
            const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openPendenz(pendenz, isCtrlKeyPressed);
        }
    }

    public passFilterToServer = (tableFilterState: any): IPromise<TSPaginationResultDTO<TSAntragDTO>> => {
        this.$log.debug('Triggering ServerFiltering with Filter Object', tableFilterState);
        return this.searchRS.searchAntraege(tableFilterState).then((response: TSPaginationResultDTO<TSAntragDTO>) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize.toString() : '0';
            return response;
        });

    }

    private openPendenz(pendenz: TSAntragDTO, isCtrlKeyPressed: boolean): void {
        this.gesuchModelManager.clearGesuch();
        const navObj: any = {
            gesuchId: pendenz.antragId,
        };
        if (isCtrlKeyPressed) {
            const url = this.$state.href('gesuch.familiensituation', navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go('gesuch.familiensituation', navObj);
        }
    }
}

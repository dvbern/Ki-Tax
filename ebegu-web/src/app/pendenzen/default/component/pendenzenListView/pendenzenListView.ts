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
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../../gesuch/service/gemeindeRS.rest';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {SearchRS} from '../../../../../gesuch/service/searchRS.rest';
import {TSAntragStatus} from '../../../../../models/enums/TSAntragStatus';
import {TSAntragDTO} from '../../../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../../../models/TSAntragSearchresultDTO';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import ILogService = angular.ILogService;
import IPromise = angular.IPromise;

export class PendenzenListViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./pendenzenListView.html');
    public controller = PendenzenListViewController;
    public controllerAs = 'vm';
}

export class PendenzenListViewController {

    public static $inject: string[] = [
        'GesuchModelManager',
        '$state',
        '$log',
        'SearchRS',
        'AuthServiceRS',
        'GemeindeRS',
    ];

    public totalResultCount: string = '0';
    public hasGemeindenInStatusAngemeldet: boolean = false;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $state: StateService,
        private readonly $log: ILogService,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public $onInit(): void {
        this.initHasGemeindenInStatusAngemeldet();
    }

    public passFilterToServer = (tableFilterState: any): IPromise<TSAntragSearchresultDTO> => {
        this.$log.debug('Triggering ServerFiltering with Filter Object', tableFilterState);
        return this.searchRS.getPendenzenList(tableFilterState).then((response: TSAntragSearchresultDTO) => {
            this.totalResultCount = response.totalResultSize ? response.totalResultSize.toString() : '0';
            return response;
        });

    }

    public editpendenzJA(pendenz: TSAntragDTO, event: any): void {
        if (pendenz) {
            const isCtrlKeyPressed: boolean = (event && event.ctrlKey);
            this.openPendenz(pendenz, isCtrlKeyPressed);
        }
    }

    private openPendenz(pendenz: TSAntragDTO, isCtrlKeyPressed: boolean): void {
        this.gesuchModelManager.clearGesuch();
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSteueramtOnlyRoles())) {
            const navObj: any = {
                gesuchId: pendenz.antragId,
            };
            this.navigate('gesuch.familiensituation', navObj, isCtrlKeyPressed);
        } else if (pendenz.status === TSAntragStatus.IN_BEARBEITUNG_SOZIALDIENST) {
            const navObj: any = {
                gesuchId: pendenz.antragId,
                dossierId: pendenz.dossierId,
                fallId: pendenz.fallId,
                gemeindeId: pendenz.gemeindeId,
            };
            this.navigate('gesuch.sozialdienstfallcreation', navObj, isCtrlKeyPressed);
        } else {
            const navObj: any = {
                gesuchId: pendenz.antragId,
                dossierId: pendenz.dossierId,
            };
            this.navigate('gesuch.fallcreation', navObj, isCtrlKeyPressed);
        }
    }

    private navigate(path: string, navObj: any, isCtrlKeyPressed: boolean): void {
        if (isCtrlKeyPressed) {
            const url = this.$state.href(path, navObj);
            window.open(url, '_blank');
        } else {
            this.$state.go(path, navObj);
        }
    }

    private initHasGemeindenInStatusAngemeldet(): void {
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorBgTsGemeindeRole())) {
            return;
        }
        this.gemeindeRS.hasGemeindenInStatusAngemeldet()
            .then(result => {
                this.hasGemeindenInStatusAngemeldet = result;
            });
    }
}

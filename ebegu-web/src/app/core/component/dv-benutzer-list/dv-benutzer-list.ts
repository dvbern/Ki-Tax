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

import {IComponentOptions, ILogService, IOnInit, IPromise, IWindowService} from 'angular';
import {take} from 'rxjs/operators';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../../gesuch/service/gemeindeRS.rest';
import {TSBenutzerStatus} from '../../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import TSBenutzer from '../../../../models/TSBenutzer';
import TSGemeinde from '../../../../models/TSGemeinde';
import TSInstitution from '../../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../../models/TSTraegerschaft';
import TSUserSearchresultDTO from '../../../../models/TSUserSearchresultDTO';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';
import {InstitutionRS} from '../../service/institutionRS.rest';
import {TraegerschaftRS} from '../../service/traegerschaftRS.rest';
import ITranslateService = angular.translate.ITranslateService;

const LOG = LogFactory.createLog('DVBenutzerListController');

export class DVBenutzerListConfig implements IComponentOptions {
    transclude = false;

    bindings = {
        onEdit: '&',
        onFilterChange: '&',
        totalResultCount: '<',
        tableId: '@',
        tableTitle: '@',
    };
    template = require('./dv-benutzer-list.html');
    controller = DVBenutzerListController;
    controllerAs = 'vm';
}

export class DVBenutzerListController implements IOnInit {

    static $inject: ReadonlyArray<string> = ['$log', 'InstitutionRS', 'TraegerschaftRS', 'AuthServiceRS', '$window',
        '$translate', 'GemeindeRS'];

    totalResultCount: number;
    displayedCollection: Array<TSBenutzer> = []; //Liste die im Gui angezeigt wird
    pagination: any;

    institutionenList: Array<TSInstitution>;
    traegerschaftenList: Array<TSTraegerschaft>;
    gemeindeList: Array<TSGemeinde>;

    selectedUsername: string;
    selectedVorname: string;
    selectedNachname: string;
    selectedEmail: string;
    selectedRole: TSRole;
    selectedGemeinde: TSGemeinde;
    selectedInstitution: TSInstitution;
    selectedTraegerschaft: TSTraegerschaft;
    selectedBenutzerStatus: TSBenutzerStatus;

    tableId: string;
    tableTitle: string;

    onFilterChange: (changedTableState: any) => IPromise<any>;
    onEdit: (user: any) => void;
    TSRoleUtil: TSRoleUtil;
    public readonly benutzerStatuses = Object.values(TSBenutzerStatus);

    constructor(private readonly $log: ILogService,
                private readonly institutionRS: InstitutionRS,
                private readonly traegerschaftenRS: TraegerschaftRS,
                private readonly authServiceRS: AuthServiceRS,
                private readonly $window: IWindowService,
                private readonly $translate: ITranslateService,
                private readonly gemeindeRS: GemeindeRS) {

        this.TSRoleUtil = TSRoleUtil;
    }

    $onInit() {
        //statt diese Listen zu laden koenne man sie auch von aussen setzen
        this.updateInstitutionenList();
        this.updateTraegerschaftenList();
        this.updateGemeindeList();
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getAllInstitutionen().then((response: any) => {
            this.institutionenList = angular.copy(response);
        });
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftenRS.getAllTraegerschaften().then((response: any) => {
            this.traegerschaftenList = angular.copy(response);
        });
    }

    /**
     * Fuer den SUPER_ADMIN muessen wir die gesamte Liste von Gemeinden zurueckgeben, da er zu keiner Gemeinde gehoert
     * aber alles machen darf. Fuer andere Benutzer geben wir die Liste von Gemeinden zurueck, zu denen er gehoert.
     */
    private updateGemeindeList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(take(1))
            .subscribe(
                gemeinden => {
                    this.gemeindeList = gemeinden;
                },
                err => LOG.error(err)
            );
    }

    editClicked(user: any, event: any) {
        this.onEdit({user: user, event: event});
    }

    public readonly callServer = (tableFilterState: any) => {
        const pagination = tableFilterState.pagination;
        this.pagination = pagination;

        if (this.onFilterChange && angular.isFunction(this.onFilterChange)) {
            this.onFilterChange({tableState: tableFilterState}).then((result: TSUserSearchresultDTO) => {
                if (result) {
                    pagination.totalItemCount = result.totalResultSize;
                    pagination.numberOfPages = Math.ceil(result.totalResultSize / pagination.number);
                    this.displayedCollection = [].concat(result.userDTOs);
                }
            });
        } else {
            this.$log.info('no callback function spcified for filtering');
        }
    };

    public getRollen(): TSRole[] {
        return this.authServiceRS.getVisibleRolesForPrincipal();
    }

    /**
     * Provided there is a row with id benutzerHeadRow it will take this row to check how many
     * columns there are. Therefore this row cannot have any colspan inside any cell and any other
     * children but td or th
     */
    public getColumnsNumber(): number {
        const element = this.$window.document.getElementById('benutzerHeadRow');
        return element.childElementCount;
    }
}




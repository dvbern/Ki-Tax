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
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSBenutzerStatus} from '../../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSSozialdienst} from '../../../../models/sozialdienst/TSSozialdienst';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSInstitution} from '../../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../../models/TSTraegerschaft';
import {TSUserSearchresultDTO} from '../../../../models/TSUserSearchresultDTO';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {LogFactory} from '../../logging/LogFactory';
import {InstitutionRS} from '../../service/institutionRS.rest';
import {SozialdienstRS} from '../../service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../service/traegerschaftRS.rest';

const LOG = LogFactory.createLog('DVBenutzerListController');

export class DVBenutzerListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        onEdit: '&',
        onFilterChange: '&',
        totalResultCount: '<',
        tableId: '@',
        tableTitle: '@',
    };
    public template = require('./dv-benutzer-list.html');
    public controller = DVBenutzerListController;
    public controllerAs = 'vm';
}

export class DVBenutzerListController implements IOnInit {

    public static $inject: ReadonlyArray<string> = [
        '$log',
        'InstitutionRS',
        'TraegerschaftRS',
        'AuthServiceRS',
        '$window',
        'GemeindeRS',
        'EinstellungRS',
        'SozialdienstRS',
    ];

    public totalResultCount: number;
    public displayedCollection: Array<TSBenutzer> = []; // Liste die im Gui angezeigt wird
    public pagination: any;

    public institutionenList: Array<TSInstitution>;
    public traegerschaftenList: Array<TSTraegerschaft>;
    public gemeindeList: Array<TSGemeinde>;
    public sozialdienstList: Array<TSSozialdienst>;

    public selectedUsername: string;
    public selectedVorname: string;
    public selectedNachname: string;
    public selectedEmail: string;
    public selectedRole: TSRole;
    public selectedGemeinde: TSGemeinde;
    public selectedInstitution: TSInstitution;
    public selectedTraegerschaft: TSTraegerschaft;
    public selectedBenutzerStatus: TSBenutzerStatus;
    public selectedSozialdienst: TSSozialdienst;

    public tableId: string;
    public tableTitle: string;

    public onFilterChange: (changedTableState: any) => IPromise<any>;
    public onEdit: (user: any) => void;
    public readonly TSRoleUtil = TSRoleUtil;
    public readonly benutzerStatuses = Object.values(TSBenutzerStatus);

    public constructor(
        private readonly $log: ILogService,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftenRS: TraegerschaftRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly $window: IWindowService,
        private readonly gemeindeRS: GemeindeRS,
        public readonly einstellungRS: EinstellungRS,
        public readonly sozialdienstRS: SozialdienstRS,
    ) {
    }

    public $onInit(): void {
        // liste sind geladen nur wenn benoetigt
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
            this.updateInstitutionenList();
            this.updateGemeindeList();
        }
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles())) {
            this.updateTraegerschaftenList();
            this.updateSozialdienstList();
        }
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenEditableForCurrentBenutzer().then((response: any) => {
            this.institutionenList = angular.copy(response);
        });
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftenRS.getAllTraegerschaften().then((response: any) => {
            this.traegerschaftenList = angular.copy(response);
        });
    }

    private updateSozialdienstList(): void {
        this.sozialdienstRS.getSozialdienstList().toPromise().then((response: any) => {
            this.sozialdienstList = angular.copy(response);
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
                err => LOG.error(err),
            );
    }

    public editClicked(user: any, event: any): void {
        this.onEdit({user, event});
    }

    public readonly callServer = (tableFilterState: any) => {
        const pagination = tableFilterState.pagination;
        this.pagination = pagination;

        if (!this.onFilterChange || !angular.isFunction(this.onFilterChange)) {
            this.$log.info('no callback function spcified for filtering');
            return;
        }
        this.onFilterChange({tableState: tableFilterState}).then((result: TSUserSearchresultDTO) => {
            if (!result) {
                return;
            }

            pagination.totalItemCount = result.totalResultSize;
            pagination.numberOfPages = Math.ceil(result.totalResultSize / pagination.number);
            this.displayedCollection = [].concat(result.userDTOs);
        });
    };

    public getRollen(): ReadonlyArray<TSRole> {
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

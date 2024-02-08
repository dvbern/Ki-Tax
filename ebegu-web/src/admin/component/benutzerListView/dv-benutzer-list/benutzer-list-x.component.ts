/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output
} from '@angular/core';
import {LegacyPageEvent as PageEvent} from '@angular/material/legacy-paginator';
import {MatSort, Sort} from '@angular/material/sort';
import {MatLegacyTableDataSource as MatTableDataSource} from '@angular/material/legacy-table';
import {take} from 'rxjs/operators';
import {CONSTANTS} from '../../../../app/core/constants/CONSTANTS';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {BenutzerRSX} from '../../../../app/core/service/benutzerRSX.rest';
import {InstitutionRS} from '../../../../app/core/service/institutionRS.rest';
import {SozialdienstRS} from '../../../../app/core/service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../../../app/core/service/traegerschaftRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {TSBenutzerTableFilterDTO} from '../../../../models/dto/TSBenutzerTableFilterDTO';
import {TSPagination} from '../../../../models/dto/TSPagination';
import {TSBenutzerStatus} from '../../../../models/enums/TSBenutzerStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSSozialdienst} from '../../../../models/sozialdienst/TSSozialdienst';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSInstitution} from '../../../../models/TSInstitution';
import {TSTraegerschaft} from '../../../../models/TSTraegerschaft';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {BenutzerListFilter} from './BenutzerListFilter';

const LOG = LogFactory.createLog('BenutzerListXComponent');

@Component({
    selector: 'dv-benutzer-list-x',
    templateUrl: './benutzer-list-x.component.html',
    styleUrls: ['./benutzer-list-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BenutzerListXComponent implements OnInit {

    @Input()
    public tableTitle: string;

    @Input()
    public tableId: string;

    @Input()
    public pendenz: boolean;

    @Output()
    public readonly edit: EventEmitter<{user: TSBenutzer}> = new EventEmitter<{user: TSBenutzer}>();

    public institutionenList: Array<TSInstitution>;
    public traegerschaftenList: Array<TSTraegerschaft>;
    public gemeindeList: Array<TSGemeinde>;
    public sozialdienstList: Array<TSSozialdienst>;

    public readonly benutzerStatuses = Object.values(TSBenutzerStatus);

    public datasource: MatTableDataSource<TSBenutzer>;

    public displayedColumns: string[] = ['username', 'vorname', 'name', 'email', 'role',
        'roleGueltigAb', 'roleGueltigBis'];
    public filterColumns: string[] = ['username-filter', 'vorname-filter', 'name-filter',
        'email-filter', 'role-filter', 'roleGueltigAb-filter', 'roleGueltigBis-filter'];

    public gemeindenStr: string;

    public paginationItems: number[];
    public page: number;
    public readonly pageSize = 20;
    public filterPredicate: BenutzerListFilter = new BenutzerListFilter();
    public sort: MatSort = new MatSort();

    public totalResultCount: number;

    /**
     * Filter change should not be triggered when user is still typing. Filter change is triggered
     * after user stopped typing for timeoutMS milliseconds
     */
    private keyupTimeout: NodeJS.Timeout;
    private readonly timeoutMS = CONSTANTS.KEYUP_TIMEOUT;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly sozialdienstRS: SozialdienstRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly cd: ChangeDetectorRef,
        private readonly benutzerRS: BenutzerRSX
    ) {
    }

    public ngOnInit(): void {
        this.initFilterSortPaginate();
        // listen sind geladen nur wenn benoetigt
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeRoles())) {
            this.updateInstitutionenList();
            this.updateGemeindeList();
            this.displayedColumns.push('gemeinde', 'institution');
            this.filterColumns.push('gemeinde-filter', 'institution-filter');
        }
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles())) {
            this.updateTraegerschaftenList();
            this.updateSozialdienstList();
            this.displayedColumns.push('traegerschaft', 'sozialdienst');
            this.filterColumns.push('traegerschaft-filter', 'sozialdienst-filter');
        }
        this.displayedColumns.push('status');
        this.filterColumns.push('status-filter');
        this.initDataSource();
    }

    private initFilterSortPaginate(): void {
        this.page = 0;
        this.filterPredicate = new BenutzerListFilter();
        this.sort = new MatSort();
    }

    private initDataSource(): void {
        this.datasource = new MatTableDataSource<TSBenutzer>([]);
        this.searchUsers();
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenEditableForCurrentBenutzer().subscribe((response: TSInstitution[]) => {
            this.institutionenList = response;
            this.institutionenList.sort((a, b) => a.name.localeCompare(b.name));
            this.cd.markForCheck();
        }, error => LOG.error(error));
    }

    private updateTraegerschaftenList(): void {
        this.traegerschaftRS.getAllTraegerschaften().then((response: TSTraegerschaft[]) => {
            this.traegerschaftenList = response;
            this.cd.markForCheck();
        });
    }

    private updateSozialdienstList(): void {
        this.sozialdienstRS.getSozialdienstList().toPromise().then((response: TSSozialdienst[]) => {
            this.sozialdienstList = response;
            this.cd.markForCheck();
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
                    this.gemeindenStr = this.gemeindenToString(gemeinden);
                    this.cd.markForCheck();
                },
                err => LOG.error(err)
            );
    }

    private gemeindenToString(gemeinden: TSGemeinde[]): string {
        return gemeinden
            .map((gem: TSGemeinde) => gem.name)
            .sort((a, b) => a.localeCompare(b))
            .join(', ');
    }

    public editClicked(user: any): void {
        this.edit.emit({user});
    }

    public getRoles(): ReadonlyArray<TSRole> {
        return this.authServiceRS.getVisibleRolesForPrincipal();
    }

    public resetSearch(): void {
        this.initFilterSortPaginate();
        this.searchUsers();
    }

    public searchUsers(): void {
        const paginate = new TSPagination();
        paginate.start = this.page * this.pageSize;
        paginate.number = this.pageSize;
        paginate.totalItemCount = this.totalResultCount;

        const filterDTO = new TSBenutzerTableFilterDTO(
            paginate,
            this.sort,
            this.filterPredicate
        );
        this.benutzerRS.searchUsers(filterDTO).then(res => {
            this.datasource.data = res.userDTOs;
            this.totalResultCount = res.totalResultSize;
            this.updatePagination();
        });
    }

    public applyFilter(value: any, property: string): void {
        // @ts-ignore
        this.filterPredicate[property] = value ? value : undefined;
        this.page = 0;
        this.searchUsers();
    }

    // für textinputs wollen wir nicht bei jedem KeyUp Event einen Request senden. Wir fügen ein Debounce hinzu
    public applyFilterWithDebounce(value: any, property: string): void {
        // @ts-ignore
        this.filterPredicate[property] = value ? value : undefined;
        this.page = 0;
        clearTimeout(this.keyupTimeout);
        this.keyupTimeout = setTimeout(() => {
            this.searchUsers();
        }, this.timeoutMS);
    }

    public sortChange($event: Sort): void {
        this.sort = $event as MatSort;
        this.page = 0;
        this.searchUsers();
    }

    public paginationChange(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.searchUsers();
    }

    private updatePagination(): void {
        this.paginationItems = [];
        for (let i = Math.max(1, this.page - 4); i <= Math.min(Math.ceil(this.totalResultCount / this.pageSize),
            this.page + 5); i++) {
            this.paginationItems.push(i);
        }
    }

    public getGemeindenForBenutzer(benutzer: TSBenutzer): string {
        return benutzer.currentBerechtigung?.gemeindeList
            .map(g => g.name)
            .join(', ');
    }
}

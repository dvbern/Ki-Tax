/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import {MatSort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {take} from 'rxjs/operators';
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
import {LogFactory} from '../../logging/LogFactory';
import {BenutzerRSX} from '../../service/benutzerRSX.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';
import {SozialdienstRS} from '../../service/SozialdienstRS.rest';
import {TraegerschaftRS} from '../../service/traegerschaftRS.rest';
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
    public totalResultCount: number;

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

    public displayedColumns: string[] = ['username', 'vorname', 'name', 'email', 'rolle',
        'roleGueltigAb', 'roleGueltigBis'];
    public filterColumns: string[] = ['username-filter', 'vorname-filter', 'name-filter',
        'email-filter', 'rolle-filter', 'roleGueltigAb-filter', 'roleGueltigBis-filter'];

    public gemeindenStr: string;

    public filterPredicate: BenutzerListFilter = {};

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly sozialdienstRS: SozialdienstRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly cd: ChangeDetectorRef,
        private readonly benutzerRS: BenutzerRSX,
    ) {
    }

    public ngOnInit(): void {
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
            this.displayedColumns.push('sozialdienst', 'traegerschaft');
            this.filterColumns.push('sozialdienst-filter', 'traegerschaft-filter');
        }
        this.displayedColumns.push('status');
        this.filterColumns.push('status-filter');
        this.initDataSource();
    }

    private initDataSource(): void {
        this.datasource = new MatTableDataSource<TSBenutzer>([]);
        this.sortData({} as any);
    }

    private updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenEditableForCurrentBenutzer().then((response: TSInstitution[]) => {
            this.institutionenList = response;
            this.cd.markForCheck();
        });
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
                err => LOG.error(err),
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

    public getRollen(): ReadonlyArray<TSRole> {
        return this.authServiceRS.getVisibleRolesForPrincipal();
    }

    // /**
    //  * Provided there is a row with id benutzerHeadRow it will take this row to check how many
    //  * columns there are. Therefore this row cannot have any colspan inside any cell and any other
    //  * children but td or th
    //  */
    // public getColumnsNumber(): number {
    //     const element = this.$window.document.getElementById('benutzerHeadRow');
    //     return element.childElementCount;
    // }

    public resetSearch(): void {

    }

    public sortData($event: MatSort): void {
        const paginate = new TSPagination();
        const sort = $event;
        const filterDTO: TSBenutzerTableFilterDTO = new TSBenutzerTableFilterDTO(
            paginate,
            sort,
            {}
        );
        this.benutzerRS.searchUsers(filterDTO).then(res => {
            this.datasource.data = res.userDTOs;
            this.totalResultCount = res.totalResultSize;
        });
    }

    public applyFilter(value: any, property: string) {

    }
}

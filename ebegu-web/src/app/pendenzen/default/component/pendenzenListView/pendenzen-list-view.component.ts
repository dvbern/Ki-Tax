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

import {ChangeDetectionStrategy, Component} from '@angular/core';
import {StateService} from '@uirouter/core';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../../gesuch/service/gemeindeRS.rest';
import {GesuchModelManager} from '../../../../../gesuch/service/gesuchModelManager';
import {SearchRS} from '../../../../../gesuch/service/searchRS.rest';
import {TSAntragStatus} from '../../../../../models/enums/TSAntragStatus';
import {TSAntragDTO} from '../../../../../models/TSAntragDTO';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {LogFactory} from '../../../../core/logging/LogFactory';
import {DVAntragListFilter} from '../../../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../../../shared/interfaces/DVAntragListItem';
import {DVPaginationEvent} from '../../../../shared/interfaces/DVPaginationEvent';

const LOG = LogFactory.createLog('PendenzenListViewComponent');

@Component({
    selector: 'pendenzen-list-view',
    templateUrl: './pendenzen-list-view.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PendenzenListViewComponent {

    public hasGemeindenInStatusAngemeldet: boolean = false;

    public data$: BehaviorSubject<DVAntragListItem[]> = new BehaviorSubject<DVAntragListItem[]>([]);
    public pagination: {
        number: number,
        totalItemCount: number,
        start: number
    } = {
        number: 20,
        totalItemCount: 0,
        start: 0,
    };
    private readonly search: { predicateObject: DVAntragListFilter } = {
        predicateObject: {},
    };

    private sort: {
        predicate?: string,
        reverse?: boolean
    } = {};

    public initialFilter: DVAntragListFilter = {};

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $state: StateService,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.authServiceRS.principal$
        .pipe(filter(principal => !!principal))
        .subscribe(principal => {
            this.initialFilter.verantwortlicherGemeinde = principal.getFullName();
            this.search.predicateObject = this.initialFilter;
            this.countData();
            this.loadData();
        }, error => {
            LOG.error(error);
        });
        this.initHasGemeindenInStatusAngemeldet();
    }

    private countData(): void {
        this.searchRS.countPendenzenList({pagination: this.pagination, search: this.search, sort: this.sort}).then(
            response => this.pagination.totalItemCount = response ? response : 0,
        );
    }

    private loadData(): void {
        this.searchRS.getPendenzenList({pagination: this.pagination, search: this.search, sort: this.sort})
            .then(response => {
                // we lose the "this" if we don't map here
                this.data$.next(response.antragDTOs.map(antragDto => {
                    return {
                        fallNummer: antragDto.fallNummer,
                        dossierId: antragDto.dossierId,
                        antragId: antragDto.antragId,
                        gemeinde: antragDto.gemeinde,
                        status: antragDto.status,
                        familienName: antragDto.familienName,
                        kinder: antragDto.kinder,
                        laufNummer: antragDto.laufnummer,
                        antragTyp: antragDto.antragTyp,
                        periode: antragDto.gesuchsperiodeString,
                        aenderungsdatum: antragDto.aenderungsdatum,
                        internePendenz: antragDto.internePendenz,
                        internePendenzAbgelaufen: antragDto.internePendenzAbgelaufen,
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG,
                        hasBesitzer: () => antragDto.hasBesitzer(),
                        isSozialdienst: antragDto.isSozialdienst,
                    };
                }));
            });
    }

    public onFilterChange(filter: DVAntragListFilter): void {
        this.search.predicateObject = {
            ...filter,
        };
        this.loadData();
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

    public onPagination(paginationEvent: DVPaginationEvent): void {
        this.pagination.number = paginationEvent.pageSize;
        this.pagination.start = paginationEvent.page * paginationEvent.pageSize;

        this.loadData();
    }

    public onSort(sort: { predicate?: string; reverse?: boolean }): void {
        this.sort = sort;

        this.loadData();
    }

    public calculatePage(): number {
        return Math.floor(this.pagination.start / this.pagination.number);
    }
}

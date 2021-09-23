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

import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
import {PageEvent} from '@angular/material/paginator';
import {MatSort, MatSortHeader, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TransitionService} from '@uirouter/angular';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {from, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSPagination} from '../../../models/dto/TSPagination';
import {getTSMitteilungsStatusForFilter, TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSVerantwortung} from '../../../models/enums/TSVerantwortung';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {MitteilungRS} from '../../core/service/mitteilungRS.rest';
import {DVPosteingangFilter} from '../../shared/interfaces/DVPosteingangFilter';
import {StateStoreService} from '../../shared/services/state-store.service';

@Component({
    selector: 'posteingang-view',
    templateUrl: './posteingang-view.component.html',
    styleUrls: ['./posteingang-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PosteingangViewComponent implements OnInit, OnDestroy, AfterViewInit {
    @ViewChild(MatSort) private readonly matSort: MatSort;

    private readonly log: Log = LogFactory.createLog('PosteingangViewComponent');

    private readonly unsubscribe$ = new Subject<void>();

    private keyupTimeout: NodeJS.Timeout;
    private readonly timeoutMS = 700;

    public readonly allColumns = [
        'sender',
        'gemeinde',
        'fallNummer',
        'familienName',
        'subject',
        'sentDatum',
        'empfaenger',
        'empfaengerVerantwortung',
        'mitteilungStatus',
    ];

    public filterColumns: string[] = [
        'sender-filter',
        'gemeinde-filter',
        'fallNummer-filter',
        'familienName-filter',
        'subject-filter',
        'sentDatum-filter',
        'empfaenger-filter',
        'empfaengerVerantwortung-filter',
        'mitteilungStatus-filter',
    ];

    // Liste die im Gui angezeigt wird
    public displayedCollection: MatTableDataSource<TSMitteilung>;
    public pagination: TSPagination = new TSPagination();
    public page: number = 0;
    public pageSize: any = 20;
    public totalItem: number = 0;
    public totalResultCount: string = '0';
    // Muss hier gespeichert werden, damit es fuer den Aufruf ab "Inkl.Erledigt"-Checkbox vorhanden ist
    public myTableFilterState: any;

    public itemsByPage: number = 20;
    public numberOfPages: number = 1;
    public selectedVerantwortung: string;
    public includeClosed: boolean = false;
    public gemeindenList: Array<TSGemeinde> = [];
    public paginationItems: number[];
    public initialEmpfaenger: TSBenutzerNoDetails;
    public filterPredicate: DVPosteingangFilter = {};

    // StateStore Properties
    public initialFilter: DVPosteingangFilter;
    public readonly stateStoreId: string = 'posteingangId';
    private sortId: string;
    private filterId: string;
    private readonly sort: {
        predicate?: string,
        reverse?: boolean
    } = {};

    public constructor(
        private readonly mitteilungRS: MitteilungRS,
        private readonly $state: StateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly transitionService: TransitionService,
        private readonly stateStore: StateStoreService,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly benutzerRS: BenutzerRSX,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.updateGemeindenList();
        this.initStateStores();
        this.initFilter();
        this.initEmpfaengerFilter();
    }

    public ngAfterViewInit(): void {
        this.displayedCollection = new MatTableDataSource<TSMitteilung>([]);
        this.initSort();
        this.passFilterToServer();
    }

    private initEmpfaengerFilter(): void {
        this.benutzerRS.getAllBenutzerBgTsOrGemeinde().then(response => {
            this.initialEmpfaenger =
                EbeguUtil.findUserByNameInList(this.filterPredicate?.empfaenger, response);
            this.changeDetectorRef.markForCheck();
        });
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public addZerosToFallNummer(fallnummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallnummer);
    }

    public gotoMitteilung(mitteilung: TSMitteilung): void {
        this.$state.go('mitteilungen.view', {
            dossierId: mitteilung.dossier.id,
            fallId: mitteilung.dossier.fall.id,
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => this.log.error(err),
            );
    }

    public getVerantwortungList(): Array<string> {
        return [TSVerantwortung.VERANTWORTUNG_BG, TSVerantwortung.VERANTWORTUNG_TS];
    }

    public getMitteilungsStatus(): Array<TSMitteilungStatus> {
        return getTSMitteilungsStatusForFilter();
    }

    public clickedIncludeClosed(): void {
        this.passFilterToServer();
    }

    private passFilterToServer(): void {
        const body = {
            pagination: {
                number: this.pageSize,
                start: this.page * this.pageSize,
            },
            search: {
                predicateObject: this.filterPredicate,
            },
            sort: this.sort,
        };
        const dataToLoad$ = from(this.mitteilungRS.searchMitteilungen(body,
            this.includeClosed)).pipe(map((result: TSMtteilungSearchresultDTO) => {
            return result;
        }));

        dataToLoad$.subscribe((result: TSMtteilungSearchresultDTO) => {
                this.setResult(result);
            },
            err => this.log.error(err),
        );
    }

    private setResult(result: TSMtteilungSearchresultDTO): void {
        if (!result) {
            return;
        }
        this.displayedCollection.data = [].concat(result.mitteilungen);
        this.totalItem = result.totalResultSize ? result.totalResultSize : 0;
        this.totalResultCount = this.totalItem.toString();
        this.updatePagination();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isSozialdienst(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSozialdienstRolle());
    }

    public isSozialdienstOrInstitution(): boolean {
        return this.isSozialdienst() || this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
    }

    private applyFilter(): void {
        clearTimeout(this.keyupTimeout);
        this.keyupTimeout = setTimeout(() => {
            this.passFilterToServer();
        }, this.timeoutMS);
    }

    public filterSender(sender: string): void {
        this.filterPredicate.sender = sender;
        this.applyFilter();
    }

    public filterGemeinde(gemeinde: string): void {
        this.filterPredicate.gemeinde = gemeinde;
        this.applyFilter();
    }

    public filterFall(query: string): void {
        this.filterPredicate.fallNummer = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterFamilieName(familienName: string): void {
        this.filterPredicate.familienName = familienName;
        this.applyFilter();
    }

    public filterSubject(subject: string): void {
        this.filterPredicate.subject = subject;
        this.applyFilter();
    }

    public filterSentDatum(query: string): void {
        this.filterPredicate.sentDatum = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterEmpfaenger(empfaenger: TSBenutzerNoDetails): void {
        this.filterPredicate.empfaenger = empfaenger ? empfaenger.getFullName() : null;
        this.applyFilter();
    }

    public filterVerantwortung(empfaengerVerantwortung: string): void {
        this.filterPredicate.empfaengerVerantwortung = empfaengerVerantwortung;
        this.applyFilter();
    }

    public filterMitteilungStatus(mitteilungStatus: string): void {
        this.filterPredicate.mitteilungStatus = mitteilungStatus;
        this.applyFilter();
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.pageSize = pageEvent.pageSize;
        this.pagination.number = pageEvent.pageSize;
        this.pagination.start = this.page * pageEvent.pageSize;
        this.passFilterToServer();
    }

    public sortData(sortEvent: Sort): void {
        this.sort.predicate = sortEvent.direction.length > 0 ? sortEvent.active : null;
        this.sort.reverse = sortEvent.direction === 'asc';
        this.passFilterToServer();
    }

    private updatePagination(): void {
        this.paginationItems = [];
        for (let i = Math.max(1, this.page - 4); i <= Math.min(Math.ceil(this.totalItem / this.pageSize),
            this.page + 5); i++) {
            this.paginationItems.push(i);
        }
    }

    private initFilter(): void {
        this.filterPredicate = (this.filterId && this.stateStore.has(this.filterId)) ?
            this.stateStore.get(this.filterId) :
            {...this.initialFilter};
    }

    private initStateStores(): void {
        this.sortId = `${this.stateStoreId}-sort`;
        this.filterId = `${this.stateStoreId}-filter`;

        this.transitionService.onStart({exiting: this.uiRouterGlobals.$current.name}, () => {
            if (this.sort.predicate) {
                this.stateStore.store(this.sortId, this.sort);
            } else {
                this.stateStore.delete(this.sortId);
                this.stateStore.delete(this.filterId);
            }

            this.stateStore.store(this.filterId, this.filterPredicate);
        });
    }

    private initSort(): void {
        // tslint:disable-next-line:early-exit
        if (this.stateStore.has(this.sortId)) {
            const stored = this.stateStore.get(this.sortId) as { predicate?: string, reverse?: boolean };
            this.sort.predicate = stored.predicate;
            this.sort.reverse = stored.reverse;
            this.matSort.active = stored.predicate;
            this.matSort.direction = stored.reverse ? 'asc' : 'desc';
            (this.matSort.sortables.get(stored.predicate) as MatSortHeader)?._setAnimationTransitionState({toState: 'active'});
        }
    }
}

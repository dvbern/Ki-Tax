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
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
    ViewEncapsulation
} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, MatSortHeader, Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {TransitionService} from '@uirouter/angular';
import {UIRouterGlobals} from '@uirouter/core';
import {BehaviorSubject, forkJoin, Observable, of, Subject, Subscription} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {getTSAntragStatusValuesByRole, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {
    getTSBetreuungsangebotTypValuesForMandant,
    TSBetreuungsangebotTyp
} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitution} from '../../../models/TSInstitution';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {DVPaginationEvent} from '../../shared/interfaces/DVPaginationEvent';
import {StateStoreService} from '../../shared/services/state-store.service';
import {CONSTANTS} from '../constants/CONSTANTS';
import {ErrorService} from '../errors/service/ErrorService';
import {LogFactory} from '../logging/LogFactory';
import {BenutzerRSX} from '../service/benutzerRSX.rest';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

const LOG = LogFactory.createLog('DVAntragListController');

@Component({
    selector: 'dv-new-antrag-list',
    templateUrl: './new-antrag-list.component.html',
    styleUrls: ['./new-antrag-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    // we need this to overwrite angular material styles
    encapsulation: ViewEncapsulation.None
})
export class NewAntragListComponent implements OnInit, OnDestroy, OnChanges, AfterViewInit {

    @ViewChild(MatPaginator) public paginator: MatPaginator;
    @ViewChild(MatTable) private readonly table: MatTable<DVAntragListItem>;
    @ViewChild(MatSort) private readonly matSort: MatSort;

    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly rowClicked: EventEmitter<{ antrag: TSAntragDTO; event: MouseEvent }> = new EventEmitter<any>();

    /**
     * Can be one of
     * 'fallNummer',
     * 'gemeinde',
     * 'familienName',
     * 'kinder',
     * 'antragTyp',
     * 'periode',
     * 'aenderungsdatum',
     * 'status',
     * 'internePendenz',
     * 'dokumenteHochgeladen',
     * 'angebote',
     * 'institutionen',
     * 'verantwortlicheTS',
     * 'verantwortlicheBG',
     * 'verantwortlicheGemeinde',
     * 'verantwortlicherGemeindeantraege'
     *
     * Hides the column in the table
     */
    @Input() public hiddenColumns: string[] = [];

    /**
     * Inits the filter values. Does not trigger the EventEmitter
     */
    @Input() public initialFilter: DVAntragListFilter;

    /**
     * Used to provide other data than the default all faelle. Providing this input disables the provided filter and
     * pagination, meaning that instead of applying filter and pagination, they are emitted via their respective event
     * to enable server-side filtering and pagination.
     */
    @Input() public data$: Observable<DVAntragListItem[]>;

    /**
     * Emits any time the filter changes. Only emits when the data$ input is provided.
     */
    @Output() public readonly filterChange: EventEmitter<DVAntragListFilter> = new EventEmitter<DVAntragListFilter>();

    /**
     * Emits any time the sort changes. Only emits when the data$ input is provided.
     */
    @Output() public readonly sortChange: EventEmitter<{
        predicate?: string;
        reverse?: boolean;
    }> = new EventEmitter<{
        predicate?: string;
        reverse?: boolean;
    }>();

    /**
     * Emits any time the user clicks on the pagination navigation. Omly emits when the data$ input is provided.
     */
    @Output() public readonly paginationEvent: EventEmitter<DVPaginationEvent> = new EventEmitter<DVPaginationEvent>();

    /**
     * The first page the list starts on
     */
    @Input() public page: number = 0;

    /**
     * How many items should be displayed per page
     */
    @Input() public pageSize: any = 20;

    /**
     * The list of states that should be displayed in the filter dropdown. Overwrites the faelle default
     */
    @Input() public filterStateList: string[];

    /**
     * The list of types that should be displayed in the types dropdown. Overwirtes the antragTypes default
     */
    @Input() public filterTypeList: string[];

    /**
     * The amount of total entries in the database. Is taken from the default request if default request is used,
     * otherwise 0
     */
    @Input()
    public totalItems: number;

    /**
     * Hides pagination
     */
    @Input()
    public disablePagination: boolean = false;

    /**
     * Does the table show pendenzen or general fälle
     * TODO: is this necessary?
     */
    @Input()
    public pendenz: boolean = false;

    /**
     * The title displayed in the top left
     */
    @Input()
    public title: string;

    /**
     * Used for the state store to identify the component. If not provided, the filter and sort are
     * not stored on navigation
     */
    @Input()
    public readonly stateStoreId: string;

    @Input()
    public readonly showRemoveButton: boolean;

    @Output()
    public readonly removeClicked = new EventEmitter<DVAntragListItem>();

    public gesuchsperiodenList: Array<string> = [];
    private allInstitutionen: TSInstitution[];
    public institutionenList$: BehaviorSubject<TSInstitution[]> = new BehaviorSubject<TSInstitution[]>([]);

    public gemeindenList: Array<TSGemeinde> = [];

    private customData: boolean = false;
    public datasource: MatTableDataSource<DVAntragListItem>;
    public filterColumns: string[] = [
        'fallNummer-filter',
        'gemeinde-filter',
        'familienName-filter',
        'kinder-filter',
        'antragTyp-filter',
        'periode-filter',
        'aenderungsdatum-filter',
        'status-filter',
        'internePendenz-filter',
        'dokumenteHochgeladen-filter',
        'angebote-filter',
        'institutionen-filter',
        'verantwortlicheTS-filter',
        'verantwortlicheBG-filter',
        'verantwortlicheGemeinde-filter',
        'verantwortlicherGemeindeantraege-filter'
    ];

    private readonly allColumns = [
        'fallNummer',
        'gemeinde',
        'familienName',
        'kinder',
        'antragTyp',
        'periode',
        'aenderungsdatum',
        'status',
        'internePendenz',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
        'verantwortlicheGemeinde',
        'verantwortlicherGemeindeantraege'
    ];

    public displayedColumns: string[];

    public filterPredicate: DVAntragListFilter;

    public searchInaktivePerioden = false;
    private searchInaktivePeriodenId: string;

    private readonly unsubscribe$ = new Subject<void>();

    // used to cancel the previous subscription so we don't have two data loads racing each other
    private dataLoadingSubscription: Subscription;
    /**
     * Filter change should not be triggered when user is still typing. Filter change is triggered
     * after user stopped typing for timeoutMS milliseconds
     */
    private keyupTimeout: NodeJS.Timeout;
    private readonly timeoutMS = CONSTANTS.KEYUP_TIMEOUT;

    private readonly sort: {
        predicate?: string;
        reverse?: boolean;
    } = {};

    public paginationItems: number[];
    public userListBgTsOrGemeinde: TSBenutzerNoDetails[];
    public userListTsOrGemeinde: TSBenutzerNoDetails[];
    public userListBgOrGemeinde: TSBenutzerNoDetails[];
    public userListGemeindeantraege: TSBenutzerNoDetails[];
    public initialGemeindeUser: TSBenutzerNoDetails;
    public initialBgGemeindeUser: TSBenutzerNoDetails;
    public initialTsGemeindeUser: TSBenutzerNoDetails;
    private sortId: string;
    private filterId: string;

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly transitionService: TransitionService,
        private readonly stateStore: StateStoreService,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly benutzerRS: BenutzerRSX
    ) {
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.updateGemeindenList();
        this.initStateStores();
        this.updateGesuchsperiodenList();
        this.initFilter(true);
        this.initDisplayedColumns();
        this.initBenutzerLists();
    }

    public ngAfterViewInit(): void {
        this.initSort();
        this.initSearchInaktivePerioden();
        this.initTable();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.hiddenColumns || changes.showRemoveButton) {
            this.updateColumns();
        }

        // eslint-disable-next-line
        if (changes.data$) {
            this.customData = !!this.data$;
            if (!changes.data$.firstChange) {
                this.loadData();
            }
        }

        if (changes.totalItems) {
            this.updatePagination();
        }
    }

    private initDisplayedColumns(): void {
        if (this.filterColumns?.length > 0) {
            if (this.pendenz) {
                this.hiddenColumns.push('verantwortlicheBG');
                this.hiddenColumns.push('verantwortlicheTS');
            } else {
                this.hiddenColumns.push('verantwortlicheGemeinde');
            }
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSozialdienstRolle())) {
                this.hiddenColumns.push('verantwortlicheBG');
                this.hiddenColumns.push('verantwortlicheTS');
                this.hiddenColumns.push('verantwortlicheGemeinde');
                this.hiddenColumns.push('internePendenz');
                this.hiddenColumns.push('dokumenteHochgeladen');
            }
            if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())) {
                this.hiddenColumns.push('verantwortlicherGemeindeantraege');
            }
        }
        this.updateColumns();
    }

    private updateColumns(): void {
        this.displayedColumns = this.allColumns.filter(column => !this.hiddenColumns.includes(column));
        if (this.showRemoveButton) {
            this.displayedColumns.push('remove');
        }
        this.filterColumns = this.displayedColumns.map(column => `${column}-filter`);
    }

    private initSort(): void {
        // eslint-disable-next-line
        if (this.stateStoreId && this.stateStore.has(this.sortId)) {
            const stored = this.stateStore.get(this.sortId) as { predicate?: string; reverse?: boolean };
            this.sort.predicate = stored.predicate;
            this.sort.reverse = stored.reverse;
            this.matSort.active = stored.predicate;
            this.matSort.direction = stored.reverse ? 'asc' : 'desc';
            (this.matSort.sortables.get(stored.predicate) as MatSortHeader)?._setAnimationTransitionState({toState: 'active'});
            this.sortChange.emit(this.sort);
        }
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().subscribe(response => {
            this.allInstitutionen = response;
            this.institutionenList$.next(this.allInstitutionen);
        }, error => LOG.error(error));
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            this.gesuchsperiodenList = [];
            response.forEach(gesuchsperiode => {
                if (this.showPeriodeInList(gesuchsperiode)) {
                    this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
                }
            });
        });
    }

    private showPeriodeInList(periode: TSGesuchsperiode): boolean {
        // falls der User die inaktiven Perioden nicht rausfiltern kann, dann wird die
        // periode immer gezeigt
        if (!this.showSearchInaktivePerioden()) {
            return true;
        }
        return this.searchInaktivePerioden || periode.isAktiv();
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                    gemeinden.sort((a, b) => a.name.localeCompare(b.name));
                },
                err => LOG.error(err)
            );
    }

    private initFilter(fromStore: boolean = false): void {
        this.filterPredicate = (fromStore && this.filterId && this.stateStore.has(this.filterId)) ?
            this.stateStore.get(this.filterId) :
            {...this.initialFilter};
        this.filterChange.emit(this.filterPredicate);
    }

    private initSearchInaktivePerioden(): void {
        if (this.stateStoreId && this.stateStore.has(this.searchInaktivePeriodenId)) {
            const stored = this.stateStore.get(this.searchInaktivePeriodenId) as { searchInaktivePerioden: boolean };
            this.searchInaktivePerioden = stored.searchInaktivePerioden;
        }
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initTable(): void {
        this.datasource = new MatTableDataSource<DVAntragListItem>([]);
        this.loadData();
    }

    private loadData(): void {
        const body = {
            pagination: {
                number: this.pageSize,
                start: this.page * this.pageSize
            },
            search: {
                predicateObject: this.filterPredicate
            },
            sort: this.sort,
            onlyAktivePerioden: !this.searchInaktivePerioden
        };
        const dataToLoad$: Observable<DVAntragListItem[]> = this.data$ ?
            this.data$ :
            this.searchRS.searchAntraege(body).pipe(map((result: TSAntragSearchresultDTO) => result.antragDTOs.map(antragDto => ({
                        fallNummer: antragDto.fallNummer,
                        dossierId: antragDto.dossierId,
                        antragId: antragDto.antragId,
                        gemeinde: antragDto.gemeinde,
                        status: antragDto.status,
                        familienName: antragDto.familienName,
                        kinder: antragDto.kinder,
                        laufNummer: antragDto.laufnummer,
                        antragTyp: antragDto.antragTyp,
                        periodenString: antragDto.gesuchsperiodeString,
                        aenderungsdatum: antragDto.aenderungsdatum,
                        internePendenz: antragDto.internePendenz,
                        internePendenzAbgelaufen: antragDto.internePendenzAbgelaufen,
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG,
                        hasBesitzer: () => antragDto.hasBesitzer(),
                        isSozialdienst: antragDto.isSozialdienst
                    }))));

        // cancel previous subscription if not closed
        this.dataLoadingSubscription?.unsubscribe();

        this.dataLoadingSubscription = dataToLoad$.subscribe((result: DVAntragListItem[]) => {
            this.datasource.data = result;
            this.updatePagination();
        }, error => {
            this.translate.get('DATA_RETRIEVAL_ERROR', error).subscribe(message => {
                this.errorService.addMesageAsError(message);
            }, translateError => console.error('Could not load translation', translateError));
        });

        this.loadTotalCount(body);
    }

    // TODO: Doctor: Refactor totalItems into Observable for smoother subscription handling
    private loadTotalCount(body: {
        search: { predicateObject: DVAntragListFilter };
        pagination: { number: any; start: number };
        sort: { predicate?: string; reverse?: boolean };
    }): void {
        if (!EbeguUtil.isNullOrUndefined(this.data$)) {
            return;
        }
        this.searchRS.countAntraege(body).subscribe(result => {
            this.totalItems = result;
        }, error => LOG.error(error));
    }

    private updatePagination(): void {
        this.paginationItems = [];
        for (let i = Math.max(1, this.page - 4); i <= Math.min(Math.ceil(this.totalItems / this.pageSize),
            this.page + 5); i++) {
            this.paginationItems.push(i);
        }
    }

    /**
     * Filter is applied only if user stopped typing for timeoutMS Milliseconds. Otherwise
     * previous event is canceled
     */
    private applyFilter(): void {
        clearTimeout(this.keyupTimeout);
        this.keyupTimeout = setTimeout(() => {
            if (this.customData) {
                this.filterChange.emit(this.filterPredicate);
            } else {
                this.page = 0;
                this.loadData();
            }
        }, this.timeoutMS);
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.pageSize = pageEvent.pageSize;

        if (this.customData) {
            this.paginationEvent.emit({
                page: this.page,
                pageSize: this.pageSize
            });
        }
        this.loadData();
    }

    public filterFall(query: string): void {
        this.filterPredicate.fallNummer = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterGemeinde(gemeinde: string): void {
        this.filterPredicate.gemeinde = gemeinde;
        this.applyFilter();
    }

    public filterType(type: string): void {
        this.filterPredicate.antragTyp = type;
        this.applyFilter();
    }

    public filterPeriode(periode: string): void {
        this.filterPredicate.gesuchsperiodeString = periode;
        this.applyFilter();
    }

    public filterStatus(state: string): void {
        this.filterPredicate.status = state;
        this.applyFilter();
    }

    public filterDocumentsUploaded(documentsUploaded: boolean): void {
        this.filterPredicate.dokumenteHochgeladen = documentsUploaded;
        this.applyFilter();
    }

    public filterInternePendenz(internePendenz: boolean): void {
        this.filterPredicate.internePendenz = internePendenz;
        this.applyFilter();
    }

    public filterAngebot(angebot: string): void {
        this.filterPredicate.angebote = angebot;
        this.applyFilter();
    }

    public filterVerantwortlicheTS(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherTS = verantwortliche ? verantwortliche.getFullName() : null;
        this.applyFilter();
    }

    public filterVerantwortlicheBG(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherBG = verantwortliche ? verantwortliche.getFullName() : null;
        this.applyFilter();
    }

    public filterVerantwortlicheGemeinde(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherGemeinde = verantwortliche ? verantwortliche.getFullName() : null;
        this.applyFilter();
    }

    public filterVerantwortlicherGemeindeantraege(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherGemeindeantraege = verantwortliche ? verantwortliche : null;
        this.applyFilter();
    }

    public filterFamilie(query: string): void {
        this.filterPredicate.familienName = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterKinder(query: string): void {
        this.filterPredicate.kinder = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterGeaendert(query: string): void {
        this.filterPredicate.aenderungsdatum = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public filterInstitution(query: string): void {
        // filter the institutitonen list for the autocomplete
        this.institutionenList$.next(
            query ?
                this.allInstitutionen.filter(institution => institution.name.toLocaleLowerCase()
                    .includes(query.toLocaleLowerCase())) :
                this.allInstitutionen
        );
        this.filterPredicate.institutionen = query.length > 0 ? query : null;
        this.applyFilter();
    }

    public getAntragTypen(): string[] | TSAntragTyp[] {
        return this.filterTypeList || getNormalizedTSAntragTypValues();
    }

    /**
     * Alle TSAntragStatus fuer das Filterdropdown
     */
    public getAntragStatus(): string[] | TSAntragStatus[] {
        return this.filterStateList || getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
    }

    /**
     * Alle Betreuungsangebot typen fuer das Filterdropdown
     */
    public getBetreuungsangebotTypen(): TSBetreuungsangebotTyp[] {
        return getTSBetreuungsangebotTypValuesForMandant(this.isTagesschulangebotEnabled());
    }

    private isTagesschulangebotEnabled(): boolean {
        return this.authServiceRS.hasMandantAngebotTS();
    }

    public sortData(sortEvent: Sort): void {
        this.sort.predicate = sortEvent.direction.length > 0 ? sortEvent.active : null;
        this.sort.reverse = sortEvent.direction === 'asc';
        if (this.customData) {
            this.sortChange.emit(this.sort);
        } else {
            this.loadData();
        }
    }

    public onEditClicked(antrag: TSAntragDTO, event: MouseEvent): void {
        this.rowClicked.emit({antrag, event});
    }

    public addZerosToFallnummer(fallNummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallNummer);
    }

    public createAngeboteString(angebote: string[]): Observable<string> {
        if (!angebote) {
            return of('');
        }
        return forkJoin(angebote.map(angebot => this.translate.get(angebot)))
            .pipe(map(translatedAngebote => translatedAngebote.join(', ')
            ));
    }

    public getAntragTypBezeichnung(element: any): string {
        let bezeichnung = this.translate.instant(element.antragTyp);
        if (element?.laufNummer > 0) {
            bezeichnung += ` ${element.laufNummer}`;
        }

        return bezeichnung;
    }

    // must be called after filterPredicate is initialized
    private initBenutzerLists(): void {
        if (this.isPendenzGemeindeRolle()) {
            this.benutzerRS.getAllBenutzerBgTsOrGemeinde().then(response => {
                this.userListBgTsOrGemeinde = response;
                this.initialGemeindeUser =
                    EbeguUtil.findUserByNameInList(this.filterPredicate?.verantwortlicherGemeinde, response);
                this.changeDetectorRef.markForCheck();
            });
        } else {
            this.benutzerRS.getAllBenutzerBgOrGemeinde().then(response => {
                this.userListBgOrGemeinde = response;
                this.initialBgGemeindeUser =
                    EbeguUtil.findUserByNameInList(this.filterPredicate?.verantwortlicherBG, response);
                this.changeDetectorRef.markForCheck();
            });
            this.benutzerRS.getAllBenutzerTsOrGemeinde().then(response => {
                this.userListTsOrGemeinde = response;
                this.initialTsGemeindeUser =
                    EbeguUtil.findUserByNameInList(this.filterPredicate?.verantwortlicherTS, response);
                this.changeDetectorRef.markForCheck();
            });
        }

        this.initBenutzerListGemeindeAntraege();
    }

    public isPendenzGemeindeRolle(): boolean {
        return this.pendenz && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles());
    }

    private initStateStores(): void {
        if (!this.stateStoreId) {
            return;
        }
        this.sortId = `${this.stateStoreId}-sort`;
        this.filterId = `${this.stateStoreId}-filter`;
        this.searchInaktivePeriodenId = `${this.stateStoreId}-searchInaktivePerioden`;

        this.transitionService.onStart({exiting: this.uiRouterGlobals.$current.name}, () => {
            if (this.sort.predicate) {
                this.stateStore.store(this.sortId, this.sort);
            } else {
                this.stateStore.delete(this.sortId);
                this.stateStore.delete(this.filterId);
            }

            this.stateStore.store(this.filterId, this.filterPredicate);
            this.stateStore.store(this.searchInaktivePeriodenId, {searchInaktivePerioden: this.searchInaktivePerioden});
        });

    }

    public getVerantwortlicheBgAndTs(antrag: DVAntragListItem): string {
        const verantwortliche: string[] = [];
        if (EbeguUtil.isNotNullOrUndefined(antrag.verantwortlicheBG)) {
            verantwortliche.push(antrag.verantwortlicheBG);
        }
        if (EbeguUtil.isNotNullOrUndefined(antrag.verantwortlicheTS)) {
            verantwortliche.push(antrag.verantwortlicheTS);
        }
        return verantwortliche.join(', ');
    }

    public resetFilter(): void {
        this.initFilter();
        this.applyFilter();
        this.initialGemeindeUser = new TSBenutzerNoDetails(this.initialGemeindeUser?.vorname,
            this.initialGemeindeUser?.nachname,
            this.initialGemeindeUser?.username,
            this.initialGemeindeUser?.gemeindeIds);
        this.initialBgGemeindeUser = new TSBenutzerNoDetails(this.initialBgGemeindeUser?.vorname,
            this.initialBgGemeindeUser?.nachname,
            this.initialBgGemeindeUser?.username,
            this.initialBgGemeindeUser?.gemeindeIds);
        this.initialTsGemeindeUser = new TSBenutzerNoDetails(this.initialTsGemeindeUser?.vorname,
            this.initialTsGemeindeUser?.nachname,
            this.initialTsGemeindeUser?.username,
            this.initialTsGemeindeUser?.gemeindeIds);

    }

    private initBenutzerListGemeindeAntraege(): void {
        if (this.hiddenColumns.includes('verantwortlicherGemeindeantraege')) {
            return;
        }

        this.benutzerRS.getAllActiveBenutzerMandant().then(response => {
            this.userListGemeindeantraege = response;
            this.changeDetectorRef.markForCheck();
        });
    }

    public changeSearchInaktivePerioden(searchInaktivenPerioden: boolean): void {
        this.searchInaktivePerioden = searchInaktivenPerioden;
        this.updateGesuchsperiodenList();
        this.loadData();
    }

    // wenn externe daten in diesen Component eingegeben werden,
    // dann zeigen wir diese Checkbox nicht, weil sie nur für die Abruf der Daten
    // über loadData() innerhalb dieses Components relevant ist.
    public showSearchInaktivePerioden(): boolean {
        return EbeguUtil.isNullOrUndefined(this.data$);
    }
}

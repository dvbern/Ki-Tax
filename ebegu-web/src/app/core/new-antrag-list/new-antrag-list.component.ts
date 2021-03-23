/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {
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
    ViewEncapsulation,
} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, forkJoin, from, Observable, of, Subject} from 'rxjs';
import {map, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SearchRS} from '../../../gesuch/service/searchRS.rest';
import {getTSAntragStatusValuesByRole, TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {getNormalizedTSAntragTypValues, TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {
    getTSBetreuungsangebotTypValuesForMandant,
    TSBetreuungsangebotTyp,
} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../../models/TSAntragSearchresultDTO';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitution} from '../../../models/TSInstitution';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DVAntragListFilter} from '../../shared/interfaces/DVAntragListFilter';
import {DVAntragListItem} from '../../shared/interfaces/DVAntragListItem';
import {DVPaginationEvent} from '../../shared/interfaces/DVPaginationEvent';
import {ErrorService} from '../errors/service/ErrorService';
import {LogFactory} from '../logging/LogFactory';
import {BenutzerRS} from '../service/benutzerRS.rest';
import {GesuchsperiodeRS} from '../service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../service/institutionRS.rest';

const LOG = LogFactory.createLog('DVAntragListController');

@Component({
    selector: 'dv-new-antrag-list',
    templateUrl: './new-antrag-list.component.html',
    styleUrls: ['./new-antrag-list.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    // we need this to overwrite angular material styles
    encapsulation: ViewEncapsulation.None,
})
export class NewAntragListComponent implements OnInit, OnDestroy, OnChanges {

    @ViewChild(MatPaginator) public paginator: MatPaginator;
    @ViewChild(MatTable) private readonly table: MatTable<DVAntragListItem>;

    /**
     * Emits when the user clicks on a row
     */
    @Output() public readonly rowClicked: EventEmitter<{ antrag: TSAntragDTO, event: MouseEvent }> = new EventEmitter<any>();

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
     * 'dokumenteHochgeladen',
     * 'angebote',
     * 'institutionen',
     * 'verantwortlicheTS',
     * 'verantwortlicheBG',
     * 'verantwortlicheGemeinde',
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
        predicate?: string,
        reverse?: boolean
    }> = new EventEmitter<{
        predicate?: string,
        reverse?: boolean
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
    public totalItems: number = 0;

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
        'dokumenteHochgeladen-filter',
        'angebote-filter',
        'institutionen-filter',
        'verantwortlicheTS-filter',
        'verantwortlicheBG-filter',
        'verantwortlicheGemeinde-filter',
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
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
        'verantwortlicheGemeinde',
    ];

    public displayedColumns: string[];

    private filterPredicate: DVAntragListFilter = {};

    private readonly unsubscribe$ = new Subject<void>();
    /**
     * Filter change should not be triggered when user is still typing. Filter change is triggered
     * after user stopped typing for timeoutMS milliseconds
     * We use 700ms because community proposes 500ms as a starting value
     * and we add some more extra for slow typers
     */
    private keyupTimeout: NodeJS.Timeout;

    private readonly timeoutMS = 700;
    private readonly sort: {
        predicate?: string,
        reverse?: boolean
    } = {};

    public paginationItems: number[];
    public userListBgTsOrGemeinde: TSBenutzerNoDetails[];
    public userListTsOrGemeinde: TSBenutzerNoDetails[];
    public userListBgOrGemeinde: TSBenutzerNoDetails[];

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly benutzerRS: BenutzerRS,
    ) {
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
        this.initFilter();
        this.initDisplayedColumns();
        this.initTable();
        this.initBenutzerLists();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.hiddenColumns) {
            this.updateColumns();
        }

        // tslint:disable-next-line:early-exit
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
        }
        this.updateColumns();
    }

    private updateColumns(): void {
        this.displayedColumns = this.allColumns.filter(column => !this.hiddenColumns.includes(column));
        this.filterColumns = this.displayedColumns.map(column => `${column}-filter`);
    }

    public updateInstitutionenList(): void {
        this.institutionRS.getInstitutionenReadableForCurrentBenutzer().then(response => {
            this.allInstitutionen = response;
            this.institutionenList$.next(this.allInstitutionen);
        });
    }

    public updateGesuchsperiodenList(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then(response => {
            response.forEach(gesuchsperiode => {
                this.gesuchsperiodenList.push(gesuchsperiode.gesuchsperiodeString);
            });
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(gemeinden => {
                    this.gemeindenList = gemeinden;
                },
                err => LOG.error(err),
            );
    }

    private initFilter(): void {
        if (this.initialFilter) {
            this.filterPredicate = {...this.filterPredicate, ...this.initialFilter};
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
                start: this.page * this.pageSize,
            },
            search: {
                predicateObject: this.filterPredicate,
            },
            sort: this.sort,
        };
        const dataToLoad$ = this.data$ ?
            this.data$ :
            from(this.searchRS.searchAntraege(body)).pipe(map((result: TSAntragSearchresultDTO) => {
                this.totalItems = result.totalResultSize;
                return result.antragDTOs.map(antragDto => {
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
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG,
                        hasBesitzer: () => antragDto.hasBesitzer(),
                    };
                });
            }));

        dataToLoad$.subscribe((result: DVAntragListItem[]) => {
            this.datasource.data = result;
            this.updatePagination();
            // TODO: we need this because the angualarJS Service returns an IPromise. Angular does not detect changes in
            //  these since they are not zone-aware. Remove once the service is migrated
            this.changeDetectorRef.markForCheck();
        }, error => {
            this.translate.get('DATA_RETRIEVAL_ERROR', error).subscribe(message => {
                this.errorService.addMesageAsError(message);
            }, translateError => console.error('Could not load translation', translateError));
        });
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
                pageSize: this.pageSize,
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
                this.allInstitutionen,
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
            .pipe(map(translatedAngebote => translatedAngebote.join(', '),
            ));
    }

    public getAntragTypBezeichnung(element: any): string {
        let bezeichnung = this.translate.instant(element.antragTyp);
        if (element?.laufNummer > 0) {
            bezeichnung += ` ${element.laufNummer}`;
        }

        return bezeichnung;
    }

    public findUserByNameInList(name: string, list: TSBenutzerNoDetails[]): TSBenutzerNoDetails | null {
        if (!name || !list) {
            return null;
        }
        return list.find(user => user.getFullName() === name);
    }

    private initBenutzerLists(): void {
        if (this.isPendenzGemeindeRolle()) {
            this.benutzerRS.getAllBenutzerBgTsOrGemeinde().then(response => {
                this.userListBgTsOrGemeinde = response;
                this.changeDetectorRef.markForCheck();
            });
        } else {
            this.benutzerRS.getAllBenutzerBgOrGemeinde().then(response => {
                this.userListBgOrGemeinde = response;
                this.changeDetectorRef.markForCheck();
            });
            this.benutzerRS.getAllBenutzerTsOrGemeinde().then(response => {
                this.userListTsOrGemeinde = response;
                this.changeDetectorRef.markForCheck();
            });
        }
    }

    public isPendenzGemeindeRolle(): boolean {
        return this.pendenz && this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOnlyRoles());
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
}

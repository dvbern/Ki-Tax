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
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PageEvent} from '@angular/material/paginator';
import {MatSort, MatSortHeader, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {TransitionService} from '@uirouter/angular';
import {StateService, UIRouterGlobals} from '@uirouter/core';
import {from, Observable, of, Subject} from 'rxjs';
import {map, mergeMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSPagination} from '../../../models/dto/TSPagination';
import {DVErrorMessageCallback} from '../../../models/DVErrorMessageCallback';
import {
    getTSMitteilungsStatusForFilter,
    TSMitteilungStatus
} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTyp} from '../../../models/enums/TSMitteilungTyp';
import {TSMitteilungTypes} from '../../../models/enums/TSMitteilungTypes';
import {TSRole} from '../../../models/enums/TSRole';
import {TSVerantwortung} from '../../../models/enums/TSVerantwortung';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../models/TSBenutzerNoDetails';
import {TSBetreuungsmitteilung} from '../../../models/TSBetreuungsmitteilung';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSMitteilung} from '../../../models/TSMitteilung';
import {TSMtteilungSearchresultDTO} from '../../../models/TSMitteilungSearchresultDTO';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgConfirmDialogComponent} from '../../core/component/dv-ng-confirm-dialog/dv-ng-confirm-dialog.component';
import {DvNgMitteilungResultDialogComponent} from '../../core/component/dv-ng-mitteilung-result-dialog/dv-ng-mitteilung-result-dialog.component';
import {TSDemoFeature} from '../../core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorServiceX} from '../../core/errors/service/ErrorServiceX';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {DemoFeatureRS} from '../../core/service/demoFeatureRS.rest';
import {MitteilungRS} from '../../core/service/mitteilungRS.rest';
import {DVPosteingangFilter} from '../../shared/interfaces/DVPosteingangFilter';
import {StateStoreService} from '../../shared/services/state-store.service';
import {PosteingangService} from '../service/posteingang.service';

const LOG = LogFactory.createLog('PosteingangViewComponent');

@Component({
    selector: 'posteingang-view',
    templateUrl: './posteingang-view.component.html',
    styleUrls: ['./posteingang-view.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PosteingangViewComponent
    implements OnInit, OnDestroy, AfterViewInit
{
    @ViewChild(MatSort) private readonly matSort: MatSort;

    private readonly log: Log = LogFactory.createLog(
        'PosteingangViewComponent'
    );

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
        'actions'
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
        'actions-filter'
    ];

    private readonly hiddenColumnsUDInstituion: string[] = [
        'empfaenger',
        'empfaengerVerantwortung'
    ];

    private readonly hiddenColumnsUD: string[] = ['familienName'];

    // Liste die im Gui angezeigt wird
    public displayedColumns: string[];
    public displayedCollection: MatTableDataSource<TSMitteilung>;
    public pagination: TSPagination = new TSPagination();
    public page: number = 0;
    public pageSize: any = 20;
    public totalItem: number = 0;
    public totalResultCount: number = 0;
    // Muss hier gespeichert werden, damit es fuer den Aufruf ab "Inkl.Erledigt"-Checkbox vorhanden ist
    public myTableFilterState: any;

    public itemsByPage: number = 20;
    public numberOfPages: number = 1;
    public selectedVerantwortung: string;
    public includeClosed: boolean = false;
    public gemeindenList: Array<TSGemeinde> = [];
    public paginationItems: number[];
    public initialEmpfaenger: TSBenutzerNoDetails;
    public filterPredicate: DVPosteingangFilter = {
        messageTypes: [
            TSMitteilungTypes.BETREUUNGSMITTEILUNG,
            TSMitteilungTypes.MITTEILUNG,
            TSMitteilungTypes.NEUEVERANLAGUNGMITTEILUNG
        ]
    };

    // StateStore Properties
    public initialFilter: DVPosteingangFilter = {
        messageTypes: [
            TSMitteilungTypes.BETREUUNGSMITTEILUNG,
            TSMitteilungTypes.MITTEILUNG,
            TSMitteilungTypes.NEUEVERANLAGUNGMITTEILUNG
        ]
    };
    public readonly stateStoreId: string = 'posteingangId';
    private readonly sortId = 'posteingangId-sort';
    private readonly filterId = 'posteingangId-filter';
    private readonly sort: {
        predicate?: string;
        reverse?: boolean;
    } = {};

    public readonly mutationsMeldungDemoFeature =
        TSDemoFeature.ALLE_MUTATIONSMELDUNGEN_VERFUEGEN;

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
        private readonly posteingangService: PosteingangService,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorServiceX,
        private readonly demoFeatureRS: DemoFeatureRS
    ) {}

    public ngOnInit(): void {
        this.updateGemeindenList();
        this.initDisplayedColumns();
        this.initSort();
        this.initFilter().subscribe(
            filter => {
                this.filterPredicate = filter;
                this.benutzerRS
                    .getAllBenutzerBgTsOrGemeinde()
                    .then(response => {
                        this.initialEmpfaenger = EbeguUtil.findUserByNameInList(
                            filter?.empfaenger,
                            response
                        );
                        this.changeDetectorRef.markForCheck();
                    });
                this.passFilterToServer();
            },
            error => LOG.error(error)
        );
    }

    public ngAfterViewInit(): void {
        this.displayedCollection = new MatTableDataSource<TSMitteilung>([]);
        this.initMatSort();
    }

    public ngOnDestroy(): void {
        this.storeFilterSortStates();
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    public addZerosToFallNummer(fallnummer: number): string {
        return EbeguUtil.addZerosToFallNummer(fallnummer);
    }

    public gotoMitteilung(mitteilung: TSMitteilung): void {
        this.$state.go('mitteilungen.view', {
            dossierId: mitteilung.dossier.id,
            fallId: mitteilung.dossier.fall.id
        });
    }

    private updateGemeindenList(): void {
        this.gemeindeRS
            .getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                gemeinden => {
                    this.gemeindenList = gemeinden;
                    this.gemeindenList.sort((a, b) =>
                        a.name.localeCompare(b.name)
                    );
                },
                err => this.log.error(err)
            );
    }

    public getVerantwortungList(): Array<string> {
        return [
            TSVerantwortung.VERANTWORTUNG_BG,
            TSVerantwortung.VERANTWORTUNG_TS
        ];
    }

    public getMitteilungsStatus(): Array<TSMitteilungStatus> {
        return getTSMitteilungsStatusForFilter();
    }

    public clickedIncludeClosed(includeClosed: boolean): void {
        this.includeClosed = includeClosed;
        this.passFilterToServer();
    }

    private passFilterToServer(): void {
        const body = {
            pagination: {
                number: this.pageSize,
                start: this.page * this.pageSize
            },
            search: {
                predicateObject: this.filterPredicate
            },
            sort: this.sort
        };
        const dataToLoad$ = from(
            this.mitteilungRS.searchMitteilungen(body, this.includeClosed)
        ).pipe(map((result: TSMtteilungSearchresultDTO) => result));

        dataToLoad$.subscribe(
            (result: TSMtteilungSearchresultDTO) => {
                this.setResult(result);
            },
            err => this.log.error(err)
        );
    }

    private setResult(result: TSMtteilungSearchresultDTO): void {
        if (!result) {
            return;
        }
        this.displayedCollection.data = [].concat(result.mitteilungen);
        this.totalItem = result.totalResultSize ? result.totalResultSize : 0;
        this.totalResultCount = this.totalItem;
        this.updatePagination();
        this.changeDetectorRef.markForCheck();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isSozialdienst(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getSozialdienstRolle()
        );
    }

    public isSozialdienstOrInstitution(): boolean {
        return (
            this.isSozialdienst() ||
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            )
        );
    }

    public isSuperAdminOrGemeinde(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtRole()
        );
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
        this.filterPredicate.empfaenger = empfaenger
            ? empfaenger.getFullName()
            : null;
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
        this.sort.predicate =
            sortEvent.direction.length > 0 ? sortEvent.active : null;
        this.sort.reverse = sortEvent.direction === 'asc';
        this.passFilterToServer();
    }

    private updatePagination(): void {
        this.paginationItems = [];
        for (
            let i = Math.max(1, this.page - 4);
            i <=
            Math.min(Math.ceil(this.totalItem / this.pageSize), this.page + 5);
            i++
        ) {
            this.paginationItems.push(i);
        }
    }

    private initFilter(): Observable<DVPosteingangFilter> {
        const initial =
            this.filterId && this.stateStore.has(this.filterId)
                ? (this.stateStore.get(this.filterId) as DVPosteingangFilter)
                : {...this.initialFilter};

        return of(initial).pipe(
            mergeMap(filter => this.adaptFilterForPrincipal(filter))
        );
    }

    private adaptFilterForPrincipal(
        filter: DVPosteingangFilter
    ): Observable<DVPosteingangFilter> {
        return this.authServiceRS.principal$.pipe(
            map(principal => {
                if (principal.hasOneOfRoles([TSRole.SUPER_ADMIN])) {
                    return filter;
                }
                return this.addEmpfaengerToFilter(filter, principal);
            })
        );
    }

    private addEmpfaengerToFilter(
        filter: DVPosteingangFilter,
        user: TSBenutzer
    ): DVPosteingangFilter {
        filter.empfaenger = user.getFullName();
        return filter;
    }

    private storeFilterSortStates() {
        if (this.sort.predicate) {
            this.stateStore.store(this.sortId, this.sort);
        } else {
            this.stateStore.delete(this.sortId);
            this.stateStore.delete(this.filterId);
        }

        this.stateStore.store(this.filterId, this.filterPredicate);
    }

    private initSort(): void {
        if (this.stateStore.has(this.sortId)) {
            const stored = this.stateStore.get(this.sortId) as {
                predicate?: string;
                reverse?: boolean;
            };
            this.sort.predicate = stored.predicate;
            this.sort.reverse = stored.reverse;
        }
    }

    private initMatSort(): void {
        this.matSort.active = this.sort.predicate;
        this.matSort.direction = this.sort.reverse ? 'asc' : 'desc';
        (
            this.matSort.sortables.get(this.sort.predicate) as MatSortHeader
        )?._setAnimationTransitionState({toState: 'active'});
    }

    private initDisplayedColumns(): void {
        if (!this.isSozialdienstOrInstitution()) {
            this.displayedColumns = this.allColumns;
            return;
        }
        this.displayedColumns = this.allColumns.filter(
            column => !this.hiddenColumnsUDInstituion.includes(column)
        );
        if (this.isSozialdienst()) {
            this.displayedColumns = this.displayedColumns.filter(
                column => !this.hiddenColumnsUD.includes(column)
            );
        }
        this.filterColumns = this.displayedColumns.map(
            column => `${column}-filter`
        );
    }

    public resetFilter(): void {
        this.filterPredicate = this.initialFilter;
        this.initFilter().subscribe(filter => {
            this.adaptFilterForPrincipal(filter);
            this.applyFilter();
        });
    }

    public setUngelesen(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS.setMitteilungUngelesen(mitteilung.id).then(() => {
            this.passFilterToServer();
            this.getMitteilungenCount();
        });
    }

    public setIgnoriert(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS
            .setMitteilungIgnoriert(mitteilung.id)
            .then(() => {
                this.passFilterToServer();
                this.getMitteilungenCount();
            })
            .then(() => {
                const errorMessageCallback = new DVErrorMessageCallback(
                    this.translate.instant('RUECKGAENGIG_MACHEN'),
                    () => this.setGelesen(mitteilung)
                );
                this.errorService.addMesageAsInfo(
                    this.translate.instant('MESSAGE_IGNORED'),
                    errorMessageCallback
                );
            });
    }

    private resetMitteilungRevertInfo() {
        this.errorService.clearAll();
    }

    public setGelesen(mitteilung: TSMitteilung): void {
        this.resetMitteilungRevertInfo();
        this.mitteilungRS.setMitteilungGelesen(mitteilung.id).then(() => {
            this.passFilterToServer();
            this.getMitteilungenCount();
        });
    }

    public isStatusGelesen(mitteilung: TSMitteilung): boolean {
        return mitteilung.mitteilungStatus === TSMitteilungStatus.GELESEN;
    }

    private getMitteilungenCount(): void {
        this.posteingangService.posteingangChanged();
    }

    public alleMutationsmeldungVerfuegen(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            frage: 'ALLE_MUTATIONSMELDUNGEN_BEARBEITEN_FRAGE'
        };
        this.dialog
            .open(DvNgConfirmDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(answer => {
                if (answer !== true) {
                    return;
                }
                dialogConfig.data =
                    this.getOpenTsBetreuungsmitteilungenOfTable();
                dialogConfig.disableClose = true;
                this.dialog
                    .open(DvNgMitteilungResultDialogComponent, dialogConfig)
                    .afterClosed()
                    .subscribe(
                        () => {
                            this.passFilterToServer();
                            this.getMitteilungenCount();
                        },
                        () => {}
                    );
            });
    }

    private getOpenTsBetreuungsmitteilungenOfTable(): TSBetreuungsmitteilung[] {
        return this.displayedCollection.data
            .filter(
                mitteilung =>
                    mitteilung.mitteilungTyp ===
                        TSMitteilungTyp.BETREUUNGSMITTEILUNG &&
                    mitteilung.mitteilungStatus !== TSMitteilungStatus.ERLEDIGT
            )
            .map(mitteilung => mitteilung as TSBetreuungsmitteilung);
    }

    public canSeeMutationsmeldungenAutomatischBearbeiten() {
        //TS-Roles can't see Mutationsmeldungen
        return (
            !this.isSozialdienstOrInstitution() &&
            !this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeTSRoles())
        );
    }

    public canBeIgnored(mitteilung: TSMitteilung): boolean {
        return (
            !mitteilung.isErledigt() &&
            !mitteilung.isIgnoriert() &&
            mitteilung.isNeueVeranlagung()
        );
    }

    public canMitteilungStatusBeReverted(mitteilung: TSMitteilung): boolean {
        return mitteilung.isIgnoriert();
    }
}

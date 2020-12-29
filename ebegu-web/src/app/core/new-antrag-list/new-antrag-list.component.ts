import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component, EventEmitter,
    OnDestroy,
    OnInit, Output,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {Sort} from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, forkJoin, Observable, of, Subject} from 'rxjs';
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
import {LogFactory} from '../logging/LogFactory';
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
export class NewAntragListComponent implements OnInit, OnDestroy {

    @ViewChild(MatPaginator) public paginator: MatPaginator;
    @ViewChild(MatTable) private readonly table: MatTable<Partial<TSAntragDTO>>;

    @Output() public readonly editClicked: EventEmitter<{ antrag: TSAntragDTO, event: Event }> = new EventEmitter<any>();

    public gesuchsperiodenList: Array<string> = [];
    private allInstitutionen: TSInstitution[];
    public institutionenList$: BehaviorSubject<TSInstitution[]> = new BehaviorSubject<TSInstitution[]>([]);
    public gemeindenList: Array<TSGemeinde> = [];

    public datasource: MatTableDataSource<Partial<TSAntragDTO>>;
    public displayedColumns: string[] = [
        'fallNummer',
        'gemeinde',
        'familienName',
        'kinder',
        'antragTyp',
        'periode',
        'aenderungsdatum',
        'status',
        'dokumenteHochgeladen',
        'angebot',
        'institution',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public filterColumns = {
        fallNummer: {
            type: 'input',
            callback: (query: string) => this.filterFall(query),
        },
    };

    private readonly filterPredicate: {
        fallNummer?: string,
        gemeinde?: string,
        familienName?: string,
        kinder?: string,
        antragTyp?: string,
        gesuchsperiodeString?: string,
        eingangsdatum?: string,
        eingangsdatumSTV?: string,
        aenderungsdatum?: string,
        status?: string,
        dokumenteHochgeladen?: boolean,
        angebote?: string,
        institutionen?: string,
        verantwortlicherTS?: string,
        verantwortlicherBG?: string,
        verantwortlicherGemeinde?: string,
    } = {};

    private readonly unsubscribe$ = new Subject<void>();

    public totalItems: number = 0;
    public page: number = 0;
    public pageSize: any = 20;
    private readonly sort: {
        predicate?: string,
        reverse?: boolean
    } = {};
    public paginationItems: number[];

    public constructor(
        private readonly institutionRS: InstitutionRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly searchRS: SearchRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.updateInstitutionenList();
        this.updateGesuchsperiodenList();
        this.updateGemeindenList();
        this.initTable();
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

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
        this.unsubscribe$.complete();
    }

    private initTable(): void {
        this.datasource = new MatTableDataSource<TSAntragDTO>([]);
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
        this.searchRS.searchAntraege(body).then((result: TSAntragSearchresultDTO) => {
            const displayedFaelle: Partial<TSAntragDTO>[] =
                result.antragDTOs.map(antragDto => {
                    return {
                        fallNummer: antragDto.fallNummer,
                        dossierId: antragDto.dossierId,
                        antragId: antragDto.antragId,
                        gemeinde: antragDto.gemeinde,
                        status: antragDto.status,
                        familienName: antragDto.familienName,
                        kinder: antragDto.kinder,
                        antragTyp: antragDto.antragTyp,
                        periode: antragDto.gesuchsperiodeString,
                        aenderungsdatum: antragDto.aenderungsdatum,
                        dokumenteHochgeladen: antragDto.dokumenteHochgeladen,
                        angebote: antragDto.angebote,
                        institutionen: antragDto.institutionen,
                        verantwortlicheTS: antragDto.verantwortlicherTS,
                        verantwortlicheBG: antragDto.verantwortlicherBG,
                        hasBesitzer: antragDto.hasBesitzer
                    };
                });
            this.datasource.data = displayedFaelle;
            this.totalItems = result.totalResultSize;
            this.paginationItems = [];
            for (let i = 1; i <= Math.ceil(this.totalItems / this.pageSize); i++) {
                this.paginationItems.push(i);
            }
            console.log(this.paginationItems);
            // TODO: we need this because the angualarJS Service returns an IPromise. Angular does not detect changes in
            //  these since they are not zone-aware. Remove once the service is migrated
            this.changeDetectorRef.markForCheck();
        });
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.pageSize = pageEvent.pageSize;
        this.loadData();
    }

    public filterFall(query: string): void {
        this.filterPredicate.fallNummer = query.length > 0 ? query : null;
        this.loadData();
    }

    public filterGemeinde(gemeinde: string): void {
        this.filterPredicate.gemeinde = gemeinde;
        this.loadData();
    }

    public filterType(type: string): void {
        this.filterPredicate.antragTyp = type;
        this.loadData();
    }

    public filterPeriode(periode: string): void {
        this.filterPredicate.gesuchsperiodeString = periode;
        this.loadData();
    }

    public filterStatus(state: string): void {
        this.filterPredicate.status = state;
        this.loadData();
    }

    public filterDocumentsUploaded(documentsUploaded: boolean): void {
        this.filterPredicate.dokumenteHochgeladen = documentsUploaded;
        this.loadData();
    }

    public filterAngebot(angebot: string): void {
        this.filterPredicate.angebote = angebot;
        this.loadData();
    }

    public filterVerantwortlicheTS(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherTS = verantwortliche ? verantwortliche.getFullName() : null;
        this.loadData();
    }

    public filterVerantwortlicheBG(verantwortliche: TSBenutzerNoDetails): void {
        this.filterPredicate.verantwortlicherBG = verantwortliche ? verantwortliche.getFullName() : null;
        this.loadData();
    }

    public filterFamilie(query: string): void {
        this.filterPredicate.familienName = query.length > 0 ? query : null;
        this.loadData();
    }

    public filterKinder(query: string): void {
        this.filterPredicate.kinder = query.length > 0 ? query : null;
        this.loadData();
    }

    public filterGeaendert(query: string): void {
        this.filterPredicate.aenderungsdatum = query.length > 0 ? query : null;
        this.loadData();
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
        this.loadData();
    }

    public getAntragTypen(): TSAntragTyp[] {
        return getNormalizedTSAntragTypValues();
    }

    /**
     * Alle TSAntragStatus fuer das Filterdropdown
     */
    public getAntragStatus(): TSAntragStatus[] {
        return getTSAntragStatusValuesByRole(this.authServiceRS.getPrincipalRole());
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
        this.loadData();
    }

    private onEditClicked(antrag: TSAntragDTO, event: Event): void {
        this.editClicked.emit({antrag, event});
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
}

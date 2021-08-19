import {CurrencyPipe} from '@angular/common';
import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {of, Subject} from 'rxjs';
import {filter, switchMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSZahlungsauftragsstatus} from '../../../models/enums/TSZahlungsauftragstatus';
import {TSZahlungslaufTyp} from '../../../models/enums/TSZahlungslaufTyp';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSZahlungsauftrag} from '../../../models/TSZahlungsauftrag';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {DvSimpleTableColumnDefinition} from '../../shared/component/dv-simple-table/dv-simple-table-column-definition';
import {StateStoreService} from '../../shared/services/state-store.service';
import {ZahlungRS} from '../services/zahlungRS.rest';

const LOG = LogFactory.createLog('ZahlungsauftragViewXComponent');

@Component({
    selector: 'zahlungsauftrag-view',
    templateUrl: './zahlungsauftrag-view-x.component.html',
    styleUrls: ['./zahlungsauftrag-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ZahlungsauftragViewXComponent implements OnInit, AfterViewInit {

    @ViewChild(NgForm) public readonly form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;
    @ViewChild(MatPaginator) private readonly paginator: MatPaginator;

    public datasource: MatTableDataSource<any> = new MatTableDataSource<any>([]);
    public data: TSZahlungsauftrag[] = [];

    public zahlungsauftragToEdit: TSZahlungsauftrag;
    public zahlungsAuftraege: TSZahlungsauftrag[] = [];
    public zahlungsAuftraegeFiltered: TSZahlungsauftrag[] = [];

    public zahlungslaufTyp: TSZahlungslaufTyp;
    public beschrieb: string;
    public faelligkeitsdatum: moment.Moment;
    public datumGeneriert: moment.Moment;
    public itemsByPage: number = 12;
    public testMode: boolean = false;
    public minDateForTestlauf: moment.Moment;
    public gemeinde: TSGemeinde;
    public filterGemeinde: TSGemeinde = null;
    // Anzuzeigende Gemeinden fuer den gewaehlten Zahlungslauftyp
    public gemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die normalen Auftraege
    public berechtigteGemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die Mahlzeitenverguenstigungen
    public berechtigteGemeindenMitMahlzeitenList: Array<TSGemeinde> = [];

    public tableColumns: DvSimpleTableColumnDefinition[] = [];

    private readonly unsubscribe$ = new Subject<void>();

    private showMahlzeitenZahlungslaeufe: boolean = false;

    public principal: TSBenutzer;

    public paginationItems: number[];
    public page: number = 0;
    public readonly PAGE_SIZE: number = 20;

    private readonly SORT_STORE_KEY = 'zahlungsauftrag-view-sort';

    public constructor(
        private readonly zahlungRS: ZahlungRS,
        private readonly $state: StateService,
        private readonly downloadRS: DownloadRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly reportRS: ReportRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly translate: TranslateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly uiRouterGlobals: UIRouterGlobals,
        private readonly cd: ChangeDetectorRef,
        private readonly dialog: MatDialog,
        private readonly currency: CurrencyPipe,
        private readonly transition: TransitionService,
        private readonly stateStore: StateStoreService,
        private readonly errorService: ErrorService,
    ) {
    }

    private sortingDataAccessor(data: TSZahlungsauftrag, header: string): string | number {
        switch (header) {
            case 'gemeinde':
                return data.gemeinde.name;
            case 'datumFaellig':
                return data.datumFaellig.valueOf();
            case 'datumGeneriert':
                return data.datumGeneriert.valueOf();
            case 'status':
                return this.getCalculatedStatus(data);
            default:
                return (data as any)[header];
        }
    }

    public ngOnInit(): void {
        const isMahlzeitenzahlungen = EbeguUtil.isNotNullAndTrue(this.uiRouterGlobals.params.isMahlzeitenzahlungen);
        this.zahlungslaufTyp = isMahlzeitenzahlungen
            ? TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER
            : TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
        this.updateZahlungsauftrag();
        this.updateGemeindenList();
        this.updateShowMahlzeitenZahlungslaeufe();
        this.applicationPropertyRS.isZahlungenTestMode().then((response: any) => {
            this.testMode = response;
        });
        this.setupTableColumns();
        this.authServiceRS.principal$.subscribe(user => this.principal = user, error => LOG.error(error));
        this.translate.onDefaultLangChange.subscribe(() => this.setupTableColumns(), (error: any) => LOG.error(error));
        this.transition.onStart({exiting: 'zahlungsauftrag.view'}, () => {
            if (this.sort.active) {
                this.stateStore.store(this.SORT_STORE_KEY, this.sort);
            } else {
                this.stateStore.delete(this.SORT_STORE_KEY);
            }
        });
    }

    public ngAfterViewInit(): void {

        if (this.stateStore.has(this.SORT_STORE_KEY)) {
            const stored = this.stateStore.get(this.SORT_STORE_KEY) as MatSort;
            this.sort.active = stored.active;
            this.sort.direction = stored.direction;
        } else {
            // initial sorting
            this.sort.active = 'datumFaellig';
            this.sort.direction = 'desc';
        }
        (this.sort.sortables.get(this.sort.active) as MatSortHeader)?._setAnimationTransitionState({toState: 'active'});
        this.datasource.sort = this.sort;
        this.datasource.sortingDataAccessor = this.sortingDataAccessor.bind(this);
        this.datasource.paginator = this.paginator;
    }

    private updateZahlungsauftrag(): void {
        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        return this.zahlungRS.getZahlungsauftraegeForRole$(principal.getCurrentRole());
                    }

                    return of([]);
                }),
            )
            .subscribe(
                zahlungsAuftraege => {
                    this.zahlungsAuftraege = zahlungsAuftraege;
                    this.datasource.data = zahlungsAuftraege;
                    this.toggleAuszahlungslaufTyp();
                },
                err => LOG.error(err),
            );
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag): void {
        this.$state.go('zahlung.view', {
            zahlungsauftragId: zahlungsauftrag.id,
            isMahlzeitenzahlungen: this.zahlungslaufTyp === TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER,
        });
    }

    public createZahlungsauftrag(): void {
        if (!this.form.valid) {
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_ERSTELLEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_ERSTELLEN_INFO'),
        };

        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(filter(result => !!result))
            .subscribe(() => {
                this.zahlungRS.createZahlungsauftrag(
                    this.zahlungslaufTyp,
                    this.gemeinde,
                    this.beschrieb,
                    this.faelligkeitsdatum,
                    this.datumGeneriert,
                ).subscribe((response: TSZahlungsauftrag) => {
                        this.errorService.addMesageAsInfo(this.translate.instant('ZAHLUNG_ERSTELLT'));
                        this.zahlungsAuftraege.push(response);
                        this.resetEditZahlungsauftrag();
                        this.resetForm();
                        this.cd.markForCheck();
                    },
                    error => this.errorService.addMesageAsError(
                        error?.error?.translatedMessage || this.translate.instant('ERROR_UNEXPECTED')));
            }, error => LOG.error(error));
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag): Promise<void> {
        const win = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS.getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, true, win);
            })
            .catch(error => {
                this.errorService.addMesageAsError(error?.error?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                win.close();
            }) as Promise<void>;
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS.getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(downloadFile.accessToken, downloadFile.filename, false, win);
            })
            // tslint:disable-next-line:no-identical-functions
            .catch(error => {
                this.errorService.addMesageAsError(error?.error?.translatedMessage || this.translate.instant(
                    'ERROR_UNEXPECTED'));
                win.close();
            });
    }

    // tslint:disable-next-line:no-unused
    public ausloesen(zahlungsauftragId: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_AUSLOESEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_AUSLOESEN_INFO'),
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(result => {   // User confirmed removal
                if (!result) {
                    return;
                }
                this.zahlungRS.zahlungsauftragAusloesen(zahlungsauftragId).subscribe((response: TSZahlungsauftrag) => {
                        const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsAuftraege);
                        if (index > -1) {
                            this.zahlungsAuftraege[index] = response;
                        }
                        EbeguUtil.handleSmarttablesUpdateBug(this.zahlungsAuftraege);
                        this.toggleAuszahlungslaufTyp();
                        this.cd.markForCheck();
                    },
                    error => this.errorService.addMesageAsError(
                        error?.error?.translatedMessage || this.translate.instant('ERROR_UNEXPECTED')));
            }, error => LOG.error(error));
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag): void {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(_zahlungsauftrag: TSZahlungsauftrag): void {
        if (!this.isEditValid()) {
            return;
        }

        this.zahlungRS.updateZahlungsauftrag(
            this.zahlungsauftragToEdit.beschrieb,
            this.zahlungsauftragToEdit.datumFaellig,
            this.zahlungsauftragToEdit.id,
        ).subscribe((response: TSZahlungsauftrag) => {
                const index = EbeguUtil.getIndexOfElementwithID(response, this.zahlungsAuftraege);
                if (index > -1) {
                    this.zahlungsAuftraege[index] = response;
                }
                // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                this.form.form.markAsPristine();
                this.resetEditZahlungsauftrag();
            },
            error => this.errorService.addMesageAsError(error?.error?.translatedMessage || this.translate.instant(
                'ERROR_UNEXPECTED')));
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        return status === TSZahlungsauftragsstatus.ENTWURF;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        return this.zahlungsauftragToEdit?.id === zahlungsauftragId;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return this.zahlungsauftragToEdit.beschrieb
                && this.zahlungsauftragToEdit.beschrieb.length > 0
                && this.zahlungsauftragToEdit.datumFaellig !== null
                && this.zahlungsauftragToEdit.datumFaellig !== undefined;
        }
        return false;
    }

    private resetEditZahlungsauftrag(): void {
        this.zahlungsauftragToEdit = null;
        this.cd.markForCheck();
    }

    public rowClass(zahlungsauftragId: string): string {
        if (this.isEditMode(zahlungsauftragId) && !this.isEditValid()) {
            return 'errorrow';
        }
        return '';
    }

    /**
     * resets all three variables needed to create a Zahlung.
     */
    private resetForm(): void {
        this.beschrieb = undefined;
        this.faelligkeitsdatum = undefined;
        this.datumGeneriert = undefined;
        this.gemeinde = null;
        this.form.form.markAsPristine();
        this.form.form.markAsUntouched();
        this.toggleAuszahlungslaufTyp();
    }

    public getCalculatedStatus(zahlungsauftrag: TSZahlungsauftrag): any {
        if (zahlungsauftrag.status !== TSZahlungsauftragsstatus.BESTAETIGT
            && this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles())
            && zahlungsauftrag.zahlungen.every(zahlung => zahlung.status === TSZahlungsstatus.BESTAETIGT)) {

            return TSZahlungsstatus.BESTAETIGT;
        }
        return zahlungsauftrag.status;
    }

    private updateGemeindenList(): void {
        this.gemeindeRS.getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                gemeinden => {
                    this.berechtigteGemeindenList = gemeinden;
                    this.cd.markForCheck();
                },
                err => LOG.error(err),
            );
    }

    private updateShowMahlzeitenZahlungslaeufe(): void {
        this.showMahlzeitenZahlungslaeufe = false;
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (!this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole())) {
            this.showMahlzeitenZahlungslaeufe = false;
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS.getGemeindenWithMahlzeitenverguenstigungForBenutzer().then(value => {
            if (value.length <= 0) {
                return;
            }
            // Sobald mindestens eine Gemeinde in mindestens einer Gesuchsperiode die
            // Mahlzeiten aktiviert hat, wird der Toggle angezeigt
            this.showMahlzeitenZahlungslaeufe = true;
            this.berechtigteGemeindenMitMahlzeitenList = value;
            this.cd.markForCheck();
        });
    }

    public toggleAuszahlungslaufTyp(): void {
        this.filterGemeinde = null;
        this.filterZahlungsAuftraege();
        this.gemeindenList
            = TSZahlungslaufTyp.GEMEINDE_INSTITUTION === this.zahlungslaufTyp
            ? Array.from(this.berechtigteGemeindenList)
            : Array.from(this.berechtigteGemeindenMitMahlzeitenList);
    }

    public filterZahlungsAuftraege(): void {
        this.page = 0;
        this.zahlungsAuftraegeFiltered =
            this.zahlungsAuftraege.filter(value => value.zahlungslaufTyp === this.zahlungslaufTyp &&
                (!this.filterGemeinde || this.filterGemeinde.id === value.gemeinde.id));
        this.updatePagination(this.zahlungsAuftraegeFiltered);
        this.datasource.data = this.zahlungsAuftraegeFiltered;
    }

    private updatePagination(items: TSZahlungsauftrag[]): void {
        this.paginationItems = [];
        for (let i = Math.max(1, this.page - 4); i <= Math.min(Math.ceil(items.length / this.PAGE_SIZE),
            this.page + 5); i++) {
            this.paginationItems.push(i);
        }
    }

    public showAuszahlungsTypToggle(): boolean {
        return this.showMahlzeitenZahlungslaeufe;
    }

    public getLabelZahlungslaufErstellen(): string {
        return this.translate.instant('BUTTON_' + this.zahlungslaufTyp);
    }

    public showInfotext(): boolean {
        return this.zahlungslaufTyp === TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
    }

    private setupTableColumns(): void {
        this.tableColumns = [
            {
                displayedName: this.translate.instant('ZAHLUNG_GENERIERT'),
                attributeName: 'datumGeneriert',
                displayFunction: (date: moment.Moment) => date.format('DD.MM.YYYY'),
            },
            {
                displayedName: this.translate.instant('GEMEINDE'),
                attributeName: 'gemeinde',
                displayFunction: (gemeinde: TSGemeinde) => gemeinde.name,
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_TOTAL'),
                attributeName: 'betragTotalAuftrag',
                displayFunction: (betrag: number) => this.currency.transform(betrag, '', ''),
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_STATUS'),
                attributeName: 'status',
                displayFunction: (
                    // tslint:disable-next-line:no-unused
                    status: TSZahlungsauftragsstatus,
                    element: TSZahlungsauftrag,
                ) => this.getCalculatedStatus(element),
            },
        ];
    }

    public getColumnsAttributeName(): string[] {
        const allColumnNames = this.tableColumns?.map(column => column.attributeName);
        allColumnNames.splice(0, 0, 'datumFaellig');
        allColumnNames.splice(3, 0, `zahlungPain`, 'zahlungPainExcel');
        allColumnNames.splice(5, 0, `beschrieb`);
        if (this.principal?.hasOneOfRoles(TSRoleUtil.getAdministratorBgGemeindeRoles())) {
            allColumnNames.push('editSave');
            allColumnNames.push('ausloesen');
        }
        return allColumnNames;
    }

    public getDisplayValue(element: any, column: any): string {
        if (column.displayFunction !== undefined) {
            return column.displayFunction(element[column.attributeName], element);
        }
        return element[column.attributeName];
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.paginator.pageIndex = this.page;
        // @ts-ignore Ugly workaround, but otherwise, paginator will not update the data
        this.paginator._emitPageEvent(this.page);
        this.updatePagination(this.zahlungsAuftraegeFiltered);
        this.cd.markForCheck();

    }

    public showForm(): boolean {
        return this.principal?.hasOneOfRoles(TSRoleUtil.getAdministratorBgGemeindeRoles());
    }

    public showGemeindeFilter(): boolean {
        return this.principal?.hasRole(TSRole.SUPER_ADMIN);
    }
}

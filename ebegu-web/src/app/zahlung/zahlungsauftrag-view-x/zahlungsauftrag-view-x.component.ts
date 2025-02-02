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
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {
    MatSort,
    MatSortHeader,
    Sort,
    SortDirection
} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {TranslateService} from '@ngx-translate/core';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import * as moment from 'moment';
import {of, Subject} from 'rxjs';
import {filter, switchMap, takeUntil} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSZahlungsauftragsstatus} from '../../../models/enums/TSZahlungsauftragstatus';
import {TSZahlungslaufTyp} from '../../../models/enums/TSZahlungslaufTyp';
import {TSZahlungsstatus} from '../../../models/enums/TSZahlungsstatus';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSPaginationResultDTO} from '../../../models/TSPaginationResultDTO';
import {TSPublicAppConfig} from '../../../models/TSPublicAppConfig';
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
import {TSGeneratedDokumentTyp} from '../../../models/enums/TSGeneratedDokumentTyp';

const LOG = LogFactory.createLog('ZahlungsauftragViewXComponent');

@Component({
    selector: 'zahlungsauftrag-view',
    templateUrl: './zahlungsauftrag-view-x.component.html',
    styleUrls: ['./zahlungsauftrag-view-x.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ZahlungsauftragViewXComponent
    implements OnInit, AfterViewInit, OnDestroy
{
    @ViewChild(NgForm) public readonly form: NgForm;
    @ViewChild(MatSort) public sort: MatSort;
    @ViewChild(MatPaginator) private readonly paginator: MatPaginator;

    public datasource: MatTableDataSource<any> = new MatTableDataSource<any>(
        []
    );
    public data: TSZahlungsauftrag[] = [];

    public zahlungsauftragToEdit: TSZahlungsauftrag;
    public zahlungsAuftraege: TSZahlungsauftrag[] = [];

    public zahlungslaufTyp: TSZahlungslaufTyp;
    public beschrieb: string;
    public faelligkeitsdatum: moment.Moment;
    public datumGeneriert: moment.Moment;
    public itemsByPage: number = 12;
    public testMode: boolean = false;
    public checkboxAuszahlungInZukunft: boolean = false;
    public auszahlungInZukunft: boolean = false;
    public minDateForTestlauf: moment.Moment;
    public gemeinde: TSGemeinde;
    // Anzuzeigende Gemeinden fuer den gewaehlten Zahlungslauftyp
    public gemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die normalen Auftraege
    public berechtigteGemeindenList: Array<TSGemeinde> = [];
    // Alle Gemeinden fuer die ich berechtigt bin fuer die Mahlzeitenverguenstigungen
    public berechtigteGemeindenMitMahlzeitenList: Array<TSGemeinde> = [];

    public tableColumns: DvSimpleTableColumnDefinition[] = [];

    private readonly unsubscribe$ = new Subject<void>();

    private hasMahlzeitenZahlungslaeufe: boolean = false;
    private hasAuszahlungAnEltern: boolean = false;

    public principal: TSBenutzer;

    public filterGemeinde: TSGemeinde = null;
    public paginationItems: number[];
    public page: number = 0;
    public readonly PAGE_SIZE: number = 20;
    public totalResult: number = 0;
    public hasInfomaZahlung: boolean = false;

    public readonly DEFAULT_SORT = {
        active: 'datumFaellig',
        direction: 'desc'
    };
    private readonly SORT_STORE_KEY = 'zahlungsauftrag-view-sort';
    private readonly FILTER_STORE_KEY = 'zahlungsauftrag-view-filter';

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
        private readonly transition: TransitionService,
        private readonly stateStore: StateStoreService,
        private readonly errorService: ErrorService
    ) {}

    public ngOnInit(): void {
        const isMahlzeitenzahlungen = EbeguUtil.isNotNullAndTrue(
            this.uiRouterGlobals.params.isMahlzeitenzahlungen
        );
        this.zahlungslaufTyp = isMahlzeitenzahlungen
            ? TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER
            : TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
        this.updateHasMahlzeitenZahlungslaeufe();
        this.updateHasAuszahlungAnElternZahlungslaeufe();
        this.applicationPropertyRS
            .isZahlungenTestMode()
            .then((response: any) => {
                this.testMode = response;
            });
        this.applicationPropertyRS
            .getCheckboxAuszahlungInZukunft()
            .then((response: any) => {
                this.checkboxAuszahlungInZukunft = response;
                this.auszahlungInZukunft = this.checkboxAuszahlungInZukunft;
            });
        this.setupTableColumns();
        this.authServiceRS.principal$.subscribe(
            user => (this.principal = user),
            error => LOG.error(error)
        );
        this.translate.onDefaultLangChange.subscribe(
            () => this.setupTableColumns(),
            (error: any) => LOG.error(error)
        );
        this.transition.onStart({exiting: 'zahlungsauftrag.view'}, () => {
            if (this.sort.active) {
                this.stateStore.store(this.SORT_STORE_KEY, this.sort);
            } else {
                this.stateStore.delete(this.SORT_STORE_KEY);
            }
            if (this.filterGemeinde) {
                this.stateStore.store(
                    this.FILTER_STORE_KEY,
                    this.filterGemeinde
                );
            } else {
                this.stateStore.delete(this.FILTER_STORE_KEY);
            }
        });
    }

    public ngAfterViewInit(): void {
        this.initSort();
        this.initGemeindenListAndFilter();
        this.updateZahlungsauftrag();
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    private initSort(): void {
        if (this.stateStore.has(this.SORT_STORE_KEY)) {
            const stored = this.stateStore.get(this.SORT_STORE_KEY) as MatSort;
            this.sort.active = stored.active;
            this.sort.direction = stored.direction;
        } else {
            this.sort.active = this.DEFAULT_SORT.active;
            this.sort.direction = this.DEFAULT_SORT.direction as SortDirection;
        }
        (
            this.sort.sortables.get(this.sort.active) as MatSortHeader
        )?._setAnimationTransitionState({toState: 'active'});
    }

    public updateZahlungsauftrag(): void {
        this.authServiceRS.principal$
            .pipe(takeUntil(this.unsubscribe$))
            .pipe(
                switchMap(principal => {
                    if (principal) {
                        return this.zahlungRS.getZahlungsauftraegeForRole$(
                            principal.getCurrentRole(),
                            this.sort,
                            this.page,
                            this.PAGE_SIZE,
                            this.filterGemeinde,
                            this.zahlungslaufTyp
                        );
                    }
                    return of(
                        new TSPaginationResultDTO<TSZahlungsauftrag>([], 0)
                    );
                })
            )
            .subscribe(
                result => {
                    this.zahlungsAuftraege = result.resultList;
                    this.datasource.data = result.resultList;
                    this.updatePagination(result.totalResultSize);
                },
                err => LOG.error(err)
            );
    }

    public gotoZahlung(zahlungsauftrag: TSZahlungsauftrag): void {
        this.$state.go('zahlung.view', {
            zahlungsauftragId: zahlungsauftrag.id,
            isMahlzeitenzahlungen:
                this.zahlungslaufTyp ===
                TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER
        });
    }

    public createZahlungsauftrag(): void {
        if (!this.form.valid) {
            return;
        }

        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_ERSTELLEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_ERSTELLEN_INFO')
        };

        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .pipe(filter(result => !!result))
            .subscribe(
                () => {
                    this.errorService.addMesageAsInfo(
                        this.translate.instant('ZAHLUNG_AUSGELOEST_INFO')
                    );
                    this.zahlungRS
                        .createZahlungsauftrag(
                            this.zahlungslaufTyp,
                            this.gemeinde,
                            this.beschrieb,
                            this.faelligkeitsdatum,
                            this.datumGeneriert,
                            this.auszahlungInZukunft
                        )
                        .subscribe(
                            (response: TSZahlungsauftrag) => {
                                this.errorService.clearAll();
                                this.errorService.addMesageAsInfo(
                                    this.translate.instant('ZAHLUNG_ERSTELLT')
                                );
                                this.zahlungsAuftraege.push(response);
                                this.resetEditZahlungsauftrag();
                                this.resetForm();
                                this.updateZahlungsauftrag();
                                this.cd.markForCheck();
                            },
                            error => LOG.error(error)
                        );
                },
                error => LOG.error(error)
            );
    }

    public downloadPain(zahlungsauftrag: TSZahlungsauftrag): Promise<void> {
        return this.downloadZahlungsfile(
            zahlungsauftrag,
            TSGeneratedDokumentTyp.PAIN001
        );
    }

    public downloadInfoma(zahlungsauftrag: TSZahlungsauftrag): Promise<void> {
        return this.downloadZahlungsfile(
            zahlungsauftrag,
            TSGeneratedDokumentTyp.INFOMA
        );
    }

    private downloadZahlungsfile(
        zahlungsauftrag: TSZahlungsauftrag,
        typ: TSGeneratedDokumentTyp
    ) {
        const win = this.downloadRS.prepareDownloadWindow();
        return this.downloadRS
            .getPain001AccessTokenGeneratedDokument(zahlungsauftrag.id, typ)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    true,
                    win
                );
            })
            .catch(error => {
                this.errorService.addMesageAsError(
                    error?.error?.translatedMessage ||
                        this.translate.instant('ERROR_UNEXPECTED')
                );
                win.close();
            }) as Promise<void>;
    }

    public downloadAllDetails(zahlungsauftrag: TSZahlungsauftrag): void {
        const win = this.downloadRS.prepareDownloadWindow();
        this.reportRS
            .getZahlungsauftragReportExcel(zahlungsauftrag.id)
            .then((downloadFile: TSDownloadFile) => {
                this.downloadRS.startDownload(
                    downloadFile.accessToken,
                    downloadFile.filename,
                    false,
                    win
                );
            })
            .catch(error => {
                this.errorService.addMesageAsError(
                    error?.error?.translatedMessage ||
                        this.translate.instant('ERROR_UNEXPECTED')
                );
                win.close();
            });
    }

    public ausloesen(zahlungsauftragId: string): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: this.translate.instant('ZAHLUNG_AUSLOESEN_CONFIRM'),
            text: this.translate.instant('ZAHLUNG_AUSLOESEN_INFO')
        };
        this.dialog
            .open(DvNgRemoveDialogComponent, dialogConfig)
            .afterClosed()
            .subscribe(
                result => {
                    // User confirmed removal
                    if (!result) {
                        return;
                    }
                    this.zahlungRS
                        .zahlungsauftragAusloesen(zahlungsauftragId)
                        .subscribe(
                            (response: TSZahlungsauftrag) => {
                                const index = EbeguUtil.getIndexOfElementwithID(
                                    response,
                                    this.zahlungsAuftraege
                                );
                                if (index > -1) {
                                    this.zahlungsAuftraege[index] = response;
                                }
                                this.updateZahlungsauftrag();
                                this.cd.markForCheck();
                            },
                            error =>
                                this.errorService.addMesageAsError(
                                    error?.error?.translatedMessage ||
                                        this.translate.instant(
                                            'ERROR_UNEXPECTED'
                                        )
                                )
                        );
                },
                error => LOG.error(error)
            );
    }

    public edit(zahlungsauftrag: TSZahlungsauftrag): void {
        this.zahlungsauftragToEdit = zahlungsauftrag;
    }

    public save(): void {
        if (!this.isEditValid()) {
            return;
        }

        this.zahlungRS
            .updateZahlungsauftrag(
                this.zahlungsauftragToEdit.beschrieb,
                this.zahlungsauftragToEdit.datumFaellig,
                this.zahlungsauftragToEdit.id
            )
            .subscribe(
                (response: TSZahlungsauftrag) => {
                    const index = EbeguUtil.getIndexOfElementwithID(
                        response,
                        this.zahlungsAuftraege
                    );
                    if (index > -1) {
                        this.zahlungsAuftraege[index] = response;
                    }
                    // nach dem es gespeichert wird, muessen wir das Form wieder auf clean setzen
                    this.form.form.markAsPristine();
                    this.resetEditZahlungsauftrag();
                },
                error =>
                    this.errorService.addMesageAsError(
                        error?.error?.translatedMessage ||
                            this.translate.instant('ERROR_UNEXPECTED')
                    )
            );
    }

    public isEditable(status: TSZahlungsauftragsstatus): boolean {
        return status === TSZahlungsauftragsstatus.ENTWURF;
    }

    public isEditMode(zahlungsauftragId: string): boolean {
        return this.zahlungsauftragToEdit?.id === zahlungsauftragId;
    }

    public isEditValid(): boolean {
        if (this.zahlungsauftragToEdit) {
            return (
                this.zahlungsauftragToEdit.beschrieb &&
                this.zahlungsauftragToEdit.beschrieb.length > 0 &&
                this.zahlungsauftragToEdit.datumFaellig !== null &&
                this.zahlungsauftragToEdit.datumFaellig !== undefined
            );
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
        this.form.resetForm();
    }

    public getCalculatedStatus(zahlungsauftrag: TSZahlungsauftrag): any {
        if (
            zahlungsauftrag.status !== TSZahlungsauftragsstatus.BESTAETIGT &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
            ) &&
            zahlungsauftrag.zahlungen.every(
                zahlung => zahlung.status === TSZahlungsstatus.BESTAETIGT
            )
        ) {
            return TSZahlungsstatus.BESTAETIGT;
        }
        return zahlungsauftrag.status;
    }

    private initGemeindenListAndFilter(): void {
        this.gemeindeRS
            .getGemeindenForPrincipal$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                gemeinden => {
                    this.berechtigteGemeindenList = gemeinden;
                    this.berechtigteGemeindenList.sort((a, b) =>
                        a.name.localeCompare(b.name)
                    );
                    this.toggleAuszahlungslaufTyp();
                    this.initFilterFromStore();
                    this.cd.markForCheck();
                },
                err => LOG.error(err)
            );
    }

    private initFilterFromStore(): void {
        if (this.stateStore.has(this.FILTER_STORE_KEY)) {
            this.filterGemeinde = this.stateStore.get(
                this.FILTER_STORE_KEY
            ) as TSGemeinde;
            this.updateZahlungsauftrag();
        }
    }

    private updateHasMahlzeitenZahlungslaeufe(): void {
        this.hasMahlzeitenZahlungslaeufe = false;
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        ) {
            this.hasMahlzeitenZahlungslaeufe = false;
            return;
        }
        // Abfragen, welche meiner berechtigten Gemeinden Mahlzeitenverguenstigung haben
        this.gemeindeRS
            .getGemeindenWithMahlzeitenverguenstigungForBenutzer()
            .then(value => {
                if (value.length <= 0) {
                    return;
                }
                // Sobald mindestens eine Gemeinde in mindestens einer Gesuchsperiode die
                // Mahlzeiten aktiviert hat, wird der Toggle angezeigt
                this.hasMahlzeitenZahlungslaeufe = true;
                this.berechtigteGemeindenMitMahlzeitenList = value;
                this.cd.markForCheck();
            });
    }

    private updateHasAuszahlungAnElternZahlungslaeufe(): void {
        this.hasAuszahlungAnEltern = false;
        // Grundsaetzliche nur fuer Superadmin und Gemeinde-Mitarbeiter
        if (
            !this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            )
        ) {
            this.hasAuszahlungAnEltern = false;
            return;
        }
        this.applicationPropertyRS
            .getPublicPropertiesCached()
            .then((response: TSPublicAppConfig) => {
                this.hasAuszahlungAnEltern = response.auszahlungAnEltern;
                this.hasInfomaZahlung = response.infomaZahlungen;
            });
    }

    public toggleAuszahlungslaufTyp(): void {
        this.filterGemeinde = null;
        this.gemeindenList =
            TSZahlungslaufTyp.GEMEINDE_INSTITUTION === this.zahlungslaufTyp ||
            !this.hasMahlzeitenZahlungslaeufe
                ? Array.from(this.berechtigteGemeindenList)
                : Array.from(this.berechtigteGemeindenMitMahlzeitenList);
        this.totalResult = 0;
        this.page = 0;
        this.updateZahlungsauftrag();
    }

    public getMsgKeyForToggleRight(): string {
        if (this.hasMahlzeitenZahlungslaeufe) {
            return 'GEMEINDE_MAHLZEITENVERGUENSTIGUNGEN';
        }
        return 'GEMEINDE_ANTRAGSTELLER';
    }

    public sortData($event: Sort): void {
        this.sort.active = $event.active;
        this.sort.direction = $event.direction;
        this.updateZahlungsauftrag();
    }

    private updatePagination(totalResultSize: number): void {
        this.totalResult = totalResultSize;
        this.paginationItems = [];
        for (
            let i = Math.max(1, this.page - 4);
            i <=
            Math.min(
                Math.ceil(totalResultSize / this.PAGE_SIZE),
                this.page + 5
            );
            i++
        ) {
            this.paginationItems.push(i);
        }
    }

    public handlePagination(pageEvent: Partial<PageEvent>): void {
        this.page = pageEvent.pageIndex;
        this.paginator.pageIndex = this.page;
        this.updateZahlungsauftrag();
    }

    public showAuszahlungsTypToggle(): boolean {
        // Wenn entweder Mahlzeitenzahlungslaeufe oder Auszahlungen an Eltern aktiviert sind,
        // soll der zweite Tab angezeigt werden
        return (
            !!this.hasMahlzeitenZahlungslaeufe || !!this.hasAuszahlungAnEltern
        );
    }

    public getLabelZahlungslaufErstellen(): string {
        if (
            this.zahlungslaufTyp === TSZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER &&
            this.hasMahlzeitenZahlungslaeufe
        ) {
            return this.translate.instant(
                'BUTTON_GEMEINDE_ZAHLUNGSLAUF_MAHLZEITEN'
            );
        }
        return this.translate.instant('BUTTON_GEMEINDE_ZAHLUNGSLAUF_GUTSCHEIN');
    }

    public showInfotext(): boolean {
        return this.zahlungslaufTyp === TSZahlungslaufTyp.GEMEINDE_INSTITUTION;
    }

    private setupTableColumns(): void {
        this.tableColumns = [
            {
                displayedName: this.translate.instant('ZAHLUNG_GENERIERT'),
                attributeName: 'datumGeneriert',
                displayFunction: (date: moment.Moment) =>
                    date.format('DD.MM.YYYY')
            },
            {
                displayedName: this.translate.instant('GEMEINDE'),
                attributeName: 'gemeinde',
                displayFunction: (gemeinde: TSGemeinde) => gemeinde.name
            },
            {
                displayedName: this.translate.instant('ZAHLUNG_STATUS'),
                attributeName: 'status',
                displayFunction: (
                    status: TSZahlungsauftragsstatus,
                    element: TSZahlungsauftrag
                ) => this.getCalculatedStatus(element)
            }
        ];
    }

    public getColumnsAttributeName(): string[] {
        const allColumnNames = this.tableColumns?.map(
            column => column.attributeName
        );
        allColumnNames.splice(0, 0, 'datumFaellig');
        allColumnNames.splice(3, 0, 'zahlungPainExcel');
        allColumnNames.splice(4, 0, `beschrieb`);
        allColumnNames.splice(5, 0, `betragTotalAuftrag`);
        if (
            this.principal?.hasOneOfRoles(
                TSRoleUtil.getAdministratorBgGemeindeRoles()
            )
        ) {
            allColumnNames.splice(3, 0, `zahlungPain`);
            allColumnNames.push('editSave');
            allColumnNames.push('ausloesen');

            if (
                this.hasInfomaZahlung &&
                this.gemeindenList.some(
                    gemeinde => gemeinde.infomaZahlungen === true
                )
            ) {
                allColumnNames.splice(3, 0, `zahlungInfoma`);
            }
        }
        return allColumnNames;
    }

    public getDisplayValue(element: any, column: any): string {
        if (column.displayFunction !== undefined) {
            return column.displayFunction(
                element[column.attributeName],
                element
            );
        }
        return element[column.attributeName];
    }

    public showForm(): boolean {
        return this.principal?.hasOneOfRoles(
            TSRoleUtil.getAdministratorBgGemeindeRoles()
        );
    }

    public showGemeindeFilter(): boolean {
        return this.gemeindenList.length > 1;
    }
}

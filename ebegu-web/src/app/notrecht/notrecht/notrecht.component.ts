/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig, MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import * as moment from 'moment';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {isBereitZumVerfuegenOderVerfuegt, TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import {SendNotrechtMitteilungComponent} from '../send-notrecht-mitteilung/send-notrecht-mitteilung.component';

@Component({
    selector: 'dv-notrecht',
    templateUrl: './notrecht.component.html',
    styleUrls: ['./notrecht.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotrechtComponent implements OnInit {

    @ViewChild(MatSort)
    private readonly sort: MatSort;

    @ViewChild(MatPaginator) public paginator: MatPaginator;

    public rueckforderungFormulare: TSRueckforderungFormular[];
    public rueckforderungFormulareSource: MatTableDataSource<TSRueckforderungFormular>;
    // tslint:disable-next-line:no-duplicate-string
    public displayedColumns = ['institutionStammdaten.institution.name', 'institutionStammdaten.betreuungsangebotTyp',
        'status', 'zahlungStufe1', 'zahlungStufe2', 'is-clickable'];
    public displayedColumnsMandant = ['institutionStammdaten.institution.name', 'institutionStammdaten.betreuungsangebotTyp',
        'status', 'zahlungStufe1', 'zahlungStufe2', 'verantwortlich', 'dokumente', 'is-clickable'];

    private readonly panelClass = 'dv-mat-dialog-send-notrecht-mitteilung';

    public showOnlyOffenePendenzen: boolean = false;
    public showOnlyMirZugewieseneAntraege: boolean = false;
    public showOnlyAntraegeWithDokumenten: boolean = false;

    public constructor(
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly cdr: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly $state: StateService,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        this.loadRueckforderungFormulareForCurrentBenutzer();
    }

    private loadRueckforderungFormulareForCurrentBenutzer(): void {
        this.notrechtRS.getRueckforderungFormulareForCurrentBenutzer().then(formulare => {
            this.rueckforderungFormulare = formulare;
            this.initDataSource(formulare);
            this.cdr.detectChanges();
        });
    }

    private initDataSource(formulare: TSRueckforderungFormular[]): void {
        this.rueckforderungFormulareSource = new MatTableDataSource<TSRueckforderungFormular>(formulare);
        this.rueckforderungFormulareSource.paginator = this.paginator;

        this.initFilter();
        this.initSort();
    }

    private initSort(): void {
        this.rueckforderungFormulareSource.sortingDataAccessor = (item, property) => {
            switch (property) {
                case 'institutionStammdaten.institution.name':
                    return item.institutionStammdaten.institution.name;
                case 'status':
                    return this.translateRueckforderungStatus(item.status);
                case 'institutionStammdaten.betreuungsangebotTyp':
                    return item.institutionStammdaten.betreuungsangebotTyp;
                case 'zahlungStufe1':
                    return this.getZahlungAusgeloest(item.stufe1FreigabeAusbezahltAm);
                case 'zahlungStufe2':
                    return this.getZahlungAusgeloest(item.stufe2VerfuegungAusbezahltAm);
                case 'verantwortlich':
                    return item.verantwortlicherName;
                case 'dokumente':
                    return item.verantwortlicherName;
                default:
                    // @ts-ignore
                    return item[property];
            }
        };
        this.rueckforderungFormulareSource.sort = this.sort;
        this.sortTable();
    }

    private sortTable(): void {
        this.sort.sort({
                id: 'institutionStammdaten.institution.name',
                start: 'asc',
                disableClear: false,
            },
        );
    }

    private initFilter(): void {
        this.rueckforderungFormulareSource.filterPredicate = (data, filter) => {
            return EbeguUtil.hasTextCaseInsensitive(data.institutionStammdaten.institution.name, filter)
                || EbeguUtil.hasTextCaseInsensitive(this.translateRueckforderungStatus(data.status), filter)
                || EbeguUtil.hasTextCaseInsensitive(data.institutionStammdaten.betreuungsangebotTyp, filter)
                || EbeguUtil.hasTextCaseInsensitive(this.getZahlungAusgeloest(data.stufe1FreigabeAusbezahltAm), filter)
                || EbeguUtil.hasTextCaseInsensitive(this.getZahlungAusgeloest(data.stufe2VerfuegungAusbezahltAm), filter)
                || EbeguUtil.hasTextCaseInsensitive(data.verantwortlicherName, filter);
        };
    }

    public initializeRueckforderungFormulare(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGSFORMULAR_INIT_CONFIRMATION_TITLE',
            text: 'RUECKFORDERUNGSFORMULAR_INIT_CONFIRMATION_TEXT',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.notrechtRS.initializeRueckforderungFormulare().then(formulare => {
                        this.errorService.addMesageAsInfo(this.translate.instant(
                            'RUECKFORDERUNG_FORMULARE_INITIALISIERT',
                            {anzahlFormulare: formulare.length}
                        ));
                        this.loadRueckforderungFormulareForCurrentBenutzer();
                    });
                },
                () => {
                });
    }

    public initializePhase2(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'RUECKFORDERUNGSFORMULAR_INIT_CONFIRMATION_TITLE',
            text: 'RUECKFORDERUNGSFORMULAR_INIT_CONFIRMATION_TEXT',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(answer => {
                    if (answer !== true) {
                        return;
                    }
                    this.notrechtRS.initializePhase2().then(() => {
                        this.loadRueckforderungFormulareForCurrentBenutzer();
                        this.errorService.addMesageAsInfo(this.translate.instant(
                            'RUECKFORDERUNG_PHASE2_INITIALISIERT'
                        ));
                    });
                },
                () => {
                });
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public sendMitteilung(isEinladung: boolean): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = true;
        dialogConfig.data = {isEinladung};
        dialogConfig.panelClass = this.panelClass;
        // Bei Ok erhalten wir die Mitteilung, die gesendet werden soll, sonst nichts
        this.dialog.open(SendNotrechtMitteilungComponent, dialogConfig).afterClosed().toPromise().then(result => {
            if (EbeguUtil.isNullOrUndefined(result) || EbeguUtil.isNullOrUndefined(result.mitteilung)) {
                return;
            }
            if (isEinladung) {
                this.notrechtRS.sendEinladung(result.mitteilung).then(() => {
                    this.errorService.addMesageAsInfo(this.translate.instant(
                        'RUECKFORDERUNG_EINLADUNG_VERSENDET'
                    ));
                    this.loadRueckforderungFormulareForCurrentBenutzer();
                });
                return;
            }
            // tslint:disable-next-line:no-identical-functions
            this.notrechtRS.sendMitteilung(result.mitteilung, result.statusToSendMitteilung).then(() => {
                this.errorService.addMesageAsInfo(this.translate.instant(
                    'RUECKFORDERUNG_MITTEILUNG_VERSENDET'
                ));
            });
        });
    }

    public getZahlungAusgeloest(date: moment.Moment | null): string {
        if (EbeguUtil.isNotNullOrUndefined(date)) {
            return date.format(CONSTANTS.DATE_FORMAT);
        }
        return this.translate.instant('NICHT_AUSGELOEST');
    }

    /**
     * Kanton darf Formular erst sehen, wenn es mindestens im Status IN_PRUEFUNG_KANTON_STUFE_1 ist
     * Superadmin darf Formular erst sehen, wenn mindestens im Status IN_BEARBEITUNG_INSTITUTION_STUFE_1 ist
     * Institutionen & Trägerschaften dürfen Formulare ab Status EINGELADEN sehen
     */
    public openFormularAllowed(formular: TSRueckforderungFormular): boolean {
        return formular.status !== TSRueckforderungStatus.NEU;
    }

    public doFilter = (value: string) => {
        if (this.rueckforderungFormulareSource) {
            this.rueckforderungFormulareSource.filter = value;
        }
    };

    public openRueckforderungFormular(formular: TSRueckforderungFormular): void {
        if (!this.openFormularAllowed(formular)) {
            return;
        }
        this.$state.go('notrecht.form', {
            rueckforderungId: formular.id,
        });
    }

    public translateRueckforderungStatus(status: string): string {
        return this.translate.instant(`RUECKFORDERUNG_STATUS_${status}`);
    }

    public showMitteilungSenden(): boolean {
        return this.isMandantOrSuperuser();
    }

    public isMandantOrSuperuser(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public getDisplayColumns(): string[] {
        return this.isMandantOrSuperuser() ? this.displayedColumnsMandant : this.displayedColumns;
    }

    public filterRueckforderungFormulare(): void {
        let filteredFormulare = this.rueckforderungFormulare;
        if (this.showOnlyOffenePendenzen) {
            filteredFormulare = filteredFormulare.filter(d => this.isOffenePendenz(d));
        }
        if (this.showOnlyAntraegeWithDokumenten) {
            filteredFormulare = filteredFormulare.filter(d => d.uncheckedDocuments);
        }
        if (this.showOnlyMirZugewieseneAntraege) {
            const currentUsername = this.authServiceRS.getPrincipal().getFullName();
            filteredFormulare = filteredFormulare.filter(d => d.verantwortlicherName === currentUsername);
        }
        this.initDataSource(filteredFormulare);
    }

    private isOffenePendenz(formular: TSRueckforderungFormular): boolean {
        return !isBereitZumVerfuegenOderVerfuegt(formular.status)
            && formular.status !== TSRueckforderungStatus.VERFUEGT_PROVISORISCH;
    }
}

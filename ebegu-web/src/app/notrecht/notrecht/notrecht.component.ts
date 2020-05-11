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
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {TSRueckforderungMitteilung} from '../../../models/TSRueckforderungMitteilung';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
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

    private readonly panelClass = 'dv-mat-dialog-send-notrecht-mitteilung';
    private tempSavedMitteilung: TSRueckforderungMitteilung;

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
                || EbeguUtil.hasTextCaseInsensitive(this.getZahlungAusgeloest(data.stufe2VerfuegungAusbezahltAm), filter);
        };
    }

    public initializeRueckforderungFormulare(): void {
        this.notrechtRS.initializeRueckforderungFormulare().then(formulare => {
            this.errorService.addMesageAsInfo(this.translate.instant(
                'RUECKFORDERUNG_FORMULARE_INITIALISIERT',
                {anzahlFormulare: formulare.length}
            ));
        });
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public sendMitteilung(): void {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = true;
        dialogConfig.data = {mitteilung: this.tempSavedMitteilung};
        dialogConfig.panelClass = this.panelClass;
        // Bei Ok erhalten wir die Mitteilung, die gesendet werden soll, sonst nichts
        this.dialog.open(SendNotrechtMitteilungComponent, dialogConfig).afterClosed().toPromise().then(result => {
            if (EbeguUtil.isNullOrUndefined(result) || EbeguUtil.isNullOrUndefined(result.mitteilung)) {
                return;
            }
            if (result.send) {
                console.log(result.mitteilung);
                return;
            }
            // Mitteilung wurde nicht gesendet, deshalb wird sie zwischengespeichert um sie allenfalls später wieder
            // zu öffnen
            this.tempSavedMitteilung = result.mitteilung;
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
        if (this.isSuperAdmin()) {
            return formular.status !== TSRueckforderungStatus.NEU &&
                formular.status !== TSRueckforderungStatus.EINGELADEN;
        }
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())) {
            return formular.status !== TSRueckforderungStatus.NEU
                && formular.status !== TSRueckforderungStatus.EINGELADEN
                && formular.status !== TSRueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1;
        }
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
}

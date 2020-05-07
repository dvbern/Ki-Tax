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

import {Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild} from '@angular/core';
import {MatSort, MatTableDataSource} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';
import * as moment from 'moment';

@Component({
    selector: 'dv-notrecht',
    templateUrl: './notrecht.component.html',
    styleUrls: ['./notrecht.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotrechtComponent implements OnInit {

    @ViewChild(MatSort)
    private readonly sort: MatSort;

    public rueckforderungFormulare: TSRueckforderungFormular[];
    public rueckforderungFormulareSource: MatTableDataSource<TSRueckforderungFormular>;
    public displayedColumns = ['institution.name', 'status', 'zahlungStufe1', 'zahlungStufe2'];

    public constructor(
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly cdr: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
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
        this.rueckforderungFormulareSource.sort = this.sort;
        this.rueckforderungFormulareSource.sortingDataAccessor = (item, property) => {
            switch (property) {
                case 'institution.name': return item.institution.name;
                case 'zahlungStufe1': return this.isZahlungAusgeloest(item.stufe1FreigabeAusbezahltAm);
                case 'zahlungStufe2': return this.isZahlungAusgeloest(item.stufe2VerfuegungAusbezahltAm);
                default:
                    // @ts-ignore
                    return item[property];
            }
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

    public isZahlungAusgeloest(date: moment.Moment | null): boolean {
        return EbeguUtil.isNotNullOrUndefined(date);
    }
}

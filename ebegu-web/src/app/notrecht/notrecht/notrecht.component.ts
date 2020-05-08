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
import {MatSort, MatTableDataSource} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import {StateService} from '@uirouter/core';
import * as moment from 'moment';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSRueckforderungStatus} from '../../../models/enums/TSRueckforderungStatus';
import {TSRueckforderungFormular} from '../../../models/TSRueckforderungFormular';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {NotrechtRS} from '../../core/service/notrechtRS.rest';

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
    public displayedColumns = ['institutionStammdaten.institution.name', 'institutionStammdaten.betreuungsangebotTyp',
        'status', 'zahlungStufe1', 'zahlungStufe2', 'clickable-hint'];

    public constructor(
        private readonly notrechtRS: NotrechtRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly cdr: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly $state: StateService,
    ) {}

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
                case 'institutionStammdaten.institution.name': return item.institutionStammdaten.institution.name;
                case 'institutionStammdaten.betreuungsangebotTyp': return item.institutionStammdaten.betreuungsangebotTyp;
                case 'zahlungStufe1': return this.getZahlungAusgeloest(item.stufe1FreigabeAusbezahltAm);
                case 'zahlungStufe2': return this.getZahlungAusgeloest(item.stufe2VerfuegungAusbezahltAm);
                default:
                    // @ts-ignore
                    return item[property];
            }
        };
        this.rueckforderungFormulareSource.filterPredicate = (data: TSRueckforderungFormular, filter: string)  => {
            return EbeguUtil.hasTextCaseInsensitive(data.institutionStammdaten.institution.name, filter)
            || EbeguUtil.hasTextCaseInsensitive(data.status, filter)
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

    public getZahlungAusgeloest(date: moment.Moment | null): string {
        if (EbeguUtil.isNotNullOrUndefined(date)) {
            return date.format(CONSTANTS.DATE_FORMAT);
        }
        return this.translate.instant('LABEL_NEIN');
    }

    public isClickable(formular: TSRueckforderungFormular): boolean {
        return formular.status !== TSRueckforderungStatus.NEU &&
            formular.status !== TSRueckforderungStatus.EINGELADEN;
    }

    public doFilter = (value: string) => {
        if (this.rueckforderungFormulareSource) {
            this.rueckforderungFormulareSource.filter = value;
        }
    };

    public openRueckforderungFormular(formular: TSRueckforderungFormular): void {
      if (!this.isClickable(formular)) {
          return;
      }
      this.$state.go('notrecht.form', {
          rueckforderungId: formular.id,
      });
    }
}

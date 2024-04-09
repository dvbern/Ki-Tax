/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {ChangeDetectorRef, Component, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort, MatSortable, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import * as moment from 'moment';
import {UebersichtVersendeteMailsRS} from '../../../app/core/service/uebersichtVersendeteMailsRS';
import {TSVersendeteMail} from '../../../models/TSVersendeteMail';

@Component({
    selector: 'dv-uebersicht-Versendete-Mails',
    templateUrl: './uebersichtVersendeteMails.component.html',
    styleUrls: ['./uebersichtVersendeteMails.component.less'],
})

export class UebersichtVersendeteMailsComponent {
    public displayedColumns: string[] = ['zeitpunktVersand', 'empfaengerAdresse', 'betreff'];

    public dataSource: MatTableDataSource<TableUebersichtVersendeteMails>;
    @ViewChild(MatSort, {static:true}) public sort: MatSort;
    @ViewChild(MatPaginator, {static: true}) public paginator: MatPaginator;
    public constructor(
        private readonly uebersichtVersendeteMailsRS: UebersichtVersendeteMailsRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.passFilterToServer();
        this.sortTable();
        this.sort.sort(<MatSortable>{
            id: 'zeitpunktVersand',
            start: 'desc'
        });
    }

    public ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
    }
    private assignResultToDataSource(result: TSVersendeteMail[]): void {
        this.dataSource.data = result.map(
            item => ({
                zeitpunktVersand: this.parseMomentToString(item.zeitpunktVersand),
                empfaengerAdresse: item.empfaengerAdresse,
                betreff: item.betreff,
            } as TableUebersichtVersendeteMails),
        );
        this.dataSource.paginator = this.paginator;
    }

    private passFilterToServer(): void {
        this.dataSource = new MatTableDataSource<TableUebersichtVersendeteMails>([]);
        this.uebersichtVersendeteMailsRS.getAllMails()
            .subscribe((result: TSVersendeteMail[]) => {
                    this.assignResultToDataSource(result);
                    this.changeDetectorRef.markForCheck();
                },
                () => {
                });
    }

    protected parseMomentToString(versand: moment.Moment): string {
        return versand.format('DD.MM.YYYY HH:mm:ss');
    }

    public doFilter(value: string): void {
        this.dataSource.filter = value.trim().toLocaleLowerCase();
    }

    private sortTable(): void {
        this.dataSource.sortingDataAccessor = (data: any, sortHeaderId: any) => {
            if (typeof data[sortHeaderId] === 'string') {
                if (sortHeaderId === 'zeitpunktVersand') {
                    return moment(data.zeitpunktVersand, 'DD.MM.YYYY HH:mm:ss').toDate().getTime();
                }
                return data[sortHeaderId].toLocaleLowerCase();
            }
            return data[sortHeaderId];
        };
    }
}

interface TableUebersichtVersendeteMails {
    zeitpunktVersand: string;
    empfaengerAdresse: string;
    betreff: string;
}


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

import {ChangeDetectorRef, Component} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {UebersichtVersendeteMailsRS} from '../../../app/core/service/uebersichtVersendeteMailsRS';
import {TSUebersichtVersendeteMails} from '../../../models/TSUebersichtVersendeteMails';

@Component({
    selector: 'dv-uebersicht-Versendete-Mails',
    templateUrl: './uebersichtVersendeteMails.component.html',
    styleUrls: ['./uebersichtVersendeteMails.component.less'],
})

export class UebersichtVersendeteMailsComponent {
    public displayedColumns: string[] = ['zeitpunktVersand', 'empfaengerAdresse', 'betreff'];

    public dataSource: MatTableDataSource<TableUebersichtVersendeteMails>;

    public constructor(
        private readonly uebersichtVersendeteMailsRS: UebersichtVersendeteMailsRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        this.passFilterToServer();
    }

    private assignResultToDataSource(result: TSUebersichtVersendeteMails[]): void {
        this.dataSource.data = result.map(
            item => ({
                zeitpunktVersand: this.parseMomentToString(item.zeitpunktVersand),
                empfaengerAdresse: item.empfaengerAdresse,
                betreff: item.betreff,
            } as TableUebersichtVersendeteMails),
        );
    }

    private passFilterToServer(): void {
        this.dataSource = new MatTableDataSource<TableUebersichtVersendeteMails>([]);
        this.uebersichtVersendeteMailsRS.getAllMails()
            .subscribe((result: TSUebersichtVersendeteMails[]) => {
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
}

interface TableUebersichtVersendeteMails {
    zeitpunktVersand: string;
    empfaengerAdresse: string;
    betreff: string;
}


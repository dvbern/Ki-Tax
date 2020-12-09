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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {Component, Inject} from '@angular/core';
import {FormControl} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import {MitteilungRS} from '../../service/mitteilungRS.rest';
import {BenutzerRS} from '../../service/benutzerRS.rest';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {map, startWith} from 'rxjs/operators';
import {Observable} from 'rxjs';

/**
 * Component fuer den GemeindeDialog. In einem Select muss der Benutzer die Gemeinde auswaehlen.
 * Keine Gemeinde wird by default ausgewaehlt, damit der Benutzer nicht aus Versehen die falsche Gemeinde auswaehlt.
 * Die GemeindeListe wird von aussen gegeben, damit dieser Component von nichts abhaengt. Die ausgewaehlte Gemeinde
 * wird dann beim Close() zurueckgegeben
 */
@Component({
    selector: 'dv-ng-mitteilung-delegation-dialog',
    templateUrl: './dv-ng-mitteilung-delegation-dialog.template.html',
})
export class DvNgMitteilungDelegationDialogComponent {

    public benutzerList: TSBenutzer[];
    public filteredBenutzerList$: Observable<TSBenutzer[]>;
    public selectedBenutzer: TSBenutzer;
    public mitteilungId: string;
    public myControl = new FormControl();

    public constructor(
        private readonly dialogRef: MatDialogRef<DvNgMitteilungDelegationDialogComponent>,
        private readonly dialogSupport: MatDialog,
        private readonly mitteilungRS: MitteilungRS,
        private readonly benutzerRS: BenutzerRS,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.mitteilungId = data.mitteilungId;
        this.selectedBenutzer = null;
        this.benutzerRS.getBenutzerTsBgOrGemeindeForGemeinde(data.gemeindeId).then((response: any) => {
            this.benutzerList = response;
            this.filteredBenutzerList$ = this.myControl.valueChanges
                .pipe(
                    startWith(''),
                    map(value => this.filterBenutzer(value))
                );
        });
    }

    private filterBenutzer(value: any): TSBenutzer[] {
        if (this.benutzerList) {
            const filterValue = value instanceof TSBenutzer ?
                value.getFullName().toLowerCase() : value.toLowerCase();
            this.unselectBenutzerIfNoLongerSelected(filterValue);
            return this.benutzerList.filter(benutzer => benutzer.getFullName().toLowerCase().includes(filterValue));
        }
        return [];
    }

    public unselectBenutzerIfNoLongerSelected(filterValue: string): void {
        if (this.selectedBenutzer && this.selectedBenutzer.getFullName() !== filterValue) {
                this.selectedBenutzer = null;
        }
    }

    public save(): void {
        this.mitteilungRS.mitteilungWeiterleiten(this.mitteilungId, this.selectedBenutzer.username).then(() => {
            this.dialogRef.close(this.selectedBenutzer.username);
        });
    }

    public close(): void {
        this.dialogRef.close();
    }

    public updateMySelection(benutzer: TSBenutzer): void {
        this.selectedBenutzer = benutzer;
    }

    public getBenutzerFullName(benutzer: TSBenutzer): string {
        if (!benutzer) {
            return '';
        }
        return benutzer.getFullName();
    }
}

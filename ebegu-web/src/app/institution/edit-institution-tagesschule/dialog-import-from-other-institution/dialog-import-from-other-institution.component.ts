/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {Component, Inject, OnInit} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {TSModulTagesschuleTyp} from '../../../../models/enums/TSModulTagesschuleTyp';
import {TSEinstellungenTagesschule} from '../../../../models/TSEinstellungenTagesschule';
import {TSInstitution} from '../../../../models/TSInstitution';
import {TSInstitutionStammdaten} from '../../../../models/TSInstitutionStammdaten';
import {TSModulTagesschuleGroup} from '../../../../models/TSModulTagesschuleGroup';
import {InstitutionStammdatenRS} from '../../../core/service/institutionStammdatenRS.rest';

@Component({
    selector: 'dv-ng-gemeinde-dialog',
    templateUrl: './dialog-import-from-other-institution.template.html',
    styleUrls: ['./dialog-import-from-other-institution.component.less'],
})
export class DialogImportFromOtherInstitution implements OnInit {

    public selectedInstitutionStammdaten: TSInstitutionStammdaten;
    public institutionStammdatenList: TSInstitutionStammdaten[];
    public selectedEinstellungTagesschule: TSEinstellungenTagesschule;
    public einstellungenTagesschule: TSEinstellungenTagesschule[];

    private readonly currentTagesschule: TSInstitution;

    public constructor(
        private readonly dialogRef: MatDialogRef<DialogImportFromOtherInstitution>,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        @Inject(MAT_DIALOG_DATA) data: any,
    ) {
        this.institutionStammdatenList = data.institutionList;
        this.currentTagesschule = data.currentTagesschule;
    }

    public ngOnInit(): void {
        this.institutionStammdatenList = this.filterStammdatenList(this.institutionStammdatenList);
    }

    /**
     *  Als Vorschläge sollen nur die Tagesschulen angezeigt werden, die mindestens eine Periode mit dynamischen Modulen
     *  haben. Die Perioden mit Scolaris Modulen werden gelöscht. Ausserdem soll die momentan
     *  aktive Tagesschule nicht im Dropdown erscheinen.
     */
    private filterStammdatenList(institutionStammdaten: TSInstitutionStammdaten[]): Array<TSInstitutionStammdaten> {
        const filtered: TSInstitutionStammdaten[] = [];
        institutionStammdaten.forEach(stammdaten => {
            // stammdatenTagesschule && einstellungenTagesschule müssen existieren. es darf nicht dieselbe Tagesschule
            // wie die aktuell ausgewählte sein
            // tslint:disable-next-line:early-exit
            if (stammdaten.institutionStammdatenTagesschule &&
                stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule &&
                stammdaten.institution.id !== this.currentTagesschule.id) {
                // nur dynamische module und nur wenn module exsitieren
                stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule = this.filterEinstellungenTagesschule(
                    stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule
                );
                // nur wenn mindestens eine Periode mit dynamischen stammdaten
                if (stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.length > 0) {
                    filtered.push(stammdaten);
                }
            }
        });
        return filtered;
    }

    /**
     * Filtert einstellungen. ModulTagesschuleTyp muss dynamisch sein und es muss mindestens ein modul existieren.
     */
    private filterEinstellungenTagesschule(einstellungenTagesschule: Array<TSEinstellungenTagesschule>):
        TSEinstellungenTagesschule[] {
        return einstellungenTagesschule.filter(einstellung => {
            return einstellung.modulTagesschuleTyp === TSModulTagesschuleTyp.DYNAMISCH
                && einstellung.modulTagesschuleGroups.length > 0;
        });
    }

    public save(): void {
        this.dialogRef.close(this.selectedEinstellungTagesschule ?
            this.copyModules() : undefined);
    }

    // module müssen kopiert werden, um sicherzustellen, dass nicht dieselben ids verwendet werden.
    private copyModules(): TSModulTagesschuleGroup[]  {
        return this.selectedEinstellungTagesschule.modulTagesschuleGroups.map(module => module.getCopy());
    }

    public close(): void {
        this.dialogRef.close();
    }
}

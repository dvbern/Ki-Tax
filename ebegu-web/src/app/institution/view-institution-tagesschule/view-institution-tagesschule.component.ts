/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSModulTagesschule from '../../../models/TSModulTagesschule';

@Component({
    selector: 'dv-view-institution-tagesschule',
    templateUrl: './view-institution-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ViewInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;

    public module: TSModulTagesschule[] = [];

    public constructor(
    ) {
    }

    public ngOnInit(): void {
        // Die Module werden pro Wochentag gespeichert. Wir zeigen hier nur den Montag an
        // als Vertreter der ganzen Woche
        if (this.stammdaten && this.stammdaten.institutionStammdatenTagesschule) {
            let moduleTagesschule = this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule;
            for (const tsModulTagesschule of moduleTagesschule) {
                if (tsModulTagesschule.wochentag === TSDayOfWeek.MONDAY) {
                    this.module.push(tsModulTagesschule);
                }
            }
        }
    }
}

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
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import EbeguUtil from '../../../utils/EbeguUtil';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-view-institution-tagesschule',
    templateUrl: './view-institution-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ViewInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;

    public gesuchsperiodenList: TSGesuchsperiode[] = [];
    public moduleProGesuchsperiode: Map<string, TSModulTagesschule[]> =
        new Map<string, TSModulTagesschule[]>();

    public constructor(
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
    ) {
    }

    public ngOnInit(): void {
        if (EbeguUtil.isNullOrUndefined(this.stammdaten.institutionStammdatenTagesschule)) {
            this.stammdaten.institutionStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
            this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule = [];
        }
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then(allGesuchsperioden => {
            this.gesuchsperiodenList = allGesuchsperioden;
            // Die Module werden pro Wochentag gespeichert. Wir zeigen hier nur den Montag an
            // als Vertreter der ganzen Woche
            this.loadModuleTagesschule();
        });
    }

    private loadModuleTagesschule(): void {
        const moduleTagesschule = this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule;
        let moduleTagesschuleMontag: TSModulTagesschule[] = [];
        for (const tsModulTagesschule of moduleTagesschule) {
            if (tsModulTagesschule.wochentag === TSDayOfWeek.MONDAY) {
                moduleTagesschuleMontag.push(tsModulTagesschule);
            }
        }
        this.fillModulTagesschuleMap(moduleTagesschuleMontag);
    }

    private fillModulTagesschuleMap(modulListFromServer: TSModulTagesschule[]): void {
        this.gesuchsperiodenList.forEach((periode: TSGesuchsperiode) => {
            const foundmodul = modulListFromServer.filter((modul: TSModulTagesschule) => (
                modul.wochentag === TSDayOfWeek.MONDAY &&
                modul.gesuchsperiodeId === periode.id
            ))[0];
            // tslint:disable-next-line:early-exit
            if (foundmodul) {
                this.moduleProGesuchsperiode.get(periode.id).push(foundmodul);
            }
        });
    }
}

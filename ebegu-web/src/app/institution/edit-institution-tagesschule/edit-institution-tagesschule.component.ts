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
import {ControlContainer, NgForm} from '@angular/forms';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import EbeguUtil from '../../../utils/EbeguUtil';
import {LogFactory} from '../../core/logging/LogFactory';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

const LOG = LogFactory.createLog('EditInstitutionTagesschuleComponent');

@Component({
    selector: 'dv-edit-institution-tagesschule',
    templateUrl: './edit-institution-tagesschule.component.html',
    styleUrls: ['./edit-institution-tagesschule.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})

export class EditInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;

    public gesuchsperiodenList: TSGesuchsperiode[] = [];
    public gemeindeList: TSGemeinde[] = [];
    public moduleProGesuchsperiode: Map<string, Map<TSModulTagesschuleName, TSModulTagesschule>> =
        new Map<string, Map<TSModulTagesschuleName, TSModulTagesschule>>();

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
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
            this.loadModuleTagesschule();
        });
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
    }

    public onPrePersist(): void {
        this.replaceTagesschulmoduleOnInstitutionStammdatenTagesschule();
    }

    public getModuleTagesschuleForGesuchsperiode(gesuchsperiodeId: string): TSModulTagesschule[] {
        let elemente: TSModulTagesschule[] = [];

        let map = this.moduleProGesuchsperiode.get(gesuchsperiodeId);
        if (!map) {
            return [];
        }
        map.forEach(el => {
            elemente.push(el);
        })
        return elemente;
    }

    private getModulTagesschule(modulname: TSModulTagesschuleName, gesuchsperiodeId: string) {
        if (!this.moduleProGesuchsperiode.has(gesuchsperiodeId)) {
            this.moduleProGesuchsperiode.set(gesuchsperiodeId, new Map<TSModulTagesschuleName, TSModulTagesschule>());
        }
        let map = this.moduleProGesuchsperiode.get(gesuchsperiodeId);
        let modul = map.get(modulname);
        if (!modul) {
            // Gespeichert wird das Modul dann fuer jeden Wochentag. Als Vertreter wird der Montag ausgefüllt
            modul = new TSModulTagesschule();
            modul.gesuchsperiodeId = gesuchsperiodeId;
            modul.wochentag = TSDayOfWeek.MONDAY;
            modul.modulTagesschuleName = modulname;
            map.set(modulname, modul);
        }
        return modul;
    }

    private loadModuleTagesschule(): void {
        this.moduleProGesuchsperiode = new Map<string, Map<TSModulTagesschuleName, TSModulTagesschule>>();
        // tslint:disable-next-line:early-exit
        if (this.stammdaten && this.stammdaten.id) {
            if (this.stammdaten.institutionStammdatenTagesschule
                && this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule
                && this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule.length > 0) {
                this.fillModulTagesschuleMap(this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule);
            }
        } else {
            this.fillModulTagesschuleMap([]);
        }
    }

    private fillModulTagesschuleMap(modulListFromServer: TSModulTagesschule[]): void {
        this.gesuchsperiodenList.forEach((gp: TSGesuchsperiode) => {
            getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
                const foundmodul = modulListFromServer.filter(modul => (
                    modul.modulTagesschuleName === modulname &&
                    modul.wochentag === TSDayOfWeek.MONDAY &&
                    modul.gesuchsperiodeId === gp.id
                ))[0];
                // tslint:disable-next-line:early-exit
                if (foundmodul) {
                    this.moduleProGesuchsperiode.get(gp.id).set(modulname, foundmodul);
                } else {
                    this.getModulTagesschule(modulname, gp.id);
                }
            });
        });
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
        const definedModulTagesschule: TSModulTagesschule[] = [];
        this.moduleProGesuchsperiode.forEach((mapOfModules, gesuchsperiodeId) => {
            mapOfModules.forEach((tempModul, modulname) => {
                if (tempModul.zeitVon && tempModul.zeitBis) {
                    tempModul.gesuchsperiodeId = gesuchsperiodeId;
                    definedModulTagesschule.push(tempModul);
                }
            })
        });
        // tslint:disable-next-line:early-exit
        if (definedModulTagesschule.length > 0) {
            if (!this.stammdaten.institutionStammdatenTagesschule) {
                this.stammdaten.institutionStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
            }
            this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule = definedModulTagesschule;
        }
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }
}

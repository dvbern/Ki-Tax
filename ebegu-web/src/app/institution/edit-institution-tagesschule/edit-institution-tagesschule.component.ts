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
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';

@Component({
    selector: 'dv-edit-institution-tagesschule',
    templateUrl: './edit-institution-tagesschule.component.html',
    styleUrls: ['./edit-institution-tagesschule.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})

export class EditInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean = false;

    public gesuchsperiodenList: TSGesuchsperiode[] = [];
    public gemeindeList: TSGemeinde[] = [];
    private moduleToEdit: Map<string, TSModulTagesschule[]> = new Map<string, TSModulTagesschule[]>();
    private modulePersisted: Map<string, TSModulTagesschule[]> = new Map<string, TSModulTagesschule[]>();

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

    /**
     * Gibt alle potenziell auszufüllenden Module für die GP zurück (also auch leere)
     */
    public getModuleTagesschuleForGesuchsperiodeToEdit(gesuchsperiodeId: string): TSModulTagesschule[] {
        return this.moduleToEdit.get(gesuchsperiodeId);
    }

    public getModuleTagesschuleForGesuchsperiodePersisted(gesuchsperiodeId: string): TSModulTagesschule[] {
        return this.modulePersisted.get(gesuchsperiodeId);
    }

    private loadModuleTagesschule(): void {
        let moduleFromServer = this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule;
        this.modulePersisted = this.toMapPerGesuchsperiode(moduleFromServer, false);
        this.moduleToEdit = new Map<string, TSModulTagesschule[]>();
        // tslint:disable-next-line:early-exit
        if (moduleFromServer && moduleFromServer.length > 0) {
            this.moduleToEdit = this.toMapPerGesuchsperiode(moduleFromServer, true);
        } else {
            this.moduleToEdit = this.toMapPerGesuchsperiode([], true);
        }
    }

    private toMapPerGesuchsperiode(moduleList: TSModulTagesschule[], addMissing: boolean): Map<string, TSModulTagesschule[]> {
        const mapPerGesuchsperiode: Map<string, TSModulTagesschule[]> = new Map<string, TSModulTagesschule[]>();
        this.gesuchsperiodenList.forEach((gp: TSGesuchsperiode) => {
            if (!mapPerGesuchsperiode.get(gp.id)) {
                mapPerGesuchsperiode.set(gp.id, []);
            }
            getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
                let foundmodul = moduleList.filter(modul => (
                    modul.modulTagesschuleName === modulname &&
                    modul.wochentag === TSDayOfWeek.MONDAY &&
                    modul.gesuchsperiodeId === gp.id
                ))[0];
                // tslint:disable-next-line:early-exit
                if (addMissing && !foundmodul) {
                    foundmodul = this.createModulTagesschule(gp.id, modulname);
                }
                if (foundmodul) {
                    mapPerGesuchsperiode.get(gp.id).push(foundmodul);
                }
            });
        });
        return mapPerGesuchsperiode;
    }

    private createModulTagesschule(gesuchsperiodeId: string, modulname: TSModulTagesschuleName) {
        // Gespeichert wird das Modul dann fuer jeden Wochentag. Als Vertreter wird der Montag ausgefüllt
        let modul = new TSModulTagesschule();
        modul.gesuchsperiodeId = gesuchsperiodeId;
        modul.wochentag = TSDayOfWeek.MONDAY;
        modul.modulTagesschuleName = modulname;
        return modul;
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
        const definedModulTagesschule: TSModulTagesschule[] = [];
        this.moduleToEdit.forEach((mapOfModules, gesuchsperiodeId) => {
            mapOfModules.forEach((tempModul) => {
                if (tempModul.zeitVon && tempModul.zeitBis) {
                    definedModulTagesschule.push(tempModul);
                }
            })
        });
        // tslint:disable-next-line:early-exit
        if (definedModulTagesschule.length > 0) {
            this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule = definedModulTagesschule;
        }
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }
}

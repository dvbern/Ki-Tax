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
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import DateUtil from '../../../utils/DateUtil';
import EbeguUtil from '../../../utils/EbeguUtil';

@Component({
    selector: 'dv-edit-institution-tagesschule',
    templateUrl: './edit-institution-tagesschule.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ],
})

export class EditInstitutionTagesschuleComponent implements OnInit {

    @Input() public stammdaten: TSInstitutionStammdaten;

    public gemeindeList: TSGemeinde[];
    public modulTageschuleMap: { [key: string]: TSModulTagesschule; } = {};

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
        if (EbeguUtil.isNullOrUndefined(this.stammdaten.institutionStammdatenTagesschule)) {
            this.stammdaten.institutionStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
            this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule = [];
        }
        this.loadModuleTagesschule();
    }

    public onPrePersist(): void {
        this.replaceTagesschulmoduleOnInstitutionStammdatenTagesschule();
    }

    public getModulTagesschuleNamen(): TSModulTagesschuleName[] {
        return getTSModulTagesschuleNameValues();
    }

    public getModulTagesschule(modulname: TSModulTagesschuleName): TSModulTagesschule {
        let modul: TSModulTagesschule = this.modulTageschuleMap[modulname];
        if (!modul) {
            // Gespeichert wird das Modul dann fuer jeden Wochentag. Als Vertreter wird der Montag ausgefüllt
            modul = new TSModulTagesschule();
            modul.wochentag = TSDayOfWeek.MONDAY;
            modul.modulTagesschuleName = modulname;
            this.modulTageschuleMap[modulname] = modul;
        }
        return modul;
    }

    private loadModuleTagesschule(): void {
        this.modulTageschuleMap = {};
        if (this.stammdaten && this.stammdaten.id) {
            if (this.stammdaten.institutionStammdatenTagesschule && this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule) {
                this.fillModulTagesschuleMap(this.stammdaten.institutionStammdatenTagesschule.moduleTagesschule);
            }
        } else {
            this.fillModulTagesschuleMap([]);
        }
    }

    private fillModulTagesschuleMap(modulListFromServer: TSModulTagesschule[]) {
        getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
            let foundmodul = modulListFromServer.filter(modul => (modul.modulTagesschuleName === modulname && modul.wochentag === TSDayOfWeek.MONDAY))[0];
            if (foundmodul) {
                (foundmodul as any).zeitVon = DateUtil.momentToHoursAndMinutes(foundmodul.zeitVon);
                (foundmodul as any).zeitBis = DateUtil.momentToHoursAndMinutes(foundmodul.zeitBis);
                this.modulTageschuleMap[modulname] = foundmodul;
            } else {
                this.modulTageschuleMap[modulname] = this.getModulTagesschule(modulname);
            }
        });
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
		let definedModulTagesschule: TSModulTagesschule[] = [];
		for (let modulname in this.modulTageschuleMap) {
			let tempModul: TSModulTagesschule = this.modulTageschuleMap[modulname];
			if (tempModul.zeitVon && tempModul.zeitBis) {
			    tempModul.zeitVon = DateUtil.hoursAndMinutesToMoment(tempModul.zeitVon);
                tempModul.zeitBis = DateUtil.hoursAndMinutesToMoment(tempModul.zeitBis);
				definedModulTagesschule.push(tempModul);
			}
		}
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

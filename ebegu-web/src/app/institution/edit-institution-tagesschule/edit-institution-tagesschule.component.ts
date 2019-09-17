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
import {MatDialog, MatDialogConfig} from '@angular/material';
import {TranslateService} from '@ngx-translate/core';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {TSDayOfWeek} from '../../../models/enums/TSDayOfWeek';
import {TSModulTagesschuleIntervall} from '../../../models/enums/TSModulTagesschuleIntervall';
import {getTSModulTagesschuleNameValues, TSModulTagesschuleName} from '../../../models/enums/TSModulTagesschuleName';
import {getTSModulTagesschuleTypen, TSModulTagesschuleTyp} from '../../../models/enums/TSModulTagesschuleTyp';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenTagesschule from '../../../models/TSInstitutionStammdatenTagesschule';
import TSModulTagesschule from '../../../models/TSModulTagesschule';
import TSModulTagesschuleGroup from '../../../models/TSModulTagesschuleGroup';
import EbeguUtil from '../../../utils/EbeguUtil';
import {DvNgRemoveDialogComponent} from '../../core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import ErrorService from '../../core/errors/service/ErrorService';
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
    public groupsPerGesuchsperiode: Map<string, Set<TSModulTagesschuleGroup>> = new Map<string, Set<TSModulTagesschuleGroup>>();
    public showModulDetail: boolean = false;
    public groupToEdit: TSModulTagesschuleGroup = undefined;

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
        private readonly gesuchsperiodeRS: GesuchsperiodeRS,
        private readonly errorService: ErrorService,
        private readonly translate: TranslateService,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        if (EbeguUtil.isNullOrUndefined(this.stammdaten.institutionStammdatenTagesschule)) {
            this.stammdaten.institutionStammdatenTagesschule = new TSInstitutionStammdatenTagesschule();
            this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups = [];
            this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp = TSModulTagesschuleTyp.DYNAMISCH;
        }
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then(allGesuchsperioden => {
            this.gesuchsperiodenList = allGesuchsperioden;
            this.initializeGesuchsperioden();
            this.loadExistingModulTagesschuleGroups();
        });
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
    }

    public onPrePersist(): void {
        this.replaceTagesschulmoduleOnInstitutionStammdatenTagesschule();
    }

    private initializeGesuchsperioden(): void {
        this.groupsPerGesuchsperiode = new Map<string, Set<TSModulTagesschuleGroup>>();
        this.gesuchsperiodenList.forEach((gp: TSGesuchsperiode) => {
            if (!this.groupsPerGesuchsperiode.get(gp.id)) {
                this.groupsPerGesuchsperiode.set(gp.id, new Set<TSModulTagesschuleGroup>());
            }
        });
    }

    private loadExistingModulTagesschuleGroups(): void {
        const moduleFromServer = this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups;
        moduleFromServer.forEach((group: TSModulTagesschuleGroup) => {
            this.groupsPerGesuchsperiode.get(group.gesuchsperiodeId).add(group);
        });
    }

    public getGroupsPerGesuchsperiode(gesuchsperiodeId: string): Set<TSModulTagesschuleGroup> {
        return this.groupsPerGesuchsperiode.get(gesuchsperiodeId);
    }

    public addModulTagesschuleGroup(gesuchsperiodeId: string): void {
        this.groupToEdit = new TSModulTagesschuleGroup();
        this.groupToEdit.gesuchsperiodeId = gesuchsperiodeId;
        this.groupToEdit.modulTagesschuleName = TSModulTagesschuleName.DYNAMISCH;
        this.groupToEdit.module = [];
        this.showModulDetail = true;
    }

    public editModulTagesschuleGroup(group: TSModulTagesschuleGroup) {
        this.groupToEdit = group;
        this.showModulDetail = true;
    }

    public removeModulTagesschuleGroup(group: TSModulTagesschuleGroup): void {
        this.groupsPerGesuchsperiode.get(group.gesuchsperiodeId).delete(group);
    }

    public applyModulTagesschuleGroup(group: TSModulTagesschuleGroup) {
        if (group.isNew()) {
            this.groupsPerGesuchsperiode.get(group.gesuchsperiodeId).add(group);
        }
        this.groupToEdit = undefined;
        this.showModulDetail = false;
    }

    public getModulTagesschuleTypen() {
        return getTSModulTagesschuleTypen();
    }

    public isModulTagesschuleTypScolaris(): boolean {
        return this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp === TSModulTagesschuleTyp.SCOLARIS;
    }

    public changeModulTagesschuleTyp(): void {
        if (this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp === TSModulTagesschuleTyp.SCOLARIS) {
            this.changeToScolaris();
        } else {
            this.changeToDynamisch();
        }
    }

    private changeToDynamisch() {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'MODUL_TYP_DYNAMISCH_TITLE',
            text: 'MODUL_TYP_DYNAMISCH_INFO',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        // Benutzer hat abgebrochen -> Flag zuruecksetzen
                        this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp
                            = TSModulTagesschuleTyp.SCOLARIS;
                        return;
                    }
                    // Die Module sind neu dynamisch -> Alle eventuell vorhandenen auf DYNAMISCH setzen
                    this.groupsPerGesuchsperiode.forEach(mapOfModules => {
                        mapOfModules.forEach(tempModul => {
                            tempModul.modulTagesschuleName = TSModulTagesschuleName.DYNAMISCH;
                        });
                    });
                },
                () => {
                    this.errorService.addMesageAsError("error");
                }
            );
    }

    private changeToScolaris() {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            title: 'MODUL_TYP_SCOLARIS_TITLE',
            text: 'MODUL_TYP_SCOLARIS_INFO',
        };
        this.dialog.open(DvNgRemoveDialogComponent, dialogConfig).afterClosed()
            .subscribe(
                userAccepted => {
                    if (!userAccepted) {
                        // Benutzer hat abgebrochen -> Flag zuruecksetzen
                        this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp
                            = TSModulTagesschuleTyp.DYNAMISCH;
                        return;
                    }
                    // Die Module sind neu nach Scolaris -> Alle eventuell vorhandenen werden gelöscht
                    this.initializeGesuchsperioden();
                    this.createModulGroupsScolaris();
                },
                () => {
                    this.errorService.addMesageAsError("error");
                }
            );
    }

    public showCreateModulGroupsScolaris(): boolean {
        return this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleTyp !== TSModulTagesschuleTyp.SCOLARIS;
    }

    public createModulGroupsScolaris(): void {
        this.gesuchsperiodenList.forEach((gp: TSGesuchsperiode) => {
            if (this.groupsPerGesuchsperiode.get(gp.id).size > 0) {
                this.errorService.addMesageAsError("TODO: Achtung, es sind bereits Module definiert. Bitte löschen Sie diese zuerst");
                return;
            }
            getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
                let modul = this.createModulGroupScolaris(gp.id, modulname);
                this.groupsPerGesuchsperiode.get(gp.id).add(modul);
            });
        });
    }

    private createModulGroupScolaris(gesuchsperiodeId: string, modulname: TSModulTagesschuleName
    ): TSModulTagesschuleGroup {
        // Gespeichert wird das Modul dann fuer jeden Wochentag. Als Vertreter wird der Montag ausgefüllt
        const group = new TSModulTagesschuleGroup();
        group.gesuchsperiodeId = gesuchsperiodeId;
        group.modulTagesschuleName = modulname;
        group.bezeichnung = this.translate.instant(modulname);
        group.intervall = TSModulTagesschuleIntervall.WOECHENTLICH;
        group.wirdPaedagogischBetreut = true;
        group.module = [];
        this.createModuleScolaris(group);
        return group;
    }

    private createModuleScolaris(group: TSModulTagesschuleGroup): void {
        const montag = new TSModulTagesschule();
        montag.wochentag = TSDayOfWeek.MONDAY;
        group.module.push(montag);

        const dienstag = new TSModulTagesschule();
        dienstag.wochentag = TSDayOfWeek.TUESDAY;
        group.module.push(dienstag);

        const mittwoch = new TSModulTagesschule();
        mittwoch.wochentag = TSDayOfWeek.WEDNESDAY;
        group.module.push(mittwoch);

        const donnerstag = new TSModulTagesschule();
        donnerstag.wochentag = TSDayOfWeek.THURSDAY;
        group.module.push(donnerstag);

        const freitag = new TSModulTagesschule();
        freitag.wochentag = TSDayOfWeek.FRIDAY;
        group.module.push(freitag);
    }

    private replaceTagesschulmoduleOnInstitutionStammdatenTagesschule(): void {
        const definedModulTagesschule: TSModulTagesschuleGroup[] = [];
        this.groupsPerGesuchsperiode.forEach(mapOfModules => {
            mapOfModules.forEach(tempModul => {
                if (tempModul.zeitVon && tempModul.zeitBis) {
                    definedModulTagesschule.push(tempModul);
                }
            });
        });
        // tslint:disable-next-line:early-exit
        if (definedModulTagesschule.length > 0) {
            this.stammdaten.institutionStammdatenTagesschule.modulTagesschuleGroups = definedModulTagesschule;
        }
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }

    public getWochentageAsString(group: TSModulTagesschuleGroup): string {
        return 'TODO: Mo Di Mi';
    }
}

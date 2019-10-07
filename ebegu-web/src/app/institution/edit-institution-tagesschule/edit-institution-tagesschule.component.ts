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
import TSEinstellungenTagesschule from '../../../models/TSEinstellungenTagesschule';
import TSGemeinde from '../../../models/TSGemeinde';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
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

    public gemeindeList: TSGemeinde[] = [];
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
        this.gesuchsperiodeRS.getAllActiveGesuchsperioden().then(allGesuchsperioden => {
            if (EbeguUtil.isNullOrUndefined(this.stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule)
                || this.stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.length === 0) {
                this.stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule = [];
                allGesuchsperioden.forEach((gp: TSGesuchsperiode) => {
                    let einstellungGP: TSEinstellungenTagesschule = new TSEinstellungenTagesschule();
                    einstellungGP.gesuchsperiode = gp;
                    einstellungGP.modulTagesschuleTyp = TSModulTagesschuleTyp.DYNAMISCH;
                    einstellungGP.modulTagesschuleGroups = [];
                    this.stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.push(einstellungGP);
                });
            }
        });
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
    }

    public onPrePersist(): void {
    }

    public institutionStammdatenTagesschuleValid(): boolean {
        let result = true;
        this.stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.forEach(einst => {
            einst.modulTagesschuleGroups.forEach((grp) => {
                if (!grp.isValid()) {
                    result = false;
                }
            });
        });
        return result;
    }

    public addModulTagesschuleGroup(): void {
        this.groupToEdit = new TSModulTagesschuleGroup();
        this.groupToEdit.modulTagesschuleName = TSModulTagesschuleName.DYNAMISCH;
        this.showModulDetail = true;
    }

    public editModulTagesschuleGroup(group: TSModulTagesschuleGroup) {
        this.groupToEdit = group;
        this.showModulDetail = true;
    }

    public removeModulTagesschuleGroup(einstellungenTagesschule: TSEinstellungenTagesschule, group: TSModulTagesschuleGroup): void {
        EbeguUtil.removeElementFromList(group, einstellungenTagesschule.modulTagesschuleGroups);
    }

    public applyModulTagesschuleGroup(einstellungenTagesschule: TSEinstellungenTagesschule, group: TSModulTagesschuleGroup) {
        EbeguUtil.replaceElementInList(group, einstellungenTagesschule.modulTagesschuleGroups);
        this.showModulDetail = false;
    }

    public getModulTagesschuleTypen() {
        return getTSModulTagesschuleTypen();
    }

    public isModulTagesschuleTypScolaris(einstellungenTagesschule: TSEinstellungenTagesschule): boolean {
        return einstellungenTagesschule.modulTagesschuleTyp === TSModulTagesschuleTyp.SCOLARIS;
    }

    public changeModulTagesschuleTyp(einstellungenTagesschule: TSEinstellungenTagesschule): void {
        if (einstellungenTagesschule.modulTagesschuleTyp === TSModulTagesschuleTyp.SCOLARIS) {
            this.changeToScolaris(einstellungenTagesschule);
        } else {
            this.changeToDynamisch(einstellungenTagesschule);
        }
    }

    private changeToDynamisch(einstellungenTagesschule: TSEinstellungenTagesschule) {
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
                        einstellungenTagesschule.modulTagesschuleTyp = TSModulTagesschuleTyp.SCOLARIS;
                        return;
                    }
                    // Die Module sind neu dynamisch -> Alle eventuell vorhandenen auf DYNAMISCH setzen
                    einstellungenTagesschule.modulTagesschuleGroups.forEach(group => {
                        group.modulTagesschuleName = TSModulTagesschuleName.DYNAMISCH;
                    });
                },
                () => {
                    this.errorService.addMesageAsError("error");
                }
            );
    }

    private changeToScolaris(einstellungenTagesschule: TSEinstellungenTagesschule) {
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
                        einstellungenTagesschule.modulTagesschuleTyp = TSModulTagesschuleTyp.DYNAMISCH;
                        return;
                    }
                    // Die Module sind neu nach Scolaris -> Alle eventuell vorhandenen werden gelöscht
                    this.createModulGroupsScolaris(einstellungenTagesschule);
                },
                () => {
                    this.errorService.addMesageAsError("error");
                }
            );
    }

    public createModulGroupsScolaris(einstellungenTagesschule: TSEinstellungenTagesschule): void {
        einstellungenTagesschule.modulTagesschuleGroups = [];
        getTSModulTagesschuleNameValues().forEach((modulname: TSModulTagesschuleName) => {
            let group = this.createModulGroupScolaris(modulname);
            einstellungenTagesschule.modulTagesschuleGroups.push(group);
        });
    }

    private createModulGroupScolaris(modulname: TSModulTagesschuleName
    ): TSModulTagesschuleGroup {
        const group = new TSModulTagesschuleGroup();
        group.modulTagesschuleName = modulname;
        group.bezeichnung = this.translate.instant(modulname);
        group.identifier = TSModulTagesschuleGroup.createIdentifier();
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

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }

    public getWochentageAsString(group: TSModulTagesschuleGroup): string {
        let wochentageAsString: string = '';
        group.module.forEach((mod) => {
            const tag = this.translate.instant(mod.wochentag + '_SHORT');
            wochentageAsString = wochentageAsString + tag + ' ';
        });
        return wochentageAsString;
    }
}

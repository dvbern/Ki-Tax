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
import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {StateService} from '@uirouter/core';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {DvNgGesuchstellerDialogComponent} from '../../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-traegerschaft-add',
    templateUrl: './traegerschaft-add.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TraegerschaftAddComponent implements OnInit {

    private readonly log: Log = LogFactory.createLog('TraegerschaftAddComponent');

    @ViewChild(NgForm) public form: NgForm;

    public traegerschaft: TSTraegerschaft = undefined;
    public adminMail: string = undefined;

    public constructor(
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        this.traegerschaft = new TSTraegerschaft();
        this.adminMail = '';
    }

    public cancel(): void {
        this.navigateBack();
    }

    public traegerschaftEinladen(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.save();
    }

    private save(): void {
        this.traegerschaftRS.createTraegerschaft(this.traegerschaft, this.adminMail)
            .then(neueTraegerschaft => {
                this.traegerschaft = neueTraegerschaft;
                this.navigateBack();
            }).catch((exception: TSExceptionReport[]) => {
            if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH') {
                this.errorService.clearAll();
                const adminRolle = 'TSRole_ADMIN_TRAEGERSCHAFT';
                const dialogConfig = new MatDialogConfig();
                dialogConfig.data = {
                    emailAdresse: this.adminMail,
                    administratorRolle: adminRolle,
                    gesuchstellerName: exception[0].argumentList[1],
                };
                this.dialog.open(DvNgGesuchstellerDialogComponent, dialogConfig).afterClosed()
                    .subscribe(answer => {
                            if (answer !== true) {
                                return;
                            }
                            this.log.warn(`Der Gesuchsteller: ' +  ${exception[0].argumentList[1]} + wird einen neuen`
                                + ` Rollen bekommen und seine Gesuch wird gelöscht werden!`);
                            this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                                () => {
                                    this.persistTraegerschaft();
                                }
                            );
                        },
                        () => {
                        });
            } else if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_NO_GESUCH') {
                this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                    () => {
                        this.errorService.clearAll();
                        this.persistTraegerschaft();
                    }
                );
            }
        });
    }

    private persistTraegerschaft(): void {
        this.traegerschaftRS.createTraegerschaft(this.traegerschaft, this.adminMail)
            .then(neueTraegerschaft => {
                this.traegerschaft = neueTraegerschaft;
                this.navigateBack();
            });
    }

    private navigateBack(): void {
        this.$state.go('traegerschaft.list');
    }
}

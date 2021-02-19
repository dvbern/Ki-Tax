/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {StateService} from '@uirouter/core';
import {TSRole} from '../../../models/enums/TSRole';
import {TSSozialdienstStatus} from '../../../models/enums/TSSozialdienstStatus';
import {TSSozialdienst} from '../../../models/sozialdienst/TSSozialdienst';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {DvNgGesuchstellerDialogComponent} from '../../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {SozialdienstRS} from '../../core/service/SozialdienstRS.rest';

@Component({
    selector: 'dv-add-sozialdienst',
    templateUrl: './add-sozialdienst.component.html',
    styleUrls: ['./add-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddSozialdienstComponent implements OnInit {
    private readonly log: Log = LogFactory.createLog('AddSozialdienstComponent');

    @ViewChild(NgForm, {static: true}) public form: NgForm;

    public sozialdienst: TSSozialdienst = undefined;
    public adminEmail: string = undefined;

    public constructor(
        private readonly $state: StateService,
        private readonly sozialdienstRS: SozialdienstRS,
        private readonly errorService: ErrorService,
        private readonly benutzerRS: BenutzerRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        this.sozialdienst = new TSSozialdienst();
        this.sozialdienst.status = TSSozialdienstStatus.EINGELADEN;
    }

    public sozialdienstEinladen(): void {
        if (!this.form.valid) {
            return;
        }
        this.sozialdienstRS.createSozialdienst(
            this.sozialdienst,
            this.adminEmail,
        )
            .subscribe(neueSozialdienst => {
                this.sozialdienst = neueSozialdienst;
                this.navigateBack();
            }, (exception: TSExceptionReport[]) => {
            if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH') {
                this.errorService.clearAll();
                const dialogConfig = new MatDialogConfig();
                dialogConfig.data = {
                    emailAdresse: this.adminEmail,
                    administratorRolle: TSRole.ADMIN_SOZIALDIENST,
                    gesuchstellerName: exception[0].argumentList[1],
                };
                this.dialog.open(DvNgGesuchstellerDialogComponent, dialogConfig).afterClosed()
                    .subscribe(answer => {
                            if (answer !== true) {
                                return;
                            }
                            this.log.warn(`Der Gesuchsteller: ${exception[0].argumentList[1]} wird einen neuen`
                                + ` Rollen bekommen und seine Gesuch wird gelöscht werden!`);
                            this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                                () => {
                                    this.persistSozialdienst();
                                },
                            );
                        },
                        () => {
                        });
            } else if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_NO_GESUCH') {
                this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                    () => {
                        this.errorService.clearAll();
                        this.persistSozialdienst();
                    },
                );
            }
        });
    }

    private navigateBack(): void {
        this.cancel();
    }

    public cancel(): void {
        this.$state.go('sozialdienst.list');
    }

    private persistSozialdienst(): void {
        this.sozialdienstRS.createSozialdienst(
            this.sozialdienst,
            this.adminEmail,
        )
            .subscribe(neueSozialdienst => {
                this.sozialdienst = neueSozialdienst;
                this.navigateBack();
            }, () => this.errorService.addMesageAsError('SOZIALDIENST_PERSIST_ERROR'));
    }
}

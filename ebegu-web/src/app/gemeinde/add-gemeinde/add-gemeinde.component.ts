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
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSBfsGemeinde} from '../../../models/TSBfsGemeinde';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {DvNgGesuchstellerDialogComponent} from '../../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';

@Component({
    selector: 'dv-add-gemeinde',
    templateUrl: './add-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddGemeindeComponent implements OnInit {

    private readonly log: Log = LogFactory.createLog('AddGemeindeComponent');

    @ViewChild(NgForm, { static: true }) public form: NgForm;

    public gemeinde: TSGemeinde = undefined;
    public adminMail: string = undefined;
    public gesuchsperiodeList: Array<TSGesuchsperiode>;
    public maxBFSNummer: number = 6806;
    public minDateTSFI = moment('20200801', 'YYYYMMDD');

    public unregisteredGemeinden$: Observable<TSBfsGemeinde[]>;
    public selectedUnregisteredGemeinde: TSBfsGemeinde;

    public tageschuleEnabledForMandant: boolean;

    public showMessageKeinAngebotSelected: boolean = false;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (gemeindeId) { // edit
            this.gemeindeRS.findGemeinde(gemeindeId).then(result => {
                this.gemeinde = result;
            });
        } else { // add
            this.initGemeinde();
            this.unregisteredGemeinden$ = from(this.gemeindeRS.getUnregisteredBfsGemeinden())
                .pipe(map(bfsGemeinden => {
                    bfsGemeinden.sort(EbeguUtil.compareByName);
                    return bfsGemeinden;
                }));
        }
        this.adminMail = '';
        const currentDate = moment();
        const futureMonth = moment(currentDate).add(1, 'M');
        const futureMonthBegin = moment(futureMonth).startOf('month');
        this.gemeinde.betreuungsgutscheineStartdatum = futureMonthBegin;
        this.gemeinde.tagesschulanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        this.gemeinde.ferieninselanmeldungenStartdatum = moment('20200801', 'YYYYMMDD');
        this.tageschuleEnabledForMandant = this.authServiceRS.hasMandantAngebotTS();

        this.initializeFlags();
    }

    /**
     * Das Tagesschule-Flag des Mandanten ist noch nicht aktiv: Die Auswahl der Angebote für die neue Gemeinde
     * erscheint nicht, wir setzen fix auf BG=true, den Rest false
     */
    private initializeFlags(): void {
        // tslint:disable-next-line:early-exit
        if (!this.tageschuleEnabledForMandant) {
            this.gemeinde.angebotBG = true;
            this.gemeinde.angebotTS = false;
            this.gemeinde.angebotFI = false;
        }
    }

    public cancel(): void {
        this.navigateBack();
    }

    public gemeindeEinladen(): void {
        if (!this.form.valid) {
            return;
        }

        this.errorService.clearAll();
        if (this.isAtLeastOneAngebotSelected()) {
            this.persistGemeindeWithGSCheck();
        }
    }

    public bfsGemeindeSelected(): void {
        if (this.selectedUnregisteredGemeinde) {
            this.gemeinde.name = this.selectedUnregisteredGemeinde.name;
            this.gemeinde.bfsNummer = this.selectedUnregisteredGemeinde.bfsNummer;
        } else {
            this.gemeinde.name = undefined;
            this.gemeinde.bfsNummer = undefined;
        }
    }

    private isAtLeastOneAngebotSelected(): boolean {
        const hasAngebot = this.gemeinde.isAtLeastOneAngebotSelected();
        this.showMessageKeinAngebotSelected = !hasAngebot;
        return hasAngebot;
    }

    private persistGemeindeWithGSCheck(): void {
        this.gemeindeRS.createGemeinde(
            this.gemeinde,
            this.adminMail
        )
            .then(neueGemeinde => {
                this.gemeinde = neueGemeinde;
                this.navigateBack();
            }).catch((exception: TSExceptionReport[]) => {
            if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH') {
                this.errorService.clearAll();
                const adminRolle = this.getUserRoleForGemeindeAdmin();
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
                            this.log.warn(`Der Gesuchsteller: ${exception[0].argumentList[1]} wird einen neuen`
                                + ` Rollen bekommen und seine Gesuch wird gelöscht werden!`);
                            this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                                () => {
                                    this.persistGemeinde();
                                }
                            );
                        },
                        () => {
                        });
            } else if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_NO_GESUCH') {
                this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                    () => {
                        this.errorService.clearAll();
                        this.persistGemeinde();
                    }
                );
            }
        });
    }

    private persistGemeinde(): void {
        this.gemeindeRS.createGemeinde(
            this.gemeinde,
            this.adminMail
        )
            .then(neueGemeinde => {
                this.gemeinde = neueGemeinde;
                this.navigateBack();
            });
    }

    private initGemeinde(): void {
        this.gemeinde = new TSGemeinde();
        this.gemeinde.status = TSGemeindeStatus.EINGELADEN;
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }

    private getUserRoleForGemeindeAdmin(): string {
        const hasBG = this.gemeinde.angebotBG;
        const hasTS = this.gemeinde.angebotTS || this.gemeinde.angebotFI;
        if (!hasBG) {
            return 'TSRole_ADMIN_TS';
        }
        if (!hasTS) {
            return 'TSRole_ADMIN_GEMEINDE';
        }
        return 'TSRole_ADMIN_GEMEINDE';
    }
}

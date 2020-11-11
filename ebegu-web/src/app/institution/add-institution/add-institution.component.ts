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
import {take} from 'rxjs/operators';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {DvNgGesuchstellerDialogComponent} from '../../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

const LOG = LogFactory.createLog('AddInstitutionComponent');

@Component({
    selector: 'dv-add-institution',
    templateUrl: './add-institution.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddInstitutionComponent implements OnInit {

    private readonly log: Log = LogFactory.createLog('AddInstitutionComponent');

    @ViewChild(NgForm, { static: true }) public form: NgForm;
    public isBGInstitution: boolean;
    public betreuungsangebote: TSBetreuungsangebotTyp[];
    public betreuungsangebot: TSBetreuungsangebotTyp;
    public traegerschaften: TSTraegerschaft[];
    public institution: TSInstitution = undefined;
    public startDate: moment.Moment;
    public adminMail: string;
    public selectedGemeinde: TSGemeinde;
    public gemeinden: Array<TSGemeinde>;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly translate: TranslateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly benutzerRS: BenutzerRS,
        private readonly dialog: MatDialog,
    ) {
    }

    public ngOnInit(): void {
        this.betreuungsangebot = this.$transition$.params().betreuungsangebot;
        this.betreuungsangebote = this.$transition$.params().betreuungsangebote;

        // initally we think it is a Betreuungsgutschein Institution
        this.isBGInstitution = true;
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE
            || this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL) {
            this.isBGInstitution = false;
        }
        this.initInstitution();

        this.traegerschaftRS.getAllActiveTraegerschaften().then(result => {
            this.traegerschaften = result;
        });
        this.startDate = this.getStartDate();

        // if it is not a Betreuungsgutschein Institution we have to load the Gemeinden
        if (!this.isBGInstitution) {
            this.loadGemeindenList();
        }
    }

    public cancel(): void {
        this.navigateBack();
    }

    public institutionErstellen(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.persistInstitutionWithGSCheck();
    }

    private persistInstitutionWithGSCheck(): void {
        this.institutionRS.createInstitution(
            this.institution,
            this.startDate,
            this.betreuungsangebot,
            this.adminMail,
            this.selectedGemeinde ? this.selectedGemeinde.id : undefined,
        ).then(neueinstitution => {
            this.institution = neueinstitution;
            this.goToNextView();
        }).catch((exception: TSExceptionReport[]) => {
            if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH') {
                this.errorService.clearAll();
                const adminRolle = 'TSRole_ADMIN_INSTITUTION';
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
                                    this.persistInstitution();
                                }
                            );
                        },
                        () => {
                        });
            } else if (exception[0].errorCodeEnum === 'ERROR_GESUCHSTELLER_EXIST_NO_GESUCH') {
                this.benutzerRS.removeBenutzer(exception[0].argumentList[0]).then(
                    () => {
                        this.errorService.clearAll();
                        this.persistInstitution();
                    }
                );
            }
        });
    }

    private persistInstitution(): void {
        this.institutionRS.createInstitution(
            this.institution,
            this.startDate,
            this.betreuungsangebot,
            this.adminMail,
            this.selectedGemeinde ? this.selectedGemeinde.id : undefined,
        ).then(neueinstitution => {
            this.institution = neueinstitution;
            this.goToNextView();
            });
    }

    private initInstitution(): void {
        this.institution = new TSInstitution();
        this.institution.status = this.isBGInstitution ?
            TSInstitutionStatus.EINGELADEN :
            TSInstitutionStatus.KONFIGURATION;
    }

    private goToNextView(): void {
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE ||
            this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL) {
            this.navigateToEdit();
        } else {
            this.navigateBack();
        }
    }

    private navigateBack(): void {
        this.$state.go('institution.list');
    }

    private navigateToEdit(): void {
        this.$state.go('institution.edit', {
            institutionId: this.institution.id,
            editMode: true
        });
    }

    public loadGemeindenList(): void {
        // tslint:disable-next-line:early-exit
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE) {
            this.gemeindeRS.getGemeindenForTSByPrincipal$()
                .pipe(take(1))
                .subscribe(
                    gemeinden => {
                        this.gemeinden = gemeinden;
                    },
                    err => LOG.error(err),
                );
        }
        // tslint:disable-next-line:early-exit
        if (this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL) {
            this.gemeindeRS.getGemeindenForFIByPrincipal$()
                .pipe(take(1))
                .subscribe(
                    gemeinden => {
                        this.gemeinden = gemeinden;
                    },
                    err => LOG.error(err),
                );
        }
    }

    /*
    Für Tagesschulen und Ferieninseln ist ein Minimaldatum für "Anmeldungen akzeptieren ab" definiert
    da Anmeldungen bei Tagesschulen frühstens ab der Periode 20/21 möglich sein können.
     */
    private getStartDate(): moment.Moment {
        const nextMonthBegin = moment().add(1, 'M').startOf('month');

        if (this.isBGInstitution || nextMonthBegin >= TSMandant.earliestDateOfTSAnmeldung) {
            return nextMonthBegin;
        }
        return TSMandant.earliestDateOfTSAnmeldung;
    }
}

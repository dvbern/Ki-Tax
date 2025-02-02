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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import {take} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import {TSExceptionReport} from '../../../models/TSExceptionReport';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitution} from '../../../models/TSInstitution';
import {TSMandant} from '../../../models/TSMandant';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvNgGesuchstellerDialogComponent} from '../../core/component/dv-ng-gesuchsteller-dialog/dv-ng-gesuchsteller-dialog.component';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {Log, LogFactory} from '../../core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {BenutzerRSX} from '../../core/service/benutzerRSX.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {DvNgSelectTraegerschaftEmailDialogComponent} from '../../core/component/dv-ng-select-traegerschaft-email-dialog/dv-ng-select-traegerschaft-email-dialog.component';

const LOG = LogFactory.createLog('AddInstitutionComponent');

@Component({
    selector: 'dv-add-institution',
    templateUrl: './add-institution.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AddInstitutionComponent implements OnInit {
    private readonly log: Log = LogFactory.createLog('AddInstitutionComponent');

    @ViewChild(NgForm, {static: true}) public form: NgForm;
    public isBGInstitution: boolean;
    public betreuungsangebote: TSBetreuungsangebotTyp[];
    public betreuungsangebot: TSBetreuungsangebotTyp;
    public traegerschaften: TSTraegerschaft[];
    public institution: TSInstitution = undefined;
    public startDate: moment.Moment;
    public adminMail: string;
    public selectedGemeinde: TSGemeinde;
    public gemeinden: Array<TSGemeinde>;

    public isLatsInstitution: boolean;

    private institutionenDurchGemeindenEinladen: boolean = false;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly traegerschaftRS: TraegerschaftRS,
        private readonly translate: TranslateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly benutzerRS: BenutzerRSX,
        private readonly dialog: MatDialog,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly cd: ChangeDetectorRef
    ) {}

    public ngOnInit(): void {
        this.betreuungsangebot = this.$transition$.params().betreuungsangebot;
        this.applicationPropertyRS.getPublicPropertiesCached().then(props => {
            this.betreuungsangebote = this.$transition$
                .params()
                .betreuungsangebote.filter(
                    (angebotTyp: TSBetreuungsangebotTyp) => {
                        switch (angebotTyp) {
                            case TSBetreuungsangebotTyp.TAGESFAMILIEN:
                                return props.angebotTFOActivated;
                            case TSBetreuungsangebotTyp.FERIENINSEL:
                                return props.angebotFIActivated;
                            case TSBetreuungsangebotTyp.TAGESSCHULE:
                                return props.angebotTSActivated;
                            case TSBetreuungsangebotTyp.MITTAGSTISCH:
                                return props.angebotMittagstischActivated;
                            default:
                                return true;
                        }
                    }
                );
        });
        this.isLatsInstitution = this.$transition$.params().latsOnly;

        // initally we think it is a Betreuungsgutschein Institution
        this.isBGInstitution = true;
        if (
            this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE ||
            this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL
        ) {
            this.isBGInstitution = false;
        }
        this.initInstitution();

        this.traegerschaftRS.getAllActiveTraegerschaften().then(result => {
            this.traegerschaften = result;
        });
        this.startDate = this.getStartDate();

        // if it is not a Betreuungsgutschein Institution we have to load the Gemeinden
        this.applicationPropertyRS
            .getInstitutionenDurchGemeindenEinladen()
            .then(result => {
                this.institutionenDurchGemeindenEinladen = result;
                if (
                    this.institutionenDurchGemeindenEinladen ||
                    !this.isBGInstitution
                ) {
                    this.loadGemeindenList();
                }
            });
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
        this.institutionRS
            .createInstitution(
                this.institution,
                this.startDate,
                this.betreuungsangebot,
                this.adminMail,
                this.selectedGemeinde ? this.selectedGemeinde.id : undefined
            )
            .subscribe(
                neueinstitution => {
                    this.institution = neueinstitution;
                    this.goToNextView();
                },
                (exception: TSExceptionReport[]) => {
                    if (
                        exception[0].errorCodeEnum ===
                        'ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH'
                    ) {
                        this.errorService.clearAll();
                        const adminRolle = 'TSRole_ADMIN_INSTITUTION';
                        const dialogConfig = new MatDialogConfig();
                        dialogConfig.data = {
                            emailAdresse: this.adminMail,
                            administratorRolle: adminRolle,
                            gesuchstellerName: exception[0].argumentList[1]
                        };
                        this.dialog
                            .open(
                                DvNgGesuchstellerDialogComponent,
                                dialogConfig
                            )
                            .afterClosed()
                            .subscribe(
                                answer => {
                                    if (answer !== true) {
                                        return;
                                    }
                                    this.log.warn(
                                        `Der Gesuchsteller: ' +  ${exception[0].argumentList[1]} + wird einen neuen` +
                                            ` Rollen bekommen und seine Gesuch wird gelöscht werden!`
                                    );
                                    this.benutzerRS
                                        .removeBenutzer(
                                            exception[0].argumentList[0]
                                        )
                                        .then(() => {
                                            this.persistInstitution();
                                        });
                                },
                                () => {}
                            );
                    } else if (
                        exception[0].errorCodeEnum ===
                        'ERROR_GESUCHSTELLER_EXIST_NO_GESUCH'
                    ) {
                        this.benutzerRS
                            .removeBenutzer(exception[0].argumentList[0])
                            .then(() => {
                                this.errorService.clearAll();
                                this.persistInstitution();
                            });
                    }
                }
            );
    }

    private persistInstitution(): void {
        this.institutionRS
            .createInstitution(
                this.institution,
                this.startDate,
                this.betreuungsangebot,
                this.adminMail,
                this.selectedGemeinde ? this.selectedGemeinde.id : undefined
            )
            .subscribe(
                neueinstitution => {
                    this.institution = neueinstitution;
                    this.goToNextView();
                },
                error => LOG.error(error)
            );
    }

    private initInstitution(): void {
        this.institution = new TSInstitution();
        if (this.isLatsInstitution) {
            this.institution.status = TSInstitutionStatus.NUR_LATS;
            return;
        }
        this.institution.status = this.isBGInstitution
            ? TSInstitutionStatus.EINGELADEN
            : TSInstitutionStatus.KONFIGURATION;
    }

    private goToNextView(): void {
        if (
            this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE ||
            this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL
        ) {
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
        let obs$;
        if (
            this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE &&
            this.isLatsInstitution
        ) {
            obs$ = this.gemeindeRS.getGemeindenForPrincipal$();
        } else if (
            this.betreuungsangebot === TSBetreuungsangebotTyp.TAGESSCHULE &&
            !this.isLatsInstitution
        ) {
            obs$ = this.gemeindeRS.getGemeindenForTSByPrincipal$();
        } else if (
            this.betreuungsangebot === TSBetreuungsangebotTyp.FERIENINSEL
        ) {
            obs$ = this.gemeindeRS.getGemeindenForFIByPrincipal$();
        } else {
            obs$ = this.gemeindeRS.getGemeindenForBGByPrincipal$();
        }
        obs$.pipe(take(1)).subscribe(
            gemeinden => {
                this.gemeinden = this.isLatsInstitution
                    ? gemeinden
                    : gemeinden.filter(gemeinde => !gemeinde.nurLats);
                this.gemeinden.sort((a, b) => a.name.localeCompare(b.name));
            },
            err => LOG.error(err)
        );
    }

    /*
    Für Tagesschulen und Ferieninseln ist ein Minimaldatum für "Anmeldungen akzeptieren ab" definiert
    da Anmeldungen bei Tagesschulen frühstens ab der Periode 20/21 möglich sein können.
     */
    private getStartDate(): moment.Moment {
        if (this.institution.status === TSInstitutionStatus.NUR_LATS) {
            return TSMandant.nurLatsInstitutionenStartdatum;
        }
        const nextMonthBegin = moment().add(1, 'M').startOf('month');

        if (
            this.isBGInstitution ||
            nextMonthBegin >= TSMandant.earliestDateOfTSAnmeldung
        ) {
            return nextMonthBegin;
        }
        return TSMandant.earliestDateOfTSAnmeldung;
    }

    public selectGemeindeVisible(): boolean {
        if (!this.isBGInstitution) {
            return true;
        }
        if (!this.institutionenDurchGemeindenEinladen) {
            return false;
        }
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorBgTsGemeindeRole()
        );
    }

    // das Dropdown ist nur für den Superadmin sichtbar, aber nicht required. für alle
    // anderen Rollen ist es entweder nicht sichtbar, oder required
    public selectGemeindeRequired(): boolean {
        return !this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public selectAdminMail(): void {
        this.benutzerRS
            .getAllEmailAdminForTraegerschaft(this.institution.traegerschaft)
            .then(mail => {
                const dialogConfig = new MatDialogConfig();

                dialogConfig.data = mail;

                this.dialog
                    .open(
                        DvNgSelectTraegerschaftEmailDialogComponent,
                        dialogConfig
                    )
                    .afterClosed()
                    .subscribe(result => {
                        this.adminMail = result.selectedEmail;
                        this.cd.markForCheck();
                    });
            });
    }
}

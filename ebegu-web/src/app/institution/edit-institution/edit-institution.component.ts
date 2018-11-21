/*
 * AGPL File-Header
 *
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import ErrorService from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';

@Component({
  selector: 'dv-edit-institution',
  templateUrl: './edit-institution.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class EditInstitutionComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;
    public readonly tomorrow: moment.Moment = DateUtil.today().add(1, 'days');

    public institution: TSInstitution;
    public stammdaten: TSInstitutionStammdaten;
    public abweichendeZahlungsAdresse: boolean;
    public editMode: boolean;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        const institutionId = this.$transition$.params().institutionId;
        if (!institutionId) {
            return;
        }

        this.institutionRS.findInstitution(institutionId).then(institution => {
            this.institution = institution;

            this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(institution.id)
                .then(stammdaten => {
                    if (stammdaten) {
                        this.stammdaten = stammdaten;
                    } else {
                        this.createInstitutionStammdaten();
                    }
                    this.abweichendeZahlungsAdresse = !!this.stammdaten.adresseKontoinhaber;
                    this.editMode = this.institution.status === TSInstitutionStatus.EINGELADEN;
                    this.changeDetectorRef.markForCheck();
                });
        });
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionProfilEditRoles());
    }

    public isBetreuungsgutscheineAkzeptierenDisabled(): boolean {
        return !this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public getHeaderTitle(): string {
        if (!this.institution.traegerschaft) {
            return this.institution.name;
        }
        return `${this.institution.name} (${this.institution.traegerschaft.name})`;
    }

    public onSubmit(): void {
        if (this.editMode) {
            this.persistStammdaten();
        } else {
            this.editMode = true;
        }
    }

    public submitButtonLabel(): string {
        if (this.editMode) {
            return this.translate.instant('INSTITUTION_SPEICHERN');
        }
        return this.translate.instant('INSTITUTION_EDIT');
    }

    public cancel(): void {
        if (this.editMode) {
            this.editMode = false;
        } else {
            this.navigateBack();
        }
    }

    public onAbweichendeZahlungsAdresseClick(): void {
        if (!this.stammdaten.adresseKontoinhaber) {
            this.stammdaten.adresseKontoinhaber = new TSAdresse();
        }
    }

    private persistStammdaten(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        if (!this.abweichendeZahlungsAdresse) { // Reset Adresse Kontoinhaber if not used
            this.stammdaten.adresseKontoinhaber = undefined;
        }
        if (this.stammdaten.telefon === '') { // Prevent phone regex error in case of empty string
            this.stammdaten.telefon = null;
        }
        this.institutionStammdatenRS.saveInstitutionStammdaten(this.stammdaten)
            .then(() => {
                this.editMode = false;
                this.changeDetectorRef.markForCheck();
            });
    }

    private createInstitutionStammdaten(): void {
        this.stammdaten = new TSInstitutionStammdaten();
        this.stammdaten.adresse = new TSAdresse();
        this.stammdaten.institution = this.institution;
        this.stammdaten.gueltigkeit = new TSDateRange();
        this.stammdaten.gueltigkeit.gueltigAb = moment();
    }

    private navigateBack(): void {
         this.$state.go('institution.list');
    }

    public getGueltigkeitTodisplay(): string {
        return `${this.translate.instant('AB')} ${DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigAb, CONSTANTS.DATE_FORMAT)}
             ${this.getBisDateIfSet()}`;
    }

    private getBisDateIfSet(): string {
        if (this.stammdaten.gueltigkeit.gueltigBis
            && DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigBis, CONSTANTS.DATE_FORMAT) !== CONSTANTS.END_OF_TIME_STRING
        ) {
            return `(${this.translate.instant('BIS')} ${DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigBis, CONSTANTS.DATE_FORMAT)})`;
        }
        return '';
    }
}

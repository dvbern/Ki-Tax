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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import * as moment from 'moment';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSInstitutionStatus} from '../../../models/enums/TSInstitutionStatus';
import {TSRole} from '../../../models/enums/TSRole';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {Permission} from '../../authorisation/Permission';
import {PERMISSIONS} from '../../authorisation/Permissions';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import ErrorService from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
  selector: 'dv-edit-institution',
  templateUrl: './edit-institution.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class EditInstitutionComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;
    public readonly tomorrow: moment.Moment = DateUtil.today().add(1, 'days');

    public traegerschaftenList: TSTraegerschaft[];
    public stammdaten: TSInstitutionStammdaten;
    public abweichendeZahlungsAdresse: boolean;
    public editMode: boolean;
    private isRegisteringInstitution: boolean = false;
    private initName: string;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly traegerschaftRS: TraegerschaftRS,
    ) {
    }

    public ngOnInit(): void {
        const institutionId = this.$transition$.params().institutionId;
        if (!institutionId) {
            return;
        }
        this.isRegisteringInstitution = this.$transition$.params().isRegistering;

        this.traegerschaftRS.getAllActiveTraegerschaften().then(allTraegerschaften => {
            this.traegerschaftenList = allTraegerschaften;
        });

        this.fetchInstitution(institutionId);
    }

    private fetchInstitution(institutionId: string): void {
        this.institutionRS.findInstitution(institutionId).then(institution => {

            this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(institution.id)
                .then(stammdaten => {
                    if (stammdaten) {
                        this.stammdaten = stammdaten;
                    } else {
                        this.createInstitutionStammdaten(institution);
                    }
                    this.abweichendeZahlungsAdresse = !!this.stammdaten.adresseKontoinhaber;
                    this.initName = this.stammdaten.institution.name;
                    this.editMode = this.stammdaten.institution.status === TSInstitutionStatus.EINGELADEN;
                    this.changeDetectorRef.markForCheck();
                });
        });
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public getMitarbeiterVisibleRoles(): TSRole[] {
        const allowedRoles  =  PERMISSIONS[Permission.ROLE_INSTITUTION].concat(TSRole.SUPER_ADMIN);
        return allowedRoles;
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getInstitutionProfilEditRoles());
    }

    public isBetreuungsgutscheineAkzeptierenDisabled(): boolean {
        return !this.isSuperAdmin();
    }

    public isSuperAdmin(): boolean {
        return this.authServiceRS.isRole(TSRole.SUPER_ADMIN);
    }

    public isRegistering(): boolean {
        return this.isRegisteringInstitution;
    }

    public getHeaderPreTitle(): string {
        let result = '';
        if (this.stammdaten.institution.traegerschaft) {
            result += this.stammdaten.institution.traegerschaft.name + ' - ';
        }
        result += this.translate.instant(this.stammdaten.betreuungsangebotTyp);
        return result;
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
                // tslint:disable-next-line:early-exit
                if (this.initName === this.stammdaten.institution.name) {
                    this.setValuesAfterSave();
                } else {
                    this.institutionRS.updateInstitution(this.stammdaten.institution)
                        .then(() => {
                            this.setValuesAfterSave();
                        });
                }
            });
    }

    private setValuesAfterSave(): void {
        this.editMode = false;
        this.changeDetectorRef.markForCheck();
        this.navigateToWelcomesite();
        // if we don't navigate away we refresh all data
        this.fetchInstitution(this.stammdaten.institution.id);
    }

    private createInstitutionStammdaten(institution: TSInstitution): void {
        this.stammdaten = new TSInstitutionStammdaten();
        this.stammdaten.adresse = new TSAdresse();
        this.stammdaten.institution = institution;
        this.stammdaten.gueltigkeit = new TSDateRange();
        this.stammdaten.gueltigkeit.gueltigAb = moment();
    }

    private navigateBack(): void {
         this.$state.go('institution.list');
    }

    private navigateToWelcomesite(): void {
        if (this.isRegisteringInstitution) {
            this.$state.go('welcome');
            return;
        }
    }

    public getGueltigkeitTodisplay(): string {
        return `${this.translate.instant('AB')} ${DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigAb, CONSTANTS.DATE_FORMAT)}
             ${this.getBisDateIfSet()}`;
    }

    private getBisDateIfSet(): string {
        if (this.stammdaten.gueltigkeit.gueltigBis
            && DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigBis, CONSTANTS.DATE_FORMAT) !== CONSTANTS.END_OF_TIME_STRING
        ) {
            return ` ${this.translate.instant('BIS')} ${DateUtil.momentToLocalDateFormat(this.stammdaten.gueltigkeit.gueltigBis, CONSTANTS.DATE_FORMAT)}`;
        }
        return '';
    }

    public compareTraegerschaft(b1: TSTraegerschaft, b2: TSTraegerschaft): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }

    public getAlterskategorien(): string {
        const alterskategorien: string[] = [];
        if (this.stammdaten.alterskategorieBaby) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_BABY'));
        }
        if (this.stammdaten.alterskategorieVorschule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_VORSCHULE'));
        }
        if (this.stammdaten.alterskategorieKindergarten) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_KINDERGARTEN'));
        }
        if (this.stammdaten.alterskategorieSchule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_SCHULE'));
        }
        return alterskategorien.join(', ');
    }

    public getPlaceholderForPlaetze(): string {
        if (this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA) {
            return this.translate.instant('INSTITUTION_ANZAHL_PLAETZE_PLACEHOLDER_1');
        }
        if (this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN) {
            return this.translate.instant('INSTITUTION_ANZAHL_PLAETZE_PLACEHOLDER_2');
        }
        return '';
    }

    public getPlaceholderForOeffnungszeiten(): string {
        return this.translate.instant('INSTITUTION_OEFFNUNGSZEITEN_PLACEHOLDER');
    }
}

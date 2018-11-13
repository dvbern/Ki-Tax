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
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../core/service/institutionStammdatenRS.rest';
import * as moment from 'moment';

@Component({
  selector: 'dv-edit-institution',
  templateUrl: './edit-institution.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditInstitutionComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public institution: TSInstitution;
    public stammdaten: TSInstitutionStammdaten;
    public abweichendeZahlungsAdresse: boolean;
    public beguStartStr: string;
    public beguEndeStr: string;
    private navigationSource: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly institutionRS: InstitutionRS,
        private readonly institutionStammdatenRS: InstitutionStammdatenRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
    ) {
    }

    public ngOnInit(): void {
        const institutionId = this.$transition$.params().institutionId;
        if (!institutionId) {
            return;
        }
        this.navigationSource = this.$transition$.from();

        this.institutionRS.findInstitution(institutionId).then(institution => {
            this.institution = institution;

            this.institutionStammdatenRS.fetchInstitutionStammdatenByInstitution(institution.id).then(stammdaten => {
                if (stammdaten) {
                    this.stammdaten = stammdaten;
                } else {
                    this.createInstitutionStammdaten();
                }
                this.setBeguVonBisStr();
                this.abweichendeZahlungsAdresse = !!this.stammdaten.adresseKontoinhaber;
                this.changeDetectorRef.markForCheck();
            });
        });
    }

    private setBeguVonBisStr(): void {
        this.beguStartStr = this.stammdaten.gueltigkeit.gueltigAb ?
            this.stammdaten.gueltigkeit.gueltigAb.format('DD.MM.YYYY') : undefined;
        this.beguEndeStr = this.stammdaten.gueltigkeit.gueltigBis ?
            this.stammdaten.gueltigkeit.gueltigBis.format('DD.MM.YYYY') : undefined;
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public getHeaderTitle(): string {
        if (!this.institution.traegerschaft) {
            return this.institution.name;
        }
        return `${this.institution.name} (${this.institution.traegerschaft.name})`;
    }

    public persistStammdaten(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        // Reset Adresse Kontoinhaber if not used
        if (!this.abweichendeZahlungsAdresse) {
            this.stammdaten.adresseKontoinhaber = undefined;
        }
        // Prevent phone regex error in case of empty string
        if (this.stammdaten.telefon === '') {
            this.stammdaten.telefon = null;
        }
        this.institutionStammdatenRS.saveInstitutionStammdaten(this.stammdaten).then(() => this.navigateBack());
    }

    public onAbweichendeZahlungsAdresseClick(): void {
        if (!this.stammdaten.adresseKontoinhaber) {
            this.stammdaten.adresseKontoinhaber = new TSAdresse();
        }
    }

    private createInstitutionStammdaten(): void {
        this.stammdaten = new TSInstitutionStammdaten();
        this.stammdaten.adresse = new TSAdresse();
        this.stammdaten.institution = this.institution;
        this.stammdaten.gueltigkeit.gueltigAb = moment();
    }

    private navigateBack(): void {
        if (!this.navigationSource.name) {
            this.$state.go('gemeinde.list');
            return;
        }
        const redirectTo = this.navigationSource.name === 'einladung.abschliessen'
            ? 'gemeinde.view'
            : this.navigationSource;
        this.$state.go(redirectTo, {institutionId: this.institution.id});
    }
}

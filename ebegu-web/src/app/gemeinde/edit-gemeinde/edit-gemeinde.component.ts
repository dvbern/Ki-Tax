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
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import TSAdresse from '../../../models/TSAdresse';
import TSBenutzer from '../../../models/TSBenutzer';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-gemeinde',
    templateUrl: './edit-gemeinde.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditGemeindeComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public stammdaten: TSGemeindeStammdaten;
    public beguStart: string;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    public previewImageURL: string = '#';
    private fileToUpload!: File;
    private navigationSource: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.navigationSource = this.$transition$.from();
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (!gemeindeId) {
            return;
        }
        // TODO: Task KIBON-217: Load from DB
        this.previewImageURL = 'https://upload.wikimedia.org/wikipedia/commons/e/e8/Ostermundigen-coat_of_arms.svg';
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.gemeindeRS.getGemeindeStammdaten(gemeindeId).then(resStamm => {
            // TODO: GemeindeStammdaten über ein Observable laden, so entfällt changeDetectorRef.markForCheck(), siehe
            this.stammdaten = resStamm;
            if (!this.stammdaten.adresse) {
                this.stammdaten.adresse = new TSAdresse();
            }
            if (!this.stammdaten.beschwerdeAdresse) {
                this.stammdaten.beschwerdeAdresse = new TSAdresse();
            }
            this.beguStart = this.stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');

            this.changeDetectorRef.markForCheck();
        });
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        if (this.stammdaten.keineBeschwerdeAdresse) {
            // Reset Beschwerdeadresse if not used
            this.stammdaten.beschwerdeAdresse = undefined;
        }
        this.gemeindeRS.saveGemeindeStammdaten(this.stammdaten).then(response => {
            this.stammdaten = response;
            this.navigateBack();
        });
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public hatBeschwerdeAdresse(): boolean {
        this.stammdaten.beschwerdeAdresse = undefined;
        if (this.stammdaten.keineBeschwerdeAdresse) {
            this.stammdaten.beschwerdeAdresse = undefined;
            return false;
        }
        this.stammdaten.beschwerdeAdresse = new TSAdresse();
        return true;
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public handleInput(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.onload = (e: any): void => {
            this.previewImageURL = e.target.result;
        };
        tmpFileReader.readAsDataURL(this.fileToUpload);
    }

    private navigateBack(): void {
        if (this.navigationSource.name) {
            this.$state.go(this.navigationSource, {gemeindeId: this.stammdaten.gemeinde.id});
            return;
        }
        this.$state.go('gemeinde.list');
    }
}

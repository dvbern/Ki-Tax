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

import {ChangeDetectionStrategy, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-edit-institution-betreuungsgutscheine',
    templateUrl: './edit-institution-betreuungsgutscheine.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class EditInstitutionBetreuungsgutscheineComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;
    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean;

    public abweichendeZahlungsAdresse: boolean;

    public constructor(
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
    ) {
    }
    //
    public ngOnInit(): void {
        this.abweichendeZahlungsAdresse =
            !!this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber;
    }

    // TODO (hefr) das muss dann irgendwie vom äusseren aufgerufen werden!
    private persistStammdaten(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        if (!this.abweichendeZahlungsAdresse) { // Reset Adresse Kontoinhaber if not used
            this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber = undefined;
        }
    }

    public onAbweichendeZahlungsAdresseClick(): void {
        if (!this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber) {
            this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber = new TSAdresse();
        }
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
}

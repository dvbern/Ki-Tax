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

import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';

@Component({
    selector: 'dv-view-institution-betreuungsgutscheine',
    templateUrl: './view-institution-betreuungsgutscheine.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class ViewInstitutionBetreuungsgutscheineComponent {

    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean;

    public constructor(
        private readonly translate: TranslateService,
    ) {
    }

    public getAlterskategorien(): string {
        const alterskategorien: string[] = [];
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieBaby) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_BABY'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieVorschule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_VORSCHULE'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieKindergarten) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_KINDERGARTEN'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieSchule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_SCHULE'));
        }
        return alterskategorien.join(', ');
    }
}

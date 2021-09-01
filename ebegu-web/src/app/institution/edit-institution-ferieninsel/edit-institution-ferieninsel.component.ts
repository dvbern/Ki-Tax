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

import {ChangeDetectionStrategy, Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSEinstellungenFerieninsel} from '../../../models/TSEinstellungenFerieninsel';
import {TSGemeinde} from '../../../models/TSGemeinde';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {EbeguUtil} from '../../../utils/EbeguUtil';

@Component({
    selector: 'dv-edit-institution-ferieninsel',
    templateUrl: './edit-institution-ferieninsel.component.html',
    styleUrls: ['./edit-institution-ferieninsel.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ],
})

export class EditInstitutionFerieninselComponent implements OnInit, OnChanges {

    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean;

    public gemeindeList: TSGemeinde[] = [];

    public constructor(
        private readonly gemeindeRS: GemeindeRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeRS.getAllGemeinden().then(allGemeinden => {
            this.gemeindeList = allGemeinden;
        });
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.stammdaten && changes.stammdaten.currentValue) {
            this.sortByPeriod();
        }
    }

    private sortByPeriod(): void {
        this.stammdaten.institutionStammdatenFerieninsel.einstellungenFerieninsel.sort((a, b) => {
            if (a.gesuchsperiode && b.gesuchsperiode) {
                return b.gesuchsperiode.gesuchsperiodeString.localeCompare(a.gesuchsperiode.gesuchsperiodeString);
            }
            return -1;
        });
    }

    public trackById(einstellungGP: TSEinstellungenFerieninsel): string {
        return einstellungGP.id;
    }

    public showGesuchsperiode(gueltigkeit: TSDateRange): boolean {
        let showGesuchsperiode = gueltigkeit.gueltigBis.isAfter(this.stammdaten.gueltigkeit.gueltigAb);
        if (EbeguUtil.isNotNullOrUndefined(this.stammdaten.gueltigkeit.gueltigBis)) {
            showGesuchsperiode = showGesuchsperiode && gueltigkeit.gueltigAb.isBefore(this.stammdaten.gueltigkeit.gueltigBis);
        }
        return showGesuchsperiode;
    }

    public compareGemeinde(b1: TSGemeinde, b2: TSGemeinde): boolean {
        return b1 && b2 ? b1.id === b2.id : b1 === b2;
    }
}

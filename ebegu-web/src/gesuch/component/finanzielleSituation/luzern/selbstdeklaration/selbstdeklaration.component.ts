/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzielleSituationSelbstdeklaration} from '../../../../../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-selbstdeklaration',
    templateUrl: './selbstdeklaration.component.html',
    styleUrls: ['./selbstdeklaration.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelbstdeklarationComponent implements OnInit {

    @Input()
    public antragstellerNummer: number; // antragsteller 1 or 2

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public year: number | string;

    @Input()
    public model: TSFinanzielleSituationContainer;

    @Input()
    public readOnly: boolean = false;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        private finSitLuService: FinanzielleSituationLuzernService
    ) {
    }

    public ngOnInit(): void {
        if (!this.model.finanzielleSituationJA.selbstdeklaration) {
            this.model.finanzielleSituationJA.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
    }


    public onValueChangeFunction = (): void => {
        this.finSitLuService.calculateMassgebendesEinkommen(this.finanzModel);
    }

}

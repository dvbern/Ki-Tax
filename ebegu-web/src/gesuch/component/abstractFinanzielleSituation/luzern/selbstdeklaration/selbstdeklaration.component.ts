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
import {TSAbstractFinanzielleSituation} from '../../../../../models/TSAbstractFinanzielleSituation';
import {TSFinanzielleSituationSelbstdeklaration} from '../../../../../models/TSFinanzielleSituationSelbstdeklaration';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationLuzernService} from '../../../finanzielleSituation/luzern/finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-selbstdeklaration',
    templateUrl: './selbstdeklaration.component.html',
    styleUrls: ['./selbstdeklaration.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelbstdeklarationComponent implements OnInit {

    @Input()
    public antragstellerNummer: number; // antragsteller 1 or 2

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public basisJahr: number;

    @Input()
    public model: TSAbstractFinanzielleSituation;

    @Input()
    public readOnly: boolean = false;

    @Input()
    public isEKV: boolean = false;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        private readonly finSitLuService: FinanzielleSituationLuzernService,
        private readonly gesuchModelManager: GesuchModelManager,
    ) {
    }

    public ngOnInit(): void {
        if (!this.model.selbstdeklaration) {
            this.model.selbstdeklaration = new TSFinanzielleSituationSelbstdeklaration();
        }
        // load initial results
        this.onValueChangeFunction();
    }

    public onValueChangeFunction = (): void => {
        if (this.isEKV) {
            this.finSitLuService.calculateEinkommensverschlechterung(this.finanzModel, this.basisJahr);
        } else {
            this.finSitLuService.calculateMassgebendesEinkommen(this.finanzModel);
        }
    }

    public antragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName();
    }

    public antragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }
}

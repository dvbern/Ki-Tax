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
import {ControlContainer, NgForm} from '@angular/forms';
import {TSFinanzielleSituationContainer} from '../../../../../models/TSFinanzielleSituationContainer';
import {TSFinanzModel} from '../../../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationLuzernService} from '../finanzielle-situation-luzern.service';

@Component({
    selector: 'dv-veranlagung',
    templateUrl: './veranlagung.component.html',
    styleUrls: ['./veranlagung.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})
export class VeranlagungComponent implements OnInit {

    @Input()
    public isGemeinsam: boolean;

    @Input()
    public antragstellerNummer: number; // antragsteller 1 or 2

    @Input()
    public year: number | string;

    @Input() public model: TSFinanzielleSituationContainer;

    @Input()
    public readOnly: boolean;

    @Input()
    public finanzModel: TSFinanzModel;

    public constructor(
        private readonly finSitLuService: FinanzielleSituationLuzernService,
        private readonly gesuchModelManager: GesuchModelManager,
    ) {
    }

    public ngOnInit(): void {
        this.finSitLuService.calculateMassgebendesEinkommen(this.finanzModel);
    }

    public onValueChangeFunction = (): void => {
        this.finSitLuService.calculateMassgebendesEinkommen(this.finanzModel);
    }

    public antragsteller1Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName();
    }

    public antragsteller2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public antragstellerName(): string {
        return this.antragstellerNummer === 1 ?
            this.gesuchModelManager.getGesuch().gesuchsteller1?.extractFullName() :
            this.gesuchModelManager.getGesuch().gesuchsteller2?.extractFullName();
    }

    public isNotNullOrUndefined(toCheck: any): boolean {
        return EbeguUtil.isNotNullOrUndefined(toCheck);
    }
}

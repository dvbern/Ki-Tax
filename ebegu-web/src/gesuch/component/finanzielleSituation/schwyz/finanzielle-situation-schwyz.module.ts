/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../../app/shared/shared.module';
import {NgGesuchModule} from '../../../ng-gesuch.module';
import {BruttolohnSchwyzComponent} from './bruttolohn-schwyz/bruttolohn-schwyz.component';
import {FinanzielleSituationGsSchwyzComponent} from './finanzielle-situation-gs-schwyz/finanzielle-situation-gs-schwyz.component';
import {FinanzielleSituationSingleGsComponent} from './finanzielle-situation-single-gs/finanzielle-situation-single-gs.component';
import {
    FinanzielleSituationStartSchwyzComponent
} from './finanzielle-situation-start-schwyz/finanzielle-situation-start-schwyz.component';
import {SteuerveranlagtSchwyzComponent} from './steuerveranlagt-schwyz/steuerveranlagt-schwyz.component';

@NgModule({
    declarations: [
        SteuerveranlagtSchwyzComponent,
        BruttolohnSchwyzComponent,
        FinanzielleSituationStartSchwyzComponent,
        FinanzielleSituationGsSchwyzComponent,
        FinanzielleSituationSingleGsComponent,
    ],
    imports: [
        SharedModule,
        CommonModule,
    ],
})
export class FinanzielleSituationSchwyzModule {
}

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
import {EinkommensverschlechterungSchwyzGsComponent} from '../../einkommensverschlechterung/schwyz/einkommensverschlechterung-schwyz-gs/einkommensverschlechterung-schwyz-gs.component';
import {EinkommensverschlechterungSchwyzResultateComponent} from '../../einkommensverschlechterung/schwyz/einkommensverschlechterung-schwyz-resultate/einkommensverschlechterung-schwyz-resultate.component';
import {BruttolohnSchwyzComponent} from './bruttolohn-schwyz/bruttolohn-schwyz.component';
import {FinanzielleSituationGsSchwyzComponent} from './finanzielle-situation-gs-schwyz/finanzielle-situation-gs-schwyz.component';
import {FinanzielleSituationSingleGsSchwyzComponent} from './finanzielle-situation-single-gs-schwyz/finanzielle-situation-single-gs-schwyz.component';
import {FinanzielleSituationStartSchwyzComponent} from './finanzielle-situation-start-schwyz/finanzielle-situation-start-schwyz.component';
import {SteuerveranlagtSchwyzComponent} from './steuerveranlagt-schwyz/steuerveranlagt-schwyz.component';
import {FinanzielleSituationResultateSchwyzComponent} from './finanzielle-situation-resultate-schwyz/finanzielle-situation-resultate-schwyz.component';

@NgModule({
    declarations: [
        SteuerveranlagtSchwyzComponent,
        BruttolohnSchwyzComponent,
        FinanzielleSituationStartSchwyzComponent,
        FinanzielleSituationGsSchwyzComponent,
        FinanzielleSituationSingleGsSchwyzComponent,
        EinkommensverschlechterungSchwyzGsComponent,
        EinkommensverschlechterungSchwyzResultateComponent,
        FinanzielleSituationResultateSchwyzComponent
    ],
    imports: [SharedModule, CommonModule]
})
export class FinanzielleSituationSchwyzModule {}

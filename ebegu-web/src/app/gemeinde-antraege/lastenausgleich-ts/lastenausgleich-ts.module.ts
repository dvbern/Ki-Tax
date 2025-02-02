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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {RouterModule} from '@angular/router';
import {UIRouterModule} from '@uirouter/angular';
import {SharedModule} from '../../shared/shared.module';
import {WizardstepXModule} from '../../wizardstepX/wizardstep-x.module';
import {FreigabeComponent} from './antrag/freigabe/freigabe.component';
import {GemeindeAngabenComponent} from './antrag/gemeinde-angaben/gemeinde-angaben.component';
import {LastenausgleichTsBerechnungComponent} from './antrag/lastenausgleich-ts-berechnung/lastenausgleich-ts-berechnung.component';
import {TagesschulenAngabenComponent} from './antrag/tagesschulen-angaben/tagesschulen-angaben.component';
import {TagesschulenListComponent} from './antrag/tagesschulen-list/tagesschulen-list.component';
import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar/lastenausgleich-ts-kommentar.component';
import {LastenausgleichTsRoutingModule} from './lastenausgleich-ts-routing/lastenausgleich-ts-routing.module';
import {LastenausgleichTSComponent} from './lastenausgleich-ts/lastenausgleich-ts.component';
import {TagesschulenUiViewComponent} from './tagesschulen-ui-view/tagesschulen-ui-view.component';

@NgModule({
    declarations: [
        LastenausgleichTSComponent,
        LastenausgleichTsKommentarComponent,
        GemeindeAngabenComponent,
        TagesschulenAngabenComponent,
        FreigabeComponent,
        LastenausgleichTsBerechnungComponent,
        TagesschulenListComponent,
        TagesschulenUiViewComponent
    ],
    imports: [
        CommonModule,
        LastenausgleichTsRoutingModule,
        MatToolbarModule,
        MatSidenavModule,
        RouterModule,
        UIRouterModule,
        SharedModule,
        ReactiveFormsModule,
        WizardstepXModule
    ]
})
export class LastenausgleichTSModule {}

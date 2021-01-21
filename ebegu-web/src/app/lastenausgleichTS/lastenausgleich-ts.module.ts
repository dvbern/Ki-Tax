/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import {MatButtonModule} from '@angular/material/button';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {RouterModule} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {SharedModule} from '../shared/shared.module';
import {WizardstepXModule} from '../wizardstepX/wizardstep-x.module';
import {FreigabeComponent} from './antrag/freigabe/freigabe.component';
import {GemeindeAngabenComponent} from './antrag/gemeinde-angaben/gemeinde-angaben.component';
import {LastenausgleichTsBerechnungComponent} from './antrag/lastenausgleich-ts-berechnung/lastenausgleich-ts-berechnung.component';
import {TagesschulenAngabenComponent} from './antrag/tagesschulen-angaben/tagesschulen-angaben.component';
import {LastenausgleichTsKommentarComponent} from './lastenausgleich-ts-kommentar/lastenausgleich-ts-kommentar.component';
import {LastenausgleichTsRoutingModule} from './lastenausgleich-ts-routing/lastenausgleich-ts-routing.module';
import {LastenausgleichTsSideNavComponent} from './lastenausgleich-ts-side-nav/lastenausgleich-ts-side-nav.component';
import {LastenausgleichTsToolbarComponent} from './lastenausgleich-ts-toolbar/lastenausgleich-ts-toolbar.component';
import {LastenausgleichTSComponent} from './lastenausgleich-ts/lastenausgleich-ts.component';
import { GemeindeAntraegeComponent } from './gemeinde-antraege/gemeinde-antraege.component';
import { TagesschulenListComponent } from './antrag/tagesschulen-list/tagesschulen-list.component';

@NgModule({
    declarations: [
        LastenausgleichTSComponent,
        LastenausgleichTsSideNavComponent,
        LastenausgleichTsKommentarComponent,
        LastenausgleichTsToolbarComponent,
        GemeindeAngabenComponent,
        TagesschulenAngabenComponent,
        FreigabeComponent,
        LastenausgleichTsBerechnungComponent,
        GemeindeAntraegeComponent,
        TagesschulenListComponent
    ],
    imports: [
        CommonModule,
        LastenausgleichTsRoutingModule,
        MatToolbarModule,
        MatSidenavModule,
        RouterModule,
        UIRouterModule,
        MatMenuModule,
        MatButtonModule,
        WizardstepXModule,
        SharedModule,
        ReactiveFormsModule,
    ],
})
export class LastenausgleichTSModule {
}

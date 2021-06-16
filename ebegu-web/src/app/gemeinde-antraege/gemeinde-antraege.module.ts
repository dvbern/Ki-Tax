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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {FerienbetreuungModule} from './ferienbetreuung/ferienbetreuung.module';
import {GemeindeAntraegeRoutingModule} from './gemeinde-antraege-routing/gemeinde-antraege-routing.module';
import {VerlaufComponent} from './gemeinde-antraege/antrag/verlauf/verlauf.component';
import {GemeindeAntraegeComponent} from './gemeinde-antraege/gemeinde-antraege.component';
import {LastenausgleichTSModule} from './lastenausgleich-ts/lastenausgleich-ts.module';
import {UnsavedChangesService} from './services/unsaved-changes.service';

@NgModule({
    declarations: [
        GemeindeAntraegeComponent,
        VerlaufComponent,
    ],
    imports: [
        CommonModule,
        LastenausgleichTSModule,
        FerienbetreuungModule,
        GemeindeAntraegeRoutingModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule
    ],
    exports: [
        LastenausgleichTSModule,
        FerienbetreuungModule,
    ],
    providers: [
        UnsavedChangesService
    ]
})
export class GemeindeAntraegeModule {
}

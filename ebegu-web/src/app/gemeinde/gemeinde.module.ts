/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {AddGemeindeComponent} from './add-gemeinde/add-gemeinde.component';
import {EditGemeindeComponent} from './edit-gemeinde/edit-gemeinde.component';
import {GemeindeHeaderComponent} from './gemeinde-header/gemeinde-header.component';
import {GemeindeKonfigComponent} from './gemeinde-konfiguration/gemeinde-konfig.component';
import {GemeindeListComponent} from './gemeinde-list/gemeinde-list.component';
import {GemeindeRoutingModule} from './gemeinde-routing/gemeinde-routing.module';
import {ViewGemeindeComponent} from './view-gemeinde/view-gemeinde.component';

@NgModule({
    imports: [
        SharedModule,
        GemeindeRoutingModule,
        MaterialModule,
    ],
    // adding custom elements schema disables Angular's element validation: you can now use transclusion for the
    // dv-accordion-tab with multi-slot transclusion (tab-title & tab-body elements).
    // See https://stackoverflow.com/a/51214263
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        GemeindeListComponent,
        AddGemeindeComponent,
        EditGemeindeComponent,
        ViewGemeindeComponent,
        GemeindeKonfigComponent,
        GemeindeHeaderComponent,
    ],
    entryComponents: [
        GemeindeListComponent,
        AddGemeindeComponent,
        EditGemeindeComponent,
        ViewGemeindeComponent,
        GemeindeKonfigComponent,
        GemeindeHeaderComponent,
    ],
    providers: [],
})
export class GemeindeModule {
}

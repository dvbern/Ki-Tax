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
import {NgxIbanModule} from 'ngx-iban';
import {MaterialModule} from '../shared/material.module';
import {SharedModule} from '../shared/shared.module';
import {AddGemeindeComponent} from './add-gemeinde/add-gemeinde.component';
import {EditGemeindeComponentBG} from './edit-gemeinde-bg/edit-gemeinde-bg.component';
import {EditGemeindeComponentFI} from './edit-gemeinde-fi/edit-gemeinde-fi.component';
import {EditGemeindeComponentStammdaten} from './edit-gemeinde-stammdaten/edit-gemeinde-stammdaten.component';
import {EditGemeindeComponentTS} from './edit-gemeinde-ts/edit-gemeinde-ts.component';
import {EditGemeindeComponent} from './edit-gemeinde/edit-gemeinde.component';
import {GemeindeFiKonfigComponent} from './gemeinde-fi-konfiguration/gemeinde-fi-konfig.component';
import {OverlappingZeitraumDirective} from './gemeinde-fi-konfiguration/overlapping-zeitraum.directive';
import {GemeindeListComponent} from './gemeinde-list/gemeinde-list.component';
import {GemeindeRoutingModule} from './gemeinde-routing/gemeinde-routing.module';
import {GemeindeTsKonfigComponent} from './gemeinde-ts-konfiguration/gemeinde-ts-konfig.component';

@NgModule({
    imports: [
        SharedModule,
        GemeindeRoutingModule,
        MaterialModule,
        NgxIbanModule,
    ],
    // adding custom elements schema disables Angular's element validation: you can now use transclusion for the
    // dv-accordion-tab with multi-slot transclusion (tab-title & tab-body elements).
    // See https://stackoverflow.com/a/51214263
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    declarations: [
        GemeindeListComponent,
        AddGemeindeComponent,
        EditGemeindeComponent,
        EditGemeindeComponentBG,
        EditGemeindeComponentFI,
        EditGemeindeComponentStammdaten,
        EditGemeindeComponentTS,
        GemeindeTsKonfigComponent,
        GemeindeFiKonfigComponent,
        OverlappingZeitraumDirective
    ],
    providers: [],
})
export class GemeindeModule {
}

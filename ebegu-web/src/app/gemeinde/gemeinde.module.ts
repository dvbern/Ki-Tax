/*
 * AGPL File-Header
 *
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

import {NgModule} from '@angular/core';
import {SharedModule} from '../shared/shared.module';
import {AddGemeindeComponent} from './add-gemeinde/add-gemeinde.component';
import {EditGemeindeComponent} from './edit-gemeinde/edit-gemeinde.component';
import {GemeindeListComponent} from './gemeinde-list/gemeinde-list.component';
import {GemeindeRoutingModule} from './gemeinde-routing/gemeinde-routing.module';

@NgModule({
    imports: [
        SharedModule,
        GemeindeRoutingModule,
    ],
    declarations: [
        GemeindeListComponent,
        AddGemeindeComponent,
        EditGemeindeComponent,
    ],
    entryComponents: [
        GemeindeListComponent,
        AddGemeindeComponent,
        EditGemeindeComponent,
    ],
    providers: [],
})
export class GemeindeModule {
}

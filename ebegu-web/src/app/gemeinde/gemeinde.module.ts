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

import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {NgAdminRoutingModule} from '../../admin/ng-admin-routing.module';
import {SharedModule} from '../shared/shared.module';
import {AddGemeindeComponent} from './add-gemeinde/add-gemeinde.component';
import {GemeindeListComponent} from './gemeinde-list/gemeinde-list.component';

@NgModule({
    imports: [
        SharedModule,
        NgAdminRoutingModule,
    ],
    declarations: [
        GemeindeListComponent,
        AddGemeindeComponent,
    ],
    entryComponents: [
        GemeindeListComponent,
        AddGemeindeComponent,
    ],
    providers: [],
})
export class GemeindeModule {
}


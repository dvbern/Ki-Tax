/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {NgModule} from '@angular/core';
import {GemeindeListComponent} from '../app/gemeindeList/gemeinde-list.component';
import {SharedModule} from '../app/shared/shared.module';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {DebuggingComponent} from './component/debugging/debugging.component';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {TraegerschaftViewComponent} from './component/traegerschaftView/traegerschaftView';
import {NgAdminRoutingModule} from './ng-admin-routing.module';

@NgModule({
    imports: [
        SharedModule,
        NgAdminRoutingModule,
    ],
    declarations: [
        TraegerschaftViewComponent,
        GemeindeListComponent,
        TestdatenViewComponent,
        BatchjobTriggerViewComponent,
        DebuggingComponent,
    ],
    entryComponents: [
        TraegerschaftViewComponent,
        GemeindeListComponent,
        TestdatenViewComponent,
        BatchjobTriggerViewComponent,
        DebuggingComponent,
    ],
    providers: [],
})
export class NgAdminModule {
}


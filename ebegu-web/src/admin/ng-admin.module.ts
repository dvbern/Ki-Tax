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

import {NgModule} from '@angular/core';
import {SharedModule} from '../app/shared/shared.module';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {BetreuungMonitoringComponent} from './component/betreuung-monitoring/betreuung-monitoring.component';
import {DebuggingComponent} from './component/debugging/debugging.component';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';
import {NgAdminRoutingModule} from './ng-admin-routing.module';

@NgModule({
    imports: [
        SharedModule,
        NgAdminRoutingModule,
    ],
    declarations: [
        TestdatenViewComponent,
        BatchjobTriggerViewComponent,
        DebuggingComponent,
        BetreuungMonitoringComponent
    ],
    providers: [],
})
export class NgAdminModule {
}

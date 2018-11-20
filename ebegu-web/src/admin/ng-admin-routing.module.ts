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
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {BenutzerComponent} from '../app/benutzer/benutzer/benutzer.component';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView';
import {DebuggingComponent} from './component/debugging/debugging.component';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView';

const states: Ng2StateDeclaration[] = [
    {
        name: 'admin.testdaten',
        url: '/testdaten',
        component: TestdatenViewComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles(),
        },
    },
    {
        name: 'admin.batchjobTrigger',
        url: '/batchjobTrigger',
        component: BatchjobTriggerViewComponent,
    },
    {
        name: 'admin.debugging',
        url: '/debug',
        component: DebuggingComponent,
    },
    {
        name: 'admin.benutzer',
        component: BenutzerComponent,
        url: '/benutzerlist/benutzer/:benutzerId',
        data: {
            roles: TSRoleUtil.getAllAdministratorRevisorRole(),
        },
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
})
export class NgAdminRoutingModule {
}

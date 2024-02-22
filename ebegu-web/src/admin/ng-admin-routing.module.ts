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
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {BenutzerComponent} from '../app/benutzer/benutzer/benutzer.component';
import {ApplicationPropertyRS} from '../app/core/rest-services/applicationPropertyRS.rest';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {AdminViewXComponent} from './component/admin-view-x/admin-view-x.component';
import {BatchjobTriggerViewComponent} from './component/batchjobTriggerView/batchjobTriggerView.component';
import {BenutzerListViewXComponent} from './component/benutzerListView/benutzer-list-view-x.component';
import {BetreuungMonitoringComponent} from './component/betreuung-monitoring/betreuung-monitoring.component';
import {DebuggingComponent} from './component/debugging/debugging.component';
import {GesuchsperiodeListViewXComponent} from './component/gesuchsperiode-list-view-x/gesuchsperiode-list-view-x.component';
import {GesuchsperiodeViewXComponent} from './component/gesuchsperiode-view-x/gesuchsperiode-view-x.component';
import {TestdatenViewComponent} from './component/testdatenView/testdatenView.component';
import {UebersichtVersendeteMailsComponent} from './component/uebersichtVersendeteMails/uebersichtVersendeteMails';

const applicationPropertiesResolver = [
    'ApplicationPropertyRS', (applicationPropertyRS: ApplicationPropertyRS) => applicationPropertyRS.getAllApplicationProperties()
];

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'admin',
        data: {
            roles: TSRoleUtil.getAdministratorRoles()
        }
    },
    {
        name: 'admin.view',
        component: AdminViewXComponent,
        url: '/admin',
        resolve: {
            applicationProperties: applicationPropertiesResolver
        },
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    },
    {
        name: 'admin.testdaten',
        url: '/testdaten',
        component: TestdatenViewComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    },
    {
        name: 'admin.batchjobTrigger',
        url: '/batchjobTrigger',
        component: BatchjobTriggerViewComponent
    },
    {
        name: 'admin.debugging',
        url: '/debug',
        component: DebuggingComponent
    },
    {
        name: 'admin.benutzer',
        component: BenutzerComponent,
        url: '/benutzerlist/benutzer/:benutzerId',
        data: {
            roles: TSRoleUtil.getAllAdministratorRevisorRole()
        }
    },
    {
        name: 'admin.betreuungMonitoring',
        url: '/betreuungMonitoring',
        component: BetreuungMonitoringComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    },
    {
        name: 'admin.gesuchsperioden',
        url: '/gesuchsperioden',
        component: GesuchsperiodeListViewXComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    },
    {
        name: 'admin.gesuchsperiode',
        url: '/parameter/gesuchsperiode/:gesuchsperiodeId',
        component: GesuchsperiodeViewXComponent,
        params: {
            gesuchsperiodeId: ''
        },
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    },
    {
        name: 'admin.benutzerlist',
        url: '/benutzerlist',
        component: BenutzerListViewXComponent,
        data: {
            roles: TSRoleUtil.getAllAdministratorRevisorRole()
        }
    },
    {
        name: 'admin.uebersichtVersendeteMails',
        url: '/uebersichtVersendeteMail',
        component: UebersichtVersendeteMailsComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles()
        }
    }
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states})
    ],
    exports: [
        UIRouterUpgradeModule
    ]
})
export class NgAdminRoutingModule {
}

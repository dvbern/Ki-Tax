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
import {Permission} from '../../authorisation/Permission';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {EinladungAbschliessenComponent} from '../einladung-abschliessen/einladung-abschliessen.component';
import {EinladungErrorComponent} from '../einladung-error/einladung-error.component';
import {LoginInfoComponent} from '../login-info/login-info.component';

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        name: 'einladung',
        abstract: true,
        url: '/einladung',
        redirectTo: 'einladung.logininfo',
        component: UiViewComponent,
        data: {
            roles: Permission.EINLADUNG_AKZEPTIEREN,
        },
    },
    {
        name: 'einladung.logininfo',
        url: '/',
        component: LoginInfoComponent,
    },
    {
        name: 'einladung.abschliessen',
        component: EinladungAbschliessenComponent,
    },
    {
        name: 'einladung.error',
        component: EinladungErrorComponent,
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
    declarations: [],
})
export class EinladungRoutingModule {
}

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
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {TraegerschaftAddComponent} from '../traegerschaft-add/traegerschaft-add.component';
import {TraegerschaftEditComponent} from '../traegerschaft-edit/traegerschaft-edit.component';
import {TraegerschaftListComponent} from '../traegerschaft-list/traegerschaft-list.component';

export const STATES: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'traegerschaft',
        abstract: true,
        url: '/traegerschaft',
        component: UiViewComponent,
    },
    {
        name: 'traegerschaft.list',
        url: '/list',
        component: TraegerschaftListComponent,
        data: {
            roles: TSRoleUtil.getMandantRoles(),
        },
    },
    {
        name: 'traegerschaft.edit',
        url: '/edit/:traegerschaftId',
        component: TraegerschaftEditComponent,
        data: {
            roles: TSRoleUtil.getMandantRoles(),
        },
    },
    {
        name: 'traegerschaft.add',
        url: '/add',
        component: TraegerschaftAddComponent,
        data: {
            roles: TSRoleUtil.getMandantRoles(),
        },
    },
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states: STATES}),
    ],
    exports: [
        UIRouterUpgradeModule,
    ],
})
export class TraegerschaftRoutingModule {
}

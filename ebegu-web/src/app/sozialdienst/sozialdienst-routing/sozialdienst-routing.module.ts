/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
import {AddSozialdienstComponent} from '../add-sozialdienst/add-sozialdienst.component';
import {ListSozialdienstComponent} from '../list-sozialdienst/list-sozialdienst.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'sozialdienst',
        abstract: true,
        url: '/sozialdienst',
        component: UiViewComponent,
    },
    {
        name: 'sozialdienst.list',
        url: '/list',
        component: ListSozialdienstComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles(),
        },
    },
    {
        name: 'sozialdienst.add',
        url: '/add',
        component: AddSozialdienstComponent,
        data: {
            roles: TSRoleUtil.getSuperAdminRoles(),
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
export class SozialdienstRoutingModule {
}

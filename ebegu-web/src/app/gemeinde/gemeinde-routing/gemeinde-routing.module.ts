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
import {AddGemeindeComponent} from '../add-gemeinde/add-gemeinde.component';
import {EditGemeindeComponent} from '../edit-gemeinde/edit-gemeinde.component';
import {GemeindeListComponent} from '../gemeinde-list/gemeinde-list.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'gemeinde',
        abstract: true,
        url: '/gemeinde',
        component: UiViewComponent
    },
    {
        name: 'gemeinde.list',
        url: '/list',
        component: GemeindeListComponent,
        data: {
            roles: TSRoleUtil.getAdministratorMandantRevisorRole()
        }
    },
    {
        name: 'gemeinde.add',
        url: '/add',
        component: AddGemeindeComponent,
        data: {
            roles: TSRoleUtil.getMandantRoles()
        }
    },
    {
        name: 'gemeinde.edit',
        url: '/edit/:gemeindeId/:isRegistering',
        component: EditGemeindeComponent,
        data: {
            roles: TSRoleUtil.getAdministratorBgTsGemeindeRole().concat(TSRoleUtil.getMandantRoles())
        },
        params: {
            isRegistering: {
                type: 'bool',
                // this parameter is optional: specify a default value
                value: false
            }
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
export class GemeindeRoutingModule {
}

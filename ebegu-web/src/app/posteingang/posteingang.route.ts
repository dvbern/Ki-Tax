/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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
import {
    NgHybridStateDeclaration,
    UIRouterUpgradeModule
} from '@uirouter/angular-hybrid';
import {TSRoleUtil} from '../../utils/TSRoleUtil';
import {PosteingangViewComponent} from './component/posteingang-view.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        abstract: true,
        name: 'posteingang',
        data: {
            roles: TSRoleUtil.getAllRolesForPosteingang()
        }
    },
    {
        name: 'posteingang.view',
        url: '/posteingang',
        component: PosteingangViewComponent
    }
];

@NgModule({
    imports: [UIRouterUpgradeModule.forChild({states})],
    exports: [UIRouterUpgradeModule]
})
export class PosteingangRoutingModule {}

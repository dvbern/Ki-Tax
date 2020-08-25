/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {NgModule} from '@angular/core';
import {NgHybridStateDeclaration, UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {NotrechtComponent} from '../notrecht/notrecht.component';
import {RueckforderungFormularComponent} from '../rueckforderung-formular/rueckforderung-formular.component';

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'notrecht',
        abstract: true,
        url: '/corona-finanzierung',
        component: UiViewComponent,
    },
    {
        name: 'notrecht.list',
        url: '/list',
        component: NotrechtComponent,
        data: {
            roles: TSRoleUtil.getAllRolesForNotrecht(),
        },
    },
    {
        name: 'notrecht.form',
        component: RueckforderungFormularComponent,
        url: '/list/rueckforderung/:rueckforderungId',
        data: {
            roles: TSRoleUtil.getAllRolesForNotrecht(),
        },
    },
    {
        name: 'notrecht.formWithAnchor',
        component: RueckforderungFormularComponent,
        url: '/list/rueckforderung/:rueckforderungId/:anchor',
        data: {
            roles: TSRoleUtil.getAllRolesForNotrecht(),
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
export class NotrechtRoutingModule {
}

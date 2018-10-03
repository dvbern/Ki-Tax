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
import {Ng2StateDeclaration} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {Transition} from '@uirouter/core';
import {getTSRoleValues} from '../models/enums/TSRole';
import {returnTo} from './authentication.route';
import {LocalLoginComponent} from './local-login/local-login.component';

export const LOCALLOGIN_STATE: Ng2StateDeclaration = {
    name: 'authentication.locallogin',
    url: '/locallogin',
    component: LocalLoginComponent,
    resolve: [
        {
            token: 'returnTo',
            deps: [Transition],
            resolveFn: returnTo,
        },
    ],
    data: {
        roles: getTSRoleValues(),
        requiresDummyLogin: true,
    },
};

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states: [LOCALLOGIN_STATE]}),
    ],
    exports: [],
})
export class NgAuthenticationRoutingModule {
}

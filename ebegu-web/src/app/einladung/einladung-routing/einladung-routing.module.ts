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
import {IPromise} from 'angular';
import {take} from 'rxjs/operators';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../models/enums/TSRole';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {ignoreNullAndUndefined} from '../../../utils/rxjs-operators';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {EinladungAbschliessenComponent} from '../einladung-abschliessen/einladung-abschliessen.component';
import {LoginInfoComponent} from '../login-info/login-info.component';
import {handleLoggedInUser} from './einladung-helpers';

authentication.$inject = ['AuthServiceRS'];

export function authentication(authService: AuthServiceRS): IPromise<TSBenutzer> {
    return authService.principal$
        .pipe(
            ignoreNullAndUndefined(),
            take(1))
        .toPromise();
}

const states: NgHybridStateDeclaration[] = [
    {
        parent: 'app',
        name: 'einladung',
        url: '/einladung?typ&userid&entityid',
        redirectTo: handleLoggedInUser,
        component: UiViewComponent,
        params: {
            typ: {
                type: 'string'
            },
            userid: {
                type: 'string'
            },
            entityid: {
                type: 'string',
                // this parameter is optional: specify a default value
                value: ''
            }
        }
    },
    {
        name: 'einladung.logininfo',
        url: '/login',
        component: LoginInfoComponent,
        data: {
            roles: [TSRole.ANONYMOUS]
        }
    },
    {
        name: 'einladung.abschliessen',
        url: '/abschliessen',
        component: EinladungAbschliessenComponent,
        data: {
            // Da ein Mitarbeiter mit irgend einer Rolle angelegt werden kann, müssen wir alle Rollen erlauben
            roles: TSRoleUtil.getAllRolesButAnonymous() // anonyme benutzer werden vom authentication.hook umgeleitet
                                                         // zur loginpage
        },
        resolve:
            {
                principal: authentication
            }
    }
];

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({states})
    ],
    exports: [
        UIRouterUpgradeModule
    ],
    declarations: []
})
export class EinladungRoutingModule {
}

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
import {Ng2StateDeclaration, RedirectToResult, TargetState, Transition} from '@uirouter/angular';
import {UIRouterUpgradeModule} from '@uirouter/angular-hybrid';
import {map, take} from 'rxjs/operators';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSEinladungTyp} from '../../../models/enums/TSEinladungTyp';
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import {getRoleBasedTargetState} from '../../../utils/AuthenticationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {UiViewComponent} from '../../shared/ui-view/ui-view.component';
import {EinladungAbschliessenComponent} from '../einladung-abschliessen/einladung-abschliessen.component';
import {EinladungErrorComponent} from '../einladung-error/einladung-error.component';
import {LoginInfoComponent} from '../login-info/login-info.component';

function getEntityTargetState(transition: Transition, principal: TSBenutzer): TargetState {
    const stateService = transition.router.stateService;

    const params = transition.params();
    const entityId: string = params.entityid;
    const typ: TSEinladungTyp = params.typ;

    switch (typ) {
        case TSEinladungTyp.MITARBEITER:
            return getRoleBasedTargetState(principal.getCurrentRole(), stateService);
        case TSEinladungTyp.GEMEINDE:
            return stateService.target('gemeinde.list');
        case TSEinladungTyp.TRAEGERSCHAFT:
            return stateService.target('admin.traegerschaft');
        case TSEinladungTyp.INSTITUTION:
            return stateService.target('admin.institution', {institutionId: entityId});
        default:
            throw new Error(`unrecognised EinladungTyp ${typ}`);
    }
}

function handleLoggedInUser(transition: Transition): Promise<RedirectToResult> {
    const authService: AuthServiceRS = transition.injector().get('AuthServiceRS');
    const stateService = transition.router.stateService;

    return authService.principal$
        .pipe(
            take(1),
            map(principal => {
                    if (!principal) {
                        return stateService.target('einladung.logininfo', transition.params(), transition.options());
                    }

                    // we are logged: redirect to the new entity
                    return getEntityTargetState(transition, principal);
                },
            ),
        )
        .toPromise();
}

const states: Ng2StateDeclaration[] = [
    {
        parent: 'app',
        name: 'einladung',
        url: '/einladung?typ&userid',
        redirectTo: handleLoggedInUser,
        component: UiViewComponent,
        params: {
            typ: {
                type: 'string',
            },
            userid: {
                type: 'string',
            },
            entityid: {
                type: 'string',
                // this parameter is optional: specify a default value
                value: '',
            },
        },
    },
    {
        name: 'einladung.logininfo',
        url: '/login',
        component: LoginInfoComponent,
        data: {
            roles: [TSRole.ANONYMOUS],
        },
    },
    {
        name: 'einladung.abschliessen',
        url: '/abschliessen',
        component: EinladungAbschliessenComponent,
        data: {
            // Da ein Mitarbeiter mit irgend einer Rolle angelegt werden kann, müssen wir alle Rollen erlauben
            roles: TSRoleUtil.getAllRolesButAnonymous(),
        },
    },
    {
        name: 'einladung.error',
        component: EinladungErrorComponent,
        // TODO Berechtigung überlegen
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

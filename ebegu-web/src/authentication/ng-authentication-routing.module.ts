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
import {
    NgHybridStateDeclaration,
    UIRouterUpgradeModule
} from '@uirouter/angular-hybrid';
import {Transition} from '@uirouter/core';
import {getTSRoleValues} from '../models/enums/TSRole';
import {returnTo} from './authentication.route';
import {LocalLoginComponent} from './local-login/local-login.component';
import {TutorialGemeindeLoginComponent} from './tutorial/tutorial-gemeinde-login/tutorial-gemeinde-login.component';
import {TutorialInstitutionLoginComponent} from './tutorial/tutorial-institution-login/tutorial-institution-login.component';

export const LOCALLOGIN_STATE: NgHybridStateDeclaration = {
    name: 'authentication.locallogin',
    url: '/locallogin',
    component: LocalLoginComponent,
    resolve: [
        {
            token: 'returnTo',
            deps: [Transition],
            resolveFn: returnTo
        }
    ],
    data: {
        roles: getTSRoleValues(),
        requiresDummyLogin: true
    }
};

export const TUTORIAL_INSTITUTION_LOGIN_STATE: NgHybridStateDeclaration = {
    name: 'authentication.tutorialInstitutionLogin',
    url: '/tutorial/institution',
    component: TutorialInstitutionLoginComponent,
    resolve: [
        {
            token: 'returnTo',
            deps: [Transition],
            resolveFn: returnTo
        }
    ],
    data: {
        roles: getTSRoleValues(),
        requiresDummyLogin: true
    }
};

export const TUTORIAL_GEMEINDE_LOGIN_STATE: NgHybridStateDeclaration = {
    name: 'authentication.tutorialGemeindeLogin',
    url: '/tutorial/gemeinde',
    component: TutorialGemeindeLoginComponent,
    resolve: [
        {
            token: 'returnTo',
            deps: [Transition],
            resolveFn: returnTo
        }
    ],
    data: {
        roles: getTSRoleValues(),
        requiresDummyLogin: true
    }
};

@NgModule({
    imports: [
        UIRouterUpgradeModule.forChild({
            states: [
                LOCALLOGIN_STATE,
                TUTORIAL_INSTITUTION_LOGIN_STATE,
                TUTORIAL_GEMEINDE_LOGIN_STATE
            ]
        })
    ],
    exports: []
})
export class NgAuthenticationRoutingModule {}

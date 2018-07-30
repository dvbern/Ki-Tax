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

import {IComponentOptions} from 'angular';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import AuthenticationUtil from '../../../utils/AuthenticationUtil';
import TSUser from '../../../models/TSUser';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {AuthLifeCycleService} from '../../service/authLifeCycle.service';
const template = require('./startView.html');
require('./startView.less');

export class StartComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
    controller = StartViewController;
    controllerAs = 'vm';
}

export class StartViewController {


    static $inject: string[] = ['$state', 'AuthLifeCycleService', 'AuthServiceRS'];

    constructor(private readonly $state: StateService, private readonly authLifeCycleService: AuthLifeCycleService, private readonly authService: AuthServiceRS) {


    }

    $onInit() {
        // todo KIBON-143 warten bis das event login_success geworfen ist
        const user: TSUser = this.authService.getPrincipal();
        if (this.authService.getPrincipal()) {  // wenn logged in
            AuthenticationUtil.navigateToStartPageForRole(user, this.$state);
        } else {
            //wenn wir noch nicht eingeloggt sind werden wir das event welches das login prozedere anstoesst
            this.authLifeCycleService.changeAuthStatus(TSAuthEvent.NOT_AUTHENTICATED, 'not logged in on startpage');
        }
    }
}

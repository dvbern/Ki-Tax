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

import * as angular from 'angular';
import {authenticationHookRunBlock} from './authentication.hook';
import {authenticationRoutes} from './authentication.route';
import {authorisationHookRunBlock} from './authorisation.hook';
import {dummyLoginHookRunBlock} from './dummyLogin.hook';
import {LoginComponentConfig} from './login/login.component';
import {SchulungComponentConfig} from './schulung/schulung.component';
import AuthServiceRS from './service/AuthServiceRS.rest';
import HttpAuthInterceptor from './service/HttpAuthInterceptor';
import HttpBuffer from './service/HttpBuffer';

export const EbeguAuthentication: angular.IModule =
    angular.module('dvbAngular.authentication', ['ngCookies'])
        .run(authenticationHookRunBlock)
        .run(authorisationHookRunBlock)
        .run(dummyLoginHookRunBlock)
        .run(authenticationRoutes)
        .service('HttpAuthInterceptor', HttpAuthInterceptor)
        .service('AuthServiceRS', AuthServiceRS)
        .service('httpBuffer', HttpBuffer)
        .component('dvSchulung', SchulungComponentConfig)
        .component('dvLogin', LoginComponentConfig);

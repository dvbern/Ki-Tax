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
import {authenticationRoutes} from './authentication.route';
import {LoginConfig} from './login/login';
import {SCHULUNG_CONFIG} from './schulung/schulungView';
import {AuthServiceRS} from './service/AuthServiceRS.rest';
import {HttpAuthInterceptor} from './service/HttpAuthInterceptor';
import {authenticationHookRunBlock} from './state-hooks/onBefore/authentication.hook';
import {authorisationHookRunBlock} from './state-hooks/onBefore/authorisation.hook';
import {dummyLoginHookRunBlock} from './state-hooks/onBefore/dummyLogin.hook';
import {mandantCheck} from './state-hooks/onBefore/mandant.hook';
import {errorAfterLoginHookRunBlock} from './state-hooks/onError/errorAfterLogin.hook';
import {erorGSRegistrationIncompleteHookRunBlock} from './state-hooks/onError/errorGSRegistrationIncomplete.hook';
import {errorLoggerHookRunBlock} from './state-hooks/onError/errorLogger.hook';
import {errorRecoveryHookRunBlock} from './state-hooks/onError/errorRecovery.hook';
import {clearErrorsHookRunBlock} from './state-hooks/onSuccess/clearErrors.hook';

export const AUTHENTICATION_JS_MODULE = angular
    .module('dvbAngular.authentication', ['ngCookies'])
    .run(mandantCheck)
    .run(authenticationHookRunBlock)
    .run(authorisationHookRunBlock)
    .run(dummyLoginHookRunBlock)
    .run(errorAfterLoginHookRunBlock)
    .run(erorGSRegistrationIncompleteHookRunBlock)
    .run(errorLoggerHookRunBlock)
    .run(errorRecoveryHookRunBlock)
    .run(clearErrorsHookRunBlock)
    .run(authenticationRoutes)
    .service('HttpAuthInterceptor', HttpAuthInterceptor)
    .service('AuthServiceRS', AuthServiceRS)
    .component('dvSchulung', SCHULUNG_CONFIG)
    .component('dvLogin', LoginConfig);

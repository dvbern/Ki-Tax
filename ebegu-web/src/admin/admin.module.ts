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

import {CORE_JS_MODULE} from '../app/core/core.angularjs.module';
import {adminRun} from './admin.route';
import {TestFaelleRS} from './service/testFaelleRS.rest';

export const ADMIN_JS_MODULE = angular.module('ebeguWeb.admin', [CORE_JS_MODULE.name, 'smart-table'])
    .service('TestFaelleRS', TestFaelleRS)
    .run(adminRun);

export default ADMIN_JS_MODULE;

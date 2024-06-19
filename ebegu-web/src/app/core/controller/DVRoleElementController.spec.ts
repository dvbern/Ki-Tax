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

import {AUTHENTICATION_JS_MODULE} from '../../../authentication/authentication.module';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSRole} from '../../../models/enums/TSRole';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {DVRoleElementController} from './DVRoleElementController';

/* eslint-disable */
describe('DVElementController', () => {
    let authServiceRS: AuthServiceRS;
    let cvElementController: DVRoleElementController;

    beforeEach(angular.mock.module(AUTHENTICATION_JS_MODULE.name));
    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(
        angular.mock.inject($injector => {
            authServiceRS = $injector.get('AuthServiceRS');
            spyOn(authServiceRS, 'getPrincipalRole').and.returnValue(
                TSRole.GESUCHSTELLER
            );
            cvElementController = new DVRoleElementController(authServiceRS);
        })
    );

    describe('checkRoles', () => {
        it('should return true for the same role as the user and no expression', () => {
            cvElementController.dvAllowedRoles = [TSRole.GESUCHSTELLER];
            cvElementController.dvExpression = undefined;
            expect(cvElementController.checkValidity()).toBe(true);
        });
        it('should return true for the same role as the user and true expression', () => {
            cvElementController.dvAllowedRoles = [TSRole.GESUCHSTELLER];
            cvElementController.dvExpression = true;
            expect(cvElementController.checkValidity()).toBe(true);
        });
        it('should return false for the same role as the user and false expression', () => {
            cvElementController.dvAllowedRoles = [TSRole.GESUCHSTELLER];
            cvElementController.dvExpression = false;
            expect(cvElementController.checkValidity()).toBe(false);
        });
        it('should return false for a different role as the user and no expression', () => {
            cvElementController.dvAllowedRoles = [
                TSRole.SACHBEARBEITER_INSTITUTION,
                TSRole.ADMIN_BG
            ];
            cvElementController.dvExpression = undefined;
            expect(cvElementController.checkValidity()).toBe(false);
        });
        it('should return false for a different role as the user and true expression', () => {
            cvElementController.dvAllowedRoles = [
                TSRole.SACHBEARBEITER_INSTITUTION,
                TSRole.ADMIN_BG
            ];
            cvElementController.dvExpression = undefined;
            expect(cvElementController.checkValidity()).toBe(false);
        });
        it('should return false for a different role as the user and false expression', () => {
            cvElementController.dvAllowedRoles = [
                TSRole.SACHBEARBEITER_INSTITUTION,
                TSRole.ADMIN_BG
            ];
            cvElementController.dvExpression = undefined;
            expect(cvElementController.checkValidity()).toBe(false);
        });
    });
});

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

import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import TSUser from '../../../models/TSUser';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {EbeguWebCore} from '../core.angularjs.module';
import UserRS from './userRS.rest';

describe('userRS', () => {

    let userRS: UserRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockUser: TSUser;
    let mockUserRest: any;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        userRS = $injector.get('UserRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        mockUser = new TSUser('Pedro', 'Jimenez');
        mockUserRest = ebeguRestUtil.userToRestObject({}, mockUser);
    });

    describe('Public API', () => {
        it('check Service name', () => {
            expect(userRS.getServiceName()).toBe('UserRS');
        });
    });

    function checkFieldValues(user1: TSUser, user2: TSUser) {
        expect(user1).toBeDefined();
        expect(user1.nachname).toEqual(user2.nachname);
        expect(user1.vorname).toEqual(user2.vorname);
    }
});

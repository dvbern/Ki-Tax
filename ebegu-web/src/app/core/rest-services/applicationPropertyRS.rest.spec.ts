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

import {ADMIN_JS_MODULE} from '../../../admin/admin.module';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSApplicationProperty} from '../../../models/TSApplicationProperty';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ApplicationPropertyRS} from './applicationPropertyRS.rest';
import IHttpBackendService = angular.IHttpBackendService;

describe('ApplicationPropertyRS', () => {

    let applicationPropertyRS: ApplicationPropertyRS;
    let $httpBackend: IHttpBackendService;
    // tslint:disable-next-line:naming-convention
    let REST_API: string;
    const testName = 'myTestName';

    const mockApplicationProp = new TSApplicationProperty(testName, 'myTestValue');

    const mockApplicationPropertyRest = {
        name: testName,
        value: 'myTestValue',
    };

    beforeEach(angular.mock.module(ADMIN_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        applicationPropertyRS = $injector.get('ApplicationPropertyRS');
        $httpBackend = $injector.get('$httpBackend');
        REST_API = $injector.get('REST_API');
    }));

    // set the mock response
    beforeEach(() => {
        $httpBackend.when('GET', `${REST_API}application-properties/key/${testName}`).respond(
            mockApplicationPropertyRest);
        $httpBackend.when('GET', `${REST_API}application-properties/`).respond([mockApplicationPropertyRest]);
        const httpOk = 200;
        $httpBackend.when('DELETE', `${REST_API}application-properties/${testName}`).respond(httpOk, '');
        const httpCreated = 201;
        $httpBackend.when('POST', `${REST_API}application-properties/${testName}`)
            .respond(httpCreated,
                mockApplicationPropertyRest,
                {Location: 'http://localhost:8080/ebegu/api/v1/application-properties/key/test2'});

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

    });

    describe('API Usage', () => {
        describe('create', () => {

            it('should create property with name and value', () => {
                $httpBackend.expectPOST(`${REST_API}application-properties/${testName}`,
                    mockApplicationPropertyRest.value);
                const promise = applicationPropertyRS.create(mockApplicationPropertyRest.name,
                    mockApplicationPropertyRest.value);
                let property: TSApplicationProperty;

                promise.then(response => {
                    property = response.data;
                });
                $httpBackend.flush();
                expect(property.name).toEqual(mockApplicationProp.name);
                expect(property.value).toEqual(mockApplicationProp.value);

            });
        });

        describe('getAllApplicationProperties', () => {

            it('should fetch a list of all properties', () => {
                $httpBackend.expectGET(`${REST_API}application-properties/`);
                const promise = applicationPropertyRS.getAllApplicationProperties();
                let list: TSApplicationProperty[];

                promise.then(data => {
                    list = data;
                });
                $httpBackend.flush();

                for (let i = 0; i < list.length; i++) {
                    const mockArray = [mockApplicationPropertyRest];
                    expect(list[i].name).toEqual(mockArray[i].name);
                    expect(list[i].value).toEqual(mockArray[i].value);
                }
            });
        });

        describe('remove', () => {

            it('should remove a property', () => {
                $httpBackend.expectDELETE(`${REST_API}application-properties/${testName}`);
                const promise = applicationPropertyRS.remove(testName);
                let status: number;

                promise.then(response => {
                    status = response.status;

                });
                $httpBackend.flush();
                const httpOk = 200;
                expect(httpOk).toEqual(status);

            });
        });

    });

    afterEach(() => {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });
});

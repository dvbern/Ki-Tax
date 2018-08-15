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

import WizardStepManager from '../../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import TSKind from '../../../models/TSKind';
import TSKindContainer from '../../../models/TSKindContainer';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {EbeguWebCore} from '../core.angularjs.module';
import KindRS from './kindRS.rest';

describe('KindRS', () => {

    let kindRS: KindRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockKind: TSKindContainer;
    let mockKindRest: any;
    let gesuchId: string;
    let $q: angular.IQService;
    let wizardStepManager: WizardStepManager;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        kindRS = $injector.get('KindRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        wizardStepManager = $injector.get('WizardStepManager');
        $q = $injector.get('$q');
        spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when({}));
    }));

    beforeEach(() => {
        gesuchId = '2afc9d9a-957e-4550-9a22-97624a000feb';
        const kindGS: TSKind = new TSKind('Pedro', 'Bern');
        TestDataUtil.setAbstractFieldsUndefined(kindGS);
        const kindJA: TSKind = new TSKind('Johan', 'Basel');
        TestDataUtil.setAbstractFieldsUndefined(kindJA);
        mockKind = new TSKindContainer(kindGS, kindJA, []);
        TestDataUtil.setAbstractFieldsUndefined(mockKind);
        mockKind.id = '2afc9d9a-957e-4550-9a22-97624a1d8feb';
        mockKindRest = ebeguRestUtil.kindContainerToRestObject({}, mockKind);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(kindRS.serviceURL).toContain('kinder');
        });
        it('check Service name', () => {
            expect(kindRS.getServiceName()).toBe('KindRS');
        });
        it('should include a findKind() function', () => {
            expect(kindRS.findKind).toBeDefined();
        });
        it('should include a saveKind() function', () => {
            expect(kindRS.saveKind).toBeDefined();
        });
        it('should include a removeKind() function', () => {
            expect(kindRS.removeKind).toBeDefined();
        });
    });
    describe('API Usage', () => {
        describe('findKind', () => {
            it('should return the Kind by id', () => {
                $httpBackend.expectGET(kindRS.serviceURL + '/find/' + mockKind.id).respond(mockKindRest);

                let foundKind: TSKindContainer;
                kindRS.findKind(mockKind.id).then((result) => {
                    foundKind = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundKind);
            });
        });
        describe('createKind', () => {
            it('should create a Kind', () => {
                let createdKind: TSKindContainer;
                $httpBackend.expectPUT(kindRS.serviceURL + '/' + gesuchId, mockKindRest).respond(mockKindRest);

                kindRS.saveKind(mockKind, gesuchId)
                    .then((result) => {
                        createdKind = result;
                    });
                $httpBackend.flush();
                checkFieldValues(createdKind);
            });
        });
        describe('updateKind', () => {
            it('should update a Kind', () => {
                const kindJA2: TSKind = new TSKind('Johan', 'Basel');
                TestDataUtil.setAbstractFieldsUndefined(kindJA2);
                mockKind.kindJA = kindJA2;
                mockKindRest = ebeguRestUtil.kindContainerToRestObject({}, mockKind);
                let updatedKindContainer: TSKindContainer;
                $httpBackend.expectPUT(kindRS.serviceURL + '/' + gesuchId, mockKindRest).respond(mockKindRest);

                kindRS.saveKind(mockKind, gesuchId)
                    .then((result) => {
                        updatedKindContainer = result;
                    });
                $httpBackend.flush();
                expect(wizardStepManager.findStepsFromGesuch).toHaveBeenCalledWith(gesuchId);
                checkFieldValues(updatedKindContainer);
            });
        });
        describe('removeKind', () => {
            it('should remove a Kind', () => {
                $httpBackend.expectDELETE(kindRS.serviceURL + '/' + encodeURIComponent(mockKind.id))
                    .respond(200);

                let deleteResult: any;
                kindRS.removeKind(mockKind.id, gesuchId)
                    .then((result) => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(wizardStepManager.findStepsFromGesuch).toHaveBeenCalledWith(gesuchId);
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(200);
            });
        });
    });

    function checkFieldValues(foundKind: TSKindContainer) {
        expect(foundKind).toBeDefined();
        expect(foundKind).toEqual(mockKind);
        expect(foundKind.kindGS).toBeDefined();
        expect(foundKind.kindGS).toEqual(mockKind.kindGS);
        expect(foundKind.kindJA).toBeDefined();
        expect(foundKind.kindJA).toEqual(mockKind.kindJA);
    }
});

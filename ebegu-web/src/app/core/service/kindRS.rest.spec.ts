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

import {IHttpBackendService, IQService} from 'angular';
import {WizardStepManager} from '../../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {KindRS} from './kindRS.rest';

describe('KindRS', () => {
    let kindRS: KindRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockKind: TSKindContainer;
    let mockKindRest: any;
    let gesuchId: string;
    let $q: IQService;
    let wizardStepManager: WizardStepManager;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            kindRS = $injector.get('KindRS');
            $httpBackend = $injector.get('$httpBackend');
            ebeguRestUtil = $injector.get('EbeguRestUtil');
            wizardStepManager = $injector.get('WizardStepManager');
            $q = $injector.get('$q');
            spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(
                $q.resolve()
            );
        })
    );

    beforeEach(() => {
        gesuchId = '2afc9d9a-957e-4550-9a22-97624a000feb';
        const kindGS = new TSKind();
        kindGS.vorname = 'Pedro';
        kindGS.nachname = 'Bern';
        TestDataUtil.setAbstractMutableFieldsUndefined(kindGS);
        const kindJA = new TSKind();
        kindJA.vorname = 'Johan';
        kindJA.nachname = 'Basel';
        TestDataUtil.setAbstractMutableFieldsUndefined(kindJA);
        mockKind = new TSKindContainer();
        mockKind.kindGS = kindGS;
        mockKind.kindJA = kindJA;
        mockKind.betreuungen = [];
        TestDataUtil.setAbstractMutableFieldsUndefined(mockKind);
        mockKind.id = '2afc9d9a-957e-4550-9a22-97624a1d8feb';
        mockKindRest = ebeguRestUtil.kindContainerToRestObject({}, mockKind);
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(kindRS.serviceURL).toContain('kinder');
        });
    });
    describe('API Usage', () => {
        describe('findKind', () => {
            it('should return the Kind by id', () => {
                $httpBackend
                    .expectGET(`${kindRS.serviceURL}/find/${mockKind.id}`)
                    .respond(mockKindRest);

                let foundKind: TSKindContainer;
                kindRS.findKind(mockKind.id).then(result => {
                    foundKind = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundKind);
            });
        });
        describe('createKind', () => {
            it('should create a Kind', () => {
                let createdKind: TSKindContainer;
                $httpBackend
                    .expectPUT(`${kindRS.serviceURL}/${gesuchId}`, mockKindRest)
                    .respond(mockKindRest);

                kindRS.saveKind(mockKind, gesuchId).then(result => {
                    createdKind = result;
                });
                $httpBackend.flush();
                checkFieldValues(createdKind);
            });
        });
        describe('updateKind', () => {
            it('should update a Kind', () => {
                const kindJA2 = new TSKind();
                kindJA2.vorname = 'Johan';
                kindJA2.nachname = 'Basel';
                TestDataUtil.setAbstractMutableFieldsUndefined(kindJA2);
                mockKind.kindJA = kindJA2;
                mockKindRest = ebeguRestUtil.kindContainerToRestObject(
                    {},
                    mockKind
                );
                let updatedKindContainer: TSKindContainer;
                $httpBackend
                    .expectPUT(`${kindRS.serviceURL}/${gesuchId}`, mockKindRest)
                    .respond(mockKindRest);

                kindRS.saveKind(mockKind, gesuchId).then(result => {
                    updatedKindContainer = result;
                });
                $httpBackend.flush();
                // eslint-disable-next-line @typescript-eslint/unbound-method
                expect(
                    wizardStepManager.findStepsFromGesuch
                ).toHaveBeenCalledWith(gesuchId);
                checkFieldValues(updatedKindContainer);
            });
        });
        describe('removeKind', () => {
            it('should remove a Kind', () => {
                const httpOk = 200;
                $httpBackend
                    .expectDELETE(
                        `${kindRS.serviceURL}/${encodeURIComponent(mockKind.id)}`
                    )
                    .respond(httpOk);

                let deleteResult: any;
                kindRS.removeKind(mockKind.id, gesuchId).then(result => {
                    deleteResult = result;
                });
                $httpBackend.flush();
                // eslint-disable-next-line @typescript-eslint/unbound-method
                expect(
                    wizardStepManager.findStepsFromGesuch
                ).toHaveBeenCalledWith(gesuchId);
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(httpOk);
            });
        });
    });

    function checkFieldValues(foundKind: TSKindContainer): void {
        expect(foundKind).toBeDefined();
        expect(foundKind.kindGS).toBeDefined();
        TestDataUtil.compareDefinedProperties(
            foundKind.kindGS,
            mockKind.kindGS
        );
        expect(foundKind.kindJA).toBeDefined();
        TestDataUtil.compareDefinedProperties(
            foundKind.kindJA,
            mockKind.kindJA
        );
    }
});

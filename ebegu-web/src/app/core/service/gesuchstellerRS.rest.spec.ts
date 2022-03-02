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
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {GesuchstellerRS} from './gesuchstellerRS.rest';
import IInjectorService = angular.auto.IInjectorService;

describe('GesuchstellerRS', () => {

    let gesuchstellerRS: GesuchstellerRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockGesuchsteller: TSGesuchstellerContainer;
    let mockGesuchstellerRest: any;
    const dummyGesuchID = '123';
    let $q: IQService;
    let wizardStepManager: WizardStepManager;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject(($injector: IInjectorService) => {
        gesuchstellerRS = $injector.get('GesuchstellerRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        wizardStepManager = $injector.get('WizardStepManager');
        $q = $injector.get('$q');
        spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.resolve());
    }));

    beforeEach(() => {
        mockGesuchsteller = new TSGesuchstellerContainer();
        mockGesuchsteller.gesuchstellerJA = new TSGesuchsteller();
        mockGesuchsteller.gesuchstellerJA.id = '2afc9d9a-957e-4550-9a22-97624a1d8fe1';
        mockGesuchsteller.gesuchstellerJA.vorname = 'Tim';
        mockGesuchsteller.gesuchstellerJA.nachname = 'Tester';
        mockGesuchsteller.id = '2afc9d9a-957e-4550-9a22-97624a1d8feb';
        mockGesuchstellerRest = ebeguRestUtil.gesuchstellerContainerToRestObject({}, mockGesuchsteller);

        const url = `${gesuchstellerRS.serviceURL}/${encodeURIComponent(mockGesuchsteller.id)}`;
        $httpBackend.whenGET(url).respond(mockGesuchstellerRest);

        TestDataUtil.mockLazyGesuchModelManagerHttpCalls($httpBackend);
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('API Usage', () => {
        describe('updateGesuchsteller', () => {
            it('should updateGesuchsteller a gesuchsteller and her adresses', () => {
                mockGesuchsteller.gesuchstellerJA.nachname = 'changedname';
                let updatedGesuchsteller: TSGesuchstellerContainer;
                $httpBackend.expectPUT(`${gesuchstellerRS.serviceURL}/${dummyGesuchID}/gsNumber/1/false`,
                    ebeguRestUtil.gesuchstellerContainerToRestObject({}, mockGesuchsteller))
                    .respond(ebeguRestUtil.gesuchstellerContainerToRestObject({}, mockGesuchsteller));

                gesuchstellerRS.saveGesuchsteller(mockGesuchsteller, dummyGesuchID, 1, false).then(result => {
                    updatedGesuchsteller = result;
                });
                $httpBackend.flush();
                // tslint:disable-next-line:no-unbound-method
                expect(wizardStepManager.findStepsFromGesuch).toHaveBeenCalledWith(dummyGesuchID);
                expect(updatedGesuchsteller).toBeDefined();
                expect(updatedGesuchsteller.gesuchstellerJA).toBeDefined();
                expect(updatedGesuchsteller.gesuchstellerJA.nachname)
                    .toEqual(mockGesuchsteller.gesuchstellerJA.nachname);
                expect(updatedGesuchsteller.id).toEqual(mockGesuchsteller.id);
            });
        });

        describe('findGesuchsteller', () => {
            it('should return the gesuchsteller by id', () => {
                    let foundGesuchsteller: TSGesuchstellerContainer;
                    const url = `${gesuchstellerRS.serviceURL}/id/${mockGesuchsteller.id}`;
                    $httpBackend.expectGET(url).respond(mockGesuchsteller);

                    gesuchstellerRS.findGesuchsteller(mockGesuchsteller.id).then(result => {
                        foundGesuchsteller = result;
                    });
                    $httpBackend.flush();
                    expect(foundGesuchsteller).toBeDefined();
                    expect(foundGesuchsteller.gesuchstellerJA.nachname)
                        .toEqual(mockGesuchsteller.gesuchstellerJA.nachname);
                },
            );
        });
    });

});

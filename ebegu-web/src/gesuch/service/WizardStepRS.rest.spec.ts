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

import {EbeguWebCore} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import TSWizardStep from '../../models/TSWizardStep';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import TestDataUtil from '../../utils/TestDataUtil.spec';
import WizardStepRS from './WizardStepRS.rest';

describe('WizardStepRS', () => {

    let wizardStepRS: WizardStepRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let REST_API: string;
    let mockWizardStep: TSWizardStep;
    let mockWizardStepRest: any;
    let mockWizardStepListRest: Array<any> = [];
    const gesuchId: string = '123123123123';

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        wizardStepRS = $injector.get('WizardStepRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        REST_API = $injector.get('REST_API');
    }));

    beforeEach(() => {
        mockWizardStep = TestDataUtil.createWizardStep(gesuchId);
        TestDataUtil.setAbstractFieldsUndefined(mockWizardStep);
        mockWizardStepRest = ebeguRestUtil.wizardStepToRestObject({}, mockWizardStep);
        mockWizardStepListRest = [mockWizardStepRest];
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(wizardStepRS.serviceURL).toContain('wizard-steps');
        });
        it('check Service name', () => {
            expect(wizardStepRS.getServiceName()).toBe('WizardStepRS');
        });
        it('should include a updateWizardStep() function', () => {
            expect(wizardStepRS.updateWizardStep).toBeDefined();
        });
        it('should include a findWizardStepsFromGesuch() function', () => {
            expect(wizardStepRS.findWizardStepsFromGesuch).toBeDefined();
        });
    });

    describe('API Usage', () => {
        describe('findWizardStepsFromGesuch', () => {
            it('should return the all wizardSteps of a Gesuch', () => {
                $httpBackend.expectGET(wizardStepRS.serviceURL + '/' + gesuchId).respond(mockWizardStepListRest);

                let foundSteps: Array<TSWizardStep>;
                wizardStepRS.findWizardStepsFromGesuch(gesuchId).then((result) => {
                    foundSteps = result;
                });
                $httpBackend.flush();
                expect(foundSteps).toBeDefined();
                expect(foundSteps.length).toEqual(1);
                expect(foundSteps[0]).toEqual(mockWizardStep);
            });
        });
    });
});

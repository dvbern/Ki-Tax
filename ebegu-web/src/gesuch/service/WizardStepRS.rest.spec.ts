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

import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../hybridTools/translationsMock';
import {TSWizardStep} from '../../models/TSWizardStep';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {WizardStepRS} from './WizardStepRS.rest';
import IHttpBackendService = angular.IHttpBackendService;

describe('WizardStepRS', () => {

    let wizardStepRS: WizardStepRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockWizardStep: TSWizardStep;
    let mockWizardStepRest: any;
    let mockWizardStepListRest: Array<any> = [];
    const gesuchId = '123123123123';

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject($injector => {
        wizardStepRS = $injector.get('WizardStepRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        mockWizardStep = TestDataUtil.createWizardStep(gesuchId);
        TestDataUtil.setAbstractMutableFieldsUndefined(mockWizardStep);
        mockWizardStepRest = ebeguRestUtil.wizardStepToRestObject({}, mockWizardStep);
        mockWizardStepListRest = [mockWizardStepRest];

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(wizardStepRS.serviceURL).toContain('wizard-steps');
        });
    });

    describe('API Usage', () => {
        describe('findWizardStepsFromGesuch', () => {
            it('should return the all wizardSteps of a Gesuch', () => {
                $httpBackend.expectGET(`${wizardStepRS.serviceURL}/${gesuchId}`).respond(mockWizardStepListRest);

                let foundSteps: Array<TSWizardStep>;
                wizardStepRS.findWizardStepsFromGesuch(gesuchId).then(result => {
                    foundSteps = result;
                });
                $httpBackend.flush();
                expect(foundSteps).toBeDefined();
                expect(foundSteps.length).toEqual(1);
                TestDataUtil.compareDefinedProperties(foundSteps[0], mockWizardStep);
            });
        });
    });
});

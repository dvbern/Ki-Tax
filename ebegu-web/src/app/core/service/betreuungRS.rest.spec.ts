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
import {TSBetreuungsstatus} from '../../../models/enums/betreuung/TSBetreuungsstatus';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {BetreuungRS} from './betreuungRS.rest';

describe('betreuungRS', () => {
    let betreuungRS: BetreuungRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockBetreuung: TSBetreuung;
    let wizardStepManager: WizardStepManager;
    let mockBetreuungRest: any;
    let gesuchId: string;
    let $q: IQService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            betreuungRS = $injector.get('BetreuungRS');
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
        gesuchId = '2afc9d9a-957e-4550-9a22-97624a000a12';
        mockBetreuung = new TSBetreuung();
        mockBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
        mockBetreuung.betreuungspensumContainers = [];
        mockBetreuung.betreuungspensumAbweichungen = null;
        TestDataUtil.setAbstractMutableFieldsUndefined(mockBetreuung);
        mockBetreuungRest = ebeguRestUtil.betreuungToRestObject(
            {},
            mockBetreuung
        );

        $httpBackend
            .whenGET(
                `${betreuungRS.serviceURL}/${encodeURIComponent(mockBetreuung.id)}`
            )
            .respond(mockBetreuungRest);

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(betreuungRS.serviceURL).toContain('betreuungen');
        });
    });

    describe('API Usage', () => {
        describe('findBetreuung', () => {
            it('should return the Betreuung by id', () => {
                $httpBackend
                    .expectGET(`${betreuungRS.serviceURL}/${mockBetreuung.id}`)
                    .respond(mockBetreuungRest);

                let foundBetreuung: TSBetreuung;
                betreuungRS.findBetreuung(mockBetreuung.id).then(result => {
                    foundBetreuung = result;
                });
                $httpBackend.flush();
                expect(foundBetreuung).toBeDefined();
                TestDataUtil.compareDefinedProperties(
                    foundBetreuung,
                    mockBetreuung
                );
            });
        });
        describe('createBetreuung', () => {
            it('should create a Betreuung', () => {
                let createdBetreuung: TSBetreuung;
                $httpBackend
                    .expectPUT(
                        `${betreuungRS.serviceURL}/betreuung/false`,
                        mockBetreuungRest
                    )
                    .respond(mockBetreuungRest);

                betreuungRS
                    .saveBetreuung(mockBetreuung, gesuchId, false)
                    .then(result => {
                        createdBetreuung = result;
                    });
                $httpBackend.flush();
                // eslint-disable-next-line @typescript-eslint/unbound-method
                expect(
                    wizardStepManager.findStepsFromGesuch
                ).toHaveBeenCalledWith(gesuchId);
                expect(createdBetreuung).toBeDefined();
                TestDataUtil.compareDefinedProperties(
                    createdBetreuung,
                    mockBetreuung
                );
            });
        });
        describe('removeBetreuung', () => {
            it('should remove a Betreuung', () => {
                const status = 200;
                $httpBackend
                    .expectDELETE(
                        `${betreuungRS.serviceURL}/${encodeURIComponent(mockBetreuung.id)}`
                    )
                    .respond(status);

                let deleteResult: any;
                betreuungRS
                    .removeBetreuung(mockBetreuung.id, gesuchId)
                    .then(result => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                // eslint-disable-next-line @typescript-eslint/unbound-method
                expect(
                    wizardStepManager.findStepsFromGesuch
                ).toHaveBeenCalledWith(gesuchId);
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(status);
            });
        });
    });
});

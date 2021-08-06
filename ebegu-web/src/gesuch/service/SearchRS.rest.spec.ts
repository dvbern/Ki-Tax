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

import {IHttpBackendService} from 'angular';
import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../models/TSAntragDTO';
import {TSAntragSearchresultDTO} from '../../models/TSAntragSearchresultDTO';
import {EbeguRestUtil} from '../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {SearchRS} from './searchRS.rest';

// tslint:disable:no-magic-numbers
describe('searchRS', () => {

    let searchRS: SearchRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockPendenz: TSAntragDTO;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        searchRS = $injector.get('SearchRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        mockPendenz = new TSAntragDTO();
        mockPendenz.antragId = 'id1';
        mockPendenz.fallNummer = 123;
        mockPendenz.familienName = 'name';
        mockPendenz.antragTyp = TSAntragTyp.ERSTGESUCH;
        mockPendenz.angebote = [TSBetreuungsangebotTyp.KITA];
        mockPendenz.institutionen = ['Inst1, Inst2'];
        mockPendenz.verantwortlicherBG = 'Juan Arbolado';
        mockPendenz.verantwortlicherTS = 'Juan Arbolado';
        ebeguRestUtil.antragDTOToRestObject({}, mockPendenz);

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('API Usage', () => {
        describe('getPendenzenList', () => {
            it('should return all pending Antraege', () => {
                const tsAntragDTO = new TSAntragDTO();
                tsAntragDTO.fallNummer = 1234;
                const searchResult: any = {
                    antragDTOs: [tsAntragDTO],
                    paginationDTO: {totalItemCount: 1},
                };

                const filter: any = {};
                $httpBackend.expectPOST(searchRS.serviceURL + '/jugendamt/', filter).respond(searchResult);

                let foundPendenzen: TSAntragSearchresultDTO;
                searchRS.getPendenzenList(filter).then(result => {
                    foundPendenzen = result;
                });
                $httpBackend.flush();
                expect(foundPendenzen).toBeDefined();
                TestDataUtil.compareDefinedProperties(foundPendenzen.antragDTOs[0], tsAntragDTO);
            });
        });
    });

});

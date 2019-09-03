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
import * as moment from 'moment';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSAdresse from '../../../models/TSAdresse';
import TSInstitution from '../../../models/TSInstitution';
import TSInstitutionStammdaten from '../../../models/TSInstitutionStammdaten';
import TSInstitutionStammdatenBetreuungsgutscheine from '../../../models/TSInstitutionStammdatenBetreuungsgutscheine';
import {TSDateRange} from '../../../models/types/TSDateRange';
import DateUtil from '../../../utils/DateUtil';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {InstitutionStammdatenRS} from './institutionStammdatenRS.rest';

describe('institutionStammdatenRS', () => {

    let institutionStammdatenRS: InstitutionStammdatenRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockInstitutionStammdaten: TSInstitutionStammdaten;
    let mockInstitutionStammdatenRest: any;
    let mockInstitution: TSInstitution;
    let mockAdresse: TSAdresse;
    let today: moment.Moment;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        institutionStammdatenRS = $injector.get('InstitutionStammdatenRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
    }));

    beforeEach(() => {
        today = DateUtil.today();
        mockInstitution = new TSInstitution('Institution_Test');
        mockAdresse = new TSAdresse();
        mockInstitutionStammdaten = new TSInstitutionStammdaten();
        mockInstitutionStammdaten.institutionStammdatenBetreuungsgutscheine = new TSInstitutionStammdatenBetreuungsgutscheine();
        mockInstitutionStammdaten.institutionStammdatenBetreuungsgutscheine.iban = 'iban';
        mockInstitutionStammdaten.betreuungsangebotTyp = TSBetreuungsangebotTyp.KITA;
        mockInstitutionStammdaten.institution = mockInstitution;
        mockInstitutionStammdaten.adresse = mockAdresse;
        mockInstitutionStammdaten.mail = 'mail@example.com';
        mockInstitutionStammdaten.telefon = 'telefon';
        mockInstitutionStammdaten.gueltigkeit = new TSDateRange(today, today);
        mockInstitutionStammdaten.id = '2afc9d9a-957e-4550-9a22-97624a1d8f05';
        mockInstitutionStammdatenRest = ebeguRestUtil.institutionStammdatenToRestObject({}, mockInstitutionStammdaten);
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
    });

    describe('API Usage', () => {
        describe('findInstitutionStammdaten', () => {
            it('should return the InstitutionStammdaten by id', () => {
                const url = `${institutionStammdatenRS.serviceURL}/id/${encodeURIComponent(mockInstitutionStammdaten.id)}`;
                $httpBackend.expectGET(url)
                    .respond(mockInstitutionStammdatenRest);

                let foundInstitutionStammdaten: TSInstitutionStammdaten;
                institutionStammdatenRS.findInstitutionStammdaten(mockInstitutionStammdaten.id).then(result => {
                    foundInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundInstitutionStammdaten, mockInstitutionStammdaten);
            });

        });

        describe('getAllInstitutionStammdaten', () => {
            it('should return all InstitutionStammdaten', () => {
                const institutionStammdatenRestArray = [mockInstitutionStammdatenRest, mockInstitutionStammdatenRest];
                $httpBackend.expectGET(institutionStammdatenRS.serviceURL).respond(institutionStammdatenRestArray);

                let returnedInstitutionStammdaten: Array<TSInstitutionStammdaten>;
                institutionStammdatenRS.getAllInstitutionStammdaten().then(result => {
                    returnedInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                expect(returnedInstitutionStammdaten).toBeDefined();
                expect(returnedInstitutionStammdaten.length).toEqual(2);
                checkFieldValues(returnedInstitutionStammdaten[0], institutionStammdatenRestArray[0]);
                checkFieldValues(returnedInstitutionStammdaten[1], institutionStammdatenRestArray[1]);
            });
        });

        describe('getAllInstitutionStammdatenByDate', () => {
            it('should return all InstitutionStammdaten im gegebenen Datum', () => {
                const institutionStammdatenRestArray = [mockInstitutionStammdatenRest, mockInstitutionStammdatenRest];
                $httpBackend.expectGET(`${institutionStammdatenRS.serviceURL}/date?date=${DateUtil.momentToLocalDate(
                    today)}`)
                    .respond(institutionStammdatenRestArray);

                let returnedInstitutionStammdaten: Array<TSInstitutionStammdaten>;
                institutionStammdatenRS.getAllInstitutionStammdatenByDate(today).then(result => {
                    returnedInstitutionStammdaten = result;
                });
                $httpBackend.flush();
                expect(returnedInstitutionStammdaten).toBeDefined();
                expect(returnedInstitutionStammdaten.length).toEqual(2);
                checkFieldValues(returnedInstitutionStammdaten[0], institutionStammdatenRestArray[0]);
                checkFieldValues(returnedInstitutionStammdaten[1], institutionStammdatenRestArray[1]);
            });
        });
    });

    function checkFieldValues(
        institutionStammdaten1: TSInstitutionStammdaten,
        institutionStammdaten2: TSInstitutionStammdaten,
    ): void {
        expect(institutionStammdaten1).toBeDefined();
        expect(institutionStammdaten1.institutionStammdatenBetreuungsgutscheine.iban)
            .toEqual(institutionStammdaten2.institutionStammdatenBetreuungsgutscheine.iban);
        expect(institutionStammdaten1.id).toEqual(institutionStammdaten2.id);
        expect(institutionStammdaten1.institution.name).toEqual(institutionStammdaten2.institution.name);
    }

});

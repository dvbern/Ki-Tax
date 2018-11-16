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

import {IHttpBackendService, IHttpService} from 'angular';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSFachstelle} from '../../../models/TSFachstelle';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import {FachstelleRS} from './fachstelleRS.rest';

describe('fachstelleRS', () => {

    let fachstelleRS: FachstelleRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let mockFachstelle: TSFachstelle;
    let mockFachstelleRest: any;
    let $http: IHttpService;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        fachstelleRS = $injector.get('FachstelleRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        $http = $injector.get('$http');
    }));

    beforeEach(() => {
        mockFachstelle = new TSFachstelle('Fachstelle_Test', 'Ein Test', true);
        mockFachstelle.id = '2afc9d9a-957e-4550-9a22-9762422d8f05';
        mockFachstelleRest = ebeguRestUtil.fachstelleToRestObject({}, mockFachstelle);
    });

    describe('Public API', () => {
        it('check URI', () => {
            expect(fachstelleRS.serviceURL).toContain('fachstellen');
        });
    });

    describe('API Usage', () => {
        describe('findFachstelle', () => {
            it('should return the Fachstelle by id', () => {
                const url = `${fachstelleRS.serviceURL}/${mockFachstelle.id}`;
                $httpBackend.expectGET(url).respond(mockFachstelleRest);

                let foundFachstelle: TSFachstelle;
                fachstelleRS.findFachstelle(mockFachstelle.id).then(result => {
                    foundFachstelle = result;
                });
                $httpBackend.flush();
                checkFieldValues(foundFachstelle, mockFachstelle);
            });

        });

        describe('createFachstelle', () => {
            it('should create an fachstelle', () => {
                let savedFachstelle: TSFachstelle;
                $httpBackend.expectPUT(fachstelleRS.serviceURL, mockFachstelleRest).respond(mockFachstelleRest);
                fachstelleRS.createFachstelle(mockFachstelle).then(result => {
                    savedFachstelle = result;
                });
                $httpBackend.flush();
                checkFieldValues(savedFachstelle, mockFachstelle);
            });
        });

        describe('updateFachstelle', () => {
            it('should update an fachstelle', () => {
                mockFachstelle.name = 'changedname';
                mockFachstelleRest = ebeguRestUtil.fachstelleToRestObject({}, mockFachstelle);
                let updatedFachstelle: TSFachstelle;
                $httpBackend.expectPUT(fachstelleRS.serviceURL, mockFachstelleRest).respond(mockFachstelleRest);
                fachstelleRS.updateFachstelle(mockFachstelle).then(result => {
                    updatedFachstelle = result;
                });
                $httpBackend.flush();
                checkFieldValues(updatedFachstelle, mockFachstelle);
            });
        });

        describe('removeFachstelle', () => {
            it('should remove an fachstelle', () => {
                const httpOk = 200;
                $httpBackend.expectDELETE(`${fachstelleRS.serviceURL}/${encodeURIComponent(mockFachstelle.id)}`)
                    .respond(httpOk);

                let deleteResult: any;
                fachstelleRS.removeFachstelle(mockFachstelle.id)
                    .then(result => {
                        deleteResult = result;
                    });
                $httpBackend.flush();
                expect(deleteResult).toBeDefined();
                expect(deleteResult.status).toEqual(httpOk);
            });
        });

        describe('getAllFachstellen', () => {
            it('should return all Fachstellen', () => {
                const fachstellenRestArray = [mockFachstelleRest, mockFachstelleRest];
                $httpBackend.expectGET(fachstelleRS.serviceURL).respond(fachstellenRestArray);
                spyOn($http, 'get').and.callThrough();
                spyOn(ebeguRestUtil, 'parseFachstellen').and.callThrough();

                fachstelleRS.getAllFachstellen();
                $httpBackend.flush();
                // tslint:disable-next-line:no-unbound-method
                expect($http.get).toHaveBeenCalledWith(fachstelleRS.serviceURL);
                // tslint:disable-next-line:no-unbound-method
                expect(ebeguRestUtil.parseFachstellen).toHaveBeenCalledWith(fachstellenRestArray);
            });
        });

    });

    function checkFieldValues(fachstelle1: TSFachstelle, fachstelle2: TSFachstelle): void {
        expect(fachstelle1).toBeDefined();
        expect(fachstelle1.name).toEqual(fachstelle2.name);
        expect(fachstelle1.id).toEqual(fachstelle2.id);
        expect(fachstelle1.beschreibung).toEqual(fachstelle2.beschreibung);
    }

});

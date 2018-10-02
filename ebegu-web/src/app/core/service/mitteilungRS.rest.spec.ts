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
import TSBetreuung from '../../../models/TSBetreuung';
import TSBetreuungsmitteilung from '../../../models/TSBetreuungsmitteilung';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import EbeguRestUtil from '../../../utils/EbeguRestUtil';
import {CORE_JS_MODULE} from '../core.angularjs.module';
import MitteilungRS from './mitteilungRS.rest';

describe('MitteilungRS', () => {

    let mitteilungRS: MitteilungRS;
    let $httpBackend: angular.IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;
    let $q: angular.IQService;
    let wizardStepManager: WizardStepManager;
    let $rootScope: angular.IRootScopeService;
    let dossier: TSDossier;
    let betreuung: TSBetreuung;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        mitteilungRS = $injector.get('MitteilungRS');
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        wizardStepManager = $injector.get('WizardStepManager');
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');
        dossier = new TSDossier();
        dossier.fall = new TSFall();
        betreuung = new TSBetreuung();
        betreuung.betreuungNummer = 123;
    }));

    describe('Public API', () => {
        it('check URI', () => {
            expect(mitteilungRS.serviceURL).toContain('mitteilungen');
        });
        it('check Service name', () => {
            expect(mitteilungRS.getServiceName()).toBe('MitteilungRS');
        });
    });
    describe('sendbetreuungsmitteilung', () => {
        it('should create the betreuungsmitteilung and send it', () => {
            const restMitteilung: any = {};
            const bm: TSBetreuungsmitteilung = new TSBetreuungsmitteilung();
            bm.betreuung = betreuung;
            spyOn(ebeguRestUtil, 'betreuungsmitteilungToRestObject').and.returnValue(restMitteilung);
            spyOn(ebeguRestUtil, 'parseBetreuungsmitteilung').and.returnValue(bm);
            $httpBackend.expectPUT(mitteilungRS.serviceURL + '/sendbetreuungsmitteilung', restMitteilung).respond($q.when(restMitteilung));

            const result: angular.IPromise<TSBetreuungsmitteilung> = mitteilungRS.sendbetreuungsmitteilung(dossier, betreuung);
            $httpBackend.flush();
            $rootScope.$apply();

            expect(result).toBeDefined();
            result.then(response => {
                expect(response.betreuung).toBe(betreuung);
            });
            $rootScope.$apply();

        });
    });
    describe('applybetreuungsmitteilung', () => {
        it('should call the services to apply the betreuungsmitteilung', () => {
            const mitteilung: TSBetreuungsmitteilung = new TSBetreuungsmitteilung();
            mitteilung.id = '987654321';

            spyOn(ebeguRestUtil, 'parseBetreuungsmitteilung').and.returnValue(betreuung);
            $httpBackend.expectPUT(mitteilungRS.serviceURL + '/applybetreuungsmitteilung/' + mitteilung.id, null).respond($q.when({id: '123456'}));

            const result: angular.IPromise<any> = mitteilungRS.applyBetreuungsmitteilung(mitteilung.id);
            $httpBackend.flush();
            $rootScope.$apply();

            expect(result).toBeDefined();
            result.then(response => {
                expect(response).toEqual({id: '123456'});
            });
            $rootScope.$apply();

        });
    });
});

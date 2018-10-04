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

import * as angular from 'angular';
import {of} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../models/enums/TSRole';
import TSBenutzer from '../../../models/TSBenutzer';
import TSDossier from '../../../models/TSDossier';
import TSFall from '../../../models/TSFall';
import TSMitteilung from '../../../models/TSMitteilung';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import MitteilungRS from '../../core/service/mitteilungRS.rest';
import {POSTEINGANG_JS_MODULE} from '../posteingang.module';
import {PosteingangViewController} from './posteingangView';

describe('posteingangView', () => {

    let authServiceRS: AuthServiceRS;
    let mitteilungRS: MitteilungRS;
    let posteingangViewController: PosteingangViewController;
    let $q: angular.IQService;
    let $rootScope: angular.IRootScopeService;
    let $httpBackend: angular.IHttpBackendService;
    let mockMitteilung: TSMitteilung;
    let gemeindeRS: GemeindeRS;

    beforeEach(angular.mock.module(POSTEINGANG_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        mitteilungRS = $injector.get('MitteilungRS');
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');
        $httpBackend = $injector.get('$httpBackend');
        mockMitteilung = mockGetMitteilung();
        gemeindeRS = $injector.get('GemeindeRS');
    }));

    describe('API Usage', () => {
        describe('searchMitteilungen', () => {
            it('should return the list of Mitteilungen', () => {
                mockRestCalls();
                spyOn(gemeindeRS, 'getGemeindenForPrincipal$').and.returnValue(of([]));
                posteingangViewController = new PosteingangViewController(mitteilungRS,
                    undefined,
                    authServiceRS,
                    gemeindeRS);
                $rootScope.$apply();
                const tableFilterState: any = {};
                posteingangViewController.passFilterToServer(tableFilterState).then(() => {
                    // tslint:disable-next-line:no-unbound-method
                    expect(mitteilungRS.searchMitteilungen).toHaveBeenCalled();
                    const list = posteingangViewController.displayedCollection;
                    expect(list).toBeDefined();
                    expect(list.length).toBe(1);
                    expect(list[0]).toEqual(mockMitteilung);
                });
            });
        });
    });

    function mockGetMitteilung(): TSMitteilung {
        const mockFall = new TSFall();
        // tslint:disable-next-line:no-magic-numbers
        mockFall.fallNummer = 123;
        const mockDossier = new TSDossier();
        mockDossier.fall = mockFall;
        const gesuchsteller = new TSBenutzer();
        gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
        const result = new TSMitteilung(mockDossier,
            undefined,
            TSMitteilungTeilnehmerTyp.GESUCHSTELLER,
            TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller,
            undefined,
            'Frage',
            'Warum ist die Banane krumm?',
            TSMitteilungStatus.NEU,
            undefined);
        const dtoList = [result];
        spyOn(mitteilungRS, 'searchMitteilungen').and.returnValue($q.when(dtoList));

        return result;
    }

    function mockRestCalls(): void {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/benutzer').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
    }
});

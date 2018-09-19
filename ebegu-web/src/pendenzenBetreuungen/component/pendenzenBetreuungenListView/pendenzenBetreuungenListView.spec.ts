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
import {StateService} from '@uirouter/core';
import {IHttpBackendService, IQService, IScope} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../app/core/service/institutionRS.rest';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import BerechnungsManager from '../../../gesuch/service/berechnungsManager';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import TSPendenzBetreuung from '../../../models/TSPendenzBetreuung';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import {EbeguWebPendenzenBetreuungen} from '../../pendenzenBetreuungen.module';
import PendenzBetreuungenRS from '../../service/PendenzBetreuungenRS.rest';
import {PendenzenBetreuungenListViewController} from './pendenzenBetreuungenListView';

describe('pendenzenBetreuungenListView', () => {

    let institutionRS: InstitutionRS;
    let gesuchsperiodeRS: GesuchsperiodeRS;
    let institutionStammdatenRS: InstitutionStammdatenRS;
    let pendenzBetreuungenRS: PendenzBetreuungenRS;
    let pendenzBetreuungenListViewController: PendenzenBetreuungenListViewController;
    let $q: IQService;
    let $scope: IScope;
    let $httpBackend: IHttpBackendService;
    let gesuchModelManager: GesuchModelManager;
    let berechnungsManager: BerechnungsManager;
    let $state: StateService;
    let CONSTANTS: any;
    let gemeindeRS: GemeindeRS;
    let authServiceRS: AuthServiceRS;
    let authLifeCycleService: AuthLifeCycleService;

    beforeEach(angular.mock.module(EbeguWebPendenzenBetreuungen.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        pendenzBetreuungenRS = $injector.get('PendenzBetreuungenRS');
        institutionRS = $injector.get('InstitutionRS');
        institutionStammdatenRS = $injector.get('InstitutionStammdatenRS');
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $q = $injector.get('$q');
        $scope = $injector.get('$rootScope');
        $httpBackend = $injector.get('$httpBackend');
        gesuchModelManager = $injector.get('GesuchModelManager');
        berechnungsManager = $injector.get('BerechnungsManager');
        $state = $injector.get('$state');
        CONSTANTS = $injector.get('CONSTANTS');
        gemeindeRS = $injector.get('GemeindeRS');
        authServiceRS = $injector.get('AuthServiceRS');
        authLifeCycleService = $injector.get('AuthLifeCycleService');
    }));

    describe('API Usage', () => {
        describe('initFinSit Pendenzenliste', () => {
            it('should return the list with all pendenzen', () => {
                const mockPendenz: TSPendenzBetreuung = mockGetPendenzenList();
                mockRestCalls();
                spyOn(gesuchsperiodeRS, 'getAllActiveGesuchsperioden').and.returnValue($q.when([TestDataUtil.createGesuchsperiode20162017()]));
                pendenzBetreuungenListViewController = new PendenzenBetreuungenListViewController(pendenzBetreuungenRS, undefined,
                    institutionRS, institutionStammdatenRS, gesuchsperiodeRS, gesuchModelManager, berechnungsManager, $state, gemeindeRS);
                pendenzBetreuungenListViewController.$onInit();

                $scope.$apply();
                expect(pendenzBetreuungenRS.getPendenzenBetreuungenList).toHaveBeenCalled();

                const list: Array<TSPendenzBetreuung> = pendenzBetreuungenListViewController.getPendenzenList();
                expect(list).toBeDefined();
                expect(list.length).toBe(1);
                expect(list[0]).toEqual(mockPendenz);
            });
        });
    });

    function mockGetPendenzenList(): TSPendenzBetreuung {
        const mockPendenz: TSPendenzBetreuung = new TSPendenzBetreuung('123.12.12.12', '123', '123', '123', 'Kind', 'Kilian', undefined,
            'Platzbestaetigung', undefined, undefined, undefined, TSBetreuungsangebotTyp.KITA, undefined);
        const result: Array<TSPendenzBetreuung> = [mockPendenz];
        spyOn(pendenzBetreuungenRS, 'getPendenzenBetreuungenList').and.returnValue($q.when(result));
        return mockPendenz;
    }

    function mockRestCalls(): void {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen/currentuser').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/institutionstammdaten/currentuser').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/benutzer').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
    }
});

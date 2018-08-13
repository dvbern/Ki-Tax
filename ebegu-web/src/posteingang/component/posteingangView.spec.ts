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

import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import MitteilungRS from '../../app/core/service/mitteilungRS.rest';
import BerechnungsManager from '../../gesuch/service/berechnungsManager';
import GesuchModelManager from '../../gesuch/service/gesuchModelManager';
import GesuchRS from '../../gesuch/service/gesuchRS.rest';
import WizardStepManager from '../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {TSMitteilungStatus} from '../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../models/enums/TSRole';
import TSDossier from '../../models/TSDossier';
import TSFall from '../../models/TSFall';
import TSMitteilung from '../../models/TSMitteilung';
import TSUser from '../../models/TSUser';
import {StateService} from '@uirouter/core';

import TestDataUtil from '../../utils/TestDataUtil.spec';
import {EbeguWebPosteingang} from '../posteingang.module';
import {PosteingangViewController} from './posteingangView';

describe('posteingangView', () => {

    let authServiceRS: AuthServiceRS;
    let gesuchRS: GesuchRS;
    let mitteilungRS: MitteilungRS;
    let posteingangViewController: PosteingangViewController;
    let $q: angular.IQService;
    let $rootScope: angular.IRootScopeService;
    let $filter: angular.IFilterService;
    let $httpBackend: angular.IHttpBackendService;
    let gesuchModelManager: GesuchModelManager;
    let berechnungsManager: BerechnungsManager;
    let $state: StateService;
    let $log: any;
    let CONSTANTS: any;
    let wizardStepManager: WizardStepManager;
    let mockMitteilung: TSMitteilung;
    let authLifeCycleService: AuthLifeCycleService;

    beforeEach(angular.mock.module(EbeguWebPosteingang.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        mitteilungRS = $injector.get('MitteilungRS');
        gesuchRS = $injector.get('GesuchRS');
        $q = $injector.get('$q');
        $rootScope = $injector.get('$rootScope');
        $filter = $injector.get('$filter');
        $httpBackend = $injector.get('$httpBackend');
        gesuchModelManager = $injector.get('GesuchModelManager');
        berechnungsManager = $injector.get('BerechnungsManager');
        $state = $injector.get('$state');
        $log = $injector.get('$log');
        CONSTANTS = $injector.get('CONSTANTS');
        wizardStepManager = $injector.get('WizardStepManager');
        mockMitteilung = mockGetMitteilung();
        authLifeCycleService = $injector.get('AuthLifeCycleService');
    }));

    describe('API Usage', () => {
        describe('searchMitteilungen', () => {
            it('should return the list of Mitteilungen', () => {
                mockRestCalls();
                posteingangViewController = new PosteingangViewController(mitteilungRS, undefined, CONSTANTS, undefined, undefined, undefined, $log, authLifeCycleService);
                $rootScope.$apply();
                const tableFilterState: any = {};
                posteingangViewController.passFilterToServer(tableFilterState).then(result => {
                    expect(mitteilungRS.searchMitteilungen).toHaveBeenCalled();
                    const list: Array<TSMitteilung> = posteingangViewController.displayedCollection;
                    expect(list).toBeDefined();
                    expect(list.length).toBe(1);
                    expect(list[0]).toEqual(mockMitteilung);
                });
            });
        });
    });

    function mockGetMitteilung(): TSMitteilung {
        const mockFall: TSFall = new TSFall();
        mockFall.fallNummer = 123;
        const mockDossier: TSDossier = new TSDossier();
        mockDossier.fall = mockFall;
        const gesuchsteller: TSUser = new TSUser();
        gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
        const mockMitteilung: TSMitteilung = new TSMitteilung(mockDossier, undefined, TSMitteilungTeilnehmerTyp.GESUCHSTELLER, TSMitteilungTeilnehmerTyp.JUGENDAMT,
            gesuchsteller, undefined, 'Frage', 'Warum ist die Banane krumm?', TSMitteilungStatus.NEU, undefined);
        const dtoList: Array<TSMitteilung> = [mockMitteilung];
        const totalSize: number = 1;
        spyOn(mitteilungRS, 'searchMitteilungen').and.returnValue($q.when(dtoList));
        return mockMitteilung;
    }

    function mockRestCalls(): void {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/benutzer').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
    }
});


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

import {StateService} from '@uirouter/core';
import * as angular from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {GesuchRS} from '../../../../gesuch/service/gesuchRS.rest';
import {SearchRS} from '../../../../gesuch/service/searchRS.rest';
import {WizardStepManager} from '../../../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {TSAntragTyp} from '../../../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../../../models/TSAntragDTO';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSPaginationResultDTO} from '../../../../models/TSPaginationResultDTO';
import {TestDataUtil} from '../../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../../../core/core.angularjs.module';
import {GesuchsperiodeRS} from '../../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import {DVQuicksearchListController} from './dv-quicksearch-list';

describe('DVQuicksearchList', () => {

    let institutionRS: InstitutionRS;
    let gesuchsperiodeRS: GesuchsperiodeRS;
    let gesuchRS: GesuchRS;
    let searchRS: SearchRS;
    let quicksearchListViewController: DVQuicksearchListController;
    let $q: angular.IQService;
    let $scope: angular.IScope;
    let $filter: angular.IFilterService;
    let $httpBackend: angular.IHttpBackendService;
    let $state: StateService;
    let wizardStepManager: WizardStepManager;
    let authServiceRS: AuthServiceRS;
    let gemeindeRS: GemeindeRS;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        searchRS = $injector.get('SearchRS');
        institutionRS = $injector.get('InstitutionRS');
        gesuchsperiodeRS = $injector.get('GesuchsperiodeRS');
        $q = $injector.get('$q');
        gesuchRS = $injector.get('GesuchRS');
        $scope = $injector.get('$rootScope');
        $filter = $injector.get('$filter');
        $httpBackend = $injector.get('$httpBackend');
        $state = $injector.get('$state');
        wizardStepManager = $injector.get('WizardStepManager');
        authServiceRS = $injector.get('AuthServiceRS');
        gemeindeRS = $injector.get('GemeindeRS');
    }));

    describe('API Usage', () => {

        describe('translateBetreuungsangebotTypList', () => {
            it('returns a comma separated string with all BetreuungsangebotTypen', () => {
                quicksearchListViewController = new DVQuicksearchListController($filter,
                    institutionRS,
                    gesuchsperiodeRS,
                    $state,
                    authServiceRS,
                    gemeindeRS);
                const list: Array<TSBetreuungsangebotTyp> = [
                    TSBetreuungsangebotTyp.KITA,
                    TSBetreuungsangebotTyp.TAGESFAMILIEN,
                ];
                expect(quicksearchListViewController.translateBetreuungsangebotTypList(list))
                    .toEqual('Kita, Tagesfamilien');
            });
            it('returns an empty string for invalid values or empty lists', () => {
                quicksearchListViewController = new DVQuicksearchListController($filter,
                    institutionRS,
                    gesuchsperiodeRS,
                    $state,
                    authServiceRS,
                    gemeindeRS);
                expect(quicksearchListViewController.translateBetreuungsangebotTypList([])).toEqual('');
                expect(quicksearchListViewController.translateBetreuungsangebotTypList(undefined)).toEqual('');
                expect(quicksearchListViewController.translateBetreuungsangebotTypList(null)).toEqual('');
            });
        });
        describe('editAntrag', () => {
            it('should call findGesuch and open the view gesuch.fallcreation with it', () => {
                const mockAntrag = mockGetAntragList();
                mockRestCalls();
                spyOn($state, 'go');
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(undefined);
                quicksearchListViewController = new DVQuicksearchListController($filter,
                    institutionRS,
                    gesuchsperiodeRS,
                    $state,
                    authServiceRS,
                    gemeindeRS);

                const tsGesuch = new TSGesuch();
                spyOn(gesuchRS, 'findGesuch').and.returnValue($q.when(tsGesuch));

                quicksearchListViewController.editAntrag(mockAntrag, undefined); // antrag wird eidtiert
                $scope.$apply();

                expect($state.go).toHaveBeenCalledWith('gesuch.fallcreation', {
                    gesuchId: '66345345',
                    dossierId: mockAntrag.dossierId,
                });

            });
        });
    });

    function mockGetAntragList(): TSAntragDTO {
        const fallNummer = 123;
        const mockAntrag = new TSAntragDTO();
        mockAntrag.antragId = '66345345';
        mockAntrag.fallNummer = fallNummer;
        mockAntrag.familienName = 'name';
        mockAntrag.antragTyp = TSAntragTyp.ERSTGESUCH;
        mockAntrag.eingangsdatum = undefined;
        mockAntrag.eingangsdatumSTV = undefined;
        mockAntrag.aenderungsdatum = undefined;
        mockAntrag.angebote = [TSBetreuungsangebotTyp.KITA];
        mockAntrag.institutionen = ['Inst1, Inst2'];
        mockAntrag.verantwortlicherBG = 'Juan Arbolado';
        mockAntrag.verantwortlicherTS = 'Juan Arbolado';
        mockAntrag.status = undefined;
        mockAntrag.gesuchsperiodeGueltigAb = undefined;
        mockAntrag.gesuchsperiodeGueltigBis = undefined;
        spyOn(searchRS, 'getPendenzenList')
            .and.returnValue($q.resolve(new TSPaginationResultDTO([mockAntrag], 1)));
        return mockAntrag;
    }

    function mockRestCalls(): void {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/benutzer').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/').respond({});
    }
});

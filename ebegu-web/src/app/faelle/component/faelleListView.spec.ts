/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IHttpBackendService, ILogService, IQService, IScope} from 'angular';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GesuchModelManager} from '../../../gesuch/service/gesuchModelManager';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {WizardStepManager} from '../../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSAntragDTO} from '../../../models/TSAntragDTO';
import {TSGesuch} from '../../../models/TSGesuch';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {FAELLE_JS_MODULE} from '../faelle.module';
import {FaelleListViewController} from './faelleListView';

describe('faelleListView', () => {

    let authServiceRS: AuthServiceRS;
    let gesuchRS: GesuchRS;
    let faelleListViewController: FaelleListViewController;
    let $q: IQService;
    let $scope: IScope;
    let $httpBackend: IHttpBackendService;
    let gesuchModelManager: GesuchModelManager;
    let $state: StateService;
    let $log: ILogService;
    let wizardStepManager: WizardStepManager;
    let mockAntrag: TSAntragDTO;

    beforeEach(angular.mock.module(FAELLE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        authServiceRS = $injector.get('AuthServiceRS');
        gesuchRS = $injector.get('GesuchRS');
        $q = $injector.get('$q');
        $scope = $injector.get('$rootScope');
        $httpBackend = $injector.get('$httpBackend');
        gesuchModelManager = $injector.get('GesuchModelManager');
        $state = $injector.get('$state');
        $log = $injector.get('$log');
        wizardStepManager = $injector.get('WizardStepManager');
        mockAntrag = mockGetPendenzenList();
    }));

    describe('API Usage', () => {
        describe('searchFaelle', () => {
            it('should return the list with found Faellen', () => {
                mockRestCalls();
                faelleListViewController = new FaelleListViewController(gesuchModelManager,
                    $state,
                    $log,
                    authServiceRS);

                $scope.$apply();
            });
        });
        describe('editPendenzJA', () => {
            it('should call findGesuch and open the view gesuch.fallcreation with it for normal user', () => {
                callEditFall();

                expect($state.go).toHaveBeenCalledWith('gesuch.fallcreation',
                    {gesuchId: '66345345', dossierId: '11111111'});

            });
            it('should call findGesuch and open the view gesuch.betreuungen with it for INS/TRAEGER user if gesuch not verfuegt',
                () => {
                    spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
                    callEditFall();
                    expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: '66345345'});
                });
            it('should call findGesuch and open the view gesuch.verfuegen with it for INS/TRAEGER user if gesuch verfuegt',
                () => {
                    spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
                    mockAntrag.status = TSAntragStatus.VERFUEGT;
                    callEditFall();
                    expect($state.go).toHaveBeenCalledWith('gesuch.verfuegen', {gesuchId: '66345345'});
                });
        });
    });

    function mockGetPendenzenList(): TSAntragDTO {
        const fallNummer = 123;
        const mockPendenz = new TSAntragDTO();
        mockPendenz.antragId = '66345345';
        mockPendenz.fallNummer = fallNummer;
        mockPendenz.familienName = 'name';
        mockPendenz.antragTyp = TSAntragTyp.ERSTGESUCH;
        mockPendenz.angebote = [TSBetreuungsangebotTyp.KITA];
        mockPendenz.institutionen = ['Inst1, Inst2'];
        mockPendenz.verantwortlicherBG = 'Juan Arbolado';
        mockPendenz.verantwortlicherTS = 'Juan Arbolado';

        mockPendenz.dossierId = '11111111';
        return mockPendenz;
    }

    function mockRestCalls(): void {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/institutionen').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/benutzer').respond({});
        $httpBackend.when('GET', '/ebegu/api/v1/gesuchsperioden/active').respond({});
    }

    function callEditFall(): TSGesuch {
        mockRestCalls();
        spyOn($state, 'go');
        spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(undefined);
        faelleListViewController = new FaelleListViewController(gesuchModelManager,
            $state,
            $log,
            authServiceRS);

        const tsGesuch = new TSGesuch();
        spyOn(gesuchRS, 'findGesuch').and.returnValue($q.when(tsGesuch));
        spyOn(gesuchRS, 'findGesuchForInstitution').and.returnValue($q.when(tsGesuch));

        faelleListViewController.editFall(mockAntrag, undefined);
        $scope.$apply();
        return tsGesuch;
    }
});

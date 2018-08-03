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

import {EbeguWebCore} from '../../../app/core/core.angularjs.module';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {TSRole} from '../../../models/enums/TSRole';
import TSUser from '../../../models/TSUser';
import {EbeguAuthentication} from '../../authentication.module';
import {AuthLifeCycleService} from '../../service/authLifeCycle.service';
import AuthServiceRS from '../../service/AuthServiceRS.rest';
import {StartViewController} from './startView';
import {StateService} from '@uirouter/core';

describe('startView', () => {

    //evtl ist modulaufteilung hier nicht ganz sauber, wir brauchen sowohl core als auch auth modul
    beforeEach(angular.mock.module(EbeguWebCore.name));
    beforeEach(angular.mock.module(EbeguAuthentication.name));

    beforeEach(angular.mock.module(ngServicesMock));

    let authLifeCycleService: AuthLifeCycleService;
    let $componentController: angular.IComponentControllerService;
    let startViewController: StartViewController;
    let authService: AuthServiceRS;
    let mockPrincipal: TSUser;
    let state: StateService;

    beforeEach(angular.mock.inject($injector => {
        $componentController = $injector.get('$componentController');
        authLifeCycleService = $injector.get('AuthLifeCycleService');
        authService = $injector.get('AuthServiceRS');
        state = $injector.get('$state');
        startViewController = new StartViewController(state, authLifeCycleService, authService);

    }));
    beforeEach(() => {
        mockPrincipal = new TSUser();
        mockPrincipal.nachname = 'mockprincipal';
        mockPrincipal.vorname = 'tester';
        mockPrincipal.currentBerechtigung.role = TSRole.GESUCHSTELLER;
    });

    it('should be defined', () => {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        expect(startViewController).toBeDefined();
    });

    it('should  broadcast "AUTH_EVENTS.notAuthenticated" if no principal is available', () => {
        const broadcast = spyOn(authLifeCycleService, 'changeAuthStatus');
        startViewController.$onInit();
        expect(broadcast).toHaveBeenCalledWith(TSAuthEvent.NOT_AUTHENTICATED, 'not logged in on startpage');
    });

    describe('should  redirect based on role of principal', () => {
        it('should go to gesuchstellerDashboard if role is gesuchsteller', () => {
            spyOn(authService, 'getPrincipal').and.returnValue(mockPrincipal);
            spyOn(state, 'go');
            startViewController.$onInit();
            expect(state.go).toHaveBeenCalledWith('gesuchsteller.dashboard');
        });
        it('should go to pendenzen if role is sachbearbeiter ja', () => {
            mockPrincipal.currentBerechtigung.role = TSRole.SACHBEARBEITER_JA;
            spyOn(authService, 'getPrincipal').and.returnValue(mockPrincipal);
            spyOn(state, 'go');
            startViewController.$onInit();
            expect(state.go).toHaveBeenCalledWith('pendenzen.list-view');
        });
    });
});

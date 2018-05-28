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

import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {DVMitteilungListController} from '../../../core/component/dv-mitteilung-list/dv-mitteilung-list';
import BetreuungRS from '../../../core/service/betreuungRS.rest';
import MitteilungRS from '../../../core/service/mitteilungRS.rest';
import FallRS from '../../../gesuch/service/fallRS.rest';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../models/enums/TSRole';
import TSFall from '../../../models/TSFall';
import TSMitteilung from '../../../models/TSMitteilung';
import TSUser from '../../../models/TSUser';
import TestDataUtil from '../../../utils/TestDataUtil';
import {EbeguWebMitteilungen} from '../../mitteilungen.module';
import {IMitteilungenStateParams} from '../../mitteilungen.route';
import ITimeoutService = angular.ITimeoutService;

describe('mitteilungenView', function () {

    let mitteilungRS: MitteilungRS;
    let authServiceRS: AuthServiceRS;
    let stateParams: IMitteilungenStateParams;
    let fallRS: FallRS;
    let betreuungRS: BetreuungRS;
    let fall: TSFall;
    let $rootScope: angular.IRootScopeService;
    let $q: angular.IQService;
    let controller: DVMitteilungListController;
    let besitzer: TSUser;
    let verantwortlicher: TSUser;
    let scope: angular.IScope;
    let $timeout: ITimeoutService;

    beforeEach(angular.mock.module(EbeguWebMitteilungen.name));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        mitteilungRS = $injector.get('MitteilungRS');
        authServiceRS = $injector.get('AuthServiceRS');
        fallRS = $injector.get('FallRS');
        betreuungRS = $injector.get('BetreuungRS');
        stateParams = $injector.get('$stateParams');
        stateParams = $injector.get('$stateParams');
        $timeout = $injector.get('$timeout');
        $rootScope = $injector.get('$rootScope');
        $q = $injector.get('$q');
        scope = $rootScope.$new();

        // prepare fall
        stateParams.fallId = '123';
        fall = new TSFall();
        fall.id = stateParams.fallId;
        besitzer = new TSUser();
        besitzer.nachname = 'Romualdo Besitzer';
        fall.besitzer = besitzer;
        verantwortlicher = new TSUser();
        verantwortlicher.nachname = 'Arnaldo Verantwortlicher';
        fall.verantwortlicher = verantwortlicher;

        spyOn(mitteilungRS, 'getEntwurfForCurrentRolleForFall').and.returnValue($q.when(undefined));
    }));

    let assertMitteilungContent = function () {
        expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(TSMitteilungStatus.ENTWURF);
        expect(controller.getCurrentMitteilung().fall).toBe(fall);
        // diese Parameter muessen im Server gesetzt werden
        expect(controller.getCurrentMitteilung().empfaenger).toBeUndefined();
        expect(controller.getCurrentMitteilung().senderTyp).toBeUndefined();
        expect(controller.getCurrentMitteilung().empfaengerTyp).toBeUndefined();
    };
    describe('loading initial data', function () {
        it('should create an empty TSMItteilung for GS', function () {
            let gesuchsteller: TSUser = new TSUser();
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);
            compareCommonAttributes(gesuchsteller);
            assertMitteilungContent();
        });
        it('should create an empty TSMItteilung for JA', function () {
            let sachbearbeiter_ja: TSUser = new TSUser();
            sachbearbeiter_ja.currentBerechtigung.role = TSRole.SACHBEARBEITER_JA;
            spyOn(authServiceRS, 'isOneOfRoles').and.callFake((roles: Array<TSRole>) => {
                return roles.indexOf(TSRole.SACHBEARBEITER_JA) >= 0;
            });

            createMitteilungForUser(sachbearbeiter_ja);

            compareCommonAttributes(sachbearbeiter_ja);
            assertMitteilungContent();
        });
        it('should create an empty TSMItteilung for Institution', function () {
            let sachbearbeiter_inst: TSUser = new TSUser();
            sachbearbeiter_inst.currentBerechtigung.role = TSRole.SACHBEARBEITER_INSTITUTION;
            spyOn(authServiceRS, 'isOneOfRoles').and.callFake((roles: Array<TSRole>) => {
                return roles.indexOf(TSRole.SACHBEARBEITER_INSTITUTION) >= 0;
            });

            createMitteilungForUser(sachbearbeiter_inst);

            compareCommonAttributes(sachbearbeiter_inst);
            assertMitteilungContent();
        });
    });
    describe('sendMitteilung', function () {
        it('should send the current mitteilung and update currentMitteilung with the new content', function () {
            let gesuchsteller: TSUser = new TSUser();
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);

            // mock saved mitteilung
            let savedMitteilung: TSMitteilung = new TSMitteilung();
            savedMitteilung.id = '321';
            savedMitteilung.mitteilungStatus = TSMitteilungStatus.NEU;
            spyOn(mitteilungRS, 'sendMitteilung').and.returnValue($q.when(savedMitteilung));
            controller.getCurrentMitteilung().subject = 'subject';
            controller.getCurrentMitteilung().message = 'message';

            controller.form = TestDataUtil.createDummyForm();
            controller.form.$dirty = true;
            controller.sendMitteilung();

            expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(TSMitteilungStatus.ENTWURF);
            expect(controller.getCurrentMitteilung().sentDatum).toBeUndefined();
            expect(controller.getCurrentMitteilung().id).toBeUndefined();
        });
    });
    describe('setErledigt', function () {
        it('should change the status from GELESEN to ERLEDIGT and save the mitteilung', function () {
            let gesuchsteller: TSUser = new TSUser();
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);

            let mitteilung: TSMitteilung = new TSMitteilung();
            mitteilung.id = '123';
            spyOn(mitteilungRS, 'setMitteilungErledigt').and.returnValue($q.when(mitteilung));

            mitteilung.mitteilungStatus = TSMitteilungStatus.ENTWURF;
            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(TSMitteilungStatus.ENTWURF); // Status ENTWURF wird nicht geaendert
            expect(mitteilungRS.setMitteilungErledigt).not.toHaveBeenCalled();

            mitteilung.mitteilungStatus = TSMitteilungStatus.NEU;
            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(TSMitteilungStatus.NEU); // Status NEU wird nicht geaendert
            expect(mitteilungRS.setMitteilungErledigt).not.toHaveBeenCalled();

            mitteilung.mitteilungStatus = TSMitteilungStatus.GELESEN;
            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(TSMitteilungStatus.ERLEDIGT); // von GELESEN auf ERLEDIGT
            expect(mitteilungRS.setMitteilungErledigt).toHaveBeenCalledWith('123');

            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(TSMitteilungStatus.GELESEN); // von ERLEDIGT auf GELESEN
            expect(mitteilungRS.setMitteilungErledigt).toHaveBeenCalledWith('123');
        });
    });

    function compareCommonAttributes(currentUser: TSUser): void {
        expect(controller.getCurrentMitteilung()).toBeDefined();
        expect(controller.getCurrentMitteilung().fall).toBe(fall);
        expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(TSMitteilungStatus.ENTWURF);
        expect(controller.getCurrentMitteilung().sender).toBe(currentUser);
        expect(controller.getCurrentMitteilung().subject).toBeUndefined();
        expect(controller.getCurrentMitteilung().message).toBeUndefined();
    }

    function createMitteilungForUser(user: TSUser): void {
        spyOn(authServiceRS, 'getPrincipal').and.returnValue(user);
        spyOn(fallRS, 'findFall').and.returnValue($q.when(fall));
        spyOn(mitteilungRS, 'getMitteilungenForCurrentRolleForFall').and.returnValue($q.when([{}]));
        spyOn(mitteilungRS, 'setAllNewMitteilungenOfFallGelesen').and.returnValue($q.when([{}]));
        controller = new DVMitteilungListController(stateParams, mitteilungRS, authServiceRS, fallRS, betreuungRS, $q, null,
            $rootScope, undefined, undefined, undefined, undefined, scope, $timeout);
        controller.$onInit();   // hack, muesste wohl eher so gehen
                                // http://stackoverflow.com/questions/38631204/how-to-trigger-oninit-or-onchanges-implictly-in-unit-testing-angular-component
        $rootScope.$apply();
    }

});

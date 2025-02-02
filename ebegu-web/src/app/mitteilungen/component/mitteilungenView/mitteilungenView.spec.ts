/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import * as angular from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {DossierRS} from '../../../../gesuch/service/dossierRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {GesuchRS} from '../../../../gesuch/service/gesuchRS.rest';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../../hybridTools/translationsMock';
import {TSMitteilungStatus} from '../../../../models/enums/TSMitteilungStatus';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSBenutzerNoDetails} from '../../../../models/TSBenutzerNoDetails';
import {TSDossier} from '../../../../models/TSDossier';
import {TSFall} from '../../../../models/TSFall';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSGemeindeStammdaten} from '../../../../models/TSGemeindeStammdaten';
import {TSMitteilung} from '../../../../models/TSMitteilung';
import {TestDataUtil} from '../../../../utils/TestDataUtil.spec';
import {DVMitteilungListController} from '../../../core/component/dv-mitteilung-list/dv-mitteilung-list';
import {BetreuungRS} from '../../../core/service/betreuungRS.rest';
import {InstitutionRS} from '../../../core/service/institutionRS.rest';
import {MitteilungRS} from '../../../core/service/mitteilungRS.rest';
import {MITTEILUNGEN_JS_MODULE} from '../../mitteilungen.module';
import {IMitteilungenStateParams} from '../../mitteilungen.route';
import ITimeoutService = angular.ITimeoutService;

describe('mitteilungenView', () => {
    let mitteilungRS: MitteilungRS;
    let authServiceRS: AuthServiceRS;
    let stateParams: IMitteilungenStateParams;
    let dossierRS: DossierRS;
    let betreuungRS: BetreuungRS;
    let fall: TSFall;
    let dossier: TSDossier;
    let $rootScope: angular.IRootScopeService;
    let $q: angular.IQService;
    let controller: DVMitteilungListController;
    let besitzer: TSBenutzer;
    let verantwortlicher: TSBenutzerNoDetails;
    let scope: angular.IScope;
    let $timeout: ITimeoutService;
    let institutionRS: InstitutionRS;
    let gemeindeRS: GemeindeRS;
    let gesuchRS: GesuchRS;

    beforeEach(angular.mock.module(MITTEILUNGEN_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            mitteilungRS = $injector.get('MitteilungRS');
            authServiceRS = $injector.get('AuthServiceRS');
            betreuungRS = $injector.get('BetreuungRS');
            stateParams = $injector.get('$stateParams');
            dossierRS = $injector.get('DossierRS');
            $timeout = $injector.get('$timeout');
            $rootScope = $injector.get('$rootScope');
            $q = $injector.get('$q');
            institutionRS = $injector.get('InstitutionRS');
            scope = $rootScope.$new();
            gemeindeRS = $injector.get('GemeindeRS');
            gesuchRS = $injector.get('GesuchRS');

            // prepare fall
            stateParams.dossierId = '123';
            fall = new TSFall();
            fall.id = stateParams.dossierId;
            dossier = new TSDossier();
            dossier.id = stateParams.dossierId;
            dossier.fall = fall;
            besitzer = new TSBenutzer();
            besitzer.nachname = 'Romualdo Besitzer';
            fall.besitzer = besitzer;
            dossier.fall.besitzer = besitzer;
            dossier.gemeinde = new TSGemeinde();
            dossier.gemeinde.id = '11111111-1111-1111-1111-111111111111';
            verantwortlicher = new TSBenutzerNoDetails();
            verantwortlicher.nachname = 'Arnaldo Verantwortlicher';
            dossier.verantwortlicherBG = verantwortlicher;

            spyOn(
                mitteilungRS,
                'getEntwurfOfDossierForCurrentRolle'
            ).and.returnValue($q.when(undefined));

            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                $injector.get('$httpBackend')
            );
            spyOn(gemeindeRS, 'getGemeindeStammdaten').and.returnValue(
                $q.when({} as TSGemeindeStammdaten)
            );
        })
    );

    const assertMitteilungContent = () => {
        expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(
            TSMitteilungStatus.NEU
        );
        expect(controller.getCurrentMitteilung().dossier).toBe(dossier);
        // diese Parameter muessen im Server gesetzt werden
        expect(controller.getCurrentMitteilung().empfaenger).toBeUndefined();
        expect(controller.getCurrentMitteilung().senderTyp).toBeUndefined();
        expect(controller.getCurrentMitteilung().empfaengerTyp).toBeUndefined();
    };
    describe('loading initial data', () => {
        it('should create an empty TSMItteilung for GS', () => {
            const gesuchsteller = new TSBenutzer();
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);
            compareCommonAttributes(gesuchsteller);
            assertMitteilungContent();
        });
        it('should create an empty TSMItteilung for JA', () => {
            const sachbearbeiterBG = new TSBenutzer();
            sachbearbeiterBG.currentBerechtigung.role =
                TSRole.SACHBEARBEITER_BG;
            spyOn(authServiceRS, 'isOneOfRoles').and.callFake(
                (roles: Array<TSRole>) =>
                    roles.indexOf(TSRole.SACHBEARBEITER_BG) >= 0
            );

            createMitteilungForUser(sachbearbeiterBG);

            compareCommonAttributes(sachbearbeiterBG);
            assertMitteilungContent();
        });
        it('should create an empty TSMItteilung for Institution', () => {
            const sachbearbeiterInst = new TSBenutzer();
            sachbearbeiterInst.currentBerechtigung.role =
                TSRole.SACHBEARBEITER_INSTITUTION;
            spyOn(authServiceRS, 'isOneOfRoles').and.callFake(
                (roles: Array<TSRole>) =>
                    roles.indexOf(TSRole.SACHBEARBEITER_INSTITUTION) >= 0
            );

            createMitteilungForUser(sachbearbeiterInst);

            compareCommonAttributes(sachbearbeiterInst);
            assertMitteilungContent();
        });
    });
    describe('sendMitteilung', () => {
        it('should send the current mitteilung and update currentMitteilung with the new content', () => {
            const gesuchsteller = new TSBenutzer();
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);

            // mock saved mitteilung
            const savedMitteilung = new TSMitteilung();
            savedMitteilung.id = '321';
            savedMitteilung.mitteilungStatus = TSMitteilungStatus.NEU;
            spyOn(mitteilungRS, 'sendMitteilung').and.returnValue(
                $q.when(savedMitteilung)
            );
            controller.getCurrentMitteilung().subject = 'subject';
            controller.getCurrentMitteilung().message = 'message';

            controller.form = TestDataUtil.createDummyForm();
            controller.form.$dirty = true;
            controller.sendMitteilung();

            expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(
                TSMitteilungStatus.NEU
            );
            expect(controller.getCurrentMitteilung().sentDatum).toBeUndefined();
            expect(controller.getCurrentMitteilung().id).toBeUndefined();
        });
    });
    describe('setErledigt', () => {
        it('should change the status from GELESEN to ERLEDIGT and save the mitteilung', () => {
            const gesuchsteller = new TSBenutzer();
            gesuchsteller.username = 'emma';
            gesuchsteller.currentBerechtigung.role = TSRole.GESUCHSTELLER;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);

            createMitteilungForUser(gesuchsteller);

            const mitteilung = new TSMitteilung();
            mitteilung.id = '123';
            mitteilung.empfaenger = gesuchsteller;
            spyOn(mitteilungRS, 'setMitteilungErledigt').and.returnValue(
                $q.when(mitteilung)
            );

            mitteilung.mitteilungStatus = TSMitteilungStatus.GELESEN;
            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(
                TSMitteilungStatus.ERLEDIGT
            ); // von GELESEN auf ERLEDIGT
            expect(mitteilungRS.setMitteilungErledigt).toHaveBeenCalledWith(
                '123'
            );

            controller.setErledigt(mitteilung);
            expect(mitteilung.mitteilungStatus).toBe(
                TSMitteilungStatus.GELESEN
            ); // von ERLEDIGT auf GELESEN
            expect(mitteilungRS.setMitteilungErledigt).toHaveBeenCalledWith(
                '123'
            );
        });
    });

    function compareCommonAttributes(currentUser: TSBenutzer): void {
        expect(controller.getCurrentMitteilung()).toBeDefined();
        expect(controller.getCurrentMitteilung().dossier).toBe(dossier);
        expect(controller.getCurrentMitteilung().mitteilungStatus).toBe(
            TSMitteilungStatus.NEU
        );
        expect(controller.getCurrentMitteilung().sender).toBe(currentUser);
        expect(controller.getCurrentMitteilung().subject).toBeUndefined();
        expect(controller.getCurrentMitteilung().message).toBeUndefined();
    }

    function createMitteilungForUser(user: TSBenutzer): void {
        spyOn(authServiceRS, 'getPrincipal').and.returnValue(user);
        spyOn(dossierRS, 'findDossier').and.returnValue($q.when(dossier));
        spyOn(
            mitteilungRS,
            'getMitteilungenOfDossierForCurrentRolle'
        ).and.returnValue($q.resolve([]));
        spyOn(
            mitteilungRS,
            'setAllNewMitteilungenOfDossierGelesen'
        ).and.returnValue($q.resolve([]));
        controller = new DVMitteilungListController(
            stateParams,
            mitteilungRS,
            authServiceRS,
            betreuungRS,
            $q,
            null,
            $rootScope,
            undefined,
            undefined,
            undefined,
            undefined,
            scope,
            $timeout,
            dossierRS,
            undefined,
            institutionRS,
            gemeindeRS,
            gesuchRS
        );
        controller.$onInit(); // hack, muesste wohl eher so gehen
        // http://stackoverflow.com/questions/38631204/how-to-trigger-oninit-or-onchanges-implictly-in-unit-testing-angular-component
        $rootScope.$apply();
    }
});

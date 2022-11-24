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
import {IComponentControllerService, IHttpBackendService, IScope} from 'angular';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSDossier} from '../../../models/TSDossier';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGemeindeStammdatenLite} from '../../../models/TSGemeindeStammdatenLite';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {ErwerbspensumListViewController} from './erwerbspensumListView';
import IInjectorService = angular.auto.IInjectorService;

describe('erwerbspensumListView', () => {

    const gemeindeTelefon = '915445152';
    const gemeindeMail = 'mail@mail.com';
    const gemeindeStammdaten = new TSGemeindeStammdatenLite();
    gemeindeStammdaten.telefon = gemeindeTelefon;
    gemeindeStammdaten.mail = gemeindeMail;

    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    let component: ErwerbspensumListViewController;
    let scope: IScope;
    let $componentController: IComponentControllerService;
    let gesuchModelManager: GesuchModelManager;
    let $q: angular.IQService;
    let dossier: TSDossier;
    let gesuchsperiode: TSGesuchsperiode;
    let einstellungRS: EinstellungRS;
    let $httpBackend: IHttpBackendService;
    let ebeguRestUtil: EbeguRestUtil;

    beforeEach(angular.mock.inject(($injector: IInjectorService) => {
        prepareDossier();
        prepareGesuchsperiode();

        gesuchModelManager = $injector.get('GesuchModelManager');
        $componentController = $injector.get('$componentController');
        einstellungRS = $injector.get('EinstellungRS');
        $q = $injector.get('$q');
        scope = $injector.get('$rootScope').$new();
        $httpBackend = $injector.get('$httpBackend');
        ebeguRestUtil = $injector.get('EbeguRestUtil');

        spyOn(gesuchModelManager, 'showInfoAusserordentlichenAnspruch').and.returnValue($q.when(false));
        spyOn(gesuchModelManager, 'getDossier').and.returnValue(dossier);
        spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(gesuchsperiode);
        spyOn(einstellungRS, 'getAllEinstellungenBySystemCached').and.returnValue(
            of([new TSEinstellung(null, TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM)])
        );
        spyOn(ebeguRestUtil, 'parseAnspruchBeschaeftigungAbhaengigkeitTyp')
            .and.returnValue(TSAnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING);
        gesuchModelManager.gemeindeStammdaten = gemeindeStammdaten;

        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        $httpBackend.when('GET', '/ebegu/api/v1/erwerbspensen/required/').respond({});
    }));

    beforeEach(() => {
        /*
         To initialise your component controller you have to setup your (mock) bindings and
         pass them to $componentController.
         */
        const bindings = {};
        component = $componentController('erwerbspensumListView', {$scope: scope}, bindings);
        scope.$apply();
    });

    it('should be defined', () => {
        expect(component).toBeDefined();
    });

    it('should set email and telefon from Gemeinde', () => {
        expect(component.gemeindeTelefon).toBe(gemeindeTelefon);
        expect(component.gemeindeEmail).toBe(gemeindeMail);
    });

    function prepareDossier(): void {
        dossier = new TSDossier();
        dossier.gemeinde = TestDataUtil.createGemeindeParis();
    }

    function prepareGesuchsperiode(): void {
        gesuchsperiode = new TSGesuchsperiode();
        gesuchsperiode.id = '123';
    }
});

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

import {TranslateService} from '@ngx-translate/core';
import {IHttpBackendService, IQService, IScope, ITimeoutService} from 'angular';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {DvDialog} from '../../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../../app/core/service/downloadRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../../hybridTools/translationsMock';
import {TSDossier} from '../../../../models/TSDossier';
import {TSDownloadFile} from '../../../../models/TSDownloadFile';
import {TSEinstellung} from '../../../../models/TSEinstellung';
import {TSFall} from '../../../../models/TSFall';
import {TSGemeinde} from '../../../../models/TSGemeinde';
import {TSGesuch} from '../../../../models/TSGesuch';
import {TSGesuchsperiode} from '../../../../models/TSGesuchsperiode';
import {TestDataUtil} from '../../../../utils/TestDataUtil.spec';
import {GESUCH_JS_MODULE} from '../../../gesuch.module';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {FreigabeService} from '../../freigabe.service';
import {FreigabeViewController} from './freigabeView';

describe('freigabeView', () => {
    let controller: FreigabeViewController;
    let $scope: IScope;
    let wizardStepManager: WizardStepManager;
    let dialog: DvDialog;
    let downloadRS: DownloadRS;
    let $q: IQService;
    let gesuchModelManager: GesuchModelManager;
    let $httpBackend: IHttpBackendService;
    let applicationPropertyRS: any;
    let authServiceRS: AuthServiceRS;
    let $timeout: ITimeoutService;
    let dossier: TSDossier;
    let fall: TSFall;
    let $translate: TranslateService;
    let einstellungRS: EinstellungRS;
    let freigabeService: FreigabeService;

    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            $scope = $injector.get('$rootScope');
            wizardStepManager = $injector.get('WizardStepManager');
            dialog = $injector.get('DvDialog');
            downloadRS = $injector.get('DownloadRS');
            $q = $injector.get('$q');
            gesuchModelManager = $injector.get('GesuchModelManager');
            $httpBackend = $injector.get('$httpBackend');
            applicationPropertyRS = $injector.get('ApplicationPropertyRS');
            authServiceRS = $injector.get('AuthServiceRS');
            $timeout = $injector.get('$timeout');
            $translate = $injector.get('$translate');
            einstellungRS = $injector.get('EinstellungRS');
            freigabeService = $injector.get('FreigabeService');

            spyOn(applicationPropertyRS, 'isDevMode').and.returnValue(
                $q.when(false)
            );
            spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
            spyOn(
                wizardStepManager,
                'updateCurrentWizardStepStatus'
            ).and.returnValue($q.resolve());

            fall = TestDataUtil.createFall();

            dossier = TestDataUtil.createDossier('', fall);
            dossier.gemeinde = TestDataUtil.createGemeindeParis();
            spyOn(gesuchModelManager, 'getDossier').and.returnValue(dossier);
            spyOn(gesuchModelManager, 'getFall').and.returnValue(fall);
            spyOn(gesuchModelManager, 'getGemeinde').and.returnValue(
                new TSGemeinde()
            );
            spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                new TSGesuchsperiode()
            );
            spyOn(einstellungRS, 'findEinstellung').and.returnValue(
                of(new TSEinstellung(null, null, 'true'))
            );

            controller = new FreigabeViewController(
                gesuchModelManager,
                $injector.get('BerechnungsManager'),
                wizardStepManager,
                dialog,
                downloadRS,
                $scope,
                applicationPropertyRS,
                authServiceRS,
                $timeout,
                $translate,
                einstellungRS,
                freigabeService
            );

            controller.form = {} as any;
            spyOn(controller, 'isGesuchValid').and.callFake(
                () => controller.form.$valid
            );
            controller.form = TestDataUtil.createDummyForm();
        })
    );

    describe('gesuchFreigeben', () => {
        it('should return undefined when the form is not valid', () => {
            controller.form.$valid = false;

            const returned = controller.gesuchEinreichen();

            expect(returned).toBeUndefined();
        });
        it('should call showDialog when form is valid', () => {
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

            controller.form.$valid = true;
            spyOn(dialog, 'showDialog').and.returnValue($q.when({}));

            const returned = controller.gesuchEinreichen();
            $scope.$apply();

            expect(dialog.showDialog).toHaveBeenCalled();
            expect(returned).toBeDefined();
        });
    });
    describe('confirmationCallback', () => {
        it('should return a Promise when the form is valid', () => {
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

            controller.form.$valid = true;

            spyOn(dialog, 'showDialog').and.returnValue($q.when({}));

            const downloadFile = new TSDownloadFile();
            downloadFile.accessToken = 'token';
            downloadFile.filename = 'name';
            spyOn(
                downloadRS,
                'getFreigabequittungAccessTokenGeneratedDokument'
            ).and.returnValue($q.resolve(downloadFile));
            spyOn(downloadRS, 'startDownload').and.returnValue();

            const fakeWindow: any = undefined;
            spyOn(downloadRS, 'prepareDownloadWindow').and.returnValue(
                fakeWindow
            );

            const gesuch = new TSGesuch();
            gesuch.id = '123';
            spyOn(gesuchModelManager, 'openGesuch').and.returnValue(
                $q.resolve(gesuch)
            );
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);

            controller.confirmationCallback();
            $scope.$apply();

            expect(
                downloadRS.getFreigabequittungAccessTokenGeneratedDokument
            ).toHaveBeenCalledWith(gesuch.id, true);
            expect(downloadRS.startDownload).toHaveBeenCalledWith(
                downloadFile.accessToken,
                downloadFile.filename,
                false,
                fakeWindow
            );
        });
    });
    describe('openFreigabequittungPDF', () => {
        let gesuch: TSGesuch;
        let tsDownloadFile: TSDownloadFile;

        beforeEach(() => {
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            spyOn(gesuchModelManager, 'openGesuch').and.returnValue(
                $q.resolve(gesuch)
            );
            spyOn(downloadRS, 'startDownload').and.returnValue();
            tsDownloadFile = new TSDownloadFile();
            spyOn(
                downloadRS,
                'getFreigabequittungAccessTokenGeneratedDokument'
            ).and.returnValue($q.resolve(tsDownloadFile));
            gesuch = new TSGesuch();
            gesuch.id = '123';
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
        });
        it('should call the service for Erstgesuch', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);

            controller.openFreigabequittungPDF(false);
            $scope.$apply();

            expect(
                downloadRS.getFreigabequittungAccessTokenGeneratedDokument
            ).toHaveBeenCalledWith(gesuch.id, false);
        });
    });
});

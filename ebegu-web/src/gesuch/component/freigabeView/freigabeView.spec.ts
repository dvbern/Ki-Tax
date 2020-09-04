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

import {IHttpBackendService, IQService, IScope, ITimeoutService} from 'angular';
import {ADMIN_JS_MODULE} from '../../../admin/admin.module';
import {DvDialog} from '../../../app/core/directive/dv-dialog/dv-dialog';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSDossier} from '../../../models/TSDossier';
import {TSDownloadFile} from '../../../models/TSDownloadFile';
import {TSGesuch} from '../../../models/TSGesuch';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
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

    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ADMIN_JS_MODULE.name));  // to inject applicationPropertyRS

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
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

        spyOn(applicationPropertyRS, 'isDevMode').and.returnValue($q.when(false));
        spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
        spyOn(wizardStepManager, 'updateCurrentWizardStepStatus').and.returnValue($q.resolve());

        dossier = TestDataUtil.createDossier('', undefined);
        dossier.gemeinde = TestDataUtil.createGemeindeParis();
        spyOn(gesuchModelManager, 'getDossier').and.returnValue(dossier);

        controller = new FreigabeViewController(gesuchModelManager, $injector.get('BerechnungsManager'),
            wizardStepManager, dialog, downloadRS, $scope, applicationPropertyRS, authServiceRS, $timeout);

        controller.form = {} as any;
        spyOn(controller, 'isGesuchValid').and.callFake(() => controller.form.$valid);
        controller.form = TestDataUtil.createDummyForm();
    }));
    describe('canBeFreigegeben', () => {
        it('should return false when not all steps are true', () => {
            spyOn(wizardStepManager, 'areAllStepsOK').and.returnValue(false);
            spyOn(wizardStepManager, 'hasStepGivenStatus').and.returnValue(true);
            expect(controller.canBeFreigegeben()).toBe(false);
        });
        it('should return false when all steps are true but not all Betreuungen are accepted', () => {
            spyOn(wizardStepManager, 'areAllStepsOK').and.returnValue(true);
            spyOn(wizardStepManager, 'hasStepGivenStatus').and.returnValue(false);

            expect(controller.canBeFreigegeben()).toBe(false);
            // tslint:disable-next-line:no-unbound-method
            expect(wizardStepManager.hasStepGivenStatus)
                .toHaveBeenCalledWith(TSWizardStepName.BETREUUNG, TSWizardStepStatus.OK);
        });
        it('should return false when all steps are true and all Betreuungen are accepted and the Gesuch is ReadOnly',
            () => {
                spyOn(wizardStepManager, 'areAllStepsOK').and.returnValue(true);
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.returnValue(true);
                spyOn(gesuchModelManager, 'isGesuchReadonly').and.returnValue(true);
                expect(controller.canBeFreigegeben()).toBe(false);
            });
        it('should return true when all steps are true and all Betreuungen are accepted and the Gesuch is not ReadOnly',
            () => {
                spyOn(wizardStepManager, 'areAllStepsOK').and.returnValue(true);
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.returnValue(true);
                spyOn(gesuchModelManager, 'isGesuchReadonly').and.returnValue(false);
                spyOn(controller, 'isGesuchInStatus').and.returnValue(true);
                expect(controller.canBeFreigegeben()).toBe(true);
            });
    });
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
            // tslint:disable-next-line:no-unbound-method
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
            spyOn(downloadRS, 'getFreigabequittungAccessTokenGeneratedDokument')
                .and.returnValue($q.resolve(downloadFile));
            spyOn(downloadRS, 'startDownload').and.returnValue();

            const fakeWindow: any = undefined;
            spyOn(downloadRS, 'prepareDownloadWindow').and.returnValue(fakeWindow);

            const gesuch = new TSGesuch();
            gesuch.id = '123';
            spyOn(gesuchModelManager, 'openGesuch').and.returnValue($q.resolve(gesuch));
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);

            controller.confirmationCallback();
            $scope.$apply();

            expect(downloadRS.getFreigabequittungAccessTokenGeneratedDokument).toHaveBeenCalledWith(gesuch.id, true);
            expect(downloadRS.startDownload)
                .toHaveBeenCalledWith(downloadFile.accessToken, downloadFile.filename, false, fakeWindow);
        });
    });
    describe('openFreigabequittungPDF', () => {
        let gesuch: TSGesuch;
        let tsDownloadFile: TSDownloadFile;

        beforeEach(() => {
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            spyOn(gesuchModelManager, 'openGesuch').and.returnValue($q.resolve(gesuch));
            spyOn(downloadRS, 'startDownload').and.returnValue();
            tsDownloadFile = new TSDownloadFile();
            spyOn(downloadRS, 'getFreigabequittungAccessTokenGeneratedDokument')
                .and.returnValue($q.resolve(tsDownloadFile));
            gesuch = new TSGesuch();
            gesuch.id = '123';
            spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
        });
        it('should call the service for Erstgesuch', () => {
            spyOn(gesuchModelManager, 'isGesuch').and.returnValue(true);

            controller.openFreigabequittungPDF(false);
            $scope.$apply();

            // tslint:disable-next-line:no-unbound-method
            expect(downloadRS.getFreigabequittungAccessTokenGeneratedDokument).toHaveBeenCalledWith(gesuch.id, false);
        });
    });
});

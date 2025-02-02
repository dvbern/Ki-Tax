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

import {waitForAsync} from '@angular/core/testing';
import {IQService, IScope, ITimeoutService} from 'angular';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {ApplicationPropertyRS} from '../../../app/core/rest-services/applicationPropertyRS.rest';
import {DemoFeatureRS} from '../../../app/core/service/demoFeatureRS.rest';
import {DownloadRS} from '../../../app/core/service/downloadRS.rest';
import {EwkRS} from '../../../app/core/service/ewkRS.rest';
import {UploadRS} from '../../../app/core/service/uploadRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSCreationAction} from '../../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../../models/enums/TSEingangsart';
import {TSFamilienstatus} from '../../../models/enums/TSFamilienstatus';
import {TSGesuchstellerKardinalitaet} from '../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSUnterhaltsvereinbarungAnswer} from '../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSFamiliensituation} from '../../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../../models/TSFamiliensituationContainer';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSGesuchsteller} from '../../../models/TSGesuchsteller';
import {TSGesuchstellerContainer} from '../../../models/TSGesuchstellerContainer';
import {GESUCH_JS_MODULE} from '../../gesuch.module';
import {IStammdatenStateParams} from '../../gesuch.route';
import {DokumenteRS} from '../../service/dokumenteRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {StammdatenViewController} from './stammdatenView';
import {TSDateRange} from '../../../models/types/TSDateRange';
import * as moment from 'moment/moment';
import ITranslateService = angular.translate.ITranslateService;

describe('stammdatenView', () => {
    let gesuchModelManager: GesuchModelManager;
    let stammdatenViewController: StammdatenViewController;
    let $stateParams: IStammdatenStateParams;
    let $q: IQService;
    let $rootScope: any;
    let $scope: IScope;
    let ewkRS: EwkRS;
    let $timeout: ITimeoutService;
    let einstellungRS: EinstellungRS;
    let applicationPropertyRS: ApplicationPropertyRS;
    let uploadRS: UploadRS;
    let downloadRS: DownloadRS;
    let dokumentRS: DokumenteRS;
    let mandantService: MandantService;
    let demoFeatureRS: DemoFeatureRS;
    let $translateMock: jasmine.SpyObj<ITranslateService>;

    beforeEach(angular.mock.module(GESUCH_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(waitForAsync(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            const wizardStepManager: WizardStepManager =
                $injector.get('WizardStepManager');
            $stateParams = $injector.get('$stateParams');
            $stateParams.gesuchstellerNumber = '1';
            gesuchModelManager.initGesuch(
                TSEingangsart.PAPIER,
                TSCreationAction.CREATE_NEW_FALL,
                undefined
            );
            $q = $injector.get('$q');
            $rootScope = $injector.get('$rootScope');
            ewkRS = $injector.get('EwkRS');
            $scope = $rootScope.$new();
            $timeout = $injector.get('$timeout');
            einstellungRS = $injector.get('EinstellungRS');
            applicationPropertyRS = $injector.get('ApplicationPropertyRS');
            uploadRS = $injector.get('UploadRS');
            downloadRS = $injector.get('DownloadRS');
            dokumentRS = $injector.get('DokumenteRS');
            mandantService = $injector.get('MandantService');
            demoFeatureRS = $injector.get('DemoFeatureRS');
            $translateMock = jasmine.createSpyObj<ITranslateService>(
                'ITranslateService',
                ['instant']
            );

            stammdatenViewController = new StammdatenViewController(
                $stateParams,
                undefined,
                gesuchModelManager,
                undefined,
                undefined,
                wizardStepManager,
                $q,
                $scope,
                $translateMock,
                undefined,
                $rootScope,
                ewkRS,
                $timeout,
                einstellungRS,
                uploadRS,
                downloadRS,
                applicationPropertyRS,
                dokumentRS,
                mandantService,
                demoFeatureRS
            );
            stammdatenViewController.demoFeature2754 = true;
        })
    ));

    describe('disableWohnadresseFor2GS', () => {
        it('should return false for 1GS und Erstgesuch', () => {
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(
                1
            );
            expect(stammdatenViewController.disableWohnadresseFor2GS()).toBe(
                false
            );
        });
        it('should return false for new 2GS und Mutation', () => {
            stammdatenViewController.gesuchstellerNumber = 2;
            gesuchModelManager.setStammdatenToWorkWith(
                new TSGesuchstellerContainer(new TSGesuchsteller())
            );
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;
            stammdatenViewController.model =
                gesuchModelManager.getStammdatenToWorkWith();
            expect(stammdatenViewController.disableWohnadresseFor2GS()).toBe(
                false
            );
        });
        it('should return true for old 2GS und Mutation', () => {
            gesuchModelManager.setGesuchstellerNumber(2);
            const gs2 = new TSGesuchstellerContainer(new TSGesuchsteller());
            gs2.vorgaengerId = '123';
            gesuchModelManager.setStammdatenToWorkWith(gs2);
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;
            expect(stammdatenViewController.disableWohnadresseFor2GS()).toBe(
                true
            );
        });
        it('should return false for 1GS und Erstgesuch', () => {
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(
                1
            );
            expect(stammdatenViewController.disableWohnadresseFor2GS()).toBe(
                false
            );
        });
        it('should return true for 1GS und Mutation', () => {
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(
                1
            );
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;
            expect(stammdatenViewController.disableWohnadresseFor2GS()).toBe(
                true
            );
        });
    });

    describe('getFamilienSituationDisplayValue', () => {
        it('should return gesuchstellerNumber.toString() if conditions are not met', () => {
            stammdatenViewController.gesuchModelManager.isFKJVTexte = false;
            stammdatenViewController.gesuchstellerNumber = 2;
            const result =
                stammdatenViewController.getFamilienSituationDisplayValue();
            expect(result).toEqual('2');
        });
        it('should return gesuchstellerNumber.toString() if conditions are not met', () => {
            stammdatenViewController.gesuchModelManager.isFKJVTexte = true;
            stammdatenViewController.demoFeature2754 = false;
            stammdatenViewController.gesuchstellerNumber = 2;
            const result =
                stammdatenViewController.getFamilienSituationDisplayValue();
            expect(result).toEqual('2');
        });
        it('should return 1 if conditions are met', () => {
            stammdatenViewController.gesuchModelManager.isFKJVTexte = true;
            stammdatenViewController.gesuchstellerNumber = 1;
            const result =
                stammdatenViewController.getFamilienSituationDisplayValue();
            expect(result).toEqual('1');
        });
        it('should return empty string if famsit null', () => {
            stammdatenViewController.gesuchModelManager.isFKJVTexte = true;
            stammdatenViewController.gesuchstellerNumber = 2;
            const result =
                stammdatenViewController.getFamilienSituationDisplayValue();
            expect(result).toEqual('');
        });

        it('should return ANDERER_ELTERNTEIL if KONKUBINAT_KEIN_KIND AND ZU_ZWEIT AND SHORT KONKUBINAT', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            famSitMock.startKonkubinat = moment('2024-01-01');
            famSitMock.geteilteObhut = true;
            famSitMock.gesuchstellerKardinalitaet =
                TSGesuchstellerKardinalitaet.ZU_ZWEIT;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            spyOn(famSitMock, 'konkubinatGetXYearsInPeriod').and.returnValue(
                true
            );
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('ANDERER_ELTERNTEIL')
            );
        });

        it('should return ANDERER_ELTERNTEIL if KONKUBINAT_KEIN_KIND,NEIN_UNTERHALTSVEREINBARUNG AND SHORT KONKUBINAT', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            famSitMock.startKonkubinat = moment('2024-01-01');
            famSitMock.geteilteObhut = false;
            famSitMock.unterhaltsvereinbarung =
                TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('ANDERER_ELTERNTEIL')
            );
        });

        it(
            'should return GS2_KONKUBINAT_KEIN_KIND if KONKUBINAT_KEIN_KIND, JA_UNTERHALTSVEREINBARUNG AND ' +
                'konkubiat gets long during periode',
            () => {
                const famSitMock = initTest();
                famSitMock.familienstatus =
                    TSFamilienstatus.KONKUBINAT_KEIN_KIND;
                famSitMock.startKonkubinat = moment('2023-01-01');
                famSitMock.geteilteObhut = false;
                famSitMock.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                    famSitMock;
                stammdatenViewController.getFamilienSituationDisplayValue();
                expect($translateMock.instant).toHaveBeenCalled();
                expect($translateMock.instant).toHaveBeenCalledWith(
                    jasmine.stringMatching('GS2_KONKUBINAT_KEIN_KIND')
                );
            }
        );

        it(
            'should return GS2_KONKUBINAT_KEIN_KIND if KONKUBINAT_KEIN_KIND, UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH AND ' +
                'konkubiat gets long during periode',
            () => {
                const famSitMock = initTest();
                famSitMock.familienstatus =
                    TSFamilienstatus.KONKUBINAT_KEIN_KIND;
                famSitMock.startKonkubinat = moment('2023-01-01');
                famSitMock.geteilteObhut = false;
                famSitMock.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                    famSitMock;
                stammdatenViewController.getFamilienSituationDisplayValue();
                expect($translateMock.instant).toHaveBeenCalled();
                expect($translateMock.instant).toHaveBeenCalledWith(
                    jasmine.stringMatching('GS2_KONKUBINAT_KEIN_KIND')
                );
            }
        );

        it(
            'should return GS2_KONKUBINAT_KEIN_KIND if KONKUBINAT_KEIN_KIND, GETEILTE_OBHUT AND ALLEINE ' +
                'AND konkubiat gets long during periode',
            () => {
                const famSitMock = initTest();
                famSitMock.familienstatus =
                    TSFamilienstatus.KONKUBINAT_KEIN_KIND;
                famSitMock.startKonkubinat = moment('2023-01-01');
                famSitMock.geteilteObhut = true;
                famSitMock.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ALLEINE;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                    famSitMock;
                stammdatenViewController.getFamilienSituationDisplayValue();
                expect($translateMock.instant).toHaveBeenCalled();
                expect($translateMock.instant).toHaveBeenCalledWith(
                    jasmine.stringMatching('GS2_KONKUBINAT_KEIN_KIND')
                );
            }
        );

        it('should return GS2_KONKUBINAT_KEIN_KIND if KONKUBINAT_KEIN_KIND, AND long konkubiat', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.KONKUBINAT_KEIN_KIND;
            famSitMock.startKonkubinat = moment('2022-01-01');
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_KONKUBINAT_KEIN_KIND')
            );
        });

        it(
            'should return ANDERER_ELTERNTEIL if KONKUBINAT_KEIN_KIND, GETEILTE_OBHUT AND ZU_ZWEIT AND  ' +
                'konkubiat gets long during periode',
            () => {
                const famSitMock = initTest();
                famSitMock.familienstatus =
                    TSFamilienstatus.KONKUBINAT_KEIN_KIND;
                famSitMock.geteilteObhut = true;
                famSitMock.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ZU_ZWEIT;
                famSitMock.startKonkubinat = moment('2023-01-01');
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                    famSitMock;
                stammdatenViewController.getFamilienSituationDisplayValue();
                expect($translateMock.instant).toHaveBeenCalled();
                expect($translateMock.instant).toHaveBeenCalledWith(
                    jasmine.stringMatching('ANDERER_ELTERNTEIL')
                );
            }
        );

        it('should return ANDERER_ELTERNTEIL if ALLEINERZIEHEND and ZU_ZWEIT', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.ALLEINERZIEHEND;
            famSitMock.geteilteObhut = true;
            famSitMock.gesuchstellerKardinalitaet =
                TSGesuchstellerKardinalitaet.ZU_ZWEIT;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('ANDERER_ELTERNTEIL')
            );
        });

        it('should return ANDERER_ELTERNTEIL if ALLEINERZIEHEND and NEIN_UNTERHALTSVEREINBARUNG', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.ALLEINERZIEHEND;
            famSitMock.geteilteObhut = false;
            famSitMock.unterhaltsvereinbarung =
                TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('ANDERER_ELTERNTEIL')
            );
        });

        it('should return GS2_VERHEIRATET if VERHEIRATET', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.VERHEIRATET;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_VERHEIRATET')
            );
        });

        it('should return GS2_KONKUBINAT if KONKUBINAT', () => {
            const famSitMock = initTest();
            famSitMock.familienstatus = TSFamilienstatus.KONKUBINAT;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMock;
            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_KONKUBINAT')
            );
        });

        it('should return GS2_VERHEIRATET if VERHEIRATET and Mutation to Alleine', () => {
            const famSitMockMutation = initTest();
            famSitMockMutation.familienstatus =
                TSFamilienstatus.ALLEINERZIEHEND;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMockMutation;
            famSitMockMutation.geteilteObhut = true;
            famSitMockMutation.gesuchstellerKardinalitaet =
                TSGesuchstellerKardinalitaet.ZU_ZWEIT;
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;

            const famSitMockErstantrag = new TSFamiliensituation();
            famSitMockErstantrag.familienstatus = TSFamilienstatus.VERHEIRATET;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationErstgesuch =
                famSitMockErstantrag;

            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_VERHEIRATET')
            );
        });

        it('should return GS2_KONKUBINAT if KONKUBINAT and Muataion to Alleine', () => {
            const famSitMockMutation = initTest();
            famSitMockMutation.familienstatus =
                TSFamilienstatus.ALLEINERZIEHEND;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMockMutation;
            famSitMockMutation.geteilteObhut = true;
            famSitMockMutation.gesuchstellerKardinalitaet =
                TSGesuchstellerKardinalitaet.ZU_ZWEIT;
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;

            const famSitMockErstantrag = new TSFamiliensituation();
            famSitMockErstantrag.familienstatus = TSFamilienstatus.KONKUBINAT;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationErstgesuch =
                famSitMockErstantrag;

            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_KONKUBINAT')
            );
        });

        it('should return GS2_VERHEIRATET if KONKUBINAT and Muataion to VERHEIRATET mit gleichem GS2', () => {
            const famSitMockMutation = initTest();
            famSitMockMutation.familienstatus = TSFamilienstatus.VERHEIRATET;
            famSitMockMutation.partnerIdentischMitVorgesuch = true;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMockMutation;
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;

            const famSitMockErstantrag = new TSFamiliensituation();
            famSitMockErstantrag.familienstatus = TSFamilienstatus.KONKUBINAT;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationErstgesuch =
                famSitMockErstantrag;

            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_VERHEIRATET')
            );
        });

        it('should return GS2_VERHEIRATET if VERHEIRATET and Muataion to KONKUBINAT mit ungleichem GS2', () => {
            const famSitMockMutation = initTest();
            famSitMockMutation.familienstatus = TSFamilienstatus.KONKUBINAT;
            famSitMockMutation.partnerIdentischMitVorgesuch = false;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA =
                famSitMockMutation;
            gesuchModelManager.getGesuch().typ = TSAntragTyp.MUTATION;

            const famSitMockErstantrag = new TSFamiliensituation();
            famSitMockErstantrag.familienstatus = TSFamilienstatus.VERHEIRATET;
            gesuchModelManager.getGesuch().familiensituationContainer.familiensituationErstgesuch =
                famSitMockErstantrag;

            stammdatenViewController.getFamilienSituationDisplayValue();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('GS2_VERHEIRATET')
            );
        });

        function initTest(): TSFamiliensituation {
            stammdatenViewController.gesuchModelManager.isFKJVTexte = true;
            stammdatenViewController.gesuchstellerNumber = 2;
            const famSitMock = new TSFamiliensituation();
            famSitMock.minDauerKonkubinat = 2;
            gesuchModelManager.getGesuch().gesuchsperiode =
                new TSGesuchsperiode();
            gesuchModelManager.getGesuch().gesuchsperiode.gueltigkeit =
                new TSDateRange(moment('2024-08-01'), moment('2025-07-31'));
            gesuchModelManager.getGesuch().familiensituationContainer =
                new TSFamiliensituationContainer();
            return famSitMock;
        }
    });
});

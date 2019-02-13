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
import {IQService, IScope} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import WizardStepManager from '../../../../gesuch/service/wizardStepManager';
import {ngServicesMock} from '../../../../hybridTools/ngServicesMocks';
import {TSAntragTyp} from '../../../../models/enums/TSAntragTyp';
import {TSEingangsart} from '../../../../models/enums/TSEingangsart';
import {TSWizardStepName} from '../../../../models/enums/TSWizardStepName';
import TSDossier from '../../../../models/TSDossier';
import TSFall from '../../../../models/TSFall';
import TSFamiliensituation from '../../../../models/TSFamiliensituation';
import TSFamiliensituationContainer from '../../../../models/TSFamiliensituationContainer';
import TSGesuch from '../../../../models/TSGesuch';
import TSGesuchsperiode from '../../../../models/TSGesuchsperiode';
import TestDataUtil from '../../../../utils/TestDataUtil.spec';
import {CORE_JS_MODULE} from '../../core.angularjs.module';
import {NavigatorController} from './dv-navigation';

// tslint:disable:no-duplicate-string no-big-function max-line-length no-identical-functions
describe('dvNavigation', () => {

    let navController: NavigatorController;
    let wizardStepManager: WizardStepManager;
    let $state: StateService;
    let $q: IQService;
    let $rootScope: IScope;
    let gesuchModelManager: GesuchModelManager;
    let authServiceRS: AuthServiceRS;
    let isStatusVerfuegen: boolean;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.inject($injector => {
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($injector.get('$httpBackend'));
        $q = $injector.get('$q');
        const $timeout = $injector.get('$timeout');
        $rootScope = $injector.get('$rootScope');
        wizardStepManager = $injector.get('WizardStepManager');
        $state = $injector.get('$state');
        gesuchModelManager = $injector.get('GesuchModelManager');
        authServiceRS = $injector.get('AuthServiceRS');
        navController = new NavigatorController(wizardStepManager, $state, gesuchModelManager,
            $injector.get('$translate'), $injector.get('ErrorService'), $q, $timeout);
        navController.dvSave = () => {
            return $q.when({});
        };
        isStatusVerfuegen = true;
        spyOn(gesuchModelManager, 'isGesuchReadonly').and.callFake(() => {
            return isStatusVerfuegen;
        });
    }));

    describe('getNextButtonName', () => {
        it('returns WEITER if dvSave exists', () => {
            isStatusVerfuegen = false;
            expect(navController.getNextButtonName()).toEqual('Speichern und Weiter');
        });
        it('returns WEITER_ONLY if status is VERFUEGEN', () => {
            expect(navController.getNextButtonName()).toEqual('Weiter');
        });
        it('returns WEITER_ONLY if dvSave does not exist', () => {
            isStatusVerfuegen = false;
            navController.dvSave = undefined;
            expect(navController.getNextButtonName()).toEqual('Weiter');
        });
    });

    describe('getNextButtonName', () => {
        it('returns ZURUECK if dvSave exists', () => {
            isStatusVerfuegen = false;
            expect(navController.getPreviousButtonName()).toEqual('Speichern und Zurück');
        });
        it('returns ZURUECK_ONLY if status is VERFUEGEN', () => {
            expect(navController.getPreviousButtonName()).toEqual('Zurück');
        });
        it('returns ZURUECK_ONLY if dvSave does not exist', () => {
            isStatusVerfuegen = false;
            navController.dvSave = undefined;
            expect(navController.getPreviousButtonName()).toEqual('Zurück');
        });
    });
    describe('nextStep', () => {
        it('moves to gesuch.familiensituation when coming from GESUCH_ERSTELLEN', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCH_ERSTELLEN);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.FAMILIENSITUATION);
            mockGesuch();
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.familiensituation', {gesuchId: '123'});
        });
        it('moves to gesuch.stammdaten when coming from FAMILIENSITUATION', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FAMILIENSITUATION);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            mockGesuch();
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '1', gesuchId: '123'});
        });
        it('moves to gesuch.stammdaten 2 when coming from GESUCHSTELLER 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(true);
            mockGesuch();
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '2', gesuchId: '123'});
        });
        it('moves to gesuch.betreuungen when coming from GESUCHSTELLER and institution', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.BETREUUNG);
            mockGesuch();
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: '123'});
        });
        it('moves to gesuch.umzug when coming from GESUCHSTELLER', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.UMZUG);
            mockGesuch();
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
            spyOn(authServiceRS, 'isRole').and.returnValue(false);
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.umzug', {gesuchId: '123'});
        });
        it('moves to gesuch.kinder when coming from UMZUG substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.UMZUG);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.KINDER);
            mockGesuch();
            navController.dvSubStep = 1;
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.kinder', {gesuchId: '123'});
        });
        it('moves to gesuch.betreuungen when coming from KINDER substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.KINDER);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.BETREUUNG);
            mockGesuch();
            navController.dvSubStep = 1;
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: '123'});
        });
        it('moves to gesuch.kinder when coming from KINDER substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.KINDER);
            navController.dvSubStep = 2;
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.kinder', {gesuchId: ''});
        });
        it('moves to gesuch.verfuegen when coming from BETREUUNG substep 1 and Institution', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.VERFUEGEN);
            mockGesuch();
            navController.dvSubStep = 1;
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.verfuegen', {gesuchId: '123'});
        });
        it('moves to gesuch.erwerbsPensen when coming from BETREUUNG substep 1 and no Institution', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            mockGesuch();
            navController.dvSubStep = 1;
            spyOn(authServiceRS, 'isRole').and.returnValue(false);
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.erwerbsPensen', {gesuchId: '123'});
        });
        it('moves to gesuch.dokumente when coming from ERWERBSPENSUM substep 1 and disabled', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.DOKUMENTE);
            mockGesuch();
            spyOn(wizardStepManager, 'isNextStepBesucht').and.returnValue(true);
            spyOn(wizardStepManager, 'isNextStepEnabled').and.returnValue(false);
            spyOn(wizardStepManager, 'updateCurrentWizardStepStatus').and.returnValue($q.when({}));
            navController.dvSubStep = 1;
            callNextStep();
            $rootScope.$apply();
            expect($state.go).toHaveBeenCalledWith('gesuch.dokumente', {gesuchId: '123'});
        });
        it('moves to gesuch.finanzielleSituation when coming from ERWERBSPENSUM substep 1 and 2GS not required', () => {
            moveComingFromErwerbspensum(false);
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationStart', {
                gesuchId: '123',
            });
        });
        it('moves to gesuch.finanzielleSituationStart when coming from ERWERBSPENSUM substep 1 and 2GS and FinSit not required',
            () => {
                moveComingFromErwerbspensum(true);
                expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationStart', {
                    gesuchId: '123',
                });
            });
        it('moves to gesuch.erwerbsPensen when coming from ERWERBSPENSUM substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            navController.dvSubStep = 2;
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.erwerbsPensen', {gesuchId: ''});
        });
        it('moves to gesuch.finanzielleSituation when coming from FINANZIELLE_SITUATION substep 1 with GS1 and 2GS required',
            () => {
                spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
                spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
                spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(true);
                navController.dvSubStep = 2;
                gesuchModelManager.setGesuch(mockGesuch());
                callNextStep();
                expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituation', {
                    gesuchstellerNumber: '2',
                    gesuchId: '123',
                });
            });
        it('moves to gesuch.finanzielleSituationResultate when coming from FINANZIELLE_SITUATION substep 1 with GS1 and 2GS NOT required',
            () => {
                spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
                spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
                spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
                navController.dvSubStep = 2;
                gesuchModelManager.setGesuch(mockGesuch());
                callNextStep();
                expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationResultate', {gesuchId: '123'});
            });
        it('moves to gesuch.finanzielleSituation when coming from FINANZIELLE_SITUATION substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            navController.dvSubStep = 1;
            gesuchModelManager.setGesuch(mockGesuch());
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue('1');
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituation', {
                gesuchstellerNumber: '1',
                gesuchId: '123',
            });
        });
        it('moves to gesuch.einkommensverschlechterungInfo when coming from FINANZIELLE_SITUATION substep 3', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            mockGesuch();
            navController.dvSubStep = 3;
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.einkommensverschlechterungInfo', {gesuchId: '123'});
        });
        it('moves to gesuch.einkommensverschlechterung when coming from EINKOMMENSVERSCHLECHTERUNG substep 1 with EV and 2GS required',
            () => {
                const gesuch = mockGesuch();
                spyOn(wizardStepManager,
                    'getCurrentStepName').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
                spyOn(gesuch,
                    'extractEinkommensverschlechterungInfo').and.returnValue({einkommensverschlechterung: true});
                spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(true);
                navController.dvSubStep = 1;
                callNextStep();
                expect($state.go).toHaveBeenCalledWith('gesuch.einkommensverschlechterung', {
                    gesuchstellerNumber: '1',
                    basisjahrPlus: '2',
                    gesuchId: '123',
                });
            });
        it('moves to gesuch.einkommensverschlechterung when coming from EINKOMMENSVERSCHLECHTERUNG substep 1 with EV and 2GS NOT required',
            () => {
                const gesuch = mockGesuch();
                spyOn(gesuch,
                    'extractEinkommensverschlechterungInfo').and.returnValue({einkommensverschlechterung: true});
                spyOn(wizardStepManager,
                    'getCurrentStepName').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
                spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
                spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue('1');
                navController.dvSubStep = 1;
                callNextStep();
                expect($state.go).toHaveBeenCalledWith('gesuch.einkommensverschlechterung', {
                    gesuchstellerNumber: '1',
                    basisjahrPlus: '2',
                    gesuchId: '123',
                });
            });
        it('moves to gesuch.dokumente when coming from EINKOMMENSVERSCHLECHTERUNG substep 1 without EV', () => {
            mockGesuch();
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.DOKUMENTE);
            spyOn(wizardStepManager, 'updateCurrentWizardStepStatus').and.returnValue($q.when({}));
            navController.dvSubStep = 1;
            callNextStep();
            $rootScope.$apply();
            expect($state.go).toHaveBeenCalledWith('gesuch.dokumente', {gesuchId: '123'});
        });
        it('moves to gesuch.verfuegen when coming from DOKUMENTE', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.DOKUMENTE);
            spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.VERFUEGEN);
            mockGesuch();
            callNextStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.verfuegen', {gesuchId: '123'});
        });
    });

    describe('previousStep', () => {
        it('moves to gesuch.fallcreation when coming from FAMILIENSITUATION', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FAMILIENSITUATION);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.GESUCH_ERSTELLEN);
            const dossier = new TSDossier();
            dossier.gemeinde = TestDataUtil.createGemeindeOstermundigen();
            dossier.id = '123';
            spyOn(gesuchModelManager, 'getDossier').and.returnValue(dossier);
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.fallcreation',
                {
                    eingangsart: 'ONLINE',
                    gesuchId: '123',
                    gesuchsperiodeId: '123',
                    dossierId: dossier.id,
                    gemeindeId: dossier.gemeinde.id,
                });
        });
        it('moves to gesuch.stammdaten when coming from GESUCHSTELLER from 2GS', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(2);
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '1', gesuchId: ''});
        });
        it('moves to gesuch.familiensituation when coming from GESUCHSTELLER from 1GS', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.FAMILIENSITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.familiensituation', {gesuchId: '123'});
        });
        it('moves to gesuch.stammdaten when coming from UMZUG', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.UMZUG);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            mockGesuch();
            navController.dvSubStep = 1;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '1', gesuchId: '123'});
        });
        it('moves to gesuch.umzug when coming from KINDER substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.KINDER);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.UMZUG);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.umzug', {gesuchId: '123'});
        });
        it('moves to gesuch.umzug when coming from KINDER substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.KINDER);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.UMZUG);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(2);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.umzug', {gesuchId: '123'});
        });
        it('moves to gesuch.kinder when coming from KINDER substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.KINDER);
            navController.dvSubStep = 2;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.kinder', {gesuchId: ''});
        });
        it('moves to gesuch.stammdaten when coming from BETREUUNG substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '1', gesuchId: '123'});
        });
        it('moves to gesuch.stammdaten when coming from BETREUUNG substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.GESUCHSTELLER);
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.stammdaten', {gesuchstellerNumber: '1', gesuchId: '123'});
        });
        it('moves to gesuch.kinder when coming from BETREUUNG substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.KINDER);
            spyOn(authServiceRS, 'isRole').and.returnValue(false);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.kinder', {gesuchId: '123'});
        });
        it('moves to gesuch.betreuungen when coming from BETREUUNG substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.BETREUUNG);
            navController.dvSubStep = 2;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: ''});
        });
        it('moves to gesuch.betreuungen when coming from ERWERBSPENSUM substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.BETREUUNG);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: '123'});
        });
        it('moves to gesuch.erwerbsPensen when coming from ERWERBSPENSUM substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            navController.dvSubStep = 2;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.erwerbsPensen', {gesuchId: ''});
        });
        it('moves to gesuch.finanzielleSituation when coming from FINANZIELLE_SITUATION substep 1 and GS2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(2);
            navController.dvSubStep = 2;
            spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(new TSGesuchsperiode());
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituation', {
                gesuchstellerNumber: '1',
                gesuchId: '',
            });
        });
        it('moves to gesuch.finanzielleSituationStart when coming from FINANZIELLE_SITUATION substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(true);
            navController.dvSubStep = 2;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationStart', {gesuchId: ''});
        });
        it('moves to gesuch.kinder when coming from FINANZIELLE_SITUATION substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationStart', {gesuchId: '123'});
        });
        it('moves to gesuch.erwerbsPensen when coming from FINANZIELLE_SITUATION substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.erwerbsPensen', {gesuchId: '123'});
        });
        it('moves to gesuch.finanzielleSituation when coming from FINANZIELLE_SITUATION substep 3', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(1);
            navController.dvSubStep = 3;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituation', {
                gesuchstellerNumber: '1',
                gesuchId: '',
            });
        });
        it('moves to gesuch.finanzielleSituation when coming from FINANZIELLE_SITUATION substep 3', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            spyOn(gesuchModelManager, 'getGesuchstellerNumber').and.returnValue(2);
            navController.dvSubStep = 3;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituation', {
                gesuchstellerNumber: '2',
                gesuchId: '',
            });
        });
        it('moves to gesuch.finanzielleSituation when coming from EINKOMMENSVERSCHLECHTERUNG substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.finanzielleSituationStart', {
                gesuchId: '123',
            });
        });
        it('moves to gesuch.einkommensverschlechterungInfo when coming from EINKOMMENSVERSCHLECHTERUNG substep 3',
            () => {
                spyOn(wizardStepManager,
                    'getCurrentStepName').and.returnValue(TSWizardStepName.EINKOMMENSVERSCHLECHTERUNG);
                navController.dvSubStep = 3;
                callPreviousStep();
                expect($state.go).toHaveBeenCalledWith('gesuch.einkommensverschlechterungInfo', {gesuchId: ''});
            });
        it('moves to gesuch.betreuungen when coming from VERFUEGEN substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.VERFUEGEN);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.BETREUUNG);
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.betreuungen', {gesuchId: '123'});
        });
        it('moves to gesuch.dokumente when coming from VERFUEGEN substep 1', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.VERFUEGEN);
            spyOn(wizardStepManager, 'getPreviousStep').and.returnValue(TSWizardStepName.DOKUMENTE);
            spyOn(wizardStepManager, 'updateCurrentWizardStepStatus').and.returnValue($q.when({}));
            spyOn(authServiceRS, 'isRole').and.returnValue(false);
            navController.dvSubStep = 1;
            mockGesuch();
            callPreviousStep();
            $rootScope.$apply();
            expect($state.go).toHaveBeenCalledWith('gesuch.dokumente', {gesuchId: '123'});
        });
        it('moves to gesuch.verfuegen when coming from VERFUEGEN substep 2', () => {
            spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.VERFUEGEN);
            navController.dvSubStep = 2;
            callPreviousStep();
            expect($state.go).toHaveBeenCalledWith('gesuch.verfuegen', {gesuchId: ''});
        });
    });

    function mockGesuch(): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.typ = TSAntragTyp.ERSTGESUCH;
        gesuch.eingangsart = TSEingangsart.ONLINE;
        gesuch.id = '123';
        gesuch.dossier = new TSDossier();
        gesuch.dossier.id = '123';
        gesuch.dossier.fall = new TSFall();
        gesuch.dossier.fall.id = '123';
        gesuch.gesuchsperiode = new TSGesuchsperiode();
        gesuch.gesuchsperiode.id = '123';
        gesuch.familiensituationContainer = new TSFamiliensituationContainer();
        gesuch.familiensituationContainer.familiensituationJA = new TSFamiliensituation();
        gesuch.familiensituationContainer.familiensituationJA.sozialhilfeBezueger = false;
        gesuch.familiensituationContainer.familiensituationJA.verguenstigungGewuenscht = true;
        spyOn(gesuch.gesuchsperiode, 'hasTagesschulenAnmeldung').and.returnValue(true);
        spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
        spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(gesuch.gesuchsperiode);
        return gesuch;
    }

    function callPreviousStep(): void {
        spyOn($state, 'go').and.returnValue({}); // do nothing
        navController.previousStep();
        $rootScope.$apply();
    }

    function callNextStep(): void {
        spyOn($state, 'go').and.returnValue({}); // do nothing
        navController.nextStep();
        $rootScope.$apply();
    }

    function moveComingFromErwerbspensum(famSitRequired: boolean): void {
        spyOn(wizardStepManager, 'getCurrentStepName').and.returnValue(TSWizardStepName.ERWERBSPENSUM);
        spyOn(wizardStepManager, 'getNextStep').and.returnValue(TSWizardStepName.FINANZIELLE_SITUATION);
        spyOn(wizardStepManager, 'isNextStepBesucht').and.returnValue(true);
        spyOn(wizardStepManager, 'isNextStepEnabled').and.returnValue(true);
        spyOn(wizardStepManager, 'updateCurrentWizardStepStatus').and.returnValue($q.when({}));
        navController.dvSubStep = 1;
        const gesuch = mockGesuch();
        gesuch.familiensituationContainer.familiensituationJA.verguenstigungGewuenscht = famSitRequired;
        spyOn(gesuchModelManager, 'areThereOnlySchulamtAngebote').and.returnValue(false);
        spyOn(gesuchModelManager, 'isGesuchsteller2Required').and.returnValue(false);
        callNextStep();
        $rootScope.$apply();
    }
});

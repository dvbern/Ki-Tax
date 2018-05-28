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

import AuthServiceRS from '../../authentication/service/AuthServiceRS.rest';
import {EbeguWebCore} from '../../core/core.module';
import AntragStatusHistoryRS from '../../core/service/antragStatusHistoryRS.rest';
import BetreuungRS from '../../core/service/betreuungRS.rest';
import KindRS from '../../core/service/kindRS.rest';
import VerfuegungRS from '../../core/service/verfuegungRS.rest';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../models/enums/TSAntragTyp';
import {TSBetreuungsangebotTyp} from '../../models/enums/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../models/enums/TSBetreuungsstatus';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSGesuchBetreuungenStatus} from '../../models/enums/TSGesuchBetreuungenStatus';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import TSBetreuung from '../../models/TSBetreuung';
import TSGesuch from '../../models/TSGesuch';
import TSInstitutionStammdaten from '../../models/TSInstitutionStammdaten';
import TSKind from '../../models/TSKind';
import TSKindContainer from '../../models/TSKindContainer';
import TSUser from '../../models/TSUser';
import TSVerfuegung from '../../models/TSVerfuegung';
import DateUtil from '../../utils/DateUtil';
import TestDataUtil from '../../utils/TestDataUtil';
import FallRS from './fallRS.rest';
import GesuchModelManager from './gesuchModelManager';
import GesuchRS from './gesuchRS.rest';
import WizardStepManager from './wizardStepManager';

describe('gesuchModelManager', function () {

    let gesuchModelManager: GesuchModelManager;
    let betreuungRS: BetreuungRS;
    let fallRS: FallRS;
    let gesuchRS: GesuchRS;
    let kindRS: KindRS;
    let scope: angular.IScope;
    let $httpBackend: angular.IHttpBackendService;
    let $q: angular.IQService;
    let authServiceRS: AuthServiceRS;
    let wizardStepManager: WizardStepManager;
    let verfuegungRS: VerfuegungRS;
    let antragStatusHistoryRS: AntragStatusHistoryRS;

    beforeEach(angular.mock.module(EbeguWebCore.name));

    beforeEach(angular.mock.inject(function ($injector: angular.auto.IInjectorService) {
        gesuchModelManager = $injector.get('GesuchModelManager');
        $httpBackend = $injector.get('$httpBackend');
        betreuungRS = $injector.get('BetreuungRS');
        fallRS = $injector.get('FallRS');
        gesuchRS = $injector.get('GesuchRS');
        kindRS = $injector.get('KindRS');
        scope = $injector.get('$rootScope');
        $q = $injector.get('$q');
        authServiceRS = $injector.get('AuthServiceRS');
        wizardStepManager = $injector.get('WizardStepManager');
        verfuegungRS = $injector.get('VerfuegungRS');
        antragStatusHistoryRS = $injector.get('AntragStatusHistoryRS');
    }));

    describe('API Usage', function () {
        describe('removeBetreuungFromKind', () => {
            it('should remove the current Betreuung from the list of the current Kind', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                createKindContainer();
                createBetreuung();
                expect(gesuchModelManager.getKindToWorkWith().betreuungen).toBeDefined();
                expect(gesuchModelManager.getKindToWorkWith().betreuungen.length).toBe(1);
                gesuchModelManager.removeBetreuungFromKind();
                expect(gesuchModelManager.getKindToWorkWith().betreuungen).toBeDefined();
                expect(gesuchModelManager.getKindToWorkWith().betreuungen.length).toBe(0);
            });
        });
        describe('saveBetreuung', () => {
            it('updates a betreuung', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                createKindContainer();
                let betreuung: TSBetreuung = createBetreuung();
                gesuchModelManager.getKindToWorkWith().id = '2afc9d9a-957e-4550-9a22-97624a000feb';

                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                let kindToWorkWith: TSKindContainer = gesuchModelManager.getKindToWorkWith();
                kindToWorkWith.nextNumberBetreuung = 5;
                spyOn(kindRS, 'findKind').and.returnValue($q.when(kindToWorkWith));
                spyOn(betreuungRS, 'saveBetreuung').and.returnValue($q.when(betreuung));
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when({}));
                spyOn(gesuchRS, 'getGesuchBetreuungenStatus').and.returnValue($q.when(TSGesuchBetreuungenStatus.ALLE_BESTAETIGT));

                gesuchModelManager.saveBetreuung(gesuchModelManager.getKindToWorkWith().betreuungen[0], TSBetreuungsstatus.WARTEN, false);
                scope.$apply();

                expect(betreuungRS.saveBetreuung).toHaveBeenCalledWith(gesuchModelManager.getBetreuungToWorkWith(), '2afc9d9a-957e-4550-9a22-97624a000feb', undefined, false);
                expect(kindRS.findKind).toHaveBeenCalledWith('2afc9d9a-957e-4550-9a22-97624a000feb');
                expect(gesuchModelManager.getKindToWorkWith().nextNumberBetreuung).toEqual(5);
                expect(gesuchModelManager.getGesuch().gesuchBetreuungenStatus).toEqual(TSGesuchBetreuungenStatus.ALLE_BESTAETIGT);
            });
        });
        describe('saveGesuchAndFall', () => {
            it('creates a Fall with a linked Gesuch', () => {
                spyOn(fallRS, 'createFall').and.returnValue($q.when({}));
                let gesuch: TSGesuch = new TSGesuch();
                gesuch.id = '123123';
                spyOn(gesuchRS, 'createGesuch').and.returnValue($q.when({data: gesuch}));
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when({}));
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                gesuchModelManager.saveGesuchAndFall();

                scope.$apply();
                expect(fallRS.createFall).toHaveBeenCalled();
                expect(gesuchRS.createGesuch).toHaveBeenCalled();
            });
            it('only updates the Gesuch because it already exists', () => {
                spyOn(gesuchRS, 'updateGesuch').and.returnValue($q.when({}));
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                gesuchModelManager.getGesuch().timestampErstellt = DateUtil.today();
                gesuchModelManager.saveGesuchAndFall();

                scope.$apply();
                expect(gesuchRS.updateGesuch).toHaveBeenCalled();
            });
        });
        describe('initGesuch', () => {
            beforeEach(() => {
                expect(gesuchModelManager.getGesuch()).toBeUndefined();
            });
            it('links the fall with the undefined user', () => {
                spyOn(authServiceRS, 'getPrincipal').and.returnValue(undefined);

                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);

                expect(gesuchModelManager.getGesuch()).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall.verantwortlicher).toBe(undefined);
            });
            it('links the fall with the current user', () => {
                let currentUser: TSUser = new TSUser('Test', 'User', 'username');
                spyOn(authServiceRS, 'getPrincipal').and.returnValue(currentUser);
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
                spyOn(fallRS, 'setVerantwortlicherJA').and.returnValue($q.when({}));
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);

                scope.$apply();
                expect(gesuchModelManager.getGesuch()).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall.verantwortlicher).toBe(currentUser);
            });
            it('does not link the fall with the current user because is not the required role', () => {
                let currentUser: TSUser = new TSUser('Test', 'User', 'username');
                spyOn(authServiceRS, 'getPrincipal').and.returnValue(currentUser);
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(false);
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);

                expect(gesuchModelManager.getGesuch()).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall).toBeDefined();
                expect(gesuchModelManager.getGesuch().fall.verantwortlicher).toBeUndefined();
            });
            it('does not force to create a new fall and gesuch', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                expect(gesuchModelManager.getGesuch()).toBeDefined();
            });
            it('does force to create a new fall and gesuch', () => {
                gesuchModelManager.initGesuch(true, TSEingangsart.PAPIER);
                expect(gesuchModelManager.getGesuch()).toBeDefined();
            });
            it('forces to create a new gesuch and fall even though one already exists', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                let oldGesuch: TSGesuch = gesuchModelManager.getGesuch();
                expect(gesuchModelManager.getGesuch()).toBeDefined();

                gesuchModelManager.initGesuch(true, TSEingangsart.PAPIER);
                expect(gesuchModelManager.getGesuch()).toBeDefined();
                expect(oldGesuch).not.toBe(gesuchModelManager.getGesuch());
            });
            it('does not force to create a new gesuch and fall and the old ones will remain', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                let oldGesuch: TSGesuch = gesuchModelManager.getGesuch();
                expect(gesuchModelManager.getGesuch()).toBeDefined();

                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                expect(gesuchModelManager.getGesuch()).toBeDefined();
                expect(oldGesuch).toBe(gesuchModelManager.getGesuch());
            });
        });
        describe('setUserAsFallVerantwortlicher', () => {
            it('puts the given user as the verantwortlicher for the fall', () => {
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                spyOn(authServiceRS, 'getPrincipal').and.returnValue(undefined);
                spyOn(fallRS, 'setVerantwortlicherJA').and.returnValue($q.when({}));
                let user: TSUser = new TSUser('Emiliano', 'Camacho');
                gesuchModelManager.setUserAsFallVerantwortlicher(user);
                scope.$apply();
                expect(gesuchModelManager.getGesuch().fall.verantwortlicher).toBe(user);
            });
        });
        describe('exist at least one Betreuung among all kinder', function () {
            it('should return false for empty list', function () {
                let gesuch: TSGesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(gesuchModelManager.getGesuch().isThereAnyBetreuung()).toBe(false);
            });
            it('should return false for a list with Kinder but no Betreuung', function () {
                let kind: TSKindContainer = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = false;
                let gesuch: TSGesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([kind]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(gesuchModelManager.getGesuch().isThereAnyBetreuung()).toBe(false);
            });
            it('should return true for a list with Kinder needing Betreuung', function () {
                let kind: TSKindContainer = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = true;
                let betreuung: TSBetreuung = new TSBetreuung();
                kind.betreuungen = [betreuung];
                let gesuch: TSGesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([kind]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(gesuchModelManager.getGesuch().isThereAnyBetreuung()).toBe(true);
            });
        });

        describe('exist kinder with betreuung needed', function () {
            it('should return false for empty list', function () {
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([]);
                expect(gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()).toBe(false);
            });
            it('should return false for a list with no Kind needing Betreuung', function () {
                let kind: TSKindContainer = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = false;
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([kind]);
                expect(gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()).toBe(false);
            });
            it('should return true for a list with Kinder needing Betreuung', function () {
                let kind: TSKindContainer = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.timestampErstellt = DateUtil.today();
                kind.kindJA.familienErgaenzendeBetreuung = true;
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([kind]);
                expect(gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()).toBe(true);
            });
        });
        describe('saveGesuchStatus', function () {
            it('should update the status of the Gesuch im Server und Client', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                spyOn(gesuchRS, 'updateGesuchStatus').and.returnValue($q.when({}));
                spyOn(antragStatusHistoryRS, 'loadLastStatusChange').and.returnValue($q.when({}));

                gesuchModelManager.saveGesuchStatus(TSAntragStatus.ERSTE_MAHNUNG);

                scope.$apply();
                expect(gesuchModelManager.getGesuch().status).toEqual(TSAntragStatus.ERSTE_MAHNUNG);
            });
        });
        describe('saveVerfuegung', function () {
            it('should save the current Verfuegung und set the status of the Betreuung to VERFUEGT', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
                createKindContainer();
                let betreuung: TSBetreuung = createBetreuung();
                gesuchModelManager.getBetreuungToWorkWith().id = '2afc9d9a-957e-4550-9a22-97624a000feb';
                let verfuegung: TSVerfuegung = new TSVerfuegung();
                spyOn(verfuegungRS, 'saveVerfuegung').and.returnValue($q.when(verfuegung));

                gesuchModelManager.saveVerfuegung(false);
                scope.$apply();

                expect(gesuchModelManager.getVerfuegenToWorkWith()).toBe(verfuegung);
                expect(gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus).toEqual(TSBetreuungsstatus.VERFUEGT);
            });
        });
        describe('calculateNewStatus', function () {
            it('should be GEPRUEFT if there is no betreuung', function () {
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(function (stepName: TSWizardStepName,
                                                                                      status: TSWizardStepStatus) {
                    return stepName === TSWizardStepName.BETREUUNG && status === TSWizardStepStatus.NOK;
                });
                let gesuch: TSGesuch = new TSGesuch();
                spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(false);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.GEPRUEFT)).toEqual(TSAntragStatus.GEPRUEFT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN)).toEqual(TSAntragStatus.GEPRUEFT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN)).toEqual(TSAntragStatus.GEPRUEFT);
            });
            it('should be PLATZBESTAETIGUNG_ABGEWIESEN if there are betreuungen and status of Betreuung is NOK', function () {
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(function (stepName: TSWizardStepName,
                                                                                      status: TSWizardStepStatus) {
                    return stepName === TSWizardStepName.BETREUUNG && status === TSWizardStepStatus.NOK;
                });
                let gesuch: TSGesuch = new TSGesuch();
                spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(true);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.GEPRUEFT)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
            });
            it('should be PLATZBESTAETIGUNG_WARTEN if the status of Betreuung is PLATZBESTAETIGUNG', function () {
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(function (stepName: TSWizardStepName,
                                                                                      status: TSWizardStepStatus) {
                    return stepName === TSWizardStepName.BETREUUNG && status === TSWizardStepStatus.PLATZBESTAETIGUNG;
                });
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.GEPRUEFT)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN)).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
            });
            it('should be GEPRUEFT if the status of Betreuung is PLATZBESTAETIGUNG', function () {
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(function (stepName: TSWizardStepName,
                                                                                      status: TSWizardStepStatus) {
                    return stepName === TSWizardStepName.BETREUUNG && status === TSWizardStepStatus.OK;
                });
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.GEPRUEFT)).toEqual(TSAntragStatus.GEPRUEFT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN)).toEqual(TSAntragStatus.GEPRUEFT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN)).toEqual(TSAntragStatus.GEPRUEFT);
            });
            it('returns the same TSAntragStatus for all others', function () {
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.ERSTE_MAHNUNG)).toEqual(TSAntragStatus.ERSTE_MAHNUNG);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN)).toEqual(TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.FREIGEGEBEN)).toEqual(TSAntragStatus.FREIGEGEBEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.IN_BEARBEITUNG_GS)).toEqual(TSAntragStatus.IN_BEARBEITUNG_GS);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.IN_BEARBEITUNG_JA)).toEqual(TSAntragStatus.IN_BEARBEITUNG_JA);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.FREIGABEQUITTUNG)).toEqual(TSAntragStatus.FREIGABEQUITTUNG);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.NUR_SCHULAMT)).toEqual(TSAntragStatus.NUR_SCHULAMT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.VERFUEGEN)).toEqual(TSAntragStatus.VERFUEGEN);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.VERFUEGT)).toEqual(TSAntragStatus.VERFUEGT);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.ZWEITE_MAHNUNG)).toEqual(TSAntragStatus.ZWEITE_MAHNUNG);
                expect(gesuchModelManager.calculateNewStatus(TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN)).toEqual(TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN);
            });
        });
        describe('hideSteps', function () {
            it('should hide the steps ABWESENHEIT and UMZUG for ONLINE Erstgesuch without umzug', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                spyOn(wizardStepManager, 'hideStep').and.returnValue(undefined);
                spyOn(wizardStepManager, 'unhideStep').and.returnValue(undefined);
                gesuchModelManager.initGesuch(true, TSEingangsart.ONLINE);

                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.UMZUG);
                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.ABWESENHEIT);
                expect(wizardStepManager.unhideStep).toHaveBeenCalledWith(TSWizardStepName.FREIGABE);
            });
            it('should hide the steps ABWESENHEIT and UMZUG and unhide FREIGABE for PAPIER Erstgesuch without umzug', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                spyOn(wizardStepManager, 'hideStep').and.returnValue(undefined);
                spyOn(wizardStepManager, 'unhideStep').and.returnValue(undefined);
                gesuchModelManager.initGesuch(true, TSEingangsart.PAPIER);

                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.UMZUG);
                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.ABWESENHEIT);
                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.FREIGABE);
            });
            it('should unhide the steps ABWESENHEIT and UMZUG for Mutation and hide FREIGABE for ONLINE Gesuch', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                spyOn(wizardStepManager, 'hideStep').and.returnValue(undefined);
                spyOn(wizardStepManager, 'unhideStep').and.returnValue(undefined);
                let gesuch: TSGesuch = new TSGesuch();
                gesuch.typ = TSAntragTyp.MUTATION;
                gesuchModelManager.setGesuch(gesuch);

                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.FREIGABE);
                expect(wizardStepManager.unhideStep).toHaveBeenCalledWith(TSWizardStepName.UMZUG);
                expect(wizardStepManager.unhideStep).toHaveBeenCalledWith(TSWizardStepName.ABWESENHEIT);
            });
            it('should unhide the step UMZUG for Erstgesuch with umzug and hide ABWESENHEIT', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                spyOn(wizardStepManager, 'hideStep').and.returnValue(undefined);
                spyOn(wizardStepManager, 'unhideStep').and.returnValue(undefined);
                let gesuch: TSGesuch = new TSGesuch();
                gesuch.typ = TSAntragTyp.ERSTGESUCH;
                gesuch.gesuchsteller1 = TestDataUtil.createGesuchsteller('Julio', 'Iglesias');
                gesuch.gesuchsteller1.addAdresse(TestDataUtil.createAdresse('wohnstrasse', '1'));
                gesuch.gesuchsteller1.addAdresse(TestDataUtil.createAdresse('umzug', '2'));
                gesuchModelManager.setGesuch(gesuch);

                expect(wizardStepManager.hideStep).not.toHaveBeenCalledWith(TSWizardStepName.UMZUG);
                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.ABWESENHEIT);
                expect(wizardStepManager.unhideStep).toHaveBeenCalledWith(TSWizardStepName.UMZUG);
                expect(wizardStepManager.unhideStep).not.toHaveBeenCalledWith(TSWizardStepName.ABWESENHEIT);
                expect(wizardStepManager.hideStep).toHaveBeenCalledWith(TSWizardStepName.FREIGABE);
            });
        });
        describe('updateBetreuungen', function () {
            it('should return empty Promise for undefined betreuung list', function () {
                let promise: angular.IPromise<Array<TSBetreuung>> = gesuchModelManager.updateBetreuungen(undefined, true);
                expect(promise).toBeDefined();
                let promiseExecuted: Array<TSBetreuung> = null;
                promise.then((response) => {
                    promiseExecuted = response;
                });
                scope.$apply();
                expect(promiseExecuted).toBe(undefined);
            });
            it('should return empty Promise for empty betreuung list', function () {
                let promise: angular.IPromise<Array<TSBetreuung>> = gesuchModelManager.updateBetreuungen([], true);
                expect(promise).toBeDefined();
                let promiseExecuted: boolean = false;
                promise.then((response) => {
                    promiseExecuted = true;
                });
                scope.$apply();
                expect(promiseExecuted).toBe(true);
            });
            it('should return a Promise with the Betreuung that was updated', function () {
                let myGesuch = new TSGesuch();
                myGesuch.id = 'gesuchID';
                TestDataUtil.setAbstractFieldsUndefined(myGesuch);
                let betreuung: TSBetreuung = new TSBetreuung();
                betreuung.id = 'betreuungId';
                let betreuungen: Array<TSBetreuung> = [betreuung];
                let kindContainer: TSKindContainer = new TSKindContainer(undefined, undefined, betreuungen);
                kindContainer.id = 'kindID';
                myGesuch.kindContainers = [kindContainer];

                spyOn(betreuungRS, 'saveBetreuungen').and.returnValue($q.when([betreuung]));
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when(undefined));
                spyOn(gesuchModelManager, 'setHiddenSteps').and.returnValue(undefined);
                gesuchModelManager.setGesuch(myGesuch);

                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

                let promise: angular.IPromise<Array<TSBetreuung>> = gesuchModelManager.updateBetreuungen(betreuungen, true);

                expect(promise).toBeDefined();
                let promiseExecuted: Array<TSBetreuung> = undefined;
                promise.then((response) => {
                    promiseExecuted = response;
                });

                scope.$apply();
                expect(betreuungRS.saveBetreuungen).toHaveBeenCalledWith(betreuungen, myGesuch.id, true);
                expect(promiseExecuted.length).toBe(1);
                expect(promiseExecuted[0]).toEqual(betreuung);
            });
        });
        describe('openGesuch', function () {
            it('should call findGesuchForInstitution for role Institution or Traegerschaft', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

                let gesuch: TSGesuch = new TSGesuch();
                gesuch.id = '123';
                spyOn(gesuchRS, 'findGesuchForInstitution').and.returnValue($q.when(gesuch));
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when({}));
                spyOn(wizardStepManager, 'unhideStep').and.returnValue($q.when({}));

                gesuchModelManager.openGesuch(gesuch.id);
                scope.$apply();

                expect(gesuchRS.findGesuchForInstitution).toHaveBeenCalledWith(gesuch.id);
                expect(gesuchModelManager.getGesuch()).toEqual(gesuch);
            });
            it('should call findGesuch for other role but Institution/Traegerschaft', function () {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);

                let gesuch: TSGesuch = new TSGesuch();
                gesuch.id = '123';
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(false);
                spyOn(gesuchRS, 'findGesuch').and.returnValue($q.when(gesuch));
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue($q.when({}));
                spyOn(wizardStepManager, 'unhideStep').and.returnValue($q.when({}));

                gesuchModelManager.openGesuch(gesuch.id);
                scope.$apply();

                expect(gesuchRS.findGesuch).toHaveBeenCalledWith(gesuch.id);
                expect(gesuchModelManager.getGesuch()).toEqual(gesuch);
            });
        });
        describe('areThereOnlySchulamtAngebote', function () {
            beforeEach(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
                gesuchModelManager.initGesuch(false, TSEingangsart.PAPIER);
            });
            it('should be true if only Schulamtangebote', function () {
                createKindWithBetreuung();
                setInstitutionToExistingBetreuung(TSBetreuungsangebotTyp.TAGESSCHULE);

                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(true);
            });
            it('should be false if not only Schulamtangebote', function () {
                createKindWithBetreuung();
                setInstitutionToExistingBetreuung(TSBetreuungsangebotTyp.KITA);

                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(false);
            });
            it('should be false if there are no Betreuungen or Kinds', function () {
                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(false);
            });
        });
    });

    // HELP METHODS

    function createKindContainer() {
        gesuchModelManager.initKinder();
        createKind();
        gesuchModelManager.getKindToWorkWith().initBetreuungList();
    }

    function createKind(): void {
        let tsKindContainer = new TSKindContainer(undefined, new TSKind());
        gesuchModelManager.getGesuch().kindContainers.push(tsKindContainer);
        gesuchModelManager.setKindIndex(gesuchModelManager.getGesuch().kindContainers.length - 1);
        tsKindContainer.kindNummer = gesuchModelManager.getKindIndex() + 1;
    }

    function createKindWithBetreuung() {
        createKindContainer();
        gesuchModelManager.getKindToWorkWith().kindJA.familienErgaenzendeBetreuung = true;
        createBetreuung();
    }

    function setInstitutionToExistingBetreuung(typ: TSBetreuungsangebotTyp) {
        let institution: TSInstitutionStammdaten = new TSInstitutionStammdaten();
        institution.betreuungsangebotTyp = typ;
        gesuchModelManager.getBetreuungToWorkWith().institutionStammdaten = institution;
    }

    function createBetreuung(): TSBetreuung {
        gesuchModelManager.getKindToWorkWith().initBetreuungList();
        let tsBetreuung: TSBetreuung = new TSBetreuung();
        tsBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
        tsBetreuung.betreuungNummer = 1;
        tsBetreuung.id = '2afc9d9a-957e-4550-9a22-97624a000feb';
        gesuchModelManager.getKindToWorkWith().betreuungen.push(tsBetreuung);
        gesuchModelManager.setBetreuungIndex(gesuchModelManager.getKindToWorkWith().betreuungen.length - 1);
        return tsBetreuung;
    }

});

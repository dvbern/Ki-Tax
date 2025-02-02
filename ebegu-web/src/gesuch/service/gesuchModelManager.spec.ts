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
import {IHttpBackendService, IQService, IScope} from 'angular';
import * as moment from 'moment';
import {CORE_JS_MODULE} from '../../app/core/core.angularjs.module';
import {AntragStatusHistoryRS} from '../../app/core/service/antragStatusHistoryRS.rest';
import {BetreuungRS} from '../../app/core/service/betreuungRS.rest';
import {KindRS} from '../../app/core/service/kindRS.rest';
import {VerfuegungRS} from '../../app/core/service/verfuegungRS.rest';
import {AuthServiceRS} from '../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../hybridTools/translationsMock';
import {TSAntragStatus} from '../../models/enums/TSAntragStatus';
import {TSBetreuungsangebotTyp} from '../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../models/enums/betreuung/TSBetreuungsstatus';
import {TSCreationAction} from '../../models/enums/TSCreationAction';
import {TSEingangsart} from '../../models/enums/TSEingangsart';
import {TSFamilienstatus} from '../../models/enums/TSFamilienstatus';
import {TSGesuchBetreuungenStatus} from '../../models/enums/TSGesuchBetreuungenStatus';
import {TSGesuchsperiodeStatus} from '../../models/enums/TSGesuchsperiodeStatus';
import {TSGesuchstellerKardinalitaet} from '../../models/enums/TSGesuchstellerKardinalitaet';
import {TSUnterhaltsvereinbarungAnswer} from '../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSWizardStepName} from '../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../models/enums/TSWizardStepStatus';
import {TSAntragStatusHistory} from '../../models/TSAntragStatusHistory';
import {TSBenutzerNoDetails} from '../../models/TSBenutzerNoDetails';
import {TSBetreuung} from '../../models/TSBetreuung';
import {TSDossier} from '../../models/TSDossier';
import {TSFamiliensituation} from '../../models/TSFamiliensituation';
import {TSFamiliensituationContainer} from '../../models/TSFamiliensituationContainer';
import {TSGesuch} from '../../models/TSGesuch';
import {TSGesuchsperiode} from '../../models/TSGesuchsperiode';
import {TSInstitutionStammdaten} from '../../models/TSInstitutionStammdaten';
import {TSKind} from '../../models/TSKind';
import {TSKindContainer} from '../../models/TSKindContainer';
import {TSVerfuegung} from '../../models/TSVerfuegung';
import {TSDateRange} from '../../models/types/TSDateRange';
import {DateUtil} from '../../utils/DateUtil';
import {TestDataUtil} from '../../utils/TestDataUtil.spec';
import {DossierRS} from './dossierRS.rest';
import {GesuchModelManager} from './gesuchModelManager';
import {GesuchRS} from './gesuchRS.rest';
import {WizardStepManager} from './wizardStepManager';

/* eslint-disable */
describe('gesuchModelManager', () => {
    let gesuchModelManager: GesuchModelManager;
    let betreuungRS: BetreuungRS;
    let dossierRS: DossierRS;
    let gesuchRS: GesuchRS;
    let kindRS: KindRS;
    let scope: IScope;
    let $httpBackend: IHttpBackendService;
    let $q: IQService;
    let authServiceRS: AuthServiceRS;
    let wizardStepManager: WizardStepManager;
    let verfuegungRS: VerfuegungRS;
    let antragStatusHistoryRS: AntragStatusHistoryRS;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            $httpBackend = $injector.get('$httpBackend');
            betreuungRS = $injector.get('BetreuungRS');
            dossierRS = $injector.get('DossierRS');
            gesuchRS = $injector.get('GesuchRS');
            kindRS = $injector.get('KindRS');
            scope = $injector.get('$rootScope');
            $q = $injector.get('$q');
            authServiceRS = $injector.get('AuthServiceRS');
            wizardStepManager = $injector.get('WizardStepManager');
            verfuegungRS = $injector.get('VerfuegungRS');
            antragStatusHistoryRS = $injector.get('AntragStatusHistoryRS');
        })
    );

    describe('API Usage', () => {
        describe('removeBetreuungFromKind', () => {
            it('should remove the current Betreuung from the list of the current Kind', waitForAsync(() => {
                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        createKindContainer();
                        createBetreuung();
                        expect(
                            gesuchModelManager.getKindToWorkWith().betreuungen
                        ).toBeDefined();
                        expect(
                            gesuchModelManager.getKindToWorkWith().betreuungen
                                .length
                        ).toBe(1);
                        gesuchModelManager.removeBetreuungFromKind();
                        expect(
                            gesuchModelManager.getKindToWorkWith().betreuungen
                        ).toBeDefined();
                        expect(
                            gesuchModelManager.getKindToWorkWith().betreuungen
                                .length
                        ).toBe(0);
                    });
            }));
        });
        describe('saveBetreuung', () => {
            it('updates a betreuung', waitForAsync(() => {
                $httpBackend
                    .when('GET', '/ebegu/api/v1/dossier/id/undefined')
                    .respond({});
                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        createKindContainer();
                        const betreuung = createBetreuung();
                        gesuchModelManager.getKindToWorkWith().id =
                            '2afc9d9a-957e-4550-9a22-97624a000feb';

                        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                            $httpBackend
                        );
                        const kindToWorkWith =
                            gesuchModelManager.getKindToWorkWith();
                        kindToWorkWith.nextNumberBetreuung = 5;
                        spyOn(kindRS, 'findKind').and.returnValue(
                            $q.when(kindToWorkWith)
                        );
                        spyOn(betreuungRS, 'saveBetreuung').and.returnValue(
                            $q.when(betreuung)
                        );
                        spyOn(
                            wizardStepManager,
                            'findStepsFromGesuch'
                        ).and.returnValue($q.resolve());
                        const bestaetigt = $q.when(
                            TSGesuchBetreuungenStatus.ALLE_BESTAETIGT
                        );
                        spyOn(
                            gesuchRS,
                            'getGesuchBetreuungenStatus'
                        ).and.returnValue(bestaetigt);

                        gesuchModelManager.saveBetreuung(
                            gesuchModelManager.getKindToWorkWith()
                                .betreuungen[0],
                            TSBetreuungsstatus.WARTEN,
                            false
                        );
                        scope.$apply();

                        expect(betreuungRS.saveBetreuung).toHaveBeenCalledWith(
                            gesuchModelManager.getBetreuungToWorkWith(),
                            undefined,
                            false
                        );

                        expect(kindRS.findKind).toHaveBeenCalledWith(
                            '2afc9d9a-957e-4550-9a22-97624a000feb'
                        );
                        expect(
                            gesuchModelManager.getKindToWorkWith()
                                .nextNumberBetreuung
                        ).toEqual(5);
                        expect(
                            gesuchModelManager.getGesuch()
                                .gesuchBetreuungenStatus
                        ).toEqual(TSGesuchBetreuungenStatus.ALLE_BESTAETIGT);
                    });
            }));
        });
        describe('saveGesuchAndFall', () => {
            it('creates a Fall with a linked Gesuch', waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );

                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        gesuchModelManager.saveGesuchAndFall().then(gesuch => {
                            expect(gesuch).toBeDefined();
                            expect(gesuch.eingangsart).toBe(
                                TSEingangsart.PAPIER
                            );
                        });
                    });
            }));
            it('only updates the Gesuch because it already exists', waitForAsync(() => {
                spyOn(gesuchRS, 'updateGesuch').and.returnValue(
                    $q.resolve(new TSGesuch())
                );
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );

                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        gesuchModelManager.getGesuch().timestampErstellt =
                            DateUtil.today();
                        gesuchModelManager.saveGesuchAndFall();

                        scope.$apply();

                        expect(gesuchRS.updateGesuch).toHaveBeenCalled();
                    });
            }));
        });
        describe('setUserAsFallVerantwortlicherBG', () => {
            it('puts the given user as the verantwortlicherBG for the fall', waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(gesuch => {
                        gesuch.dossier = new TSDossier();
                        gesuch.dossier.id = 'myId';
                        spyOn(authServiceRS, 'getPrincipal').and.returnValue(
                            undefined
                        );
                        spyOn(
                            dossierRS,
                            'setVerantwortlicherBG'
                        ).and.returnValue($q.resolve(gesuch.dossier) as any);
                        const user = new TSBenutzerNoDetails(
                            'Emilianito',
                            'Camacho'
                        );
                        gesuchModelManager.setUserAsFallVerantwortlicherBG(
                            user
                        );
                        scope.$apply();
                        expect(
                            gesuchModelManager.getGesuch().dossier
                                .verantwortlicherBG
                        ).toBe(user);
                    });
            }));
        });
        describe('exist at least one Betreuung among all kinder', () => {
            it('should return false for empty list', () => {
                const gesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(
                    gesuchModelManager.getGesuch().isThereAnyBetreuung()
                ).toBe(false);
            });
            it('should return false for a list with Kinder but no Betreuung', () => {
                const kind = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = false;
                const gesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([
                    kind
                ]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(
                    gesuchModelManager.getGesuch().isThereAnyBetreuung()
                ).toBe(false);
            });
            it('should return true for a list with Kinder needing Betreuung', () => {
                const kind = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = true;
                const betreuung = new TSBetreuung();
                kind.betreuungen = [betreuung];
                const gesuch = new TSGesuch();
                spyOn(gesuch, 'getKinderWithBetreuungList').and.returnValue([
                    kind
                ]);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(
                    gesuchModelManager.getGesuch().isThereAnyBetreuung()
                ).toBe(true);
            });
        });

        describe('exist kinder with betreuung needed', () => {
            it('should return false for empty list', () => {
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([]);
                expect(
                    gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()
                ).toBe(false);
            });
            it('should return false for a list with no Kind needing Betreuung', () => {
                const kind = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.familienErgaenzendeBetreuung = false;
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([
                    kind
                ]);
                expect(
                    gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()
                ).toBe(false);
            });
            it('should return true for a list with Kinder needing Betreuung', () => {
                const kind = new TSKindContainer();
                kind.kindJA = new TSKind();
                kind.kindJA.timestampErstellt = DateUtil.today();
                kind.kindJA.familienErgaenzendeBetreuung = true;
                spyOn(gesuchModelManager, 'getKinderList').and.returnValue([
                    kind
                ]);
                expect(
                    gesuchModelManager.isThereAnyKindWithBetreuungsbedarf()
                ).toBe(true);
            });
        });
        describe('saveGesuchStatus', () => {
            it('should update the status of the Gesuch im Server und Client', waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        spyOn(gesuchRS, 'updateGesuchStatus').and.returnValue(
                            $q.resolve() as any
                        );
                        spyOn(
                            antragStatusHistoryRS,
                            'loadLastStatusChange'
                        ).and.returnValue(
                            $q.resolve(new TSAntragStatusHistory()) as any
                        );

                        gesuchModelManager.saveGesuchStatus(
                            TSAntragStatus.ERSTE_MAHNUNG
                        );

                        scope.$apply();
                        expect(gesuchModelManager.getGesuch().status).toEqual(
                            TSAntragStatus.ERSTE_MAHNUNG
                        );
                    });
            }));
        });
        describe('saveVerfuegung', () => {
            it('should save the current Verfuegung und set the status of the Betreuung to VERFUEGT', waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                gesuchModelManager
                    .initGesuch(
                        TSEingangsart.PAPIER,
                        TSCreationAction.CREATE_NEW_FALL,
                        undefined
                    )
                    .then(() => {
                        createKindContainer();
                        createBetreuung();
                        gesuchModelManager.getBetreuungToWorkWith().id =
                            '2afc9d9a-957e-4550-9a22-97624a000feb';
                        const verfuegung = new TSVerfuegung();
                        spyOn(verfuegungRS, 'saveVerfuegung').and.returnValue(
                            $q.when(verfuegung)
                        );

                        gesuchModelManager.saveVerfuegung(
                            false,
                            false,
                            'bemerkungen'
                        );
                        scope.$apply();

                        expect(
                            gesuchModelManager.getVerfuegenToWorkWith()
                        ).toBe(verfuegung);
                        expect(
                            gesuchModelManager.getBetreuungToWorkWith()
                                .betreuungsstatus
                        ).toEqual(TSBetreuungsstatus.VERFUEGT);
                    });
            }));
        });
        describe('calculateNewStatus', () => {
            it('should be GEPRUEFT if there is no betreuung', () => {
                const hasStepGivenStatus = (
                    stepName: TSWizardStepName,
                    status: TSWizardStepStatus
                ) =>
                    stepName === TSWizardStepName.BETREUUNG &&
                    status === TSWizardStepStatus.NOK;
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(
                    hasStepGivenStatus
                );
                const gesuch = new TSGesuch();
                spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(false);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.GEPRUEFT
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
            });
            it('should be PLATZBESTAETIGUNG_ABGEWIESEN if there are betreuungen and status of Betreuung is NOK', () => {
                const hasStepGivenStatus = (
                    stepName: TSWizardStepName,
                    status: TSWizardStepStatus
                ) =>
                    stepName === TSWizardStepName.BETREUUNG &&
                    status === TSWizardStepStatus.NOK;
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(
                    hasStepGivenStatus
                );
                const gesuch = new TSGesuch();
                spyOn(gesuch, 'isThereAnyBetreuung').and.returnValue(true);
                spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.GEPRUEFT
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN);
            });
            it('should be PLATZBESTAETIGUNG_WARTEN if the status of Betreuung is PLATZBESTAETIGUNG', () => {
                const hasStepGivenStatus = (
                    stepName: TSWizardStepName,
                    status: TSWizardStepStatus
                ) =>
                    stepName === TSWizardStepName.BETREUUNG &&
                    status === TSWizardStepStatus.PLATZBESTAETIGUNG;
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(
                    hasStepGivenStatus
                );
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.GEPRUEFT
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN
                    )
                ).toEqual(TSAntragStatus.PLATZBESTAETIGUNG_WARTEN);
            });
            it('should be GEPRUEFT if the status of Betreuung is PLATZBESTAETIGUNG', () => {
                const hasStepGivenStatus = (
                    stepName: TSWizardStepName,
                    status: TSWizardStepStatus
                ) =>
                    stepName === TSWizardStepName.BETREUUNG &&
                    status === TSWizardStepStatus.OK;
                spyOn(wizardStepManager, 'hasStepGivenStatus').and.callFake(
                    hasStepGivenStatus
                );
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.GEPRUEFT
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_WARTEN
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.PLATZBESTAETIGUNG_ABGEWIESEN
                    )
                ).toEqual(TSAntragStatus.GEPRUEFT);
            });
            it('returns the same TSAntragStatus for all others', () => {
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.ERSTE_MAHNUNG
                    )
                ).toEqual(TSAntragStatus.ERSTE_MAHNUNG);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN
                    )
                ).toEqual(TSAntragStatus.ERSTE_MAHNUNG_ABGELAUFEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.FREIGEGEBEN
                    )
                ).toEqual(TSAntragStatus.FREIGEGEBEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.IN_BEARBEITUNG_GS
                    )
                ).toEqual(TSAntragStatus.IN_BEARBEITUNG_GS);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.IN_BEARBEITUNG_JA
                    )
                ).toEqual(TSAntragStatus.IN_BEARBEITUNG_JA);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.FREIGABEQUITTUNG
                    )
                ).toEqual(TSAntragStatus.FREIGABEQUITTUNG);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.NUR_SCHULAMT
                    )
                ).toEqual(TSAntragStatus.NUR_SCHULAMT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.VERFUEGEN
                    )
                ).toEqual(TSAntragStatus.VERFUEGEN);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.VERFUEGT
                    )
                ).toEqual(TSAntragStatus.VERFUEGT);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.ZWEITE_MAHNUNG
                    )
                ).toEqual(TSAntragStatus.ZWEITE_MAHNUNG);
                expect(
                    gesuchModelManager.calculateNewStatus(
                        TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN
                    )
                ).toEqual(TSAntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN);
            });
        });
        describe('updateBetreuungen', () => {
            it('should return empty Promise for undefined betreuung list', () => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                const promise = gesuchModelManager.updateBetreuungen(
                    undefined,
                    true
                );
                expect(promise).toBeDefined();
                let promiseExecuted: Array<TSBetreuung> = null;
                promise.then(response => {
                    promiseExecuted = response;
                });
                scope.$apply();
                expect(promiseExecuted).toBe(undefined);
            });
            it('should return empty Promise for empty betreuung list', () => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                const promise = gesuchModelManager.updateBetreuungen([], true);
                expect(promise).toBeDefined();
                let promiseExecuted = false;
                promise.then(() => {
                    promiseExecuted = true;
                });
                scope.$apply();
                expect(promiseExecuted).toBe(true);
            });
            it('should return a Promise with the Betreuung that was updated', () => {
                const gesuchPeriode = new TSGesuchsperiode();
                gesuchPeriode.id = '123';
                spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                    gesuchPeriode
                );
                const myGesuch = new TSGesuch();
                myGesuch.id = 'gesuchID';
                TestDataUtil.setAbstractMutableFieldsUndefined(myGesuch);
                const betreuung = new TSBetreuung();
                betreuung.id = 'betreuungId';
                const betreuungen = [betreuung];
                const kindContainer = new TSKindContainer();
                kindContainer.betreuungen = betreuungen;
                kindContainer.id = 'kindID';
                myGesuch.kindContainers = [kindContainer];

                spyOn(betreuungRS, 'saveBetreuungen').and.returnValue(
                    $q.when([betreuung])
                );
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(
                    $q.when(undefined)
                );
                gesuchModelManager.setGesuch(myGesuch);

                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );

                const promise = gesuchModelManager.updateBetreuungen(
                    betreuungen,
                    true
                );

                expect(promise).toBeDefined();
                let promiseExecuted: Array<TSBetreuung>;
                promise.then(response => {
                    promiseExecuted = response;
                });

                scope.$apply();

                expect(betreuungRS.saveBetreuungen).toHaveBeenCalledWith(
                    betreuungen,
                    myGesuch.id,
                    true
                );
                expect(promiseExecuted.length).toBe(1);
                expect(promiseExecuted[0]).toEqual(betreuung);
            });
        });
        describe('openGesuch', () => {
            it('should call findGesuchForInstitution for role Institution or Traegerschaft', () => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                $httpBackend
                    .when('GET', '/ebegu/api/v1/antragStatusHistory/123')
                    .respond({});

                const gesuch = new TSGesuch();
                gesuch.id = '123';
                spyOn(gesuchRS, 'findGesuchForInstitution').and.returnValue(
                    $q.resolve(gesuch)
                );
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(
                    $q.resolve()
                );
                spyOn(wizardStepManager, 'unhideStep').and.returnValue();
                const gesuchPeriode = new TSGesuchsperiode();
                gesuchPeriode.id = '123';
                spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                    gesuchPeriode
                );

                gesuchModelManager.openGesuch(gesuch.id);
                scope.$apply();

                expect(gesuchRS.findGesuchForInstitution).toHaveBeenCalledWith(
                    gesuch.id
                );
                expect(gesuchModelManager.getGesuch()).toEqual(gesuch);
            });
            it('should call findGesuch for other role but Institution/Traegerschaft', () => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                $httpBackend
                    .when('GET', '/ebegu/api/v1/antragStatusHistory/123')
                    .respond({});

                const gesuch = new TSGesuch();
                gesuch.id = '123';
                spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(false);
                spyOn(gesuchRS, 'findGesuch').and.returnValue($q.when(gesuch));
                spyOn(wizardStepManager, 'findStepsFromGesuch').and.returnValue(
                    $q.resolve()
                );
                spyOn(wizardStepManager, 'unhideStep').and.returnValue();
                const gesuchPeriode = new TSGesuchsperiode();
                gesuchPeriode.id = '123';
                spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                    gesuchPeriode
                );

                gesuchModelManager.openGesuch(gesuch.id);
                scope.$apply();

                expect(gesuchRS.findGesuch).toHaveBeenCalledWith(gesuch.id);
                expect(gesuchModelManager.getGesuch()).toEqual(gesuch);
            });
        });
        describe('areThereOnlySchulamtAngebote', () => {
            beforeEach(waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                gesuchModelManager.initGesuch(
                    TSEingangsart.PAPIER,
                    TSCreationAction.CREATE_NEW_FALL,
                    undefined
                );
            }));
            it('should be true if only Schulamtangebote', () => {
                createKindWithBetreuung();
                setInstitutionToExistingBetreuung(
                    TSBetreuungsangebotTyp.TAGESSCHULE
                );

                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(
                    true
                );
            });
            it('should be false if not only Schulamtangebote', () => {
                createKindWithBetreuung();
                setInstitutionToExistingBetreuung(TSBetreuungsangebotTyp.KITA);

                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(
                    false
                );
            });
            it('should be false if there are no Betreuungen or Kinds', () => {
                expect(gesuchModelManager.areThereOnlySchulamtAngebote()).toBe(
                    false
                );
            });
        });
        describe('isGesuchsteller2Required', () => {
            const dateFrom = moment('2020-08-01');
            const dateTo = moment('2021-07-31');
            const gesuchsperiode = new TSGesuchsperiode(
                TSGesuchsperiodeStatus.AKTIV,
                new TSDateRange(dateFrom, dateTo)
            );
            beforeEach(waitForAsync(() => {
                TestDataUtil.mockDefaultGesuchModelManagerHttpCalls(
                    $httpBackend
                );
                gesuchModelManager.initGesuch(
                    TSEingangsart.PAPIER,
                    TSCreationAction.CREATE_NEW_FALL,
                    undefined
                );
            }));
            it('should be false if ALLEINERZIEHEND', () => {
                createFamsit(false, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should be true if VERHEIRATET', () => {
                createFamsit(false, TSFamilienstatus.VERHEIRATET);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be true if KONKUBINAT', () => {
                createFamsit(false, TSFamilienstatus.KONKUBINAT);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be true if KONKUBINAT_KEIN_KIND', () => {
                createFamsit(false, TSFamilienstatus.KONKUBINAT_KEIN_KIND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                let startKonkubinat = moment('2018-01-01');
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.startKonkubinat =
                    startKonkubinat;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
                startKonkubinat = moment('2019-12-01');
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.startKonkubinat =
                    startKonkubinat;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should be false if ALLEINERZIEHEND with geteilte Obhut and FKJV ALLEINE', () => {
                createFamsit(true, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.geteilteObhut =
                    true;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ALLEINE;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should be true if ALLEINERZIEHEND with geteilte Obhut and FKJV ZU_ZWEIT', () => {
                createFamsit(true, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.geteilteObhut =
                    true;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ZU_ZWEIT;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be false if ALLEINERZIEHEND with no geteilte Obhut and unterhaltsvereinbarung', () => {
                createFamsit(true, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.geteilteObhut =
                    false;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should be false if ALLEINERZIEHEND with no geteilte Obhut and unterhaltsvereinbarung nicht moeglich', () => {
                createFamsit(true, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.geteilteObhut =
                    false;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should be true if ALLEINERZIEHEND with no geteilte Obhut and no unterhaltsvereinbarung', () => {
                createFamsit(true, TSFamilienstatus.ALLEINERZIEHEND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.geteilteObhut =
                    false;
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be true if VERHEIRATET with FKJV', () => {
                createFamsit(true, TSFamilienstatus.VERHEIRATET);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be true if KONKUBINAT with FKJV', () => {
                createFamsit(true, TSFamilienstatus.KONKUBINAT);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('should be true if KONKUBINAT_KEIN_KIND with FKJV and Konkubinat is old', () => {
                createFamsit(true, TSFamilienstatus.KONKUBINAT_KEIN_KIND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                const startKonkubinat = moment('2015-01-01');
                gesuchModelManager.getGesuch().familiensituationContainer.familiensituationJA.startKonkubinat =
                    startKonkubinat;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );
            });
            it('different cases should be true if KONKUBINAT_KEIN_KIND with FKJV and Konkubinat is young', () => {
                createFamsit(true, TSFamilienstatus.KONKUBINAT_KEIN_KIND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                const startKonkubinat = moment('2019-12-01');

                const famSit =
                    gesuchModelManager.getGesuch().familiensituationContainer
                        .familiensituationJA;
                famSit.startKonkubinat = startKonkubinat;

                famSit.geteilteObhut = true;
                famSit.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ALLEINE;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );

                famSit.gesuchstellerKardinalitaet =
                    TSGesuchstellerKardinalitaet.ZU_ZWEIT;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );

                famSit.geteilteObhut = false;
                famSit.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );

                famSit.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    true
                );

                famSit.unterhaltsvereinbarung =
                    TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
                expect(gesuchModelManager.isGesuchsteller2Required()).toBe(
                    false
                );
            });
            it('should test different edge cases for konkubinat duration calculation', () => {
                createFamsit(true, TSFamilienstatus.KONKUBINAT_KEIN_KIND);
                gesuchModelManager.getGesuch().gesuchsperiode = gesuchsperiode;
                const famSit =
                    gesuchModelManager.getGesuch().familiensituationContainer
                        .familiensituationJA;

                famSit.startKonkubinat = moment('2021-07-31');
                expect(
                    famSit.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
                        moment('2021-07-31')
                    )
                ).toBe(false);

                famSit.startKonkubinat = moment('2018-08-01');
                expect(
                    famSit.konkubinatGetsLongerThanXYearsBeforeEndOfPeriode(
                        moment('2021-07-31')
                    )
                ).toBe(true);

                const gp = new TSGesuchsperiode();
                gp.gueltigkeit = new TSDateRange(
                    moment('2020-08-01'),
                    moment('2021-07-31')
                );

                famSit.startKonkubinat = moment('2018-07-31');
                expect(
                    famSit.konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
                        gp
                    )
                ).toBe(false);

                famSit.startKonkubinat = moment('2018-08-01');
                expect(
                    famSit.konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
                        gp
                    )
                ).toBe(true);

                famSit.startKonkubinat = moment('2019-07-31');
                expect(
                    famSit.konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
                        gp
                    )
                ).toBe(true);
            });
        });
    });

    // HELP METHODS
    function createFamsit(
        isFKJV: boolean,
        familienStatus: TSFamilienstatus
    ): void {
        const famSitCont = new TSFamiliensituationContainer();
        const famSit = new TSFamiliensituation();
        famSit.minDauerKonkubinat = 2;
        famSit.fkjvFamSit = isFKJV;
        famSit.familienstatus = familienStatus;
        famSitCont.familiensituationJA = famSit;
        gesuchModelManager.getGesuch().familiensituationContainer = famSitCont;
    }

    function createKindContainer(): void {
        gesuchModelManager.initKinder();
        createKind();
        gesuchModelManager.getKindToWorkWith().initBetreuungList();
    }

    function createKind(): void {
        const tsKindContainer = new TSKindContainer();
        tsKindContainer.kindJA = new TSKind();
        gesuchModelManager.getGesuch().kindContainers.push(tsKindContainer);
        gesuchModelManager.setKindIndex(
            gesuchModelManager.getGesuch().kindContainers.length - 1
        );
        tsKindContainer.kindNummer = gesuchModelManager.getKindIndex() + 1;
    }

    function createKindWithBetreuung(): void {
        createKindContainer();
        gesuchModelManager.getKindToWorkWith().kindJA.familienErgaenzendeBetreuung =
            true;
        createBetreuung();
    }

    function setInstitutionToExistingBetreuung(
        typ: TSBetreuungsangebotTyp
    ): void {
        const institution = new TSInstitutionStammdaten();
        institution.betreuungsangebotTyp = typ;
        gesuchModelManager.getBetreuungToWorkWith().institutionStammdaten =
            institution;
    }

    function createBetreuung(): TSBetreuung {
        gesuchModelManager.getKindToWorkWith().initBetreuungList();
        const tsBetreuung = new TSBetreuung();
        tsBetreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
        tsBetreuung.betreuungNummer = 1;
        tsBetreuung.id = '2afc9d9a-957e-4550-9a22-97624a000feb';
        tsBetreuung.kindId = '2afc9d9a-957e-4550-9a22-97624a000feb';
        gesuchModelManager.getKindToWorkWith().betreuungen.push(tsBetreuung);
        gesuchModelManager.setBetreuungIndex(
            gesuchModelManager.getKindToWorkWith().betreuungen.length - 1
        );
        return tsBetreuung;
    }
});

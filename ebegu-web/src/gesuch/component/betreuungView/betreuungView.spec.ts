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
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSBetreuungsangebotTyp} from '../../../models/enums/betreuung/TSBetreuungsangebotTyp';
import {TSBetreuungsstatus} from '../../../models/enums/betreuung/TSBetreuungsstatus';
import {TSAntragStatus} from '../../../models/enums/TSAntragStatus';
import {TSAntragTyp} from '../../../models/enums/TSAntragTyp';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSIntegrationTyp} from '../../../models/enums/TSIntegrationTyp';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSBetreuungspensum} from '../../../models/TSBetreuungspensum';
import {TSBetreuungspensumContainer} from '../../../models/TSBetreuungspensumContainer';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSErweiterteBetreuung} from '../../../models/TSErweiterteBetreuung';
import {TSErweiterteBetreuungContainer} from '../../../models/TSErweiterteBetreuungContainer';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSGesuchsperiode} from '../../../models/TSGesuchsperiode';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSInstitutionStammdatenBetreuungsgutscheine} from '../../../models/TSInstitutionStammdatenBetreuungsgutscheine';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import {DateUtil} from '../../../utils/DateUtil';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {IBetreuungStateParams} from '../../gesuch.route';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {BetreuungViewController} from './betreuungView';
import ITranslateService = angular.translate.ITranslateService;
/* eslint-disable max-len */
describe('betreuungView', () => {
    const betreuungenState = 'gesuch.betreuungen';

    let betreuungView: BetreuungViewController;
    let gesuchModelManager: GesuchModelManager;
    let $state: StateService;
    let ebeguUtil: EbeguUtil;
    let $q: angular.IQService;
    let betreuung: TSBetreuung;
    let kind: TSKindContainer;
    let $rootScope: angular.IRootScopeService;
    let $httpBackend: angular.IHttpBackendService;
    let authServiceRS: AuthServiceRS;
    let wizardStepManager: WizardStepManager;
    let $stateParams: IBetreuungStateParams;
    let $timeout: angular.ITimeoutService;
    let einstellungRS: EinstellungRS;
    let institutionStammdatenRS: InstitutionStammdatenRS;
    let mandantService: MandantService;
    let ebeguRestUtil: EbeguRestUtil;
    let $translateMock: jasmine.SpyObj<ITranslateService>;

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(
        angular.mock.inject($injector => {
            gesuchModelManager = $injector.get('GesuchModelManager');
            $state = $injector.get('$state');
            ebeguUtil = $injector.get('EbeguUtil');
            $httpBackend = $injector.get('$httpBackend');
            $q = $injector.get('$q');
            $stateParams = $injector.get('$stateParams');
            $timeout = $injector.get('$timeout');
            einstellungRS = $injector.get('EinstellungRS');
            institutionStammdatenRS = $injector.get('InstitutionStammdatenRS');
            mandantService = $injector.get('MandantService');
            ebeguRestUtil = $injector.get('EbeguRestUtil');
            const applicationPropertyRS = $injector.get(
                'ApplicationPropertyRS'
            );
            // they always need to be mocked
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
            TestDataUtil.mockLazyGesuchModelManagerHttpCalls($httpBackend);

            betreuung = new TSBetreuung();
            betreuung.timestampErstellt = DateUtil.today();
            betreuung.betreuungsstatus = TSBetreuungsstatus.AUSSTEHEND;
            const erweiterteBetreuungContainer =
                new TSErweiterteBetreuungContainer();
            erweiterteBetreuungContainer.erweiterteBetreuungJA =
                new TSErweiterteBetreuung();
            erweiterteBetreuungContainer.erweiterteBetreuungJA.keineKesbPlatzierung =
                true;
            betreuung.erweiterteBetreuungContainer =
                erweiterteBetreuungContainer;
            betreuung.institutionStammdaten = createInstitutionStammdaten(
                '1',
                TSBetreuungsangebotTyp.KITA
            );

            kind = new TSKindContainer();
            $stateParams = {
                betreuungNumber: '0',
                kindNumber: '0',
                betreuungsangebotTyp: TSBetreuungsangebotTyp.KITA
            };
            spyOn(gesuchModelManager, 'getKindToWorkWith').and.returnValue(
                kind
            );
            spyOn(
                gesuchModelManager,
                'convertKindNumberToKindIndex'
            ).and.returnValue(0);
            spyOn(
                gesuchModelManager,
                'convertBetreuungNumberToBetreuungIndex'
            ).and.returnValue(0);
            spyOn(gesuchModelManager, 'isNeuestesGesuch').and.returnValue(true);
            // model = betreuung;
            spyOn(gesuchModelManager, 'getBetreuungToWorkWith').and.callFake(
                () =>
                    // wenn betreuung view ihr model schon kopiert hat geben wir das zurueck, sonst sind wir noch im
                    // constructor der view und geben betreuung zurueck
                    betreuungView ? betreuungView.model : betreuung
            );
            const gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
            gesuchsperiode.id = '0621fb5d-a187-5a91-abaf-8a813c4d263a';
            spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(
                gesuchsperiode
            );
            spyOn(gesuchModelManager, 'getGemeinde').and.returnValue(
                TestDataUtil.createGemeindeParis()
            );
            gesuchModelManager.gemeindeKonfiguration =
                TestDataUtil.createGemeindeKonfiguration();
            $rootScope = $injector.get('$rootScope');
            authServiceRS = $injector.get('AuthServiceRS');
            spyOn(authServiceRS, 'isRole').and.returnValue(true);
            spyOn(authServiceRS, 'isOneOfRoles').and.returnValue(true);
            spyOn(authServiceRS, 'getPrincipal').and.returnValue(
                TestDataUtil.createSuperadmin()
            );
            spyOn(
                einstellungRS,
                'getAllEinstellungenBySystemCached'
            ).and.returnValue(of([]));
            spyOn(
                applicationPropertyRS,
                'getPublicPropertiesCached'
            ).and.resolveTo({});
            spyOn(einstellungRS, 'findEinstellung').and.returnValue(
                of(new TSEinstellung())
            );
            spyOn(
                institutionStammdatenRS,
                'getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde'
            ).and.returnValue($q.resolve([]));
            $translateMock = jasmine.createSpyObj<ITranslateService>(
                'ITranslateService',
                ['instant']
            );

            wizardStepManager = $injector.get('WizardStepManager');
            betreuungView = new BetreuungViewController(
                $state,
                gesuchModelManager,
                ebeguUtil,
                $rootScope,
                $injector.get('BerechnungsManager'),
                $injector.get('ErrorService'),
                authServiceRS,
                wizardStepManager,
                $stateParams,
                $injector.get('MitteilungRS'),
                $injector.get('DvDialog'),
                $injector.get('$log'),
                einstellungRS,
                $injector.get('GlobalCacheService'),
                $timeout,
                $translateMock,
                $injector.get('ApplicationPropertyRS'),
                mandantService,
                ebeguRestUtil
            );
        })
    );

    beforeEach(function (done) {
        betreuungView.$onInit();
        $rootScope.$apply();
        betreuungView.model = betreuung;
        kind.betreuungen = [betreuung];
        betreuungView.betreuungsangebot = TSBetreuungsangebotTyp.KITA;

        betreuungView.form = TestDataUtil.createDummyForm();
        betreuungView.minPensumSprachlicheIndikation = 40;
        // You can call any async task, when done() is called the test will begin
        setTimeout(() => {
            done();
        }, 100);
    });

    describe('API Usage', () => {
        describe('Object creation', () => {
            it('create an empty list of Betreuungspensen for a role different than Institution', () => {
                const myBetreuungView = new BetreuungViewController(
                    $state,
                    gesuchModelManager,
                    ebeguUtil,
                    $rootScope,
                    null,
                    null,
                    authServiceRS,
                    wizardStepManager,
                    $stateParams,
                    undefined,
                    undefined,
                    undefined,
                    undefined,
                    undefined,
                    $timeout,
                    undefined,
                    undefined,
                    mandantService,
                    ebeguRestUtil
                );
                myBetreuungView.model = betreuung;
                expect(myBetreuungView.getBetreuungspensen()).toBeDefined();
                expect(myBetreuungView.getBetreuungspensen().length).toEqual(0);
            });
        });
        describe('cancel existing object', () => {
            it('should not remove the kind and then go to betreuungen', () => {
                spyOn($state, 'go');
                spyOn(gesuchModelManager, 'removeBetreuungFromKind');
                betreuungView.cancel();

                expect(
                    gesuchModelManager.removeBetreuungFromKind
                ).not.toHaveBeenCalled();

                expect($state.go).toHaveBeenCalledWith(betreuungenState, {
                    gesuchId: ''
                });
            });
        });
        describe('cancel non-existing object', () => {
            it('should remove the betreuung from kind and then go to betreuungen', () => {
                spyOn($state, 'go');
                betreuungView.model.timestampErstellt = undefined;
                betreuungView.model.timestampErstellt = undefined;
                spyOn(gesuchModelManager, 'removeBetreuungFromKind');
                betreuungView.cancel();

                expect(
                    gesuchModelManager.removeBetreuungFromKind
                ).toHaveBeenCalled();

                expect($state.go).toHaveBeenCalledWith(betreuungenState, {
                    gesuchId: ''
                });
            });
        });
        describe('getInstitutionenSDList', () => {
            beforeEach(() => {
                gesuchModelManager
                    .getActiveInstitutionenForGemeindeList()
                    .push(
                        createInstitutionStammdaten(
                            '1',
                            TSBetreuungsangebotTyp.KITA
                        )
                    );
                gesuchModelManager
                    .getActiveInstitutionenForGemeindeList()
                    .push(
                        createInstitutionStammdaten(
                            '2',
                            TSBetreuungsangebotTyp.KITA
                        )
                    );
                gesuchModelManager
                    .getActiveInstitutionenForGemeindeList()
                    .push(
                        createInstitutionStammdaten(
                            '3',
                            TSBetreuungsangebotTyp.TAGESFAMILIEN
                        )
                    );
                gesuchModelManager
                    .getActiveInstitutionenForGemeindeList()
                    .push(
                        createInstitutionStammdaten(
                            '4',
                            TSBetreuungsangebotTyp.TAGESSCHULE
                        )
                    );
            });
            it('should return a list with 2 Institutions of type TSBetreuungsangebotTyp.KITA', () => {
                betreuungView.betreuungsangebot = {key: 'KITA', value: 'kita'};
                const list = betreuungView.getInstitutionenSDList();
                expect(list).toBeDefined();
                expect(list.length).toBe(2);
                expect(
                    list[0].institutionStammdatenBetreuungsgutscheine.iban
                ).toBe('1');
                expect(
                    list[1].institutionStammdatenBetreuungsgutscheine.iban
                ).toBe('2');
            });
        });

        /**
         * Some of these tests are usless and don't work anymore.
         */

        describe('submit', () => {
            it('submits all data of current Betreuung', () => {
                testSubmit($q.when({}), true);
            });
            it('submits but data are invalid and does not move forward', () => {
                testSubmit($q.reject(), false);
            });
        });
        describe('platzAbweisen()', () => {
            it('must change the status of the Betreuung to ABGEWIESEN and restore initial values of Betreuung', () => {
                spyOn(gesuchModelManager, 'saveBetreuung').and.returnValue(
                    $q.resolve(new TSBetreuung())
                );
                spyOn(gesuchModelManager, 'setBetreuungToWorkWith').and.stub();
                betreuungView.model.grundAblehnung = 'mein Grund';
                expect(
                    gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus
                ).toEqual(TSBetreuungsstatus.AUSSTEHEND);
                expect(betreuungView.model.datumAblehnung).toBeUndefined();

                betreuungView.platzAbweisen();

                // Status wird serverseitig gesetzt
                const betreuungToWorkWith =
                    gesuchModelManager.getBetreuungToWorkWith();
                expect(betreuungToWorkWith.betreuungsstatus).toEqual(
                    TSBetreuungsstatus.AUSSTEHEND
                );
                expect(betreuungToWorkWith.grundAblehnung).toEqual(
                    'mein Grund'
                );
                expect(betreuungToWorkWith.datumAblehnung).toEqual(
                    DateUtil.today()
                );

                expect(gesuchModelManager.saveBetreuung).toHaveBeenCalled();
            });
        });
        describe('platzAnfordern()', () => {
            it('must change the status of the Betreuung to WARTEN', () => {
                spyOn(gesuchModelManager, 'saveBetreuung').and.returnValue(
                    $q.resolve(new TSBetreuung())
                );
                betreuungView.model.vertrag = true;
                betreuungView.model.institutionStammdaten =
                    createInstitutionStammdaten(
                        '3',
                        TSBetreuungsangebotTyp.KITA
                    );
                // betreuung.timestampErstellt = undefined;
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.AUSSTEHEND;
                expect(
                    gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus
                ).toEqual(TSBetreuungsstatus.AUSSTEHEND);
                betreuungView.platzAnfordern();
                // Status wird serverseitig gesetzt
                expect(
                    gesuchModelManager.getBetreuungToWorkWith().betreuungsstatus
                ).toEqual(TSBetreuungsstatus.AUSSTEHEND);

                expect(gesuchModelManager.saveBetreuung).toHaveBeenCalled();
            });
        });
        describe('removeBetreuungspensum', () => {
            it('should remove the betreuungspensum from the list', () => {
                betreuungView.getBetreuungModel().betreuungspensumContainers =
                    [];
                betreuungView.createBetreuungspensum();
                expect(betreuungView.getBetreuungspensen().length).toEqual(1);
                betreuungView.removeBetreuungspensum(
                    betreuungView.getBetreuungspensen()[0]
                );
                expect(betreuungView.getBetreuungspensen().length).toEqual(0);
                betreuungView.createBetreuungspensum();
                expect(betreuungView.getBetreuungspensen().length).toEqual(1);
                betreuungView.removeBetreuungspensum(
                    betreuungView.getBetreuungspensen()[0]
                );
                expect(betreuungView.getBetreuungspensen().length).toEqual(0);
            });
        });
        describe('isMutationsmeldungAllowed', () => {
            const vorgaengerId = '111-222-333-444-555';

            it('should be false if the Gesuch is not a Mutation and is not verfuegt', () => {
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.BESTAETIGT;
                initGesuch(
                    TSAntragTyp.ERSTGESUCH,
                    TSAntragStatus.IN_BEARBEITUNG_JA,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(false);
            });
            it('should be true if the Gesuch is not a Mutation but the betreuung is in Status Verfuegt', () => {
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.VERFUEGT;
                initGesuch(
                    TSAntragTyp.ERSTGESUCH,
                    TSAntragStatus.VERFUEGT,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
            it('should be false if the Gesuch is not a Mutation and is in Status VERFUEGT but the betreuung is not in Status VERFUEGT', () => {
                // this case is actually not possible
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.BESTAETIGT;
                initGesuch(
                    TSAntragTyp.ERSTGESUCH,
                    TSAntragStatus.VERFUEGT,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(false);
            });
            it('should be false if the Gesuch is not a Mutation and is not in Status VERFUEGT but the betreuung is in Status VERFUEGT', () => {
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.VERFUEGT;
                initGesuch(
                    TSAntragTyp.ERSTGESUCH,
                    TSAntragStatus.VERFUEGEN,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(false);
            });
            it('should be true if the Gesuch is not gesperrtWegenBeschwerde though STV status and the betreuung is in Status VERFUEGT', () => {
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.VERFUEGT;
                initGesuch(
                    TSAntragTyp.ERSTGESUCH,
                    TSAntragStatus.PRUEFUNG_STV,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
            it('should be true if the Gesuch is a Mutation and the betreuung has a vorgaengerID', () => {
                betreuungView.model.vorgaengerId = vorgaengerId;
                initGesuch(
                    TSAntragTyp.MUTATION,
                    TSAntragStatus.IN_BEARBEITUNG_JA,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
            it('should be false if the Gesuch is a Mutation and the betreuung has no vorgaengerID, i.e. it is a new Betreuung', () => {
                betreuungView.model.vorgaengerId = undefined;
                initGesuch(
                    TSAntragTyp.MUTATION,
                    TSAntragStatus.IN_BEARBEITUNG_JA,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(false);
            });
            it('should be true if the Mutation is gesperrtWegenBeschwerde although the Betreuung has a vorgaengerId', () => {
                betreuungView.model.vorgaengerId = vorgaengerId;
                initGesuch(
                    TSAntragTyp.MUTATION,
                    TSAntragStatus.IN_BEARBEITUNG_JA,
                    true
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
            it('should be true if the Gesuch is a Mutation and is in Status VERFUEGEN but the betreuung is in Status VERFUEGT and has a vorgaengerId', () => {
                betreuungView.model.vorgaengerId = vorgaengerId;
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.VERFUEGT;
                initGesuch(
                    TSAntragTyp.MUTATION,
                    TSAntragStatus.VERFUEGEN,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
            it('should be true if the Gesuch is a Mutation and is in Status VERFUEGT and the betreuung is in Status VERFUEGT but has no vorgaengerId', () => {
                betreuungView.model.vorgaengerId = undefined;
                betreuungView.model.betreuungsstatus =
                    TSBetreuungsstatus.VERFUEGT;
                initGesuch(
                    TSAntragTyp.MUTATION,
                    TSAntragStatus.VERFUEGT,
                    false
                );
                expect(betreuungView.isMutationsmeldungAllowed()).toBe(true);
            });
        });
        describe('showInstitutionenList', () => {
            it('should showInstitutionenList and not showInstitutionenAsText for FALSCHE INSTITUTION', () => {
                // initGesuch(TSAntragTyp.ERSTGESUCH, TSAntragStatus.IN_BEARBEITUNG_JA, false);
                spyOn(
                    betreuungView,
                    'isTageschulenAnmeldungAktiv'
                ).and.returnValue(true);
                spyOn(betreuungView, 'isEnabled').and.returnValue(true);
                spyOn(betreuungView, 'isBetreuungsstatus').and.returnValue(
                    true
                );
                spyOn(betreuungView, 'isTagesschule').and.returnValue(true);

                expect(betreuungView.showInstitutionenList()).toBe(true);
                expect(betreuungView.showInstitutionenAsText()).toBe(false);
            });
            it('should showInstitutionenList and not showInstitutionenAsText for enabled TAGESSCHULE neue Gesuchsperiode', () => {
                // initGesuch(TSAntragTyp.ERSTGESUCH, TSAntragStatus.IN_BEARBEITUNG_JA, false);
                spyOn(
                    betreuungView,
                    'isTageschulenAnmeldungAktiv'
                ).and.returnValue(true);
                spyOn(betreuungView, 'isEnabled').and.returnValue(true);
                spyOn(betreuungView, 'isBetreuungsstatus').and.returnValue(
                    false
                );
                spyOn(betreuungView, 'isTagesschule').and.returnValue(true);

                expect(betreuungView.showInstitutionenList()).toBe(true);
                expect(betreuungView.showInstitutionenAsText()).toBe(false);
            });
            it('should not showInstitutionenList and showInstitutionenAsText for disabled TAGESSCHULE neue Gesuchsperiode', () => {
                // initGesuch(TSAntragTyp.ERSTGESUCH, TSAntragStatus.IN_BEARBEITUNG_JA, false);
                spyOn(
                    betreuungView,
                    'isTageschulenAnmeldungAktiv'
                ).and.returnValue(true);
                spyOn(betreuungView, 'isEnabled').and.returnValue(false);
                spyOn(betreuungView, 'isBetreuungsstatus').and.returnValue(
                    false
                );
                spyOn(betreuungView, 'isTagesschule').and.returnValue(true);

                expect(betreuungView.showInstitutionenList()).toBe(false);
                expect(betreuungView.showInstitutionenAsText()).toBe(true);
            });
        });
        describe('showPensumUnterschrittenCheckBox', () => {
            it('should not show checkbox if betreuungspensum List is undefined', () => {
                spyOn(betreuungView, 'getBetreuungspensen').and.returnValue(
                    undefined
                );
                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    false
                );
            });

            it('should not show checkbox if betreuungspensum List is empty', () => {
                spyOn(betreuungView, 'getBetreuungspensen').and.returnValue([]);
                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    false
                );
            });

            it('should not show checkbox if no PensumFachstellenList is undefined', () => {
                spyOn(betreuungView, 'getKindModel').and.returnValue(kind);
                spyOn(betreuungView, 'getBetreuungspensen').and.returnValue([
                    new TSBetreuungspensumContainer()
                ]);

                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    false
                );

                kind.kindJA = new TSKind();
                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    false
                );
            });

            it('should not show checkbox if no betreuungspensum less than 40% pensum', () => {
                const pensumFachstelleSozialeIndikation =
                    new TSPensumFachstelle();
                pensumFachstelleSozialeIndikation.integrationTyp =
                    TSIntegrationTyp.SPRACHLICHE_INTEGRATION;

                kind.kindJA = new TSKind();
                kind.kindJA.pensumFachstellen = [
                    pensumFachstelleSozialeIndikation
                ];

                const betreuungspensum = new TSBetreuungspensumContainer();
                betreuungspensum.betreuungspensumJA = new TSBetreuungspensum();
                betreuungspensum.betreuungspensumJA.pensum = 40;

                spyOn(betreuungView, 'getKindModel').and.returnValue(kind);
                spyOn(betreuungView, 'getBetreuungspensen').and.returnValue([
                    betreuungspensum
                ]);
                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    false
                );
            });

            it('should not show checkbox if betreuungspensum less than 40% pensum', () => {
                const pensumFachstelleSozialeIndikation =
                    new TSPensumFachstelle();
                pensumFachstelleSozialeIndikation.integrationTyp =
                    TSIntegrationTyp.SPRACHLICHE_INTEGRATION;
                kind.kindJA = new TSKind();
                kind.kindJA.pensumFachstellen = [
                    pensumFachstelleSozialeIndikation
                ];

                const betreuungspensum = new TSBetreuungspensumContainer();
                betreuungspensum.betreuungspensumJA = new TSBetreuungspensum();
                betreuungspensum.betreuungspensumJA.pensum = 30;

                spyOn(betreuungView, 'getKindModel').and.returnValue(kind);
                spyOn(betreuungView, 'getBetreuungspensen').and.returnValue([
                    betreuungspensum
                ]);
                expect(betreuungView.showPensumUnterschrittenCheckBox()).toBe(
                    true
                );
            });
        });

        it('should call addMesageAsError when betreuungsangebotTyp does not match', () => {
            // Setup the model to have a different betreuungsangebotTyp
            betreuungView.betreuungsangebot =
                TSBetreuungsangebotTyp.MITTAGSTISCH;
            betreuungView.model.vertrag = true;
            betreuungView.model.institutionStammdaten =
                createInstitutionStammdaten('2', TSBetreuungsangebotTyp.KITA);
            betreuungView.platzAnfordern();
            expect($translateMock.instant).toHaveBeenCalled();
            expect($translateMock.instant).toHaveBeenCalledWith(
                jasmine.stringMatching('ERROR_FALSCHE_ANGEBOT')
            );
        });
    });

    function initGesuch(
        typ: TSAntragTyp,
        status: TSAntragStatus,
        gesperrtWegenBeschwerde: boolean
    ): TSGesuch {
        const gesuch = new TSGesuch();
        gesuch.typ = typ;
        gesuch.status = status;
        gesuch.gesperrtWegenBeschwerde = gesperrtWegenBeschwerde;
        gesuch.gesuchsperiode = new TSGesuchsperiode();
        gesuch.gesuchsperiode.status = TSGesuchsperiodeStatus.AKTIV;
        spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
        return gesuch;
    }

    function createInstitutionStammdaten(
        iban: string,
        betAngTyp: TSBetreuungsangebotTyp
    ): TSInstitutionStammdaten {
        const instStam1 = new TSInstitutionStammdaten();
        instStam1.institutionStammdatenBetreuungsgutscheine =
            new TSInstitutionStammdatenBetreuungsgutscheine();
        instStam1.institutionStammdatenBetreuungsgutscheine.iban = iban;
        instStam1.betreuungsangebotTyp = betAngTyp;
        return instStam1;
    }

    /**
     * Das Parameter promiseResponse ist das Object das die Methode gesuchModelManager.saveBetreuung() zurueckgeben
     * muss. Wenn dieses eine Exception (reject) ist, muss der $state nicht geaendert werden und daher wird die Methode
     * $state.go()  nicht aufgerufen. Ansonsten wird sie mit  dem naechsten state 'gesuch.betreuungen' aufgerufen
     */
    function testSubmit(promiseResponse: any, moveToNextStep: boolean): void {
        betreuungView.model.vertrag = true;
        spyOn($state, 'go');
        spyOn(gesuchModelManager, 'saveBetreuung').and.returnValue(
            promiseResponse
        );
        spyOn(gesuchModelManager, 'setBetreuungToWorkWith').and.callFake(
            b => b
        );
        spyOn(
            gesuchModelManager,
            'updateVerguenstigungGewuenschtFlag'
        ).and.callFake(() => {});
        betreuungView.model.institutionStammdaten = createInstitutionStammdaten(
            '1',
            TSBetreuungsangebotTyp.KITA
        );

        betreuungView.platzAnfordern();
        $rootScope.$apply();

        expect(gesuchModelManager.saveBetreuung).toHaveBeenCalled();
        if (moveToNextStep) {
            expect($state.go).toHaveBeenCalledWith(betreuungenState, {
                gesuchId: ''
            });
        } else {
            expect($state.go).not.toHaveBeenCalled();
        }
    }
});

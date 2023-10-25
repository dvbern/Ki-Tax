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

import {BehaviorSubject, of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {MANDANTS} from '../../../app/core/constants/MANDANTS';
import {CORE_JS_MODULE} from '../../../app/core/core.angularjs.module';
import {InstitutionStammdatenRS} from '../../../app/core/service/institutionStammdatenRS.rest';
import {MandantService} from '../../../app/shared/services/mandant.service';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {ngServicesMock} from '../../../hybridTools/ngServicesMocks';
import {translationsMock} from '../../../hybridTools/translationsMock';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSFachstellenTyp} from '../../../models/enums/TSFachstellenTyp';
import {TSIntegrationTyp} from '../../../models/enums/TSIntegrationTyp';
import {TSKinderabzugTyp} from '../../../models/enums/TSKinderabzugTyp';
import {TSBetreuung} from '../../../models/TSBetreuung';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSGesuch} from '../../../models/TSGesuch';
import {TSKind} from '../../../models/TSKind';
import {TSKindContainer} from '../../../models/TSKindContainer';
import {TSPensumFachstelle} from '../../../models/TSPensumFachstelle';
import {EbeguRestUtil} from '../../../utils/EbeguRestUtil';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {IKindStateParams} from '../../gesuch.route';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {KindViewController} from './kindView';

function createEinstellungen(
    sozialeIntegrationBis = TSEinschulungTyp.VORSCHULALTER,
    sprachlicheIntegrationBis = TSEinschulungTyp.VORSCHULALTER,
    spracheAmtspracheDisabled = 'true',
    maxPensumAusserordentlicherAnspruch = '20',
    kinderabzugTyp = TSKinderabzugTyp.FKJV_2,
    anspruchUnabhaengigBeschaeftigungspensum = TSAnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING,
    zemisDisabled = 'true',
    fachstellenTyp = TSFachstellenTyp.BERN,
): TSEinstellung[] {
    return [
        new TSEinstellung(null, TSEinstellungKey.ZEMIS_DISABLED, zemisDisabled),
        new TSEinstellung(null, TSEinstellungKey.SPRACHE_AMTSPRACHE_DISABLED, spracheAmtspracheDisabled),
        new TSEinstellung(null,
            TSEinstellungKey.FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH,
            maxPensumAusserordentlicherAnspruch),
        new TSEinstellung(null, TSEinstellungKey.KINDERABZUG_TYP, kinderabzugTyp),
        new TSEinstellung(null,
            TSEinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
            anspruchUnabhaengigBeschaeftigungspensum),
        new TSEinstellung(null, TSEinstellungKey.FACHSTELLEN_TYP, fachstellenTyp),
        new TSEinstellung(null, TSEinstellungKey.FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, sozialeIntegrationBis),
        new TSEinstellung(null, TSEinstellungKey.SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE, sprachlicheIntegrationBis),
    ];
}

function createPensumFachstelle(integrationTyp: TSIntegrationTyp): TSPensumFachstelle {
    const pensumFachstelle = new TSPensumFachstelle();
    pensumFachstelle.integrationTyp = integrationTyp;
    return pensumFachstelle;
}

/* eslint-disable max-len */
describe('kindView', () => {

    let kindView: KindViewController;
    let gesuchModelManager: GesuchModelManager;
    let berechnungsManager: BerechnungsManager;
    let ebeguUtil: EbeguUtil;
    let $q: angular.IQService;
    let betreuung: TSBetreuung;
    let kind: TSKindContainer;
    let $rootScope: angular.IRootScopeService;
    let $httpBackend: angular.IHttpBackendService;
    let authServiceRS: AuthServiceRS;
    let wizardStepManager: WizardStepManager;
    let $stateParams: IKindStateParams;
    let $timeout: angular.ITimeoutService;
    let einstellungRS: EinstellungRS;
    let institutionStammdatenRS: InstitutionStammdatenRS;
    let mandantService: MandantService;
    let ebeguRestUtil: EbeguRestUtil;

    const einstellungen$ = new BehaviorSubject(createEinstellungen());
    const mandant$ = new BehaviorSubject(MANDANTS.BERN);

    beforeEach(angular.mock.module(CORE_JS_MODULE.name));

    beforeEach(angular.mock.module(ngServicesMock));

    beforeEach(angular.mock.module(translationsMock));

    beforeEach(angular.mock.inject($injector => {
        gesuchModelManager = $injector.get('GesuchModelManager');
        berechnungsManager = $injector.get('BerechnungsManager');
        ebeguUtil = $injector.get('EbeguUtil');
        $httpBackend = $injector.get('$httpBackend');
        $q = $injector.get('$q');
        $stateParams = $injector.get('$stateParams');
        $timeout = $injector.get('$timeout');
        einstellungRS = $injector.get('EinstellungRS');
        institutionStammdatenRS = $injector.get('InstitutionStammdatenRS');
        mandantService = $injector.get('MandantService');
        ebeguRestUtil = $injector.get('EbeguRestUtil');
        const applicationPropertyRS = $injector.get('ApplicationPropertyRS');
        // they always need to be mocked
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        TestDataUtil.mockDefaultGesuchModelManagerHttpCalls($httpBackend);
        TestDataUtil.mockLazyGesuchModelManagerHttpCalls($httpBackend);

        spyOn(einstellungRS, 'getAllEinstellungenBySystemCached').and.returnValue(einstellungen$.asObservable());
        spyOnProperty(mandantService, 'mandant$', 'get').and.returnValue(mandant$);
        const gesuch = new TSGesuch();
        kind = new TSKindContainer();
        kind.kindJA = new TSKind();
        gesuch.kindContainers = [kind];
        $stateParams = {kindNumber: '0'};
        spyOn(gesuchModelManager, 'getGesuch').and.returnValue(gesuch);
        spyOn(gesuchModelManager, 'getKindToWorkWith').and.returnValue(kind);
        spyOn(gesuchModelManager, 'convertKindNumberToKindIndex').and.returnValue(0);
        spyOn(gesuchModelManager, 'convertBetreuungNumberToBetreuungIndex').and.returnValue(0);
        spyOn(gesuchModelManager, 'isNeuestesGesuch').and.returnValue(true);
        spyOn(applicationPropertyRS, 'getPublicPropertiesCached').and.resolveTo(({}));
        spyOn(einstellungRS, 'findEinstellung').and.returnValue(of(new TSEinstellung()));
        // model = betreuung;
        const gesuchsperiode = TestDataUtil.createGesuchsperiode20162017();
        gesuchsperiode.id = '0621fb5d-a187-5a91-abaf-8a813c4d263a';
        spyOn(gesuchModelManager, 'getGesuchsperiode').and.returnValue(gesuchsperiode);
        spyOn(gesuchModelManager, 'getGemeinde').and.returnValue(TestDataUtil.createGemeindeParis());
        gesuchModelManager.gemeindeKonfiguration = TestDataUtil.createGemeindeKonfiguration();
        $rootScope = $injector.get('$rootScope');
        authServiceRS = $injector.get('AuthServiceRS');

        wizardStepManager = $injector.get('WizardStepManager');
        kindView = new KindViewController(
            $stateParams,
            gesuchModelManager,
            berechnungsManager,
            $rootScope,
            $injector.get('ErrorService'),
            wizardStepManager,
            $q,
            $injector.get('$translate'),
            $timeout,
            einstellungRS,
            $injector.get('GlobalCacheService'),
            authServiceRS,
            ebeguRestUtil,
            mandantService,
            $injector.get('FjkvKinderabzugExchangeService'),
            $injector.get('HybridFormBridgeService'),
        );
    }));

    beforeEach(function(done) {
        kindView.$onInit();
        $rootScope.$apply();
        kindView.model = kind;
        kind.betreuungen = [betreuung];

        kindView.form = TestDataUtil.createDummyForm();
        // You can call any async task, when done() is called the test will begin
        setTimeout(() => {
            done();
        }, 100);
    });

    afterEach(() => {
        kindView.$onDestroy();
    });

    describe('pensumFachstellen validity', () => {
        describe('Sprachliche Integration', () => {
            describe('BERN', () => {
                beforeEach(() => {
                    mandant$.next(MANDANTS.BERN);
                    kind.kindJA.pensumFachstellen.push(createPensumFachstelle(TSIntegrationTyp.SPRACHLICHE_INTEGRATION));
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is KINDERGARTEN1', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.KINDERGARTEN1));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is KINDERGARTEN2', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.KINDERGARTEN2));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should not be valid when kind is in KINDERGARTEN1 and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(1);
                    expect(errors[0].error).toMatch('Das Kind ist nicht mehr im Vorschulalter');
                });
                it('should be valid when kind is in KINDERGARTEN1 and einstellung is KINDERGARTEN1', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.KINDERGARTEN1));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in KINDERGARTEN1 and einstellung is KINDERGARTEN2', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.KINDERGARTEN2));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
            });
            describe('LUZERN', () => {
                beforeEach(() => {
                    mandant$.next(MANDANTS.LUZERN);
                    kind.kindJA.pensumFachstellen.push(createPensumFachstelle(TSIntegrationTyp.SPRACHLICHE_INTEGRATION));
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is FREIWILLIGER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is OBLIGATORISCHER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should not be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(1);
                    expect(errors[0].error).toMatch('Das Kind ist nicht mehr im Vorschulalter');
                });
                it('should be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is FREIWILLIGER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is OBLIGATORISCHER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
            });
        });
        describe('Soziale Integration', () => {
            describe('BERN', () => {
                beforeEach(() => {
                    mandant$.next(MANDANTS.BERN);
                    kind.kindJA.pensumFachstellen.push(createPensumFachstelle(TSIntegrationTyp.SOZIALE_INTEGRATION));
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is KINDERGARTEN1', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.KINDERGARTEN1));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is KINDERGARTEN2', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.KINDERGARTEN2));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should not be valid when kind is in KINDERGARTEN1 and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(1);
                    expect(errors[0].error).toMatch('Eine Fachstelle zur sozialen Indikation');
                });
                it('should be valid when kind is in KINDERGARTEN1 and einstellung is KINDERGARTEN1', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.KINDERGARTEN1));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in KINDERGARTEN1 and einstellung is KINDERGARTEN2', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.KINDERGARTEN1;
                    einstellungen$.next(createEinstellungen(TSEinschulungTyp.KINDERGARTEN2));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
            });
        });
        describe('Zusatzleistung Integration', () => {
            describe('LUZERN', () => {
                beforeEach(() => {
                    mandant$.next(MANDANTS.LUZERN);
                    kind.kindJA.pensumFachstellen.push(createPensumFachstelle(TSIntegrationTyp.ZUSATZLEISTUNG_INTEGRATION));
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is FREIWILLIGER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in VORSCHULALTER and einstellung is OBLIGATORISCHER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.VORSCHULALTER;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should not be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is VORSCHULALTER', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.VORSCHULALTER));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(1);
                    expect(errors[0].error).toMatch('Das Kind ist nicht mehr im Vorschulalter');
                });
                it('should be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is FREIWILLIGER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
                it('should be valid when kind is in FREIWILLIGER_KINDERGARTEN and einstellung is OBLIGATORISCHER_KINDERGARTEN', () => {
                    kind.kindJA.einschulungTyp = TSEinschulungTyp.FREIWILLIGER_KINDERGARTEN;
                    einstellungen$.next(createEinstellungen(undefined, TSEinschulungTyp.OBLIGATORISCHER_KINDERGARTEN));
                    const errors = kindView.checkFachstellenValidity();
                    expect(errors.length).toBe(0);
                });
            });
        });
    });
});

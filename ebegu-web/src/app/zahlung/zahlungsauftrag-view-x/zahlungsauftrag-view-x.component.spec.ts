import {CurrencyPipe} from '@angular/common';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../../core/service/zahlungRS.rest';
import {StateStoreService} from '../../shared/services/state-store.service';
import {SharedModule} from '../../shared/shared.module';

import {ZahlungsauftragViewXComponent} from './zahlungsauftrag-view-x.component';

describe('ZahlungsauftragViewXComponent', () => {
    let component: ZahlungsauftragViewXComponent;
    let fixture: ComponentFixture<ZahlungsauftragViewXComponent>;

    const zahlungRSSpy = jasmine.createSpyObj(ZahlungRS.name, ['getAllZahlungsauftraege']);
    const stateServiceSpy = jasmine.createSpyObj(StateService.name, ['go']);
    const downloadResSpy = jasmine.createSpyObj(DownloadRS.name, ['getAccessTokenDokument']);
    const appPropSpy = jasmine.createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['getAllowedMimetypes', 'isZahlungenTestMode']);
    const reportRSSpy = jasmine.createSpyObj(ReportRS.name, ['']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$', 'isOneOfRoles']);
    const gemeindeRSSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name, ['getGemeindenForPrincipal$', 'getGemeindenWithMahlzeitenverguenstigungForBenutzer']);
    const stateStoreSpy = jasmine.createSpyObj<StateStoreService>(StateStoreService.name, ['get', 'has']);
    const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name, ['params']);
    const currencySpy = jasmine.createSpyObj(CurrencyPipe.name, ['']);
    const transitionSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name, ['onStart']);
    const errorServiceSpy = jasmine.createSpyObj(ErrorService.name);

    uiRouterGlobalsSpy.params = {} as any;
    authServiceSpy.principal$ = of(new TSBenutzer());
    authServiceSpy.isOneOfRoles.and.returnValue(false);
    gemeindeRSSpy.getGemeindenForPrincipal$.and.returnValue(of([]));
    gemeindeRSSpy.getGemeindenWithMahlzeitenverguenstigungForBenutzer.and.resolveTo([]);
    appPropSpy.isZahlungenTestMode.and.resolveTo(false);
    stateStoreSpy.has.and.returnValue(false);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule, BrowserAnimationsModule],
            declarations: [ZahlungsauftragViewXComponent],
            providers: [
                {provide: ZahlungRS, useValue: zahlungRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: DownloadRS, useValue: downloadResSpy},
                {provide: ApplicationPropertyRS, useValue: appPropSpy},
                {provide: ReportRS, useValue: reportRSSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: StateStoreService, useValue: stateStoreSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobalsSpy},
                {provide: CurrencyPipe, useValue: currencySpy},
                {provide: TransitionService, useValue: transitionSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ZahlungsauftragViewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

import {CurrencyPipe} from '@angular/common';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, TransitionService, UIRouterGlobals} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TSBenutzer} from '../../../models/TSBenutzer';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {ReportRS} from '../../core/service/reportRS.rest';
import {ZahlungRS} from '../../core/service/zahlungRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {ZahlungviewXComponent} from './zahlungview-x.component';

describe('ZahlungviewXComponent', () => {
    let component: ZahlungviewXComponent;
    let fixture: ComponentFixture<ZahlungviewXComponent>;

    const zahlungRSSpy = jasmine.createSpyObj(ZahlungRS.name, ['getAllZahlungsauftraege']);
    const stateServiceSpy = jasmine.createSpyObj(StateService.name, ['go']);
    const downloadResSpy = jasmine.createSpyObj(DownloadRS.name, ['getAccessTokenDokument']);
    const reportRSSpy = jasmine.createSpyObj(ReportRS.name, ['']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['principal$', 'isOneOfRoles']);
    const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name, ['params']);
    const currencySpy = jasmine.createSpyObj(CurrencyPipe.name, ['']);
    const transitionSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name, ['onStart']);

    authServiceSpy.principal$ = of(new TSBenutzer());

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule, BrowserAnimationsModule],
            declarations: [ZahlungviewXComponent],
            providers: [
                {provide: ZahlungRS, useValue: zahlungRSSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: DownloadRS, useValue: downloadResSpy},
                {provide: ReportRS, useValue: reportRSSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobalsSpy},
                {provide: CurrencyPipe, useValue: currencySpy},
                {provide: TransitionService, useValue: transitionSpy},
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ZahlungviewXComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

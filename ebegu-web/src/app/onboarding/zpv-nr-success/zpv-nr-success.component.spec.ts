import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {SharedModule} from '../../shared/shared.module';

import {ZpvNrSuccessComponent} from './zpv-nr-success.component';

describe('ZpvNrSuccessComponent', () => {
    let component: ZpvNrSuccessComponent;
    let fixture: ComponentFixture<ZpvNrSuccessComponent>;

    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['getPrincipal']);
    const gesuchRSSpy = jasmine.createSpyObj<GesuchRS>(GesuchRS.name, ['findGesuchOfGesuchsteller']);
    const uiRouterGlobalsSpy = jasmine.createSpyObj<UIRouterGlobals>(UIRouterGlobals.name,
        ['params']);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule
            ],
            declarations: [ZpvNrSuccessComponent],
            providers: [
                {provide: AuthServiceRS, useValue: authServiceSpy},
                {provide: GesuchRS, useValue: gesuchRSSpy},
                {provide: UIRouterGlobals, useValue: uiRouterGlobalsSpy}
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ZpvNrSuccessComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

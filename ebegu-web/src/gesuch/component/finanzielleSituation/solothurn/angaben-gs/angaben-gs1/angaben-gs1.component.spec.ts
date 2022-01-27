import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../../hybridTools/mockUpgradedComponent';
import {NgGesuchModule} from '../../../../../ng-gesuch.module';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {SolothurnFinSitTestHelpers} from '../../SolothurnFinSitTestHelpers';

import {AngabenGs1Component} from './angaben-gs1.component';

describe('AngabenGs1Component', () => {
    let component: AngabenGs1Component;
    let fixture: ComponentFixture<AngabenGs1Component>;
    const gesuchModelManagerSpy = SolothurnFinSitTestHelpers.createGesuchModelManagerMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NgGesuchModule
            ],
            declarations: [AngabenGs1Component],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptGesuchModelManager()
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(SolothurnFinSitTestHelpers.createGesuch());
        fixture = TestBed.createComponent(AngabenGs1Component);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

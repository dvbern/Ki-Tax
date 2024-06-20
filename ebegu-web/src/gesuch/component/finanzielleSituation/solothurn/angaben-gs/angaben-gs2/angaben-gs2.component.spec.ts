import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../../hybridTools/mockUpgradedDirective';
import {GesuchModelManager} from '../../../../../service/gesuchModelManager';
import {FinanzielleSituationSolothurnService} from '../../finanzielle-situation-solothurn.service';
import {SolothurnFinSitTestHelpers} from '../../SolothurnFinSitTestHelpers';

import {AngabenGs2Component} from './angaben-gs2.component';

const gesuchModelManagerSpy =
    SolothurnFinSitTestHelpers.createGesuchModelManagerMock();

describe('AngabenGs2Component', () => {
    let component: AngabenGs2Component;
    let fixture: ComponentFixture<AngabenGs2Component>;
    const finSitSolothurnServiceMock =
        SolothurnFinSitTestHelpers.createFinSitSolothurnServiceMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AngabenGs2Component],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptGesuchModelManager(),
                {
                    provide: FinanzielleSituationSolothurnService,
                    useValue: finSitSolothurnServiceMock
                },
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptFinSitSolothurnServiceMock()
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(
            SolothurnFinSitTestHelpers.createGesuch()
        );
        fixture = TestBed.createComponent(AngabenGs2Component);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {FinanzielleSituationSolothurnService} from '../finanzielle-situation-solothurn.service';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';

import {FinanzielleSituationStartSolothurnComponent} from './finanzielle-situation-start-solothurn.component';

describe('FinanzielleSituationStartSolothurnComponent', () => {
    let component: FinanzielleSituationStartSolothurnComponent;
    let fixture: ComponentFixture<FinanzielleSituationStartSolothurnComponent>;
    const gesuchModelManagerSpy = SolothurnFinSitTestHelpers.createGesuchModelManagerMock();
    const finSitSolothurnServiceMock = SolothurnFinSitTestHelpers.createFinSitSolothurnServiceMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FinanzielleSituationStartSolothurnComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptGesuchModelManager(),
                {provide: FinanzielleSituationSolothurnService, useValue: finSitSolothurnServiceMock},
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptFinSitSolothurnServiceMock(),
            ],
            imports: [
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        gesuchModelManagerSpy.getGesuch.and.returnValue(SolothurnFinSitTestHelpers.createGesuch());
        fixture = TestBed.createComponent(FinanzielleSituationStartSolothurnComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

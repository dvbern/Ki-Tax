import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';

import {SteuerveranlagungErhaltenComponent} from './steuerveranlagung-erhalten.component';

describe('SteuerveranlagungErhaltenComponent', () => {
    let component: SteuerveranlagungErhaltenComponent;
    let fixture: ComponentFixture<SteuerveranlagungErhaltenComponent>;
    const gesuchModelmanagerSpy =
        SolothurnFinSitTestHelpers.createGesuchModelManagerMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SteuerveranlagungErhaltenComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelmanagerSpy},
                {provide: NgForm, useValue: new NgForm([], [])},
                ...SolothurnFinSitTestHelpers.getMockProvidersExceptGesuchModelManager()
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SteuerveranlagungErhaltenComponent);
        component = fixture.componentInstance;
        component.model =
            SolothurnFinSitTestHelpers.createFinanzModel().finanzielleSituationContainerGS1;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

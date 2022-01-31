import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';

import {SteuerveranlagungGemeinsamComponent} from './steuerveranlagung-gemeinsam.component';

describe('SteuerveranlagungGemeinsamComponent', () => {
    let component: SteuerveranlagungGemeinsamComponent;
    let fixture: ComponentFixture<SteuerveranlagungGemeinsamComponent>;
    const gesuchModelManagerSpy = SolothurnFinSitTestHelpers.createGesuchModelManagerMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SteuerveranlagungGemeinsamComponent],
            providers: [
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy},
                {provide: NgForm, useValue: new NgForm([], [])},
            ],
            imports: [
                SharedModule,
            ],
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SteuerveranlagungGemeinsamComponent);
        component = fixture.componentInstance;
        component.model = SolothurnFinSitTestHelpers.createFinanzModel();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

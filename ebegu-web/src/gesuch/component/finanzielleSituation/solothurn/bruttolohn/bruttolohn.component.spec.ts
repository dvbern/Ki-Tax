import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedDirective';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';

import {BruttolohnComponent} from './bruttolohn.component';

describe('BruttolohnComponent', () => {
    let component: BruttolohnComponent;
    let fixture: ComponentFixture<BruttolohnComponent>;
    const gesuchModelManagerSpy =
        SolothurnFinSitTestHelpers.createGesuchModelManagerMock();

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BruttolohnComponent],
            providers: [
                {provide: NgForm, useValue: new NgForm([], [])},
                {provide: GesuchModelManager, useValue: gesuchModelManagerSpy}
            ],
            imports: [SharedModule]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BruttolohnComponent);
        component = fixture.componentInstance;
        component.model =
            SolothurnFinSitTestHelpers.createFinanzModel().finanzielleSituationContainerGS1;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

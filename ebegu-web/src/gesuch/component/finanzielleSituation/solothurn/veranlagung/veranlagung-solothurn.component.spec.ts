import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgForm} from '@angular/forms';
import {SharedModule} from '../../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../../hybridTools/mockUpgradedComponent';
import {SolothurnFinSitTestHelpers} from '../SolothurnFinSitTestHelpers';

import {VeranlagungSolothurnComponent} from './veranlagung-solothurn.component';

describe('VeranlagungSolothurnComponent', () => {
    let component: VeranlagungSolothurnComponent;
    let fixture: ComponentFixture<VeranlagungSolothurnComponent>;
    const formMock = new NgForm([], []);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [VeranlagungSolothurnComponent],
            providers: [
                {provide: NgForm, useValue: formMock}
            ],
            imports: [
                SharedModule
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(VeranlagungSolothurnComponent);
        component = fixture.componentInstance;
        component.model = SolothurnFinSitTestHelpers.createFinanzModel().finanzielleSituationContainerGS1;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

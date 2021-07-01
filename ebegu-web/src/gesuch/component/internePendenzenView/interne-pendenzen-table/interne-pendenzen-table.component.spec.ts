import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SharedModule} from '../../../../app/shared/shared.module';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';

import {InternePendenzenTableComponent} from './interne-pendenzen-table.component';

describe('InternePendenzenTableComponent', () => {
    let component: InternePendenzenTableComponent;
    let fixture: ComponentFixture<InternePendenzenTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [InternePendenzenTableComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(InternePendenzenTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

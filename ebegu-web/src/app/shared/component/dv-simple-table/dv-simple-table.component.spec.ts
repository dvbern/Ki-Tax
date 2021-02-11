import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SHARED_MODULE_OVERRIDES} from '../../../../hybridTools/mockUpgradedComponent';
import {SharedModule} from '../../shared.module';

import {DvSimpleTableComponent} from './dv-simple-table.component';

describe('DvSimpleTableComponent', () => {
    let component: DvSimpleTableComponent;
    let fixture: ComponentFixture<DvSimpleTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SharedModule],
            declarations: [DvSimpleTableComponent]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DvSimpleTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

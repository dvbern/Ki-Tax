import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DvOnboardingComponent} from './dv-onboarding.component';

describe('DvOnboardingComponent', () => {
    let component: DvOnboardingComponent;
    let fixture: ComponentFixture<DvOnboardingComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [DvOnboardingComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(DvOnboardingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

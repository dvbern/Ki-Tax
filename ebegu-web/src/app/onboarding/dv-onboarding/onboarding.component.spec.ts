import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingComponent} from './onboarding.component';
import createSpyObj = jasmine.createSpyObj;

describe('OnboardingComponent', () => {
    let component: OnboardingComponent;
    let fixture: ComponentFixture<OnboardingComponent>;

    const gemeindeRSSpy = createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAllGemeinden']);

    beforeEach(async(() => {
        gemeindeRSSpy.getAllGemeinden.and.returnValue([]);

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            declarations: [OnboardingComponent],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(OnboardingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load all Gemeinden', () => {
        expect(gemeindeRSSpy.getAllGemeinden).toHaveBeenCalled();
    });
});

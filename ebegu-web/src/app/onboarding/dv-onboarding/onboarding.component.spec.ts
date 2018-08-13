import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingComponent} from './onboarding.component';
import createSpyObj = jasmine.createSpyObj;

describe('OnboardingComponent', () => {
    let component: OnboardingComponent;
    let fixture: ComponentFixture<OnboardingComponent>;

    const gemeindeRSSpy = createSpyObj<GemeindeRS>(GemeindeRS.name, ['getAllGemeinden']);
    const applicationPropertyRSSpy = createSpyObj<ApplicationPropertyRS>(ApplicationPropertyRS.name, ['isDummyMode']);

    beforeEach(async(() => {
        gemeindeRSSpy.getAllGemeinden.and.returnValue(of([]).toPromise());
        applicationPropertyRSSpy.isDummyMode.and.returnValue(of(true).toPromise());

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true})
            ],
            declarations: [OnboardingComponent],
            providers: [
                {provide: GemeindeRS, useValue: gemeindeRSSpy},
                {provide: ApplicationPropertyRS, useValue: applicationPropertyRSSpy},
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

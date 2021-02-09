import {ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService, TransitionService} from '@uirouter/angular';

import {TagesschulenUiViewComponent} from './tagesschulen-ui-view.component';

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['go']);

const transitionServiceSpy = jasmine.createSpyObj<TransitionService>(TransitionService.name,
    ['onSuccess']);

describe('TagesschulenUiViewComponent', () => {
    let component: TagesschulenUiViewComponent;
    let fixture: ComponentFixture<TagesschulenUiViewComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TagesschulenUiViewComponent],
            providers: [
                {provide: StateService, useValue: stateServiceSpy},
                {provide: TransitionService, useValue: transitionServiceSpy},
            ],
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TagesschulenUiViewComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

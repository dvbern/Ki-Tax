import {HttpClientModule} from '@angular/common/http';
import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlContainer, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {StateService, UIRouterModule} from '@uirouter/angular';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {WindowRef} from '../../core/service/windowRef.service';
import {MaterialModule} from '../../shared/material.module';

import {GemeindeAntraegeComponent} from './gemeinde-antraege.component';

const gesuchPeriodeSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
    ['findGesuchsperiode', 'getAllActiveGesuchsperioden']);

const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name,
    ['reload']);

const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name,
    ['addMesageAsError']);

const controlContainerSpy = jasmine.createSpyObj<ControlContainer>(ControlContainer.name,
    ['path']);

// We mock the dv loading buttondirective to make the setup easier since these are unit tests
@Directive({
    selector: '[dvLoadingButtonX]',
})
export class MockDvLoadingButtonXDirective {

    @Input() public type: any;
    @Input() public delay: any;
    @Input() public buttonClass: string;
    @Input() public forceWaitService: any;
    @Input() public ariaLabel: any;
    @Input() public buttonDisabled: any;
    @Output() public readonly buttonClick: EventEmitter<any> = new EventEmitter<any>();

}

describe('GemeindeAntraegeComponent', () => {
    let component: GemeindeAntraegeComponent;
    let fixture: ComponentFixture<GemeindeAntraegeComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                HttpClientModule,
                UIRouterModule,
                ReactiveFormsModule,
                FormsModule,
                MaterialModule,
                TranslateModule.forRoot(),
                UpgradeModule,
                BrowserAnimationsModule,
            ],
            declarations: [GemeindeAntraegeComponent, MockDvLoadingButtonXDirective],
            providers: [
                WindowRef,
                {provide: GesuchsperiodeRS, useValue: gesuchPeriodeSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ControlContainer, useValue: controlContainerSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
            ],
        })
            .compileComponents();

        gesuchPeriodeSpy.getAllActiveGesuchsperioden.and.returnValue(Promise.resolve([]));
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(GemeindeAntraegeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

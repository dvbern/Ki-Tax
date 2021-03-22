/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import {HttpClientModule} from '@angular/common/http';
import {Directive, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlContainer, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {UpgradeModule} from '@angular/upgrade/static';
import {TranslateModule} from '@ngx-translate/core';
import {StateService, UIRouterModule} from '@uirouter/angular';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBenutzer} from '../../../models/TSBenutzer';
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

const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name,
    ['isOneOfRoles', 'principal$']);

const user = new TSBenutzer();

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

    authServiceSpy.principal$ = of(user) as any;

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
                {provide: AuthServiceRS, useValue: authServiceSpy}
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

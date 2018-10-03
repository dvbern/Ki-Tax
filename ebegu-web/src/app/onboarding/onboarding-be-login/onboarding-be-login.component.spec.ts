/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {Transition, UIRouterModule} from '@uirouter/angular';
import {SharedModule} from '../../shared/shared.module';

import {OnboardingBeLoginComponent} from './onboarding-be-login.component';
import createSpyObj = jasmine.createSpyObj;

describe('OnboardingBeLoginComponent', () => {
    let component: OnboardingBeLoginComponent;
    let fixture: ComponentFixture<OnboardingBeLoginComponent>;

    const transitionSpy = createSpyObj<Transition>(Transition.name, ['params']);
    const gemeindeId = '1';

    beforeEach(async(() => {
        transitionSpy.params.and.returnValue({gemeindeId});

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                UIRouterModule.forRoot({useHash: true}),
            ],
            declarations: [OnboardingBeLoginComponent],
            providers: [
                {provide: Transition, useValue: transitionSpy},

            ],
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(OnboardingBeLoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should read a gemeindeId state param', () => {
        expect(transitionSpy.params).toHaveBeenCalled();
    });
});

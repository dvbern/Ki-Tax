/*
 * AGPL File-Header
 *
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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib-esm';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import ErrorService from '../../core/errors/service/ErrorService';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeHeaderComponent} from './gemeinde-header.component';

describe('GemeindeHeaderComponent', () => {

    const url = 'http://logo.png';
    const transitionTo: StateDeclaration = {name: 'gemeinde.edit'};
    let component: GemeindeHeaderComponent;
    let fixture: ComponentFixture<GemeindeHeaderComponent>;

    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['uploadLogoImage', 'getLogoUrl']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['to']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, ['isOneOfRoles']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [
                GemeindeHeaderComponent,
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        gemeindeServiceSpy.getLogoUrl.and.returnValue(url);
        transitionSpy.to.and.returnValue(transitionTo);
    }));

    beforeEach(async(() => {
        fixture = TestBed.createComponent(GemeindeHeaderComponent);
        component = fixture.componentInstance;
        component.gemeinde = TestDataUtil.createGemeindeBern();
        fixture.detectChanges();
    }));

    it('should create', async(() => {
        expect(component).toBeTruthy();
    }));

    it('should set LogoUrl', async(() => {
        component.logoImageUrl$.subscribe(
            next => expect(next).toBe(url),
            () => {},
        );
    }));

    describe('isEditionMode', () => {
        it('should be true if transition.to is view.gemeinde', async(() => {
            transitionTo.name = 'gemeinde.edit';
            component.ngOnInit();
            expect(component.isEditionMode()).toBe(true);
        }));
        it('should be false if transition.to is NOT view.gemeinde', async(() => {
            transitionTo.name = 'otherTransition';
            component.ngOnInit();
            expect(component.isEditionMode()).toBe(false);
        }));
    });
});

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

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {waitForAsync, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {TestDataUtil} from '../../../utils/TestDataUtil.spec';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {GesuchsperiodeRS} from '../../core/service/gesuchsperiodeRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {GemeindeModule} from '../gemeinde.module';
import {EditGemeindeComponent} from './edit-gemeinde.component';

describe('EditGemeindeComponent', () => {

    let component: EditGemeindeComponent;
    let fixture: ComponentFixture<EditGemeindeComponent>;

    const gemeindeServiceSpy = jasmine.createSpyObj<GemeindeRS>(GemeindeRS.name,
        ['getGemeindenForPrincipal$', 'findGemeinde']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const gesuchsperiodeServiceSpy = jasmine.createSpyObj<GesuchsperiodeRS>(GesuchsperiodeRS.name,
        ['getAllGesuchsperioden']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const authServiceSpy = jasmine.createSpyObj<AuthServiceRS>(AuthServiceRS.name, {
        isOneOfRoles: true,
    });

    beforeEach(waitForAsync(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
                GemeindeModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: GemeindeRS, useValue: gemeindeServiceSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: AuthServiceRS, useValue: authServiceSpy},
            ],
            declarations: [
            ],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();

        gemeindeServiceSpy.getGemeindenForPrincipal$.and.returnValue(of(
            [TestDataUtil.createGemeindeParis(), TestDataUtil.createGemeindeLondon()]));
        transitionSpy.params.and.returnValue({});
        transitionSpy.from.and.returnValue({});
        gesuchsperiodeServiceSpy.getAllGesuchsperioden.and.returnValue(Promise.resolve([]));
    }));

    beforeEach(waitForAsync(() => {
        fixture = TestBed.createComponent(EditGemeindeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', waitForAsync(() => {
        expect(component).toBeTruthy();
    }));
});

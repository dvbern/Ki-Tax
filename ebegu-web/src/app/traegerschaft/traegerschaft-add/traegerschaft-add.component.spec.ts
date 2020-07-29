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
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import {ErrorService} from '../../core/errors/service/ErrorService';
import {BenutzerRS} from '../../core/service/benutzerRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {I18nServiceRSRest} from '../../i18n/services/i18nServiceRS.rest';
import {MaterialModule} from '../../shared/material.module';
import {SharedModule} from '../../shared/shared.module';
import {TraegerschaftModule} from '../traegerschaft.module';

import {TraegerschaftAddComponent} from './traegerschaft-add.component';

describe('TraegerschaftAddComponent', () => {

    let component: TraegerschaftAddComponent;
    let fixture: ComponentFixture<TraegerschaftAddComponent>;

    const traegerschaftRSSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name,
        ['findTraegerschaft']);
    const transitionSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params', 'from']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const i18nServiceSpy = jasmine
        .createSpyObj<I18nServiceRSRest>(I18nServiceRSRest.name, ['extractPreferredLanguage']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['removeBenutzer']);

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
                MaterialModule,
                TraegerschaftModule,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                {provide: Transition, useValue: transitionSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: TraegerschaftRS, useValue: traegerschaftRSSpy},
                {provide: I18nServiceRSRest, useValue: i18nServiceSpy},
                {provide: BenutzerRS, useValue: benutzerServiceSpy},
            ],
            declarations: [],
        }).overrideModule(SharedModule, SHARED_MODULE_OVERRIDES,
        ).compileComponents();

        transitionSpy.params.and.returnValue({});
        transitionSpy.from.and.returnValue({});
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TraegerschaftAddComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

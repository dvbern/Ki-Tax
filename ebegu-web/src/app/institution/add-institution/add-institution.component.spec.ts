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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {StateService, Transition} from '@uirouter/core';
import {of} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {SHARED_MODULE_OVERRIDES} from '../../../hybridTools/mockUpgradedComponent';
import TestDataUtil from '../../../utils/TestDataUtil.spec';
import ErrorService from '../../core/errors/service/ErrorService';
import BenutzerRS from '../../core/service/benutzerRS.rest';
import GesuchsperiodeRS from '../../core/service/gesuchsperiodeRS.rest';
import {InstitutionRS} from '../../core/service/institutionRS.rest';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';
import {SharedModule} from '../../shared/shared.module';
import {AddInstitutionComponent} from './add-institution.component';

describe('AddInstitutionComponent', () => {

    let component: AddInstitutionComponent;
    let fixture: ComponentFixture<AddInstitutionComponent>;

    const insitutionServiceSpy = jasmine.createSpyObj<InstitutionRS>(InstitutionRS.name,
        ['getInstitutionenForCurrentBenutzer']);
    const errorServiceSpy = jasmine.createSpyObj<ErrorService>(ErrorService.name, ['getErrors']);
    const benutzerServiceSpy = jasmine.createSpyObj<BenutzerRS>(BenutzerRS.name, ['findBenutzerByEmail']);
    const transitionServiceSpy = jasmine.createSpyObj<Transition>(Transition.name, ['params']);
    const stateServiceSpy = jasmine.createSpyObj<StateService>(StateService.name, ['go']);
    const traegerschaftSpy = jasmine.createSpyObj<TraegerschaftRS>(TraegerschaftRS.name, ['getAllTraegerschaften']);

    beforeEach(async(() => {

        TestBed.configureTestingModule({
            imports: [
                SharedModule,
                NoopAnimationsModule,
            ],
            providers: [
                {provide: Transition, useValue: transitionServiceSpy},
                {provide: StateService, useValue: stateServiceSpy},
                {provide: ErrorService, useValue: errorServiceSpy},
                {provide: InstitutionRS, useValue: insitutionServiceSpy},
                {provide: TraegerschaftRS, useValue: transitionServiceSpy},
                {provide: BenutzerRS, useValue: benutzerServiceSpy},
            ],
            declarations: [
                AddInstitutionComponent,
            ]
        })
            .overrideModule(SharedModule, SHARED_MODULE_OVERRIDES)
            .compileComponents();

        traegerschaftSpy.getAllTraegerschaften.and.returnValue(Promise.resolve([]));
        transitionServiceSpy.params.and.returnValue({institutionId: undefined});
    }));

    beforeEach(async(() => {
        fixture = TestBed.createComponent(AddInstitutionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    }));

    it('should create', async(() => {
        expect(component).toBeTruthy();
    }));
});
